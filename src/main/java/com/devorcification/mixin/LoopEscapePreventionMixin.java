package com.devorcification.mixin;

import com.devorcification.Devorcification;
import com.devorcification.world.LoopDimension;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class LoopEscapePreventionMixin {

    @Mixin(ServerPlayerEntity.class)
    public static class ServerPlayerEscape {
        @Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true)
        private void devorcification$preventEscape(ServerWorld destination, CallbackInfoReturnable<?> cir) {
            ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
            if (self.server.getPlayerManager().isOperator(self.getGameProfile())) {
                return;
            }
            if (LoopDimension.isLoopDimension(self.getWorld())) {
                Devorcification.LOGGER.info("[Devorcification] Blocked changeDimension from The Loop by {}", self.getName().getString());
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
    }

    @Mixin(ServerPlayerInteractionManager.class)
    public static class GameModeEscape {
        @Shadow
        protected ServerPlayerEntity player;

        @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
        private void devorcification$cancelMining(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
            ServerPlayerEntity player = this.player;
            if (player == null) return;
            if (!LoopDimension.isLoopDimension(player.getWorld())) return;
            if (player.server.getPlayerManager().isOperator(player.getGameProfile())) return;
            BlockState state = player.getWorld().getBlockState(pos);
            if (state.isAir() || state.isOf(Blocks.VOID_AIR)) {
                return;
            }
            Devorcification.LOGGER.info("[Devorcification] Blocked block break at {} in The Loop by {}", pos, player.getName().getString());
            cir.setReturnValue(false);
        }
    }
}
