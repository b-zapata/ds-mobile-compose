package study.doomscrolling.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import study.doomscrolling.app.BuildConfig
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.entities.DeviceEntity
import java.util.UUID

class ConsentViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    fun acceptConsent(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val existing = db.deviceDao().getDevice()
                if (existing == null) {
                    db.deviceDao().insertDevice(
                        DeviceEntity(
                            deviceId = UUID.randomUUID().toString(),
                            appVersion = BuildConfig.VERSION_NAME ?: "1.0",
                            enrolledAt = null
                        )
                    )
                }
            }
            onComplete()
        }
    }
}
