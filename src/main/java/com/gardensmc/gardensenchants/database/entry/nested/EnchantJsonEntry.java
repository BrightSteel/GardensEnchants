package com.gardensmc.gardensenchants.database.entry.nested;

import com.gardensmc.gardensenchants.enchantment.EnchantmentInstance;
import com.gardensmc.gardensenchants.enchantment.EnchantmentInstanceSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

public class EnchantJsonEntry {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(EnchantmentInstance.class, new EnchantmentInstanceSerializer())
            .create();
    @Getter
    private final EnchantmentInstance[] enchantments;

    public EnchantJsonEntry(EnchantmentInstance[] enchantments) {
        this.enchantments = enchantments;
    }

    public EnchantJsonEntry(String json) {
        this.enchantments = gson.fromJson(json, EnchantmentInstance[].class);
    }

    public String toJson() {
        return gson.toJson(enchantments);
    }
}
