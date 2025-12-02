package client

import java.io.File
import java.util.*

object SpotifyAuthConfig {
  private val properties = Properties()

  init {
    val propertiesFile = File("spotify.properties")
    if (propertiesFile.exists()) {
      propertiesFile.inputStream().use { properties.load(it) }
    }
  }

  val clientId: String
    get() = System.getenv("SPOTIFY_CLIENT_ID")
      ?: properties.getProperty("spotify.client.id")
      ?: throw IllegalStateException("SPOTIFY_CLIENT_ID not set. Set environment variable or add to spotify.properties file")

  val clientSecret: String
    get() = System.getenv("SPOTIFY_CLIENT_SECRET")
      ?: properties.getProperty("spotify.client.secret")
      ?: throw IllegalStateException("SPOTIFY_CLIENT_SECRET not set. Set environment variable or add to spotify.properties file")

  val redirectUri: String
    get() = System.getenv("SPOTIFY_REDIRECT_URI")
      ?: properties.getProperty("spotify.redirect.uri")
      ?: "http://127.0.0.1:8000/callback"

  val scopes: String
    get() = System.getenv("SPOTIFY_SCOPES")
      ?: properties.getProperty("spotify.scopes")
      ?: "user-top-read user-read-private user-read-email"
}

