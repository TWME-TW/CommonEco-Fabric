package cc.thonly.eco.command;

import cc.thonly.eco.impl.EcoManagerAccessor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandPay {
    public static void register(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        String[] l = {"pay"};
        for (String cmd : l) {
            serverCommandSourceCommandDispatcher.register(CommandManager.literal(cmd)
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .suggests(CommandPlayerSuggestionProvider.create())
                            .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                    .executes(CommandPay::target)
                            )
                    )
            );
        }
    }
    public static int target(CommandContext<ServerCommandSource> context) {
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        String player = StringArgumentType.getString(context, "player");
        Double amount = DoubleArgumentType.getDouble(context, "amount");
        PlayerEntity playerEntity = context.getSource().getPlayer();

        if(playerEntity == null) return 1;

        if (amount <= 0) {
            context.getSource().sendFeedback(() -> Text.literal("§c转账金额必须为正数"), false);
            return 1;
        }

        PlayerEntity targetEntity = playerManager.getPlayer(player);

        if (targetEntity == null) {
            context.getSource().sendFeedback(() -> Text.literal("§c目标玩家不存在"), false);
            return 2;
        }

        var ecoManager = ((EcoManagerAccessor) (playerEntity)).getEcoManager();
        var targetEcoManager = ((EcoManagerAccessor) (targetEntity)).getEcoManager();

        if (ecoManager.ecoProfile.balance >= amount) {
            ecoManager.ecoProfile.balance -= amount;
            targetEcoManager.ecoProfile.balance += amount;
            ecoManager.save();
            targetEcoManager.save();
            context.getSource().sendFeedback(() -> Text.literal("§e已向 " + player + " 的账户余额转账 " + amount + "，你现在的余额为 " + ecoManager.ecoProfile.balance), false);
            targetEntity.getCommandSource().sendFeedback(() -> Text.literal("§e你已收到来自 " + playerEntity.getName().getString() + " 的转账 " + amount), false);
        } else {
            context.getSource().sendFeedback(() -> Text.literal("§c你账户余额不足"), false);
        }
        return 0;
    }
}
