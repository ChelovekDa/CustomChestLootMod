package ru.hcc.customchestloot.util;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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

    public void saveRegion(Region region) {
        HashMap<String, Region> allRegions = getAllRegions();

        for (Region reg : getAllRegions().values()) {
            if (reg.equalsIgnoreName(region)) {
                allRegions.remove(reg.name);
                break;
            }
        }

        allRegions.put(region.name, region);

        try {
            Map<String, Map<String, Object>> map = new HashMap<>();
            for (Region reg : allRegions.values()) map.put(reg.name, reg.toMap());

            FileWriter writer = new FileWriter(getModsConfigDirectory().resolve(FileName.REGIONS_DATA.getFileName()).toFile(), false);
            writer.write(prettyPrinting(new JSONObject(map)));
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteRegion(Region value) {
        HashMap<String, Region> allRegions = getAllRegions();
        allRegions.remove(value.name);

        try {
            Map<String, Map<String, Object>> map = new HashMap<>();
            for (Region reg : allRegions.values()) map.put(reg.name, reg.toMap());

            FileWriter writer = new FileWriter(getModsConfigDirectory().resolve(FileName.REGIONS_DATA.getFileName()).toFile(), false);
            writer.write(prettyPrinting(new JSONObject(map)));
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public HashMap<String, Region> getAllRegions() {
        HashMap<String, Region> result = new HashMap<>();
        JSONObject jsonObject = readFile(FileName.REGIONS_DATA);

        for (Object nameKey : jsonObject.keySet()) {
            JSONObject node = (JSONObject) jsonObject.get(nameKey);
            Region region = new Region(0, 0, 0);

            region.x = ((Number) node.get("x")).intValue();
            region.y = ((Number) node.get("y")).intValue();
            region.z = ((Number) node.get("z")).intValue();
            region.name = String.valueOf(nameKey);

            if (node.get("parent") == null) region.parent = null;
            else region.parent = String.valueOf(node.get("parent"));

            ArrayList<ChestItem> items = new ArrayList<>();
            JSONArray array = (JSONArray) node.get("items");

            if (!array.isEmpty()) {
                for (Object item : array) items.add(ChestItem.of(String.valueOf(item)));
                region.items = items;
            }

            result.put(region.name, region);

        }

        return result;
    }

}
