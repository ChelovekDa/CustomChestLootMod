package ru.hcc.customchestloot.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import ru.hcc.customchestloot.Main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class ChestManager extends FileManager {

    private static class NullChestItem extends ChestItem {

        protected NullChestItem(float chance) {
            super("minecraft:air", chance, (byte) 1);
        }

    }

    private static class ItemGenerator {

        @NotNull
        private static ArrayList<ChestItem> generate(ArrayList<ChestItem> items, int slotsSize) {
            return setItemsCount(getCreateList(items, slotsSize), slotsSize);
        }

        @NotNull
        private static ArrayList<ChestItem> setItemsCount(ArrayList<ChestItem> items, int slotsSize) {
            ArrayList<ChestItem> result = new ArrayList<>();

            byte amount = (byte) (((byte) slotsSize) - ((byte) items.size()));

            if (amount <= 0 || items.isEmpty()) return items;

            items.sort(Comparator.comparingInt(item -> item.count));
            Collections.reverse(items);

            float totalWeight = 0.0f;
            for (ChestItem item : items) totalWeight += item.chance;

            byte chance;
            if (items.size() > 20 && totalWeight >= 1) chance = 5;
            else chance = 6;

            for (ChestItem chestItem : items) {
                if (amount >= 1) {
                    if (chestItem.count <= 2) result.add(chestItem);
                    else {
                        if (RANDOM.nextInt(10) <= chance) {
                            byte count;

                            if (chestItem.count % 3 == 0 && amount >= 2) {
                                count = (byte) (chestItem.count / 3);
                                for (int i = 0; i < 3; i++) result.add(new ChestItem(chestItem, count));
                                amount -= 2;
                                continue;
                            }

                            else if (chestItem.count == 4) {
                                result.add(new ChestItem(chestItem, (byte) 3));
                                result.add(new ChestItem(chestItem, (byte) 1));
                                amount--;
                                continue;
                            }

                            byte bound = (byte) (((byte) Math.round((float) chestItem.count / 3) - 1) * 2);

                            count = (byte) RANDOM.nextInt(1, bound);
                            result.add(new ChestItem(chestItem, (byte) (chestItem.count - count)));
                            result.add(new ChestItem(chestItem, count));
                            amount--;
                        }
                        else result.add(chestItem);
                    }
                }
                else result.add(chestItem);
            }

            int removingSize = result.size() - slotsSize;
            if (removingSize >= 5) {
                while (removingSize >= 5) {
                    result = setItemsCount(result, slotsSize);
                    removingSize = result.size() - slotsSize;
                }
            }

            Collections.shuffle(result);

            return result;
        }

        @NotNull
        private static ArrayList<ChestItem> getCreateList(ArrayList<ChestItem> items, int slotsSize) {
            ArrayList<ChestItem> results = new ArrayList<>();

            float totalWeight = 0.0f;
            for (ChestItem item : items) totalWeight += item.chance;

            ChestItem nullItem = new NullChestItem(1 - totalWeight);

            if (nullItem.chance <= 0) nullItem.chance = 0.99f;
            items.add(nullItem);

            int limit = items.size();
            if (limit >= 35 && totalWeight >= 1) limit = 18;

            for (int i = 0; i < limit; i++) {
                totalWeight = 0.0f;
                for (ChestItem item : items) totalWeight += item.chance;

                float randomValue = RANDOM.nextFloat() * totalWeight;

                float cumulativeWeight = 0.0f;

                for (ChestItem entry : items) {
                    cumulativeWeight += entry.chance;
                    if (randomValue <= cumulativeWeight) {
                        if (!entry.id.equals("minecraft:air")) {
                            results.add(entry);
                            items.remove(entry);
                        }
                        break;
                    }
                }
            }

            int removingCount = results.size() - slotsSize;
            if (removingCount > 0) {
                while (removingCount > 0) {
                    results = getCreateList(results, slotsSize);
                    removingCount = results.size() - slotsSize;
                }
            }

            return results;
        }

    }

    private final Config cfg = new Config();
    private static final Random RANDOM = new Random();
    private final FileManager fileManager = new FileManager();

    private int timer;
    private final int interval;
    private final int save_interval;

    public ChestManager() {
        this.interval = Integer.parseInt(String.valueOf(cfg.cfg.get("update-chests-interval")));
        this.save_interval = Integer.parseInt(String.valueOf(cfg.cfg.get("saving-time-interval")));
    }

    public void startTimer() {
        setTimer();

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                timer++;

                if (timer % save_interval == 0) {
                    cfg.saveTimer(timer);
                }
                if (timer >= interval) {
                    timer = 0;
                    restoreChests(world.getServer().getWorld(World.OVERWORLD));
                }
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> cfg.saveTimer(timer));

        if ((boolean) cfg.cfg.get("change-chest-names")) {

            PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
                int[] cords = new int[] {pos.getX(), pos.getY(), pos.getZ()};

                if (fileManager.isCordsExisting(cords) && blockEntity != null) {
                    NbtCompound nbtCompound = blockEntity.createNbtWithId(world.getRegistryManager());
                    nbtCompound.putString("CustomName", Text.Serialization.toJsonString(Text.translatable("container.chest"), world.getRegistryManager()));

                    blockEntity.read(nbtCompound, world.getRegistryManager());
                    blockEntity.markDirty();
                }
                return true;
            });

            PlayerBlockBreakEvents.CANCELED.register((world, player, pos, state, blockEntity) -> {
                int[] cords = new int[] {pos.getX(), pos.getY(), pos.getZ()};

                if (fileManager.isCordsExisting(cords) && blockEntity != null) {
                    NbtCompound nbtCompound = blockEntity.createNbt(world.getRegistryManager());
                    nbtCompound.putString("CustomName", "Loot");
                    blockEntity.read(nbtCompound, world.getRegistryManager());
                    blockEntity.markDirty();
                }
            });
        }

    }

    private void setTimer() {
        try {
            Path path = getModsConfigDirectory().resolve(FileName.TIMER_DATA.getFileName());

            if (!Files.exists(path)) {
                new File(path.toString()).createNewFile();
                this.timer = 0;
                return;
            }

            BufferedReader br = new BufferedReader(new FileReader(path.toString()));
            this.timer = Integer.parseInt(br.readLine().replace("\n", ""));
        } catch (IOException ex) {
            this.timer = 0;
            throw new RuntimeException();
        }
    }

    private void updateChest(int[] cords, World world, ArrayList<ChestItem> items) {
        BlockPos pos = new BlockPos(cords[0], cords[1], cords[2]);

        if (!world.getBlockState(pos).getBlock().equals(Blocks.CHEST)) {
            world.setBlockState(pos, Blocks.CHEST.getDefaultState());
        }

        ChestBlockEntity blockEntity = (ChestBlockEntity) world.getBlockEntity(pos);
        assert blockEntity != null;

        NbtCompound nbtCompound = blockEntity.createNbt(world.getRegistryManager());
        nbtCompound.putString("CustomName", "Loot");
        blockEntity.read(nbtCompound, world.getRegistryManager());

        blockEntity.clear();

        ArrayList<Byte> slots = new ArrayList<>();
        for (byte i = 0; i < blockEntity.size(); i++) slots.add(i);
        int size = slots.size();

        ArrayList<ChestItem> gotItems = ItemGenerator.generate((ArrayList<ChestItem>) items.clone(), size);

        for (ChestItem chestItem : gotItems) {
            Item item = Registries.ITEM.getOrEmpty(Identifier.tryParse(chestItem.id)).orElse(null);

            int slot = RANDOM.nextInt(slots.size());

            try {
                blockEntity.setStack(slots.get(slot), new ItemStack(Objects.requireNonNull(item), RANDOM.nextInt(1, (chestItem.count + 1))));
            } catch (IndexOutOfBoundsException e) {
                Main.LOGGER.warn("It is not possible to add the %s item to the chest because all slots are occupied!".formatted(chestItem.id));
                break;
            }
            slots.remove(slot);
        }
        blockEntity.markDirty();
    }

    private void restoreChests(World world) {
        int chestCount = 0;

        Main.LOGGER.info("Staring to update chests...");
        for (LootTable lootTable : getAllLootTables()) {
            for (int[] cords : lootTable.chests) {
                this.updateChest(cords, world, lootTable.items);
                chestCount++;
            }
        }
        Main.LOGGER.info("The %d chests has been successfully updated!".formatted(chestCount));
    }

    @NotNull
    public String updateChests(LootTable lootTable, World world) {
        if (interval - timer <= 100) return "";
        int chestCount = 0;

        Main.LOGGER.info("Staring update '%s' loottable...".formatted(lootTable.name));
        for (int[] cords : lootTable.chests) {
            this.updateChest(cords, world, lootTable.items);
            chestCount++;
        }
        Main.LOGGER.info("The %d chests has been successfully updated!".formatted(chestCount));
        return "§aБыло успешно обновлено сундуков: %d!".formatted(chestCount);
    }

    @NotNull
    public String updateChests(ArrayList<int[]> chestCords, World world) {
        if (interval - timer <= 100) return "";
        int chestCount = 0;

        ArrayList<LootTable> lootTables = getAllLootTables();

        Main.LOGGER.info("Staring to update chests by user action...");
        for (int[] cords : chestCords) {
            for (LootTable lootTable : lootTables) {
                if (fileManager.isCordsExisting(cords, lootTable.name)) {
                    this.updateChest(cords, world, lootTable.items);
                    chestCount++;
                }
            }
        }
        Main.LOGGER.info("The %d chests has been successfully updated!".formatted(chestCount));
        return "§aБыло успешно обновлено сундуков: %d!".formatted(chestCount);
    }

}