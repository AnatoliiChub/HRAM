package com.achub.hram.di

import android.app.NotificationManager
import android.content.Context
import com.achub.hram.tracking.HramNotificator
import com.achub.hram.tracking.Notificator
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
    ): Notificator = HramNotificator(ctx = ctx, notificationManager = notificationManager)

    @Single
    fun provideNotificationManager(ctx: Context): NotificationManager {
        return ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}
