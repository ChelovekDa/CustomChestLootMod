package ru.hcc.customchestloot.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import ru.hcc.customchestloot.util.*;
import ru.hcc.customchestloot.util.Number;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;


@Environment(EnvType.SERVER)
public class CustomLootCommand {

    private final FileManager fileManager =  new FileManager();

    public CustomLootCommand() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("loottable").requires(source -> source.hasPermissionLevel(2))

                .then(CommandManager.literal("create")

                    .then(CommandManager.argument("%s_x".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                        .then(CommandManager.argument("%s_y".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                            .then(CommandManager.argument("%s_z".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                .executes(context -> createCommand(context, Number.FIRST))
                                .then(CommandManager.argument("%s_x".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                    .then(CommandManager.argument("%s_y".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                        .then(CommandManager.argument("%s_z".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                            .executes(context -> createCommand(context, Number.FIRST, Number.SECOND)))))))))

                .then(CommandManager.literal("set")

                        .then(CommandManager.literal("-name")

                                .then(CommandManager.argument("region_name", StringArgumentType.word())
                                    .then(CommandManager.literal("parent")
                                        .then(CommandManager.argument("arg", StringArgumentType.word())
                                            .executes(context -> setCommand(context, "parent", "name-flag"))))

                                    .then(CommandManager.literal("name")
                                        .then(CommandManager.argument("arg", StringArgumentType.word())
                                            .executes(context -> setCommand(context, "name", "name-flag"))))

                                    .then(CommandManager.literal("items")
                                        .then(CommandManager.argument("arg", StringArgumentType.greedyString())
                                            .executes(context -> setCommand(context, "items", "name-flag"))))))

                        .then(CommandManager.literal("-cords")

                                .then(CommandManager.argument("%s_x".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                    .then(CommandManager.argument("%s_y".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                        .then(CommandManager.argument("%s_z".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())

                                                .then(CommandManager.literal("parent")
                                                        .then(CommandManager.argument("arg", StringArgumentType.word())
                                                                .executes(context -> setCommand(context, "parent", "cords-flag"))))

                                                .then(CommandManager.literal("name")
                                                        .then(CommandManager.argument("arg", StringArgumentType.word())
                                                                .executes(context -> setCommand(context, "name", "cords-flag"))))

                                                .then(CommandManager.literal("items")
                                                        .then(CommandManager.argument("arg", StringArgumentType.greedyString())
                                                                .executes(context -> setCommand(context, "items", "cords-flag")))))))))

                .then(CommandManager.literal("update")

                        .then(CommandManager.literal("-name")
                                .then(CommandManager.argument("name", StringArgumentType.word())
                                        .executes(context -> updateCommand(context, "name-flag"))))

                        .then(CommandManager.literal("-cords")
                                .then(CommandManager.argument("%s_x".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                        .then(CommandManager.argument("%s_y".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                                .then(CommandManager.argument("%s_z".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                                        .executes(context -> updateCommand(context, "cords-flag")))))))

                .then(CommandManager.literal("del")

                        .then(CommandManager.literal("-name")

                                .then(CommandManager.argument("name", StringArgumentType.word())
                                    .executes(context -> deleteCommand(context, "name-flag"))))

                        .then(CommandManager.literal("-cords")

                                .then(CommandManager.argument("%s_x".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                    .then(CommandManager.argument("%s_y".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                        .then(CommandManager.argument("%s_z".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                            .executes(context -> deleteCommand(context, "cords-flag"))))))

                        .then(CommandManager.literal("-region")

                                .then(CommandManager.argument("%s_x".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                    .then(CommandManager.argument("%s_y".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                        .then(CommandManager.argument("%s_z".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())

                                            .then(CommandManager.argument("%s_x".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                                .then(CommandManager.argument("%s_y".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                                    .then(CommandManager.argument("%s_z".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                                        .executes(context -> deleteCommand(context, "region-flag"))))))))))
        )));
    }

    private int deleteCommand(CommandContext<ServerCommandSource> context, String flag) {
        HashMap<String, Region> allRegions = fileManager.getAllRegions();

        switch (flag) {

            case "name-flag":
                String name = StringArgumentType.getString(context, "name");

                if (allRegions.containsKey(name)) {
                    fileManager.deleteRegion(allRegions.get(name));
                    context.getSource().sendMessage(Text.literal("§aУспешно удален сундук '%s'!".formatted(name)));
                    return 0;
                }
                else {
                    context.getSource().sendMessage(Text.literal("§cНевозможно удалить сундук под именем '%s', поскольку его не существует!".formatted(name)));
                    return -1;
                }

            case "cords-flag":
                Region region = Region.fromCommandContext(context, Number.FIRST);

                for (Region reg : allRegions.values()) {
                    if (reg.equalsIgnoreName(region)) {
                        fileManager.deleteRegion(reg);
                        context.getSource().sendMessage(Text.literal("§aУспешно удален сундук '%s'!".formatted(reg.name)));
                        return 0;
                    }
                }
                context.getSource().sendMessage(Text.literal("§cНевозможно обновить сундук на %s, поскольку его не существует!".formatted(region.toString())));
                return -1;

            case "region-flag":
                int[] reg1 = Region.fromCommandContext(context, Number.FIRST).getCords();
                int[] reg2 = Region.fromCommandContext(context, Number.SECOND).getCords();

                HashSet<int[]> values = new HashSet<>();
                for (Region reg : allRegions.values()) values.add(reg.getCords());

                BlockPos.iterate(reg1[0], reg1[1], reg1[2], reg2[0], reg2[1], reg2[2]).forEach(pos -> {
                    for (int[] value : values) {
                        if (pos.getX() == value[0] && pos.getY() == value[1] && pos.getZ() == value[2]) {
                            fileManager.deleteRegion(allRegions.get(String.valueOf(value[0]) + value[1] + value[2]));
                            context.getSource().sendMessage(Text.literal("§aУспешно удален сундук '%s'!".formatted(String.valueOf(value[0]) + value[1] + value[2])));
                        }
                    }
                });

                return 0;

            default:
                return 0;
        }
    }

    private int updateCommand(CommandContext<ServerCommandSource> context, String flag) {
        Region region = null;
        HashMap<String, Region> allRegions = fileManager.getAllRegions();

        switch (flag) {

            case "name-flag":
                String name = StringArgumentType.getString(context, "name");

                if (allRegions.containsKey(name)) region = allRegions.get(name);
                else {
                    context.getSource().sendMessage(Text.literal("§cНевозможно обновить сундук под именем '%s', поскольку его не существует!".formatted(name)));
                    return -1;
                }
                break;

            case "cords-flag":
                region = Region.fromCommandContext(context, Number.FIRST);
                boolean is = false;

                for (Region reg : allRegions.values()) {
                    if (reg.equalsIgnoreName(region)) {
                        region = reg;
                        is = true;
                        break;
                    }
                }

                if (!is) {
                    context.getSource().sendMessage(Text.literal("§cНевозможно обновить сундук на %s, поскольку его не существует!".formatted(region.toString())));
                    return -1;
                }
                break;

        }

        assert region != null;
        new Timer().chestUpdate(region, context.getSource().getWorld());
        context.getSource().sendMessage(Text.literal("§aУспешно обновлен сундук '%s'!".formatted(region.name)));

        return 0;
    }

    private int setCommand(CommandContext<ServerCommandSource> context, String arg, String flag) {
        Region region = null;
        HashMap<String, Region> allRegions = fileManager.getAllRegions();

        switch (flag) {
            case "cords-flag":
                region = Region.fromCommandContext(context, Number.FIRST);
                boolean is = false;

                for (Region reg : allRegions.values()) {
                    if (reg.equalsIgnoreName(region)) {
                        region = reg;
                        is = true;
                        break;
                    }
                }

                if (!is) {
                    context.getSource().sendMessage(Text.literal("§cНевозможно обновить сундук на %s, поскольку его не существует!".formatted(region.toString())));
                    return -1;
                }
                break;

            case "name-flag":
                String regionName = StringArgumentType.getString(context, "region_name");

                if (allRegions.containsKey(regionName)) region = allRegions.get(regionName);
                else {
                    context.getSource().sendMessage(Text.literal("§cНевозможно обновить сундук под именем '%s', поскольку его не существует!".formatted(regionName)));
                    return -1;
                }
                break;
        }

        assert region != null;

        String commandArg = StringArgumentType.getString(context, "arg");

        assert commandArg != null;

        switch (arg) {

            case "parent":

                if (allRegions.containsKey(commandArg)) {
                    region.parent = commandArg;
                    context.getSource().sendMessage(Text.literal("§aУспешно установлен родитель %s для %s!".formatted(commandArg, region.name)));
                }
                else { context.getSource().sendMessage(Text.literal("§cНевозможно установить родителя %s для %s, поскольку такого родителя не существует!".formatted(commandArg, region.toString()))); }

                break;

            case "name":

                if (!allRegions.containsKey(commandArg)) {
                    region.name = commandArg;
                    context.getSource().sendMessage(Text.literal("§aУспешно установлено имя %s для %s!".formatted(commandArg, region.toString())));
                }
                else { context.getSource().sendMessage(Text.literal("§cНевозможно установить имя '%s' для %s, поскольку такое имя уже существует!".formatted(commandArg, region.toString()))); }

                break;

            case "items":
                String[] vars = commandArg.split(",");

                for (String var : vars) {
                    ChestItem item = ChestItem.of(var);

                    if (item != null) {
                        region.items.add(item);
                        context.getSource().sendMessage(Text.literal("§aУспешно добавлен предмет %s для %s!".formatted(item.toString(), region.name)));
                    }
                    else { context.getSource().sendMessage(Text.literal("§cНевозможно установить для этого сундука предмет \"%s\", поскольку его объект равен null!".formatted(var))); }
                }

                break;
        }

        fileManager.saveRegion(region);

        return 0;
    }

    private int createCommand(CommandContext<ServerCommandSource> context, Number ...numbers) {
        ArrayList<Region> regions = new ArrayList<>();
        HashMap<String, Region> regionHashMap = fileManager.getAllRegions();
        for (Number num : numbers) regions.add(Region.fromCommandContext(context, num));

        if (regions.size() == 1) {
            Region region = regions.getFirst();

            for (Region reg : regionHashMap.values()) {
                if (reg.equalsIgnoreName(region)) {
                    context.getSource().sendMessage(Text.literal("§cНевозможно сохранить %s поскольку такой сундук уже зарегистрирован!".formatted(region.toString())));
                    return -1;
                }
            }

            BlockPos pos = new BlockPos(region.x, region.y, region.z);

            if (!context.getSource().getWorld().getBlockState(pos).getBlock().equals(Blocks.CHEST)) {
                context.getSource().sendMessage(Text.literal("§cНевозможно сохранить %s поскольку они не содержат сундук!".formatted(region.toString())));
                return -1;
            }

            else {
                context.getSource().sendMessage(Text.literal("§aСундук по координатам %s успешно сохранен под именем %s!".formatted(region.toString(), region.name)));
                fileManager.saveRegion(region);
            }
        }

        else {
            final ServerWorld world = context.getSource().getWorld();
            final ServerCommandSource source = context.getSource();
            AtomicInteger count = new AtomicInteger(0);

            HashSet<int[]> values = new HashSet<>();
            for (Region region : regionHashMap.values()) values.add(region.getCords());

            BlockPos.iterate(
                    regions.getFirst().x, regions.getFirst().y, regions.getFirst().z,
                    regions.get(1).x, regions.get(1).y, regions.get(1).z)
                    .forEach(pos -> {

                        if (world.getBlockState(pos).getBlock().equals(Blocks.CHEST)) {
                            boolean is = false;

                            for (int[] value : values) {
                                if (pos.getX() == value[0] && pos.getY() == value[1] && pos.getZ() == value[2]) {
                                    is = true;
                                    break;
                                }
                            }

                            if (!is) {
                                Region region = new Region(pos.getX(), pos.getY(), pos.getZ());
                                fileManager.saveRegion(region);
                                source.sendMessage(Text.literal("§aСундук по координатам %s успешно сохранен под именем '%s'!".formatted(region.toString(), region.name)));
                                count.incrementAndGet();
                            }
                        }
            });

            if (count.get() > 0) source.sendMessage(Text.literal("§aУспешно сохранены сундуки: %s.".formatted(count)));
            else source.sendMessage(Text.literal("§cНе было сохранено ни одного сундука!"));

        }

        return 0;
    }

}