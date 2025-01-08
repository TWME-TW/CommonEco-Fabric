package cc.thonly.eco.command;

import cc.thonly.eco.EcoConfig;
import cc.thonly.eco.api.EcoAPI;
import cc.thonly.eco.api.EcoManager;
import com.github.zly2006.enclosure.EnclosureArea;
import com.github.zly2006.enclosure.EnclosureList;
import com.github.zly2006.enclosure.ServerMain;
import com.github.zly2006.enclosure.command.BuilderScope;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kotlin.Unit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ExpandSubCommand {
    public static void register(@NotNull BuilderScope builderScope) {
        registerExpandSubcommand(builderScope);
    }

    private static Object registerExpandSubcommand(Object object) {
        BuilderScope builderScope = (BuilderScope) object;
        LiteralArgumentBuilder builder = ((LiteralArgumentBuilder) builderScope.getParent());
        builder.then(CommandManager.literal("expand")
                .then(CommandManager.argument("name", StringArgumentType.string())
//                        .suggests(CommandPlayerAllEnclosuresProvider.create())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                .executes((context -> {
                                    PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
                                    PlayerEntity player = context.getSource().getPlayer();
                                    String name = StringArgumentType.getString(context, "name");
                                    Integer block_amount = IntegerArgumentType.getInteger(context, "amount");
                                    if (name == null || player == null || block_amount <= 0) return 1;

                                    EcoManager ecoManager = EcoAPI.getEcoManager(player);
                                    Double price = ecoManager.ecoProfile.balance;
                                    Double price_ratio = (Double) EcoConfig.getConfig().getOrDefault("eco_block_ratio", 1.0);
                                    double playerYaw = player.getYaw();
                                    double playerPitch = player.getPitch();
                                    String key = PlayerVelocity.getKey(playerYaw, playerPitch);
                                    int value = PlayerVelocity.getMathPosition(playerYaw, playerPitch);

                                    EnclosureList allEnclosures = ServerMain.INSTANCE.getAllEnclosures(context.getSource().getWorld());
                                    if (!allEnclosures.getNames().contains(name)) return 1;

                                    for (EnclosureArea area : allEnclosures.getAreas()) {
                                        if (area.getName().equalsIgnoreCase(name)) {
                                            Integer old_minX = area.getMinX();
                                            Integer old_minY = area.getMinY();
                                            Integer old_minZ = area.getMinZ();
                                            Integer old_maxX = area.getMaxZ();
                                            Integer old_maxY = area.getMaxZ();
                                            Integer old_maxZ = area.getMaxZ();

                                            Integer new_minX = area.getMinX();
                                            Integer new_minY = area.getMinY();
                                            Integer new_minZ = area.getMinZ();
                                            Integer new_maxX = area.getMaxZ();
                                            Integer new_maxY = area.getMaxZ();
                                            Integer new_maxZ = area.getMaxZ();

                                            switch (value) {
                                                case 1: // 北
                                                    new_maxZ = old_maxZ + block_amount; // 向北拓展
                                                    break;
                                                case 2: // 南
                                                    new_minZ = old_minZ - block_amount; // 向南拓展
                                                    break;
                                                case 3: // 东
                                                    new_maxX = old_maxX + block_amount; // 向东拓展
                                                    break;
                                                case 4: // 西
                                                    new_minX = old_minX - block_amount; // 向西拓展
                                                    break;
                                                case 5: // 上
                                                    new_maxY = old_maxY + block_amount; // 向上拓展
                                                    break;
                                                case 6: // 下
                                                    new_minY = old_minY - block_amount; // 向下拓展
                                                    break;
                                                default:
                                                    break;
                                            }

                                            int volume = (old_maxX - old_minX) * (old_maxY - old_minY) * (old_maxZ - old_minZ);
                                            if(price >= volume * price_ratio|| player.hasPermissionLevel(2)) {
                                                area.setMinX(new_minX);
                                                area.setMinY(new_minY);
                                                area.setMinZ(new_minZ);
                                                area.setMaxX(new_maxX);
                                                area.setMaxY(new_maxY);
                                                area.setMaxZ(new_maxZ);

                                                if(player.hasPermissionLevel(2)) ecoManager.ecoProfile.balance -= volume *  price_ratio;

                                                context.getSource().sendFeedback(() -> Text.translatable("message.enclosure.expand.success", volume * price_ratio), false);
                                            } else {
                                                context.getSource().sendFeedback(() -> Text.translatable("message.enclosure.expand.fail.amount", volume * price_ratio), false);
                                            }

                                            break;
                                        }
                                    }

                                    return 0;
                                }))
                        )
                )
        );
        return Unit.INSTANCE;
    }
}
