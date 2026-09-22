package com.gardensmc.gardensenchants.enchantment.custom;

import com.gardensmc.gardensfurniture.GardensFurniture;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

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

    public EnchantRestrict(String id) {
        this.id = id;
    }

    public boolean isPermitted(Material material) {
        return permittedMaterials.contains(material);
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
        if (permittedMaterials.contains(itemStack.getType())) {
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
        if (permittedMaterials.contains(itemStack.getType())) {
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

        public Builder(String id) {
            this.id = id;
        }

        public Builder withMaterial(Material material) {
            this.permittedMaterials.add(material);
            return this;
        }

        public Builder withCustomItem(String identifier) {
            this.permittedIdentifiers.add(identifier);
            return this;
        }

        public Builder withMaterials(Material... materials) {
            this.permittedMaterials.addAll(Arrays.stream(materials).toList());
            return this;
        }

        public Builder withCustomItems(String... identifiers) {
            this.permittedIdentifiers.addAll(Arrays.stream(identifiers).toList());
            return this;
        }

        public Builder withIcon(char icon) {
            this.icons.add(icon);
            return this;
        }

        public Builder withIcons(Collection<Character> icons) {
            this.icons.addAll(icons);
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

        public Builder withCustomItemSuffix(String suffix) {
            this.permittedIdSuffixes.add(suffix);
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
                    children
            );
            registeredEnchantRestricts.put(id, enchantRestrict);
            return enchantRestrict;
        }

    }
}
