package com.eclipse.cobblemon.flan

import com.eclipse.cobblemon.flan.api.FlanBypass
import com.eclipse.cobblemon.flan.config.CobblemonFlanConfig
import com.eclipse.cobblemon.flan.listener.CobblemonFlanEventListener
import com.eclipse.cobblemon.flan.permission.FlanPermissionChecker
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import org.slf4j.LoggerFactory

object CobblemonFlan : ModInitializer {
    const val MOD_ID = "cobblemon-flan"

    private val logger = LoggerFactory.getLogger("CobblemonFlan")
    val permissionChecker = FlanPermissionChecker()
    private val eventListener = CobblemonFlanEventListener()

    override fun onInitialize() {
        logger.info("Initializing Cobblemon Flan Integration...")

        // Load config
        CobblemonFlanConfig.load()

        // Register event listeners
        eventListener.register()

        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register { _ ->
            logger.info("Cobblemon Flan Integration active - protecting claims!")
        }

        // Clear all bypasses on server shutdown to prevent stale state
        ServerLifecycleEvents.SERVER_STOPPED.register { _ ->
            FlanBypass.clearAll()
            logger.info("Cobblemon Flan bypasses cleared on shutdown")
        }

        // Remove bypass on player disconnect to prevent unbounded set growth
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            FlanBypass.removeBypass(handler.player.uuid)
        }

        logger.info("Cobblemon Flan Integration initialized!")
    }
}
