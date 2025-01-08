package cc.thonly.eco.command;

import cc.thonly.eco.api.EcoManagerAccessor;
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
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        String[] l = {"pay"};
        for (String cmd : l) {
            dispatcher.register(CommandManager.literal(cmd)
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
        double amount = DoubleArgumentType.getDouble(context, "amount");
        PlayerEntity playerEntity = context.getSource().getPlayer();

        if (playerEntity == null) return 1;

        if (amount <= 0) {
            context.getSource().sendFeedback(() -> Text.translatable("message.pay.invalid_amount"), false);
            return 1;
        }

        PlayerEntity targetEntity = playerManager.getPlayer(player);

        if (targetEntity == null) {
            context.getSource().sendFeedback(() -> Text.translatable("message.pay.target_not_found"), false);
            return 2;
        }

        var ecoManager = ((EcoManagerAccessor) playerEntity).getEcoManager();
        var targetEcoManager = ((EcoManagerAccessor) targetEntity).getEcoManager();

        if (ecoManager.ecoProfile.balance >= amount) {
            ecoManager.ecoProfile.balance -= amount;
            targetEcoManager.ecoProfile.balance += amount;
            ecoManager.save();
            targetEcoManager.save();

            context.getSource().sendFeedback(() -> Text.translatable("message.pay.success_sender", player, amount, ecoManager.ecoProfile.balance), false);
            targetEntity.getCommandSource().sendFeedback(() -> Text.translatable("message.pay.success_receiver", playerEntity.getName().getString(), amount), false);
        } else {
            context.getSource().sendFeedback(() -> Text.translatable("message.pay.insufficient_balance"), false);
        }

        return 0;
    }
}
