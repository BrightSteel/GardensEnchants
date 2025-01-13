//package com.gardensmc.gardensenchants.database;
//
//import com.gardensmc.gardensenchants.utils.EnchantUtils;
//
//import java.time.LocalDateTime;
//
//public class EnchantEntry {
//    private LocalDateTime expiration;
//
//    private CustomEnchantmentOld[] enchantments;
//    private String[] levels;
//
//    private Integer[] destructions;
//
//    // used by database
//    public EnchantEntry(String enchant, LocalDateTime expiration) {
//        this.expiration = expiration;
//        decodeEnchant(enchant);
//    }
//
//    public EnchantEntry(CustomEnchantmentOld[] enchantments, String[] levels, Integer[] destructions, LocalDateTime expiration) {
//        this.enchantments = enchantments;
//        this.levels = levels;
//        this.destructions = destructions;
//        this.expiration = expiration;
//    }
//
//    public CustomEnchantmentOld[] getEnchantments() {
//        return enchantments;
//    }
//
//    public String[] getLevels() {
//        return levels;
//    }
//
//    public Integer[] getDestructions() {
//        return destructions;
//    }
//
//    public LocalDateTime getExpiration() {
//        return expiration;
//    }
//
//    public String getEnchant() {
//        StringBuilder stringBuilder = new StringBuilder();
//        for (int i = 0; i < 3; i++) {
//            stringBuilder.append(enchantments[i].getBookNamespace()).append(",").append(levels[i]).append(",").append(destructions[i]).append(";");
//        }
//        // encode enchantments as one string
//        return stringBuilder.substring(0, stringBuilder.length() - 1);
//    }
//
//    private void decodeEnchant(String enchant) {
//        String[] pairs = enchant.split(";");
//
//        String[] levels_temp = new String[3];
//        String[] bookNamespaces = new String[3];
//        Integer[] destructions_temp = new Integer[3];
//        for (int i = 0; i < 3; i++) {
//            String[] data = pairs[i].split(",");
//            bookNamespaces[i] = data[0];
//            levels_temp[i] = data[1];
//            try {
//                destructions_temp[i] = Integer.parseInt(data[2]);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        // temp fix from old namespace format
//        for (int i = 0; i < bookNamespaces.length; i++) {
//            if (bookNamespaces[i].contains(":")) {
//                String[] parts = bookNamespaces[i].split(":");
//                bookNamespaces[i] = parts[1];
//            }
//        }
//
//        enchantments = EnchantUtils.getEnchantmentsFromKey(bookNamespaces);
//        levels = levels_temp;
//        destructions = destructions_temp;
//    }
//}
