package fr.hytashi.burghers.command;

import fr.hytashi.burghers.Main;
import fr.hytashi.burghers.npc.NPC;
import fr.hytashi.burghers.npc.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CommandBurghers implements CommandExecutor {

    private static final String DEFAULT_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYxMjgyMzg3MTUwOCwKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ2MWVhNTI1N2U3ZGM4NjQxM2JhN2FhMTUxNDJlOWRiYzE1YzY1YzE4NTE3NmI5YmZlZTFiN2QzY2RiYWVlMGEiCiAgICB9CiAgfQp9";
    private static final String DEFAULT_SIGNATURE = "NLDviMhfvk29rg4Mhwqj0FESxMHN6N7wEciZcpzMALeiNMhdERGkW3jrMt/yx4tORPcTMP46ltEJmaAODEqmbOhTFUj7nUQXYY5HgCbVNQg4A9OeNFBk6DvBxd/pQBR2X2sIFq9O6uefk/FPSkwHAiGokINgo1bxXjZ9TPsppf90o2GUWy5/l39Pqt65zOZqL+22c46UurRvSnrragRW9E6WYVPYegvJEWDgVmUi626S3BkUomcZVDg5Ix0M4zh6822PEykHwWuuR2KldoOhu+32Ergxf4fG3FCKGRomuSHaMdXcyHvFW0yb7O0OYYUgSGehHPPiCVzAgtADzLZFfSsbuv4UbNqWHDkOU9IflOtOP4sXT5/7ValIfVr9TOwgQeoqI0C1X7UsX25eg7VwRhqkvUJt9wLjJFwXqllLlnYUWjvdDhBPKJBDjTxIckvKlCX0nH5BtNFhN23r1eZSlXh2FBOxYfsxHKmfref6f+SNV8mRU5p6e+OqqHeX66sHYwFe/oSmxPJkq/YRbGoxOuErlDvZ3+hZmlEINoKHjdsb2sNhg22J+GeVeasobrRFWq10OOhqgwsl9okq+deVB7T1so/5rr8OJSbSnkUkx5Msk12kwzc9h1zWxLGp4Ca2O7DusH/D9fY2cHLUPZ04lK6eQvOXgCilOjJ0lJR8oSc=";

    private static final HashMap<Player, UUID> selected = new HashMap<>();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player) || !sender.isOp())
            return false;

        if (args.length < 1)
            return false;

        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {

            if (args[1].length() > 16) {
                sender.sendMessage(ChatColor.RED + "Error: Name cannot be longer than 16 characters");
                return false;
            }

            NPC npc = NPC.builder()
                    .setDisplayName(args[1])
                    .setLocation(player.getLocation())
                    .setProperty("textures", DEFAULT_TEXTURE, DEFAULT_SIGNATURE)
                    .setNameTagVisible(false)
                    .build();
            selected.put(player, npc.getUUID());

        } else if (args.length == 1 && (args[0].equalsIgnoreCase("sel") || args[0].equalsIgnoreCase("select"))) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
                if (NPCManager.getNPCS().isEmpty()) return;

                Location location = player.getLocation();
                AtomicReference<NPC> selected = new AtomicReference<>(null);
                NPCManager.getNPCS().values().stream()
                        .filter(npc -> npc.getLocation().getWorld().equals(player.getWorld()))
                        .filter(npc -> npc.getLocation().distance(player.getLocation()) < 10.0)
                        .forEach(npc -> {
                            if (selected.get() == null || npc.getLocation().distanceSquared(location) < selected.get().getLocation().distanceSquared(location)) {
                                selected.set(npc);
                            }
                        });
                if (selected.get() != null) {
                    CommandBurghers.selected.put(player, selected.get().getUUID());
                    sendSync(player, ChatColor.AQUA + "You selected " + ChatColor.YELLOW + selected.get().getDisplayName());
                }
            });
        } else if (args.length != 1 || !args[0].equalsIgnoreCase("rem") && !args[0].equalsIgnoreCase("remove")) {

            if (args.length == 2 && args[0].equalsIgnoreCase("skin")) {
                if (hasNotSelected(player)) return false;
                NPC npc = NPCManager.getNPC(selected.get(player));
                npc.setSkin(args[1]);
                sendSync(player, ChatColor.AQUA + "Successfully updated " + ChatColor.YELLOW + npc.getDisplayName() + ChatColor.AQUA + "'s skin");
            } else if (args.length == 1 && (args[0].equalsIgnoreCase("tph") || args[0].equalsIgnoreCase("tphere"))) {
                if (hasNotSelected(player)) return false;

                NPC npc = NPCManager.getNPC(selected.get(player));
                npc.setLocation(player.getLocation());
                sendSync(player, ChatColor.YELLOW + npc.getDisplayName() + ChatColor.AQUA + " was teleported to your location");
            } else if (args.length == 1 && args[0].equalsIgnoreCase("tp")) {
                if (hasNotSelected(player)) return false;

                NPC npc = NPCManager.getNPC(selected.get(player));
                player.teleport(npc.getLocation());
                sendSync(player, ChatColor.AQUA + "You teleported to " + ChatColor.YELLOW + npc.getDisplayName());
            } else if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
                if (hasNotSelected(player)) return false;

                NPC npc = NPCManager.getNPC(selected.get(player));
                player.sendMessage("name=" + npc.getDisplayName() + ChatColor.WHITE + ", entityID=" + npc.getEntityID() + ", UUID=" + npc.getUUID());
            } else if (args.length == 2 && args[0].equalsIgnoreCase("rename")) {
                if (hasNotSelected(player)) return false;

                NPC npc = NPCManager.getNPC(selected.get(player));
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> player.sendMessage(ChatColor.AQUA + "Successfully renamed " + ChatColor.YELLOW + npc.getDisplayName() + ChatColor.AQUA + " to " + ChatColor.YELLOW + args[1]));
                npc.setDisplayName(args[1]);
            } else if (args.length == 1 && args[0].equalsIgnoreCase("name")) {
                if (hasNotSelected(player)) return false;

                NPC npc = NPCManager.getNPC(selected.get(player));
                npc.toggleNametagVisibility();
                sendSync(player, ChatColor.AQUA + "Nametag visibility toggled");
            } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                for (NPC npc : NPCManager.getNPCS().values()) {
                    player.sendMessage(npc.getDisplayName());
                }
            }

        } else if (selected.containsKey(player) && selected.get(player) != null) {
            NPC npc = NPCManager.getNPC(selected.get(player));
            npc.remove();
            sendSync(player, ChatColor.AQUA + "Successfully removed " + ChatColor.YELLOW + npc.getDisplayName());
        } else {
            sendSync(player, ChatColor.RED + "You must have an NPC selected to execute that command");
        }

        return true;

    }

    private void sendSync(Player player, String msg) {
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> player.sendMessage(msg));
    }

    private boolean hasNotSelected(Player player) {
        if (!selected.containsKey(player) || selected.get(player) == null) {
            sendSync(player, "You must have an NPC selected to execute that command");
            return true;
        }
        return false;
    }

}
