package domain

import kotlinx.serialization.Serializable

@Serializable
data class TopTrackAlbumImageResponse(
  val url: String,
  val height: Int,
  val width: Int,
)
