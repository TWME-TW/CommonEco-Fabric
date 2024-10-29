package cc.thonly.eco.mixin;

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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Pseudo
@Mixin(SessionListener.Companion.class)
public abstract class SessionListenerCompanionAccessor implements ServerPlayConnectionEvents.Join, ServerPlayConnectionEvents.Disconnect, AttackBlockCallback, UseBlockCallback, ServerEntityWorldChangeEvents.AfterPlayerChange  {

}
