package com.gardensmc.gardensenchants.cache;

import lombok.Setter;

public class OnlinePlayerData {

    private int combatTicks;
    @Setter
    private int doubleStrikeCooldown;

    public void tick(int ticksPassed) {
        if (combatTicks > 0) {
            combatTicks = Math.max(0, combatTicks - ticksPassed);
        }
        if (doubleStrikeCooldown > 0) {
            doubleStrikeCooldown = Math.max(0, doubleStrikeCooldown - ticksPassed);
        }
    }

    public boolean isInCombat() {
        return combatTicks > 0;
    }

    public boolean hasDoubleStrikeCooldown() {
        return doubleStrikeCooldown > 0;
    }

    public void startCombatTimer() {
        combatTicks = 100;
    }
}
