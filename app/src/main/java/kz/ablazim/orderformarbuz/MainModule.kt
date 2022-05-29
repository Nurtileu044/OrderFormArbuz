package kz.ablazim.orderformarbuz

import kz.ablazim.orderformarbuz.di.InjectionModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object MainModule : InjectionModule {
    override fun create(): Module = module {
        viewModel { MainViewModel() }
    }
}