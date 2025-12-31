package ru.hcc.customchestloot.util;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import java.util.*;


public class Region {

    public int x;
    public int y;
    public int z;

    public ArrayList<ChestItem> items;
    public String name;

    @Nullable
    public String parent;

    public Region(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.items = new ArrayList<>();
        this.name = String.valueOf(this.x) + this.y + this.z;
        this.parent = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region = (Region) o;
        return x == region.x && y == region.y && z == region.z && Objects.equals(name, region.name);
    }

    public boolean equalsIgnoreName(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region = (Region) o;
        return x == region.x && y == region.y && z == region.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, name, parent);
    }

    public static Region fromCommandContext(CommandContext<ServerCommandSource> context, Number number) {
        return new Region(
                IntegerArgumentType.getInteger(context, "%s_x".formatted(number.getNumber())),
                IntegerArgumentType.getInteger(context, "%s_y".formatted(number.getNumber())),
                IntegerArgumentType.getInteger(context, "%s_z".formatted(number.getNumber()))
        );
    }

    @Override
    public String toString() {
        return "%s %s %s".formatted(this.x, this.y, this.z);
    }

    public int[] getCords() {
        return new int[] {this.x, this.y, this.z};
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("x", this.x);
        map.put("y", this.y);
        map.put("z", this.z);

        ArrayList<String> arrayList = new ArrayList<>();
        for (ChestItem item : this.items) arrayList.add(item.toString());

        map.put("items", arrayList);
        map.put("name", this.name);
        map.put("parent", this.parent);

        return map;
    }

}
