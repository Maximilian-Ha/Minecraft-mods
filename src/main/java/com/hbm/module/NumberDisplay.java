package com.hbm.module;

import com.hbm.util.BobMathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Portiert aus 1.7.10: com.hbm.module.NumberDisplay (urspruenglich von UFFR).
 *
 * Eine Siebensegmentanzeige fuer Oberflaechen. Jede Ziffer besteht aus drei waagerechten und
 * vier senkrechten Balken; Laenge, Dicke und Abstand lassen sich einstellen, ebenso Farbe,
 * Stellenzahl, Nachkommastellen, fuehrende Nullen und Blinken.
 *
 * ABWEICHUNGEN:
 * - Das Original haelt eine Referenz auf die Oberflaeche und holt sich daraus laufend
 *   guiLeft/guiTop/zLevel. In 1.21 ist GuiGraphics nur waehrend des Zeichnens gueltig, darum
 *   bekommt die Anzeige beides beim Zeichnen uebergeben statt beim Anlegen.
 * - Die Balken zeichnet das Original mit dem Tessellator, hier uebernimmt das GuiGraphics.fill.
 * - Ein Fehler des Originals ist BEHOBEN: drawPeriod rechnet die senkrechte Lage des Punktes
 *   aus guiLeft statt aus guiTop. Der Punkt saesse damit irgendwo auf dem Bildschirm.
 */
public class NumberDisplay {

    /** Lage der Anzeige, bezogen auf die linke obere Ecke der Oberflaeche. */
    private final int displayX;
    private final int displayY;

    private int color;
    /** Abstand zwischen zwei Ziffern. */
    private int padding = 3;
    private boolean blink = false;

    private float maxNum;
    private float minNum;
    private boolean customBounds = false;

    private boolean isFloat = false;
    /** Wie viele Nachkommastellen. */
    private int floatPad = 1;
    /** Ob links mit Nullen aufgefuellt wird. */
    private boolean pads = false;
    /** Hoechstzahl der Stellen. */
    private int digitLength = 3;

    private Number numIn = 0;
    private char[] toDisp = { '0', '0', '0' };

    /** Verschiebung der gerade gezeichneten Ziffer gegenueber der ersten. */
    private int dispOffset = 0;

    private int verticalLength = 5;
    private int horizontalLength = 4;
    private int thickness = 1;

    public NumberDisplay(int x, int y) {
        this.displayX = x;
        this.displayY = y;
        this.color = 0xFFFF55;
    }

    public NumberDisplay(int x, int y, int color) {
        this(x, y);
        this.color = color;
    }

    public NumberDisplay(int x, int y, ChatFormatting c) {
        this(x, y);
        this.color = enumToColor(c);
    }

    /** Die Farbe hinter einem Formatierungszeichen. Unbekanntes wird gelb, wie im Original. */
    private static int enumToColor(ChatFormatting c) {

        if(c == null || !c.isColor()) return 0xFFFF55;

        return switch(c) {
            case BLACK -> 0x000000;
            case DARK_BLUE -> 0x0000AA;
            case DARK_GREEN -> 0x00AA00;
            case DARK_AQUA -> 0x00AAAA;
            case DARK_RED -> 0xAA0000;
            case DARK_PURPLE -> 0xAA00AA;
            case GOLD -> 0xFFAA00;
            case GRAY -> 0xAAAAAA;
            case DARK_GRAY -> 0x555555;
            case BLUE -> 0x5555FF;
            case GREEN -> 0x55FF55;
            case AQUA -> 0x55FFFF;
            case RED -> 0xFF5555;
            case LIGHT_PURPLE -> 0xFF55FF;
            case WHITE -> 0xFFFFFF;
            default -> 0xFFFF55;
        };
    }

    public void setColor(int color) { this.color = color; }

    /* Zeichnen */

