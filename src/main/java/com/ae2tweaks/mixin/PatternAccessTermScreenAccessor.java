package com.ae2tweaks.mixin;

import java.util.HashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.client.gui.me.patternaccess.PatternContainerRecord;

@Mixin(PatternAccessTermScreen.class)
public interface PatternAccessTermScreenAccessor {

    @Accessor(value = "byId", remap = false)
    HashMap<Long, PatternContainerRecord> ae2ct$getById();
}
