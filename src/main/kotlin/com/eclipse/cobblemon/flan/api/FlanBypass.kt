package com.eclipse.cobblemon.flan.api

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Public API for bypassing Flan claim protections.
 *
 * Other mods (e.g., eclipse-tournament, battle-factory) can add player UUIDs
 * to temporarily bypass all cobblemon-flan permission checks.
 *
 * Usage from another mod (via reflection if no compile dependency):
 *   FlanBypass.addBypass(playerUuid)
 *   // ... do protected action ...
 *   FlanBypass.removeBypass(playerUuid)
 */
object FlanBypass {

    private val bypassed: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    /**
     * Add a player to the bypass set. They will skip all Flan permission checks.
     */
    @JvmStatic
    fun addBypass(playerId: UUID) {
        bypassed.add(playerId)
    }

    /**
     * Remove a player from the bypass set.
     */
    @JvmStatic
    fun removeBypass(playerId: UUID) {
        bypassed.remove(playerId)
    }

    /**
     * Check if a player is currently bypassing Flan checks.
     */
    @JvmStatic
    fun isBypassed(playerId: UUID): Boolean {
        return bypassed.contains(playerId)
    }

    /**
     * Clear all bypasses (called on server shutdown).
     */
    @JvmStatic
    fun clearAll() {
        bypassed.clear()
    }
}
