package cc.thonly.eco.mixin;

import cc.thonly.eco.api.EcoManager;
import cc.thonly.eco.impl.EcoManagerAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements EcoManagerAccessor {
    @Unique
    private final PlayerEntity player = (PlayerEntity) (Object) this;
    @Unique
    EcoManager ecoManager = new EcoManager(this.player);
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    public EcoManager getEcoManager() {
        return this.ecoManager;
    }
}
