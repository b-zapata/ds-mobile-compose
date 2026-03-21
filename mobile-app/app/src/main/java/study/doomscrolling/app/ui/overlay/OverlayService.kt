package study.doomscrolling.app.ui.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.flow.MutableStateFlow
import study.doomscrolling.app.R
import study.doomscrolling.app.domain.intervention.InterventionCompletionNotifier

/**
 * Foreground service that displays the prompt overlay above other apps using WindowManager.
 * Implements LifecycleOwner and SavedStateRegistryOwner so ComposeView can compose.
 */
class OverlayService : LifecycleService(), SavedStateRegistryOwner {

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private var overlayView: ComposeView? = null
    private var windowManager: WindowManager? = null
    private var currentInterventionId: String? = null
    private var currentSessionId: String? = null
    
    // Audio Focus management to mute other apps
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    // Manage timer in the service to ensure it ticks reliably
    private val secondsLeftFlow = MutableStateFlow(12)
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (secondsLeftFlow.value > 0) {
                secondsLeftFlow.value -= 1
                Log.d(TAG, "Timer Tick: ${secondsLeftFlow.value}")
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val promptText = intent?.getStringExtra(EXTRA_PROMPT_TEXT) ?: return super.onStartCommand(intent, flags, startId)
        currentInterventionId = intent.getStringExtra(EXTRA_INTERVENTION_ID)
        currentSessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        requestMute()
        
        // Reset and start timer
        secondsLeftFlow.value = 12
        handler.removeCallbacks(timerRunnable)
        handler.postDelayed(timerRunnable, 1000)
        
        showOverlay(promptText)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun requestMute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener { /* handle changes if needed */ }
                .build()
            
            audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
        Log.i(TAG, "Audio focus requested (muting/ducking other apps)")
    }

    private fun releaseMute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
        Log.i(TAG, "Audio focus released")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_overlay_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { setShowBadge(false) }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_overlay_title))
            .setContentText(getString(R.string.notification_overlay_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun showOverlay(promptText: String) {
        val wm = windowManager ?: return
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
        
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setContent {
                val secondsLeft by secondsLeftFlow.collectAsState()
                PromptOverlayView(
                    promptText = promptText,
                    secondsLeft = secondsLeft,
                    onContinue = {
                        handleCompletion(action = "continued_session")
                    },
                    onCloseApp = {
                        handleCompletion(action = "closed_app")
                    }
                )
            }
        }
        overlayView = composeView
        wm.addView(composeView, layoutParams)
    }

    private fun handleCompletion(action: String) {
        val interventionId = currentInterventionId
        val sessionId = currentSessionId
        if (interventionId != null && sessionId != null) {
            InterventionCompletionNotifier.notifyCompleted(
                interventionId = interventionId,
                sessionId = sessionId,
                action = action
            )
        } else {
            Log.w(TAG, "Missing intervention/session id on overlay completion")
        }
        
        releaseMute()
        removeOverlay()
        Log.i(TAG, "Prompt overlay dismissed with action: $action")
        stopSelf()
    }

    private fun removeOverlay() {
        handler.removeCallbacks(timerRunnable)
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                Log.w(TAG, "Error removing overlay", e)
            }
            overlayView = null
        }
    }

    override fun onDestroy() {
        releaseMute()
        removeOverlay()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OverlayService"
        private const val CHANNEL_ID = "prompt_overlay"
        private const val NOTIFICATION_ID = 2

        const val EXTRA_PROMPT_ID = "prompt_id"
        const val EXTRA_PROMPT_TEXT = "prompt_text"
        const val EXTRA_PROMPT_CATEGORY = "prompt_category"
        const val EXTRA_INTERVENTION_ID = "intervention_id"
        const val EXTRA_SESSION_ID = "session_id"
    }
}
