package cc.thonly.eco;

import cc.thonly.eco.api.obj.ConfigObj;
import cc.thonly.eco.api.CurrencyRegistry;
import cc.thonly.eco.api.EcoAPI;
import cc.thonly.eco.api.EcoProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class EcoConfig {
    public static final Path config_path = FabricLoader.getInstance().getConfigDir().resolve("eco-common").resolve("eco-config.json");
    public static final Path data = FabricLoader.getInstance().getConfigDir().resolve("eco-common").resolve("player");
    public static final ObjectMapper objectMapper = new ObjectMapper();
    private static Map<String, Object> config;
    private static final Map<String, Object> defaultConfig = new LinkedHashMap<>();

    static {
        defaultConfig.put("eco_block_ratio", 1.0);
        defaultConfig.put("select_item", "minecraft:wooden_hoe");
        try {
            Files.createDirectories(data.getParent());
            Files.createFile(config_path);
        } catch (IOException e) {
        }
    }

    public static void load() {
        CommonEconomy.LOGGER.info("Loading user data");
        File configFile = new File(config_path.toUri());
        try {
            if (!configFile.exists()) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, defaultConfig);
            } else if (configFile.length() == 0) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, defaultConfig);
            }
            config = (Map<String, Object>) objectMapper.readValue(configFile, Map.class);
            ConfigObj.read(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadPlayerData();
        CurrencyRegistry.load();
    }

    public static void loadPlayerData() {
        ObjectMapper objectMapper = new ObjectMapper();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(data, "*.json")) {
            for (Path path : stream) {
                EcoProfile profile = objectMapper.readValue(path.toFile(), EcoProfile.class);
                EcoAPI.getAllProfiles().put(profile.name,profile);
                EcoAPI.getAllProfilesFromUUID().put(profile.uuid,profile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> getConfig() {
        return config;
    }
}