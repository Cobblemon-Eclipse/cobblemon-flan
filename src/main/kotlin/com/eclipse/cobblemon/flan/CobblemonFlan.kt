package com.eclipse.cobblemon.flan

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.eclipse.cobblemon.flan.config.CobblemonFlanConfig
import com.eclipse.cobblemon.flan.di.CobblemonFlanModule
import com.eclipse.cobblemon.flan.di.CobblemonFlanLoggerService
import com.eclipse.cobblemon.flan.listener.CobblemonFlanEventListener
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.loadKoinModules

object CobblemonFlan : ModInitializer, KoinComponent {
    const val MOD_ID = "cobblemon-flan"

    private val logger: CobblemonFlanLoggerService by inject()
    private val eventListener: CobblemonFlanEventListener by inject()

    override fun onInitialize() {
        // If Koin is already started (e.g. by eclipse-core), add our module
        // Otherwise start Koin ourselves for standalone operation
        if (GlobalContext.getOrNull() != null) {
            loadKoinModules(CobblemonFlanModule.module)
        } else {
            startKoin {
                modules(CobblemonFlanModule.module)
            }
        }

        logger.info("Initializing Cobblemon Flan Integration...")

        // Load config
        CobblemonFlanConfig.load()

        // Register event listeners
        registerEventListeners()

        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            logger.info("Cobblemon Flan Integration active - protecting claims!")
        }

        logger.info("Cobblemon Flan Integration initialized!")
    }

    private fun registerEventListeners() {
        eventListener.register()
    }
}
