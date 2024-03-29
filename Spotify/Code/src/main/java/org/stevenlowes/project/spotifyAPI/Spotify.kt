package org.stevenlowes.project.spotifyAPI

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysis
import com.wrapper.spotify.model_objects.specification.AudioFeatures
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified
import com.wrapper.spotify.model_objects.specification.PlaylistTrack
import com.wrapper.spotify.model_objects.specification.Track
import java.util.*

class Spotify {
    companion object {
        val api: SpotifyApi = SpotifyApi.builder()
                .setClientId(SpotifyAuth.clientId)
                .setClientSecret(SpotifyAuth.clientSecret)
                .setRedirectUri(SpotifyAuth.redirectUri).build()

        private const val bodgePlaylistId = "4IvDNhvA1GDggmipuHqrvZ"

        fun play(id: String, maxLengthMs: Long? = null, onFinish: () -> Unit = {}) {
            var success = false
            var startPlayTime: Long? = null
            while (!success) {
                try {
                    val playlist = api.getPlaylist(bodgePlaylistId).build().execute()
                    api.addTracksToPlaylist(playlist.id, arrayOf("spotify:track:$id")).build().execute()
                    Thread.sleep(500)
                    api.skipUsersPlaybackToNextTrack().build().execute()
                    Thread.sleep(500)
                    api.startResumeUsersPlayback().build().execute()
                    Thread.sleep(500)
                    success = true
                    startPlayTime = System.currentTimeMillis()
                }
                catch (ex: Exception) {
                    println("Exception in spotify play ${ex.message}")
                }
                finally {
                    val playing = api.usersCurrentlyPlayingTrack.build().execute()
                    val playlist = api.getPlaylist(bodgePlaylistId).build().execute()

                    val array = JsonArray()
                    playlist.tracks.items
                            .filter { it.track.id != playing.item.id }
                            .forEach {
                                val track = JsonObject()
                                track.addProperty("uri", "spotify:track:${it.track.id}")
                                array.add(track)
                            }

                    var removeSuccess = false
                    while(!removeSuccess) {
                        try {
                            api.removeTracksFromPlaylist(playlist.id, array).build().execute()
                            removeSuccess = true
                        }
                        catch(ex: Exception){
                        }
                    }
                    Thread.sleep(500)
                }
            }

            val endPlayTime = startPlayTime!! + getTrack(id).durationMs

            val timer = Timer()
            val task = object : TimerTask() {
                override fun run() {
                    var pauseSuccess = false
                    while(!pauseSuccess) {
                        try {
                            api.pauseUsersPlayback().build().execute()
                            pauseSuccess = true
                        }
                        catch(ex: Exception){
                        }
                    }
                    onFinish()
                }
            }

            val now = System.currentTimeMillis()
            val sleepFor = endPlayTime - now

            val actualDelay = Math.min(sleepFor, maxLengthMs ?: Long.MAX_VALUE)

            timer.schedule(task, actualDelay)
        }

        fun seekToPercent(percent: Double) {
            val track = currentlyPlaying()
            api.seekToPositionInCurrentlyPlayingTrack((track.durationMs * percent).toInt()).build().execute()
        }

        fun currentlyPlaying(): Track {
            val currentlyPlaying = api.usersCurrentlyPlayingTrack.build().execute()!!
            return currentlyPlaying.item
        }

        fun getTrack(id: String): Track {
            return api.getTrack(id).build().execute()!!
        }

        fun getAnalysedTrack(id: String): AnalysedTrack {
            val trackFuture = api.getTrack(id).build().executeAsync<Track>()
            val featuresFuture = api.getAudioFeaturesForTrack(id).build().executeAsync<AudioFeatures>()
            val analysisFuture = api.getAudioAnalysisForTrack(id).build().executeAsync<AudioAnalysis>()

            val track = trackFuture.get()
            val features = featuresFuture.get()
            val analysis = analysisFuture.get()

            return AnalysedTrack(track, features, analysis)
        }

        fun getAnalysedTrackFromLink(songLink: String): AnalysedTrack {
            val id = songLink.substringAfterLast("/track/").take(22)
            return getAnalysedTrack(id)
        }

        fun getPlaylists(): List<PlaylistSimplified> {
            val user = api.currentUsersProfile.build().execute()
            return api.getListOfUsersPlaylists(user.id).build().execute().items.toList()
        }

        fun getPlaylist(playlistId: String): List<PlaylistTrack> {
            return api.getPlaylist(playlistId).build().execute().tracks.items.toList()
        }

        fun pause() {
            api.pauseUsersPlayback().build().executeAsync<String>()
        }
    }
}

fun main(args: Array<String>) {
    SpotifyAuth.refreshAuth()
    Spotify.play("73W6szqFleS0znx2Y7V0BZ")
}
