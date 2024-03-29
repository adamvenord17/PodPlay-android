/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.avenord.podplay.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.avenord.podplay.R
import com.avenord.podplay.service.PodplayMediaCallback.PodplayMediaListener
import com.avenord.podplay.ui.PodcastActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

class PodplayMediaService : MediaBrowserServiceCompat(), PodplayMediaListener {

  private lateinit var mediaSession: MediaSessionCompat

  override fun onStateChanged() {
    displayNotification()
  }

  override fun onStopPlaying() {
    stopSelf()
    stopForeground(true)
  }

  override fun onPausePlaying() {
    stopForeground(false)
  }

  override fun onCreate() {
    super.onCreate()
    createMediaSession()
  }

  override fun onTaskRemoved(rootIntent: Intent?) {
    super.onTaskRemoved(rootIntent)
    mediaSession.controller.transportControls.stop()
  }

  override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    if (parentId.equals(PODPLAY_EMPTY_ROOT_MEDIA_ID)) {
      result.sendResult(null)
    }
  }
  override fun onGetRoot(clientPackageName: String,
                         clientUid: Int, rootHints: Bundle?): BrowserRoot? {
    return MediaBrowserServiceCompat.BrowserRoot(
        PODPLAY_EMPTY_ROOT_MEDIA_ID, null)
  }

  private fun createMediaSession() {

    mediaSession = MediaSessionCompat(this, "PodplayMediaService")

    mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

    setSessionToken(mediaSession.sessionToken)

    val callBack = PodplayMediaCallback(this, mediaSession)
    callBack.listener = this
    mediaSession.setCallback(callBack)
  }

  private fun getPausePlayActions():
      Pair<NotificationCompat.Action, NotificationCompat.Action>  {
    val pauseAction = NotificationCompat.Action(
        R.drawable.ic_pause_white, getString(R.string.pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
            PlaybackStateCompat.ACTION_PAUSE))
    val playAction = NotificationCompat.Action(
        R.drawable.ic_play_arrow_white, getString(R.string.play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
            PlaybackStateCompat.ACTION_PLAY))
    return Pair(pauseAction, playAction)
  }

  private fun isPlaying(): Boolean {
    if (mediaSession.controller.playbackState != null) {
      return mediaSession.controller.playbackState.state ==
          PlaybackStateCompat.STATE_PLAYING
    } else {
      return false
    }
  }

  private fun getNotificationIntent(): PendingIntent {
    val openActivityIntent = Intent(this, PodcastActivity::class.java)
    openActivityIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    return PendingIntent.getActivity(
        this@PodplayMediaService, 0, openActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel() {
    val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (notificationManager.getNotificationChannel(PLAYER_CHANNEL_ID) == null) {
      val channel = NotificationChannel(PLAYER_CHANNEL_ID, "Player",
          NotificationManager.IMPORTANCE_LOW)
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun createNotification(mediaDescription: MediaDescriptionCompat,
                                 bitmap: Bitmap?): Notification {

    val notificationIntent = getNotificationIntent()
    val (pauseAction, playAction) = getPausePlayActions()
    val notification = NotificationCompat.Builder(
        this@PodplayMediaService, PLAYER_CHANNEL_ID)

    notification
        .setContentTitle(mediaDescription.title)
        .setContentText(mediaDescription.subtitle)
        .setLargeIcon(bitmap)
        .setContentIntent(notificationIntent)
        .setDeleteIntent(
            MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                PlaybackStateCompat.ACTION_STOP))
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setSmallIcon(R.drawable.ic_episode_icon)
        .addAction(if (isPlaying()) pauseAction else playAction)
        .setStyle(
            android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP)))
    return notification.build()
  }

  private fun displayNotification() {

    if (mediaSession.controller.metadata == null) {
      return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createNotificationChannel()
    }

    val mediaDescription = mediaSession.controller.metadata.description

    GlobalScope.launch {
      val iconUrl = URL(mediaDescription.iconUri.toString())
      val bitmap = BitmapFactory.decodeStream(iconUrl.openStream())
      val notification = createNotification(mediaDescription, bitmap)
      ContextCompat.startForegroundService(
          this@PodplayMediaService,
          Intent(this@PodplayMediaService, PodplayMediaService::class.java))
      startForeground(PodplayMediaService.NOTIFICATION_ID, notification)
    }
  }

  companion object {
    private const val PODPLAY_EMPTY_ROOT_MEDIA_ID = "podplay_empty_root_media_id"
    private const val PLAYER_CHANNEL_ID = "podplay_player_channel"
    private const val NOTIFICATION_ID = 1
  }
}
