package cc.thonly.eco;

import cc.thonly.eco.init.InitCommand;
import net.fabricmc.api.ModInitializer;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonEconomy implements ModInitializer {
	public static final String MOD_ID = "CommonEco(Fabric)";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer server;
	@Override
	public void onInitialize() {
		InitCommand.init();
		LOGGER.info("Initializing Command...");
	}
}