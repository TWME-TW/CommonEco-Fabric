package cc.thonly.eco.api;

import cc.thonly.eco.EcoConfig;
import net.minecraft.entity.player.PlayerEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EcoManager {
    private PlayerEntity player;
    public String name;
    public String uuid;
    public EcoProfile ecoProfile;
    public EcoManager(PlayerEntity player) {
        Thread thread = new Thread(() -> {
            while (player.getGameProfile()==null && !Thread.currentThread().isInterrupted()) {
            }
            if (!Thread.currentThread().isInterrupted()) {
                this.name = player.getName().getString();
                this.uuid = player.getGameProfile().getId().toString();
                this.player = player;
                this.load();
                this.save();
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
    }
    public void load() {
        this.ecoProfile = EcoAPI.getPlayerData(this.getPlayer());
        if(this.ecoProfile == null) {
            this.ecoProfile = new EcoProfile();
            ecoProfile.name = this.name;
            ecoProfile.uuid = this.uuid;
            ecoProfile.balance = 0.0;
            EcoAPI.getAllProfiles().put(this.ecoProfile.name,this.ecoProfile);
            EcoAPI.getAllProfilesFromUUID().put(this.ecoProfile.uuid,this.ecoProfile);
        }
    }
    public void save() {
        try {
            Path filePath = EcoConfig.data.resolve(this.name + ".json");
            Files.createDirectories(filePath.getParent());
            EcoConfig.objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), this.ecoProfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}
