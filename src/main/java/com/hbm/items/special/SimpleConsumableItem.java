package com.hbm.items.special;

import com.hbm.items.NtmItems;
import com.hbm.lib.ModEffect;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.special.ItemSimpleConsumable.
 *
 * Ein Gegenstand, der auf Rechtsklick eine Kleinigkeit tut und dabei einen Behaelter
 * zuruecklaesst. Das Original nennt das im eigenen Kommentar "the power of generics and
 * delegates": eine Klasse, und was sie tut, haengt als Lambda daran. Der Port macht es
 * genauso -- das ist dieselbe Bauart, die SyringeItem schon benutzt.
 *
 * BERICHTIGUNG: im Kopf von SyringeItem stand, ItemSimpleConsumable sei "ein eigenes
 * Teilsystem". Das ist es nicht. Es sind 181 Zeilen mit vier Lambda-Feldern und drei
 * Hilfsmethoden, und davon braucht diese Runde die Haelfte.
 *
 * NICHT UEBERNOMMEN: die Trefferwirkung (setHitAction). Sie gehoert zu den drei Spritzen
 * -- Gegenmittel, Gift, Wunderspritze --, die man auch jemand anderem in den Arm rammen
 * kann; die stehen noch aus. Ein Feld, das niemand setzt, waere toter Code.
 */
public class SimpleConsumableItem extends Item {

    /** Was der Gegenstand tut. Laeuft nur auf der Serverseite, wie im Original. */
    private final BiConsumer<ItemStack, Player> wirkung;

    /** Die Schluessel der Hinweiszeilen; der Wortlaut steht im Sprach-Erzeuger. */
    private final String[] hinweis;

    public SimpleConsumableItem(Properties properties, BiConsumer<ItemStack, Player> wirkung, String... hinweis) {
        super(properties);
        this.wirkung = wirkung;
        this.hinweis = hinweis;
    }

    /*
     * Die sieben Gegenstaende des Originals, mit dessen Zahlen. Die drei Radaway-Staerken
     * unterscheiden sich in nichts als der Dauer.
     */

    /** Radaway: zieht Strahlung ab, so lange der Effekt anliegt, und laesst den leeren Beutel. */
    public static SimpleConsumableItem radaway(Properties properties, int dauer, String hinweis) {
        return new SimpleConsumableItem(properties, (stack, nutzer) -> {
            gibKlangUndVerbrauche(stack, nutzer, NtmSoundEvents.RADAWAY.get(), new ItemStack(NtmItems.IV_EMPTY.get()));
            verlaengereEffekt(nutzer, ModEffect.RADAWAY, dauer, 0);
        }, hinweis);
    }

    /**
     * Der leere Beutel wird am eigenen Arm gefuellt: fuenf Herzen fuer eine Blutkonserve.
     * Wer damit auf null geht, stirbt daran -- auch das steht so im Original.
     *
     * DER FAKEPLAYER IST AUSGENOMMEN. Sonst liesse sich der Beutel von einer Maschine
     * halten und fuellen, die kein Leben hat, das sie verlieren koennte.
     */
    public static SimpleConsumableItem blutbeutelLeer(Properties properties) {
        return new SimpleConsumableItem(properties, (stack, nutzer) -> {
            if(nutzer instanceof FakePlayer) return;

            gibKlangUndVerbrauche(stack, nutzer, NtmSoundEvents.SYRINGE.get(), new ItemStack(NtmItems.IV_BLOOD.get()));
            nutzer.setHealth(Math.max(nutzer.getHealth() - 5F, 0F));

            if(nutzer.getHealth() <= 0F) nutzer.die(nutzer.level().damageSources().magic());
        });
    }

    /** Und zurueck: die Konserve gibt die fuenf Herzen wieder her. */
    public static SimpleConsumableItem blutbeutelVoll(Properties properties) {
        return new SimpleConsumableItem(properties, (stack, nutzer) -> {
            gibKlangUndVerbrauche(stack, nutzer, NtmSoundEvents.RADAWAY.get(), new ItemStack(NtmItems.IV_EMPTY.get()));
            nutzer.heal(5F);
        });
    }

    /** Dasselbe mit Erfahrung statt Blut: hundert Punkte hinein ... */
    public static SimpleConsumableItem erfahrungsbeutelLeer(Properties properties) {
        return new SimpleConsumableItem(properties, (stack, nutzer) -> {
            if(nutzer.totalExperience < 100) return;

            gibKlangUndVerbrauche(stack, nutzer, NtmSoundEvents.SYRINGE.get(), new ItemStack(NtmItems.IV_XP.get()));
            nutzer.giveExperiencePoints(-100);
        });
    }

    /** ... und wieder heraus. */
    public static SimpleConsumableItem erfahrungsbeutelVoll(Properties properties) {
        return new SimpleConsumableItem(properties, (stack, nutzer) -> {
            gibKlangUndVerbrauche(stack, nutzer, SoundEvents.EXPERIENCE_ORB_PICKUP, new ItemStack(NtmItems.IV_XP_EMPTY.get()));
            nutzer.giveExperiencePoints(100);
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if(level.isClientSide) return InteractionResultHolder.success(stack);

        this.wirkung.accept(stack, player);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        for(String schluessel : this.hinweis) components.add(Component.translatable(schluessel).withStyle(ChatFormatting.YELLOW));
    }

    /**
     * Entspricht ItemSimpleConsumable.giveSoundAndDecrement: einen abziehen, den Klang
     * spielen, den Behaelter zurueckgeben.
     *
     * ABWEICHUNG GEGENUEBER SyringeItem: dort tritt der Behaelter an die Stelle der letzten
     * Spritze. Hier nicht -- das Original legt ihn immer ins Inventar und wirft ihn vor die
     * Fuesse, wenn dort kein Platz ist. Beim Beutel faellt das auf, weil er im Wechsel
     * benutzt wird: waere er der Rueckgabewert, landete die Konserve in der Hand statt im
     * Beutelstapel.
     */
    public static void gibKlangUndVerbrauche(ItemStack stack, Player nutzer, SoundEvent klang, ItemStack behaelter) {

        stack.shrink(1);

        nutzer.level().playSound(null, nutzer.getX(), nutzer.getY(), nutzer.getZ(),
                klang, SoundSource.PLAYERS, 1.0F, 1.0F);

        if(!nutzer.getInventory().add(behaelter)) nutzer.drop(behaelter, false);
    }

    /**
     * Entspricht ItemSimpleConsumable.addPotionEffect. Liegt der Effekt schon an, wird
     * seine Restdauer AUFADDIERT statt ueberschrieben -- zwei Beutel wirken also doppelt so
     * lange und nicht nur so lange wie einer.
     */
    public static void verlaengereEffekt(LivingEntity wesen, Holder<MobEffect> effekt, int dauer, int stufe) {

        MobEffectInstance anliegend = wesen.getEffect(effekt);
        int gesamt = anliegend == null ? dauer : anliegend.getDuration() + dauer;

        wesen.addEffect(new MobEffectInstance(effekt, gesamt, stufe));
    }
}
