package com.forgelegends.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.forgelegends.data.repository.CustomConceptRepository
import com.forgelegends.data.repository.DataStoreCustomConceptRepository
import com.forgelegends.data.repository.DataStoreGameRepository
import com.forgelegends.data.repository.DataStoreWeaponShowcaseRepository
import com.forgelegends.data.repository.GameRepository
import com.forgelegends.data.repository.WeaponShowcaseRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

private val Context.gameStateDataStore: DataStore<Preferences> by preferencesDataStore(name = "game_state")
private val Context.weaponShowcaseDataStore: DataStore<Preferences> by preferencesDataStore(name = "weapon_showcase")
private val Context.customConceptsDataStore: DataStore<Preferences> by preferencesDataStore(name = "custom_concepts")

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: DataStoreGameRepository): GameRepository

    @Binds
    @Singleton
    abstract fun bindWeaponShowcaseRepository(impl: DataStoreWeaponShowcaseRepository): WeaponShowcaseRepository

    @Binds
    @Singleton
    abstract fun bindCustomConceptRepository(impl: DataStoreCustomConceptRepository): CustomConceptRepository

    companion object {
        @Provides
        @Singleton
        @Named("game_state")
        fun provideGameStateDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.gameStateDataStore
        }

        @Provides
        @Singleton
        @Named("weapon_showcase")
        fun provideWeaponShowcaseDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.weaponShowcaseDataStore
        }

        @Provides
        @Singleton
        @Named("custom_concepts")
        fun provideCustomConceptsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.customConceptsDataStore
        }
    }
}
