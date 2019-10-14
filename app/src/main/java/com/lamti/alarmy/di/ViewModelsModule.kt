package com.lamti.alarmy.di

import com.lamti.alarmy.data.Repository
import com.lamti.alarmy.ui.MainVieModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { MainVieModel( Repository(get()) ) }
}