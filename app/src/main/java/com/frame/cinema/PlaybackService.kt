package com.frame.cinema

import androidx.media3.common.Player
import androidx.media3.session.CommandButton
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/** Owns playback independently of the Activity so audio can continue in the background. */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this).build()
        notificationProvider.setSmallIcon(R.drawable.ic_notification)
        setMediaNotificationProvider(notificationProvider)

        val player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(15_000L)
            .setSeekForwardIncrementMs(15_000L)
            .build()
        val rewindButton = CommandButton.Builder(CommandButton.ICON_SKIP_BACK_15)
            .setPlayerCommand(Player.COMMAND_SEEK_BACK)
            .setSlots(CommandButton.SLOT_BACK)
            .build()
        val forwardButton = CommandButton.Builder(CommandButton.ICON_SKIP_FORWARD_15)
            .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
            .setSlots(CommandButton.SLOT_FORWARD)
            .build()
        val closeButton = CommandButton.Builder(CommandButton.ICON_UNDEFINED)
            .setCustomIconResId(R.drawable.ic_media_close)
            .setDisplayName("关闭")
            .setPlayerCommand(Player.COMMAND_STOP)
            .setSlots(CommandButton.SLOT_OVERFLOW)
            .build()

        val mediaButtons = com.google.common.collect.ImmutableList.of(
            rewindButton,
            forwardButton,
            closeButton
        )

        mediaSession = MediaSession.Builder(this, player)
            .setMediaButtonPreferences(mediaButtons)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                        .buildUpon()
                        .add(Player.COMMAND_SEEK_BACK)
                        .add(Player.COMMAND_SEEK_FORWARD)
                        .add(Player.COMMAND_STOP)
                        .build()

                    if (session.isMediaNotificationController(controller)) {
                        return MediaSession.ConnectionResult.AcceptedResultBuilder(session, controller)
                            .setAvailablePlayerCommands(playerCommands)
                            .setMediaButtonPreferences(mediaButtons)
                            .build()
                    }

                    return MediaSession.ConnectionResult.accept(
                        MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS,
                        playerCommands
                    )
                }
            })
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
