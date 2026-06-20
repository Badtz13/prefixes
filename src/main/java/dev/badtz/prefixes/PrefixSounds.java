package dev.badtz.prefixes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class PrefixSounds {
    public static final SoundEvent REFORGE = registerSound("reforge");

    public static final SoundEvent P_BULKY = registerSound("bulky");
    public static final SoundEvent P_DANGEROUS = registerSound("dangerous");
    public static final SoundEvent P_DULL = registerSound("dull");
    public static final SoundEvent P_HEAVY = registerSound("heavy");
    public static final SoundEvent P_LARGE = registerSound("large");
    public static final SoundEvent P_LEGENDARY = registerSound("legendary");
    public static final SoundEvent P_LIGHT = registerSound("light");
    public static final SoundEvent P_MASSIVE = registerSound("massive");
    public static final SoundEvent P_POINTY = registerSound("pointy");
    public static final SoundEvent P_SAVAGE = registerSound("savage");
    public static final SoundEvent P_SHAMEFUL = registerSound("shameful");
    public static final SoundEvent P_SHARP = registerSound("sharp");
    public static final SoundEvent P_SMALL = registerSound("small");
    public static final SoundEvent P_TERRIBLE = registerSound("terrible");
    public static final SoundEvent P_TINY = registerSound("tiny");
    public static final SoundEvent PUNHAPPY = registerSound("unhappy");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath(Prefixes.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier,
                SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void initialize() {
        Prefixes.LOGGER.info("Registering " + Prefixes.MOD_ID + " Sounds");
    }

}