    /** Zeichnet eine selbst zusammengestellte Ziffernfolge. */
    public void drawNumber(GuiGraphics guiGraphics, int guiLeft, int guiTop, char[] num) {

        if(this.blink && !BobMathUtil.getBlink()) return;

        int gap = this.digitLength - num.length;

        for(int i = 0; i < num.length; i++) {
            // Der Dezimalpunkt zaehlt nicht als Stelle, er sitzt zwischen zweien.
            if(num[i] == '.') gap--;
            this.dispOffset = (this.padding + this.horizontalLength + 2 * this.thickness) * (i + gap);
            this.drawChar(guiGraphics, guiLeft, guiTop, num[i]);
        }

        if(this.pads) this.padOut(guiGraphics, guiLeft, guiTop, gap);
    }

    /** Zeichnet die zuletzt gesetzte Zahl. */
    public void drawNumber(GuiGraphics guiGraphics, int guiLeft, int guiTop) {
        if(this.isFloat) this.formatForFloat();
        this.drawNumber(guiGraphics, guiLeft, guiTop, this.toDisp);
    }

    public void drawNumber(GuiGraphics guiGraphics, int guiLeft, int guiTop, Number num) {
        this.setNumber(num);
        this.drawNumber(guiGraphics, guiLeft, guiTop);
    }

    private void padOut(GuiGraphics guiGraphics, int guiLeft, int guiTop, int gap) {

        if(gap <= 0) return;

        for(int i = 0; i < gap; i++) {
            this.dispOffset = (this.padding + this.horizontalLength + 2 * this.thickness) * i;
            this.drawChar(guiGraphics, guiLeft, guiTop, '0');
        }
    }

    /** Zeichnet ein einzelnes Zeichen. dispOffset muss vorher stehen. */
    public void drawChar(GuiGraphics g, int gl, int gt, char num) {

        switch(num) {
            case '1' -> { vert(g, gl, gt, 1,0); vert(g, gl, gt, 1,1); }
            case '2' -> { horz(g, gl, gt, 0); vert(g, gl, gt, 1,0); horz(g, gl, gt, 1); vert(g, gl, gt, 0,1); horz(g, gl, gt, 2); }
            case '3' -> { horz(g, gl, gt, 0); horz(g, gl, gt, 1); horz(g, gl, gt, 2); vert(g, gl, gt, 1,0); vert(g, gl, gt, 1,1); }
            case '4' -> { vert(g, gl, gt, 0,0); vert(g, gl, gt, 1,0); vert(g, gl, gt, 1,1); horz(g, gl, gt, 1); }
            case '5' -> { horz(g, gl, gt, 0); horz(g, gl, gt, 1); horz(g, gl, gt, 2); vert(g, gl, gt, 0,0); vert(g, gl, gt, 1,1); }
            case '6' -> { horz(g, gl, gt, 0); horz(g, gl, gt, 1); horz(g, gl, gt, 2); vert(g, gl, gt, 0,0); vert(g, gl, gt, 0,1); vert(g, gl, gt, 1,1); }
            case '7' -> { horz(g, gl, gt, 0); vert(g, gl, gt, 1,0); vert(g, gl, gt, 1,1); }
            case '8' -> { horz(g, gl, gt, 0); horz(g, gl, gt, 1); horz(g, gl, gt, 2); vert(g, gl, gt, 0,0); vert(g, gl, gt, 1,0); vert(g, gl, gt, 0,1); vert(g, gl, gt, 1,1); }
            case '9' -> { horz(g, gl, gt, 0); horz(g, gl, gt, 1); horz(g, gl, gt, 2); vert(g, gl, gt, 0,0); vert(g, gl, gt, 1,0); vert(g, gl, gt, 1,1); }
            case '0' -> { horz(g, gl, gt, 0); horz(g, gl, gt, 2); vert(g, gl, gt, 0,0); vert(g, gl, gt, 0,1); vert(g, gl, gt, 1,0); vert(g, gl, gt, 1,1); }
            case '-' -> horz(g, gl, gt, 1);
            case '.' -> period(g, gl, gt);
            // 'E' und alles Unbekannte wird zum Fehlerzeichen, so wie im Original.
            default -> { horz(g, gl, gt, 0); horz(g, gl, gt, 1); horz(g, gl, gt, 2); vert(g, gl, gt, 0, 0); vert(g, gl, gt, 0, 1); }
        }
    }
    /** Ein waagerechter Balken: 0 oben, 1 in der Mitte, 2 unten. */
    private void horz(GuiGraphics g, int gl, int gt, int pos) {
        int offset = pos * (this.verticalLength + this.thickness);
        this.segment(g, gl + this.displayX + this.dispOffset + this.thickness, gt + this.displayY + offset,
                this.horizontalLength, this.thickness);
    }

