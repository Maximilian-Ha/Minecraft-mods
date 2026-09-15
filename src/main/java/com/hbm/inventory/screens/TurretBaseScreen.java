package com.hbm.inventory.screens;

import com.hbm.blockentity.turret.TurretBaseBlockEntity;
import com.hbm.inventory.menus.TurretBaseMenu;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUITurretBase.
 *
 * Oben der Ein-Aus-Schalter und die vier Zielschalter, darunter die Strichliste der erledigten
 * Ziele, links die Namensliste des Zielchips mit ihren vier Knoepfen, rechts die Stromsaeule.
 *
 * Die Strichliste zaehlt in Fuenfergruppen und wird ab 36 durch einen einzigen langen Strich
 * ersetzt -- ab da lohnt das Zaehlen nicht mehr.
 *
 * Alle Bauarten teilen sich dieses Menue; welches Bild darunter liegt, sagt der Turm.
 *
 * ABWEICHUNG: die Anzeige der moeglichen Patronenarten ueber den Munitionsfaechern zeigt hier
 * eine einfache Liste statt der durchlaufenden Gegenstandsreihe des Originals; die dafuer noetige
 * Zeichenroutine (drawStackText) hat der Port nicht.
 */
public class TurretBaseScreen extends InfoScreen<TurretBaseMenu> {

    protected final TurretBaseBlockEntity turret;
    protected EditBox field;
    protected int index;

