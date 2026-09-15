package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.MachineAnnihilatorBlockEntity;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.menus.MachineAnnihilatorMenu;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIMachineAnnihilator.
 *
 * Die Besonderheit ist das Textfeld in der Mitte: dort steht der Name des Vorrats, auf den
 * gezaehlt wird. Wer ihn aendert, faengt einen neuen Zaehler an -- mehrere Annihilatoren auf
 * demselben Namen zaehlen dagegen zusammen.
 */
public class MachineAnnihilatorScreen extends InfoScreen<MachineAnnihilatorMenu> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/processing/gui_annihilator.png");

    private final MachineAnnihilatorBlockEntity be;
    private EditBox pool;

    public MachineAnnihilatorScreen(MachineAnnihilatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.be = menu.be;
        this.imageWidth = 176;
        this.imageHeight = 208;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;

        this.pool = new EditBox(this.font, this.leftPos + 31, this.topPos + 85, 80, 8, Component.empty());
        this.pool.setTextColor(0x00FF00);
        this.pool.setTextColorUneditable(0x00FF00);
        this.pool.setBordered(false);
        this.pool.setMaxLength(20);
        this.pool.setValue(this.be.pool == null ? "" : this.be.pool);
        this.addWidget(this.pool);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.pool.render(guiGraphics, mouseX, mouseY, partialTicks);

        ItemStack watched = this.be.slots.get(8);

        if(!watched.isEmpty() && this.isHovered(mouseX, mouseY, 151, 35, 18, 18)) {

            Component name = watched.getHoverName();

            if(watched.getItem() instanceof IItemFluidIdentifier id) {
                FluidType type = id.getType(this.be.getLevel(), this.be.getBlockPos(), watched);
                if(type != null) name = Component.translatable(type.getUnlocalizedName());
            }

            guiGraphics.renderComponentTooltip(this.font, List.of(
                    name.copy().append(":"),
                    Component.literal(String.format(Locale.US, "%,d", this.be.monitor))
            ), mouseX, mouseY);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.pool.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(this.pool.charTyped(codePoint, modifiers)) {
            this.sendPool();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(this.pool.isFocused() && this.pool.keyPressed(keyCode, scanCode, modifiers)) {
            this.sendPool();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void sendPool() {
        CompoundTag tag = new CompoundTag();
        tag.putString("pool", this.pool.getValue());
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.be.getBlockPos()));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