    /** Ein senkrechter Balken: posX 0 links / 1 rechts, posY 0 oben / 1 unten. */
    private void vert(GuiGraphics g, int gl, int gt, int posX, int posY) {
        int offsetX = posX * (this.horizontalLength + this.thickness);
        int offsetY = posY * (this.verticalLength + this.thickness);
        this.segment(g, gl + this.displayX + offsetX + this.dispOffset, gt + this.displayY + offsetY + this.thickness,
                this.thickness, this.verticalLength);
    }

    private void period(GuiGraphics g, int gl, int gt) {
        this.segment(g,
                gl + this.displayX + this.dispOffset + this.padding - (int) Math.ceil(this.padding / 2D) + (this.horizontalLength + this.thickness),
                gt + this.displayY + 2 * (this.verticalLength + this.thickness),
                this.thickness, this.thickness);
    }

    private void segment(GuiGraphics g, int x, int y, int width, int height) {
        g.fill(x, y, x + width, y + height, 0xFF000000 | this.color);
    }

    /* Zahl setzen und formatieren */

    public void setNumber(Number num) {

        this.numIn = num;
        if(this.customBounds) this.numIn = Mth.clamp(num.doubleValue(), this.minNum, this.maxNum);

        if(this.isFloat) {
            this.formatForFloat();
        } else {
            this.toDisp = Long.toString(Math.round(this.numIn.doubleValue())).toCharArray();
            this.toDisp = this.truncOrExpand();
        }
    }

    public Number getNumber() { return this.numIn; }

    public char[] getDispNumber() { return this.toDisp.clone(); }

    public NumberDisplay setBlinks(boolean doesBlink) { this.blink = doesBlink; return this; }

    public NumberDisplay setPadding(int p) { this.padding = p; return this; }

    public NumberDisplay setDigitLength(int l) {
        this.digitLength = l;
        this.toDisp = this.truncOrExpand();
        return this;
    }

    public NumberDisplay setSegmentSize(int vertical, int horizontal, int thickness) {
        this.verticalLength = vertical;
        this.horizontalLength = horizontal;
        this.thickness = thickness;
        return this;
    }

    public NumberDisplay setMaxMin(float max, float min) {
        if(min > max) throw new IllegalArgumentException("Minimum value is larger than maximum value!");
        this.maxNum = max;
        this.minNum = min;
        this.customBounds = true;
        return this;
    }

    /** Fuellt links mit Nullen auf. */
    public NumberDisplay setPadNumber() { this.pads = true; return this; }

    public NumberDisplay setFloat() { return this.setFloat(1); }

    public NumberDisplay setFloat(int pad) {
        this.floatPad = pad;
        this.isFloat = true;
        this.formatForFloat();
        return this;
    }

    private void formatForFloat() {

        BigDecimal bd = new BigDecimal(this.numIn.toString()).setScale(this.floatPad, RoundingMode.HALF_UP);
        char[] proc = bd.toPlainString().toCharArray();

        this.toDisp = proc.length == this.digitLength ? proc : this.truncOrExpand();
    }

    /**
     * Schneidet die Ziffernfolge auf die Stellenzahl zu. Arrays.copyOf fuellt mit dem
     * Nullzeichen auf; das wird hier zur Ziffer null, so wie im Original.
     */
    private char[] truncOrExpand() {

        if(!this.isFloat) return this.toDisp;

        char[] out = Arrays.copyOf(this.toDisp, this.digitLength);
        for(int i = 0; i < this.digitLength; i++) if(out[i] == CHAR_NUL) out[i] = '0';

        return out;
    }

    /** Das Fuellzeichen von Arrays.copyOf. */
    private static final char CHAR_NUL = (char) 0;
}
