package com.seif.distancetrackerapp.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.seif.distancetrackerapp.MainActivity
import com.seif.distancetrackerapp.R
import com.seif.distancetrackerapp.util.Constants.ACTION_NAVIGATE_TO_MAPS_FRAGMENT
import com.seif.distancetrackerapp.util.Constants.NOTIFICATION_CHANNEL_ID
import com.seif.distancetrackerapp.util.Constants.PENDING_INTENT_REQUEST_CODE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {

    @ServiceScoped
    @Provides
    fun providePendingIntent(
        @ApplicationContext context: Context
    ): PendingIntent {
        return PendingIntent.getActivity(
            context,
            PENDING_INTENT_REQUEST_CODE,
            Intent(context, MainActivity::class.java).apply {
                this.action = ACTION_NAVIGATE_TO_MAPS_FRAGMENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) // prevent cancelling notification if user click on notification panel
            .setOngoing(false)
            .setSmallIcon(R.drawable.ic_run)
            .setContentIntent(pendingIntent) // so when user click on notification then our maps fragment will opened
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}

// setOngoing(): (are sorted above regular notification in notification panel and it doesn't have an 'X' close button and not affected by 'clearAll' button
// Ongoing notifications cannot be dismissed by the user, so your application
// or service must take care of canceling them. They are typically used to indicate a background task
// that the user is actively engaged with (e.g., playing music) or is pending in some way and therefore occupying the device (e.g., a file download, sync operation, active network connection).