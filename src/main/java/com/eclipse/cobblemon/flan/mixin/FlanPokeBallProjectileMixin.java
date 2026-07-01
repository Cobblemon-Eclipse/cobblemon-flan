package com.eclipse.cobblemon.flan.mixin;

import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes Flan ignore Poke Ball projectiles in its projectile-hit handler, so catching a wild
 * Pokemon is governed ONLY by cobblemon-flan's pokemon_catch permission (checked separately on
 * THROWN_POKEBALL_HIT) and never by Flan's built-in hurt_animal.
 *
 * Flan treats a thrown Poke Ball as a generic projectile attack and, for a wild Pokemon, falls
 * back to hurt_animal — discarding the ball before Cobblemon's catch logic runs. Returning false
 * from projectileHit for an EmptyPokeBallEntity lets the ball through. Directly hitting a Pokemon
 * (melee, arrows, etc.) is unaffected and still governed by hurt_animal, exactly like vanilla.
 */
@Mixin(targets = "io.github.flemmli97.flan.event.EntityInteractEvents", remap = false)
public class FlanPokeBallProjectileMixin {

    @Inject(method = "projectileHit", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cobblemonFlan$ignorePokeBalls(ProjectileEntity proj, HitResult res, CallbackInfoReturnable<Boolean> cir) {
        if (proj instanceof EmptyPokeBallEntity) {
            cir.setReturnValue(false);
        }
    }
}
