package ru.hcc.customchestloot.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.hcc.customchestloot.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Timer extends FileManager {

    private final Config cfg = new Config();
    private final FileManager fileManager = new FileManager();
    private static final Random RANDOM = new Random();

    private int timer;
    private final int interval;
    private final int save_interval;

    public Timer() {
        this.interval = Integer.parseInt(String.valueOf(cfg.cfg.get("update-chests-interval")));
        this.save_interval = Integer.parseInt(String.valueOf(cfg.cfg.get("saving-time-interval")));
    }

    public void start() {
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

    private static boolean isCreating(float chance) {
        return Integer.parseInt(String.valueOf(chance * 100).replace(".", "")) >= RANDOM.nextInt(100);
    }

    private void restoreChests(World world) {
        HashMap<String, Region> regionHashMap = fileManager.getAllRegions();

        for (String name : regionHashMap.keySet()) {
            Region region = regionHashMap.get(name);
            chestUpdate(region, world);
        }
    }

    public void chestUpdate(Region region, World world) {
        HashMap<String, Region> regionHashMap = fileManager.getAllRegions();

        BlockPos pos = new BlockPos(region.x, region.y, region.z);

        if (!world.getBlockState(pos).getBlock().equals(Blocks.CHEST)) {
            world.setBlockState(pos, Blocks.CHEST.getDefaultState());
        }

        ChestBlockEntity blockEntity = (ChestBlockEntity) world.getBlockEntity(pos);
        assert blockEntity != null;

        ArrayList<Byte> slots = new ArrayList<>();
        for (byte i = 0; i < blockEntity.size(); i++) slots.add(i);

        for (int i = 0; i < blockEntity.size(); i++) blockEntity.removeStack(i);

        Region object;

        if (region.parent == null) object = region;
        else {
            object = regionHashMap.get(region.parent);

            if (object == null) {
                object = region;
                region.parent = null;
                fileManager.saveRegion(region);
            }
        }

        for (ChestItem chestItem : object.items) {

            if (isCreating(chestItem.chance)) {
                Item item = Registries.ITEM.getOrEmpty(Identifier.tryParse(chestItem.id)).orElse(null);

                if (item == null) {
                    Main.LOGGER.warn("Can't get the item '%s' for %s. Maybe it doesn't exist.".formatted(chestItem.id, region.name));
                    continue;
                }

                int slot = RANDOM.nextInt(slots.size());

                int count = chestItem.count;

                if (count > 1 && item.getDefaultStack().getMaxCount() > 1) count = RANDOM.nextInt(1, chestItem.count);
                else count = 1;

                try {
                    blockEntity.setStack(slots.get(slot), new ItemStack(item, count));
                } catch (IndexOutOfBoundsException e) {
                    Main.LOGGER.warn("It is not possible to add the %s item to the %s chest because all slots are occupied!".formatted(chestItem.id, region.name));
                    break;
                }
                blockEntity.markDirty();
                slots.remove(slot);
            }
        }

        Main.LOGGER.info("The '%s' chest on %s has been successfully updated!".formatted(region.name, region.toString()));
    }

}
