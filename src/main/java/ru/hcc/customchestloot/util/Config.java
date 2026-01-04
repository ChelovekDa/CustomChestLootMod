package ru.hcc.customchestloot.util;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;


public class Config extends FileManager {

    public HashMap<String, Object> cfg = new HashMap<>();

    public Config() {
        this.cfg.put("update-chests-interval", 12000);
        this.cfg.put("saving-time-interval", 400);
        this.cfg.put("change-chest-names", true);

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
        save();
    }

}
