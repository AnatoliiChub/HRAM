package com.achub.hram.di

import android.app.NotificationManager
import android.content.Context
import com.achub.hram.tracking.ActivityTrackingManager
import com.achub.hram.tracking.HramNotificator
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class NotificationModule {
    @Single
    fun provideHramNotificator(
        ctx: Context,
        notificationManager: NotificationManager,
        trackingManager: ActivityTrackingManager,
    ): HramNotificator {
        return HramNotificator(
            ctx = ctx,
            notificationManager = notificationManager,
            trackingManager = trackingManager,
        )
    }

    @Single
    fun provideNotificationManager(ctx: Context): NotificationManager {
        return ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}
