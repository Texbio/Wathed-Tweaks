package com.texbio.wathedtweaks.mixin;

import com.texbio.wathedtweaks.RoomsConfig;
import dev.doctor4t.wathe.game.mapeffect.HarpyExpressTrainMapEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.List;

@Mixin(value = HarpyExpressTrainMapEffect.class, remap = false)
public class HarpyExpressTrainMapEffectMixin {

    // Replaces the hardcoded `% 7` in:
    //   roomNumber = roomNumber % 7 + 1;
    // with the configured max rooms value loaded from world save.
    @ModifyConstant(method = "initializeMapEffects", constant = @Constant(intValue = 7))
    private int modifyRoomCount(int original, ServerWorld serverWorld, List<?> players) {
        MinecraftServer server = serverWorld.getServer();
        return RoomsConfig.get(server).getMaxRooms();
    }
}
