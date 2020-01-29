package com.lamti.alarmy.di

import com.lamti.alarmy.data.Repository
import com.lamti.alarmy.domain.managers.AlarmyManager
import com.lamti.alarmy.ui.main_activity.AlarmVieModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { AlarmVieModel(Repository(get())) }
    single { AlarmyManager }
}
