package com.hbm.items.weapon.grenade;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.grenade.GrenadeUniversal;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.grenade.GrenadeExtraItem.GrenadeExtra;
import com.hbm.items.weapon.grenade.GrenadeFillingItem.GrenadeFilling;
import com.hbm.items.weapon.grenade.GrenadeFuzeItem.GrenadeFuze;
import com.hbm.items.weapon.grenade.GrenadeShellItem.GrenadeShell;
import com.hbm.util.EnumUtil;
import com.hbm.util.TagsUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.grenade.ItemGrenadeUniversal.
 *
 * Die zusammengesetzte Granate. Vier Bauteile stecken in ihr -- Koerper, Fuellung, Zuender und
 * wahlweise ein Aufsatz -- und jedes davon gibt es auch einzeln als Gegenstand. Welche
 * Zusammenstellung ein Stapel traegt, steht in seinen Zusatzdaten.
 *
 * ABWEICHUNG, wo die Zusammenstellung steht: das Original schreibt vier Zahlen ins NBT des
 * Stapels. Auf 1.21 gibt es dafuer die eigenen Datenbestandteile; der Port legt sie unter
 * denselben vier Schluesseln in die Zusatzdaten, damit die Bedeutung erhalten bleibt.
 *
 * NICHT UEBERNOMMEN: das Ziehen. Im Original liegt die Granate erst nach einer Anlaufzeit
 * (grenadeDeployment in HbmPlayerProps) wurfbereit in der Hand, mit eigener Bewegung und
 * eigenen Toenen je Koerper. Dafuer fehlen dem Port die Spielereigenschaften und der Zeichner;
 * die Granate fliegt hier sofort. Die Anlaufzeiten stehen trotzdem schon im Koerper
 * (drawDuration), damit sie beim Nachreichen nicht neu gemessen werden muessen.
 */
public class GrenadeUniversalItem extends Item {

    public static final String KEY_SHELL = "shell";
    public static final String KEY_FILLING = "filling";
    public static final String KEY_FUZE = "fuze";
    public static final String KEY_EXTRA = "extra";

    public GrenadeUniversalItem(Properties properties) {
        super(properties);
    }

    /** Der Stapel richtet sich nach dem Koerper: vier Handgranaten, aber nur eine Nuka. */
    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getShell(stack).stackLimit;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if(!level.isClientSide) {
            GrenadeUniversal granate = new GrenadeUniversal(NtmEntityTypes.GRENADE_UNIVERSAL.get(), level);
            granate.setGrenadeItem(stack);
            granate.werfen(player);
            level.addFreshEntity(granate);
        }

        if(!player.isCreative()) stack.shrink(1);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static GrenadeShell getShell(ItemStack stack) {
        return EnumUtil.grabEnumSafely(GrenadeShell.class, lies(stack, KEY_SHELL, 0));
    }

    public static GrenadeFilling getFilling(ItemStack stack) {
        return EnumUtil.grabEnumSafely(GrenadeFilling.class, lies(stack, KEY_FILLING, GrenadeFilling.HE.ordinal()));
    }

    public static GrenadeFuze getFuze(ItemStack stack) {
        return EnumUtil.grabEnumSafely(GrenadeFuze.class, lies(stack, KEY_FUZE, 0));
    }

    /** Der Aufsatz ist das einzige Bauteil, das fehlen darf -- dann steht hier nichts. */
    @Nullable
    public static GrenadeExtra getExtra(ItemStack stack) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        if(!tag.contains(KEY_EXTRA)) return null;
        return EnumUtil.grabEnumSafely(GrenadeExtra.class, tag.getInt(KEY_EXTRA));
    }

    private static int lies(ItemStack stack, String schluessel, int ersatz) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        return tag.contains(schluessel) ? tag.getInt(schluessel) : ersatz;
    }

    public static ItemStack make(GrenadeShell shell, GrenadeFilling filling, GrenadeFuze fuze) { return make(shell, filling, fuze, null, 1); }
    public static ItemStack make(GrenadeShell shell, GrenadeFilling filling, GrenadeFuze fuze, GrenadeExtra extra) { return make(shell, filling, fuze, extra, 1); }

    public static ItemStack make(GrenadeShell shell, GrenadeFilling filling, GrenadeFuze fuze, @Nullable GrenadeExtra extra, int anzahl) {

        ItemStack stack = new ItemStack(NtmItems.GRENADE_UNIVERSAL.get(), anzahl);

        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.putInt(KEY_SHELL, shell.ordinal());
        tag.putInt(KEY_FILLING, filling.ordinal());
        tag.putInt(KEY_FUZE, fuze.ordinal());
        if(extra != null) tag.putInt(KEY_EXTRA, extra.ordinal());
        TagsUtil.putCustomData(stack, tag);

        return stack;
    }

    /**
     * Jede Zusammenstellung, die es geben kann: jeder Koerper mit jeder Fuellung, die
     * hineinpasst, mit jedem Zuender, einmal ohne und einmal mit jedem Aufsatz.
     */
    public static List<ItemStack> alleZusammenstellungen() {

        List<ItemStack> liste = new ArrayList<>();

        for(GrenadeShell shell : GrenadeShell.values())
        for(GrenadeFilling filling : GrenadeFilling.values()) {

            if(!filling.compatibleShells.contains(shell)) continue;

            for(GrenadeFuze fuze : GrenadeFuze.values()) {
                liste.add(make(shell, filling, fuze));
                for(GrenadeExtra extra : GrenadeExtra.values()) liste.add(make(shell, filling, fuze, extra));
            }
        }

        return liste;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        components.add(teil("grenade_shell", getShell(stack).name()).withStyle(ChatFormatting.YELLOW));
        components.add(teil("grenade_filling", getFilling(stack).name()).withStyle(ChatFormatting.YELLOW));
        components.add(teil("grenade_fuze", getFuze(stack).name()).withStyle(ChatFormatting.YELLOW));

        GrenadeExtra extra = getExtra(stack);
        if(extra != null) components.add(teil("grenade_extra", extra.name()).withStyle(ChatFormatting.RED));
    }

    private static net.minecraft.network.chat.MutableComponent teil(String gegenstand, String wert) {
        return Component.translatable("item.hbmsntm." + gegenstand + "." + wert.toLowerCase(Locale.US));
    }
}
