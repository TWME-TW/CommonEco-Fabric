package cc.thonly.eco.mixin;

import com.github.zly2006.enclosure.UpdateChecker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(UpdateChecker.class)
public class UpdateCheckerMixin {
    /**
     * @author
     * @reason
     */
    @Overwrite
    public final void notifyUpdate(ServerPlayerEntity serverPlayeri) {
    }
}
