package fr.hytashi.burghers.implementation;

import java.io.*;
import com.mojang.authlib.properties.*;

public class SerializableProperty implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String value;
    private final String signature;

    public SerializableProperty(final Property property) {
        this.name = property.getName();
        this.value = property.getValue();
        this.signature = property.getSignature();
    }

    public SerializableProperty(final String name, final String value, final String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    public Property toMojangProperty() {
        return new Property(this.name, this.value, this.signature);
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String getSignature() {
        return this.signature;
    }
}
