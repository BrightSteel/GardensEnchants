package com.gardensmc.gardensenchants.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired after a player pays at the enchanting table: for an enchant (Java or Bedrock) or a re-roll. For the transaction
 * log (GardensPlayers).
 *
 * @param kind  "enchant" or "reroll"
 * @param item  the enchant bought (id_level), or for a re-roll the item type re-rolled for
 */
public class TableSpendEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String kind;
    private final String item;
    private final int acorns;
    private final int xp;

    public TableSpendEvent(Player player, String kind, String item, int acorns, int xp) {
        this.player = player;
        this.kind = kind;
        this.item = item;
        this.acorns = acorns;
        this.xp = xp;
    }

    public Player getPlayer() {
        return player;
    }

    public String getKind() {
        return kind;
    }

    public String getItem() {
        return item;
    }

    public int getAcorns() {
        return acorns;
    }

    public int getXp() {
        return xp;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
