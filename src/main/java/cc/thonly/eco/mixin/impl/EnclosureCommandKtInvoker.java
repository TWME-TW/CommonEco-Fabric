package cc.thonly.eco.mixin.impl;

import com.github.zly2006.enclosure.command.BuilderScope;
import com.github.zly2006.enclosure.command.EnclosureCommandKt;
import com.mojang.brigadier.CommandDispatcher;
import kotlin.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnclosureCommandKt.class)
public interface EnclosureCommandKtInvoker {

    @Invoker(value = "register$lambda$23$lambda$16", remap = false)
    public static Unit register$lambda$23$lambda$16(CommandDispatcher $dispatcher, BuilderScope $this$literal) {
        throw new AssertionError();
    }

    @Invoker(value = "register$lambda$23$lambda$19", remap = false)
    public static Unit register$lambda$23$lambda$19(BuilderScope $this$literal) {
        throw new AssertionError();
    }

    @Invoker(value = "register$lambda$23$lambda$21", remap = false)
    public static Unit register$lambda$23$lambda$21(BuilderScope $this$literal) {
        throw new AssertionError();
    }

    @Invoker(value = "register$lambda$23$lambda$22", remap = false)
    public static Unit register$lambda$23$lambda$22(BuilderScope $this$literal) {
        throw new AssertionError();
    }
}
