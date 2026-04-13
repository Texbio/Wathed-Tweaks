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

public class RoomsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("wathe:rooms")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.literal("setmax")
                        .then(
                            CommandManager.argument("max", IntegerArgumentType.integer(
                                    RoomsConfig.MIN_ROOMS, RoomsConfig.MAX_ROOMS))
                                .executes(context -> {
                                    int max = IntegerArgumentType.getInteger(context, "max");
                                    RoomsConfig config = RoomsConfig.get(context.getSource().getServer());
                                    config.setMaxRooms(max);
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("Max rooms set to " + max + " (takes effect on next game start)"),
                                        true
                                    );
                                    return 1;
                                })
                        )
                )
                .then(
                    CommandManager.literal("giveRoomKey")
                        .then(
                            CommandManager.argument("roomName", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String roomName = StringArgumentType.getString(context, "roomName");
                                    ItemStack stack = new ItemStack(WatheItems.KEY);
                                    stack.apply(
                                        DataComponentTypes.LORE,
                                        LoreComponent.DEFAULT,
                                        component -> new LoreComponent(
                                            Text.literal(roomName)
                                                .getWithStyle(Style.EMPTY.withItalic(false).withColor(0xFF8C00))
                                        )
                                    );
                                    ServerCommandSource source = context.getSource();
                                    if (source.getPlayer() != null) {
                                        source.getPlayer().giveItemStack(stack);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}
