package cc.thonly.eco.api;

import cc.thonly.eco.EcoConfig;
import cc.thonly.eco.impl.EcoManagerAccessor;
import net.minecraft.entity.player.PlayerEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class EcoAPI {
    protected static final Map<String, EcoProfile> playerData = new LinkedHashMap<>();
    protected static final Map<String, EcoProfile> playerDataByUUID = new LinkedHashMap<>();

    public static Map<String, EcoProfile> getAllProfiles() {
        return EcoAPI.playerData;
    }

    public static Map<String, EcoProfile> getAllProfilesFromUUID() {
        return EcoAPI.playerDataByUUID;
    }

    public static double getAmount(PlayerEntity player) {
        EcoProfile data = getPlayerData(player);
        return data != null ? data.balance : 0.0;
    }

    public static double getAmount(String name) {
        EcoProfile data = getPlayerData(name);
        return data != null ? data.balance : 0.0;
    }

    public static double getAmount(UUID uuid) {
        EcoProfile data = getPlayerData(uuid);
        return data != null ? data.balance : 0.0;
    }

    public static void addAmount(PlayerEntity player, double amount) {
        EcoProfile data = getPlayerData(player);
        if (data != null) {
            data.balance += amount;
        }
    }

    public static void addAmount(String name, double amount) {
        EcoProfile data = getPlayerData(name);
        if (data != null) {
            data.balance += amount;
        }
    }

    public static void addAmount(UUID uuid, double amount) {
        EcoProfile data = getPlayerData(uuid);
        if (data != null) {
            data.balance += amount;
        }
    }

    public static void removeAmount(PlayerEntity player, double amount) {
        EcoProfile data = getPlayerData(player);
        if (data != null) {
            data.balance -= amount;
        }
    }

    public static void removeAmount(String name, double amount) {
        EcoProfile data = getPlayerData(name);
        if (data != null) {
            data.balance -= amount;
        }
    }

    public static void removeAmount(UUID uuid, double amount) {
        EcoProfile data = getPlayerData(uuid);
        if (data != null) {
            data.balance -= amount;
        }
    }

    public static boolean hasAmount(PlayerEntity player, double amount) {
        EcoProfile data = getPlayerData(player);
        return data != null && data.balance >= amount;
    }

    public static boolean hasAmount(String name, double amount) {
        EcoProfile data = getPlayerData(name);
        return data != null && data.balance >= amount;
    }

    public static boolean hasAmount(UUID uuid, double amount) {
        EcoProfile data = getPlayerData(uuid);
        return data != null && data.balance >= amount;
    }

    public static void save(PlayerEntity player) {
        EcoManager ecoManager = getEcoManager(player);
        EcoProfile data = getPlayerData(player);
        if (ecoManager != null && data != null) {
            ecoManager.save();
        }
    }

    public static EcoProfile getPlayerData(String name) {
        return EcoAPI.playerData.getOrDefault(name, null);
    }

    public static EcoProfile getPlayerData(UUID uuid) {
        return EcoAPI.playerDataByUUID.getOrDefault(uuid.toString(), null);
    }

    public static EcoProfile getPlayerData(PlayerEntity player) {
        return EcoAPI.playerData.getOrDefault(player.getGameProfile().getName(), null);
    }

    public static void reload() {
        EcoConfig.load();
    }

    public static EcoManager getEcoManager(PlayerEntity player) {
        return ((EcoManagerAccessor) (player)).getEcoManager();
    }
}