package com.hbm.items.tools;

import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.network.ConveyorBendableBlock;
import com.hbm.blocks.network.CraneBaseBlock;
import com.hbm.util.TagsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemConveyorWand.
 *
 * Der Bandstab legt Foerderbaender -- einzeln oder gleich eine ganze Strecke. Er ist im Original
 * der EINZIGE Weg, an ein Band zu kommen: die Baender selbst haben dort kein Werkbankrezept.
 *
 * ZWEI KLICKS UND EINE STRECKE. Der erste Klick merkt sich Ort und Flaeche, der zweite baut von
 * dort bis hierher -- mit Kurven, wo der Weg abbiegen muss, mit Steigband und Schacht, wo er
 * die Hoehe wechselt. Wer nur EIN Band setzen will, klickt mit Schleichtaste.
 *
 * DER WEG WIRD ZWEIMAL GEGANGEN: einmal trocken, um zu sehen, ob er ueberhaupt frei ist und ob
 * genug Baender da sind, und erst dann wirklich. So bleibt bei einer versperrten Strecke kein
 * halbes Band stehen. Das steht so im Original.
 *
 * ABWEICHUNG: der Stab VERBRAUCHT BAENDER AUS DEM RUCKSACK, nicht sich selbst. Im Original ist
 * der Stab zugleich der Vorrat -- man haelt sechzehn Staebe und legt sechzehn Baender. Im Port
 * haben die Baender eigene Gegenstaende und eigene Rezepte (siehe Runde 94), und es waere
 * verwirrend, daneben einen zweiten Vorrat zu fuehren. Der Stab ist deshalb ein Werkzeug: er
 * legt, was man ohnehin dabeihat, und geht dabei nicht kaputt.
 *
 * ABWEICHUNG: das Original zeigt die geplante Strecke als Geisterbild an, bevor man den zweiten
 * Klick setzt. Dafuer braucht es RenderOverhead, das der Port nicht hat. Gebaut wird trotzdem
 * dasselbe -- man sieht es nur erst hinterher.
 *
 * ABWEICHUNG: das Original reisst mit Schleichtaste in der Schoepferrunde eine ganze Bandlinie
 * auf einmal ab. Das ist eine Bequemlichkeit fuer den Bau und nicht Teil des Foerdernetzes.
 */
public class ConveyorWandItem extends Item {

    public enum ConveyorType {
        REGULAR,
        EXPRESS,
        DOUBLE,
        TRIPLE
    }

    public final ConveyorType type;

    public ConveyorWandItem(Properties properties, ConveyorType type) {
        super(properties.stacksTo(1));
        this.type = type;
    }

    /** Nur das einfache Band kennt Steigband und Schacht -- so haelt es das Original. */
    public boolean hasSnakesAndLadders() {
        return this.type == ConveyorType.REGULAR;
    }

