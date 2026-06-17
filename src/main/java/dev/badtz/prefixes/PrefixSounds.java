package dev.badtz.prefixes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class PrefixSounds {
    public static final SoundEvent REFORGE = registerSound("reforge");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath(Prefixes.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier,
                SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void initialize() {
        Prefixes.LOGGER.info("Registering " + Prefixes.MOD_ID + " Sounds");
    }

}
