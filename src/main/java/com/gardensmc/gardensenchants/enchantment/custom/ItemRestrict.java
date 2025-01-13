package com.gardensmc.gardensenchants.enchantment.custom;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum ItemRestrict {
    AXE(Suffixes.AXE),
    PICKAXE(Suffixes.PICK_AXE),
    TOOL(Suffixes.TOOLS),
    ARMOR(Suffixes.ARMOR),
    WEAPON(Suffixes.WEAPONS),
    SWORD(Suffixes.SWORD),
    HELMET(Suffixes.HELMET),
    BOOTS(Suffixes.BOOTS),
    WAND(itemStack -> itemStack.getType() == Material.STICK),
    ALL(Suffixes.all());

    private final Predicate<ItemStack> isPermitted;

    ItemRestrict(String... materialSuffix) {
        this.isPermitted = itemStack -> Arrays.stream(materialSuffix)
                .anyMatch(suffix -> itemStack
                        .getType()
                        .toString()
                        .toLowerCase().endsWith(suffix.toLowerCase()));
    }

    ItemRestrict(Predicate<ItemStack> isPermitted) {
        this.isPermitted = isPermitted;
    }

    public boolean isEnchantable(ItemStack itemStack) {
        return isPermitted.test(itemStack);
    }
    
    static class Suffixes {
        public static final String SWORD = "sword";
        public static final String SHOVEL = "shovel";
        public static final String AXE = "axe";
        public static final String PICK_AXE = "pickaxe";
        public static final String HELMET = "helmet";
        public static final String BOOTS = "boots";
        public static final String CHEST_PLATE = "chestplate";
        public static final String LEGGINGS = "leggings";

        public static final String[] TOOLS = new String[] {
                SWORD, SHOVEL, AXE, PICK_AXE
        };
        public static final String[] ARMOR = new String[] {
                HELMET, CHEST_PLATE, LEGGINGS, BOOTS
        };
        public static final String[] WEAPONS = new String[] {
                SWORD, AXE
        };
        
        public static String[] all() {
            return Stream.concat(
                    Arrays.stream(TOOLS),
                    Arrays.stream(ARMOR)
            ).toArray(String[]::new);
        }

    }
}
