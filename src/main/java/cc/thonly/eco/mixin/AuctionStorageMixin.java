package cc.thonly.eco.mixin;

import cc.thonly.eco.api.EcoAPI;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import systems.brn.servershop.ServerShop;
import systems.brn.servershop.lib.Util;
import systems.brn.servershop.lib.records.AuctionRecord;
import systems.brn.servershop.lib.storages.AuctionStorage;

import java.util.ArrayList;

@Pseudo
@Mixin(value = AuctionStorage.class, remap = false)
public abstract class AuctionStorageMixin {
    @Shadow @Final public ArrayList<AuctionRecord> auctions;

    @Shadow public abstract boolean save();

    @Shadow @Final public MinecraftServer server;

    @Inject(method = "addAuction", at = @At("HEAD"), cancellable = true)
    public void addAuction(ServerPlayerEntity seller, int price, ItemStack itemStack, boolean fromCursorStack, CallbackInfo ci) {
        PlayerInventory playerInventory = seller.getInventory();
        if (itemStack.isEmpty()) {
            seller.sendMessage(Text.translatable("message.servershop.auction.empty"), true);
        } else {
            if (price > 0) {
                int remaining;
                if (fromCursorStack) {
                    remaining = 0;
                } else {
                    remaining = Util.removeFromInventory(playerInventory, itemStack.copy(), itemStack.getCount());
                }

                int soldCount = itemStack.getCount() - remaining;
                ItemStack sellingStack = itemStack.copy();
                sellingStack.setCount(soldCount);
                if (soldCount == 0) {
                    seller.sendMessage(Text.translatable("message.servershop.sell.not_enough"), true);
                } else {
                    seller.sendMessage(Text.translatable("message.servershop.sell.auction", new Object[]{soldCount, itemStack.getName(), price}), true);
                    if (fromCursorStack) {
                        itemStack.setCount(0);
                    }

                    this.auctions.add(new AuctionRecord(price, sellingStack, seller.getUuid()));
                    this.save();
                }
            } else {
                seller.sendMessage(Text.translatable("message.servershop.sell.bad_price", new Object[]{price}), true);
            }

        }
        ci.cancel();
    }
    @Inject(method = "buyAuction", at = @At("HEAD"), cancellable = true)
    public void buyAuction(ServerPlayerEntity buyer, AuctionRecord auction, CallbackInfo ci) {
        PlayerInventory playerInventory = buyer.getInventory();
        ItemStack itemStack = Util.removePrices(auction.stack());
        int buyPrice = auction.buyPrice() * itemStack.getCount();
        long playerBalance = ServerShop.balanceStorage.getBalance(buyer);
        if (buyPrice > 0 && this.auctions.contains(auction)) {
            if (playerBalance < (long)buyPrice && !buyer.getUuid().equals(auction.sellerUUID())) {
                buyer.sendMessage(Text.translatable("message.servershop.buy.not_enough", new Object[]{buyPrice, playerBalance, (long)buyPrice - playerBalance}), true);
            } else {
                ItemStack remaining = Util.insertStackIntoInventory(playerInventory, itemStack.copy());
                int boughtCount = itemStack.getCount() - remaining.getCount();
                if (boughtCount > 0) {
                    int toDeduce = auction.buyPrice() * boughtCount;
                    if (!buyer.getUuid().equals(auction.sellerUUID())) {
                        //ServerShop.balanceStorage.removeBalance(buyer, (long)toDeduce);
                        //ServerShop.balanceStorage.addBalance(auction.sellerUUID(), (long)toDeduce);
                        EcoAPI.removeAmount(buyer,toDeduce);
                        EcoAPI.getAllProfilesFromUUID().get(auction.sellerUUID().toString()).balance += toDeduce;
                        playerBalance = ServerShop.balanceStorage.getBalance(buyer);
                        buyer.sendMessage(Text.translatable("message.servershop.buy.success", new Object[]{boughtCount, itemStack.getName(), toDeduce, playerBalance}), true);
                        ServerPlayerEntity seller = this.server.getPlayerManager().getPlayer(auction.sellerUUID());
                        if (seller != null && !seller.isDisconnected()) {
                            long sellerBalance = ServerShop.balanceStorage.getBalance(seller);
                            seller.sendMessage(Text.translatable("message.servershop.buy.other", new Object[]{buyer.getName(), boughtCount, itemStack.getName(), toDeduce, sellerBalance}), true);
                        }
                    } else {
                        buyer.sendMessage(Text.translatable("message.servershop.buy.own", new Object[]{boughtCount, itemStack.getName(), toDeduce, playerBalance}), true);
                    }

                    this.auctions.remove(auction);
                    if (!remaining.isEmpty()) {
                        this.auctions.add(new AuctionRecord(buyPrice - toDeduce, remaining, auction.sellerUUID()));
                    }

                    this.save();
                } else {
                    buyer.sendMessage(Text.translatable("message.servershop.buy.inventory"), true);
                }
            }
        } else {
            buyer.sendMessage(Text.translatable("message.servershop.buy.not_available", new Object[]{itemStack.getName()}), true);
        }
        ci.cancel();
    }
}
