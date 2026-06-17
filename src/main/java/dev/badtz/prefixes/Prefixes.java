package dev.badtz.prefixes;

import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.badtz.prefixes.loot.PrefixLoot;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class Prefixes implements ModInitializer {
	public static final String MOD_ID = "prefixes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final DataComponentType<Identifier> PREFIX =
			Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("prefix"),
					DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).build());

	@Override
	public void onInitialize() {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(id("prefix_loader"),
				new PrefixManager());
		PrefixLoot.initialize();
		ModBlocks.initialize();
		PrefixStats.initialize();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					Commands.literal("prefix").then(Commands.literal("random").executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						ItemStack stack = player.getMainHandItem();

						if (stack.isEmpty()) {
							context.getSource()
									.sendFailure(Component.literal("Hold an item first"));
							return 0;
						}

						if (!PrefixApplier.applyRandom(stack, player.getRandom())) {
							context.getSource().sendFailure(
									Component.literal("No valid random prefix for this item"));
							return 0;
						}

						Identifier prefixId = stack.get(PREFIX);
						String message = prefixId == null ? "Applied random prefix"
								: "Applied random prefix: " + prefixId;

						context.getSource().sendSuccess(() -> Component.literal(message), true);

						return 1;
					})).then(
							Commands.argument("id", StringArgumentType.greedyString())
									.suggests((context, builder) -> SharedSuggestionProvider
											.suggest(getPrefixSuggestions(), builder))
									.executes(context -> {
										ServerPlayer player =
												context.getSource().getPlayerOrException();
										ItemStack stack = player.getMainHandItem();
										String rawId = StringArgumentType.getString(context, "id");

										PrefixManager.PrefixDefinition prefix =
												getCommandPrefix(rawId);

										if (prefix == null) {
											context.getSource().sendFailure(
													Component.literal("Unknown prefix: " + rawId));
											return 0;
										}

										if (stack.isEmpty()) {
											context.getSource().sendFailure(
													Component.literal("Hold an item first"));
											return 0;
										}

										if (!PrefixApplier.canApply(stack, prefix)) {
											context.getSource().sendFailure(Component
													.literal("Cannot be applied to this item."));
											return 0;
										}

										PrefixApplier.apply(stack, prefix);

										String message =
												"Set prefix to " + commandType(prefix.type()) + ":"
														+ prefix.id().getPath();

										context.getSource().sendSuccess(
												() -> Component.literal(message), true);

										return 1;
									})));
		});

		LOGGER.info("Prefixes initialized");
	}

	private static PrefixManager.PrefixDefinition getCommandPrefix(String rawId) {
		String[] parts = rawId.split(":", 2);

		if (parts.length == 2) {
			return switch (parts[0]) {
				case "weapon", "weapons" -> PrefixManager.weapons().get(id(parts[1]));
				case "tool", "tools" -> PrefixManager.tools().get(id(parts[1]));
				default -> null;
			};
		}

		Identifier prefixId = id(rawId);
		PrefixManager.PrefixDefinition weapon = PrefixManager.weapons().get(prefixId);
		PrefixManager.PrefixDefinition tool = PrefixManager.tools().get(prefixId);

		if (weapon != null && tool != null) {
			return null;
		}

		return weapon != null ? weapon : tool;
	}

	private static Stream<String> getPrefixSuggestions() {
		Stream<String> weapons =
				PrefixManager.weapons().keySet().stream().map(id -> "weapons:" + id.getPath());

		Stream<String> tools =
				PrefixManager.tools().keySet().stream().map(id -> "tools:" + id.getPath());

		return Stream.concat(weapons, tools);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private static String commandType(PrefixManager.PrefixType type) {
		return switch (type) {
			case WEAPON -> "weapons";
			case TOOL -> "tools";
		};
	}

	public static String starsForTier(int tier) {
		return switch (tier) {
			case 2 -> "★★★★★";
			case 1 -> "★★★★☆";
			case 0 -> "★★★☆☆";
			case -1 -> "★★☆☆☆";
			case -2 -> "★☆☆☆☆";
			default -> "★★★☆☆";
		};
	}

	public static Rarity rarityForTier(int tier) {
		return switch (tier) {
			case 1 -> Rarity.UNCOMMON;
			case 2 -> Rarity.RARE;
			default -> Rarity.COMMON;
		};
	}

	public static void playReforgeSound(Level level, BlockPos pos,
			PrefixManager.PrefixDefinition prefix) {
		float pitch = switch (prefix.tier()) {
			case 2 -> 1.35f;
			case 1 -> 1.15f;
			case 0 -> 1.0f;
			case -1 -> 0.8f;
			case -2 -> 0.65f;
			default -> 1.0f;
		};

		float volume = switch (prefix.tier()) {
			case 2 -> 1.2f;
			case 1 -> 1.0f;
			case 0 -> 0.9f;
			case -1 -> 0.8f;
			case -2 -> 0.7f;
			default -> 0.9f;
		};

		level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, volume, pitch);
	}

	public static void awardFiveStarAdvancement(ServerPlayer player,
			PrefixManager.PrefixDefinition prefix) {
		if (player == null || prefix.tier() < 2) {
			return;
		}

		AdvancementHolder advancement = player.level().getServer().getAdvancements()
				.get(id("adventure/find_five_star_prefix"));

		if (advancement != null) {
			player.getAdvancements().award(advancement, "found");
		}
	}
}
