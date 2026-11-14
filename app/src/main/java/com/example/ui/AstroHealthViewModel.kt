package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AstroHealthDatabase
import com.example.data.model.BodyZone
import com.example.data.model.CountermeasureProtocol
import com.example.data.model.DailyTask
import com.example.data.model.GroundCommMessage
import com.example.data.model.HealthLog
import com.example.data.model.MissionEnvironment
import com.example.data.repository.AstroHealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AstroHealthUiState(
    val logs: List<HealthLog> = emptyList(),
    val latestLog: HealthLog? = null,
    val currentEnvironment: MissionEnvironment = MissionEnvironment.MARS_TRANSIT,
    val dailyTasks: List<DailyTask> = emptyList(),
    val commMessages: List<GroundCommMessage> = emptyList(),
    val protocols: List<CountermeasureProtocol> = emptyList(),
    val selectedZone: BodyZone? = null,
    val selectedProtocol: CountermeasureProtocol? = null,
    val isLoggingSheetOpen: Boolean = false,
    val crewReadinessScore: Int = 94,
    val crewXp: Int = 850,
    val missionStreakDays: Int = 42
)

private data class UiSelections(
    val selectedZone: BodyZone? = null,
    val selectedProtocol: CountermeasureProtocol? = null,
    val isLoggingSheetOpen: Boolean = false
)

class AstroHealthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AstroHealthRepository

    private val _uiSelections = MutableStateFlow(UiSelections())

    init {
        val db = AstroHealthDatabase.getDatabase(application)
        repository = AstroHealthRepository(db.healthLogDao())
    }

    // Combine repository data (5 flows)
    private val repoDataFlow = combine(
        repository.allLogs,
        repository.latestLog,
        repository.currentEnvironment,
        repository.dailyTasks,
        repository.commMessages
    ) { logs, latest, env, tasks, comms ->
        val readiness = latest?.getReadinessScore() ?: 92
        val xp = tasks.filter { it.isCompleted }.sumOf { it.xp } + 500
        val streak = latest?.missionDay ?: 42

        AstroHealthUiState(
            logs = logs,
            latestLog = latest,
            currentEnvironment = env,
            dailyTasks = tasks,
            commMessages = comms,
            protocols = repository.getProtocols(),
            crewReadinessScore = readiness,
            crewXp = xp,
            missionStreakDays = streak
        )
    }

    val uiState: StateFlow<AstroHealthUiState> = combine(
        repoDataFlow,
        _uiSelections
    ) { baseState, selections ->
        baseState.copy(
            selectedZone = selections.selectedZone,
            selectedProtocol = selections.selectedProtocol,
            isLoggingSheetOpen = selections.isLoggingSheetOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AstroHealthUiState(protocols = repository.getProtocols())
    )

    fun selectZone(zone: BodyZone?) {
        _uiSelections.value = _uiSelections.value.copy(selectedZone = zone)
    }

    fun selectProtocol(protocol: CountermeasureProtocol?) {
        _uiSelections.value = _uiSelections.value.copy(selectedProtocol = protocol)
    }

    fun setLoggingSheetOpen(isOpen: Boolean) {
        _uiSelections.value = _uiSelections.value.copy(isLoggingSheetOpen = isOpen)
    }

    fun setEnvironment(env: MissionEnvironment) {
        repository.setEnvironment(env)
    }

    fun toggleTask(taskId: String) {
        repository.toggleTask(taskId)
    }

    fun sendUplink(notes: String) {
        repository.sendUplinkToGround(notes)
    }

    fun logHealthIndicator(log: HealthLog) {
        viewModelScope.launch {
            repository.insertLog(log)
            setLoggingSheetOpen(false)
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLog(id)
        }
    }
}
