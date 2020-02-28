package com.lamti.alarmy

import android.content.Context
import com.lamti.alarmy.data.models.Alarm
import com.lamti.alarmy.domain.managers.AlarmyManager
import org.junit.Test

class AlarmyManagerTest {

    private lateinit var testedClass: AlarmyManager
    private lateinit var context: Context

    @Test
    fun `given alarm, when alarm has four days, then create repeating alarm`() {
        testedClass = AlarmyManager

        AlarmyManager.addAlarm(
            getRepeatingAlarm(),
            context
        )

    }


    private fun getRepeatingAlarm(): Alarm {
        return Alarm(
            100, "14:00", 1212121212, "Alarm title",
            listOf("Sunday", "Monday", "Tuesday"), listOf(1, 2, 3),
            false, true, false, true
        )
    }
}
