package com.hbm.items.food;

import com.hbm.inventory.NtmFoods;
import com.hbm.registry.NtmMobEffects;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.food.ItemPill.
 *
 * Eine Tablette. Sie saettigt nicht und laesst sich deshalb immer schlucken, auch mit vollem
 * Magen. Was sie bewirkt, haengt als Lambda daran -- im Original steht dafuer wieder eine
 * Kette aus "if(this == ModItems.xyz)" in onFoodEaten.
 *
 * JEDE TABLETTE LEGT DIE UEBELKEITSSPERRE AN, fuenf Sekunden lang, und zwar anders als bei
 * der Spritze OHNE vorher zu pruefen, ob sie schon anliegt: man kann Tabletten also hinter-
 * einander schlucken, sie sperren nur die Spritzen aus. So steht es im Original.
 *
 * NOCH NICHT PORTIERT sind die uebrigen Tabletten des Originals -- plan_c, pill_red, radx,
 * siox, pill_herbal und xanax. Sie haengen an Dingen, die der Port anders oder noch gar nicht
 * loest: eigene Schadensquellen, Asbest- und Staublunge, der Todeszustand.
 */
public class PillItem extends Item {

    /** Was die Tablette tut. Laeuft nur auf der Serverseite. */
    private final Consumer<Player> wirkung;

    /** Die Schluessel der Hinweiszeilen; der Wortlaut steht im Sprach-Erzeuger. */
    private final String[] hinweis;

    public PillItem(Properties properties, Consumer<Player> wirkung, String... hinweis) {
        super(properties.food(NtmFoods.PILL));
        this.wirkung = wirkung;
        this.hinweis = hinweis;
    }

    /** Die Jodtablette: nimmt dieselben neun Wirkungen weg wie der Sanitaetsbeutel. */
    public static PillItem jod(Properties properties) {
        return new PillItem(properties, NtmMobEffects::clearNegativeEffects, "desc.item.pill_iodine");
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity esser) {

        ItemStack rest = super.finishUsingItem(stack, level, esser);

        if(!level.isClientSide && esser instanceof Player spieler) {
            NtmMobEffects.applyPotionSickness(spieler, 5);
            this.wirkung.accept(spieler);
        }

        return rest;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        for(String schluessel : this.hinweis) components.add(Component.translatable(schluessel).withStyle(ChatFormatting.YELLOW));
    }
}
