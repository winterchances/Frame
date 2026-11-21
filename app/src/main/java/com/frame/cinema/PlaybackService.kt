package com.frame.cinema

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures

/** Owns playback independently of the Activity so audio can continue in the background. */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var playbackRepository: NativeLibraryRepository
    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressSaveRunnable = object : Runnable {
        override fun run() {
            mediaSession?.player?.let(::savePlaybackProgress)
            progressHandler.postDelayed(this, 5_000L)
        }
    }

    private fun savePlaybackProgress(player: Player) {
        val mediaId = player.currentMediaItem?.mediaId
        val videoId = mediaId?.toLongOrNull() ?: return
        if (player.playbackState != Player.STATE_READY &&
            player.playbackState != Player.STATE_ENDED
        ) return

        val positionMs = player.currentPosition.coerceAtLeast(0L)
        val durationMs = player.duration.takeIf {
            it > 0L && it != androidx.media3.common.C.TIME_UNSET
        } ?: playbackRepository.backupVideos()
            .firstOrNull { it.id == videoId }?.durationMs ?: 0L

        if (positionMs > 0L || player.playbackState == Player.STATE_ENDED) {
            playbackRepository.updatePlayback(
                videoId,
                positionMs,
                durationMs,
                player.playbackState == Player.STATE_ENDED
            )
        }
    }

    private val rewindCommand = SessionCommand("com.frame.cinema.REWIND_15", Bundle.EMPTY)
    private val forwardCommand = SessionCommand("com.frame.cinema.FORWARD_15", Bundle.EMPTY)
    private val closeCommand = SessionCommand("com.frame.cinema.CLOSE_PLAYBACK", Bundle.EMPTY)

    override fun onCreate() {
        super.onCreate()
        playbackRepository = NativeLibraryRepository(this)
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this).build()
        notificationProvider.setSmallIcon(R.drawable.ic_notification)
        setMediaNotificationProvider(notificationProvider)

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(androidx.media3.common.AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) savePlaybackProgress(player)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                    savePlaybackProgress(player)
                }
            }
        })
        progressHandler.postDelayed(progressSaveRunnable, 5_000L)

        val rewindButton = CommandButton.Builder(CommandButton.ICON_SKIP_BACK_15)
            .setDisplayName("快退 15 秒")
            .setSessionCommand(rewindCommand)
            .setSlots(CommandButton.SLOT_BACK_SECONDARY)
            .build()
        val forwardButton = CommandButton.Builder(CommandButton.ICON_SKIP_FORWARD_15)
            .setDisplayName("快进 15 秒")
            .setSessionCommand(forwardCommand)
            .setSlots(CommandButton.SLOT_FORWARD)
            .build()
        val closeButton = CommandButton.Builder(CommandButton.ICON_UNDEFINED)
            .setCustomIconResId(R.drawable.ic_media_close)
            .setDisplayName("关闭")
            .setSessionCommand(closeCommand)
            .setSlots(CommandButton.SLOT_FORWARD_SECONDARY)
            .build()

        val mediaButtons = com.google.common.collect.ImmutableList.of(
            rewindButton,
            forwardButton,
            closeButton
        )

        val availableSessionCommands =
            MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(rewindCommand)
                .add(forwardCommand)
                .add(closeCommand)
                .build()

        val openPlayerIntent = Intent(this, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_PLAYER
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val sessionActivity = PendingIntent.getActivity(
            this,
            1001,
            openPlayerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivity)
            .setMediaButtonPreferences(mediaButtons)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(availableSessionCommands)
                        .setAvailablePlayerCommands(
                            MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                                .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                                .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                                .build()
                        )
                        .setMediaButtonPreferences(mediaButtons)
                        .build()
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): com.google.common.util.concurrent.ListenableFuture<SessionResult> {
                    when (customCommand.customAction) {
                        rewindCommand.customAction -> {
                            val currentPosition = player.currentPosition
                            player.seekTo((currentPosition - 15_000L).coerceAtLeast(0L))
                        }
                        forwardCommand.customAction -> {
                            val currentPosition = player.currentPosition
                            val targetPosition = currentPosition + 15_000L
                            val duration = player.duration
                            player.seekTo(if (duration != androidx.media3.common.C.TIME_UNSET) targetPosition.coerceAtMost(duration) else targetPosition)
                        }
                        closeCommand.customAction -> {
                            savePlaybackProgress(player)
                            player.stop()
                            player.clearMediaItems()
                            stopSelf()
                        }
                        else -> return super.onCustomCommand(
                            session,
                            controller,
                            customCommand,
                            args
                        )
                    }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            })
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        progressHandler.removeCallbacks(progressSaveRunnable)
        mediaSession?.let { session ->
            savePlaybackProgress(session.player)
            session.player.release()
            session.release()
        }
        if (::playbackRepository.isInitialized) playbackRepository.close()
        mediaSession = null
        super.onDestroy()
    }
}
