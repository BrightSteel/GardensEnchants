package com.gardensmc.gardensenchants.enchantment.custom;

import com.gardensmc.gardensfurniture.GardensFurniture;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gardensmc.gardensenchants.enchantment.custom.EnchantRestricts.registeredEnchantRestricts;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EnchantRestrict {

    @Getter
    private String id;

    private List<Material> permittedMaterials;
    private List<String> permittedCustomIdentifiers;
    private List<String> permittedCustomIdSuffixes;
    private List<Character> icons;

    private List<EnchantRestrict> children;

    // EnderWardrobe's own items (items.yml there), by id: their enderwardrobe:offhand tag (staffs) or
    // enderwardrobe:item tag (backpacks, balloons)
    private List<String> permittedWardrobeItems;

    // backpacks with storage, as EnderWardrobe tells them apart
    private boolean permitsBackpacks;

    private static final NamespacedKey WARDROBE_OFFHAND = new NamespacedKey("enderwardrobe", "offhand");
    private static final NamespacedKey WARDROBE_ITEM = new NamespacedKey("enderwardrobe", "item");
    /** In withWardrobeItems: every EnderWardrobe offhand item. */
    public static final String ANY_OFFHAND = "offhand:*";

    public EnchantRestrict(String id) {
        this.id = id;
    }

    /**
     * A leaf restrict is a concrete item type (e.g. sword, pickaxe) that permits items
     * directly rather than delegating to child restricts.
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Whether this restrict permits the item directly (by material or custom identifier),
     * without recursing into child restricts. Used to resolve the specific type an item
     * belongs to - i.e. the same type its matching typed book would use.
     */
    public boolean isDirectlyPermitted(ItemStack itemStack) {
        if (permittedMaterials.contains(itemStack.getType()) || isPermittedWardrobeItem(itemStack)) {
            return true;
        }
        if (!permittedCustomIdentifiers.isEmpty() || !permittedCustomIdSuffixes.isEmpty()) {
            var customItem = GardensFurniture.itemRegistry.getCustomItem(itemStack);
            if (customItem != null) {
                var identifier = customItem.getIdentifier();
                return permittedCustomIdentifiers.contains(identifier) || permittedCustomIdSuffixes.contains(identifier);
            }
        }
        return false;
    }

    public boolean isEnchantable(ItemStack itemStack) {
        if (permittedMaterials.contains(itemStack.getType()) || isPermittedWardrobeItem(itemStack)) {
            return true;
        }
        if (!permittedCustomIdentifiers.isEmpty() || !permittedCustomIdSuffixes.isEmpty()) {
            var customItem = GardensFurniture.itemRegistry.getCustomItem(itemStack);
            if (customItem != null) {
                var identifier = customItem.getIdentifier();
                if (permittedCustomIdentifiers.contains(identifier) || permittedCustomIdSuffixes.contains(identifier)) {
                    return true;
                }
            }
        }
        return children.stream().anyMatch(enchantRestrict -> enchantRestrict.isEnchantable(itemStack));
    }

    private boolean isPermittedWardrobeItem(ItemStack itemStack) {
        if (permitsBackpacks && isBackpack(itemStack)) {
            return true;
        }
        if (permittedWardrobeItems.isEmpty() || !itemStack.hasItemMeta()) {
            return false;
        }
        var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        var offhand = pdc.get(WARDROBE_OFFHAND, PersistentDataType.STRING);
        // "offhand:*": any EnderWardrobe offhand item, its own or one another plugin added (a staff)
        if (offhand != null && permittedWardrobeItems.contains(ANY_OFFHAND)) return true;
        var id = offhand != null ? offhand : pdc.get(WARDROBE_ITEM, PersistentDataType.STRING);
        return id != null && permittedWardrobeItems.contains(id);
    }

    private static boolean isBackpack(ItemStack itemStack) {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("EnderWardrobe") == null) {
            return false;
        }
        var api = org.bukkit.Bukkit.getServicesManager().load(com.enderbeam.enderwardrobe.api.WardrobeApi.class);
        return api != null && api.backpackRows(itemStack) > 0;
    }

    /** A new EnderWardrobe backpack or balloon by its id, or null without EnderWardrobe. */
    @org.jetbrains.annotations.Nullable
    public static ItemStack wardrobeItem(String id) {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("EnderWardrobe") == null) return null;
        var api = org.bukkit.Bukkit.getServicesManager().load(com.enderbeam.enderwardrobe.api.WardrobeApi.class);
        return api == null ? null : api.wardrobeItem(id);
    }

    /**
     * One sample item per leaf type this restrict covers, used to check that an enchant really
     * applies to the type (restricts alone don't know e.g. that fortune can't go on a sword).
     */
    public List<ItemStack> getSampleItems() {
        if (isLeaf()) {
            if (!permittedMaterials.isEmpty()) {
                return List.of(new ItemStack(permittedMaterials.getFirst()));
            }
            if (permitsBackpacks) {
                var sample = wardrobeItem("backpack");
                return sample == null ? List.of() : List.of(sample);
            }
            if (!permittedWardrobeItems.isEmpty()) {
                // stands in for the EnderWardrobe item: all that matters here is its tag
                var sample = new ItemStack(Material.LEATHER_HORSE_ARMOR);
                sample.editMeta(meta -> meta.getPersistentDataContainer().set(WARDROBE_OFFHAND, PersistentDataType.STRING, permittedWardrobeItems.getFirst()));
                return List.of(sample);
            }
            // a type made of GardensFurniture items, like watering cans
            var sample = permittedCustomIdentifiers.isEmpty() ? null : GardensFurniture.itemRegistry.buildItem(permittedCustomIdentifiers.getFirst());
            return sample == null ? List.of() : List.of(sample);
        }
        return children.stream().flatMap(child -> child.getSampleItems().stream()).toList();
    }

    // the concrete item types this covers: itself, or its children's types
    public List<EnchantRestrict> getLeaves() {
        if (isLeaf()) {
            return List.of(this);
        }
        return children.stream().flatMap(child -> child.getLeaves().stream()).distinct().toList();
    }

    public List<Character> iconRepresentation() {
        return icons;
    }

    public boolean isEqualOrChild(EnchantRestrict enchantRestrict) {
        return enchantRestrict == this || children.stream().anyMatch(child -> child.isEqualOrChild(enchantRestrict));
    }

    /**
     * Whether this restrict and the given one share any item type. Unlike {@link #isEqualOrChild},
     * this handles groups that aren't nested in each other - e.g. "all" and "excavation" both
     * contain pickaxes, even though excavation isn't a child of all.
     */
    public boolean overlaps(EnchantRestrict enchantRestrict) {
        return isEqualOrChild(enchantRestrict)
                || enchantRestrict.isEqualOrChild(this)
                || children.stream().anyMatch(child -> child.overlaps(enchantRestrict));
    }

    public static class Builder {

        private final String id;

        private final List<Material> permittedMaterials = new ArrayList<>();
        private final List<String> permittedIdentifiers = new ArrayList<>();
        private final List<String> permittedIdSuffixes = new ArrayList<>();
        private List<Character> icons = new ArrayList<>();

        private List<EnchantRestrict> children = new ArrayList<>();
        private final List<String> permittedWardrobeItems = new ArrayList<>();
        private boolean permitsBackpacks;

        public Builder(String id) {
            this.id = id;
        }

        public Builder withMaterial(Material material) {
            this.permittedMaterials.add(material);
            return this;
        }

        public Builder withMaterials(Material... materials) {
            this.permittedMaterials.addAll(Arrays.stream(materials).toList());
            return this;
        }

        // GardensFurniture items, by identifier
        public Builder withCustomIdentifiers(String... identifiers) {
            this.permittedIdentifiers.addAll(Arrays.asList(identifiers));
            return this;
        }

        // backpacks with storage (EnderWardrobe's)
        public Builder withBackpacks() {
            this.permitsBackpacks = true;
            return this;
        }

        // EnderWardrobe offhand items, by id
        public Builder withWardrobeItems(String... ids) {
            this.permittedWardrobeItems.addAll(Arrays.asList(ids));
            return this;
        }

        public Builder withIcon(char icon) {
            this.icons.add(icon);
            return this;
        }

        public Builder withChild(EnchantRestrict enchantRestrict) {
            this.children.add(enchantRestrict);
            this.icons.addAll(enchantRestrict.icons);
            return this;
        }

        public Builder withChildren(EnchantRestrict... enchantRestrict) {
            Arrays.stream(enchantRestrict).forEach(this::withChild);
            return this;
        }

        public Builder setIcons(List<Character> icons) {
            this.icons = icons;
            return this;
        }

        public EnchantRestrict build() {
            var enchantRestrict = new EnchantRestrict(
                    id,
                    permittedMaterials,
                    permittedIdentifiers,
                    permittedIdSuffixes,
                    icons,
                    children,
                    permittedWardrobeItems,
                    permitsBackpacks
            );
            registeredEnchantRestricts.put(id, enchantRestrict);
            return enchantRestrict;
        }

    }
}
