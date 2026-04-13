package com.ae2tweaks.mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.TreeMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.client.gui.me.patternaccess.PatternContainerRecord;
import appeng.core.definitions.AEBlocks;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;

/**
 * When the player shift-clicks an encoded pattern in their inventory while the Pattern Access Terminal is open,
 * move it into the first available empty slot belonging to a Molecular Assembler.
 * <p>
 * Only assembler-compatible patterns are accepted (crafting, smithing, stonecutting).
 * Processing patterns are silently blocked since assemblers cannot use them.
 * <p>
 * Supports rapid shift-clicking (Mouse Tweaks drag) and shift+double-click (move all matching).
 * After each move, client prediction state is synced so the next operation sees a clean cursor.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class PatternAccessTermScreenMixin {

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void ae2ct$onShiftClickPlayerSlot(Slot slot, int slotIdx, int mouseButton, ClickType clickType,
            CallbackInfo ci) {
        if (!((Object) this instanceof PatternAccessTermScreen<?> screen)) {
            return;
        }

        if (clickType != ClickType.QUICK_MOVE || mouseButton != 0 || slot == null || !slot.hasItem()) {
            return;
        }

        // Only intercept encoded patterns
        if (!PatternDetailsHelper.isEncodedPattern(slot.getItem())) {
            return;
        }

        // Only allow patterns that molecular assemblers can use (crafting, smithing, stonecutting).
        // Processing patterns and any other non-assembler patterns are blocked.
        // This check works with any mod's patterns (Extended AE, etc.) without hard dependencies.
        var mc = Minecraft.getInstance();
        if (mc.level != null) {
            var details = PatternDetailsHelper.decodePattern(slot.getItem(), mc.level);
            if (!(details instanceof IMolecularAssemblerSupportedPattern)) {
                ci.cancel();
                return;
            }
        }

        var byId = ((PatternAccessTermScreenAccessor) screen).ae2ct$getById();
        var assemblerItem = AEBlocks.MOLECULAR_ASSEMBLER.block().asItem();

        // Collect assembler records sorted in visual order:
        //   - Groups sorted alphabetically by name (matching terminal display)
        //   - Records within each group sorted by their natural order (sort value)
        var sortedByGroup = new TreeMap<String, ArrayList<PatternContainerRecord>>();
        for (var record : byId.values()) {
            var group = record.getGroup();
            if (group.icon() == null || group.icon().getItem() != assemblerItem) {
                continue;
            }
            sortedByGroup
                    .computeIfAbsent(group.name().getString().toLowerCase(Locale.ROOT), k -> new ArrayList<>())
                    .add(record);
        }

        // Find the first empty slot in visual order (topmost first)
        PatternContainerRecord targetRecord = null;
        int targetSlot = -1;

        outer:
        for (var records : sortedByGroup.values()) {
            Collections.sort(records);
            for (var record : records) {
                var inv = record.getInventory();
                for (int i = 0; i < inv.size(); i++) {
                    if (inv.getStackInSlot(i).isEmpty()) {
                        targetRecord = record;
                        targetSlot = i;
                        break outer;
                    }
                }
            }
        }

        if (targetRecord == null) {
            // No empty assembler slots — silently block the shift-click
            ci.cancel();
            return;
        }

        // Save the pattern before PICKUP removes it from the slot via client prediction
        var patternStack = slot.getItem().copy();

        // Step 1: Pick up the pattern from the player inventory.
        // Sends ServerboundContainerClickPacket and updates client prediction.
        if (mc.gameMode != null && mc.player != null) {
            mc.gameMode.handleInventoryMouseClick(
                    screen.getMenu().containerId,
                    slot.index,
                    0,
                    ClickType.PICKUP,
                    mc.player);
        }

        // Step 2: Place into the target assembler slot via AE2's packet.
        var packet = new InventoryActionPacket(
                InventoryAction.PICKUP_OR_SET_DOWN,
                targetSlot,
                targetRecord.getServerId());
        NetworkHandler.instance().sendToServer(packet);

        // Step 3: Sync client prediction state to match server after both packets.
        // Critical for Mouse Tweaks (rapid shift-click) and shift+double-click (move all matching).
        screen.getMenu().setCarried(ItemStack.EMPTY);
        targetRecord.getInventory().setItemDirect(targetSlot, patternStack);

        ci.cancel();
    }
}
