package com.example.brigadist.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadist.data.analytics.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val repository: AnalyticsRepository = AnalyticsRepository()
) : ViewModel() {
    
    private val _permissionChangesState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val permissionChangesState: StateFlow<AnalyticsUiState> = _permissionChangesState.asStateFlow()
    
    private val _permissionStatusState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val permissionStatusState: StateFlow<AnalyticsUiState> = _permissionStatusState.asStateFlow()
    
    private val _profileUpdatesState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val profileUpdatesState: StateFlow<AnalyticsUiState> = _profileUpdatesState.asStateFlow()
    
    private val _screenViewsState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val screenViewsState: StateFlow<AnalyticsUiState> = _screenViewsState.asStateFlow()
    
    val dashboardData: StateFlow<AnalyticsDashboardData> = combine(
        permissionChangesState,
        permissionStatusState,
        profileUpdatesState,
        screenViewsState
    ) { permissionChanges, permissionStatus, profileUpdates, screenViews ->
        AnalyticsDashboardData(
            permissionChanges = permissionChanges,
            permissionStatus = permissionStatus,
            profileUpdates = profileUpdates,
            screenViews = screenViews
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsDashboardData(
            permissionChanges = AnalyticsUiState.Loading,
            permissionStatus = AnalyticsUiState.Loading,
            profileUpdates = AnalyticsUiState.Loading,
            screenViews = AnalyticsUiState.Loading
        )
    )
    
    init {
        loadAllData()
    }
    
    private fun loadAllData() {
        loadPermissionChanges()
        loadPermissionStatus()
        loadProfileUpdates()
        loadScreenViews()
    }
    
    private fun loadPermissionChanges() {
        viewModelScope.launch {
            repository.getPermissionChanges().collect { result ->
                _permissionChangesState.value = result.fold(
                    onSuccess = { AnalyticsUiState.Success(it) },
                    onFailure = { AnalyticsUiState.Error(it.message ?: "Failed to load permission changes") }
                )
            }
        }
    }
    
    private fun loadPermissionStatus() {
        viewModelScope.launch {
            repository.getPermissionStatus().collect { result ->
                _permissionStatusState.value = result.fold(
                    onSuccess = { AnalyticsUiState.Success(it) },
                    onFailure = { AnalyticsUiState.Error(it.message ?: "Failed to load permission status") }
                )
            }
        }
    }
    
    private fun loadProfileUpdates() {
        viewModelScope.launch {
            repository.getProfileUpdates().collect { result ->
                _profileUpdatesState.value = result.fold(
                    onSuccess = { AnalyticsUiState.Success(it) },
                    onFailure = { AnalyticsUiState.Error(it.message ?: "Failed to load profile updates") }
                )
            }
        }
    }
    
    private fun loadScreenViews() {
        viewModelScope.launch {
            repository.getScreenViews().collect { result ->
                _screenViewsState.value = result.fold(
                    onSuccess = { AnalyticsUiState.Success(it) },
                    onFailure = { AnalyticsUiState.Error(it.message ?: "Failed to load screen views") }
                )
            }
        }
    }
    
    fun retryPermissionChanges() {
        _permissionChangesState.value = AnalyticsUiState.Loading
        loadPermissionChanges()
    }
    
    fun retryPermissionStatus() {
        _permissionStatusState.value = AnalyticsUiState.Loading
        loadPermissionStatus()
    }
    
    fun retryProfileUpdates() {
        _profileUpdatesState.value = AnalyticsUiState.Loading
        loadProfileUpdates()
    }
    
    fun retryScreenViews() {
        _screenViewsState.value = AnalyticsUiState.Loading
        loadScreenViews()
    }
    
    fun retryAll() {
        _permissionChangesState.value = AnalyticsUiState.Loading
        _permissionStatusState.value = AnalyticsUiState.Loading
        _profileUpdatesState.value = AnalyticsUiState.Loading
        _screenViewsState.value = AnalyticsUiState.Loading
        loadAllData()
    }
}
