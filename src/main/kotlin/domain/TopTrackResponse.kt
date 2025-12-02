package domain

import kotlinx.serialization.Serializable

@Serializable
data class TopTrackResponse(
  val total: Int,
  val items: List<TopTrackItemResponse>,
)
