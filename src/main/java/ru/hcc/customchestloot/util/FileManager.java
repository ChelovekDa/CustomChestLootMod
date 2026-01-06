package ru.hcc.customchestloot.util;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import ru.hcc.customchestloot.Main;
import tools.jackson.databind.ObjectMapper;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Number;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class FileManager {

    protected static String prettyPrinting(JSONObject object) {
        ObjectMapper mapper = new ObjectMapper();
        Object json = mapper.readValue(object.toJSONString(), Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    protected static Path getModsConfigDirectory() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(Main.ID);

        if (!Files.exists(path)) path.toFile().mkdirs();

        return path;
    }

    @NotNull
    protected JSONObject readFile(@NotNull FileName fileName) {
        try {
            Path path = getModsConfigDirectory().resolve(fileName.getFileName());
            if (!Files.exists(path)) path.toFile().createNewFile();
            return (JSONObject) new JSONParser().parse(new FileReader(path.toFile()));
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public void saveTimer(int now) {
        try {
            FileWriter writer = new FileWriter(getModsConfigDirectory().resolve(FileName.TIMER_DATA.getFileName()).toString(), false);
            writer.write(String.valueOf(now));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveLT(LootTable table) {
        Map<String, JSONObject> map = new HashMap<>();
        try {
            for (LootTable lootTable : getAllLootTables()) {
                if (lootTable.equals(table)) map.put(table.name, table.toJSON());
                else map.put(lootTable.name, lootTable.toJSON());
            }

            if (map.isEmpty() || !map.containsKey(table.name)) map.put(table.name, table.toJSON());

            FileWriter writer = new FileWriter(getModsConfigDirectory().resolve(FileName.REGIONS_DATA.getFileName()).toFile(), false);
            writer.write(prettyPrinting(new JSONObject(map)));
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteLT(String name) {
        Map<String, JSONObject> map = new HashMap<>();
        try {
            for (LootTable lootTable : getAllLootTables()) {
                if (!lootTable.name.equals(name)) map.put(lootTable.name, lootTable.toJSON());
            }

            FileWriter writer = new FileWriter(getModsConfigDirectory().resolve(FileName.REGIONS_DATA.getFileName()).toFile(), false);
            writer.write(prettyPrinting(new JSONObject(map)));
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public ArrayList<String> getLootTableNames() {
        return new ArrayList<>(readFile(FileName.REGIONS_DATA).keySet());
    }

    public boolean isCordsExisting(int[] cords) {
        JSONObject jsonObject = readFile(FileName.REGIONS_DATA);

        for (Object key : jsonObject.keySet()) {
            JSONArray array = (JSONArray) ((JSONObject) jsonObject.get(key)).get("chests");

            for (Object cordsArrays : array) {
                JSONArray cordsArray = (JSONArray) cordsArrays;

                for (int i = 0; i < cordsArray.size(); i++) {
                    if (cords[i] == ((Number) cordsArray.get(i)).intValue()) return true;
                }
            }
        }

        return false;
    }

    public boolean isCordsExisting(int[] cords, String lootTableName) {
        JSONObject jsonObject = readFile(FileName.REGIONS_DATA);

        for (Object key : jsonObject.keySet()) {
            if (!String.valueOf(key).equals(lootTableName)) continue;

            JSONArray array = (JSONArray) ((JSONObject) jsonObject.get(key)).get("chests");

            for (Object cordsArrays : array) {
                JSONArray cordsArray = (JSONArray) cordsArrays;

                for (int i = 0; i < cordsArray.size(); i++) {
                    if (cords[i] == ((Number) cordsArray.get(i)).intValue()) return true;
                }
            }
        }

        return false;
    }

    public ArrayList<int[]> getAllCords() {
        ArrayList<int[]> arrayList = new ArrayList<>();
        JSONObject jsonObject = readFile(FileName.REGIONS_DATA);

        for (Object key : jsonObject.keySet()) {
            JSONArray array = (JSONArray) ((JSONObject) jsonObject.get(key)).get("chests");

            for (Object cordsArrays : array) {
                JSONArray cordsArray = (JSONArray) cordsArrays;
                int[] cords = new int[3];

                for (int i = 0; i < cordsArray.size(); i++) cords[i] = ((Number) cordsArray.get(i)).intValue();
                arrayList.add(cords);
            }
        }

        return arrayList;
    }

    @Nullable
    public LootTable getLootTable(String name) {
        JSONObject jsonObject = readFile(FileName.REGIONS_DATA);

        for (Object loottableKey : jsonObject.keySet()) {
            if (String.valueOf(loottableKey).equals(name)) {

                JSONObject node = (JSONObject) jsonObject.get(loottableKey);

                JSONArray array = (JSONArray) node.get("chests");
                ArrayList<int[]> chests = new ArrayList<>();

                for (Object cordsArrays : array) {
                    JSONArray cordsArray = (JSONArray) cordsArrays;
                    int[] cords = new int[3];

                    for (int i = 0; i < cordsArray.size(); i++) cords[i] = ((Number) cordsArray.get(i)).intValue();
                    chests.add(cords);
                }
                array.clear();

                array = (JSONArray) node.get("items");
                ArrayList<ChestItem> items = new ArrayList<>();

                ChestItem object;
                for (Object item : array) {
                    object = ChestItem.of(String.valueOf(item));

                    if (object == null) Main.LOGGER.warn("Can't add '%s' to loot table because it returned a null source!".formatted(String.valueOf(item)));
                    else items.add(object);
                }

                return new LootTable(chests, items, String.valueOf(loottableKey));
            }

        }

        return null;
    }

    @NotNull
    public ArrayList<LootTable> getAllLootTables() {
        ArrayList<LootTable> result = new ArrayList<>();
        JSONObject jsonObject = readFile(FileName.REGIONS_DATA);

        for (Object loottableKey : jsonObject.keySet()) {
            JSONObject node = (JSONObject) jsonObject.get(loottableKey);

            JSONArray array = (JSONArray) node.get("chests");
            ArrayList<int[]> chests = new ArrayList<>();

            for (Object cordsArrays : array) {
                JSONArray cordsArray = (JSONArray) cordsArrays;
                int[] cords = new int[3];

                for (int i = 0; i < cordsArray.size(); i++) cords[i] = ((Number) cordsArray.get(i)).intValue();
                chests.add(cords);
            }

            array = (JSONArray) node.get("items");
            ArrayList<ChestItem> items = new ArrayList<>();

            ChestItem object;
            for (Object item : array) {
                object = ChestItem.of(String.valueOf(item));

                if (object == null) Main.LOGGER.warn("Can't add '%s' to loot table because it returned a null source!".formatted(String.valueOf(item)));
                else items.add(object);
            }

            result.add(new LootTable(chests, items, String.valueOf(loottableKey)));
        }

        return result;
    }

}
