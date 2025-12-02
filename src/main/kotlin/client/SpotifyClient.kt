package client

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object SpotifyClient {

  private const val BASE_URL = "https://api.spotify.com/v1/"

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  val okHttpClient = OkHttpClient()
    .newBuilder()
    .addInterceptor(AuthorizationInterceptor)
    .addInterceptor(RequestInterceptor)
    .build()

  fun getClient(): Retrofit =
    Retrofit.Builder()
      .client(okHttpClient)
      .baseUrl(BASE_URL)
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()
}