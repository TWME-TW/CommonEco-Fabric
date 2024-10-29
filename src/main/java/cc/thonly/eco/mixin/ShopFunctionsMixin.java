package cc.thonly.eco.mixin;

import cc.thonly.eco.api.EcoAPI;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import systems.brn.servershop.ServerShop;
import systems.brn.servershop.lib.ShopFunctions;
import systems.brn.servershop.lib.Util;
import systems.brn.servershop.lib.records.ItemPriceRecord;

@Pseudo
@Mixin(ShopFunctions.class)
public class ShopFunctionsMixin {
    @Inject(method = "buy", at = @At("HEAD"), cancellable = true)
    private static void buy(ItemStack itemStackIn, ServerPlayerEntity player, boolean overlay, CallbackInfo ci) {
        ItemStack itemStack = itemStackIn.copy();
        PlayerInventory playerInventory = player.getInventory();
        ItemPriceRecord price = ServerShop.priceStorage.getPrices(itemStack);
        //long playerBalance = ServerShop.balanceStorage.getBalance(player);
        double playerBalance = EcoAPI.getAmount(player);
        if (price.buyPrice() > 0) {
            int count = Math.min(itemStack.getCount(), Util.maxInsertionAmount(playerInventory, itemStack.copy()));
            itemStack.setCount(count);
            int buyPrice = price.buyPrice() * itemStack.getCount();
            if (playerBalance >= (long)buyPrice) {
                ItemStack remaining = Util.insertStackIntoInventory(playerInventory, itemStack.copy());
                int boughtCount = itemStack.getCount() - remaining.getCount();
                if (boughtCount > 0) {
                    int toDeduce = price.buyPrice() * boughtCount;
                    EcoAPI.removeAmount(player, toDeduce);
                    //ServerShop.balanceStorage.removeBalance(player, (long)toDeduce);
                    //playerBalance = ServerShop.balanceStorage.getBalance(player);
                    playerBalance = EcoAPI.getAmount(player);
                    player.sendMessage(Text.translatable("message.servershop.buy.success", new Object[]{boughtCount, itemStack.getName(), toDeduce, playerBalance}), overlay);
                } else {
                    player.sendMessage(Text.translatable("message.servershop.buy.inventory"), overlay);
                }
            } else {
                player.sendMessage(Text.translatable("message.servershop.buy.not_enough", new Object[]{buyPrice, playerBalance, (long)buyPrice - playerBalance}), overlay);
            }
        } else {
            player.sendMessage(Text.translatable("message.servershop.buy.not_available", new Object[]{itemStack.getName()}), overlay);
        }

        ci.cancel();
    }
    @Inject(method = "sell", at = @At("HEAD"), cancellable = true)
    private static void sell(ItemStack itemStackIn, ServerPlayerEntity player, boolean isCursorStack, boolean overlay, CallbackInfo ci) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack itemStack = itemStackIn.copy();
        ItemPriceRecord price = ServerShop.priceStorage.getPrices(itemStack);
        int sellPrice = price.sellPrice();
        if (sellPrice > 0) {
            int remaining;
            if (!isCursorStack) {
                remaining = Util.removeFromInventory(playerInventory, itemStack.copy(), itemStack.getCount());
            } else {
                remaining = 0;
                itemStackIn.setCount(0);
            }

            int toAdd = sellPrice * (itemStack.getCount() - remaining);
            int soldCount = itemStack.getCount() - remaining;
            //ServerShop.balanceStorage.addBalance(player, (long)toAdd);
            EcoAPI.addAmount(player,toAdd);
            if (soldCount == 0) {
                player.sendMessage(Text.translatable("message.servershop.sell.not_enough"), overlay);
            } else {
                //long playerBalance = ServerShop.balanceStorage.getBalance(player);
                double playerBalance = EcoAPI.getAmount(player);
                player.sendMessage(Text.translatable("message.servershop.sell.success", new Object[]{soldCount, itemStack.getName(), toAdd, playerBalance}), overlay);
            }
        } else {
            player.sendMessage(Text.translatable("message.servershop.sell.not_available", new Object[]{itemStack.getName()}), overlay);
        }

        ci.cancel();
    }
}
