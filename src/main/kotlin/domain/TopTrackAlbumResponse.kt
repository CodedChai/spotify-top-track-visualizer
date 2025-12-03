package domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopTrackAlbumResponse(
  @SerialName("album_type")
  val albumType: String,
  val name: String,
  @SerialName("release_date")
  val releaseDate: String,
  @SerialName("release_date_precision")
  val releaseDatePrecision: String,
  val images: List<TopTrackAlbumImageResponse>,
)
