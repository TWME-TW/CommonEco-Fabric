package cc.thonly.eco.command;

import cc.thonly.eco.api.CurrencyRegistry;
import cc.thonly.eco.api.EcoAPI;
import cc.thonly.eco.api.EcoItem;
import cc.thonly.eco.api.EcoManagerAccessor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class CommandEco {
    public static void register(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        String[] l = {"eco"};
        for (String cmd : l) {
            serverCommandSourceCommandDispatcher.register(CommandManager.literal(cmd)
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .executes(CommandEco::handle)
                            .then(CommandManager.literal("set")
                                    .requires(source -> source.hasPermissionLevel(2))
                                    .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                            .executes(CommandEco::handleSetAmount))
                            )
                            .then(CommandManager.literal("add")
                                    .requires(source -> source.hasPermissionLevel(2))
                                    .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                            .executes(CommandEco::handleAddAmount))
                            )
                            .then(CommandManager.literal("remove")
                                    .requires(source -> source.hasPermissionLevel(2))
                                    .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                            .executes(CommandEco::handleRemoveAmount))
                            )
                    )
                    .then(CommandManager.literal("reload")
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(CommandEco::reload)
                    )
                    .then(CommandManager.literal("store")
                            .then(CommandManager.argument("item_amount", IntegerArgumentType.integer())
                                    .executes(CommandEco::store)
                            )
                    )
                    .then(CommandManager.literal("withdraw")
                            .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                    .executes(CommandEco::withdraw)
                            )
                    )
                    .then(CommandManager.literal("register")
                            .requires(source -> source.hasPermissionLevel(2))
                            .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                    .executes(CommandEco::registerItem)
                            )
                    )
                    .then(CommandManager.literal("unregister")
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(CommandEco::unregisterItem)
                    )
                    .then(CommandManager.literal("execute")
                            .requires(source -> source.hasPermissionLevel(2))
                            .then(CommandManager.argument("condition", DoubleArgumentType.doubleArg())
                                    .then(CommandManager.argument("command", StringArgumentType.string())
                                            .then(CommandManager.argument("deduct", BoolArgumentType.bool())
                                                    .executes((context) -> {
                                                        boolean deduct = BoolArgumentType.getBool(context, "deduct");
                                                        return CommandEco.execute(context, deduct);
                                                    })
                                            )
                                            .executes((context) -> {
                                                return CommandEco.execute(context, true);
                                            })
                                    )
                            )
                    )
                    .then(CommandManager.literal("sign")
                            .requires(source -> source.hasPermissionLevel(2))
                            .then(CommandManager.argument("condition", DoubleArgumentType.doubleArg())
                                    .then(CommandManager.argument("command", StringArgumentType.string())
                                            .then(CommandManager.argument("deduct", BoolArgumentType.bool())
                                                    .executes((context) -> {
                                                        boolean deduct = BoolArgumentType.getBool(context, "deduct");
                                                        return CommandEco.getSign(context, deduct);
                                                    })
                                            )
                                            .executes((context) -> CommandEco.getSign(context, true))
                                    )
                            )
                    )
            );
        }
    }
    public static int registerItem(CommandContext<ServerCommandSource> context) {
        if(!context.getSource().isExecutedByPlayer()) return 1;
        PlayerEntity player = context.getSource().getPlayer();
        Item handItem = player.getMainHandStack().getItem();
        double amount = DoubleArgumentType.getDouble(context,"amount");
        if(amount == 0) return 1;
        CurrencyRegistry.register(handItem, amount);
        context.getSource().sendFeedback(() -> Text.translatable("message.currency.registered"), false);
        return 0;
    }
    public static int unregisterItem(CommandContext<ServerCommandSource> context) {
        if(!context.getSource().isExecutedByPlayer()) return 1;
        PlayerEntity player = context.getSource().getPlayer();
        Item handItem = player.getMainHandStack().getItem();
        CurrencyRegistry.unregister(handItem);
        context.getSource().sendFeedback(() -> Text.translatable("message.currency.unregistered"), false);
        return 0;
    }
    public static int getSign(CommandContext<ServerCommandSource> context, boolean deduct) {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getServer();
        PlayerEntity player = source.getPlayer();
        double condition = DoubleArgumentType.getDouble(context, "condition");
        String commandArg = StringArgumentType.getString(context, "command");

        if (player != null) {
            commandArg = commandArg.replaceAll("\"", "\\\"");

            String ecoCmd = "eco execute " + condition + " \\\\\"" + commandArg + "\\\\\" " + deduct;

            String cmd = "give " + player.getName().getString() + " oak_sign[block_entity_data={id:oak_sign,front_text:{messages:['{\"text\":\"say hello\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + ecoCmd + "\"}}','[\" \"]','[\" \"]','[\" \"]']}}]";

            System.out.println(cmd);

            try {
                server.getCommandManager().executeWithPrefix(source, cmd);
            } catch (Exception e) {
                source.sendMessage(Text.translatable("command.execution.failed"));
                return 1;
            }
        }

        return 0;
    }


    public static int execute(CommandContext<ServerCommandSource> context, boolean deduct) {
        double condition = DoubleArgumentType.getDouble(context, "condition");
        String commandArg = StringArgumentType.getString(context, "command");
        ArrayList<String> CommandArray = new ArrayList<>();
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getServer();
        PlayerEntity player = source.getPlayer();

        System.out.println(context.getSource().getPlayer().getName().getString());

        if (player != null && commandArg != null) {
            commandArg = commandArg.replaceAll("%player%", player.getName().getString());
            var manager = EcoAPI.getEcoManager(player);

            if (deduct && manager.ecoProfile.balance < condition) {
                source.sendMessage(Text.translatable("message.pay.insufficient_balance"));
                return 1;
            }

            String[] commands = commandArg.split("&&");
            for (String command : commands) {
                CommandArray.add(command.trim());
            }

            for (String command : CommandArray) {
                server.getCommandManager().executeWithPrefix(source, command);
            }

            if (deduct) {
                manager.ecoProfile.balance -= condition;
                source.sendMessage(Text.translatable("command.balance.self", manager.ecoProfile.balance));
            }
        }

        return 0;
    }

    public static int store(CommandContext<ServerCommandSource> context) {
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        PlayerEntity playerEntity = playerManager.getPlayer(context.getSource().getName());
        int item_amount = IntegerArgumentType.getInteger(context, "item_amount");

        if (playerEntity != null) {
            ItemStack handStack = playerEntity.getMainHandStack();
            Item itemType = handStack.getItem();
            int amount = handStack.getCount();
            Identifier item_id = Registries.ITEM.getId(itemType);

            if (CurrencyRegistry.getRegistry().containsKey(item_id.toString())) {
                EcoItem ecoItem = CurrencyRegistry.getRegistry().get(item_id.toString());
                if (item_amount > amount) {
                    playerEntity.sendMessage(Text.translatable("message.insufficient.items", item_amount, amount), false);
                    return 0;
                }

                EcoAPI.addAmount(playerEntity, ecoItem.getValue() * item_amount);
                handStack.decrement(item_amount);
                playerEntity.sendMessage(Text.translatable("message.store.success", ecoItem.getValue() * item_amount), false);
            } else {
                playerEntity.sendMessage(Text.translatable("message.currency.not.exists", item_amount), false);
            }
        }
        return 0;
    }

    public static int withdraw(CommandContext<ServerCommandSource> context) {
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        PlayerEntity playerEntity = playerManager.getPlayer(context.getSource().getName());
        double amountToWithdraw = DoubleArgumentType.getDouble(context, "amount");

        if (playerEntity != null) {
            List<Map.Entry<String, EcoItem>> sortedEntries = new ArrayList<>(CurrencyRegistry.getRegistry().entrySet());
            sortedEntries.sort((entry1, entry2) -> Double.compare(entry2.getValue().getValue(), entry1.getValue().getValue()));

            double totalCost = 0;
            StringBuilder message = new StringBuilder(Text.translatable("message.withdraw.success").getString()).append("\n");

            for (Map.Entry<String, EcoItem> entry : sortedEntries) {
                Item item = entry.getValue().getItem();
                double itemPrice = entry.getValue().getValue();

                if (itemPrice > 0) {
                    int quantity = (int) (amountToWithdraw / itemPrice);

                    if (quantity > 0) {
                        double cost = quantity * itemPrice;

                        if (EcoAPI.hasAmount(playerEntity, cost)) {
                            ItemStack itemStack = new ItemStack(item, quantity);
                            playerEntity.giveItemStack(itemStack);
                            EcoAPI.removeAmount(playerEntity, cost);
                            totalCost += cost;
                            amountToWithdraw -= cost;

                            message.append(Text.translatable("message.withdraw.item", quantity, item.getName().getString()).getString()).append("\n");

                            if (amountToWithdraw <= 0) {
                                break;
                            }
                        } else {
                            playerEntity.sendMessage(Text.translatable("message.withdraw.insufficient_balance", quantity, item.getName().getString()), false);
                        }
                    }
                }
            }
            if (totalCost > 0) {
                playerEntity.sendMessage(Text.literal(message.toString()), false);
            } else if (amountToWithdraw > 0) {
                playerEntity.sendMessage(Text.translatable("message.withdraw.not_enough_funds"), false);
            }
        }
        return 0;
    }

    private static int reload(CommandContext<ServerCommandSource> context) {
        EcoAPI.reload();
        context.getSource().sendFeedback(()->Text.translatable("message.reload"),false);
        return 0;
    }
    private static int handle(CommandContext<ServerCommandSource> context) {
        return CommandBalance.target(context);
    }
    private static int handleSetAmount(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        PlayerEntity targetEntity = playerManager.getPlayer(playerName);
        if (targetEntity == null) return 1;

        var ecoManager = ((EcoManagerAccessor) (targetEntity)).getEcoManager();
        ecoManager.ecoProfile.balance = amount;
        ecoManager.save();

        context.getSource().sendFeedback(() -> Text.translatable("message.set_balance", playerName, amount), false);
        return 0;
    }

    private static int handleAddAmount(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        PlayerEntity targetEntity = playerManager.getPlayer(playerName);
        if (targetEntity == null) return 1;

        var ecoManager = ((EcoManagerAccessor) (targetEntity)).getEcoManager();
        ecoManager.ecoProfile.balance += amount;
        ecoManager.save();

        context.getSource().sendFeedback(() -> Text.translatable("message.add_balance", playerName, amount), false);
        return 0;
    }

    private static int handleRemoveAmount(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        PlayerEntity targetEntity = playerManager.getPlayer(playerName);
        if (targetEntity == null) return 1;

        var ecoManager = ((EcoManagerAccessor) (targetEntity)).getEcoManager();
        ecoManager.ecoProfile.balance -= amount;
        ecoManager.save();

        context.getSource().sendFeedback(() -> Text.translatable("message.remove_balance", playerName, amount), false);
        return 0;
    }
    public static String decodeBase64(String base64) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        return new String(decodedBytes);
    }
}
