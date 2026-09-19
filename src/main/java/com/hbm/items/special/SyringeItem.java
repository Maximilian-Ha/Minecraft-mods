package com.hbm.items.special;

import com.hbm.items.NtmItems;
import com.hbm.registry.NtmMobEffects;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Portiert aus 1.7.10: com.hbm.items.special.ItemSyringe.
 *
 * Eine gefuellte Metallspritze. Rechtsklick spritzt sie, danach bleibt die leere Huelle
 * zurueck -- und eine Weile lang wirkt keine zweite. Diese Sperre ist die Trankuebelkeit;
 * ohne sie liesse sich mit einem Stapel Stimpaks jede Verletzung wegklicken.
 *
 * ABWEICHUNG: das Original ist EINE Klasse mit einer Kette aus "if(this == ModItems.xyz)",
 * eine Abfrage je Spritze -- der Quelltext traegt dort nicht umsonst die Marke @Spaghetti.
 * Der Port haengt die Wirkung stattdessen an den Gegenstand: jede Spritze bekommt bei der
 * Anmeldung ihre eigene. Die Zahlen sind unveraendert.
 *
 * NOCH NICHT PORTIERT: syringe_taint und syringe_mkunicorn (beide brauchen die Verseuchung),
 * die Blutbeutel und der Sanitaetsbeutel (ItemSimpleConsumable, ein eigenes Teilsystem).
 */
public class SyringeItem extends Item {

    /** Was diese Spritze tut. */
    private final Consumer<Player> wirkung;

    /** Wie lange die Uebelkeit danach anliegt, in Sekunden. */
    private final int uebelkeit;

    /** Die Schluessel der Hinweiszeilen; der Wortlaut steht im Sprach-Erzeuger. */
    private final String[] hinweis;

    /** Was uebrig bleibt. Die vier Metallspritzen lassen eine Metallhuelle zurueck, das
     *  Gegenmittel eine glaeserne -- so steht es im Original. */
    private final Supplier<Item> huelle;

    public SyringeItem(Properties properties, Consumer<Player> wirkung, int uebelkeit, String... hinweis) {
        this(properties, wirkung, uebelkeit, () -> NtmItems.SYRINGE_METAL_EMPTY.get(), hinweis);
    }

    public SyringeItem(Properties properties, Consumer<Player> wirkung, int uebelkeit, Supplier<Item> huelle, String... hinweis) {
        super(properties);
        this.wirkung = wirkung;
        this.uebelkeit = uebelkeit;
        this.huelle = huelle;
        this.hinweis = hinweis;
    }

    /* Die vier Spritzen des Originals, mit dessen Zahlen. */

    public static SyringeItem stimpak(Properties properties) {
        return new SyringeItem(properties, player -> player.heal(5F), 5, "desc.item.syringe.stimpak");
    }

    public static SyringeItem medx(Properties properties) {
        return new SyringeItem(properties,
                player -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 4 * 60 * 20, 2)),
                5, "desc.item.syringe.medx");
    }

    public static SyringeItem psycho(Properties properties) {
        return new SyringeItem(properties, player -> {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2 * 60 * 20, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2 * 60 * 20, 0));
        }, 5, "desc.item.syringe.psycho.resistance", "desc.item.syringe.psycho.strength");
    }

    public static SyringeItem superStimpak(Properties properties) {
        return new SyringeItem(properties, player -> {
            player.heal(25F);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 0));
        }, 15, "desc.item.syringe.super.heal", "desc.item.syringe.super.slow");
    }

    /**
     * Das Gegenmittel. Es raeumt alle Trankwirkungen ab -- auch die guten, denn es
     * unterscheidet nicht.
     *
     * ABWEICHUNG: im Original ist es kein ItemSyringe, sondern ein ItemSimpleConsumable, und
     * das laesst sich auch jemand anderem in den Arm rammen (setHitActionServer). Diesen
     * zweiten Weg hat der Port nicht; hier wirkt die Spritze nur auf den, der sie haelt.
     * Die Uebelkeitssperre gilt in beiden Faellen.
     */
    public static SyringeItem antidote(Properties properties) {
        return new SyringeItem(properties, player -> player.removeAllEffects(), 5,
                () -> NtmItems.SYRINGE_EMPTY.get(), "desc.item.syringe.antidote");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if(NtmMobEffects.hasPotionSickness(player)) return InteractionResultHolder.pass(stack);
        if(level.isClientSide) return InteractionResultHolder.success(stack);

        this.wirkung.accept(player);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), NtmSoundEvents.SYRINGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        NtmMobEffects.applyPotionSickness(player, this.uebelkeit);

        stack.shrink(1);

        /*
         * Die leere Huelle bleibt uebrig. War das die letzte Spritze, tritt sie an deren
         * Stelle; sonst wandert sie ins Inventar, und wenn dort kein Platz ist, vor die Fuesse.
         * Genauso macht es das Original.
         */
        ItemStack huelle = new ItemStack(this.huelle.get());

        if(stack.isEmpty()) return InteractionResultHolder.consume(huelle);
        if(!player.getInventory().add(huelle)) player.drop(huelle, false);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        for(String schluessel : this.hinweis) components.add(Component.translatable(schluessel).withStyle(ChatFormatting.YELLOW));
    }
}
