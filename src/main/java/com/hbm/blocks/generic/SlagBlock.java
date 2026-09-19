package com.hbm.blocks.generic;

import com.hbm.blockentity.SlagBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.machine.ScrapsItem;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockDynamicSlag.
 *
 * Die Schlackenpfuetze. Was der Schlackenabstich ablaesst, landet hier: eine flache Lache, die
 * nach unten faellt, sich mit der Lache darunter vereinigt und sich zur Seite ausbreitet, wenn
 * genug beisammen ist. Abgebaut gibt sie ihren ganzen Inhalt als Schrottklumpen zurueck.
 *
 * ABWEICHUNG, Hoehe: das Original liest die Blockgrenzen bei jedem Bild aus der Blockentitaet.
 * Auf 1.21 waere das ein dynamischer Umriss samt eigenem Renderer; stattdessen steht die Hoehe
 * in Sechzehnteln im Blockzustand (LEVEL). Das Modell kennt sie damit unmittelbar, der Umriss
 * ist wie bei jedem anderen Block zwischenspeicherbar, und die genaue Menge bleibt trotzdem in
 * der Blockentitaet -- gerundet wird nur, was man sieht.
 *
 * ABWEICHUNG, Farbe: das Original erzeugt beim Laden fuer jedes Material eine eigene Textur und
 * bildet Weiss auf die helle, 0x505050 auf die dunkle Materialfarbe ab. Der Port faerbt die
 * Graustufentextur stattdessen ueber einen Farbhandler mit der hellen Materialfarbe ein --
 * dasselbe Verfahren, das der Port schon fuer den Schrottklumpen benutzt.
 */
public class SlagBlock extends BaseEntityBlock {

    /** Die Hoehe der Lache in Sechzehnteln, minus eins: LEVEL 0 ist ein Sechzehntel hoch. */
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;

    public static final MapCodec<SlagBlock> CODEC = simpleCodec(SlagBlock::new);

    private static final VoxelShape[] SHAPES = new VoxelShape[16];

    static {
        for(int i = 0; i < 16; i++) SHAPES[i] = Block.box(0, 0, 0, 16, i + 1, 16);
    }

    public SlagBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 15));
    }

    @Override public MapCodec<SlagBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SlagBlockEntity(pos, state);
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LEVEL)];
    }

    /** Welche der sechzehn Stufen zu dieser Menge gehoert. Auch ein Rest ist einen Zoll hoch. */
    public static int stufe(int amount) {
        return Mth.clamp(amount * 16 / SlagBlockEntity.MAX_AMOUNT, 1, 16) - 1;
    }

    /**
     * Setzt eine Pfuetze an diese Stelle, mit Inhalt, und plant ihren naechsten Schritt ein.
     * Die Rufer pruefen vorher, dass der Platz frei ist.
     */
    public static void setze(Level level, BlockPos pos, NTMMaterial mat, int amount) {

        if(mat == null || amount <= 0) return;

        // Ohne diese Schranke laesst setBlock den Block unter der Weltuntergrenze fallen, und
        // das anschliessende getBlockEntity griffe ins Leere. Das Original kommt ohne sie aus,
        // weil dort der Boden bei y = 0 liegt und der Abstich nur nach unten sucht.
        if(!level.isInWorldBounds(pos)) return;

        Block slag = NtmBlocks.SLAG.get();
        level.setBlock(pos, slag.defaultBlockState().setValue(LEVEL, stufe(amount)), Block.UPDATE_ALL);

        if(level.getBlockEntity(pos) instanceof SlagBlockEntity tile) {
            tile.mat = mat;
            tile.amount = amount;
            tile.setChanged();
        }

        level.scheduleTick(pos, slag, 1);
    }

    /** Bringt den Blockzustand auf die Menge, die in der Blockentitaet steht. */
    public static void aktualisiere(Level level, BlockPos pos, SlagBlockEntity tile) {

        tile.setChanged();

        BlockState state = level.getBlockState(pos);
        if(!(state.getBlock() instanceof SlagBlock)) return;

        BlockState neu = state.setValue(LEVEL, stufe(tile.amount));

        if(neu != state) level.setBlock(pos, neu, Block.UPDATE_ALL);
        else level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        /* Ohne Blockentitaet ist die Pfuetze nichts als ein Loch im Boden -- weg damit. */
        if(!(level.getBlockEntity(pos) instanceof SlagBlockEntity self) || self.mat == null || self.amount <= 0) {
            level.removeBlock(pos, false);
            return;
        }

        BlockPos below = pos.below();

        /* Nach unten fallen: die ganze Lache zieht um. */
        if(pos.getY() > level.getMinBuildHeight() && level.getBlockState(below).canBeReplaced()) {
            setze(level, below, self.mat, self.amount);
            level.removeBlock(pos, false);
            return;
        }

        /* Sonst in die Lache darunter abgeben, sofern sie dasselbe fuehrt. */
        if(level.getBlockEntity(below) instanceof SlagBlockEntity unten
                && unten.mat == self.mat && unten.amount < SlagBlockEntity.MAX_AMOUNT) {

            int transfer = Math.min(SlagBlockEntity.MAX_AMOUNT - unten.amount, self.amount);
            unten.amount += transfer;
            self.amount -= transfer;

            if(self.amount <= 0) level.removeBlock(pos, false);
            else aktualisiere(level, pos, self);

            aktualisiere(level, below, unten);
            level.scheduleTick(below, this, 1);
            return;
        }

        /* Und zuletzt zur Seite, aber erst ab einem Fuenftel Fuellung. */
        List<Direction> frei = new ArrayList<>(4);

        for(Direction dir : Direction.Plane.HORIZONTAL) {
            if(level.getBlockState(pos.relative(dir)).canBeReplaced()) frei.add(dir);
        }

        if(self.amount >= SlagBlockEntity.MAX_AMOUNT / 5 && !frei.isEmpty()) {

            int toSpread = Math.max(self.amount / (frei.size() * 2), 1);

            for(Direction dir : frei) {
                setze(level, pos.relative(dir), self.mat, toSpread);
                self.amount -= toSpread;
            }

            // Bleibt nichts uebrig, verschwindet die Lache sofort. Das Original laesst sie mit
            // Menge null liegen und plant hier auch keinen weiteren Schritt ein -- sie bliebe
            // als unsichtbarer, aber begehbarer Rest stehen, bis zufaellig jemand sie anstoesst.
            if(self.amount <= 0) level.removeBlock(pos, false);
            else aktualisiere(level, pos, self);
        }
    }

    /*
     * Die Pfuetze traegt noLootTable und gibt ihren Inhalt hier selbst heraus: einen
     * Schrottklumpen ueber die ganze Menge. Das Original macht dasselbe in getDrops, nur dass
     * es die Blockentitaet noch aus der Welt lesen kann -- auf 1.21 steht sie in den
     * Beuteparametern, weil der Block zu diesem Zeitpunkt schon fort sein darf.
     */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {

        List<ItemStack> drops = new ArrayList<>();

        LootParams params = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);

        if(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SlagBlockEntity tile
                && tile.mat != null && tile.amount > 0) {
            drops.add(ScrapsItem.create(new MaterialStack(tile.mat, tile.amount)));
        }

        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {

        if(level.getBlockEntity(pos) instanceof SlagBlockEntity tile && tile.mat != null) {
            return ScrapsItem.create(new MaterialStack(tile.mat, tile.amount));
        }

        return ItemStack.EMPTY;
    }
}
