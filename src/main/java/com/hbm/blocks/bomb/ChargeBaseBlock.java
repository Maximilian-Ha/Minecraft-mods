package com.hbm.blocks.bomb;

import api.hbm.block.IFuckingExplode;
import api.hbm.block.IToolable;
import api.hbm.block.IToolable.ToolType;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.bomb.ChargeBlockEntity;
import com.hbm.entity.item.TNTPrimedBase;
import com.hbm.interfaces.IBomb;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.bomb.BlockChargeBase.
 *
 * Die Haftladung klebt an der Flaeche, auf die man sie setzt, und traegt eine Schaltuhr. Ein
 * Rechtsklick stellt die Zeit weiter, ein Rechtsklick im Schleichen startet sie. Ab da laeuft
 * sie ab und piept jede Sekunde; bei null geht sie hoch.
 *
 * SIE LAESST SICH NICHT ABBAUEN, NUR ENTSCHAERFEN. Wer sie zerschlaegt, zuendet sie -- das
 * macht breakBlock im Original, und hier onRemove. Nur der Entschaerfer nimmt sie sicher ab,
 * und auch der erst, wenn die Uhr nicht laeuft: ein Druck darauf haelt zuerst die Uhr an.
 *
 * DAS FELD sicher IST DIE NOTBREMSE GEGEN DIE ENDLOSSCHLEIFE. explode() setzt den Block auf
 * Luft, und das ruft onRemove -- das wieder explodieren wuerde. Das Original loest das mit
 * einem statischen Schalter, und hier steht derselbe.
 *
 * DIE HALTERUNG WIRD GEPRUEFT, ABER DER VERLUST SPRENGT NICHT. Faellt die Wand weg, an der sie
 * haengt, verschwindet die Ladung ersatzlos. Im Original steht die Zeile, die stattdessen
 * explodieren wuerde, auskommentiert daneben -- uebernommen ist der Zustand, nicht die Absicht.
 */
public abstract class ChargeBaseBlock extends BaseEntityBlock implements IBomb, IToolable, IFuckingExplode {

    /** Waehrend explode() laeuft: onRemove soll dann nicht noch einmal zuenden. */
    public static boolean sicher = false;

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final float F = 0.0625F;

    /** Die Kaesten je Richtung, in der Reihenfolge von Direction: unten, oben, N, S, W, O. */
    private static final VoxelShape[] KAESTEN = {
            Shapes.box(0, 10 * F, 0, 1, 1, 1),
            Shapes.box(0, 0, 0, 1, 6 * F, 1),
            Shapes.box(0, 0, 10 * F, 1, 1, 1),
            Shapes.box(0, 0, 0, 1, 1, 6 * F),
            Shapes.box(10 * F, 0, 0, 1, 1, 1),
            Shapes.box(0, 0, 0, 6 * F, 1, 1) };

    public ChargeBaseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, p, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return KAESTEN[state.getValue(FACING).ordinal()];
    }

    /** Wie im Original: die Ladung hat keinen Koerper, man laeuft durch sie hindurch. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction seite = state.getValue(FACING);
        BlockPos traeger = pos.relative(seite.getOpposite());
        return level.getBlockState(traeger).isFaceSturdy(level, traeger, seite);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if(!level.isClientSide && !this.canSurvive(state, level, pos)) {
            sicher = true;
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            sicher = false;
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean bleibt = state.is(newState.getBlock());
        super.onRemove(state, level, pos, newState, movedByPiston);
        if(!bleibt && !sicher) this.explode(level, pos);
    }

    /* Aus einer fremden Explosion wird ein scharfes TNT mit Zuendzeit null -- so bricht die
     * Kette ab, statt sich rekursiv aufzurufen. */
    @Override
    public void wasExploded(Level level, BlockPos pos, @Nullable Explosion explosion) {
        if(!level.isClientSide) {
            TNTPrimedBase tnt = new TNTPrimedBase(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    explosion != null ? explosion.getIndirectSourceEntity() : null, this);
            tnt.fuse = 0;
            tnt.detonateOnCollision = false;
            level.addFreshEntity(tnt);
        }
    }

    @Override
    public void explodeEntity(Level level, double x, double y, double z, TNTPrimedBase entity) {
        this.explode(level, BlockPos.containing(x, y, z));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(!(level.getBlockEntity(pos) instanceof ChargeBlockEntity ladung)) return InteractionResult.PASS;
        if(ladung.started) return InteractionResult.PASS;

        if(player.isShiftKeyDown()) {

            if(ladung.timer > 0) {
                ladung.started = true;
                level.playSound(null, pos, NtmSoundEvents.FSTBMB_START.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }

        } else {
            ladung.timer = naechsteZeit(ladung.timer);
            level.playSound(null, pos, NtmSoundEvents.TECH_BOOP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        ladung.sync();
        return InteractionResult.CONSUME;
    }

    /**
     * Die Leiter der Schaltzeiten in Ticks: 5 s, 10 s, 15 s, 30 s, 1 min, 3 min, 5 min, aus.
     *
     * Das Original schreibt sie als Kette von sieben if-else-Vergleichen; jeder andere Wert --
     * etwa einer, den eine Falle gesetzt hat -- faellt in den else-Zweig und stellt auf null.
     * Diese Tabelle tut dasselbe.
     */
    private static int naechsteZeit(int jetzt) {
        return switch(jetzt) {
            case 0 -> 100;
            case 100 -> 200;
            case 200 -> 300;
            case 300 -> 600;
            case 600 -> 1200;
            case 1200 -> 3600;
            case 3600 -> 6000;
            default -> 0;
        };
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.DEFUSER) return false;
        if(!(level.getBlockEntity(pos) instanceof ChargeBlockEntity ladung)) return false;

        if(ladung.started) {
            ladung.started = false;
            level.playSound(null, pos, NtmSoundEvents.FSTBMB_START.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            ladung.sync();
        } else {
            sicher = true;
            if(!level.isClientSide) {
                Block.popResource(level, pos, new ItemStack(this));
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
            sicher = false;
        }

        return true;
    }

    /*
     * OHNE ITooltipProvider. Das Original nennt die Schnittstelle in der Klassenzeile, ruft
     * ihre Standardzeilen aber nie auf -- addInformation schreibt drei feste Saetze und sonst
     * nichts. Eine Schnittstelle, die nirgends wirkt, steht hier nicht.
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("Right-click to change timer.").withStyle(ChatFormatting.YELLOW));
        components.add(Component.literal("Sneak-click to arm.").withStyle(ChatFormatting.YELLOW));
        components.add(Component.literal("Can only be disarmed and removed with defuser.").withStyle(ChatFormatting.RED));
    }
}