    public TurretBaseScreen(TurretBaseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.turret = menu.be;

        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    /**
     * Welches Bild die Oberflaeche traegt, sagt der Turm selbst -- alle Bauarten teilen sich ein
     * Menue, aber jede hat ihr eigenes Bild.
     */
    protected ResourceLocation getTexture() { return this.turret.getGuiTexture(); }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;

        this.field = new EditBox(this.font, this.leftPos + 10, this.topPos + 65, 50, 14, Component.empty());
        this.field.setMaxLength(25);
        this.field.setBordered(false);
        this.field.setTextColor(0x00FF00);
        this.addRenderableWidget(this.field);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.drawElectricityInfo(guiGraphics, mouseX, mouseY, leftPos + 152, topPos + 45, 16, 52, turret.getPower(), turret.getMaxPower());

        Component on = Component.translatable("turret.on").withStyle(ChatFormatting.GREEN);
        Component off = Component.translatable("turret.off").withStyle(ChatFormatting.RED);

        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, leftPos + 8, topPos + 30, 10, 10, mouseX, mouseY,
                Component.translatable("turret.players", turret.targetPlayers ? on : off));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, leftPos + 22, topPos + 30, 10, 10, mouseX, mouseY,
                Component.translatable("turret.animals", turret.targetAnimals ? on : off));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, leftPos + 36, topPos + 30, 10, 10, mouseX, mouseY,
                Component.translatable("turret.mobs", turret.targetMobs ? on : off));
        this.drawCustomInfoStat(guiGraphics, mouseX, mouseY, leftPos + 50, topPos + 30, 10, 10, mouseX, mouseY,
                Component.translatable("turret.machines", turret.targetMachines ? on : off));

        /* Ueber den Munitionsfaechern steht, was der Turm ueberhaupt verschiesst. */
        if(this.menu.getCarried().isEmpty() && this.isHovered(mouseX, mouseY, 79, 62, 54, 54) && !this.isOverFilledAmmoSlot(mouseX, mouseY)) {

            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("turret.ammo").withStyle(ChatFormatting.YELLOW));
            for(ItemStack stack : turret.getAmmoTypesForDisplay()) lines.add(stack.getHoverName().copy().withStyle(ChatFormatting.GRAY));

            guiGraphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /** Liegt in dem Fach unter dem Zeiger schon etwas, gilt dessen eigener Hinweis. */
    private boolean isOverFilledAmmoSlot(int mouseX, int mouseY) {
        for(int i = 1; i < 10; i++) {
            if(this.menu.slots.get(i).hasItem() && this.isHovered(mouseX, mouseY,
                    this.menu.slots.get(i).x, this.menu.slots.get(i).y, 16, 16)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {

        CompoundTag tag = new CompoundTag();

        if(this.isHovered(x, y, 115, 26, 18, 18)) { this.click(); tag.putBoolean("toggle", true); }
        if(this.isHovered(x, y, 8, 30, 10, 10)) { this.click(); tag.putBoolean("players", true); }
        if(this.isHovered(x, y, 22, 30, 10, 10)) { this.click(); tag.putBoolean("animals", true); }
        if(this.isHovered(x, y, 36, 30, 10, 10)) { this.click(); tag.putBoolean("mobs", true); }
        if(this.isHovered(x, y, 50, 30, 10, 10)) { this.click(); tag.putBoolean("machines", true); }

        int count = this.getCount();

        if(count > 0) {
            if(this.isHovered(x, y, 7, 80, 18, 18)) {
                this.click();
                this.index--;
                if(this.index < 0) this.index = count - 1;
            }
            if(this.isHovered(x, y, 43, 80, 18, 18)) {
                this.click();
                this.index++;
                this.index %= count;
            }
        }

        if(this.isHovered(x, y, 7, 98, 18, 18)) {
            this.click();
            if(!this.field.getValue().isEmpty()) {
                tag.putString("name", this.field.getValue());
                this.field.setValue("");
            }
        }

        if(this.isHovered(x, y, 43, 98, 18, 18) && count > 0) {
            this.click();
            tag.putInt("del", this.index);
        }

        if(!tag.isEmpty()) PacketDistributor.sendToServer(new CompoundTagControl(tag, turret.getBlockPos()));

        return super.mouseClicked(x, y, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        List<String> names = turret.getWhitelist();

        while(this.index >= this.getCount()) this.index--;
        if(this.index < 0) this.index = 0;

        Component shown = names == null
                ? Component.translatable("turret.none").withStyle(ChatFormatting.ITALIC)
                : Component.literal(names.get(this.index));

        /* Der Name steht in halber Groesse, sonst passt er nicht in das Feld. */
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5F, 0.5F, 1F);
        guiGraphics.drawString(this.font, shown, 24, 102, 0x00FF00, false);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(this.getTexture(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if(this.isHovered(mouseX, mouseY, 7, 80, 18, 18)) guiGraphics.blit(this.getTexture(), leftPos + 7, topPos + 80, 176, 58, 18, 18);
        if(this.isHovered(mouseX, mouseY, 43, 80, 18, 18)) guiGraphics.blit(this.getTexture(), leftPos + 43, topPos + 80, 194, 58, 18, 18);
        if(this.isHovered(mouseX, mouseY, 7, 98, 18, 18)) guiGraphics.blit(this.getTexture(), leftPos + 7, topPos + 98, 176, 76, 18, 18);
        if(this.isHovered(mouseX, mouseY, 43, 98, 18, 18)) guiGraphics.blit(this.getTexture(), leftPos + 43, topPos + 98, 194, 76, 18, 18);

        int i = turret.getPowerScaled(53);
        guiGraphics.blit(this.getTexture(), leftPos + 152, topPos + 97 - i, 194, 52 - i, 16, i);

        if(turret.isOn) guiGraphics.blit(this.getTexture(), leftPos + 115, topPos + 26, 176, 40, 18, 18);
        if(turret.targetPlayers) guiGraphics.blit(this.getTexture(), leftPos + 8, topPos + 30, 176, 0, 10, 10);
        if(turret.targetAnimals) guiGraphics.blit(this.getTexture(), leftPos + 22, topPos + 30, 176, 10, 10, 10);
        if(turret.targetMobs) guiGraphics.blit(this.getTexture(), leftPos + 36, topPos + 30, 176, 20, 10, 10);
        if(turret.targetMachines) guiGraphics.blit(this.getTexture(), leftPos + 50, topPos + 30, 176, 30, 10, 10);

        this.renderTallies(guiGraphics);
    }

    /** Die Strichliste: je Fuenfergruppe ein durchgestrichener Block, ab 36 nur noch ein Balken. */
    private void renderTallies(GuiGraphics guiGraphics) {

        int tallies = turret.stattrak;

        if(tallies >= 36) {
            guiGraphics.blit(this.getTexture(), leftPos + 77, topPos + 50, 176, 120, 63, 6);
            return;
        }

        int steps = (int) Math.ceil(tallies / 5D);

        for(int s = 0; s < steps; s++) {

            int m = tallies % 5;

            if(s < steps - 1 || m == 0) {
                guiGraphics.blit(this.getTexture(), leftPos + 77 + 9 * s, topPos + 50, 194, 94, 9, 6);
            } else {
                guiGraphics.blit(this.getTexture(), leftPos + 77 + 9 * s, topPos + 50, 176, 94, m * 2, 6);
            }
        }
    }

    private int getCount() {
        List<String> names = turret.getWhitelist();
        return names == null ? 0 : names.size();
    }
}
