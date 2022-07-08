package videoeditor.compressor.video.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes


class AppNotification(
    private val context: Context,
    @DrawableRes private val smallIcon: Int,
    private val id: Int,
    intent: Intent,
) :
    INotification {
    var notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification: Notification.Builder by lazy {
        Notification.Builder(context)
            .setSmallIcon(smallIcon)
            .setContentText("Checking for processes")
            .setContentTitle("Video Compressor")
            .setSound(null, null)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
    }


    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "downloader",
                "Video Downloader notification",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
            notification.setChannelId("downloader")
        }
    }

    override fun show(title: String, message: String) {
        Log.d("TAGTAGTAGTAG", "show: $title $message")
        notificationManager.notify(
            id, notification
                .setContentTitle(title)
                .setOngoing(false)
                .setContentText(message)
                .build()
        )
    }

    fun showSuccessNotification(title: String, outputPath: String) {
        notificationManager.notify(
            id, notification
                .setContentTitle(title)
                .setOngoing(false)
                .setProgress(0, 0, false)
                .setContentText("Completed" + outputPath)
                .build()
        )
    }

    override fun updateProgress(progress: Int) {
        notification.setOngoing(true)
        notification.setProgress(100, progress, false)
        notificationManager.notify(id, notification.build())
    }

    override fun updateProgress(progress: Long, total: Long) {
        notification.setOngoing(true)
        notification.setContentText("TODO()")
        notification.setProgress(100, ((progress / total.toFloat()) * 100).toInt(), false)
        notificationManager.notify(id, notification.build())
    }

    override fun cancel() {
        notificationManager.cancel(id)
    }

    override fun getIntent(): PendingIntent {
        TODO("Not yet implemented")
    }
}


interface INotification {
    fun show(title: String, message: String)
    fun updateProgress(progress: Int)
    fun updateProgress(progress: Long, total: Long)
    fun cancel()
    fun getIntent(): PendingIntent

}