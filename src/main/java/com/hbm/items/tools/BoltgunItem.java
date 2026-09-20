package com.hbm.items.tools;

import api.hbm.block.IToolable.ToolType;

import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.items.BoltItem;
import com.hbm.items.IAnimatedItem;
import com.hbm.items.NtmItems;
import com.hbm.network.toclient.HbmAnimation;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.InventoryUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemBoltgun.
 *
 * DIE BOLZENPISTOLE. Sie ist das einzige Werkzeug der Sorte ToolType.BOLT -- ohne sie warteten
 * im Port vier Umbauschritte an Watz und ICF auf ein Werkzeug, das es nicht gab.
 *
 * SIE KANN DREIERLEI, und das ist ein Werkzeug mehr als die uebrigen Einstellwerkzeuge:
 *
 *   * RECHTSKLICK AUF EIN BAUTEIL -- der gewoehnliche Werkzeuggriff aus ToolingItem. Das
 *     Bauteil verschluckt die Bolzen selbst und geht eine Baustufe weiter.
 *   * RECHTSKLICK AUF EINEN GEWOEHNLICHEN BLOCK -- das Original fuehrt dafuer einen zweiten,
 *     winzigen Katalog: Stein wird fuer einen Durastahlbolzen zu Bruchstein. Genau ein
 *     Eintrag, und er steht unten.
 *   * LINKSKLICK AUF EIN WESEN -- sie schiesst ihm einen Bolzen aus dem Rucksack entgegen.
 *     Zehn Schaden, und die Ruestung haelt ihn nicht auf.
 *
 * DIE MUNITION KOMMT AUS DEM RUCKSACK, nicht aus der Waffe, und die Reihenfolge ist die des
 * Originals: Stahl vor Wolfram vor Durastahl. Der erste Bolzen, der sich findet, wird
 * verschossen -- wer sparsam sein will, laesst die teuren zu Hause.
 *
 * ABWEICHUNGEN:
 *   * Das Original vergibt beim Toeten eines Spielers den Erfolg achGoFish. Ein Erfolgssystem
 *     gibt es im Port nicht; nachgemessen, es ist nirgends portiert.
 *   * setDamageBypassesArmor() des Originals ist im Port eine eigene Schadensart
 *     (NtmDamageTypes.BOLTGUN im Sack BYPASSES_ARMOR) -- in 1.21 haengt das an der Schadensart,
 *     nicht am einzelnen Schlag.
 *   * Das Original blendet den Rueckstoss auf dem Client sofort ein und schickt dem Server nur
 *     das Ergebnis. Der Port schickt die Bewegung vom Server, wie jede andere auch; das kostet
 *     einen Paketweg und spart eine Sonderregel.
 */
public class BoltgunItem extends ToolingItem implements IAnimatedItem {

    /** Die einzige Bewegung, die dieser Gegenstand kennt. */
    public static final short ANIM_RECOIL = 0;

    /**
     * Die Bolzensorten in der Reihenfolge des Originals. Blei fehlt absichtlich: ein Bleibolzen
     * ist im Original kein Geschoss dieser Waffe.
     */
    private static final BoltItem.Type[] SORTEN = {
            BoltItem.Type.STEEL, BoltItem.Type.TUNGSTEN, BoltItem.Type.DURA_STEEL };

    /** Was ein gewoehnlicher Block kostet und woraus er wird. */
    public record Umbau(Block ziel, AStack[] kosten) { }

    /**
     * Der zweite Katalog. Er steht hier und nicht in ToolConversionBlock, weil dessen Katalog
     * ueber (Werkzeug, Block, Baustufe) schlaegt -- ein Stein hat keine Baustufe, und er setzt
     * auch kein onScrew um, ueber das der Block selbst antworten koennte.
     */
    public static final Map<Block, Umbau> BLOCKUMBAU = new HashMap<>();

    static {
        BLOCKUMBAU.put(Blocks.STONE, new Umbau(Blocks.COBBLESTONE,
                new AStack[] { new TagStack(MaterialShapes.BOLT.getTag(Mats.MAT_DURA), 1) }));
    }

    public BoltgunItem(Properties properties) {
        super(ToolType.BOLT, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {

        InteractionResult ergebnis = super.onItemUseFirst(stack, context);

        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if(ergebnis == InteractionResult.SUCCESS) {
            knallen(level, player, Vec3.atCenterOf(pos));
            return ergebnis;
        }

        if(player == null || level.isClientSide) return ergebnis;

        Umbau umbau = BLOCKUMBAU.get(level.getBlockState(pos).getBlock());
        if(umbau == null) return ergebnis;

        List<AStack> kosten = new ArrayList<>(List.of(umbau.kosten()));
        if(!kosten.isEmpty() && !InventoryUtil.doesPlayerHaveAStacks(player, kosten, true)) return ergebnis;

        level.setBlock(pos, umbau.ziel().defaultBlockState(), 3);
        knallen(level, player, Vec3.atCenterOf(pos));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {

        Level level = player.level;
        if(!entity.isAlive()) return false;
        if(level.isClientSide) return false;

        int platz = sucheBolzen(player);
        if(platz < 0) return false;

        player.getInventory().removeItem(platz, 1);

        /*
         * Das Original nimmt attackEntityFromIgnoreIFrame, damit der Bolzen auch trifft, wenn
         * das Wesen gerade erst getroffen wurde. Im Port ist hurtNT der Weg dorthin; fuer
         * alles, was kein Lebewesen ist (Fahrzeuge, Bilderrahmen), bleibt der schlichte
         * Schlag, denn hurtNT rechnet mit Ruestungswerten, die es dort nicht gibt.
         */
        if(entity instanceof LivingEntity lebewesen) {
            EntityDamageUtil.hurtNT(lebewesen, level.damageSources().source(NtmDamageTypes.BOLTGUN, player),
                    10F, true, false, 0D, 0F, 0F);
        } else {
            entity.hurt(level.damageSources().source(NtmDamageTypes.BOLTGUN, player), 10F);
        }

        knallen(level, player, entity.position().add(0D, entity.getBbHeight() / 2D, 0D));
        return true;
    }

    /** Der Platz im Rucksack, auf dem der erste passende Bolzen liegt, oder -1. */
    private static int sucheBolzen(Player player) {

        for(BoltItem.Type sorte : SORTEN) {
            for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack slot = player.getInventory().getItem(i);
                if(slot.isEmpty()) continue;
                if(slot.getItem() != NtmItems.BOLT.get()) continue;
                if(MetaHelper.getMeta(slot) != sorte.meta) continue;
                return i;
            }
        }

        return -1;
    }

    /** Knall, Rauchwolke und Rueckstoss -- im Original processNetwork. */
    private static void knallen(Level level, Player player, Vec3 ort) {

        level.playSound(null, ort.x, ort.y, ort.z, NtmSoundEvents.BOLTGUN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        if(level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, ort.x, ort.y, ort.z, 1, 0D, 0D, 0D, 0D);
        }

        if(player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new HbmAnimation(ANIM_RECOIL, 0, 0));
        }
    }

    /**
     * Der Rueckstoss: das Rohr faehrt in fuenfzig Millisekunden eine Einheit zurueck und in
     * hundert wieder vor. Wortgetreu aus dem Original.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public BusAnimation getAnimation(ItemStack stack, short animType) {

        if(animType != ANIM_RECOIL) return null;

        return new BusAnimation().addBus("RECOIL", new BusAnimationSequence()
                .addPos(1, 0, 1, 50)
                .addPos(0, 0, 1, 100));
    }
}
