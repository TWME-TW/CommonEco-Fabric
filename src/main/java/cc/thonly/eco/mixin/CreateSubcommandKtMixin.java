package cc.thonly.eco.mixin;

import cc.thonly.eco.api.obj.ConfigObj;
import cc.thonly.eco.api.EcoAPI;
import cc.thonly.eco.api.EcoManager;
import com.github.zly2006.enclosure.command.CreateSubcommandKt;
import com.github.zly2006.enclosure.command.EnclosureCommandKt;
import com.github.zly2006.enclosure.command.Session;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(CreateSubcommandKt.class)
public class CreateSubcommandKtMixin {
    @Inject(method = "createEnclosure", at = @At("HEAD"), cancellable = true, remap = false)
    private static void createEnclosure(CommandContext<ServerCommandSource> context, CallbackInfo ci) {
        PlayerEntity player = context.getSource().getPlayer();
        EcoManager ecoManager = EcoAPI.getEcoManager(player);
        if(ecoManager == null) {
            ci.cancel();
        }
        Session session = EnclosureCommandKt.sessionOf((ServerCommandSource)context.getSource());
        BlockPos blockPos1 = session.getPos1();
        BlockPos blockPos2 = session.getPos2();
        int size = calculateVolume(blockPos1,blockPos2);
        if (ecoManager != null && !(player.hasPermissionLevel(2)) && ecoManager.ecoProfile.balance < size * ConfigObj.eco_block_ratio) {
            context.getSource().sendFeedback(() -> Text.literal("§c你的余额不足,创建该领地需要 "+size * ConfigObj.eco_block_ratio), false);
            ci.cancel();
        }
    }
    @Inject(method = "createEnclosure", at = @At(value = "INVOKE", target = "Lcom/github/zly2006/enclosure/EnclosureList;addArea(Lcom/github/zly2006/enclosure/EnclosureArea;)V", shift = At.Shift.AFTER))
    private static void afterAddArea(CommandContext<ServerCommandSource> context, CallbackInfo ci) {
        PlayerEntity player = context.getSource().getPlayer();
        EcoManager ecoManager = EcoAPI.getEcoManager(player);
        if(player.hasPermissionLevel(2)) {
            return;
        }
        Session session = EnclosureCommandKt.sessionOf((ServerCommandSource)context.getSource());
        BlockPos blockPos1 = session.getPos1();
        BlockPos blockPos2 = session.getPos2();
        int size = calculateVolume(blockPos1,blockPos2);
        double currency = size * ConfigObj.eco_block_ratio;
        ecoManager.ecoProfile.balance -= currency;
    }
    @Unique
    private static int calculateVolume(BlockPos pos1, BlockPos pos2) {
        int xLength = Math.abs(pos2.getX() - pos1.getX()) + 1;
        int yLength = Math.abs(pos2.getY() - pos1.getY()) + 1;
        int zLength = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        return xLength * yLength * zLength;
    }
}
