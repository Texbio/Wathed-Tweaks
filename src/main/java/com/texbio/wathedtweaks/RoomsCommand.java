package com.texbio.wathedtweaks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public class RoomsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("wathe:rooms")
                .requires(source -> source.hasPermissionLevel(2))

                // /wathe:rooms setmax <0-255>
                .then(CommandManager.literal("setmax")
                    .then(CommandManager.argument("max", IntegerArgumentType.integer(
                            RoomsConfig.MIN_ROOMS, RoomsConfig.MAX_ROOMS))
                        .executes(context -> {
                            int max = IntegerArgumentType.getInteger(context, "max");
                            RoomsConfig config = RoomsConfig.get(context.getSource().getServer());
                            config.setMaxRooms(max);

                            String msg = max == 0
                                ? "Standard room keys disabled. Only custom rooms will be given (if enabled)."
                                : "Max standard rooms set to " + max + " (takes effect on next game start).";

                            context.getSource().sendFeedback(() -> Text.literal(msg), true);
                            return 1;
                        })))

                // /wathe:rooms giveRoomKey <name or number>
                // If the input is a plain integer, "Room " is prepended automatically.
                .then(CommandManager.literal("giveRoomKey")
                    .then(CommandManager.argument("roomName", StringArgumentType.greedyString())
                        .executes(context -> {
                            String input = StringArgumentType.getString(context, "roomName").trim();
                            String roomName;
                            try {
                                Integer.parseInt(input);
                                roomName = "Room " + input;
                            } catch (NumberFormatException e) {
                                roomName = input;
                            }

                            ItemStack stack = new ItemStack(WatheItems.KEY);
                            final String finalName = roomName;
                            stack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT,
                                component -> new LoreComponent(
                                    Text.literal(finalName)
                                        .getWithStyle(Style.EMPTY.withItalic(false).withColor(0xFF8C00))
                                ));

                            ServerCommandSource source = context.getSource();
                            if (source.getPlayer() != null) {
                                source.getPlayer().giveItemStack(stack);
                            }
                            return 1;
                        })))

                // /wathe:rooms customadd <full name including "Room" if desired>
                .then(CommandManager.literal("customadd")
                    .then(CommandManager.argument("roomName", StringArgumentType.greedyString())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "roomName").trim();
                            RoomsConfig config = RoomsConfig.get(context.getSource().getServer());
                            config.addCustomRoom(name);
                            context.getSource().sendFeedback(
                                () -> Text.literal("Added custom room: \"" + name + "\""), true);
                            return 1;
                        })))

                // /wathe:rooms customremove <name>
                .then(CommandManager.literal("customremove")
                    .then(CommandManager.argument("roomName", StringArgumentType.greedyString())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "roomName").trim();
                            RoomsConfig config = RoomsConfig.get(context.getSource().getServer());
                            boolean removed = config.removeCustomRoom(name);
                            if (removed) {
                                context.getSource().sendFeedback(
                                    () -> Text.literal("Removed custom room: \"" + name + "\""), true);
                            } else {
                                context.getSource().sendFeedback(
                                    () -> Text.literal("No custom room named \"" + name + "\" found."), false);
                            }
                            return 1;
                        })))

                // /wathe:rooms customlist
                .then(CommandManager.literal("customlist")
                    .executes(context -> {
                        RoomsConfig config = RoomsConfig.get(context.getSource().getServer());
                        List<String> rooms = config.getCustomRooms();
                        String status = config.isCustomGiveEnabled() ? "enabled" : "disabled";

                        StringBuilder sb = new StringBuilder();
                        sb.append("Custom room giving: ").append(status).append("\n");
                        if (rooms.isEmpty()) {
                            sb.append("No custom rooms defined.");
                        } else {
                            sb.append("Custom rooms (").append(rooms.size()).append("):");
                            for (int i = 0; i < rooms.size(); i++) {
                                sb.append("\n  ").append(i + 1).append(". ").append(rooms.get(i));
                            }
                        }

                        String message = sb.toString();
                        context.getSource().sendFeedback(() -> Text.literal(message), false);
                        return 1;
                    }))

                // /wathe:rooms customtogglegive
                .then(CommandManager.literal("customtogglegive")
                    .executes(context -> {
                        RoomsConfig config = RoomsConfig.get(context.getSource().getServer());
                        config.toggleCustomGive();
                        String state = config.isCustomGiveEnabled() ? "enabled" : "disabled";
                        context.getSource().sendFeedback(
                            () -> Text.literal("Custom room giving is now " + state + "."), true);
                        return 1;
                    }))
        );
    }
}
