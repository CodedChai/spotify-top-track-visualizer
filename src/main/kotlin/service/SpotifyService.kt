package service

import client.SpotifyClient
import client.TopTracksApi
import okhttp3.ResponseBody

class SpotifyService {
  private val retrofit = SpotifyClient.getClient()
  private val userTopTracksApi = retrofit.create(TopTracksApi::class.java)

  fun handleTopTracks() {
    val topTracksResponse = userTopTracksApi
      .getTopTracks(50, "long_term", 0)
      .execute()

    val successful = topTracksResponse.isSuccessful
    val httpStatusCode = topTracksResponse.code()
    val httpStatusMessage = topTracksResponse.message()

    val body = topTracksResponse.body()

    val errorBody: ResponseBody? = topTracksResponse.errorBody()


  }
}