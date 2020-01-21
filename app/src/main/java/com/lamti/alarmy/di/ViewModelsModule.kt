package com.lamti.alarmy.di

import com.lamti.alarmy.data.Repository
import com.lamti.alarmy.AlarmyManager
import com.lamti.alarmy.ui.main_activity.MainVieModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { MainVieModel(Repository(get(), androidContext())) }
    single { AlarmyManager }
}
