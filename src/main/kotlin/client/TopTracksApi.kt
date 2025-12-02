package client

import domain.TopTrackResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TopTracksApi {
  @GET("me/top/tracks")
  suspend fun getTopTracks(
    @Query("limit") limit: Int,
    @Query("time_range") timeRange: String,
    @Query("offset") offset: Int = 0,
  ): Call<List<TopTrackResponse>>
}