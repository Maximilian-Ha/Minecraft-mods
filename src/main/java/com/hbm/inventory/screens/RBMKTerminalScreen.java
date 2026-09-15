package com.hbm.inventory.screens;

import com.hbm.blockentity.machine.rbmk.RBMKTerminalBlockEntity;
import com.hbm.network.toserver.CompoundTagControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.gui.GUIScreenRBMKTerminal.
 *
 * Eine Oberflaeche, die fast nichts zeigt: nur die Befehlsliste in der Ecke. Die eingetippte
 * Zeile steht nicht hier, sondern auf der Tafel selbst -- der Renderer holt sie sich ueber
 * getWorkingLine. Darum ist das Eingabefeld unsichtbar und wird nie gezeichnet.
 *
 * lastTerminal merkt sich, an welchem Terminal getippt wird, damit nur dieses den Schreibstrich
 * zeigt und nicht jedes in Sichtweite.
 */
public class RBMKTerminalScreen extends Screen {

    public static RBMKTerminalBlockEntity lastTerminal;

    private final RBMKTerminalBlockEntity terminal;
    private EditBox line;

    public RBMKTerminalScreen(RBMKTerminalBlockEntity terminal) {
        super(Component.translatable("container.rbmkTerminal"));
        this.terminal = terminal;
        lastTerminal = terminal;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {
        this.line = new EditBox(this.font, 0, 0, 0, 0, Component.empty());
        this.line.setMaxLength(50);
        this.setInitialFocus(this.line);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        /* Fliegt das Terminal in die Luft, waehrend man davorsteht, schliesst sich die Ansicht. */
        if(this.terminal.isRemoved()) {
            this.onClose();
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5F, 0.5F, 1F);

        guiGraphics.drawString(this.font, "[Esc] - Quit", 2, 2, 0xffffff, false);
        guiGraphics.drawString(this.font, "chan <channel> - Set selected channel", 2, 12, 0xffffff, false);
        guiGraphics.drawString(this.font, "send <cmd> - Send single signal over selected channel", 2, 22, 0xffffff, false);
        guiGraphics.drawString(this.font, "start <cmd> - Continuously send signal over selected channel", 2, 32, 0xffffff, false);
        guiGraphics.drawString(this.font, "stop - Stop continuous sending", 2, 42, 0xffffff, false);
        guiGraphics.drawString(this.font, "clear - Delete command history", 2, 52, 0xffffff, false);

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if(keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {

            CompoundTag tag = new CompoundTag();
            tag.putString("cmd", this.line.getValue());
            PacketDistributor.sendToServer(new CompoundTagControl(tag, this.terminal.getBlockPos()));

            this.line.setValue("");
            return true;
        }

        /* Pos1, Ende und die Pfeile bleiben aussen vor -- der Schreibstrich sitzt immer hinten. */
        if(keyCode == GLFW.GLFW_KEY_HOME || keyCode == GLFW.GLFW_KEY_END
                || keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) return true;

        if(this.line.keyPressed(keyCode, scanCode, modifiers)) return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(this.line.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        lastTerminal = null;
        super.onClose();
    }

    /** Die gerade getippte Zeile -- der Renderer schreibt sie oben auf die Tafel. */
    public static String getWorkingLine(RBMKTerminalBlockEntity terminal) {

        if(terminal != lastTerminal) return "";

        if(Minecraft.getInstance().screen instanceof RBMKTerminalScreen screen && screen.line != null) {
            return screen.line.getValue();
        }

        return "";
    }
}
