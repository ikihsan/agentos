package dev.agentos.launcher

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.agentos.core.common.AgentLogger

/**
 * Main Application class for Agent OS Launcher.
 */
@HiltAndroidApp
class AgentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        AgentLogger.i(AgentLogger.TAG_MAIN, "Agent OS Launcher starting...")
        
        // Initialize any app-wide services here
        initializeServices()
    }

    private fun initializeServices() {
        // Future: Initialize crash reporting, analytics, etc.
        AgentLogger.i(AgentLogger.TAG_MAIN, "Services initialized")
    }
}
