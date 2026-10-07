package com.gardensmc.gardensenchants.enchantment.custom;

import com.gardensmc.gardensenchants.enchantment.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused") // all custom enchants should be made available here when registered
public class CustomEnchantments {

    public static final Map<String, CustomEnchantment> customEnchantments = new HashMap<>();

    public static final CustomEnchantment SKULL_STEALER = register("skull_stealer", builder -> builder
            .rarity(EnchantRarity.UNCOMMON)
            .lore("<gray>Chance of dropping player's skull on death")
            .enchantRestrict(EnchantRestricts.WEAPON));
    public static final CustomEnchantment TIMBER = register( "timber", builder -> builder
            .lore("<gray>Chop down logs or leaves in one swing")
            .rarity(EnchantRarity.RARE)
            .enchantRestrict(EnchantRestricts.AXE));
    public static final CustomEnchantment VEIN_MINER = register("vein_miner", builder -> builder
            .lore("<gray>Break entire ore veins")
            .rarity(EnchantRarity.RARE)
            .enchantRestrict(EnchantRestricts.PICKAXE));
    public static final CustomEnchantment BUBBLE_HEAD = register("bubble_head", builder -> builder
            .lore("<gray>Breathe underwater")
            .rarity(EnchantRarity.EPIC)
            .enchantRestrict(EnchantRestricts.HELMET));
    public static final CustomEnchantment LIFE_STEAL = register("life_steal", builder -> builder
            .enchantRestrict(EnchantRestricts.WEAPON)
            .rarity(EnchantRarity.RARE)
            .lore("<gray>Steals some of target's health on hit")
            .maxLevel(3));
    public static final CustomEnchantment DOUBLE_STRIKE = register("double_strike", builder -> builder
            .lore("<gray>Chance of striking target twice")
            .enchantRestrict(EnchantRestricts.WEAPON)
            .rarity(EnchantRarity.EPIC)
            .maxLevel(3));
    public static final CustomEnchantment NIGHT_VISION = register("night_vision", builder -> builder
            .lore("<gray>See in the dark")
            .enchantRestrict(EnchantRestricts.HELMET)
            .rarity(EnchantRarity.RARE));
    public static final CustomEnchantment JELLY_LEGS = register("jelly_legs", builder -> builder
            .lore("<gray>Chance of taking no fall damage")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.RARE)
            .maxLevel(5));
    // EnderWardrobe balloons (worn in its balloon slot; see events/BalloonEnchants)
    public static final CustomEnchantment FEATHERWEIGHT = register("featherweight", builder -> builder
            .lore("<gray>Fall slower and jump higher", "<gray>while you wear it")
            .enchantRestrict(EnchantRestricts.BALLOON)
            .rarity(EnchantRarity.EPIC)
            .maxLevel(3));
    public static final CustomEnchantment TETHERED = register("tethered", builder -> builder
            .lore("<gray>Catches a fall that would kill you,", "<gray>then needs time to recover")
            .enchantRestrict(EnchantRestricts.BALLOON)
            .rarity(EnchantRarity.LEGENDARY)
            .maxLevel(3));
    public static final CustomEnchantment UPDRAFT = register("updraft", builder -> builder
            .lore("<gray>Press jump in the air", "<gray>for one more jump")
            .enchantRestrict(EnchantRestricts.BALLOON)
            .rarity(EnchantRarity.EPIC));
    public static final CustomEnchantment DRIFT = register("drift", builder -> builder
            .lore("<gray>Hold jump while falling", "<gray>to drift gently down")
            .enchantRestrict(EnchantRestricts.BALLOON)
            .conflictsWith(UPDRAFT)
            .rarity(EnchantRarity.EPIC));
    public static final CustomEnchantment LIFT = register("lift", builder -> builder
            .lore("<gray>Hold sneak in the air", "<gray>to rise for a moment")
            .enchantRestrict(EnchantRestricts.BALLOON)
            .rarity(EnchantRarity.EPIC));
    public static final CustomEnchantment BUOYANT = register("buoyant", builder -> builder
            .lore("<gray>Float up in water;", "<gray>sneak to dive")
            .enchantRestrict(EnchantRestricts.BALLOON)
            .rarity(EnchantRarity.UNCOMMON));
    public static final CustomEnchantment WEATHERVANE = register("weathervane", builder -> builder
            .lore("<gray>The wind carries you along", "<gray>in rain and storms")
            .enchantRestrict(EnchantRestricts.BALLOON)
            .rarity(EnchantRarity.UNCOMMON)
            .maxLevel(2));
    // EnderWardrobe backpacks (the one worn in its backpack slot; see events/BackpackEnchants)
    public static final CustomEnchantment RESTOCK = register("restock", builder -> builder
            .lore("<gray>Tops your hotbar back up", "<gray>from your backpack")
            .enchantRestrict(EnchantRestricts.BACKPACK)
            .rarity(EnchantRarity.RARE));
    public static final CustomEnchantment QUICK_DEPOSIT = register("quick_deposit", builder -> builder
            .lore("<gray>Sneak and use a chest to put in", "<gray>what it already holds")
            .displayName("Quick Deposit")
            .enchantRestrict(EnchantRestricts.BACKPACK)
            .rarity(EnchantRarity.UNCOMMON));
    public static final CustomEnchantment COMPACTOR = register("compactor", builder -> builder
            .lore("<gray>Packs nines into blocks", "<gray>inside your backpack")
            .enchantRestrict(EnchantRestricts.BACKPACK)
            .rarity(EnchantRarity.EPIC));
    public static final CustomEnchantment SNACK_POUCH = register("snack_pouch", builder -> builder
            .lore("<gray>Eats from your backpack", "<gray>when you get hungry")
            .displayName("Snack Pouch")
            .enchantRestrict(EnchantRestricts.BACKPACK)
            .rarity(EnchantRarity.RARE));
    // EnderWardrobe walking sticks (held, either hand; see events/WalkingStickEnchants)
    public static final CustomEnchantment STEADY = register("steady", builder -> builder
            .lore("<gray>Resist knockback while you hold it")
            .enchantRestrict(EnchantRestricts.WALKING_STICK)
            .rarity(EnchantRarity.RARE)
            .maxLevel(3));
    public static final CustomEnchantment SHEPHERD = register("shepherd", builder -> builder
            .lore("<gray>Farm animals follow you", "<gray>while you hold it")
            .enchantRestrict(EnchantRestricts.WALKING_STICK)
            .rarity(EnchantRarity.UNCOMMON));
    public static final CustomEnchantment TREKKER = register("trekker", builder -> builder
            .lore("<gray>Walking, sprinting and jumping", "<gray>make you less hungry while you hold it")
            .enchantRestrict(EnchantRestricts.WALKING_STICK)
            .rarity(EnchantRarity.RARE)
            .maxLevel(3));
    public static final CustomEnchantment MOUNTAINEER = register("mountaineer", builder -> builder
            .lore("<gray>Step up two blocks without jumping", "<gray>while you hold it")
            .enchantRestrict(EnchantRestricts.WALKING_STICK)
            .rarity(EnchantRarity.EPIC));
    public static final CustomEnchantment SUPER_BREAKER = register("super_breaker", builder -> builder
            .lore("<gray>Breaks blocks in a 3x3 pattern")
            .rarity(EnchantRarity.LEGENDARY)
            .enchantRestrict(EnchantRestricts.EXCAVATION));
    public static final CustomEnchantment SMELTER = register("smelter", builder -> builder
            .lore("<gray>Auto smelts ores")
            .rarity(EnchantRarity.RARE)
            .enchantRestrict(EnchantRestricts.PICKAXE));
    public static final CustomEnchantment ETERNAL = register( "eternal", builder -> builder
            .lore("<gray>Prevents your item from taking durability damage")
            .rarity(EnchantRarity.LEGENDARY));
    public static final CustomEnchantment NETHERLING = register("netherling", builder -> builder
            .lore("<gray>Deal extra damage to nether mobs")
            .rarity(EnchantRarity.EPIC)
            .enchantRestrict(EnchantRestricts.SWORD));
    public static final CustomEnchantment SILK_TOUCH_SPAWNER = register("silk_touch_spawner", builder -> builder
            .lore("<gray>Spawners will drop themselves when mined")
            .displayName("Silk Spawner")
            .enchantRestrict(EnchantRestricts.TOOLS)
            .rarity(EnchantRarity.LEGENDARY));
    public static final CustomEnchantment SPEEDY = register("speedy", builder -> builder
            .lore("<gray>Grants you a permanent speed boost")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.LEGENDARY)
            .maxLevel(3));
    public static final CustomEnchantment EXPLOSIVE = register("explosive", builder -> builder
            .lore("<gray>Do explosion damage on hit")
            .enchantRestrict(EnchantRestricts.BOW)
            .rarity(EnchantRarity.RARE)
            .maxLevel(4));
    public static final CustomEnchantment BARRAGE = register("barrage", builder -> builder
            .lore("<gray>Shoot multiple arrows at the same time")
            .enchantRestrict(EnchantRestricts.BOW)
            .conflictsWith(EXPLOSIVE)
            .rarity(EnchantRarity.RARE)
            .maxLevel(3)
    );
    public static final CustomEnchantment SPRINTER = register("sprinter", builder -> builder
            .lore("<gray>Consume less food when running")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.UNCOMMON)
            .maxLevel(3)
    );
    public static final CustomEnchantment CLOAK = register("cloak", builder -> builder
            .lore("<gray>Become invisible to sight-based mobs", "<gray>when not in combat")
            .enchantRestrict(EnchantRestricts.ARMOR)
            .rarity(EnchantRarity.EPIC)
    );
    public static final CustomEnchantment FLUTTER = register("flutter", builder -> builder
            .lore("<gray>Hold shift to activate slow falling")
            .enchantRestrict(EnchantRestricts.ELYTRA)
            .rarity(EnchantRarity.EPIC)
    );
    public static final CustomEnchantment PLATED = register("plated", builder -> builder
            .lore("<gray>Armors your elytra like a chestplate:", "<gray>leather, iron, diamond, netherite")
            .enchantRestrict(EnchantRestricts.ELYTRA)
            .rarity(EnchantRarity.EPIC)
            .maxLevel(4));
    public static final CustomEnchantment FLIPPERS = register("flippers", builder -> builder
            .lore("<gray>Gain Dolphin's Grace while swimming")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.EPIC)
    );
    public static final CustomEnchantment SIPHON = register("siphon", builder -> builder
            .lore("<gray>Suck drops and xp straight into your inventory")
            .enchantRestrict(EnchantRestricts.TOOLS)
            .rarity(EnchantRarity.UNCOMMON)
    );
    public static final CustomEnchantment ROCKET_BOOTS = register("rocket_boots", builder -> builder
            .lore("<gray>Hold shift to gain jump boost")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.LEGENDARY)
            .maxLevel(4)
    );
    public static final CustomEnchantment ESCAPE_ARTIST = register("escape_artist", builder -> builder
            .lore("<gray>Gain speed when health is 1 heart or lower")
            .enchantRestrict(EnchantRestricts.BOOTS)
            .rarity(EnchantRarity.UNCOMMON)
            .maxLevel(2)
    );
    public static final CustomEnchantment ORB_BOOST = register("orb_boost", builder -> builder
            .lore("<gray>Gain more exp when collecting orbs")
            .enchantRestrict(EnchantRestricts.CHESTPLATE)
            .rarity(EnchantRarity.EPIC)
            .maxLevel(3)
    );
    public static final CustomEnchantment MUNCHER = register("muncher", builder -> builder
            .lore("<gray>Replenish more hunger when eating")
            .enchantRestrict(EnchantRestricts.HELMET)
            .rarity(EnchantRarity.UNCOMMON)
            .maxLevel(3)
    );

    public static final CustomEnchantment REPLANTER = register("replanter", builder -> builder
            .lore("<gray>Replants fully grown crops you break")
            .enchantRestrict(EnchantRestricts.HOE)
            .rarity(EnchantRarity.UNCOMMON)
    );
    public static final CustomEnchantment HARVESTER = register("harvester", builder -> builder
            .lore("<gray>Harvests the fully grown crops around", "<gray>the one you break, planter boxes too")
            .enchantRestrict(EnchantRestricts.HOE)
            .rarity(EnchantRarity.RARE)
    );
    public static final CustomEnchantment TILLER = register("tiller", builder -> builder
            .lore("<gray>Tills a 3x3 area at once")
            .enchantRestrict(EnchantRestricts.HOE)
            .rarity(EnchantRarity.UNCOMMON)
    );

    public static final CustomEnchantment GREEN_THUMB = register("green_thumb", builder -> builder
            .lore("<gray>Better odds of 2 and 3 star crops", "<gray>when harvesting planter boxes")
            .enchantRestrict(EnchantRestricts.HOE)
            .rarity(EnchantRarity.RARE)
            .maxLevel(3)
    );
    public static final CustomEnchantment BOUNTIFUL = register("bountiful", builder -> builder
            .lore("<gray>Chance of an extra crop", "<gray>when harvesting planter boxes")
            .enchantRestrict(EnchantRestricts.HOE)
            .rarity(EnchantRarity.RARE)
            .maxLevel(2)
    );
    public static final CustomEnchantment WIDE_SPRAY = register("wide_spray", builder -> builder
            .lore("<gray>Also waters the dry planter boxes", "<gray>around the one you water")
            .enchantRestrict(EnchantRestricts.WATERING_CAN)
            .rarity(EnchantRarity.RARE)
            .maxLevel(2)
    );
    public static final CustomEnchantment RESERVOIR = register("reservoir", builder -> builder
            .lore("<gray>Holds an extra charge of water per level")
            .enchantRestrict(EnchantRestricts.WATERING_CAN)
            .rarity(EnchantRarity.UNCOMMON)
            .maxLevel(5)
    );
    public static final CustomEnchantment DEEP_SOAK = register("deep_soak", builder -> builder
            .lore("<gray>Planter boxes you water stay wet", "<gray>for two growth days")
            .enchantRestrict(EnchantRestricts.WATERING_CAN)
            .rarity(EnchantRarity.EPIC)
    );

    private static CustomEnchantment register(String id, Consumer<CustomEnchantment.Builder> builder) {
        var enchantmentBuilder = new CustomEnchantment.Builder(id);
        builder.accept(enchantmentBuilder);

        var customEnchantment = enchantmentBuilder.build();
        customEnchantments.put(id, customEnchantment);
        return customEnchantment;
    }

}
