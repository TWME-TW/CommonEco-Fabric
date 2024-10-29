package cc.thonly.eco.command;

import cc.thonly.eco.api.CurrencyRegistry;
import cc.thonly.eco.api.EcoAPI;
import cc.thonly.eco.api.EcoItem;
import cc.thonly.eco.api.EcoManager;
import cc.thonly.eco.impl.EcoManagerAccessor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.ArrayList;
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
                                    .executes(CommandEco::register)
                            )
                    )
                    .then(CommandManager.literal("unregister")
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(CommandEco::unregister)
                    )
            );
        }
    }
    public static int register(CommandContext<ServerCommandSource> context) {
        if(!context.getSource().isExecutedByPlayer()) return 1;
        PlayerEntity player = context.getSource().getPlayer();
        Item handItem = player.getMainHandStack().getItem();
        double amount = DoubleArgumentType.getDouble(context,"amount");
        if(amount == 0) return 1;
        CurrencyRegistry.register(handItem,amount);
        context.getSource().sendMessage(Text.literal("§6货币注册成功"));
        return 0;
    }
    public static int unregister(CommandContext<ServerCommandSource> context) {
        if(!context.getSource().isExecutedByPlayer()) return 1;
        PlayerEntity player = context.getSource().getPlayer();
        Item handItem = player.getMainHandStack().getItem();
        CurrencyRegistry.unregister(handItem);
        context.getSource().sendMessage(Text.literal("§6货币取消注册成功"));
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
                    playerEntity.sendMessage(Text.literal("§c物品数量不足！填写的数量: " + item_amount + ", 当前数量: " + amount), false);
                    return 0;
                }

                EcoAPI.addAmount(playerEntity, ecoItem.getValue() * item_amount);
                handStack.decrement(item_amount);
                playerEntity.sendMessage(Text.literal("§b成功存入了" + ecoItem.getValue() * item_amount), false);
            } else {
                playerEntity.sendMessage(Text.literal("§c不存在此类型的货币" + item_amount), false);
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

            StringBuilder message = new StringBuilder("成功提取：\n");

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

                            message.append(quantity).append(" 个 ").append(item.getName().getString()).append("\n");

                            if (amountToWithdraw <= 0) {
                                break;
                            }
                        } else {
                            playerEntity.sendMessage(Text.literal("余额不足，无法提取 " + quantity + " 个 " + item.getName().getString() + "。"), false);
                        }
                    }
                }
            }

            if (totalCost > 0) {
                playerEntity.sendMessage(Text.literal(message.toString()), false);
            } else if (amountToWithdraw > 0) {
                playerEntity.sendMessage(Text.literal("余额不足，无法完全提取。"), false);
            }
        }

        return 0;
    }




    private static int reload(CommandContext<ServerCommandSource> context) {
        EcoAPI.reload();
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
        if(targetEntity == null) return 1;
        var ecoManager = ((EcoManagerAccessor) (targetEntity)).getEcoManager();
        ecoManager.ecoProfile.balance = amount;
        ecoManager.save();

        context.getSource().sendFeedback(() -> Text.literal("§b已将 " + playerName + " 的余额设置为 " + amount), false);
        return 0;
    }

    private static int handleAddAmount(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        PlayerEntity targetEntity = playerManager.getPlayer(playerName);
        if(targetEntity == null) return 1;
        var ecoManager = ((EcoManagerAccessor) (targetEntity)).getEcoManager();
        ecoManager.ecoProfile.balance += amount;
        ecoManager.save();

        context.getSource().sendFeedback(() -> Text.literal("§b已向 " + playerName + " 的账户添加 " + amount), false);
        return 0;
    }


    private static int handleRemoveAmount(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        PlayerEntity targetEntity = playerManager.getPlayer(playerName);
        if(targetEntity == null) return 1;
        var ecoManager = ((EcoManagerAccessor) (targetEntity)).getEcoManager();
        ecoManager.ecoProfile.balance -= amount;
        ecoManager.save();

        context.getSource().sendFeedback(() -> Text.literal("§b已从 " + playerName + " 的账户移除 " + amount), false);
        return 0;
    }
}
