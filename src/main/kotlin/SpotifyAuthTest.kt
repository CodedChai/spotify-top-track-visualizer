import client.SpotifyAuthManager
import io.github.oshai.kotlinlogging.KotlinLogging
import service.SpotifyService

private val logger = KotlinLogging.logger {}

suspend fun main() {
  try {
    logger.info { "=== Spotify Authentication Test ===" }

    logger.info { "Authenticating with Spotify..." }
    val accessToken = SpotifyAuthManager.getAccessToken()
    logger.info { "Successfully authenticated! Token: ${accessToken.take(20)}..." }

    logger.info { "Fetching your top tracks..." }
    val spotifyService = SpotifyService()
    val topTrackResponse = spotifyService.handleTopTracks()
    spotifyService.getAllTrackInfo(topTrackResponse)

    logger.info { "=== Test Complete ===" }

  } catch (e: Exception) {
    logger.error(e) { "Authentication or API call failed" }
    throw e
  }
}

