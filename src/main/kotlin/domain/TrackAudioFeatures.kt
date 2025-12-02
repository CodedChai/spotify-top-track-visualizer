package domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackAudioFeatures(
  val acousticness: Float,
  val danceability: Float,
  val energy: Float,
  @SerialName("duration_ms")
  val durationMs: Int,
  val liveness: Int,
  val valence: Int,
  @SerialName("track_href")
  val trackHref: String,
  val tempo: Float,
  val uri: String,
  val id: String,
)
