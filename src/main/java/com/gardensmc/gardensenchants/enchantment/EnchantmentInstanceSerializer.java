package com.gardensmc.gardensenchants.enchantment;

import com.google.gson.*;

import java.lang.reflect.Type;

public class EnchantmentInstanceSerializer implements JsonSerializer<EnchantmentInstance>, JsonDeserializer<EnchantmentInstance> {

    @Override
    public JsonElement serialize(
            EnchantmentInstance enchantmentInstance, Type type, JsonSerializationContext jsonSerializationContext
    ) {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("name", enchantmentInstance.getEnchantment().getId());
        jsonObject.addProperty("level", enchantmentInstance.getLevel());
        return jsonObject;
    }


    @Override
    public EnchantmentInstance deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var jsonObject = jsonElement.getAsJsonObject();
        return new EnchantmentInstance(
                GardensEnchantments.INSTANCE.getEnchantment(jsonObject.get("name").getAsString()),
                jsonObject.get("level").getAsInt()
        );
    }
}
