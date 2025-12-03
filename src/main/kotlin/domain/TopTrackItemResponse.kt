package domain

import kotlinx.serialization.Serializable

@Serializable
data class TopTrackItemResponse(
  val name: String,
  val popularity: Long,
  val type: String,
  val uri: String,
  val id: String,
  val href: String,
  val artists: List<TopTrackSimplifiedArtistResponse>,
  val album: TopTrackAlbumResponse,
)