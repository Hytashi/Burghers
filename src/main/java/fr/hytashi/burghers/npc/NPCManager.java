package fr.hytashi.burghers.npc;

import fr.hytashi.burghers.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NPCManager {

    private static final ConcurrentHashMap<UUID, NPC> NPCs = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<UUID, NPC> getNPCS() {
        return NPCs;
    }

    public static void addNPC(NPC npc) {
        NPCs.put(npc.getUUID(), npc);
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                File file = new File(Main.getInstance().getDataFolder(), "npc/" + npc.getUUID().toString() + ".ser");
                FileOutputStream fileOut = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(npc);
                out.close();
                fileOut.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void removeNPC(NPC npc) {
        NPCs.remove(npc.getUUID());
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            new File(Main.getInstance().getDataFolder(), "npc/" + npc.getUUID().toString() + ".ser").delete();
        });
    }

    public static NPC getNPC(UUID uuid) {
        return NPCs.get(uuid);
    }

    public static void update() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                NPCs.values().forEach(npc -> {
                    if (npc.getLocation().getWorld().equals(player.getLocation().getWorld())
                            && !npc.getViewers().contains(player)
                            && npc.getLocation().distance(player.getLocation()) <= 100.0) {
                        npc.show(player);
                    } else if (npc.getViewers().contains(player) && (!npc.getLocation().getWorld().equals(player.getLocation().getWorld())
                                || npc.getLocation().distance(player.getLocation()) > 100.0)) {
                        npc.hide(player);
                    }
                });
            }
        });
    }

}
