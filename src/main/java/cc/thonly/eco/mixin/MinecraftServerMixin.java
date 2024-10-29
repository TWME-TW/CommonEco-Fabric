package cc.thonly.eco.mixin;

import cc.thonly.eco.CommonEconomy;
import cc.thonly.eco.EcoConfig;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(at = @At("HEAD"), method = "loadWorld")
	private void init(CallbackInfo info) {
		CommonEconomy.server = ((MinecraftServer)(Object) this);
		EcoConfig.load();
	}
}