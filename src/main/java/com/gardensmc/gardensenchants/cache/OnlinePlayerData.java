package com.gardensmc.gardensenchants.cache;

public class OnlinePlayerData {

    private int combatTicks;

    public void tick(int ticksPassed) {
        if (combatTicks > 0) {
            combatTicks = Math.max(0, combatTicks - ticksPassed);
        }
    }

    public boolean isInCombat() {
        return combatTicks > 0;
    }

    public void startCombatTimer() {
        combatTicks = 100;
    }
}
