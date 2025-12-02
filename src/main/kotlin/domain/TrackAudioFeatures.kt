package domain

import kotlinx.serialization.Serializable

@Serializable
data class TrackAudioFeatures(
  val acousticness: Float,
  val danceability: Float,
  val energy: Float,
  val durationMs: Int,
  val liveness: Int,
  val valence: Int,
  val trackHref: String,
  val tempo: Float,
  val uri: String,
  val id: String,
)
