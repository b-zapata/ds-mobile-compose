package study.doomscrolling.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.repository.BaselineStats
import study.doomscrolling.app.data.repository.SessionRepository

data class BaselineStatsUiState(
    val stats: BaselineStats? = null,
    val loading: Boolean = true
)

class BaselineStatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SessionRepository by lazy {
        val db = AppDatabase.getInstance(getApplication())
        SessionRepository(db.sessionDao(), db.deviceDao())
    }

    private val _uiState = MutableStateFlow(BaselineStatsUiState())
    val uiState: StateFlow<BaselineStatsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = BaselineStatsUiState(loading = true)
            val stats = repository.getBaselineStats()
            _uiState.value = BaselineStatsUiState(stats = stats, loading = false)
        }
    }

    /**
     * Debug: run baseline import now (UsageStats or event fallback), then refresh stats.
     * Use when baseline data is missing or to re-import without going through onboarding.
     */
    fun importBaselineNow() {
        viewModelScope.launch {
            _uiState.value = BaselineStatsUiState(loading = true)
            repository.importBaselineFromUsageStats(getApplication())
            val stats = repository.getBaselineStats()
            _uiState.value = BaselineStatsUiState(stats = stats, loading = false)
        }
    }
}

