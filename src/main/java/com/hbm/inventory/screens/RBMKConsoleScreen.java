package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKColumnType;
import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity.RBMKColumn;
import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity.ScreenType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toserver.CompoundTagControl;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIRBMKConsole.
 *
 * Das Reaktorpult. Alle Blit- und Trefferkoordinaten sind unveraendert uebernommen.
 *
 * Diese Oberflaeche hat kein Inventar; sie wird ueber
 * {@link com.hbm.network.toclient.OpenScreenPacket} geoeffnet, nicht ueber einen MenuProvider.
 *
 * ABWEICHUNG: den Flussverlauf zeichnet das Original mit GL_LINES ueber den Tessellator. In
 * 1.21 gibt es dafuer keine Entsprechung mehr -- hier stehen kurze gefuellte Rechtecke, die
 * denselben Streckenzug ergeben.
 */
public class RBMKConsoleScreen extends Screen {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/gui/reactors/gui_rbmk_console.png");

    private static final int GRID = RBMKConsoleBlockEntity.GRID;
    private static final int CELL = 10;
    private static final int GRID_X = 86;
    private static final int GRID_Y = 11;

    private final RBMKConsoleBlockEntity console;

    private int guiLeft;
    private int guiTop;
    private final int xSize = 244;
    private final int ySize = 172;

    private boolean[] selection = new boolean[GRID * GRID];
    private boolean az5Lid = true;
    private long lastPress = 0;

    private EditBox field;

    public RBMKConsoleScreen(RBMKConsoleBlockEntity console) {
        super(Component.translatable("container.rbmkConsole"));
        this.console = console;
    }

