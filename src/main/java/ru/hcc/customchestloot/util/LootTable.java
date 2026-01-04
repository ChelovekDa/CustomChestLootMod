package ru.hcc.customchestloot.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.Objects;


public class LootTable {

    public ArrayList<int[]> chests;
    public ArrayList<ChestItem> items;
    public String name;

    public LootTable(ArrayList<int[]> chests, ArrayList<ChestItem> items, String name) {
        this.chests = chests;

        if (items == null || items.isEmpty()) {
            items = new ArrayList<>();
            items.add(ChestItem.of("minecraft:stone=0.9=63"));
            items.add(ChestItem.of("minecraft:diamond=0.9=1"));
        }

        this.items = items;

        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootTable lootTable = (LootTable) o;
        return Objects.equals(name, lootTable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public LootTable(String name) {
        this.name = name;
        this.chests = new ArrayList<>();

        this.items = new ArrayList<>();
        items.add(ChestItem.of("minecraft:stone=0.9=63"));
        items.add(ChestItem.of("minecraft:diamond=0.9=1"));
    }

    public JSONObject toJSON() {
        JSONObject value = new JSONObject();
        value.put("name", this.name);

        JSONArray array = new JSONArray();
        for (ChestItem item : this.items) array.add(item.toString());
        value.put("items", array);

        array = new JSONArray();
        for (int[] cords : this.chests) {
            JSONArray cordsArray = new JSONArray();
            for (int num : cords) cordsArray.add(num);
            array.add(cordsArray);
        }
        value.put("chests", array);

        return value;
    }
}
