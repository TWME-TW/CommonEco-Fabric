package cc.thonly.eco.mixin;

import cc.thonly.eco.api.obj.ConfigObj;
import com.github.zly2006.enclosure.ServerMain;
import com.github.zly2006.enclosure.command.EnclosureCommandKt;
import com.github.zly2006.enclosure.command.Session;
import com.github.zly2006.enclosure.listeners.SessionListener;
import com.github.zly2006.enclosure.utils.TrT;
import kotlin.jvm.internal.Intrinsics;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(SessionListener.class)
public abstract class SessionListenerMixin implements ServerPlayConnectionEvents.Join, ServerPlayConnectionEvents.Disconnect, AttackBlockCallback, UseBlockCallback, ServerEntityWorldChangeEvents.AfterPlayerChange  {

    /**
     * @author
     * @reason
     */
    @Overwrite
    public @NotNull ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        Intrinsics.checkNotNullParameter(player, "player");
        Intrinsics.checkNotNullParameter(world, "world");
        Intrinsics.checkNotNullParameter(hand, "hand");
        Intrinsics.checkNotNullParameter(pos, "pos");
        Intrinsics.checkNotNullParameter(direction, "direction");
        if (player.getStackInHand(hand).getItem() == ServerMain.INSTANCE.getOperationItem() && player instanceof ServerPlayerEntity) {
            Session var10000 = (Session)ServerMain.INSTANCE.getPlayerSessions().get(player.getUuid());
            Intrinsics.checkNotNull(var10000);
            if (!Intrinsics.areEqual(var10000.getPos1(), pos)) {
                Session session = (Session)ServerMain.INSTANCE.getPlayerSessions().get(player.getUuid());
                Intrinsics.checkNotNull(session);
                session.syncDimension((ServerPlayerEntity)player);
                session.setPos1(pos);
                EnclosureCommandKt.enable(session);
                session.trySync();
                ServerPlayerEntity var8 = (ServerPlayerEntity)player;
                Object[] var7 = new Object[]{pos.toShortString()};
                var8.sendMessage((Text)TrT.of("enclosure.message.set_pos_1", var7));
                if(session.getPos1() != null && session.getPos2() != null) {
                    int size = calculateVolume(session.getPos1(),session.getPos2());
                    var8.sendMessage(Text.literal("§e选区大小: " + (size)));
                    var8.sendMessage(Text.literal("§e预计花费: " + (size * ConfigObj.eco_block_ratio)));
                }
            }

            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }
    /**
     * @author
     * @reason
     */
    @Overwrite
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        Intrinsics.checkNotNullParameter(player, "player");
        Intrinsics.checkNotNullParameter(world, "world");
        Intrinsics.checkNotNullParameter(hand, "hand");
        Intrinsics.checkNotNullParameter(hitResult, "hitResult");
        if (player instanceof ServerPlayerEntity && (((ServerPlayerEntity)player).getMainHandStack().getItem() == ServerMain.INSTANCE.getOperationItem() && hand == Hand.MAIN_HAND || ((ServerPlayerEntity)player).getOffHandStack().getItem() == ServerMain.INSTANCE.getOperationItem() && hand == Hand.OFF_HAND)) {
            Session var10000 = (Session)ServerMain.INSTANCE.getPlayerSessions().get(player.getUuid());
            Intrinsics.checkNotNull(var10000);
            if (!Intrinsics.areEqual(var10000.getPos2(), hitResult.getBlockPos())) {
                Session session = (Session)ServerMain.INSTANCE.getPlayerSessions().get(player.getUuid());
                Intrinsics.checkNotNull(session);
                if (Intrinsics.areEqual(session.getPos1(), BlockPos.ORIGIN) && ServerMain.INSTANCE.getOperationItem() instanceof HoeItem) {
                    Item var6 = world.getBlockState(hitResult.getBlockPos()).getBlock().asItem();
                    if (Intrinsics.areEqual(var6, Items.DIRT) || Intrinsics.areEqual(var6, Items.DIRT_PATH) || Intrinsics.areEqual(var6, Items.ROOTED_DIRT) || Intrinsics.areEqual(var6, Items.COARSE_DIRT) || Intrinsics.areEqual(var6, Items.GRASS_BLOCK)) {
                        return ActionResult.PASS;
                    }
                }

                session.syncDimension((ServerPlayerEntity)player);
                session.setPos2(hitResult.getBlockPos());
                EnclosureCommandKt.enable(session);
                session.trySync();
                ServerPlayerEntity var7 = (ServerPlayerEntity)player;
                Object[] var8 = new Object[]{hitResult.getBlockPos().toShortString()};
                var7.sendMessage((Text) TrT.of("enclosure.message.set_pos_2", var8));
                if(session.getPos1() != null && session.getPos2() != null) {
                    int size = calculateVolume(session.getPos1(),session.getPos2());
                    var7.sendMessage(Text.literal("§e选区大小: " + (size)));
                    var7.sendMessage(Text.literal("§e预计花费: " + (size * ConfigObj.eco_block_ratio)));
                }
            }

            return ActionResult.FAIL;
        } else {
            return ActionResult.PASS;
        }
    }
    @Unique
    private static int calculateVolume(BlockPos pos1, BlockPos pos2) {
        int xLength = Math.abs(pos2.getX() - pos1.getX()) + 1;
        int yLength = Math.abs(pos2.getY() - pos1.getY()) + 1;
        int zLength = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        return xLength * yLength * zLength;
    }
}
