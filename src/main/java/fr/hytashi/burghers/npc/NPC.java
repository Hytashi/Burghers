package fr.hytashi.burghers.npc;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import fr.hytashi.burghers.Main;
import fr.hytashi.burghers.implementation.SerializableLocation;
import fr.hytashi.burghers.implementation.SerializableProperty;
import fr.hytashi.burghers.utils.RandomName;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.ScoreboardTeam;
import net.minecraft.server.v1_12_R1.ScoreboardTeamBase.EnumNameTagVisibility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NPC implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String displayName;
    private final String identifier = (new RandomName(12)).nextString();
    private SerializableLocation location;
    private SerializableProperty property;
    private final int entityID = 10000 + NPCManager.getNPCS().size();
    private final UUID uuid = UUID.randomUUID();
    private boolean nametagVisible = true;

    private transient Hologram customNameTag;
    private transient boolean created = false;
    private transient ConcurrentLinkedQueue<Player> viewers;

    public void setProperty(String name, String value, String signature) {
        this.property = new SerializableProperty(name, value, signature);
        this.update();
    }

    public void setLocation(Location location) {
        this.location = new SerializableLocation(location);
        this.updateNametag();
        this.update();
    }

    public void setDisplayName(String name) {
        this.displayName = name.replaceAll("&", "§");
        this.updateNametag();
        this.update();
    }

    public void setNametagVisible(boolean visible) {
        this.nametagVisible = visible;
        this.updateNametag();
        this.update();
    }

    public void toggleNametagVisibility() {
        this.setNametagVisible(!this.getNametagVisible());
    }

    public void setSkin(String url) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                URL target = new URL("https://api.mineskin.org/generate/url");
                HttpURLConnection con = (HttpURLConnection) target.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setConnectTimeout(1000);
                con.setReadTimeout(30000);
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                out.writeBytes("url=" + URLEncoder.encode(url, StandardCharsets.UTF_8));
                out.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                JSONObject output = (JSONObject) (new JSONParser()).parse(reader);
                JSONObject data = (JSONObject) output.get("data");
                JSONObject texture = (JSONObject) data.get("texture");
                String textureEncoded = (String) texture.get("value");
                String signature = (String) texture.get("signature");
                con.disconnect();
                this.setProperty("textures", textureEncoded, signature);
            } catch (ParseException | IOException ex) {
                ex.printStackTrace();
            }

        });
    }

    public void create() {
        this.viewers = new ConcurrentLinkedQueue<>();
        this.created = true;
        this.updateNametag();
        NPCManager.addNPC(this);
        Bukkit.getOnlinePlayers().stream()
                .filter((p) -> p.getWorld().equals(this.location.getWorld()))
                .forEach(this::show);
    }

    public void remove() {
        Bukkit.getOnlinePlayers().stream().filter((p) -> p.getWorld().equals(this.location.getWorld())).forEach(this::hide);
        NPCManager.removeNPC(this);
        this.customNameTag.delete();
    }

    public void show(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            this.viewers.add(player);
            WrapperPlayServerNamedEntitySpawn spawnPacket = new WrapperPlayServerNamedEntitySpawn();
            spawnPacket.setEntityID(this.entityID);
            spawnPacket.setPlayerUUID(this.uuid);
            spawnPacket.setX(this.location.getX());
            spawnPacket.setY(this.location.getY());
            spawnPacket.setZ(this.location.getZ());
            spawnPacket.setYaw((byte) ((int) (this.location.getYaw() * 256.0F / 360.0F)));
            spawnPacket.setPitch(this.location.getPitch());
            WrappedGameProfile p = new WrappedGameProfile(this.uuid, this.identifier);
            p.getProperties().put("textures", new WrappedSignedProperty(this.property.getName(), this.property.getValue(), this.property.getSignature()));
            PlayerInfoData data = new PlayerInfoData(p, 10, NativeGameMode.CREATIVE, WrappedChatComponent.fromText(this.identifier));
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            WrappedDataWatcher.WrappedDataWatcherObject secondLayer = new WrappedDataWatcher.WrappedDataWatcherObject(13, Registry.get(Byte.class));
            watcher.setObject(secondLayer, (byte) 127);
            WrappedDataWatcher.WrappedDataWatcherObject hideWhenFarAway = new WrappedDataWatcher.WrappedDataWatcherObject(3, Registry.get(Boolean.class));
            watcher.setObject(hideWhenFarAway, false);
            spawnPacket.setMetadata(watcher);
            WrapperPlayServerPlayerInfo playerInfoPacket = new WrapperPlayServerPlayerInfo();
            playerInfoPacket.setAction(PlayerInfoAction.ADD_PLAYER);
            playerInfoPacket.setData(Collections.singletonList(data));
            WrapperPlayServerPlayerInfo hideTabPacket = new WrapperPlayServerPlayerInfo();
            hideTabPacket.setAction(PlayerInfoAction.REMOVE_PLAYER);
            hideTabPacket.setData(Collections.singletonList(data));
            WrapperPlayServerEntityHeadRotation rotation = new WrapperPlayServerEntityHeadRotation();
            rotation.setEntityID(this.entityID);
            float yaw = this.location.getYaw() * 256.0F / 360.0F;
            rotation.setHeadYaw((byte) ((int) yaw));
            WrapperPlayServerEntityTeleport tp = new WrapperPlayServerEntityTeleport();
            tp.setEntityID(this.entityID);
            tp.setX(this.location.getX());
            tp.setY(this.location.getY());
            tp.setZ(this.location.getZ());
            tp.setYaw(this.location.getYaw());
            tp.setPitch(this.location.getPitch());
            playerInfoPacket.sendPacket(player);
            spawnPacket.sendPacket(player);
            rotation.sendPacket(player);
            tp.sendPacket(player);
            this.sendPacketsLater(player, 100L, hideTabPacket.getHandle());
            ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), p.getName());
            team.setNameTagVisibility(EnumNameTagVisibility.NEVER);
            ArrayList<String> playerToAdd = new ArrayList<>();
            playerToAdd.add(this.identifier);
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 1));
            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, playerToAdd, 3));
            if (this.nametagVisible) {
                this.customNameTag.setShowPlayer(player);
            }
        });
    }

    public void hide(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            this.viewers.remove(player);
            WrapperPlayServerEntityDestroy destroyPacket = new WrapperPlayServerEntityDestroy();
            int[] ids = new int[]{this.entityID};
            destroyPacket.setEntityIds(ids);
            destroyPacket.sendPacket(player);
            this.customNameTag.removeShowPlayer(player);
        });
    }

    private void update() {
        if (NPCManager.getNPCS().containsKey(this.uuid)) {
            NPCManager.addNPC(this);
            Bukkit.getOnlinePlayers().stream()
                    .filter((p) -> p.getWorld().equals(this.location.getWorld()))
                    .forEach((p) -> {
                        this.hide(p);
                        this.show(p);
                    });
        }

    }

    public Location getLocation() {
        return this.location.toBukkitLocation();
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public boolean getNametagVisible() {
        return this.nametagVisible;
    }

    public ArrayList<Player> getViewers() {
        return new ArrayList<>(this.viewers);
    }

    private void updateNametag() {
        if (!this.created)
            return;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            if (this.customNameTag != null) {
                this.customNameTag.delete();
            }
            this.customNameTag = DHAPI.createHologram(getHologramName(),
                    this.getLocation().clone().add(0.0, 2.3, 0.0),
                    Collections.singletonList(this.displayName));
            this.customNameTag.setDefaultVisibleState(false);
        });
    }

    private String getHologramName() {
        return "npcnametag-"
                + uuid
                + location.getWorldName().replaceAll("#", "")
                + entityID;
    }

    private void sendPacketsLater(Player player, long delay, PacketContainer... packets) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            for (PacketContainer pc : packets) {
                Main.getProtocolManager().sendServerPacket(player, pc);
            }
        }, delay);
    }

    public static NPCBuilder builder() {
        return new NPCBuilder();
    }

}
