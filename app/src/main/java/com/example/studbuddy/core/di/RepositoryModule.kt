package com.example.studbuddy.core.di

import android.content.Context
import com.example.studbuddy.core.SettingsManager
import com.example.studbuddy.core.UserManager
import com.example.studbuddy.core.db.StudBuddyDatabase
import com.example.studbuddy.core.repository.StudBuddyRepository
import com.example.studbuddy.core.repository.DriveBackupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideStudBuddyRepository(database: StudBuddyDatabase): StudBuddyRepository {
        return StudBuddyRepository(database)
    }

    @Provides
    @Singleton
    fun provideDriveBackupRepository(
        @ApplicationContext context: Context,
        database: StudBuddyDatabase,
        settingsManager: SettingsManager
    ): DriveBackupRepository {
        return DriveBackupRepository(context, database, settingsManager)
    }

    @Provides
    @Singleton
    fun provideUserManager(@ApplicationContext context: Context): UserManager {
        return UserManager(context)
    }

    @Provides
    @Singleton
    fun provideSettingsManager(@ApplicationContext context: Context): SettingsManager {
        return SettingsManager(context)
    }
}
