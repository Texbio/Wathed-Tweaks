package com.ae2tweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import appeng.api.config.Settings;
import appeng.api.config.ViewItems;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.me.common.MEStorageScreen;

/**
 * When the ME terminal view mode is set to "Craftable", prevent shift-clicking encoded patterns
 * from the player inventory into the ME network. Other items are still allowed.
 *
 * Targets vanilla AbstractContainerScreen because the mixin AP cannot generate refmap entries
 * for methods on mod classes (AE2). A runtime instanceof check filters to ME terminals only.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MEStorageScreenMixin {

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void ae2ct$blockPatternInsertInCraftableView(Slot slot, int slotIdx, int mouseButton,
            ClickType clickType, CallbackInfo ci) {
        if (!((Object) this instanceof MEStorageScreen<?> screen)) {
            return;
        }

        if (clickType != ClickType.QUICK_MOVE || slot == null || !slot.hasItem()) {
            return;
        }

        if (!PatternDetailsHelper.isEncodedPattern(slot.getItem())) {
            return;
        }

        var viewMode = ((IConfigurableObject) screen.getMenu()).getConfigManager().getSetting(Settings.VIEW_MODE);
        if (viewMode == ViewItems.CRAFTABLE) {
            ci.cancel();
        }
    }
}
