package ru.hcc.customchestloot.util;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;


public class Config extends FileManager {

    private final HashMap<String, Object> BASE_CONFIG_DATA = new HashMap<>();
    public HashMap<String, Object> cfg = new HashMap<>();

    public Config() {
        BASE_CONFIG_DATA.put("update-chests-interval", 12000);
        BASE_CONFIG_DATA.put("saving-time-interval", 400);

        this.setCfg();
    }

    public void save() {
        try {
            Path path = getModsConfigDirectory().resolve(FileName.CONFIG.getFileName());
            if (!Files.exists(path)) new File(path.toString()).createNewFile();

            FileWriter writer = new FileWriter(path.toString());
            writer.write(prettyPrinting(new JSONObject(this.cfg)));
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setCfg() {
        JSONObject jsonObject = readFile(FileName.CONFIG);

        for (Object key : jsonObject.keySet()) this.cfg.put(String.valueOf(key), jsonObject.get(key));

        if (this.cfg.isEmpty()) {
            this.cfg.putAll(BASE_CONFIG_DATA);
            save();
        }
    }

}
