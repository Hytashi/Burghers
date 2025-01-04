package fr.hytashi.burghers.event;

import fr.hytashi.burghers.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NPCInteractEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final NPC npc;
    private final Player player;

    public NPCInteractEvent(final NPC npc, final Player player) {
        this.npc = npc;
        this.player = player;
    }

    public NPC getNPC() {
        return this.npc;
    }

    public Player getPlayer() {
        return this.player;
    }

    public HandlerList getHandlers() {
        return NPCInteractEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return NPCInteractEvent.handlers;
    }

}
