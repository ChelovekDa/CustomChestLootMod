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
import ru.hcc.customchestloot.util.Timer;
import java.util.*;


@Environment(EnvType.SERVER)
public class CustomLootCommand {

    private final FileManager fileManager =  new FileManager();

    public CustomLootCommand() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("loottable").requires(source -> source.hasPermissionLevel(2))

                .then(CommandManager.literal("create")

                    .then(CommandManager.argument("loottable_name", StringArgumentType.word())
                        .then(CommandManager.argument("%s_x".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                            .then(CommandManager.argument("%s_y".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                .then(CommandManager.argument("%s_z".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                    .executes(context -> createCommand(context, Number.FIRST))
                                    .then(CommandManager.argument("%s_x".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                        .then(CommandManager.argument("%s_y".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                            .then(CommandManager.argument("%s_z".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                                .executes(context -> createCommand(context, Number.FIRST, Number.SECOND))))))))))

                .then(CommandManager.literal("update")

                        .then(CommandManager.literal("-name")
                                .then(CommandManager.argument("name", StringArgumentType.word())
                                        .executes(context -> updateCommand(context, "name-flag"))))

                        .then(CommandManager.literal("-cords")
                                .then(CommandManager.argument("%s_x".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                        .then(CommandManager.argument("%s_y".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                                .then(CommandManager.argument("%s_z".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                                        .executes(context -> updateCommand(context, "cords-flag"))))))

                        .then(CommandManager.literal("-region")
                                .then(CommandManager.argument("%s_x".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                        .then(CommandManager.argument("%s_y".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())
                                                .then(CommandManager.argument("%s_z".formatted(Number.FIRST.getNumber()), IntegerArgumentType.integer())

                                                        .then(CommandManager.argument("%s_x".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                                                .then(CommandManager.argument("%s_y".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                                                        .then(CommandManager.argument("%s_z".formatted(Number.SECOND.getNumber()), IntegerArgumentType.integer())
                                                                                .executes(context -> updateCommand(context, "region-flag"))))))))))

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
        switch (flag) {

            case "name-flag":
                String name = StringArgumentType.getString(context, "name");

                if (fileManager.getLootTableNames().contains(name)) {
                    fileManager.deleteLT(name);
                    context.getSource().sendMessage(Text.literal("§aУспешно удалена таблица '%s'!".formatted(name)));
                    return 0;
                }
                else {
                    context.getSource().sendMessage(Text.literal("§cНевозможно удалить таблицу '%s', поскольку её не существует!".formatted(name)));
                    return -1;
                }

            case "cords-flag":
                int[] cords = new int[] {
                        IntegerArgumentType.getInteger(context, "%s_x".formatted(Number.FIRST.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_y".formatted(Number.FIRST.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_z".formatted(Number.FIRST.getNumber()))
                };

                if (Arrays.asList(fileManager.getAllCords()).contains(cords)) {
                    for (LootTable lootTable : fileManager.getAllLootTables()) {
                        if (lootTable.chests.contains(cords)) {
                            lootTable.chests.remove(cords);
                            fileManager.saveLT(lootTable);
                            context.getSource().sendMessage(Text.literal("§aСундук %s успешно удален!".formatted(String.valueOf(cords[0]) + cords[1] + cords[2])));
                            return 0;
                        }
                    }
                }
                else {
                    context.getSource().sendMessage(Text.literal("§cНевозможно обновить сундук на %s, поскольку его не существует!".formatted(String.valueOf(cords[0]) + cords[1] + cords[2])));
                    return -1;
                }

            case "region-flag":
                int[] first = new int[] {
                        IntegerArgumentType.getInteger(context, "%s_x".formatted(Number.FIRST.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_y".formatted(Number.FIRST.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_z".formatted(Number.FIRST.getNumber()))
                };

                int[] second = new int[] {
                        IntegerArgumentType.getInteger(context, "%s_x".formatted(Number.SECOND.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_y".formatted(Number.SECOND.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_z".formatted(Number.SECOND.getNumber()))
                };

                BlockPos min = new BlockPos(
                        Math.min(first[0], second[0]),
                        Math.min(first[1], second[1]),
                        Math.min(first[2], second[2])
                );
                BlockPos max = new BlockPos(
                        Math.max(first[0], second[0]),
                        Math.max(first[1], second[1]),
                        Math.max(first[2], second[2])
                );

                int count = 0;
                ArrayList<LootTable> tables = fileManager.getAllLootTables();

                for (BlockPos pos : BlockPos.iterate(min, max)) {
                    for (LootTable lootTable : tables) if (lootTable.chests.remove( new int[] {pos.getX(), pos.getY(), pos.getZ()})) {
                        count++;
                        break;
                    }
                }

                for (LootTable lootTable : tables) fileManager.saveLT(lootTable);
                context.getSource().sendMessage(Text.literal("§aУспешно удалено элементов: %s!".formatted(String.valueOf(count))));

                return 0;

            default:
                return 0;
        }
    }

    private int updateCommand(CommandContext<ServerCommandSource> context, String flag) {
        final ServerWorld world = context.getSource().getWorld();

        switch (flag) {

            case "name-flag":
                String name = StringArgumentType.getString(context, "name");

                if (fileManager.getLootTableNames().contains(name)) {
                    new Timer().restoreChests(Objects.requireNonNull(fileManager.getLootTable(name)), world);
                    context.getSource().sendMessage(Text.literal("§aТаблица %s успешно обновлена!".formatted(name)));
                    return 0;
                }
                else {
                    context.getSource().sendMessage(Text.literal("§cТаблицы '%s' не существует!".formatted(name)));
                    return -1;
                }

            case "cords-flag":
                int[] cords = new int[] {
                        IntegerArgumentType.getInteger(context, "%s_x".formatted(Number.FIRST.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_y".formatted(Number.FIRST.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_z".formatted(Number.FIRST.getNumber()))
                };

                if (Arrays.asList(fileManager.getAllCords()).contains(cords)) {
                    ArrayList<int[]> object = new ArrayList<>();
                    object.add(cords);
                    new Timer().chestsUpdate(object, world);
                    context.getSource().sendMessage(Text.literal("§aСундук %s успешно обновлен!".formatted(String.valueOf(cords[0]) + cords[1] + cords[2])));
                    return 0;
                }

                context.getSource().sendMessage(Text.literal("§cНевозможно обновить сундук на %s, поскольку он не прикреплен ни к одной из таблиц лута!".formatted(String.valueOf(cords[0]) + cords[1] + cords[2])));
                return -1;

            case "-region":

                int[] first = {
                        IntegerArgumentType.getInteger(context, "%s_x".formatted(Number.FIRST.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_y".formatted(Number.FIRST.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_z".formatted(Number.FIRST.getNumber()))
                };
                int[] second = {
                        IntegerArgumentType.getInteger(context, "%s_x".formatted(Number.SECOND.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_y".formatted(Number.SECOND.getNumber())),
                        IntegerArgumentType.getInteger(context, "%s_z".formatted(Number.SECOND.getNumber()))
                };

                BlockPos min = new BlockPos(
                        Math.min(first[0], second[0]),
                        Math.min(first[1], second[1]),
                        Math.min(first[2], second[2])
                );
                BlockPos max = new BlockPos(
                        Math.max(first[0], second[0]),
                        Math.max(first[1], second[1]),
                        Math.max(first[2], second[2])
                );

                int count = 0;
                ArrayList<int[]> coordinates = (ArrayList<int[]>) Arrays.asList(fileManager.getAllCords());
                ArrayList<int[]> blocks = new ArrayList<>();

                for (BlockPos pos : BlockPos.iterate(min, max)) {
                    int[] cord = new int[] {pos.getX(), pos.getY(), pos.getZ()};
                    if (coordinates.contains(cord)) {
                        blocks.add(cord);
                        count++;
                    }
                }

                coordinates.clear();
                new Timer().chestsUpdate(blocks, world);

                context.getSource().sendMessage(Text.literal("§aУспешно обновлено сундуков: %s".formatted(String.valueOf(count))));

                break;
        }
        return 0;
    }

    private int createCommand(CommandContext<ServerCommandSource> context, Number ...numbers) {
        String loottableName = StringArgumentType.getString(context, "loottable_name");
        final ServerWorld world = context.getSource().getWorld();
        final ServerCommandSource source = context.getSource();

        if (fileManager.getLootTableNames().contains(loottableName)) {
            context.getSource().sendMessage(Text.literal("§cНевозможно сохранить таблицу '%s', поскольку такая таблица уже существует!".formatted(loottableName)));
            return -1;
        }

        LootTable lootTable = new LootTable(loottableName);

        if (numbers.length == 1) {
            int[] cords = {
                    IntegerArgumentType.getInteger(context, "%s_x".formatted(numbers[0].getNumber())),
                    IntegerArgumentType.getInteger(context, "%s_y".formatted(numbers[0].getNumber())),
                    IntegerArgumentType.getInteger(context, "%s_z".formatted(numbers[0].getNumber()))
            };

            if (!world.getBlockState(new BlockPos(cords[0], cords[1], cords[2])).getBlock().equals(Blocks.CHEST)) {
                context.getSource().sendMessage(Text.literal("§cНевозможно сохранить таблицу '%s', поскольку переданные координаты не содержат сундук! ".formatted(loottableName)));
                return -1;
            }

            lootTable.chests.add(cords);

            context.getSource().sendMessage(Text.literal("§aТаблица '%s' успешно сохранена с 1 сундуком!".formatted(lootTable.name)));
        }
        else {
            int[] first = {
                    IntegerArgumentType.getInteger(context, "%s_x".formatted(numbers[0].getNumber())),
                    IntegerArgumentType.getInteger(context, "%s_y".formatted(numbers[0].getNumber())),
                    IntegerArgumentType.getInteger(context, "%s_z".formatted(numbers[0].getNumber()))
            };
            int[] second = {
                    IntegerArgumentType.getInteger(context, "%s_x".formatted(numbers[1].getNumber())),
                    IntegerArgumentType.getInteger(context, "%s_y".formatted(numbers[1].getNumber())),
                    IntegerArgumentType.getInteger(context, "%s_z".formatted(numbers[1].getNumber()))
            };

            BlockPos min = new BlockPos(
                    Math.min(first[0], second[0]),
                    Math.min(first[1], second[1]),
                    Math.min(first[2], second[2])
            );
            BlockPos max = new BlockPos(
                    Math.max(first[0], second[0]),
                    Math.max(first[1], second[1]),
                    Math.max(first[2], second[2])
            );

            int count = 0;
            int[][] coordinates = fileManager.getAllCords();
            ArrayList<int[]> blocks = new ArrayList<>();

            for (BlockPos pos : BlockPos.iterate(min, max)) {
                if (world.getBlockState(pos).getBlock().equals(Blocks.CHEST)) {
                    boolean is = false;

                    for (int[] value : coordinates) {
                        if (pos.getX() == value[0] && pos.getY() == value[1] && pos.getZ() == value[2]) {
                            is = true;
                            break;
                        }
                    }

                    if (!is) {
                        blocks.add(new int[] {pos.getX(), pos.getY(), pos.getZ()});
                        source.sendMessage(Text.literal("§aКоординаты %s сохранены.".formatted(String.valueOf(pos.getX()) + pos.getY() + pos.getZ())));
                        count++;
                    }
                }
            }

            HashSet<int[]> set = new HashSet<>(lootTable.chests);
            set.addAll(blocks);
            lootTable.chests = new ArrayList<>(set);

            if (count > 0) source.sendMessage(Text.literal("§aУспешно сохранены сундуки: %s.".formatted(count)));
            else source.sendMessage(Text.literal("§cНе было сохранено ни одного сундука!"));

        }
        fileManager.saveLT(lootTable);

        return 0;
    }

}