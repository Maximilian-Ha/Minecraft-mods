package com.hbm.blocks.generic;

import com.hbm.blockentity.IPersistentNBT;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.storage.CrateBaseBlockEntity;
import com.hbm.blockentity.machine.storage.FileCabinetBlockEntity;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.config.NtmConfig;
import com.hbm.util.TagsUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: ModBlocks.filing_cabinet, ein BlockDecoContainer mit
 * TileEntityFileCabinet.
 *
 * DER AKTENSCHRANK IST KEINE KISTE, auch wenn er sich wie eine verhaelt: er ist schmaler als
 * ein Block und steht nur auf drei Vierteln der Tiefe. Die Masse des Originals --
 * setBlockBoundsTo(.1875F, 0F, 0F, .8125F, 1F, .75F) -- stehen unten als Umriss, einmal je
 * Blickrichtung gedreht.
 *
 * SEINE ZWEI SORTEN sind im Original die Metadatenwerte 0 und 1 desselben Blocks (GREEN und
 * STEEL aus DecoCabinetEnum). Im Port sind es zwei Blockanmeldungen mit derselben Klasse --
 * so hat es die Kistenfamilie auch, und so bleibt der Gegenstand eindeutig.
 *
 * DAS SCHLOSS UND DIE SPINNEN kommen von CrateBaseBlockEntity mit; im Original stammt der
 * Aktenschrank ebenfalls von TileEntityCrateBase ab.
 */
public class FileCabinetBlock extends BaseEntityBlock implements IPersistentInfoProvider {

    public static final MapCodec<FileCabinetBlock> CODEC = simpleCodec(FileCabinetBlock::new);

    /** Acht Faecher, wie im Original: TileEntityFileCabinet ruft super(8). */
    private static final int FAECHER = 8;

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /* Der Umriss des Originals, je Blickrichtung. Der Schrank steht an der Wand: volle Hoehe,
     * zehn Sechzehntel breit, zwoelf Sechzehntel tief. */
    private static final VoxelShape NORD = Block.box(3, 0, 4, 13, 16, 16);
    private static final VoxelShape SUED = Block.box(3, 0, 0, 13, 16, 12);
    private static final VoxelShape WEST = Block.box(4, 0, 3, 16, 16, 13);
    private static final VoxelShape OST = Block.box(0, 0, 3, 12, 16, 13);

    public FileCabinetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(FACING)) {
            case SOUTH -> SUED;
            case WEST -> WEST;
            case EAST -> OST;
            default -> NORD;
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FileCabinetBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> beType) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    /* Die Schubladen fahren heraus -- das zeichnet der Renderer der Blockentitaet, nicht ein
     * Modell aus dem Ressourcenpaket. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if(!level.isClientSide) IPersistentNBT.restoreData(level, pos, stack);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof CrateBaseBlockEntity crate && crate.canAccess(player)) {
            CrateBaseBlockEntity.spawnSpiders(player, level, crate);
            if(blockEntity instanceof MenuProvider menu) {
                player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), pos);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(!level.isClientSide && !state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof CrateBaseBlockEntity crate) {
                if(NtmConfig.SERVER.CRATE_KEEP_CONTENTS.get()) {
                    for(ItemStack stack : crate.getDrops(this)) Block.popResource(level, pos, stack);
                } else {
                    Containers.dropContents(level, pos, crate);
                    Block.popResource(level, pos, new ItemStack(this));
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity be, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return IPersistentNBT.getDropsFromLootParams(state, params);
    }

    /* Dieselbe Belegungsanzeige wie an den Kisten -- der Aktenschrank ist im Original eine. */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        if(TagsUtil.getCustomData(stack).contains(IPersistentNBT.NBT_PERSISTENT_KEY)) return;
        this.belegung(components, 0);
    }

    @Override
    public void appendHoverText(ItemStack stack, CompoundTag tag, List<Component> components, Item.TooltipContext context, TooltipFlag flag) {
        int belegt = tag.contains("Items") ? tag.getList("Items", 10).size() : 0;
        this.belegung(components, belegt);
    }

    private void belegung(List<Component> components, int belegt) {
        double anteil = belegt * 100.0D / FAECHER;
        components.add(Component.translatable("desc.crate_slots_used", belegt, FAECHER,
                String.format(Locale.ROOT, "%.1f", anteil)).withStyle(this.farbe(anteil)));
    }

    private ChatFormatting farbe(double anteil) {
        if(anteil >= 90.0D) return ChatFormatting.RED;
        if(anteil >= 50.0D) return ChatFormatting.GOLD;
        if(anteil > 0.0D) return ChatFormatting.YELLOW;
        return ChatFormatting.GREEN;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof CrateBaseBlockEntity crate) {
            CompoundTag tag = new CompoundTag();
            crate.writeNBT(tag);
            if(!tag.isEmpty()) TagsUtil.putCustomData(stack, tag);
        }
        return stack;
    }
}
