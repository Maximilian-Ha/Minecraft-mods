package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import com.zuxelus.energycontrol.items.cards.ItemCardText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Die Zeilen einer Textkarte eintippen.
 *
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.gui.GuiCardText. Die erste Fassung dieses
 * Ports behalf sich mit dem Namen der Karte aus dem Amboss; mit dem Steuerpaket aus Stufe 4
 * geht es wieder so, wie es gedacht war -- acht Zeilen, jede mit eigener Farbe.
 */
public class CardTextScreen extends Screen {

    private static final int LINE_HEIGHT = 20;
    private static final int FIELD_WIDTH = 150;

    private final Screen parent;
    private final BlockPos pos;

    private final EditBox[] fields = new EditBox[ItemCardText.MAX_LINES];
    private final int[] colors = new int[ItemCardText.MAX_LINES];
    private final ItemCardReader initial;

    public CardTextScreen(Screen parent, BlockPos pos, ItemStack card) {
        super(Component.translatable("gui.energycontrol.card_text"));
        this.parent = parent;
        this.pos = pos;

        ItemCardReader reader = new ItemCardReader(card);
        for(int i = 0; i < ItemCardText.MAX_LINES; i++) {
            colors[i] = reader.getInt("color" + i);
        }
        this.initial = reader;
    }

    private int top() {
        return height / 2 - (ItemCardText.MAX_LINES * LINE_HEIGHT) / 2 - 10;
    }

    private int left() {
        return width / 2 - (FIELD_WIDTH + 24) / 2;
    }

    @Override
    protected void init() {
        super.init();

        for(int i = 0; i < ItemCardText.MAX_LINES; i++) {
            int line = i;
            int y = top() + i * LINE_HEIGHT;

            EditBox box = new EditBox(font, left(), y, FIELD_WIDTH, 16, Component.empty());
            box.setMaxLength(48);
            box.setValue(initial.getString("line" + i));
            fields[i] = box;
            addRenderableWidget(box);

            addRenderableWidget(Button.builder(Component.literal(" "), button -> cycleColor(line))
                    .bounds(left() + FIELD_WIDTH + 4, y, 20, 16).build());
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> apply())
                .bounds(width / 2 - 82, top() + ItemCardText.MAX_LINES * LINE_HEIGHT + 6, 80, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> back())
                .bounds(width / 2 + 2, top() + ItemCardText.MAX_LINES * LINE_HEIGHT + 6, 80, 20).build());

        setInitialFocus(fields[0]);
    }

    /** Die Farbe einer Zeile geht durch die sechzehn Farben der Tafel; null heisst Grundfarbe. */
    private void cycleColor(int line) {
        int current = colors[line];
        int index = -1;
        for(int i = 0; i < InfoPanelBlockEntity.COLORS.length; i++) {
            if(InfoPanelBlockEntity.COLORS[i] == current) {
                index = i;
                break;
            }
        }
        index++;
        colors[line] = index >= InfoPanelBlockEntity.COLORS.length ? 0 : InfoPanelBlockEntity.COLORS[index];
    }

    private void apply() {
        ListTag lines = new ListTag();
        for(EditBox field : fields) lines.add(StringTag.valueOf(field.getValue()));

        CompoundTag tag = new CompoundTag();
        tag.putString("action", "text");
        tag.put("lines", lines);
        tag.putIntArray("colors", colors);
        ControlSender.send(pos, tag);

        back();
    }

    private void back() {
        if(minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(font, title, width / 2, top() - 20, 0xFFFFFF);

        // Das Farbfeld liegt hinter dem Knopf; der Knopf selbst traegt keinen Text.
        for(int i = 0; i < ItemCardText.MAX_LINES; i++) {
            int y = top() + i * LINE_HEIGHT;
            int x = left() + FIELD_WIDTH + 4;
            int color = colors[i] == 0 ? 0xFFFFFF : colors[i];
            guiGraphics.fill(x + 5, y + 5, x + 15, y + 11, 0xFF000000 | color);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        back();
    }
}
