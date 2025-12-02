package client

import com.sun.net.httpserver.HttpServer
import domain.TokenResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Desktop
import java.io.File
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLEncoder
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

object SpotifyAuthManager {
  private const val AUTH_URL = "https://accounts.spotify.com/authorize"
  private const val TOKEN_URL = "https://accounts.spotify.com/api/token"
  private const val TOKEN_FILE = "spotify_token.json"

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  @Volatile
  private var currentToken: TokenResponse? = null

  @Volatile
  private var tokenExpiryTime: Instant? = null

  @Volatile
  private var isAuthenticating = false


  fun getAccessToken(): String {
    if (currentToken == null) {
      loadTokenFromFile()
    }

    val now = Instant.now()
    if (currentToken != null && tokenExpiryTime != null) {
      if (now.plusSeconds(60).isAfter(tokenExpiryTime)) {
        logger.info { "Token expired or expiring soon, refreshing..." }
        refreshAccessToken()
      }
    } else {
      if (!isAuthenticating) {
        authenticate()
      }
    }

    return currentToken?.accessToken
      ?: throw IllegalStateException("No access token available. Authentication may have failed.")
  }

  fun authenticate() {
    if (isAuthenticating) {
      logger.warn { "Authentication already in progress" }
      return
    }

    isAuthenticating = true
    try {
      logger.info { "Starting Spotify OAuth authentication..." }

      val redirectUri = SpotifyAuthConfig.redirectUri
      val uri = URI(redirectUri)
      val port = if (uri.port > 0) uri.port else 8000
      val callbackPath = uri.path ?: "/callback"

      val latch = CountDownLatch(1)
      var authorizationCode: String? = null
      var error: String? = null

      val server = HttpServer.create(InetSocketAddress(port), 0)
      server.createContext(callbackPath) { exchange ->
        val query = exchange.requestURI.query
        val params = query?.split("&")?.associate {
          val (key, value) = it.split("=")
          key to value
        } ?: emptyMap()

        authorizationCode = params["code"]
        error = params["error"]

        val response = if (authorizationCode != null) {
          "Authentication successful! You can close this window and return to the application."
        } else {
          "Authentication failed: ${error ?: "unknown error"}. You can close this window."
        }

        exchange.sendResponseHeaders(200, response.length.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }

        latch.countDown()
      }

      server.start()
      logger.info { "Local server started on port $port" }

      try {
        val authUrl = buildAuthorizationUrl()
        logger.info { "Opening browser for authorization..." }
        logger.info { "If browser doesn't open, visit: $authUrl" }

        if (Desktop.isDesktopSupported()) {
          Desktop.getDesktop().browse(URI(authUrl))
        }

        val received = latch.await(2, TimeUnit.MINUTES)

        if (!received) {
          throw IllegalStateException("Authentication timeout - no response received within 2 minutes")
        }

        if (error != null) {
          throw IllegalStateException("Authentication error: $error")
        }

        if (authorizationCode == null) {
          throw IllegalStateException("No authorization code received")
        }

        exchangeCodeForToken(authorizationCode!!)
        logger.info { "Authentication successful!" }

      } finally {
        server.stop(0)
        logger.info { "Local server stopped" }
      }
    } finally {
      isAuthenticating = false
    }
  }

  private fun buildAuthorizationUrl(): String {
    val params = mapOf(
      "client_id" to SpotifyAuthConfig.clientId,
      "response_type" to "code",
      "redirect_uri" to SpotifyAuthConfig.redirectUri,
      "scope" to SpotifyAuthConfig.scopes
    )

    val queryString = params.entries.joinToString("&") { (key, value) ->
      "$key=${URLEncoder.encode(value, "UTF-8")}"
    }

    return "$AUTH_URL?$queryString"
  }

