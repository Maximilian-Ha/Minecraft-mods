package com.hbm.util;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.util.CrucibleUtil.
 *
 * Das Giessen selbst: ein senkrechter Strahl vom Ausguss nach unten, und was er trifft,
 * bekommt das Metall -- sofern es ein ICrucibleAcceptor ist. Trifft er nichts Passendes,
 * geht die Portion verloren, es sei denn, der Aufrufer giesst im Sicherheitsmodus.
 *
 * ABWEICHUNG: das Original reicht ein Vec3 hinein und schreibt den Auftreffpunkt direkt
 * hinein. Auf 1.21 ist Vec3 unveraenderlich, deshalb nimmt der Port dafuer eine
 * einzelliges Feld -- dasselbe Muster, das das Original schon fuer den Trefferpunkt benutzt.
 */
public class CrucibleUtil {

    /**
     * Giesst einen einzelnen Materialstapel. Zurueck kommt, was uebrig bleibt.
     * Der uebergebene Stapel wird dabei unmittelbar veraendert; wer das nicht will, kopiert
     * ihn vorher.
     */
    public static MaterialStack pourSingleStack(Level level, double x, double y, double z, double range, boolean safe, MaterialStack stack, int quanta, @Nullable Vec3[] impactPosHolder) {

        BlockHitResult[] hitHolder = new BlockHitResult[1];
        ICrucibleAcceptor acc = getPouringTarget(level, new Vec3(x, y, z), new Vec3(x, y - range, z), hitHolder);
        BlockHitResult hit = hitHolder[0];

        if(acc == null) {
            spill(hit, safe, stack, quanta, impactPosHolder);
            return stack;
        }

        MaterialStack ret = tryPourStack(level, acc, hit, stack, impactPosHolder);

        if(ret != null) return ret;

        spill(hit, safe, stack, quanta, impactPosHolder);
        return stack;
    }

    /**
     * Giesst aus einer Liste. Zurueck kommt, was tatsaechlich abgeflossen ist.
     * Die Stapel der Liste werden unmittelbar veraendert.
     */
    public static MaterialStack pourFullStack(Level level, double x, double y, double z, double range, boolean safe, List<MaterialStack> stacks, int quanta, @Nullable Vec3[] impactPosHolder) {

        if(stacks.isEmpty()) return null;

        BlockHitResult[] hitHolder = new BlockHitResult[1];
        ICrucibleAcceptor acc = getPouringTarget(level, new Vec3(x, y, z), new Vec3(x, y - range, z), hitHolder);
        BlockHitResult hit = hitHolder[0];

        if(acc == null) return spill(hit, safe, stacks, quanta, impactPosHolder);

        for(MaterialStack stack : stacks) {
            if(stack.material == null) continue;

            int amountToPour = Math.min(stack.amount, quanta);
            MaterialStack toPour = new MaterialStack(stack.material, amountToPour);
            MaterialStack left = tryPourStack(level, acc, hit, toPour, impactPosHolder);

            if(left != null) {
                stack.amount -= (amountToPour - left.amount);
                // ABWEICHUNG: das Original rechnet hier stack.amount - left.amount und benutzt
                // damit den schon verminderten Restbestand. Zurueck soll aber kommen, was
                // abgeflossen ist, und das ist amountToPour - left.amount.
                return new MaterialStack(stack.material, amountToPour - left.amount);
            }
        }

        return spill(hit, safe, stacks, quanta, impactPosHolder);
    }

    /**
     * Versucht, den Stapel beim gefundenen Abnehmer abzuladen. Zurueck kommt der Rest, oder
     * null, wenn der Abnehmer nichts nimmt -- dann verschuettet der Aufrufer.
     */
    public static MaterialStack tryPourStack(Level level, ICrucibleAcceptor acc, BlockHitResult hit, MaterialStack stack, @Nullable Vec3[] impactPosHolder) {

        if(stack.material.smeltable != SmeltingBehavior.SMELTABLE) return null;

        Vec3 loc = hit.getLocation();
        Direction side = hit.getDirection();

        if(acc.canAcceptPartialPour(level, hit.getBlockPos(), loc.x, loc.y, loc.z, side, stack)) {
            MaterialStack left = acc.pour(level, hit.getBlockPos(), loc.x, loc.y, loc.z, side, stack);

            if(left == null) left = new MaterialStack(stack.material, 0);
            if(impactPosHolder != null) impactPosHolder[0] = loc;

            return left;
        }

        return null;
    }

    /** Sucht das Ziel des Gusses per Strahl von oben nach unten. */
    public static ICrucibleAcceptor getPouringTarget(Level level, Vec3 start, Vec3 end, @Nullable BlockHitResult[] hitHolder) {

        // ABWEICHUNG: der Strahl fragt den Umriss ab, nicht den Kollisionskoerper. Das Original
        // benutzt die Blockgrenzen (collisionRayTrace), und die entsprechen auf 1.21 dem Umriss.
        // Mit dem Kollisionskoerper fiele der Guss mitten durch Becken und Form hindurch: deren
        // Kollisionskoerper ist der Trogrand, die Mitte ist hohl.
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, CollisionContext.empty()));

        if(hitHolder != null) hitHolder[0] = hit;
        if(hit == null || hit.getType() != HitResult.Type.BLOCK) return null;

        return level.getBlockState(hit.getBlockPos()).getBlock() instanceof ICrucibleAcceptor acc ? acc : null;
    }

    /** Wie unten, nimmt aber eine Liste und benutzt schlicht den ersten Stapel daraus. */
    public static MaterialStack spill(@Nullable BlockHitResult hit, boolean safe, List<MaterialStack> stacks, int quanta, @Nullable Vec3[] impactPos) {
        if(stacks.isEmpty()) return null;

        MaterialStack ret = spill(hit, safe, stacks.get(0), quanta, impactPos);
        stacks.removeIf(o -> o.amount <= 0);

        return ret;
    }

    /**
     * Was passiert, wenn der Strahl keinen Abnehmer trifft. Im Sicherheitsmodus nichts,
     * sonst geht die Portion verloren. Zurueck kommt, was verloren ging.
     */
    public static MaterialStack spill(@Nullable BlockHitResult hit, boolean safe, MaterialStack stack, int quanta, @Nullable Vec3[] impactPos) {

        if(safe) return null;

        MaterialStack toWaste = new MaterialStack(stack.material, Math.min(stack.amount, quanta));
        stack.amount -= toWaste.amount;

        if(impactPos != null && hit != null) impactPos[0] = hit.getLocation();

        return toWaste;
    }
}