    @Override
    protected void init() {
        super.init();

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        this.field = new EditBox(this.font, this.guiLeft + 9, this.guiTop + 84, 35, 9, Component.empty());
        this.field.setTextColor(0x00FF00);
        this.field.setTextColorUneditable(0x008000);
        this.field.setBordered(false);
        this.field.setMaxLength(3);
        this.addRenderableWidget(this.field);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        this.drawBackgroundLayer(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        int index = this.indexAt(mouseX, mouseY);

        if(index >= 0 && this.console.columns[index] != null) {
            guiGraphics.renderComponentTooltip(this.font, this.describe(this.console.columns[index]), mouseX, mouseY);
        }

        this.hover(guiGraphics, mouseX, mouseY, 61, 70, 10, 10, Component.literal("Select all control rods"));
        this.hover(guiGraphics, mouseX, mouseY, 72, 70, 10, 10, Component.literal("Deselect all"));
        this.hover(guiGraphics, mouseX, mouseY, 70, 82, 12, 12, Component.literal("Cycle steam channel compressor setting"));
        this.hover(guiGraphics, mouseX, mouseY, 48, 82, 12, 12, Component.literal("Apply rod level to selection"));

        String[] colors = {"red", "yellow", "green", "blue", "purple"};
        ChatFormatting[] formats = {ChatFormatting.RED, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE};

        for(int k = 0; k < 5; k++) {
            this.hover(guiGraphics, mouseX, mouseY, 6 + k * 11, 70, 10, 10,
                    Component.literal("Left click: Select " + colors[k] + " group").withStyle(formats[k]),
                    Component.literal("Right click: Assign " + colors[k] + " group").withStyle(formats[k]));
        }

        for(int j = 0; j < 3; j++) {
            for(int k = 0; k < 2; k++) {
                int id = j * 2 + k;
                ScreenType type = this.console.screens[id].type;
                this.hover(guiGraphics, mouseX, mouseY, 6 + 40 * k, 8 + 21 * j, 18, 18,
                        Component.literal("Display " + (id + 1) + ": " + type.name().toLowerCase(Locale.US)).withStyle(ChatFormatting.YELLOW));
                this.hover(guiGraphics, mouseX, mouseY, 24 + 40 * k, 8 + 21 * j, 18, 18,
                        Component.literal("Assign selection to display " + (id + 1)));
            }
        }
    }

    private void hover(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int w, int h, Component... text) {
        if(this.guiLeft + x <= mouseX && this.guiLeft + x + w > mouseX && this.guiTop + y < mouseY && this.guiTop + y + h >= mouseY) {
            guiGraphics.renderComponentTooltip(this.font, List.of(text), mouseX, mouseY);
        }
    }

    /** Index der Rasterzelle unter dem Zeiger, oder -1. */
    private int indexAt(double mouseX, double mouseY) {

        if(mouseX < this.guiLeft + GRID_X || mouseX >= this.guiLeft + GRID_X + GRID * CELL) return -1;
        if(mouseY <= this.guiTop + GRID_Y || mouseY > this.guiTop + GRID_Y + GRID * CELL) return -1;

        int index = ((int) mouseX - GRID_X - this.guiLeft) / CELL + ((int) mouseY - GRID_Y - this.guiTop) / CELL * GRID;
        return index >= 0 && index < this.selection.length ? index : -1;
    }

    /**
     * Die Werte einer Saeule als Text. Das Original baut die Liste in RBMKColumn.getFancyStats
     * zusammen; hier steht sie, weil sie nur auf dem Client gebraucht wird.
     */
    private List<Component> describe(RBMKColumn col) {

        List<Component> stats = new ArrayList<>();
        stats.add(Component.literal(col.type.name()));
        stats.add(Component.translatable("rbmk.heat", fmt(col.data.getDouble("heat")) + " C").withStyle(ChatFormatting.YELLOW));

        switch(col.type) {
            case FUEL, FUEL_SIM -> {
                stats.add(Component.translatable("rbmk.rod.depletion", fmt((1D - col.data.getDouble("enrichment")) * 100D) + "%").withStyle(ChatFormatting.GREEN));
                stats.add(Component.translatable("rbmk.rod.xenon", fmt(col.data.getDouble("xenon")) + "%").withStyle(ChatFormatting.DARK_PURPLE));
                stats.add(Component.translatable("rbmk.rod.coreTemp", fmt(col.data.getDouble("c_coreHeat")) + " C").withStyle(ChatFormatting.DARK_RED));
                stats.add(Component.translatable("rbmk.rod.skinTemp", fmt(col.data.getDouble("c_heat")) + " C", fmt(col.data.getDouble("c_maxHeat")) + " C").withStyle(ChatFormatting.RED));
            }
            case BOILER -> {
                stats.add(Component.translatable("rbmk.boiler.water", col.data.getInt("water"), col.data.getInt("maxWater")).withStyle(ChatFormatting.BLUE));
                stats.add(Component.translatable("rbmk.boiler.steam", col.data.getInt("steam"), col.data.getInt("maxSteam")).withStyle(ChatFormatting.WHITE));
                stats.add(Component.translatable("rbmk.boiler.type", Fluids.fromID(col.data.getShort("type")).getName()).withStyle(ChatFormatting.YELLOW));
            }
            case CONTROL, CONTROL_AUTO -> {
                stats.add(Component.translatable("rbmk.control.level", (int) (col.data.getDouble("level") * 100D) + "%").withStyle(ChatFormatting.YELLOW));
            }
            case HEATEX -> {
                stats.add(Fluids.fromID(col.data.getShort("type")).getName().copy()
                        .append(Component.literal(" " + col.data.getInt("water") + "/" + col.data.getInt("maxWater") + "mB")).withStyle(ChatFormatting.BLUE));
                stats.add(Fluids.fromID(col.data.getShort("hottype")).getName().copy()
                        .append(Component.literal(" " + col.data.getInt("steam") + "/" + col.data.getInt("maxSteam") + "mB")).withStyle(ChatFormatting.RED));
            }
            default -> { }
        }

        if(col.data.getBoolean("moderated")) stats.add(Component.translatable("rbmk.moderated").withStyle(ChatFormatting.YELLOW));

        return stats;
    }

    private static String fmt(double value) {
        return String.valueOf(((int) (value * 10)) / 10D);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        boolean right = button == 1;

        int index = this.indexAt(mouseX, mouseY);

        if(index >= 0 && this.console.columns[index] != null) {
            this.selection[index] = !this.selection[index];
            this.click(0.75F + (this.selection[index] ? 0.25F : 0.0F));
            return true;
        }

        if(this.isHovered(mouseX, mouseY, 72, 70, 10, 10)) {
            this.selection = new boolean[GRID * GRID];
            this.click(0.5F);
            return true;
        }

        if(this.isHovered(mouseX, mouseY, 61, 70, 10, 10)) {
            this.selection = new boolean[GRID * GRID];
            for(int j = 0; j < this.console.columns.length; j++) {
                if(this.isType(j, RBMKColumnType.CONTROL)) this.selection[j] = true;
            }
            this.click(1.5F);
            return true;
        }

        if(this.isHovered(mouseX, mouseY, 70, 82, 12, 12)) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("compressor", true);
            tag.putIntArray("cols", this.selected(RBMKColumnType.BOILER));
            this.send(tag);
            this.click(1F);
            return true;
        }

        for(int k = 0; k < 5; k++) {

            if(!this.isHovered(mouseX, mouseY, 6 + k * 11, 70, 10, 10)) continue;

            if(right) {
                CompoundTag tag = new CompoundTag();
                tag.putByte("assignColor", (byte) k);
                tag.putIntArray("cols", this.selected(RBMKColumnType.CONTROL));
                this.send(tag);
            } else {
                this.selection = new boolean[GRID * GRID];
                for(int j = 0; j < this.console.columns.length; j++) {
                    if(this.isType(j, RBMKColumnType.CONTROL) && this.console.columns[j].data.getShort("color") == k) this.selection[j] = true;
                }
            }

            this.click(0.8F + k * 0.1F);
            return true;
        }

        if(this.isHovered(mouseX, mouseY, 30, 138, 28, 28)) {

            if(this.az5Lid) {
                this.az5Lid = false;
                this.play(NtmSoundEvents.RBMK_AZ5_COVER.get(), 0.5F);

            } else if(this.lastPress + 3000 < System.currentTimeMillis()) {

                this.lastPress = System.currentTimeMillis();

                CompoundTag tag = new CompoundTag();
                tag.putDouble("level", 0);
                for(int j = 0; j < this.console.columns.length; j++) {
                    if(this.isType(j, RBMKColumnType.CONTROL)) tag.putInt("sel_" + j, j);
                }
                this.send(tag);
                this.click(1F);
            }

            return true;
        }

        if(this.isHovered(mouseX, mouseY, 48, 82, 12, 12)) {

            int level;
            try {
                level = (int) Mth.clamp(Double.parseDouble(this.field.getValue()), 0, 100);
            } catch(NumberFormatException ignored) {
                return true;
            }

            this.field.setValue(String.valueOf(level));

            CompoundTag tag = new CompoundTag();
            tag.putDouble("level", level * 0.01D);
            for(int j = 0; j < this.selection.length; j++) {
                if(this.selection[j]) tag.putInt("sel_" + j, j);
            }
            this.send(tag);
            this.click(1F);
            return true;
        }

        for(int j = 0; j < 3; j++) {
            for(int k = 0; k < 2; k++) {

                int id = j * 2 + k;

                if(this.isHovered(mouseX, mouseY, 6 + 40 * k, 8 + 21 * j, 18, 18)) {
                    CompoundTag tag = new CompoundTag();
                    tag.putByte("toggle", (byte) id);
                    this.send(tag);
                    this.click(0.5F);
                    return true;
                }

                if(this.isHovered(mouseX, mouseY, 24 + 40 * k, 8 + 21 * j, 18, 18)) {
                    CompoundTag tag = new CompoundTag();
                    tag.putByte("id", (byte) id);
                    for(int s = 0; s < this.selection.length; s++) {
                        if(this.selection[s]) tag.putBoolean("s" + s, true);
                    }
                    this.send(tag);
                    this.click(0.75F);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isType(int index, RBMKColumnType type) {
        return this.console.columns[index] != null && this.console.columns[index].type == type;
    }

    /** Die ausgewaehlten Saeulen des gegebenen Typs. */
    private int[] selected(RBMKColumnType type) {
        List<Integer> list = new ArrayList<>();
        for(int j = 0; j < this.console.columns.length; j++) {
            if(this.selection[j] && this.isType(j, type)) list.add(j);
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private void send(CompoundTag tag) {
        PacketDistributor.sendToServer(new CompoundTagControl(tag, this.console.getBlockPos()));
    }

    private boolean isHovered(double mouseX, double mouseY, int x, int y, int w, int h) {
        return this.guiLeft + x <= mouseX && this.guiLeft + x + w > mouseX && this.guiTop + y < mouseY && this.guiTop + y + h >= mouseY;
    }

    private void click(float pitch) {
        this.play(SoundEvents.UI_BUTTON_CLICK.value(), pitch);
    }

    private void play(net.minecraft.sounds.SoundEvent sound, float pitch) {
        if(this.minecraft != null) this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }

    private void drawBackgroundLayer(GuiGraphics guiGraphics) {

        guiGraphics.blit(TEXTURE, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, 256, 256);

        if(this.az5Lid) guiGraphics.blit(TEXTURE, this.guiLeft + 30, this.guiTop + 138, 228, 172, 28, 28, 256, 256);

        for(int j = 0; j < 3; j++) {
            for(int k = 0; k < 2; k++) {
                int id = j * 2 + k;
                guiGraphics.blit(TEXTURE, this.guiLeft + 6 + 40 * k, this.guiTop + 8 + 21 * j, this.console.screens[id].type.offset, 238, 18, 18, 256, 256);
            }
        }

        for(int i = 0; i < this.console.columns.length; i++) {

            RBMKColumn col = this.console.columns[i];
            if(col == null) continue;

            int x = GRID_X + CELL * (i % GRID);
            int y = GRID_Y + CELL * (i / GRID);

            guiGraphics.blit(TEXTURE, this.guiLeft + x, this.guiTop + y, col.type.offset, 172, CELL, CELL, 256, 256);

            double maxHeat = Math.max(col.data.getDouble("maxHeat"), 1);
            int h = Mth.clamp((int) Math.ceil((col.data.getDouble("heat") - 20) * 10 / maxHeat), 0, 10);
            if(h > 0) guiGraphics.blit(TEXTURE, this.guiLeft + x, this.guiTop + y + CELL - h, 0, 192 - h, 10, h, 256, 256);

            this.drawColumnDetail(guiGraphics, col, x, y);

            if(this.selection[i]) guiGraphics.blit(TEXTURE, this.guiLeft + x, this.guiTop + y, 0, 192, 10, 10, 256, 256);
        }

        this.drawFluxGraph(guiGraphics);
    }

    private void drawColumnDetail(GuiGraphics guiGraphics, RBMKColumn col, int x, int y) {

        switch(col.type) {

            case CONTROL, CONTROL_AUTO -> {
                if(col.type == RBMKColumnType.CONTROL) {
                    int color = col.data.getShort("color");
                    if(color > -1) guiGraphics.blit(TEXTURE, this.guiLeft + x, this.guiTop + y, color * CELL, 202, 10, 10, 256, 256);
                }
                int fr = 8 - (int) Math.ceil(col.data.getDouble("level") * 8);
                if(fr > 0) guiGraphics.blit(TEXTURE, this.guiLeft + x + 4, this.guiTop + y + 1, 24, 183, 2, fr, 256, 256);
            }

            case FUEL, FUEL_SIM -> {
                if(!col.data.contains("c_heat")) return;

                int fh = Mth.clamp((int) Math.ceil((col.data.getDouble("c_heat") - 20) * 8 / Math.max(col.data.getDouble("c_maxHeat"), 1)), 0, 8);
                if(fh > 0) guiGraphics.blit(TEXTURE, this.guiLeft + x + 1, this.guiTop + y + CELL - fh - 1, 11, 191 - fh, 2, fh, 256, 256);

                int fe = Mth.clamp((int) Math.ceil(col.data.getDouble("enrichment") * 8), 0, 8);
                if(fe > 0) guiGraphics.blit(TEXTURE, this.guiLeft + x + 4, this.guiTop + y + CELL - fe - 1, 14, 191 - fe, 2, fe, 256, 256);

                int fx = Mth.clamp((int) Math.ceil(col.data.getDouble("xenon") * 8 / 100), 0, 8);
                if(fx > 0) guiGraphics.blit(TEXTURE, this.guiLeft + x + 7, this.guiTop + y + CELL - fx - 1, 17, 191 - fx, 2, fx, 256, 256);
            }

            case BOILER -> {
                int fw = Mth.clamp((int) Math.ceil(col.data.getInt("water") * 8 / Math.max(col.data.getDouble("maxWater"), 1)), 0, 8);
                if(fw > 0) guiGraphics.blit(TEXTURE, this.guiLeft + x + 1, this.guiTop + y + CELL - fw - 1, 41, 191 - fw, 3, fw, 256, 256);

                int fs = Mth.clamp((int) Math.ceil(col.data.getInt("steam") * 8 / Math.max(col.data.getDouble("maxSteam"), 1)), 0, 8);
                if(fs > 0) guiGraphics.blit(TEXTURE, this.guiLeft + x + 6, this.guiTop + y + CELL - fs - 1, 46, 191 - fs, 3, fs, 256, 256);

                short type = col.data.getShort("type");
                if(type == Fluids.STEAM.getID())          guiGraphics.blit(TEXTURE, this.guiLeft + x + 4, this.guiTop + y + 1, 44, 183, 2, 2, 256, 256);
                if(type == Fluids.HOTSTEAM.getID())       guiGraphics.blit(TEXTURE, this.guiLeft + x + 4, this.guiTop + y + 3, 44, 185, 2, 2, 256, 256);
                if(type == Fluids.SUPERHOTSTEAM.getID())  guiGraphics.blit(TEXTURE, this.guiLeft + x + 4, this.guiTop + y + 5, 44, 187, 2, 2, 256, 256);
                if(type == Fluids.ULTRAHOTSTEAM.getID())  guiGraphics.blit(TEXTURE, this.guiLeft + x + 4, this.guiTop + y + 7, 44, 189, 2, 2, 256, 256);
            }

            case HEATEX -> {
                int cc = Mth.clamp((int) Math.ceil(col.data.getInt("water") * 8 / Math.max(col.data.getDouble("maxWater"), 1)), 0, 8);
                if(cc > 0) guiGraphics.blit(TEXTURE, this.guiLeft + x + 1, this.guiTop + y + CELL - cc - 1, 131, 191 - cc, 3, cc, 256, 256);

                int hc = Mth.clamp((int) Math.ceil(col.data.getInt("steam") * 8 / Math.max(col.data.getDouble("maxSteam"), 1)), 0, 8);
                if(hc > 0) guiGraphics.blit(TEXTURE, this.guiLeft + x + 6, this.guiTop + y + CELL - hc - 1, 136, 191 - hc, 3, hc, 256, 256);
            }

            default -> { }
        }
    }

    /** Der Flussverlauf der letzten Minute, als Streckenzug aus kurzen Rechtecken. */
    private void drawFluxGraph(GuiGraphics guiGraphics) {

        int highest = Integer.MIN_VALUE;
        int lowest = Integer.MAX_VALUE;

        for(int i : this.console.fluxBuffer) {
            if(i > highest) highest = i;
            if(i < lowest) lowest = i;
        }

        int range = Math.max(highest - lowest, 1);
        int count = this.console.fluxBuffer.length;

        for(int i = 0; i < count - 1; i++) {

            double x1 = this.guiLeft + 7 + i * 74D / count;
            double x2 = this.guiLeft + 7 + (i + 1) * 74D / count;
            double y1 = this.guiTop + 127 - (this.console.fluxBuffer[i] - lowest) * 24D / range;
            double y2 = this.guiTop + 127 - (this.console.fluxBuffer[i + 1] - lowest) * 24D / range;

            int steps = Math.max((int) Math.ceil(Math.abs(y2 - y1)), 1);

            for(int s = 0; s < steps; s++) {
                int px = (int) Math.round(x1 + (x2 - x1) * s / steps);
                int py = (int) Math.round(y1 + (y2 - y1) * s / steps);
                guiGraphics.fill(px, py, px + 2, py + 1, 0xFF00FF00);
            }
        }

        guiGraphics.drawString(this.font, String.valueOf(highest), this.guiLeft + 8, this.guiTop + 98, 0x00FF00, false);
        guiGraphics.drawString(this.font, String.valueOf(lowest), this.guiLeft + 8, this.guiTop + 127, 0x00FF00, false);

        for(int i = 0; i < this.console.screens.length; i++) {
            String display = this.console.screens[i].display;
            if(display == null) continue;
            guiGraphics.drawString(this.font, display + this.console.screens[i].type.unit(),
                    this.guiLeft + 6 + 40 * (i % 2), this.guiTop + 27 + 21 * (i / 2), 0x00FF00, false);
        }
    }
}
