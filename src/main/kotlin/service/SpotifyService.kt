package service

import client.SpotifyClient
import client.TopTracksApi
import client.TrackAudioFeaturesApi
import domain.TopTrackResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class SpotifyService {
  private val retrofit = SpotifyClient.getClient()
  private val userTopTracksApi = retrofit.create(TopTracksApi::class.java)
  private val trackAudioFeaturesApi = retrofit.create(TrackAudioFeaturesApi::class.java)
  private val logger = KotlinLogging.logger {}

  suspend fun handleTopTracks(): TopTrackResponse {
    val topTracksResponse = userTopTracksApi
      .getTopTracks(50, "long_term", 0)

    val successful = topTracksResponse.isSuccessful
    val httpStatusCode = topTracksResponse.code()
    val httpStatusMessage = topTracksResponse.message()

    val body = topTracksResponse.body()

    val errorBody: ResponseBody? = topTracksResponse.errorBody()

    body?.also {
      logger.info { "Top tracks: $it" }
    }

    return body!!
  }

  suspend fun getAllTrackInfo(topTracks: TopTrackResponse) = withContext(Dispatchers.IO.limitedParallelism(32)) {
    val trackResponses = topTracks.items.map { track ->
      trackAudioFeaturesApi.getAudioFeatures(track.id)
      delay(50)
    }

    logger.info { trackResponses }
  }
}