package com.texbio.wathedtweaks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class WathedTweaks implements ModInitializer {

    public static final String MOD_ID = "wathed_tweaks";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            RoomsCommand.register(dispatcher)
        );
    }
}
