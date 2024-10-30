package cc.thonly.eco.command;

import cc.thonly.eco.api.EcoAPI;
import cc.thonly.eco.api.EcoProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.AbstractMap;
import java.util.stream.Collectors;

public class CommandBalanceTop {
    private static final int ENTRIES_PER_PAGE = 8; // 每页显示8个玩家

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        String[] commands = {"balancetop", "baltop"};
        for (String cmd : commands) {
            dispatcher.register(CommandManager.literal(cmd)
                    .executes(context -> run(context, 1)) // 默认显示第一页
                    .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                            .executes(context -> run(context, IntegerArgumentType.getInteger(context, "page"))))
            );
        }
    }

    public static int run(CommandContext<ServerCommandSource> context, int page) {
        Map<String, EcoProfile> ecoProfileMap = EcoAPI.getAllProfiles();

        List<Map.Entry<String, EcoProfile>> sortedProfiles = ecoProfileMap.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue().balance, entry1.getValue().balance))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) sortedProfiles.size() / ENTRIES_PER_PAGE);
        page = Math.min(page, totalPages);

        int finalPage = page;
        context.getSource().sendFeedback(() -> Text.translatable("command.baltop.title", finalPage, totalPages), false);

        int startIndex = (page - 1) * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, sortedProfiles.size());

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<String, EcoProfile> entry = sortedProfiles.get(i);
            String name = entry.getValue().name;
            double amount = entry.getValue().balance;
            int finalI = i;
            context.getSource().sendFeedback(() -> Text.translatable("command.baltop.entry", finalI + 1, name, amount), false);
        }

        PlayerEntity playerEntity = context.getSource().getPlayer();
        if (playerEntity != null) {
            String playerName = playerEntity.getGameProfile().getName();
            EcoProfile playerProfile = ecoProfileMap.get(playerName);
            if (playerProfile != null) {
                int playerRank = sortedProfiles.indexOf(new AbstractMap.SimpleEntry<>(playerName, playerProfile)) + 1;
                context.getSource().sendFeedback(() -> Text.translatable("command.baltop.player.rank", playerRank), false);
            }
        }
        return 0;
    }
}
