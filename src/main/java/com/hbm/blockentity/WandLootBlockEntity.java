package com.hbm.blockentity;

import com.hbm.itempool.ItemPool;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.LootGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Portiert aus 1.7.10: die eingebettete Klasse BlockWandLoot.TileEntityWandLoot.
 *
 * Sie haelt, was aus dem Beutestab werden soll, und fuehrt den Tausch beim ersten Servertick
 * aus. Danach ist sie weg -- eine Blockentitaet, die sich selbst ueberschreibt.
 *
 * WAS SIE TUT, HAENGT AM ERSATZBLOCK:
 *   * ein BEHAELTER (Truhe, im Original ausserdem der Tresor) wird mit min bis max Zuegen aus
 *     dem benannten Vorrat gefuellt;
 *   * ein BEUTESOCKEL bekommt statt dessen ein Beuterezept aus LootGenerator -- dort steht
 *     nicht nur, was darauf liegt, sondern auch wo.
 *
 * DIE DREHUNG kommt als Winkel, nicht als Richtung. Das Original merkt sich beim Setzen den
 * Blickwinkel des Spielers und setzt den Ersatzblock spaeter ueber einen Scheinspieler mit
 * genau diesem Winkel -- ein Umweg, den es braucht, weil onBlockPlacedBy einen Spieler
 * verlangt. Hier genuegt Direction.fromYRot; die Truhe schaut dem Setzenden entgegen.
 */
public class WandLootBlockEntity extends BlockEntity {

    private String ersatz = "hbmsntm:deco_loot";
    private String pool = "";
    private int min = 0;
    private int max = 0;
    private float rot = 0F;

    public WandLootBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.WAND_LOOT.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WandLootBlockEntity stab) {
        stab.ersetze(level, pos);
    }

    private void ersetze(Level level, BlockPos pos) {

        ResourceLocation name = ResourceLocation.tryParse(this.ersatz);
        Block block = name == null ? null : BuiltInRegistries.BLOCK.get(name);

        if(block == null || (block == Blocks.AIR && !"minecraft:air".equals(this.ersatz))) {
            NuclearTechMod.LOGGER.warn("Beutestab bei {} zeigt auf einen unbekannten Block: {}", pos, this.ersatz);
            level.removeBlock(pos, false);
            return;
        }

        BlockState neu = block.defaultBlockState();
        if(neu.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            neu = neu.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.fromYRot(this.rot).getOpposite());
        }

        level.setBlock(pos, neu, Block.UPDATE_ALL);

        BlockEntity nachfolger = level.getBlockEntity(pos);

        if(nachfolger instanceof Container behaelter) {
            fuelle(behaelter, level.getRandom());
            nachfolger.setChanged();
        } else if(nachfolger instanceof LootDecoBlockEntity) {
            LootGenerator.applyLoot(level, pos, this.pool);
        }
    }

    /**
     * Wie WeightedRandomChestContent.generateChestContents in 1.7.10: so viele Zuege, wie der
     * Stab vorgibt, jeder in ein zufaelliges Fach.
     *
     * EIN UNTERSCHIED: das Original schreibt in ein beliebiges Fach und ueberschreibt dabei,
     * was schon darin liegt -- bei zwoelf Zuegen in siebenundzwanzig Faechern geht so
     * regelmaessig Beute verloren. Hier wird vom gewuerfelten Fach aus das naechste freie
     * gesucht.
     */
    private void fuelle(Container behaelter, RandomSource random) {

        ItemPool vorrat = ItemPool.get(this.pool);
        if(vorrat == null) {
            NuclearTechMod.LOGGER.warn("Beutestab bei {} nennt einen unbekannten Vorrat: {}", this.getBlockPos(), this.pool);
            return;
        }

        int anzahl = this.min;
        if(this.max - this.min > 0) anzahl += random.nextInt(this.max - this.min);

        int groesse = behaelter.getContainerSize();
        if(groesse <= 0) return;

        for(int i = 0; i < anzahl; i++) {
            ItemStack stapel = vorrat.draw(random);
            if(stapel.isEmpty()) continue;

            int start = random.nextInt(groesse);
            for(int j = 0; j < groesse; j++) {
                int fach = (start + j) % groesse;
                if(behaelter.getItem(fach).isEmpty()) {
                    behaelter.setItem(fach, stapel);
                    break;
                }
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if(tag.contains("block")) this.ersatz = tag.getString("block");
        this.pool = tag.getString("pool");
        this.min = tag.getInt("min");
        this.max = tag.getInt("max");
        this.rot = tag.getFloat("rot");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("block", this.ersatz);
        tag.putString("pool", this.pool);
        tag.putInt("min", this.min);
        tag.putInt("max", this.max);
        tag.putFloat("rot", this.rot);
    }
}
