package client

import okhttp3.Interceptor
import okhttp3.Response

object AuthorizationInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val accessToken = SpotifyAuthManager.getAccessToken()
    val requestWithHeader = chain.request()
      .newBuilder()
      .header("Authorization", "Bearer $accessToken")
      .build()
    return chain.proceed(requestWithHeader)
  }
}