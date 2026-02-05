package dev.agentos.automation.domain

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.agentos.core.common.AgentLogger
import dev.agentos.core.common.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Launches apps and activities.
 */
@Singleton
class AppLauncher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager

    /**
     * Launch an app by package name.
     */
    fun launchApp(packageName: String): Result<Unit> {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                AgentLogger.i(AgentLogger.TAG_AUTOMATION, "Launched app: $packageName")
                Result.success(Unit)
            } else {
                Result.error("App not found: $packageName")
            }
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_AUTOMATION, "Failed to launch app: $packageName", e)
            Result.error(e)
        }
    }

    /**
     * Launch an activity with intent.
     */
    fun launchIntent(intent: Intent): Result<Unit> {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            AgentLogger.i(AgentLogger.TAG_AUTOMATION, "Launched intent: ${intent.action}")
            Result.success(Unit)
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_AUTOMATION, "Failed to launch intent", e)
            Result.error(e)
        }
    }

    /**
     * Open a URL in the default browser.
     */
    fun openUrl(url: String): Result<Unit> {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            AgentLogger.i(AgentLogger.TAG_AUTOMATION, "Opened URL: $url")
            Result.success(Unit)
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_AUTOMATION, "Failed to open URL: $url", e)
            Result.error(e)
        }
    }

    /**
     * Start a phone call.
     */
    fun makeCall(phoneNumber: String): Result<Unit> {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            AgentLogger.i(AgentLogger.TAG_AUTOMATION, "Dialing: $phoneNumber")
            Result.success(Unit)
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_AUTOMATION, "Failed to dial: $phoneNumber", e)
            Result.error(e)
        }
    }

    /**
     * Open SMS app with pre-filled recipient.
     */
    fun sendSms(phoneNumber: String, message: String? = null): Result<Unit> {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                message?.let { putExtra("sms_body", it) }
            }
            context.startActivity(intent)
            AgentLogger.i(AgentLogger.TAG_AUTOMATION, "Opening SMS to: $phoneNumber")
            Result.success(Unit)
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_AUTOMATION, "Failed to open SMS", e)
            Result.error(e)
        }
    }

    /**
     * Open email app.
     */
    fun sendEmail(
        to: String,
        subject: String? = null,
        body: String? = null
    ): Result<Unit> {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$to")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                body?.let { putExtra(Intent.EXTRA_TEXT, it) }
            }
            context.startActivity(intent)
            AgentLogger.i(AgentLogger.TAG_AUTOMATION, "Opening email to: $to")
            Result.success(Unit)
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_AUTOMATION, "Failed to open email", e)
            Result.error(e)
        }
    }

    /**
     * Open settings.
     */
    fun openSettings(settingsAction: String = android.provider.Settings.ACTION_SETTINGS): Result<Unit> {
        return try {
            val intent = Intent(settingsAction).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            AgentLogger.i(AgentLogger.TAG_AUTOMATION, "Opened settings: $settingsAction")
            Result.success(Unit)
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_AUTOMATION, "Failed to open settings", e)
            Result.error(e)
        }
    }

    /**
     * Check if an app is installed.
     */
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Get list of installed apps.
     */
    fun getInstalledApps(): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        return packageManager.queryIntentActivities(intent, 0).map { resolveInfo ->
            InstalledApp(
                packageName = resolveInfo.activityInfo.packageName,
                appName = resolveInfo.loadLabel(packageManager).toString(),
                activityName = resolveInfo.activityInfo.name
            )
        }
    }
}

/**
 * Information about an installed app.
 */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val activityName: String
)
