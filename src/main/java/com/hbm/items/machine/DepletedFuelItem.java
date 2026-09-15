package com.hbm.items.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.ICustomItemModelRegister;
import com.hbm.items.IMetaItem;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.main.NuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemDepletedFuel.
 *
 * Abgebrannter Brennstoff. Er faellt heiss an und muss im Brennstoffbecken abkuehlen, bevor er
 * sich weiterverarbeiten laesst -- heiss ist er roetlich eingefaerbt und traegt einen Hinweis im
 * Tooltip.
 *
 * Der Zustand steckt wie im Original in einer Zahl: null ist abgekuehlt, eins ist heiss.
 *
 * ABWEICHUNG: das Original leitet von ItemNuclearWaste ab, damit fallen gelassener Abfall zu
 * einer EntityItemWaste wird, die nie verfaellt. Diese Entitaet ist noch nicht portiert -- wie
 * bei den RBMK-Pellets in Runde 32.
 */
public class DepletedFuelItem extends Item implements IMetaItem, ICustomItemModelRegister {

    /** Abgekuehlt. */
    public static final int COOL = 0;
    /** Frisch aus dem Reaktor, muss ins Becken. */
    public static final int HOT = 1;

    /** Die Faerbung des heissen Zustands, unveraendert aus dem Original. */
    public static final int HOT_TINT = 0xFFBFA5;

    /** Mehrere Abfallsorten teilen sich eine Textur, darum steht sie hier und nicht im Namen. */
    private final String texture;

    public DepletedFuelItem(String texture, Properties properties) {
        super(properties.component(NtmDataComponents.META.get(), 0));
        this.texture = texture;
    }

    public static boolean isHot(ItemStack stack) {
        return MetaHelper.getMeta(stack) > 0;
    }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        stacks.add(MetaHelper.newStack(item, 1, COOL));
        stacks.add(MetaHelper.newStack(item, 1, HOT));
    }

    /**
     * Beide Zustaende teilen sich die Textur; den Unterschied macht die Faerbung, die
     * NuclearTechModClient registriert. Das Modell sorgt nur dafuer, dass die geteilte Textur
     * ueberhaupt gefunden wird -- der Gegenstand heisst ja anders als sie.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerItemModel(ItemModelProvider provider, ResourceLocation modelLocation) {

        ItemModelBuilder builder = provider.getBuilder(modelLocation.toString());
        String namespace = modelLocation.getNamespace();

        for(int i = 0; i <= HOT; i++) {
            builder.override()
                    .predicate(NuclearTechMod.withDefaultNamespace("item_meta"), i)
                    .model(provider.getBuilder(modelLocation.getPath() + "_" + i)
                            .parent(new ModelFile.UncheckedModelFile("item/generated"))
                            .texture("layer0", ResourceLocation.fromNamespaceAndPath(namespace, "item/" + this.texture)))
                    .end();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        if(isHot(stack)) list.add(Component.translatable("desc.item.wasteCooling").withStyle(ChatFormatting.GOLD));
    }
}
