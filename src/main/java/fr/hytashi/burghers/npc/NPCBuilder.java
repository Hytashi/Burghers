package fr.hytashi.burghers.npc;

import com.mojang.authlib.properties.Property;
import org.bukkit.Location;

public class NPCBuilder {

    private String displayName;
    private Location location;
    private Property property;
    private boolean nametagVisible = true;

    public NPCBuilder setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public NPCBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    public NPCBuilder setProperty(Property property) {
        this.property = property;
        return this;
    }

    public NPCBuilder setProperty(String name, String value, String signature) {
        this.property = new Property(name, value, signature);
        return this;
    }

    public NPCBuilder setNameTagVisible(boolean nametagVisible) {
        this.nametagVisible = nametagVisible;
        return this;
    }

    public NPC build() {
        NPC npc = new NPC();
        npc.setDisplayName(this.displayName);
        npc.setLocation(this.location);
        if (this.property == null) {
            this.setProperty("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYxMjgyMzg3MTUwOCwKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ2MWVhNTI1N2U3ZGM4NjQxM2JhN2FhMTUxNDJlOWRiYzE1YzY1YzE4NTE3NmI5YmZlZTFiN2QzY2RiYWVlMGEiCiAgICB9CiAgfQp9", "NLDviMhfvk29rg4Mhwqj0FESxMHN6N7wEciZcpzMALeiNMhdERGkW3jrMt/yx4tORPcTMP46ltEJmaAODEqmbOhTFUj7nUQXYY5HgCbVNQg4A9OeNFBk6DvBxd/pQBR2X2sIFq9O6uefk/FPSkwHAiGokINgo1bxXjZ9TPsppf90o2GUWy5/l39Pqt65zOZqL+22c46UurRvSnrragRW9E6WYVPYegvJEWDgVmUi626S3BkUomcZVDg5Ix0M4zh6822PEykHwWuuR2KldoOhu+32Ergxf4fG3FCKGRomuSHaMdXcyHvFW0yb7O0OYYUgSGehHPPiCVzAgtADzLZFfSsbuv4UbNqWHDkOU9IflOtOP4sXT5/7ValIfVr9TOwgQeoqI0C1X7UsX25eg7VwRhqkvUJt9wLjJFwXqllLlnYUWjvdDhBPKJBDjTxIckvKlCX0nH5BtNFhN23r1eZSlXh2FBOxYfsxHKmfref6f+SNV8mRU5p6e+OqqHeX66sHYwFe/oSmxPJkq/YRbGoxOuErlDvZ3+hZmlEINoKHjdsb2sNhg22J+GeVeasobrRFWq10OOhqgwsl9okq+deVB7T1so/5rr8OJSbSnkUkx5Msk12kwzc9h1zWxLGp4Ca2O7DusH/D9fY2cHLUPZ04lK6eQvOXgCilOjJ0lJR8oSc=");
        }

        npc.setProperty(this.property.getName(), this.property.getValue(), this.property.getSignature());
        npc.setNametagVisible(this.nametagVisible);
        npc.create();
        return npc;
    }

}
