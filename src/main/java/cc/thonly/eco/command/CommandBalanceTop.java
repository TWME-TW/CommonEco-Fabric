package cc.thonly.eco.command;

import cc.thonly.eco.api.EcoAPI;
import cc.thonly.eco.api.EcoProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.*;
import java.util.stream.Collectors;

public class CommandBalanceTop {
    public static void register(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        String[] l = {"balancetop","baltop"};
        for (String cmd : l) {
            serverCommandSourceCommandDispatcher.register(CommandManager.literal(cmd)
                    .executes(CommandBalanceTop::run)
            );
        }
    }
    public static int run(CommandContext<ServerCommandSource> context) {
        Map<String, EcoProfile> ecoProfileMap = EcoAPI.getAllProfiles();

        List<Map.Entry<String, EcoProfile>> sortedProfiles = ecoProfileMap.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue().balance, entry1.getValue().balance))
                .collect(Collectors.toList());

        context.getSource().sendFeedback(() -> Text.literal("§b排行榜"), false);

        int i = 0;
        for (Map.Entry<String, EcoProfile> entry : sortedProfiles) {
            if (i >= 8) break;
            String name = entry.getValue().name;
            double amount = entry.getValue().balance;
            int finalI = i;
            context.getSource().sendFeedback(() -> Text.literal("§e" + (finalI + 1) + " " + name + ": " + amount), false);
            i++;
        }

        PlayerEntity playerEntity = context.getSource().getPlayer();
        if (playerEntity != null) {
            String playerName = playerEntity.getGameProfile().getName();
            EcoProfile playerProfile = ecoProfileMap.get(playerName);
            if (playerProfile != null) {
                int playerRank = sortedProfiles.indexOf(new AbstractMap.SimpleEntry<>(playerName, playerProfile)) + 1;
                context.getSource().sendFeedback(() -> Text.literal("§b你的排行: §e" + playerRank), false);
            }
        }
        return 0;
    }
}
