package com.hbm.entity.grenade;

import com.hbm.entity.item.BuoyantItemEntity;
import com.hbm.items.NtmItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.ItemGrenadeFishing.
 *
 * Fischen mit Sprengstoff. Der Knall ist SCHWAECHER als der der gewoehnlichen Dynamitstange
 * und richtet keinen Blockschaden an -- Staerke drei, einen Vierteblock ueber der Stelle,
 * ohne Feuer. Danach werden fuenfzehn Punkte in einem Wuerfel von fuenfzehn Bloecken Kante
 * gezogen; wo Wasser steht, treibt ein Fisch auf.
 *
 * DIE FISCHE TREIBEN, SIE SINKEN NICHT: sie kommen als BuoyantItemEntity in die Welt, mit
 * einem Aufwaertsschwung von eins.
 *
 * NACHGEMESSEN, UND DARUM EINE ABWEICHUNG IM WERKZEUG: das Original zieht seine Beute mit
 * FishingHooks.getRandomFishable(rand, chance, 0, 100). Die HUNDERT ist die Anzahl der
 * Ticks, die eine Angel gebraucht haette, und sie druckt Schrott und Schaetze nach dessen
 * eigener Rechnung auf nahezu null -- es kommt praktisch nur Fisch heraus. Auf 1.21 gibt es
 * diese Rechnung nicht mehr; die Beutetabelle gameplay/fishing wuerfelt Schrott und Schaetze
 * mit. Der Port nimmt darum gleich die Untertabelle gameplay/fishing/fish, die genau das
 * enthaelt, was das Original praktisch ausgibt.
 */
public class DynamiteFishing extends Dynamite {

    /** Wie oft gewuerfelt wird, und wie weit. Beide Zahlen aus dem Original. */
    private static final int VERSUCHE = 15;
    private static final int STREUUNG = 15;

    public DynamiteFishing(EntityType<? extends DynamiteFishing> type, Level level) {
        super(type, level);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(NtmItems.STICK_DYNAMITE_FISHING.get());
    }

    @Override
    protected void zuende() {

        this.level().explode(null, this.getX(), this.getY() + 0.25D, this.getZ(), 3F,
                Level.ExplosionInteraction.NONE);

        if(this.level() instanceof ServerLevel level) {

            for(int i = 0; i < VERSUCHE; i++) {

                BlockPos pos = BlockPos.containing(this.getX(), this.getY(), this.getZ()).offset(
                        this.random.nextInt(STREUUNG) - 7,
                        this.random.nextInt(STREUUNG) - 7,
                        this.random.nextInt(STREUUNG) - 7);

                if(!level.getFluidState(pos).is(FluidTags.WATER)) continue;

                for(ItemStack fisch : this.fangen(level, pos)) {

                    BuoyantItemEntity beute = new BuoyantItemEntity(level,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, fisch);
                    beute.setDeltaMovement(0, 1, 0);

                    level.addFreshEntity(beute);
                }
            }
        }

        this.discard();
    }

    /** Ein Wurf aus der Fischtabelle, so als haette dort jemand geangelt. */
    private List<ItemStack> fangen(ServerLevel level, BlockPos pos) {

        LootTable tabelle = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING_FISH);

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                .create(LootContextParamSets.FISHING);

        return tabelle.getRandomItems(params);
    }
}
