package com.hbm.blocks.generic;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.PedestalBlockEntity;
import com.hbm.extprop.HbmPlayerAttachments;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.PedestalRecipes;
import com.hbm.inventory.recipes.PedestalRecipes.PedestalExtraCondition;
import com.hbm.inventory.recipes.PedestalRecipes.PedestalRecipe;
import com.hbm.particle.helper.ExplosionSmallCreator;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockPedestal.
 *
 * DAS RITUAL. Neun Sockel, einer in der Mitte und acht ringsum im Abstand DREI -- nicht
 * nebeneinander, sondern weit auseinander, so dass das Ganze einen Kreis von sieben mal
 * sieben Bloecken einnimmt. Wer auf jeden das Richtige legt und den mittleren mit Redstone
 * beschickt, bekommt darauf ein Einzelstueck. Die Auflagen der acht aussen werden dabei
 * verbraucht, die des mittleren durch das Erzeugnis ersetzt.
 *
 * DIE ABSTANDSTABELLE ist die des Originals, Schritt fuer Schritt nachgerechnet: die vier
 * geraden Richtungen liegen drei Bloecke entfernt, die vier Ecken je zwei in beide
 * Richtungen. Das ist NICHT dasselbe wie drei in beide Richtungen, und es sieht im Spiel
 * auch anders aus -- die Ecken stehen naeher. So steht es dort, und so bleibt es.
 *
 * DIE ZUSATZBEDINGUNGEN werden hier geprueft, nicht in der Rezeptliste: Vollmond, Neumond,
 * Tageslicht und der Ruf des Spielers haengen an der Welt, nicht am Rezept.
 *
 * NICHT UEBERNOMMEN: die Statistik statLegendary, die das Original jedem Spieler im Umkreis
 * von fuenfzig Bloecken gutschreibt. Der Port hat kein eigenes Statistiksystem -- es gibt
 * nirgends eine ResourceLocation, die man hier hochzaehlen koennte.
 */
public class PedestalBlock extends BaseEntityBlock {

    public static final MapCodec<PedestalBlock> CODEC = simpleCodec(PedestalBlock::new);

