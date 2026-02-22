package com.eclipse.cobblemon.flan.permission

import io.github.flemmli97.flan.api.ClaimHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.slf4j.LoggerFactory

/**
 * Handles permission checks against Flan claims
 */
class FlanPermissionChecker {

    private val logger = LoggerFactory.getLogger("CobblemonFlan")

    companion object {
        // Cobblemon-specific permissions (these need to be registered via datapacks)
        val POKEMON_SPAWN = Identifier.of("cobblemon-flan", "pokemon_spawn")
        val POKEMON_CATCH = Identifier.of("cobblemon-flan", "pokemon_catch")
        val POKEMON_BATTLE = Identifier.of("cobblemon-flan", "pokemon_battle")
        val POKEMON_SENDOUT = Identifier.of("cobblemon-flan", "pokemon_sendout")
        val POKEMON_RIDE = Identifier.of("cobblemon-flan", "pokemon_ride")
        val DISPLAY_CASE = Identifier.of("cobblemon-flan", "display_case")
    }

    /**
     * Check if a player can perform an action at a position
     */
    fun canInteract(player: ServerPlayerEntity, pos: BlockPos, permission: Identifier): Boolean {
        return try {
            ClaimHandler.canInteract(player, pos, permission)
        } catch (e: Exception) {
            logger.warn("Error checking Flan permission: ${e.message}")
            true // Default to allowing if error
        }
    }

    fun canPokemonSpawn(player: ServerPlayerEntity?, pos: BlockPos): Boolean {
        if (player == null) return true
        return canInteract(player, pos, POKEMON_SPAWN)
    }

    fun canCatchPokemon(player: ServerPlayerEntity, pos: BlockPos): Boolean {
        return canInteract(player, pos, POKEMON_CATCH)
    }

    fun canBattlePokemon(player: ServerPlayerEntity, pos: BlockPos): Boolean {
        return canInteract(player, pos, POKEMON_BATTLE)
    }

    fun canSendOutPokemon(player: ServerPlayerEntity, pos: BlockPos): Boolean {
        return canInteract(player, pos, POKEMON_SENDOUT)
    }

    fun canRidePokemon(player: ServerPlayerEntity, pos: BlockPos): Boolean {
        return canInteract(player, pos, POKEMON_RIDE)
    }

    fun canUseDisplayCase(player: ServerPlayerEntity, pos: BlockPos): Boolean {
        return canInteract(player, pos, DISPLAY_CASE)
    }
}
