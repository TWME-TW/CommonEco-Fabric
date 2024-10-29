package cc.thonly.eco.api;

import cc.thonly.eco.api.obj.CurrencyObj;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CurrencyRegistry {
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final Path item = FabricLoader.getInstance().getConfigDir().resolve("eco-common").resolve("item.json");
    private static final Map<String, Object> defaultConfig = new LinkedHashMap<>();
    protected static final Map<String, EcoItem> registry = new LinkedHashMap<>();
    static {
        defaultConfig.put("items", new ArrayList<>());
    }
    public static void load() {
        File configFile = item.toFile();
        if (!configFile.exists()) {
            try {
                Files.createDirectories(item.getParent());
                objectMapper.writeValue(configFile, defaultConfig);
            } catch (IOException e) {
            }
        }

        try {
            Map<String, Object> config = objectMapper.readValue(configFile, Map.class);
            registerCurrency(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void save() {
        ArrayList<CurrencyObj> items = new ArrayList<>();

        for (Map.Entry<String, EcoItem> entry : registry.entrySet()) {
            String itemId = entry.getKey();
            EcoItem ecoItem = entry.getValue();

            CurrencyObj currency = new CurrencyObj();
            currency.item_id = itemId;
            currency.value = ecoItem.value;
            items.add(currency);
        }

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("items", items);

        try {
            objectMapper.writeValue(item.toFile(), config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void registerCurrency(Map<String, Object> config) {
        registry.clear();
        if (config.containsKey("items")) {
            ArrayList<Object> arr = (ArrayList<Object>) config.get("items");
            for (Object obj : arr) {
                try {
                    CurrencyObj currency = objectMapper.convertValue(obj, CurrencyObj.class);
                    Item item = Registries.ITEM.get(Identifier.of(currency.item_id));
                    Double value = currency.value;
                    EcoItem ecoItem = new EcoItem(item,value);
                    registry.put(currency.item_id, ecoItem);
                } catch (IllegalArgumentException e) {
                }
            }
        }
    }

    public static Map<String, EcoItem> getRegistry() {
        return registry;
    }

    public static EcoItem register(Item item, Double value) {
        EcoItem ecoItem = new EcoItem(item,value);
        registry.put(Registries.ITEM.getId(item).toString(), ecoItem);
        save();
        return ecoItem;
    }
    public static boolean unregister(Item item) {
        String itemId = Registries.ITEM.getId(item).toString();
        if (registry.containsKey(itemId)) {
            registry.remove(itemId);
            save();
            return true;
        }
        return false;
    }

}
