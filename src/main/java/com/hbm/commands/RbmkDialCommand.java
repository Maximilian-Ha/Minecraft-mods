package com.hbm.commands;

import com.hbm.blockentity.machine.rbmk.RBMKDials;
import com.hbm.blockentity.machine.rbmk.RBMKDials.RBMKKeys;
import com.hbm.saveddata.RBMKDialsSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Ersetzt die Gamerules, ueber die das Original die RBMK-Dials gestellt hat.
 * /ntmrbmk list | get &lt;dial&gt; | set &lt;dial&gt; &lt;wert&gt; | reset
 */
public class RbmkDialCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("ntmrbmk")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("list").executes(RbmkDialCommand::list))
                        .then(Commands.literal("reset").executes(RbmkDialCommand::reset))
                        .then(Commands.literal("get")
                                .then(Commands.argument("dial", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for(RBMKKeys key : RBMKKeys.values()) builder.suggest(key.keyString);
                                            return builder.buildFuture();
                                        })
                                        .executes(RbmkDialCommand::get)
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("dial", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for(RBMKKeys key : RBMKKeys.values()) builder.suggest(key.keyString);
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("value", StringArgumentType.word())
                                                .executes(RbmkDialCommand::set)
                                        )
                                )
                        )
        );
    }

    private static int list(CommandContext<CommandSourceStack> context) {

        RBMKDialsSavedData data = RBMKDials.getData(context.getSource().getLevel());
        if(data == null) return 0;

        for(RBMKKeys key : RBMKKeys.values()) {
            String value = read(data, key);
            boolean isDefault = !data.has(key.keyString);
            context.getSource().sendSuccess(() -> Component.literal(key.keyString + " = " + value + (isDefault ? " (Standard)" : "")), false);
        }

        return RBMKKeys.values().length;
    }

    private static int get(CommandContext<CommandSourceStack> context) {

        RBMKKeys key = RBMKKeys.byName(StringArgumentType.getString(context, "dial"));
        if(key == null) return fail(context, "Unbekannter Dial");

        RBMKDialsSavedData data = RBMKDials.getData(context.getSource().getLevel());
        if(data == null) return 0;

        String value = read(data, key);
        context.getSource().sendSuccess(() -> Component.literal(key.keyString + " = " + value), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context) {

        RBMKKeys key = RBMKKeys.byName(StringArgumentType.getString(context, "dial"));
        if(key == null) return fail(context, "Unbekannter Dial");

        RBMKDialsSavedData data = RBMKDials.getData(context.getSource().getLevel());
        if(data == null) return 0;

        String raw = StringArgumentType.getString(context, "value");

        try {
            if(key.defValue instanceof Boolean) {
                data.setBoolean(key.keyString, Boolean.parseBoolean(raw));
            } else if(key.defValue instanceof Integer) {
                data.setInt(key.keyString, Integer.parseInt(raw));
            } else {
                data.setDouble(key.keyString, Double.parseDouble(raw));
            }
        } catch(NumberFormatException ex) {
            return fail(context, "Wert passt nicht zum Typ des Dials");
        }

        String value = read(data, key);
        context.getSource().sendSuccess(() -> Component.literal(key.keyString + " = " + value), true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) {

        RBMKDialsSavedData data = RBMKDials.getData(context.getSource().getLevel());
        if(data == null) return 0;

        data.clear();
        context.getSource().sendSuccess(() -> Component.literal("Alle RBMK-Dials auf Standard zurueckgesetzt"), true);
        return 1;
    }

    private static String read(RBMKDialsSavedData data, RBMKKeys key) {
        if(key.defValue instanceof Boolean def) return String.valueOf(data.getBoolean(key.keyString, def));
        if(key.defValue instanceof Integer def) return String.valueOf(data.getInt(key.keyString, def));
        return String.valueOf(data.getDouble(key.keyString, (Double) key.defValue));
    }

    private static int fail(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendFailure(Component.literal(message));
        return 0;
    }
}
