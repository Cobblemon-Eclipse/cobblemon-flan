package com.eclipse.cobblemon.flan.listener

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.spawning.influence.SaccharineLogSlatheredInfluence
import com.cobblemon.mod.common.block.SaccharineLogBlock
import com.eclipse.cobblemon.flan.CobblemonFlan
import com.eclipse.cobblemon.flan.api.FlanBypass
import com.eclipse.cobblemon.flan.config.CobblemonFlanConfig
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import org.slf4j.LoggerFactory

/**
 * Listens to Cobblemon events and checks Flan permissions
 */
class CobblemonFlanEventListener {

    private val logger = LoggerFactory.getLogger("CobblemonFlan")
    private val permissionChecker get() = CobblemonFlan.permissionChecker

    fun register() {
        logger.info("Registering Cobblemon event listeners for Flan protection...")

        registerSpawnProtection()
        registerCatchProtection()
        registerBattleProtection()
        registerSendOutProtection()
        registerRideProtection()
        registerBlockInteractionProtection()

        logger.info("Cobblemon Flan event listeners registered!")
    }

    private fun registerSpawnProtection() {
        CobblemonEvents.ENTITY_SPAWN.subscribe { event ->
            try {
                val config = CobblemonFlanConfig.config
                if (!config.protections.preventWildSpawns) return@subscribe

                // Allow honey-lure spawns through even when wild spawns are blocked
                if (config.protections.allowHoneyLureSpawns &&
                    event.spawnablePosition.markers.contains(SaccharineLogSlatheredInfluence.SACCHARINE_LOG_SLATHERED_MARKER)) {
                    return@subscribe
                }

                val spawnPos = event.spawnablePosition.position
                val causeEntity = event.spawnablePosition.cause.entity
                val player = causeEntity as? ServerPlayerEntity ?: return@subscribe
                if (FlanBypass.isBypassed(player.uuid)) return@subscribe

                if (!permissionChecker.canPokemonSpawn(player, spawnPos)) {
                    event.cancel()
                    logger.debug("Blocked wild spawn at $spawnPos in claim near ${player.name.string}")
                }
            } catch (e: Exception) {
                logger.warn("Error in spawn protection: ${e.message}")
                event.cancel() // Fail closed - deny spawn on error
            }
        }

        logger.debug("Spawn protection registered")
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
                event.cancel() // Fail closed - deny catch on error
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
                event.cancel() // Fail closed - deny battle on error
            }
        }

        logger.debug("Battle protection registered")
    }

    private fun registerSendOutProtection() {
        CobblemonEvents.POKEMON_SENT_PRE.subscribe { event ->
            try {
                val config = CobblemonFlanConfig.config
                if (!config.protections.preventSendOut) return@subscribe

                val pokemon = event.pokemon
                val playerUuid = pokemon.getOwnerUUID() ?: return@subscribe
                if (FlanBypass.isBypassed(playerUuid)) return@subscribe

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
                event.cancel() // Fail closed - deny send out on error
            }
        }

        logger.debug("Send out protection registered")
    }

    private fun registerRideProtection() {
        CobblemonEvents.RIDE_EVENT_PRE.subscribe { event ->
            try {
                val config = CobblemonFlanConfig.config
                if (!config.protections.preventRiding) return@subscribe

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
                event.cancel() // Fail closed - deny ride on error
            }
        }

        logger.debug("Ride protection registered")
    }

    private fun registerBlockInteractionProtection() {
        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            if (world.isClient) return@register ActionResult.PASS

            try {
                val serverPlayer = player as? ServerPlayerEntity ?: return@register ActionResult.PASS
                if (FlanBypass.isBypassed(serverPlayer.uuid)) return@register ActionResult.PASS
                val pos = hitResult.blockPos
                val blocked = when (val block = world.getBlockState(pos).block) {
                    CobblemonBlocks.DISPLAY_CASE -> blockDisplayCase(serverPlayer, pos)
                    CobblemonBlocks.PC -> blockPCUse(serverPlayer, pos)
                    CobblemonBlocks.HEALING_MACHINE -> blockHealingMachine(serverPlayer, pos)
                    is SaccharineLogBlock -> blockHoneyLure(serverPlayer, pos, hand, hitResult)
                    else -> false
                }
                if (blocked) return@register ActionResult.FAIL
            } catch (e: Exception) {
                logger.warn("Error in block interaction protection: ${e.message}")
                return@register ActionResult.FAIL // Fail closed - deny interaction on error
            }

            ActionResult.PASS
        }

        logger.debug("Block interaction protection registered")
    }

    // Each returns true if the interaction should be denied (fail closed on the caller's catch).
    private fun blockDisplayCase(player: ServerPlayerEntity, pos: BlockPos): Boolean {
        val config = CobblemonFlanConfig.config
        if (config.protections.preventDisplayCaseInteraction && !permissionChecker.canUseDisplayCase(player, pos)) {
            sendMessage(player, config.messages.cannotUseDisplayCase)
            logger.debug("Blocked display case interaction by ${player.name.string} at $pos")
            return true
        }
        return false
    }

    private fun blockPCUse(player: ServerPlayerEntity, pos: BlockPos): Boolean {
        val config = CobblemonFlanConfig.config
        if (config.protections.preventPCUse && !permissionChecker.canUsePC(player, pos)) {
            sendMessage(player, config.messages.cannotUsePC)
            logger.debug("Blocked PC interaction by ${player.name.string} at $pos")
            return true
        }
        return false
    }

    private fun blockHealingMachine(player: ServerPlayerEntity, pos: BlockPos): Boolean {
        val config = CobblemonFlanConfig.config
        if (config.protections.preventHealingMachineUse && !permissionChecker.canUseHealingMachine(player, pos)) {
            sendMessage(player, config.messages.cannotUseHealingMachine)
            logger.debug("Blocked Healing Machine interaction by ${player.name.string} at $pos")
            return true
        }
        return false
    }

    private fun blockHoneyLure(player: ServerPlayerEntity, pos: BlockPos, hand: Hand, hitResult: BlockHitResult): Boolean {
        val config = CobblemonFlanConfig.config
        // Only intercept the honey-bottle-on-vertical-side interaction that turns it into a slathered log
        if (config.protections.preventHoneyLurePlacement &&
            player.getStackInHand(hand).isOf(Items.HONEY_BOTTLE) &&
            hitResult.side != Direction.UP &&
            hitResult.side != Direction.DOWN &&
            !permissionChecker.canPlaceHoneyLure(player, pos)) {
            sendMessage(player, config.messages.cannotPlaceHoneyLure)
            logger.debug("Blocked honey lure placement by ${player.name.string} at $pos")
            return true
        }
        return false
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
