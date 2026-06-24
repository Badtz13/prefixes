package dev.badtz.prefixes;

import net.minecraft.resources.Identifier;

public interface PrefixTrackedProjectile {
    void prefixes$setPrefixId(Identifier prefixId);

    Identifier prefixes$getPrefixId();
}
