package cc.thonly.eco.command;

import com.github.zly2006.enclosure.Enclosure;
import com.github.zly2006.enclosure.EnclosureArea;
import com.github.zly2006.enclosure.EnclosureList;
import com.github.zly2006.enclosure.ServerMain;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandPlayerAllEnclosuresProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            MinecraftServer server = context.getSource().getServer();
            ServerMain instance = ServerMain.INSTANCE;
            EnclosureList allEnclosures = instance.getAllEnclosures(context.getSource().getWorld());
            Collection<EnclosureArea> areas = allEnclosures.getAreas();
            for (EnclosureArea area : areas) {
                String name = area.getName();
                if (area.getUuid().toString().equalsIgnoreCase(player.getUuidAsString().toString())) {
                    builder.suggest(name);
                }
            }
        }
        return builder.buildFuture();
    }

    public static CommandPlayerAllEnclosuresProvider create() {
        return new CommandPlayerAllEnclosuresProvider();
    }
}
