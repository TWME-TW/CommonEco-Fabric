package cc.thonly.eco;

import cc.thonly.eco.api.EcoAPI;
import cc.thonly.eco.api.EcoManager;
import cc.thonly.eco.init.InitCommand;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonEconomy implements ModInitializer {
	public static final String MOD_ID = "CommonEco(Fabric)";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer server;
	@Override
	public void onInitialize() {
		InitCommand.init();
		Placeholders.register(Identifier.of("eco","balance"),(ctx,arg) -> {
			if (!ctx.hasPlayer()) {
				return PlaceholderResult.invalid("No player!");
			}
			PlayerEntity player = ctx.player();
			EcoManager manager = EcoAPI.getEcoManager(player);
			return PlaceholderResult.value(String.valueOf(manager.ecoProfile.balance));
		});
		LOGGER.info("Initializing Command...");
	}
}