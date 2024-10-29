package cc.thonly.eco.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class CommandPlayerSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        if(context.getSource().getPlayer()!=null) {
            MinecraftServer server = context.getSource().getServer();
            for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                builder.suggest(player.getGameProfile().getName().toString());
            }
        }
        return builder.buildFuture();
    }

    public static CommandPlayerSuggestionProvider create() {
        return new CommandPlayerSuggestionProvider();
    }
}