    /* Fuss, Saeule und Deckplatte -- genau die drei Kaesten, die RenderPedestal zeichnet. */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(2, 4, 2, 14, 12, 14),
            Block.box(0, 12, 0, 16, 16, 16));

    public PedestalBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<PedestalBlock> codec() { return CODEC; }

    /* Der Sockel selbst kommt aus dem Modell, der Gegenstand darauf aus RenderPedestal. */
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        return createTickerHelper(type, NtmBlockEntityTypes.PEDESTAL.get(), PedestalBlockEntity::serverTick);
    }

    /** Mit etwas in der Hand: ablegen, aber nur auf einen leeren Sockel. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!(level.getBlockEntity(pos) instanceof PedestalBlockEntity sockel)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if(!sockel.item.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;

        /* Der GANZE Stapel wandert auf den Sockel -- die Rezepte fragen nach Stapelgroessen
         * von bis zu vierundsechzig, ein Stueck je Sockel wuerde sie unerfuellbar machen. */
        sockel.item = stack.copy();
        player.setItemInHand(hand, ItemStack.EMPTY);
        sockel.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);

        return ItemInteractionResult.CONSUME;
    }

    /** Mit leerer Hand: mitnehmen, wenn etwas daliegt. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {

        if(player.isShiftKeyDown()) return InteractionResult.PASS;
        if(!(level.getBlockEntity(pos) instanceof PedestalBlockEntity sockel)) return InteractionResult.PASS;
        if(sockel.item.isEmpty()) return InteractionResult.PASS;

        if(level.isClientSide) return InteractionResult.SUCCESS;

        player.setItemInHand(InteractionHand.MAIN_HAND, sockel.item.copy());
        sockel.item = ItemStack.EMPTY;
        sockel.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        if(!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof PedestalBlockEntity sockel
                && !sockel.item.isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, sockel.item);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /* Die acht Nachbarn, in der Lesereihenfolge des Rezepts: links oben nach rechts unten.
     * Die Mitte steht an Platz vier und wird darum hier ausgelassen. */
    private static final int[][] RING = {
            {-2, -2}, {0, -3}, {2, -2},
            {-3,  0}, {0,  0}, {3,  0},
            {-2,  2}, {0,  3}, {2,  2}};

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {

        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);

        if(level.isClientSide || !level.hasNeighborSignal(pos)) return;

        PedestalBlockEntity[] sockel = new PedestalBlockEntity[9];
        for(int i = 0; i < 9; i++) {
            BlockPos ziel = pos.offset(RING[i][0], 0, RING[i][1]);
            sockel[i] = level.getBlockEntity(ziel) instanceof PedestalBlockEntity p ? p : null;
        }
        if(sockel[4] == null) return;

        List<Player> nahe = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(20, 20, 20));

        rezepte: for(PedestalRecipe rezept : PedestalRecipes.recipes) {

            if(!bedingungErfuellt(level, rezept.extra, nahe)) continue;

            for(int i = 0; i < 9; i++) {
                ItemStack liegt = sockel[i] != null ? sockel[i].item : ItemStack.EMPTY;
                AStack verlangt = rezept.input[i];

                if(verlangt == null) {
                    if(!liegt.isEmpty()) continue rezepte;
                    continue;
                }
                if(liegt.isEmpty()) continue rezepte;
                if(!verlangt.matchesRecipe(liegt, true) || verlangt.stacksize != liegt.getCount()) continue rezepte;
            }

            /* Die acht aussen werden geleert, die Mitte bekommt das Erzeugnis. */
            for(int i = 0; i < 9; i++) {
                if(i == 4 || rezept.input[i] == null || sockel[i] == null) continue;
                sockel[i].item = ItemStack.EMPTY;
                sockel[i].setChanged();
                BlockPos ziel = sockel[i].getBlockPos();
                level.sendBlockUpdated(ziel, level.getBlockState(ziel), level.getBlockState(ziel), 3);
            }

            sockel[4].item = rezept.output.copy();
            sockel[4].setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            ExplosionSmallCreator.composeEffect(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 10, 2.5F, 1F);

            return;
        }
    }

    /**
     * Die Himmelswinkel sind die des Originals: 0,35 bis 0,65 ist Nacht, unter 0,15 oder
     * ueber 0,85 ist heller Tag. getTimeOfDay in 1.21 ist dasselbe wie getCelestialAngle.
     */
    private static boolean bedingungErfuellt(Level level, PedestalExtraCondition bedingung, List<Player> nahe) {

        float winkel = level.getTimeOfDay(0F);

        switch(bedingung) {
        case FULL_MOON:
            if(winkel < 0.35F || winkel > 0.65F) return false;
            return level.getMoonPhase() == 0;
        case NEW_MOON:
            if(winkel < 0.35F || winkel > 0.65F) return false;
            return level.getMoonPhase() == 4;
        case SUN:
            return !(winkel > 0.15F && winkel < 0.85F);
        case BAD_KARMA:
            for(Player spieler : nahe) if(HbmPlayerAttachments.getData(spieler).reputation <= -10) return true;
            return false;
        case GOOD_KARMA:
            for(Player spieler : nahe) if(HbmPlayerAttachments.getData(spieler).reputation >= 10) return true;
            return false;
        default:
            return true;
        }
    }

    // ---------------------------------------------------------------------------------
    // DAS EINTRAGSREGISTER. Ein Sockel mit Talisman traegt sich hier ein; das
    // Meteoritensystem liest die Liste und laesst den Einschlag aus oder lenkt ihn ab.
    // Die Eintraege verfallen nach drei Sekunden von selbst -- so merkt das System, wenn
    // der Talisman weggenommen oder der Sockel abgebaut wurde.
    // ---------------------------------------------------------------------------------

    public enum EntryType { CHARM_OF_PROTECTION, METEORITE_CHARM }

    public record Entry(EntryType type, BlockPos pos, long timestamp) { }

    /** Drei Sekunden, wie im Original. */
    public static final int TIMEOUT = 60;

    private static final Map<ResourceKey<Level>, List<Entry>> EINTRAEGE = new HashMap<>();

    public static void pushEntry(Level level, EntryType type, BlockPos pos) {
        EINTRAEGE.computeIfAbsent(level.dimension(), k -> new ArrayList<>())
                .add(new Entry(type, pos.immutable(), level.getGameTime()));
    }

    /**
     * Gibt die noch gueltigen Eintraege einer Dimension und raeumt dabei die abgelaufenen
     * weg. Das Original hat dafuer zwei Methoden; getrennt waere hier nur eine Falle, denn
     * wer die Liste liest, ohne aufzuraeumen, sieht Talismane, die laengst fort sind.
     */
    public static List<Entry> getEntries(Level level) {
        List<Entry> eintraege = EINTRAEGE.get(level.dimension());
        if(eintraege == null) return List.of();
        long jetzt = level.getGameTime();
        eintraege.removeIf(e -> e.timestamp() < jetzt - TIMEOUT);
        return eintraege;
    }
}
