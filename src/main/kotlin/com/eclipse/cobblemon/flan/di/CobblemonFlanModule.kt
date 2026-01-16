package com.eclipse.cobblemon.flan.di

import com.eclipse.cobblemon.flan.listener.CobblemonFlanEventListener
import com.eclipse.cobblemon.flan.permission.FlanPermissionChecker
import org.koin.dsl.module

object CobblemonFlanModule {
    val module = module {
        single<CobblemonFlanLoggerService> { CobblemonFlanLoggerServiceImpl() }
        single { FlanPermissionChecker(get()) }
        single { CobblemonFlanEventListener(get(), get()) }
    }
}
