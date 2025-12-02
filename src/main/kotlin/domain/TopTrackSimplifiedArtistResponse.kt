package domain

import kotlinx.serialization.Serializable

@Serializable
data class TopTrackSimplifiedArtistResponse(
  val id: String,
  val name: String,
  val uri: String,
)
