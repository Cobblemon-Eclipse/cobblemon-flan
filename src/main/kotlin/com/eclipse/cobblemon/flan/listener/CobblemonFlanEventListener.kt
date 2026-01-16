package com.eclipse.cobblemon.flan.listener

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.eclipse.cobblemon.flan.config.CobblemonFlanConfig
import com.eclipse.cobblemon.flan.di.CobblemonFlanLoggerService
import com.eclipse.cobblemon.flan.permission.FlanPermissionChecker
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
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

        logger.info("Cobblemon Flan event listeners registered!")
    }

    private fun registerCatchProtection() {
        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe { event ->
            val config = CobblemonFlanConfig.config
            if (!config.protections.preventCatching) return@subscribe

            val pokeBall = event.pokeBall
            val thrower = pokeBall.owner
            if (thrower !is ServerPlayerEntity) return@subscribe

            val pokemon = event.pokemon
            val targetPos = pokemon.blockPos

            if (!permissionChecker.canCatchPokemon(thrower, targetPos)) {
                event.cancel()
                sendMessage(thrower, config.messages.cannotCatch)
                logger.debug("Blocked catch attempt by ${thrower.name.string} at $targetPos")
            }
        }

        logger.debug("Catch protection registered")
    }

    private fun registerBattleProtection() {
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe { event ->
            val config = CobblemonFlanConfig.config
            if (!config.protections.preventBattles) return@subscribe

            // Check all participants
            for (actor in event.battle.actors) {
                val playerUuids = actor.getPlayerUUIDs()
                for (uuid in playerUuids) {
                    val player = event.battle.players.find { it.uuid == uuid }

                    if (player is ServerPlayerEntity) {
                        val pos = player.blockPos
                        if (!permissionChecker.canBattlePokemon(player, pos)) {
                            event.cancel()
                            sendMessage(player, config.messages.cannotBattle)
                            logger.debug("Blocked battle for ${player.name.string} at $pos")
                            return@subscribe
                        }
                    }
                }
            }
        }

        logger.debug("Battle protection registered")
    }

    private fun registerSendOutProtection() {
        CobblemonEvents.POKEMON_SENT_PRE.subscribe { event ->
            val config = CobblemonFlanConfig.config
            if (!config.protections.preventSendOut) return@subscribe

            // Get player from the pokemon's owner
            val pokemon = event.pokemon
            val playerUuid = pokemon.getOwnerUUID() ?: return@subscribe
            val world = event.level
            val server = world.server ?: return@subscribe
            val player = server.playerManager.getPlayer(playerUuid) ?: return@subscribe

            val pos = BlockPos.ofFloored(event.position)
            if (!permissionChecker.canSendOutPokemon(player, pos)) {
                event.cancel()
                sendMessage(player, config.messages.cannotSendOut)
                logger.debug("Blocked send out by ${player.name.string} at $pos")
            }
        }

        logger.debug("Send out protection registered")
    }

    private fun registerRideProtection() {
        CobblemonEvents.RIDE_EVENT_PRE.subscribe { event ->
            val config = CobblemonFlanConfig.config
            if (!config.protections.preventRiding) return@subscribe

            // event.player is always a ServerPlayerEntity (mappings handle the conversion)
            @Suppress("USELESS_IS_CHECK")
            val player = event.player as? ServerPlayerEntity ?: return@subscribe

            val pos = player.blockPos
            if (!permissionChecker.canRidePokemon(player, pos)) {
                event.cancel()
                sendMessage(player, config.messages.cannotRide)
                logger.debug("Blocked riding by ${player.name.string} at $pos")
            }
        }

        logger.debug("Ride protection registered")
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
