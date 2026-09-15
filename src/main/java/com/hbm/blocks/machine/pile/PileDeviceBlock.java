package com.hbm.blocks.machine.pile;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.pile.PileControlBlockEntity;
import com.hbm.blockentity.machine.pile.PileCoreBlockEntity;
import com.hbm.blockentity.machine.pile.PileLoaderBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.items.machine.PileRodItem;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.pile.BlockPileDevice.
 *
 * Gemeinsame Grundlage der drei Geraete des Chicago Pile: Nachlader, Luefter und
 * Steuerstabantrieb.
 *
 * - Der Nachlader haengt seitlich an einem Brennstoffkanal. Ein Stab in der Hand wird eingelegt,
 *   ein Rechtsklick mit leerer Hand schiebt ihn hinein; ein Redstonesignal tut dasselbe.
 * - Der Luefter haengt seitlich an einem Lueftungskanal und pumpt Pressluft hinein.
 * - Der Steuerstabantrieb steht OBEN auf einem Steuerkanal und faehrt den Stab ein oder aus,
 *   je nach Redstonesignal.
 *
 * ABWEICHUNG: das Original ist ein Block mit drei Untertypen im Metadatenwert. Im Port sind es
 * drei eigene Bloecke -- so wie der Port es mit allen Metadaten-Bloecken haelt. Die Richtung
 * steht in der Zustands-Eigenschaft FACING.
 */
public abstract class PileDeviceBlock extends BaseEntityBlock implements IToolable, ILookOverlay {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    /** Ob das Geraet oben auf der Anlage steht (Steuerstabantrieb) oder seitlich daran. */
    protected final boolean onTop;
    /** Ob sich Staebe einlegen lassen (nur der Nachlader). */
    protected final boolean takesRods;

    protected PileDeviceBlock(Properties properties, boolean onTop, boolean takesRods) {
        super(properties);
        this.onTop = onTop;
        this.takesRods = takesRods;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {

        /*
         * Nachlader und Luefter schauen aus der Wand heraus, an die sie gesetzt wurden; der
         * Steuerstabantrieb steht obendrauf und richtet sich nach dem Spieler.
         */
        if(this.onTop || context.getClickedFace().getAxis() == Direction.Axis.Y) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> beType) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    /* Das Original zeichnet die Geraete allein ueber den Renderer der Blockentitaet. */
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    /** Rechtsklick mit leerer Hand auf den Nachlader schiebt den eingelegten Stab hinein. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(!this.takesRods || player.isShiftKeyDown()) return InteractionResult.PASS;
        if(level.isClientSide) return InteractionResult.SUCCESS;

        if(!(level.getBlockEntity(pos) instanceof PileLoaderBlockEntity loader)) return InteractionResult.PASS;
        if(loader.progress > 0D || loader.loading) return InteractionResult.CONSUME;

        loader.loading = true;
        return InteractionResult.CONSUME;
    }

    /** Ein Stab in der Hand wird eingelegt statt eingeschoben. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(!this.takesRods || player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!(stack.getItem() instanceof PileRodItem)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(level.isClientSide) return ItemInteractionResult.SUCCESS;

        if(!(level.getBlockEntity(pos) instanceof PileLoaderBlockEntity loader)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(loader.progress > 0D || loader.loading || !loader.getItem(0).isEmpty()) return ItemInteractionResult.CONSUME;

        loader.setItem(0, stack.copyWithCount(1));
        if(!player.getAbilities().instabuild) stack.shrink(1);

        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                NtmSoundEvents.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1F, 1F);

        return ItemInteractionResult.CONSUME;
    }

    /**
     * Die Handbohrmaschine wirkt durch das Geraet hindurch auf den Kanal dahinter -- so laesst
     * sich ein Kanal auch dann noch schliessen, wenn schon ein Geraet davorsteht.
     */
    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        BlockPos pilePos;
        Direction side;

        if(this.onTop) {
            pilePos = pos.below();
            side = Direction.UP;
        } else {
            Direction facing = level.getBlockState(pos).getValue(FACING);
            pilePos = pos.relative(facing.getOpposite());
            side = facing;
        }

        if(level.getBlockState(pilePos).getBlock() instanceof PileBlock pile) {
            return pile.onScrew(level, player, pilePos, side, tool);
        }

        return false;
    }

    /** Was beim Hinsehen ueber dem Geraet steht. */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        List<Component> text = new ArrayList<>();
        BlockEntity be = level.getBlockEntity(pos);

        if(be instanceof PileLoaderBlockEntity loader) {

            text.add(Component.literal("Temp: " + Math.round(loader.channelTemp) + " / " + PileCoreBlockEntity.MAX_HEAT + " C"));

            if(!loader.getItem(0).isEmpty()) text.add(Component.literal("Loading: ").append(loader.getItem(0).getHoverName()));

            if(!loader.channelStack.isEmpty()) {
                text.add(Component.literal("Last rod: ").append(loader.channelStack.getHoverName()));
                if(loader.channelDepletion > 0) text.add(Component.literal("Depletion: " + Math.round(loader.channelDepletion) + "%"));
            }
        }

        if(be instanceof PileControlBlockEntity control) {
            text.add(Component.literal("Extraction level: " + (int) (control.extraction * 100) + "%"));
        }

        if(!text.isEmpty()) ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
    }
}
