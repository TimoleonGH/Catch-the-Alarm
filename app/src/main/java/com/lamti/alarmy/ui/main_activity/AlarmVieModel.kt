package com.lamti.alarmy.ui.main_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamti.alarmy.data.Repository
import com.lamti.alarmy.data.models.Alarm
import kotlinx.coroutines.launch

class AlarmVieModel(private val repository: Repository) : ViewModel() {

    val allAlarms: LiveData<List<Alarm>> = repository.allAlarms
    var insertedAlarmID = 0L

    fun insert(alarm: Alarm) = viewModelScope.launch {
        insertedAlarmID = repository.insert(alarm)
    }

    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.update(alarm)
    }

    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.delete(alarm)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