  private fun exchangeCodeForToken(code: String) {
    logger.info { "Exchanging authorization code for access token..." }

    val requestBody = FormBody.Builder()
      .add("grant_type", "authorization_code")
      .add("code", code)
      .add("redirect_uri", SpotifyAuthConfig.redirectUri)
      .build()

    val credentials = "${SpotifyAuthConfig.clientId}:${SpotifyAuthConfig.clientSecret}"
    val base64Credentials = Base64.getEncoder().encodeToString(credentials.toByteArray())

    val request = Request.Builder()
      .url(TOKEN_URL)
      .post(requestBody)
      .header("Authorization", "Basic $base64Credentials")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .build()

    httpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        throw IllegalStateException("Token exchange failed: ${response.code} - ${response.body?.string()}")
      }

      val responseBody = response.body?.string()
        ?: throw IllegalStateException("Empty response body")

      currentToken = json.decodeFromString<TokenResponse>(responseBody)
      tokenExpiryTime = Instant.now().plusSeconds(currentToken!!.expiresIn.toLong())

      saveTokenToFile()
      logger.info { "Access token obtained and saved" }
    }
  }

  private fun refreshAccessToken() {
    val refreshToken = currentToken?.refreshToken
      ?: throw IllegalStateException("No refresh token available")

    logger.info { "Refreshing access token..." }

    val requestBody = FormBody.Builder()
      .add("grant_type", "refresh_token")
      .add("refresh_token", refreshToken)
      .build()

    val credentials = "${SpotifyAuthConfig.clientId}:${SpotifyAuthConfig.clientSecret}"
    val base64Credentials = Base64.getEncoder().encodeToString(credentials.toByteArray())

    val request = Request.Builder()
      .url(TOKEN_URL)
      .post(requestBody)
      .header("Authorization", "Basic $base64Credentials")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .build()

    httpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        logger.error { "Token refresh failed: ${response.code} - ${response.body?.string()}" }

        currentToken = null
        tokenExpiryTime = null
        deleteTokenFile()
        authenticate()
        return
      }

      val responseBody = response.body?.string()
        ?: throw IllegalStateException("Empty response body")

      val newToken = json.decodeFromString<TokenResponse>(responseBody)

      currentToken = if (newToken.refreshToken == null) {
        newToken.copy(refreshToken = refreshToken)
      } else {
        newToken
      }

      tokenExpiryTime = Instant.now().plusSeconds(currentToken!!.expiresIn.toLong())

      saveTokenToFile()
      logger.info { "Access token refreshed" }
    }
  }

  private fun saveTokenToFile() {
    try {
      val tokenData = json.encodeToString(TokenResponse.serializer(), currentToken!!)
      val expiryData = tokenExpiryTime?.toString() ?: ""
      val fileContent = "$tokenData\n$expiryData"

      val tokenFile = File(TOKEN_FILE)
      tokenFile.writeText(fileContent)

      logger.debug { "Token saved to file" }
    } catch (e: Exception) {
      logger.warn(e) { "Failed to save token to file" }
    }
  }

  private fun loadTokenFromFile() {
    try {
      val file = File(TOKEN_FILE)
      if (!file.exists()) {
        logger.debug { "No saved token file found" }
        return
      }

      val lines = file.readLines()
      if (lines.isEmpty()) {
        logger.warn { "Token file is empty" }
        return
      }

      currentToken = json.decodeFromString<TokenResponse>(lines[0])
      tokenExpiryTime = if (lines.size > 1 && lines[1].isNotBlank()) {
        Instant.parse(lines[1])
      } else {
        Instant.now().minusSeconds(3600)
      }

      logger.info { "Token loaded from file" }
    } catch (e: Exception) {
      logger.warn(e) { "Failed to load token from file" }
      currentToken = null
      tokenExpiryTime = null
    }
  }

  private fun deleteTokenFile() {
    try {
      val file = File(TOKEN_FILE)
      if (file.exists()) {
        file.delete()
        logger.debug { "Token file deleted" }
      }
    } catch (e: Exception) {
      logger.warn(e) { "Failed to delete token file" }
    }
  }

  fun logout() {
    currentToken = null
    tokenExpiryTime = null
    deleteTokenFile()
    logger.info { "Logged out - token cleared" }
  }
}

