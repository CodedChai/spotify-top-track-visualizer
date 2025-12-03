package service

import client.SpotifyClient
import client.TopTracksApi
import domain.TopTrackResponse
import io.github.oshai.kotlinlogging.KotlinLogging

class SpotifyService {
  private val retrofit = SpotifyClient.getClient()
  private val userTopTracksApi = retrofit.create(TopTracksApi::class.java)
  private val logger = KotlinLogging.logger {}

  suspend fun handleTopTracks(): TopTrackResponse {
    val topTracksResponse = userTopTracksApi
      .getTopTracks(50, "long_term", 0)

    topTracksResponse.isSuccessful
    topTracksResponse.code()
    topTracksResponse.message()

    val body = topTracksResponse.body()

    topTracksResponse.errorBody()

    body?.also {
      logger.info { "Top tracks: $it" }
    }

    return body!!
  }
}