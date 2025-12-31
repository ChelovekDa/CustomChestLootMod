package ru.hcc.customchestloot.util;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChestItem {

    public String id;
    public float chance;
    public byte count;

    @Override
    public String toString() {
        return "%s=%s=%s".formatted(id, String.valueOf(chance), String.valueOf(count));
    }

    private ChestItem(String id, float chance, byte count) {
        this.id = id;
        this.chance = chance;
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChestItem item = (ChestItem) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chance, count);
    }

    public static boolean isValid(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        return id != null && Registries.ITEM.containsId(id);
    }

    @Nullable
    public static ChestItem of(String[] mas) {
        if (isValid(mas[0])) return new ChestItem(mas[0], Float.parseFloat(mas[1]), Byte.parseByte(mas[2]));
        else return null;
    }

    @Nullable
    public static ChestItem of(String str) {
        return of(str
                .replaceAll("\"", "")
                .replaceAll("'", "")
                .replace("}", "")
                .replace("{", "")
                .replace("[", "")
                .replace("]", "")
                .split("="));
    }
}
