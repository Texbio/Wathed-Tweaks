package com.texbio.wathedtweaks;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.ArrayList;
import java.util.List;

public class RoomsConfig extends PersistentState {

    public static final int DEFAULT_MAX_ROOMS = 8;
    public static final int MIN_ROOMS = 0;
    public static final int MAX_ROOMS = 255;

    private static final String SAVE_ID = "wathed_tweaks_rooms";

    private int maxRooms = DEFAULT_MAX_ROOMS;
    private final List<String> customRooms = new ArrayList<>();
    private boolean customGiveEnabled = false;

    // -------------------------------------------------------------------------
    // Key pool — built fresh each time, consumed at game start
    // -------------------------------------------------------------------------

    /**
     * Returns the full ordered list of room names to distribute to players.
     * Standard rooms come first (Room 1 .. Room maxRooms), then custom rooms
     * if custom giving is enabled. Returns an empty list if nothing is enabled.
     */
    public List<String> buildKeyPool() {
        List<String> pool = new ArrayList<>();
        for (int i = 1; i <= maxRooms; i++) {
            pool.add("Room " + i);
        }
        if (customGiveEnabled) {
            pool.addAll(customRooms);
        }
        return pool;
    }

    // -------------------------------------------------------------------------
    // PersistentState serialization
    // -------------------------------------------------------------------------

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("maxRooms", maxRooms);
        nbt.putBoolean("customGiveEnabled", customGiveEnabled);

        NbtList list = new NbtList();
        for (String room : customRooms) {
            list.add(NbtString.of(room));
        }
        nbt.put("customRooms", list);

        return nbt;
    }

    private static RoomsConfig fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        RoomsConfig config = new RoomsConfig();
        config.maxRooms = nbt.getInt("maxRooms");
        config.customGiveEnabled = nbt.getBoolean("customGiveEnabled");

        NbtList list = nbt.getList("customRooms", NbtString.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) {
            config.customRooms.add(list.getString(i));
        }

        return config;
    }

    private static final PersistentState.Type<RoomsConfig> TYPE = new PersistentState.Type<>(
            RoomsConfig::new,
            RoomsConfig::fromNbt,
            null
    );

    // -------------------------------------------------------------------------
    // Access
    // -------------------------------------------------------------------------

    public static RoomsConfig get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        PersistentStateManager manager = overworld.getPersistentStateManager();
        return manager.getOrCreate(TYPE, SAVE_ID);
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public int getMaxRooms() {
        return maxRooms;
    }

    public void setMaxRooms(int value) {
        maxRooms = Math.max(MIN_ROOMS, Math.min(MAX_ROOMS, value));
        markDirty();
    }

    public List<String> getCustomRooms() {
        return customRooms;
    }

    public void addCustomRoom(String name) {
        customRooms.add(name);
        markDirty();
    }

    public boolean removeCustomRoom(String name) {
        boolean removed = customRooms.remove(name);
        if (removed) markDirty();
        return removed;
    }

    public boolean isCustomGiveEnabled() {
        return customGiveEnabled;
    }

    public void toggleCustomGive() {
        customGiveEnabled = !customGiveEnabled;
        markDirty();
    }
}
