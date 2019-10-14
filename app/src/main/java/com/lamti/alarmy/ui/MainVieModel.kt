package com.lamti.alarmy.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamti.alarmy.data.Repository
import com.lamti.alarmy.data.models.Alarm
import kotlinx.coroutines.launch

class MainVieModel (private val repository: Repository) : ViewModel() {

    val allAlarms: LiveData<List<Alarm>> = repository.allAlarms

    fun insert(alarm: Alarm) = viewModelScope.launch {
        repository.insert(alarm)
    }

    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.update(alarm)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}