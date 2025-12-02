package client

import domain.TrackAudioFeatures
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TrackAudioFeaturesApi {
  @GET("audio-features/{id}")
  suspend fun getAudioFeatures(
    @Path("id") id: String
  ): Response<TrackAudioFeatures>
}