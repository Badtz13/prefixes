package dev.badtz.prefixes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;

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

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("prefix")
					.then(Commands.argument("id", StringArgumentType.string()).executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						ItemStack stack = player.getMainHandItem();
						String rawId = StringArgumentType.getString(context, "id");

						Identifier prefixId =
								rawId.contains(":") ? Identifier.tryParse(rawId) : id(rawId);

						if (prefixId == null) {
							context.getSource()
									.sendFailure(Component.literal("Invalid prefix id: " + rawId));
							return 0;
						}

						if (stack.isEmpty()) {
							context.getSource()
									.sendFailure(Component.literal("Hold an item first"));
							return 0;
						}

						PrefixManager.PrefixDefinition prefix = PrefixManager.get(prefixId);

						if (prefix == null) {
							context.getSource()
									.sendFailure(Component.literal("Unknown prefix: " + prefixId));
							return 0;
						}

						if (!PrefixApplier.canApply(stack, prefix)) {
							context.getSource().sendFailure(
									Component.literal("Cannot be applied to this item."));
							return 0;
						}

						PrefixApplier.apply(stack, prefix);

						context.getSource().sendSuccess(
								() -> Component.literal("Set prefix to " + prefixId), true);

						return 1;
					})));
		});

		LOGGER.info("Prefixes initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
