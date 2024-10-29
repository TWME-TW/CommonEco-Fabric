package cc.thonly.eco.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import systems.brn.servershop.commands.*;

@Pseudo
@Mixin(CommandRegistry.class)
public class CommandRegistryMixin {
    @Inject(method = "commandRegister", at = @At("HEAD"), cancellable = true)
    private static void commandRegister(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment, CallbackInfo ci) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("shop").then(((LiteralArgumentBuilder)CommandManager.literal("edit").requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).executes(ShopCommands::edit))).then(((LiteralArgumentBuilder)CommandManager.literal("load").requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).executes(ShopCommands::load))).then(((LiteralArgumentBuilder)CommandManager.literal("save").requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).executes(ShopCommands::save))).then(((LiteralArgumentBuilder)CommandManager.literal("set").requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).then(CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).then(CommandManager.argument("buyprice", IntegerArgumentType.integer(-1)).then(CommandManager.argument("sellprice", IntegerArgumentType.integer(-1)).executes(ShopCommands::set)))))).then(((LiteralArgumentBuilder)CommandManager.literal("setHand").requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).then(CommandManager.argument("buyprice", IntegerArgumentType.integer(-1)).then(CommandManager.argument("sellprice", IntegerArgumentType.integer(-1)).executes(ShopCommands::setHand))))).executes(ShopCommands::shop));

        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("auction").then(((LiteralArgumentBuilder)CommandManager.literal("load").requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).executes(AuctionCommands::load))).then(((LiteralArgumentBuilder)CommandManager.literal("save").requires((serverCommandSource) -> serverCommandSource.hasPermissionLevel(2))).executes(AuctionCommands::save))).then(((LiteralArgumentBuilder)CommandManager.literal("create").then(CommandManager.argument("sellprice", IntegerArgumentType.integer(1)).executes(AuctionCommands::createHand))).executes(AuctionCommands::create))).executes(AuctionCommands::browse));

        //dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("pay").then(CommandManager.argument("recipient", EntityArgumentType.player()).then(CommandManager.argument("amount", IntegerArgumentType.integer(1)).executes(PayCommand::run))));

        /*dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("balance").then(((RequiredArgumentBuilder)CommandManager.argument("target", EntityArgumentType.player()).requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).executes(BalanceCommand::getOthers))).then(((LiteralArgumentBuilder)CommandManager.literal("load").requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).executes(BalanceCommand::load))).then(((LiteralArgumentBuilder)CommandManager.literal("save").requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).executes(BalanceCommand::save))).then(((LiteralArgumentBuilder)CommandManager.literal("list").requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).executes(BalanceCommand::list))).then(((LiteralArgumentBuilder)CommandManager.literal("set").requires((serverCommandSource) -> {
            return serverCommandSource.hasPermissionLevel(2);
        })).then(((RequiredArgumentBuilder)CommandManager.argument("balance", LongArgumentType.longArg(0L)).executes(BalanceCommand::selfSet)).then(CommandManager.argument("recipient", EntityArgumentType.player()).executes(BalanceCommand::othersSet))))).executes(BalanceCommand::self));
        */
        dispatcher.register(CommandManager.literal("buy").then(((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).then(CommandManager.argument("count", IntegerArgumentType.integer(1)).executes(StoreCommands::buyCount))).executes(StoreCommands::buyOne)));
        dispatcher.register(CommandManager.literal("sell").then(((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).then(CommandManager.argument("count", IntegerArgumentType.integer(1)).executes(StoreCommands::sellCount))).executes(StoreCommands::sellOne)));
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("price").then(CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(PriceCommand::run))).executes(PriceCommand::runHand));

        ci.cancel();
    }
}
