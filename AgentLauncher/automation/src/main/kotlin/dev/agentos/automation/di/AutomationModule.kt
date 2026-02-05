package dev.agentos.automation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.agentos.automation.data.AccessibilityActionExecutor
import dev.agentos.automation.domain.ActionExecutor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AutomationModule {

    @Binds
    @Singleton
    abstract fun bindActionExecutor(
        impl: AccessibilityActionExecutor
    ): ActionExecutor
}
