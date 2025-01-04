package fr.hytashi.burghers;

import com.comphenix.packetwrapper.WrapperPlayClientUseEntity;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import fr.hytashi.burghers.command.CommandBurghers;
import fr.hytashi.burghers.event.NPCInteractEvent;
import fr.hytashi.burghers.npc.NPC;
import fr.hytashi.burghers.npc.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Objects;

public class Main extends JavaPlugin {

    private static Main INSTANCE;
    private static ProtocolManager PROTOCOL_MANAGER;
    private static final HashMap<Player, Long> LAST_EVENT = new HashMap<>();

    public void onEnable() {

        INSTANCE = this;

        this.getCommand("burghers").setExecutor(new CommandBurghers());

        PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();

        PROTOCOL_MANAGER.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, Client.USE_ENTITY) {

            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientUseEntity packet = new WrapperPlayClientUseEntity(event.getPacket());

                if (packet.getTargetID() < 10000) return;

                // Pour éviter d'envoyer l'event plusieurs fois car reçoit parfois le packet en double
                if (System.nanoTime() - Main.LAST_EVENT.getOrDefault(event.getPlayer(), 0L) < 1_000_000L) {
                    return;
                }

                NPCManager.getNPCS().values().stream()
                        .filter((n) -> n.getEntityID() == packet.getTargetID())
                        .findAny()
                        .ifPresent(npc -> {
                            Bukkit.getServer().getPluginManager().callEvent(new NPCInteractEvent(npc, event.getPlayer()));
                            Main.LAST_EVENT.put(event.getPlayer(), System.nanoTime());
                        });
            }

        });

        Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> {
            File[] npcFiles = Objects.requireNonNull((new File(getInstance().getDataFolder(), "npc/")).listFiles());
            for (File file : npcFiles) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    NPC npc = (NPC) objectInputStream.readObject();
                    objectInputStream.close();
                    fileInputStream.close();
                    npc.create();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(getInstance(), NPCManager::update, 60L, 30L);

    }

    public static Main getInstance() {
        return INSTANCE;
    }

    public static ProtocolManager getProtocolManager() {
        return PROTOCOL_MANAGER;
    }

}
