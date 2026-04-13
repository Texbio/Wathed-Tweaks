package com.texbio.wathedtweaks.mixin;

import com.texbio.wathedtweaks.RoomsConfig;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Replaces the room key distribution loop in HarpyExpressTrainMapEffect.
 *
 * We inject right AFTER the last train setup call (setTime) and cancel the
 * rest of the original method, running our own loop that uses RoomsConfig's
 * key pool instead of the hardcoded Room 1-7 cycle.
 *
 * remap = false on @Mixin because HarpyExpressTrainMapEffect is a Wathe class,
 * not a Minecraft class. The @At target string uses Wathe's own descriptor and
 * also does not need remapping.
 */
@Mixin(targets = "dev.doctor4t.wathe.game.mapeffect.HarpyExpressTrainMapEffect", remap = false)
public abstract class HarpyExpressTrainMapEffectMixin {

    @Inject(
        method = "initializeMapEffects",
        at = @At(
            value = "INVOKE",
            // Last train setup call — inject immediately after it, before the player loop
            target = "Ldev/doctor4t/wathe/cca/TrainWorldComponent;setTime(I)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true,
        remap = false
    )
    private void afterTrainSetup(ServerWorld serverWorld, List<ServerPlayerEntity> players, CallbackInfo ci) {
        RoomsConfig config = RoomsConfig.get(serverWorld.getServer());
        List<String> pool = config.buildKeyPool();

        // Shuffle in our own loop exactly as the original does
        Collections.shuffle(players);

        for (int i = 0; i < players.size(); i++) {
            ServerPlayerEntity player = players.get(i);

            // --- Key ---
            // Only give a key if the pool is non-empty; if empty, players get no key
            if (!pool.isEmpty()) {
                String roomName = pool.get(i % pool.size());
                ItemStack keyStack = new ItemStack(WatheItems.KEY);
                final String finalRoomName = roomName;
                keyStack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT,
                    component -> new LoreComponent(
                        Text.literal(finalRoomName)
                            .getWithStyle(Style.EMPTY.withItalic(false).withColor(0xFF8C00))
                    ));
                player.giveItemStack(keyStack);
            }

            // --- Letter ---
            // Replicates original letter logic. Room type is based on pool index:
            //   index 0          → grand_suite  (was Room 1)
            //   index 1, 2       → cabin_suite  (were Rooms 2-3)
            //   index 3 and up   → twin_cabin   (were Rooms 4-7, and any extra/custom rooms)
            wathedTweaks$giveLetter(player, pool.isEmpty() ? 3 : (i % pool.size()));
        }

        ci.cancel();
    }

    @Unique
    private static void wathedTweaks$giveLetter(ServerPlayerEntity player, int poolIndex) {
        ItemStack letter = new ItemStack(WatheItems.LETTER);
        letter.set(DataComponentTypes.ITEM_NAME, Text.translatable(letter.getTranslationKey()));

        int letterColor = 0xC5AE8B;
        String tipString = "tip.letter.";

        String roomType = switch (poolIndex) {
            case 0 -> "grand_suite";
            case 1, 2 -> "cabin_suite";
            default -> "twin_cabin";
        };
        final String finalRoomType = roomType;

        letter.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, component -> {
            List<Text> text = new ArrayList<>();
            UnaryOperator<Style> stylizer = style -> style.withItalic(false).withColor(letterColor);

            Text displayName = player.getDisplayName();
            String name = displayName != null
                ? displayName.getString()
                : player.getName().getString();

            // Strip supporter icon if present (original behaviour)
            if (!name.isEmpty() && name.charAt(name.length() - 1) == '\uE780') {
                name = name.substring(0, name.length() - 1);
            }
            final String finalName = name;

            text.add(Text.translatable(tipString + "name", finalName)
                .styled(s -> s.withItalic(false).withColor(0xFFFFFF)));
            text.add(Text.translatable(tipString + "room").styled(stylizer));
            text.add(Text.translatable(tipString + "tooltip1",
                Text.translatable(tipString + "room." + finalRoomType).getString()
            ).styled(stylizer));
            text.add(Text.translatable(tipString + "tooltip2").styled(stylizer));

            return new LoreComponent(text);
        });

        player.giveItemStack(letter);
    }
}
