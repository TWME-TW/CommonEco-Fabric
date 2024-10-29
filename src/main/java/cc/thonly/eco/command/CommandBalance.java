package cc.thonly.eco.command;

import cc.thonly.eco.impl.EcoManagerAccessor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandBalance {
    public static void register(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        String[] l = {"balance","money","bal","eco"};
        for (String cmd : l) {
            serverCommandSourceCommandDispatcher.register(CommandManager.literal(cmd)
                    .executes(CommandBalance::run)
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .suggests(CommandPlayerSuggestionProvider.create())
                            .executes(CommandBalance::target)
                    )
            );
        }
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        PlayerEntity playerEntity = playerManager.getPlayer(context.getSource().getName());
        if(playerEntity != null) {
            var ecoManager = ((EcoManagerAccessor) (playerEntity)).getEcoManager();
            context.getSource().sendFeedback(() -> Text.literal("§b" + "你的账户余额为 " + ecoManager.ecoProfile.balance),false);
        }
        return 0;
    }
    public static int target(CommandContext<ServerCommandSource> context) {
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        String player = StringArgumentType.getString(context, "player");
        PlayerEntity playerEntity = playerManager.getPlayer(player);
        if(playerEntity != null) {
            var ecoManager = ((EcoManagerAccessor) (playerEntity)).getEcoManager();
            context.getSource().sendFeedback(() -> Text.literal("§b" + player + "的账户余额为 " + ecoManager.ecoProfile.balance),false);
        }
        return 0;
    }
}
