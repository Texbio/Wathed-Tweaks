package com.texbio.wathedtweaks;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class RoomsConfig extends PersistentState {

    public static final int DEFAULT_MAX_ROOMS = 8;
    public static final int MIN_ROOMS = 1;
    public static final int MAX_ROOMS = 255;

    private static final String SAVE_ID = "wathed_tweaks_rooms";

    private int maxRooms = DEFAULT_MAX_ROOMS;

    // -------------------------------------------------------------------------
    // PersistentState serialization
    // -------------------------------------------------------------------------

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("maxRooms", maxRooms);
        return nbt;
    }

    private static RoomsConfig fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        RoomsConfig config = new RoomsConfig();
        config.maxRooms = nbt.getInt("maxRooms");
        return config;
    }

    private static final PersistentState.Type<RoomsConfig> TYPE = new PersistentState.Type<>(
            RoomsConfig::new,
            RoomsConfig::fromNbt,
            null
    );

    // -------------------------------------------------------------------------
    // Access — always go through the overworld's PersistentStateManager
    // -------------------------------------------------------------------------

    public static RoomsConfig get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        PersistentStateManager manager = overworld.getPersistentStateManager();
        return manager.getOrCreate(TYPE, SAVE_ID);
    }

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    public int getMaxRooms() {
        return maxRooms;
    }

    public void setMaxRooms(int value) {
        maxRooms = Math.max(MIN_ROOMS, Math.min(MAX_ROOMS, value));
        markDirty();
    }
}