    public Block conveyor() {
        return switch(this.type) {
            case EXPRESS -> NtmBlocks.CONVEYOR_EXPRESS.get();
            case DOUBLE -> NtmBlocks.CONVEYOR_DOUBLE.get();
            case TRIPLE -> NtmBlocks.CONVEYOR_TRIPLE.get();
            default -> NtmBlocks.CONVEYOR.get();
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Player player = context.getPlayer();
        Level level = context.getLevel();

        if(player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();

        if(player.isShiftKeyDown() && !this.isArmed(stack)) {
            return this.placeSingle(level, player, stack, pos, side);
        }

        if(!this.isArmed(stack)) {
            this.arm(stack, pos, side);
            if(level.isClientSide) player.displayClientMessage(
                    Component.translatable("conveyorWand.armed").withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.SUCCESS;
        }

        if(!level.isClientSide) this.build(level, player, stack, pos, side);

        this.disarm(stack);
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------------------------
    // Der gemerkte Anfangspunkt
    // ------------------------------------------------------------------------------------

    private boolean isArmed(ItemStack stack) {
        return TagsUtil.getCustomData(stack).contains("wandX");
    }

    private void arm(ItemStack stack, BlockPos pos, Direction side) {

        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.putInt("wandX", pos.getX());
        tag.putInt("wandY", pos.getY());
        tag.putInt("wandZ", pos.getZ());
        tag.putInt("wandSide", side.get3DDataValue());
        TagsUtil.putCustomData(stack, tag);
    }

    private void disarm(ItemStack stack) {

        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.remove("wandX");
        tag.remove("wandY");
        tag.remove("wandZ");
        tag.remove("wandSide");
        TagsUtil.putCustomData(stack, tag);
    }

    // ------------------------------------------------------------------------------------
    // Ein einzelnes Band
    // ------------------------------------------------------------------------------------

    /**
     * Mit Schleichtaste wird ein einzelnes Band gesetzt. Klickt man dabei von oben oder unten
     * auf ein einfaches Band, wird dieses erst zum Steigband oder Schacht -- so wachsen
     * senkrechte Strecken, ohne dass man die Bauform eigens umschalten muss.
     */
    private InteractionResult placeSingle(Level level, Player player, ItemStack stack, BlockPos pos, Direction side) {

        BlockState onState = level.getBlockState(pos);
        Block onBlock = onState.getBlock();

        if(this.hasSnakesAndLadders() && onBlock == NtmBlocks.CONVEYOR.get()) {

            Direction facing = onState.getValue(BlockStateProperties.HORIZONTAL_FACING);

            if(side == Direction.UP) {
                onBlock = NtmBlocks.CONVEYOR_LIFT.get();
                if(!level.isClientSide) level.setBlock(pos, onBlock.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
            } else if(side == Direction.DOWN) {
                onBlock = NtmBlocks.CONVEYOR_CHUTE.get();
                if(!level.isClientSide) level.setBlock(pos, onBlock.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
            }
        }

        Block toPlace = this.conveyor();

        if(this.hasSnakesAndLadders()) {
            if(onBlock == NtmBlocks.CONVEYOR_LIFT.get() && side == Direction.UP) toPlace = NtmBlocks.CONVEYOR_LIFT.get();
            if(onBlock == NtmBlocks.CONVEYOR_CHUTE.get() && side == Direction.DOWN) toPlace = NtmBlocks.CONVEYOR_CHUTE.get();
        }

        BlockPos target = pos.relative(side);
        if(!level.getBlockState(target).canBeReplaced()) return InteractionResult.FAIL;

        if(level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack supply = this.findSupply(player);
        if(supply.isEmpty()) {
            player.displayClientMessage(Component.translatable("conveyorWand.empty").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        Direction facing = player.getDirection();
        level.setBlock(target, toPlace.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);

        if(!player.hasInfiniteMaterials()) supply.shrink(1);

        return InteractionResult.CONSUME;
    }

    // ------------------------------------------------------------------------------------
    // Die ganze Strecke
    // ------------------------------------------------------------------------------------

    private void build(Level level, Player player, ItemStack stack, BlockPos endPos, Direction endSide) {

        CompoundTag tag = TagsUtil.getCustomData(stack);

        BlockPos startPos = new BlockPos(tag.getInt("wandX"), tag.getInt("wandY"), tag.getInt("wandZ"));
        Direction startSide = Direction.from3DDataValue(tag.getInt("wandSide"));

        int available = this.countSupply(player);

        /* Erst trocken gehen -- steht etwas im Weg oder reichen die Baender nicht, wird gar
         * nicht gebaut. */
        int needed = this.route(level, null, player, startPos, startSide, endPos, endSide, available);

        if(needed < 0) {
            player.displayClientMessage(Component.translatable("conveyorWand.obstructed").withStyle(ChatFormatting.RED), true);
            return;
        }

        if(needed == 0) {
            player.displayClientMessage(Component.translatable("conveyorWand.empty").withStyle(ChatFormatting.RED), true);
            return;
        }

        this.route(level, level, player, startPos, startSide, endPos, endSide, available);

        if(!player.hasInfiniteMaterials()) this.consume(player, needed);

        player.displayClientMessage(Component.translatable("conveyorWand.built", needed).withStyle(ChatFormatting.GREEN), true);
    }

    /**
     * Portiert aus construct() des Originals. Geht vom Anfangspunkt Schritt fuer Schritt auf den
     * Zielpunkt zu und setzt dabei Bandstuecke; biegt ab, wenn der gerade Weg sich vom Ziel
     * entfernt oder versperrt ist.
     *
     * Ist "build" null, wird nur gerechnet. Zurueck kommt die Zahl der gesetzten Stuecke, null
     * wenn der Vorrat nicht reicht, und minus eins, wenn der Weg versperrt ist.
     */
    private int route(Level level, @Nullable Level build, Player player,
            BlockPos startPos, Direction startSide, BlockPos endPos, Direction endSide, int max) {

        Direction dir = startSide;
        Direction targetDir = endSide;

        /* Ein einzelnes Stueck senkrecht ueber dem Anfangspunkt: es gibt keinen Weg, nur eine
         * Setzung, und die Richtung gibt der Spieler vor. */
        if(startPos.equals(endPos) && startSide == endSide && dir.getAxis() == Direction.Axis.Y) {

            BlockPos single = startPos.relative(dir);
            if(!level.getBlockState(single).canBeReplaced()) return -1;
            if(max < 1) return 0;

            if(build != null) this.place(build, single, this.conveyor(), player.getDirection(), null);
            return 1;
        }

        boolean vertical = this.hasSnakesAndLadders();

        BlockPos target = endPos.relative(targetDir);
        BlockPos at = startPos.relative(dir);

        if(dir.getAxis() == Direction.Axis.Y) {
            dir = towards(at, endPos, endPos, null, false, vertical);
        }

        Block endBlock = level.getBlockState(endPos).getBlock();
        boolean targetHorizontal = targetDir.getAxis() != Direction.Axis.Y;
        boolean turnToTarget = targetHorizontal
                || endBlock instanceof CraneBaseBlock
                || endBlock == NtmBlocks.CONVEYOR_LIFT.get()
                || endBlock == NtmBlocks.CONVEYOR_CHUTE.get();

        Direction horDir = dir.getAxis() == Direction.Axis.Y ? player.getDirection().getOpposite() : dir;

        /* Zuerst auf Bodenhoehe fallen, wenn das geht. */
        if(vertical && at.getY() > target.getY() && level.getBlockState(at.below()).canBeReplaced()) {
            dir = Direction.DOWN;
        }

        for(int depth = 1; depth <= max; depth++) {

            if(!level.getBlockState(at).canBeReplaced()) return -1;

            Block block = this.forDirection(dir);
            Direction facing = this.facingFor(block, dir, targetDir, horDir);
            Direction bend = null;

            BlockPos next = at.relative(dir);

            int fromDistance = taxi(at, target);
            int toDistance = taxi(next, target);
            int finalDistance = taxi(next, endPos);

            boolean notAtTarget = (turnToTarget ? finalDistance : fromDistance) > 0;
            boolean obstructed = notAtTarget && !level.getBlockState(next).canBeReplaced();

            if((toDistance >= fromDistance && notAtTarget) || obstructed) {

                Direction newDir = towards(at, turnToTarget ? endPos : target, target, dir, obstructed, vertical);

                if(newDir == Direction.UP) {
                    block = NtmBlocks.CONVEYOR_LIFT.get();
                    facing = this.facingFor(block, dir, targetDir, horDir);
                } else if(newDir == Direction.DOWN) {
                    block = NtmBlocks.CONVEYOR_CHUTE.get();
                    facing = this.facingFor(block, dir, targetDir, horDir);
                } else if(newDir != dir) {
                    bend = newDir;
                }

                dir = newDir;
                if(dir.getAxis() != Direction.Axis.Y) horDir = dir;
            }

            if(build != null) this.place(build, at, block, facing, bend);

            if(at.equals(target)) return depth;

            at = at.relative(dir);
        }

        return 0;
    }

    /**
     * Setzt ein Bandstueck. Soll es abbiegen, wird die Bauform gewaehlt, deren Ausgang wirklich
     * in die gewuenschte Richtung zeigt.
     *
     * Das Original zaehlt dafuer vier oder acht auf die Metadaten. Nachzurechnen, welche
     * Drehrichtung das im Port ist, waere eine Fehlerquelle ohne Not -- hier steht stattdessen
     * dieselbe Zuordnung, die ConveyorBendableBlock.getOutputDirection benutzt, und sie wird
     * durchprobiert statt umgestellt.
     */
    private void place(Level level, BlockPos pos, Block block, Direction facing, @Nullable Direction bend) {

        BlockState state = block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);

        if(bend != null && block instanceof ConveyorBendableBlock) {

            Direction primary = facing.getOpposite();

            for(ConveyorBendableBlock.ConveyorShape shape : ConveyorBendableBlock.ConveyorShape.values()) {

                Direction out = switch(shape) {
                    case LEFT -> primary.getCounterClockWise();
                    case RIGHT -> primary.getClockWise();
                    case STRAIGHT -> primary;
                };

                if(out == bend) { state = state.setValue(ConveyorBendableBlock.SHAPE, shape); break; }
            }
        }

        level.setBlock(pos, state, 3);
    }

    private Block forDirection(Direction dir) {
        if(dir == Direction.UP) return NtmBlocks.CONVEYOR_LIFT.get();
        if(dir == Direction.DOWN) return NtmBlocks.CONVEYOR_CHUTE.get();
        return this.conveyor();
    }

    /** Die Blockrichtung zeigt dorthin, WOHER die Ware kommt -- deshalb ueberall die Gegenseite. */
    private Direction facingFor(Block block, Direction dir, Direction targetDir, Direction horDir) {

        if(block != NtmBlocks.CONVEYOR_LIFT.get() && block != NtmBlocks.CONVEYOR_CHUTE.get()) {
            return dir.getAxis() == Direction.Axis.Y ? horDir.getOpposite() : dir.getOpposite();
        }

        if(targetDir.getAxis() == Direction.Axis.Y) return horDir.getOpposite();
        return targetDir;
    }

    private static Direction towards(BlockPos from, BlockPos to, BlockPos target,
            @Nullable Direction heading, boolean obstructed, boolean vertical) {

        if(vertical && (from.getY() != to.getY() || from.getY() != target.getY())
                && (obstructed
                    || (from.getX() == to.getX() && from.getZ() == to.getZ())
                    || (from.getX() == target.getX() && from.getZ() == target.getZ()))) {
            return from.getY() > to.getY() ? Direction.DOWN : Direction.UP;
        }

        if(Math.abs(from.getX() - to.getX()) > Math.abs(from.getZ() - to.getZ())) {
            if(heading == Direction.EAST || heading == Direction.WEST) {
                return from.getZ() > to.getZ() ? Direction.NORTH : Direction.SOUTH;
            }
            return from.getX() > to.getX() ? Direction.WEST : Direction.EAST;
        }

        if(heading == Direction.NORTH || heading == Direction.SOUTH) {
            return from.getX() > to.getX() ? Direction.WEST : Direction.EAST;
        }
        return from.getZ() > to.getZ() ? Direction.NORTH : Direction.SOUTH;
    }

    private static int taxi(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    // ------------------------------------------------------------------------------------
    // Der Vorrat
    // ------------------------------------------------------------------------------------

    /**
     * Gezaehlt und verbraucht wird das Band, das dieser Stab legt -- nicht Steigband und
     * Schacht, die er unterwegs daraus macht. Im Original ist das dasselbe Stueck; hier sind es
     * drei Bloecke, und eine Strecke, die an einer Steigung scheitert, weil gerade kein
     * Steigband im Rucksack liegt, waere eine Zumutung.
     */
    private ItemStack findSupply(Player player) {

        if(player.hasInfiniteMaterials()) return new ItemStack(this.conveyor());

        for(ItemStack stack : player.getInventory().items) {
            if(stack.is(this.conveyor().asItem())) return stack;
        }

        return ItemStack.EMPTY;
    }

    private int countSupply(Player player) {

        if(player.hasInfiniteMaterials()) return 256;

        int count = 0;
        for(ItemStack stack : player.getInventory().items) {
            if(stack.is(this.conveyor().asItem())) count += stack.getCount();
        }

        return Math.min(count, 256);
    }

    private void consume(Player player, int amount) {

        for(ItemStack stack : player.getInventory().items) {

            if(amount <= 0) return;
            if(!stack.is(this.conveyor().asItem())) continue;

            int taken = Math.min(amount, stack.getCount());
            stack.shrink(taken);
            amount -= taken;
        }
    }
}
