package cc.thonly.eco.mixin;

import cc.thonly.eco.command.ExpandSubCommand;
import cc.thonly.eco.mixin.impl.EnclosureCommandKtInvoker;
import com.github.zly2006.enclosure.command.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import kotlin.jvm.internal.Intrinsics;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.zly2006.enclosure.command.EnclosureCommandKt.*;

@Pseudo
@Mixin(EnclosureCommandKt.class)
public abstract class EnclosureCommandKtMixin {
    @Redirect(remap = false, method = "register", at = @At(value = "INVOKE", target = "Lcom/github/zly2006/enclosure/command/EnclosureCommandKt;registerConfirmCommand(Lcom/github/zly2006/enclosure/command/BuilderScope;)V"))
    private static void redirectRegister(BuilderScope<?> $this$registerConfirmCommand) {
        registerConfirmCommand($this$registerConfirmCommand);
        ExpandSubCommand.register($this$registerConfirmCommand);
    }

}
