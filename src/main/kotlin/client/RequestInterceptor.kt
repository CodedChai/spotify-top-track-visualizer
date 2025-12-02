package client

import okhttp3.Interceptor
import okhttp3.Response

object RequestInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    println("Outgoing request to ${request.url}")

    return chain.proceed(request).also { response ->
      println("Incoming response from ${request.url} is $response")
    }
  }
}