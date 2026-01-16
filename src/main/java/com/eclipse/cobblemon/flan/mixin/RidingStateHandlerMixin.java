package com.eclipse.cobblemon.flan.mixin;

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.server.pokemon.update.ServerboundUpdateRidingStatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Mixin to suppress IndexOutOfBoundsException spam from Cobblemon's riding state handler.
 * This is a workaround for a Cobblemon 1.7.1 bug where HorseState.decode receives
 * malformed packets.
 */
@Mixin(targets = "com.cobblemon.mod.common.net.serverhandling.pokemon.update.ServerboundUpdateRidingStateHandler", remap = false)
public abstract class RidingStateHandlerMixin implements ServerNetworkPacketHandler<ServerboundUpdateRidingStatePacket> {

    /**
     * @author cobblemon-flan
     * @reason Suppress IndexOutOfBoundsException from malformed riding state packets
     */
    @Overwrite
    public void handle(ServerboundUpdateRidingStatePacket packet, MinecraftServer server, ServerPlayerEntity player) {
        try {
            var entity = player.getWorld().getEntityById(packet.getEntity());
            if (entity == null) return;
            if (!(entity instanceof PokemonEntity pokemonEntity)) return;
            if (entity.getControllingPassenger() != player) return;
            var buffer = packet.getData();
            if (buffer == null) return;

            var ridingController = pokemonEntity.getRidingController();
            if (ridingController == null) return;

            var context = ridingController.getContext();
            if (context != null && context.getSettings() != null) {
                if (!context.getSettings().getKey().equals(packet.getBehaviour())) {
                    ridingController.changeBehaviour(packet.getBehaviour());
                }
            }

            if (ridingController.getContext() != null && ridingController.getContext().getState() != null) {
                ridingController.getContext().getState().decode(buffer);
            }
        } catch (IndexOutOfBoundsException e) {
            // Silently ignore malformed packet - this is a Cobblemon bug
        } catch (Exception e) {
            // Silently ignore other errors to prevent spam
        }
    }
}
