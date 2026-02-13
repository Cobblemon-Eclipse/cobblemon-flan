package com.eclipse.cobblemon.flan.listener

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.eclipse.cobblemon.flan.api.FlanBypass
import com.eclipse.cobblemon.flan.config.CobblemonFlanConfig
import com.eclipse.cobblemon.flan.di.CobblemonFlanLoggerService
import com.eclipse.cobblemon.flan.permission.FlanPermissionChecker
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos

/**
 * Listens to Cobblemon events and checks Flan permissions
 */
class CobblemonFlanEventListener(
    private val logger: CobblemonFlanLoggerService,
    private val permissionChecker: FlanPermissionChecker
) {

    fun register() {
        logger.info("Registering Cobblemon event listeners for Flan protection...")

        registerCatchProtection()
        registerBattleProtection()
        registerSendOutProtection()
        registerRideProtection()
        registerDisplayCaseProtection()

        logger.info("Cobblemon Flan event listeners registered!")
    }

    private fun registerCatchProtection() {
        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe { event ->
            try {
                val config = CobblemonFlanConfig.config
                if (!config.protections.preventCatching) return@subscribe

                val pokeBall = event.pokeBall
                val thrower = pokeBall.owner as? ServerPlayerEntity ?: return@subscribe
                if (FlanBypass.isBypassed(thrower.uuid)) return@subscribe

                val pokemon = event.pokemon
                val targetPos = pokemon.blockPos

                if (!permissionChecker.canCatchPokemon(thrower, targetPos)) {
                    event.cancel()
                    sendMessage(thrower, config.messages.cannotCatch)
                    logger.debug("Blocked catch attempt by ${thrower.name.string} at $targetPos")
                }
            } catch (e: Exception) {
                logger.warn("Error in catch protection: ${e.message}")
            }
        }

        logger.debug("Catch protection registered")
    }

    private fun registerBattleProtection() {
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe { event ->
            try {
                val config = CobblemonFlanConfig.config
                if (!config.protections.preventBattles) return@subscribe

                // Iterate through all players in the battle directly
                for (player in event.battle.players) {
                    // Cast to ServerPlayerEntity (Yarn mapping)
                    val serverPlayer = player as? ServerPlayerEntity
                    if (serverPlayer != null) {
                        if (FlanBypass.isBypassed(serverPlayer.uuid)) continue
                        val pos = serverPlayer.blockPos
                        if (!permissionChecker.canBattlePokemon(serverPlayer, pos)) {
                            event.cancel()
                            sendMessage(serverPlayer, config.messages.cannotBattle)
                            logger.debug("Blocked battle for ${serverPlayer.name.string} at $pos")
                            return@subscribe
                        }
                    }
                }
            } catch (e: Exception) {
                logger.warn("Error in battle protection: ${e.message}")
            }
        }

        logger.debug("Battle protection registered")
    }

    private fun registerSendOutProtection() {
        CobblemonEvents.POKEMON_SENT_PRE.subscribe { event ->
            try {
                val config = CobblemonFlanConfig.config
                if (!config.protections.preventSendOut) return@subscribe

                // Get player from the pokemon's owner
                val pokemon = event.pokemon
                val playerUuid = pokemon.getOwnerUUID() ?: return@subscribe
                if (FlanBypass.isBypassed(playerUuid)) return@subscribe

                // Get server from the level (works with both Mojmap and Yarn via remapping)
                val server = event.level.server ?: return@subscribe
                val player = server.playerManager.getPlayer(playerUuid) ?: return@subscribe

                val pos = BlockPos.ofFloored(event.position)
                if (!permissionChecker.canSendOutPokemon(player, pos)) {
                    event.cancel()
                    sendMessage(player, config.messages.cannotSendOut)
                    logger.debug("Blocked send out by ${player.name.string} at $pos")
                }
            } catch (e: Exception) {
                logger.warn("Error in send out protection: ${e.message}")
            }
        }

        logger.debug("Send out protection registered")
    }

    private fun registerRideProtection() {
        CobblemonEvents.RIDE_EVENT_PRE.subscribe { event ->
            try {
                val config = CobblemonFlanConfig.config
                if (!config.protections.preventRiding) return@subscribe

                // Cast player to ServerPlayerEntity (Yarn mapping from Mojmap ServerPlayer)
                val player = event.player as? ServerPlayerEntity ?: return@subscribe
                if (FlanBypass.isBypassed(player.uuid)) return@subscribe
                val pos = player.blockPos

                if (!permissionChecker.canRidePokemon(player, pos)) {
                    event.cancel()
                    sendMessage(player, config.messages.cannotRide)
                    logger.debug("Blocked riding by ${player.name.string} at $pos")
                }
            } catch (e: Exception) {
                logger.warn("Error in ride protection: ${e.message}")
            }
        }

        logger.debug("Ride protection registered")
    }

    private fun registerDisplayCaseProtection() {
        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            if (world.isClient) return@register ActionResult.PASS

            try {
                val config = CobblemonFlanConfig.config
                if (!config.protections.preventDisplayCaseInteraction) return@register ActionResult.PASS

                val serverPlayer = player as? ServerPlayerEntity ?: return@register ActionResult.PASS
                if (FlanBypass.isBypassed(serverPlayer.uuid)) return@register ActionResult.PASS
                val pos = hitResult.blockPos
                val blockState = world.getBlockState(pos)

                // Check if this is a Cobblemon display case
                if (blockState.block == CobblemonBlocks.DISPLAY_CASE) {
                    if (!permissionChecker.canUseDisplayCase(serverPlayer, pos)) {
                        sendMessage(serverPlayer, config.messages.cannotUseDisplayCase)
                        logger.debug("Blocked display case interaction by ${serverPlayer.name.string} at $pos")
                        return@register ActionResult.FAIL
                    }
                }
            } catch (e: Exception) {
                logger.warn("Error in display case protection: ${e.message}")
            }

            ActionResult.PASS
        }

        logger.debug("Display case protection registered")
    }

    private fun sendMessage(player: ServerPlayerEntity, message: String) {
        val config = CobblemonFlanConfig.config
        val fullMessage = config.messages.prefix + message
        // Simple message sending - strip MiniMessage tags for basic message
        player.sendMessage(Text.literal(
            fullMessage.replace(Regex("<[^>]+>"), "")
        ))
    }
}
