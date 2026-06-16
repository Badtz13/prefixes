package dev.badtz.prefixes.client;

import net.fabricmc.api.ClientModInitializer;

public class PrefixesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ReforgingTableHud.initialize();
	}
}
