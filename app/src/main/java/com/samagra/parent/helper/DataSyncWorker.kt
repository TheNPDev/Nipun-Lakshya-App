package com.samagra.parent.helper

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.data.db.DbHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.constants.Constants
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.PostHogManager.capture
import com.samagra.commons.posthog.PostHogManager.createContext
import com.samagra.commons.posthog.PostHogManager.createProperties
import com.samagra.commons.posthog.data.Cdata
import com.samagra.parent.BuildConfig
import com.samagra.parent.R
import com.samagra.parent.ui.splash.SplashActivity
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class DataSyncWorker(
    private val context: Context, params: WorkerParameters
) : Worker(context, params) {

    @SuppressLint("BinaryOperationInTimber")
    override fun doWork(): Result {
        Timber.d("doWork: ")
        val prefs = CommonsPrefsHelperImpl(context, "prefs")
        if (prefs.isLoggedIn.not())
            return Result.success()
        val helper = SyncingHelper()
        val submissions = DbHelper.db.getAssessmentSubmissionDao().getSubmissions()
        showTestNotification(
            isCompleted = false,
            message = "Syncing started -, Student Submissions :${submissions.size}"
        )
        showStatusNotification(
            isCompleted = false,
            message = "Assessments Submission Syncing",
            isSuccess = false
        )
        val isSuccessSubmissions = helper.syncSubmissions(prefs, submissions)
        this.showTestNotification(
            isCompleted = false,
            message = "Syncing in Progress - " +
                    "\nStudent Submissions : ${submissions.size} status - $isSuccessSubmissions"
        )
        this.showTestNotification(
            true,
            "Syncing stopped - "  +

                    "\nStudent Submissions : ${submissions.size} status - $isSuccessSubmissions"
        )
        val schoolSubmissions = DbHelper.db.getSchoolSubmissionDao().getSubmissions()
        val isSchoolSubmissionSuccess = helper.syncSchoolSubmission(prefs, schoolSubmissions)
        this.showTestNotification(
            true,
            "Syncing stopped - " +
                    "\nStudent Submissions : ${submissions.size} status - $isSuccessSubmissions" +
                    "\nSchool Submissions : ${schoolSubmissions.size} status - $isSchoolSubmissionSuccess"
        )
        prefs.markDataSynced()
        return Result.success()
    }

    private fun showStatusNotification(isCompleted: Boolean, message: String?, isSuccess: Boolean) {
        if (BuildConfig.DEBUG.not()) {
            return
        }
        val notificationChannel = Constants.NL_SYNCING_NOTIFICATION_CHANNEL
        val title = context.getString(R.string.app_name) + " Submissions Syncing"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannel,
                title,
                if (isCompleted) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "YOUR_NOTIFICATION_CHANNEL_DESCRIPTION"
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext, notificationChannel
        )
            .setSmallIcon(R.mipmap.ic_launcher_mpp)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setOngoing(!isCompleted)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle(title) // title for notification
            .setContentText(message) // message for notification;
        if (!isCompleted || !isSuccess) {
            mBuilder.setNotificationSilent()
        }
        mBuilder.setProgress(0, 0, !isCompleted)
        val intent = Intent(
            applicationContext,
            SplashActivity::class.java
        )
        val pi = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        mBuilder.setContentIntent(pi)

        val notificationManager =
            context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            title.hashCode(),
            mBuilder.build()
        )
    }

    private fun showTestNotification(isCompleted: Boolean, message: String) {
        if (!BuildConfig.DEBUG) {
            return
        }
        val notificationChannel = Constants.NL_SYNCING_NOTIFICATION_CHANNEL
        val sdf = SimpleDateFormat("dd-MM-yyyy - HH:mm:ss. SSS", Locale.getDefault())
        val title = "Syncing Worker " + sdf.format(Date())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannel,
                title,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Syncing Service"
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext, notificationChannel
        )
            .setSmallIcon(R.mipmap.ic_launcher_mpp)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle(title) // title for notification
            .setContentText(message) // message for notification;
        val intent = Intent(
            applicationContext,
            SplashActivity::class.java
        )
        val pi = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        mBuilder.setContentIntent(pi)

        val notificationManager =
            context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            title.hashCode(),
            mBuilder.build()
        )
    }
}