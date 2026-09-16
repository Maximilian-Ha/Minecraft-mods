package com.zuxelus.energycontrol.blockentity;

import com.zuxelus.energycontrol.ECConfig;
import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.IItemCard;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.ITouchAction;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.init.ECBlockEntityTypes;
import com.zuxelus.energycontrol.items.ItemUpgrade;
import com.zuxelus.energycontrol.items.ItemUpgrade.UpgradeType;
import com.zuxelus.energycontrol.items.cards.ItemCardBase;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import com.zuxelus.energycontrol.items.cards.ItemCardText;
import com.zuxelus.energycontrol.menus.InfoPanelMenu;
import com.zuxelus.energycontrol.blocks.InfoPanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.tileentities.TileEntityInfoPanel.
 *
 * Die Informationstafel. Sie liest in festem Takt die Karte in ihrem Fach aus und zeigt
 * deren Zeilen auf ihrer Vorderseite.
 *
 * NICHT UEBERNOMMEN: der Stromverbrauch. Im Original zog die Tafel EU aus einem
 * IC2-Netz und blieb ohne Strom dunkel. IC2 gibt es auf 1.21.1 nicht, und die Tafel an
 * HBMs Stromnetz zu haengen haette den Kern des Mods von HBM abhaengig gemacht -- dann
 * liesse er sich ohne HBM nicht mehr laden. Die Tafel arbeitet deshalb ohne Strom.
 *
 * NICHT UEBERNOMMEN: die Erweiterungen fuer grosse Schirme und der Beruehrungsbetrieb.
 * Siehe docs/ROADMAP.md.
 */
public class InfoPanelBlockEntity extends CardReaderBlockEntity {

    public static final int SLOT_CARD = 0;
    public static final int SLOT_UPGRADE_RANGE = 1;
    public static final int SLOT_UPGRADE_COLOR = 2;
    public static final int SLOT_UPGRADE_TOUCH = 3;

    /** Anfangsfarbe des Hintergrunds: das Gruen der Originaltafel. */
    public static final int DEFAULT_BACKGROUND = 0x00CC00;
    public static final int DEFAULT_TEXT = 0x000000;

    /** Reichweite ohne Aufwertung, in Bloecken. */
    public static final int BASE_RANGE = 8;

    /** Die Taktstufen, die der Knopf in der Oberflaeche durchschaltet. */
    public static final int[] TICK_RATES = { 1, 5, 10, 20, 40, 100, 200 };

    private final Map<String, Integer> displaySettings = new HashMap<>();

    private boolean showLabels = true;
    private int colorBackground = DEFAULT_BACKGROUND;
    private int colorText = DEFAULT_TEXT;
    private int tickRate = ECConfig.infoPanelRefreshPeriod();

    private int updateTicker;

    /** Auf dem Client zwischengespeicherte Zeilen, damit der Renderer nicht je Bild rechnet. */
    private List<PanelString> cachedLines;
    private ItemStack cachedFor = ItemStack.EMPTY;

    /**
     * Die Flaeche, die diese Tafel bespielt -- sie selbst plus ihre Erweiterungen. Wird auf
     * dem Server in jedem Takt neu ermittelt und mit uebertragen; der Renderer braucht sie,
     * und auf dem Client laesst sie sich nicht zuverlaessig selbst ausrechnen (die Nachbarn
     * koennen in einem noch nicht geladenen Chunk liegen).
     */
    private BlockPos screenMin;
    private BlockPos screenMax;

    public InfoPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ECBlockEntityTypes.INFO_PANEL.get(), pos, state, 4);
    }

    // ------------------------------------------------------------------- Takt

    public void tick() {
        if(level == null || level.isClientSide) return;

        tickPower();
        // Ohne Strom bleibt der Schirm leer -- und die Karte wird auch nicht mehr
        // nachgefuehrt, sonst stuende nach dem Einschalten ein alter Messwert da.
        if(!isPowered()) return;

        if(updateTicker-- > 0) return;
        updateTicker = Math.max(1, tickRate) - 1;

        // Nur abgleichen, wenn es etwas zu melden gibt: eine Tafel ohne Karte und ohne
        // Aenderung an der Flaeche haette sonst je Takt ein Paket verschickt.
        boolean changed = updateScreen();
        changed |= updateCards();
        if(changed) sync();
    }

    /**
     * Rechnet die Schirmflaeche neu aus. Das geschieht in jedem Takt statt beim Setzen und
     * Abbauen einzelner Bloecke: eine Suche ueber hoechstens vierundsechzig Bloecke einmal je
     * Sekunde kostet nichts, und dafuer gibt es keinen Zustand, der veralten kann.
     */
    private boolean updateScreen() {
        Direction facing = getBlockState().getValue(InfoPanelBlock.FACING);
        PanelScreens.Screen screen = PanelScreens.around(level, worldPosition, facing);

        if(screen.min().equals(screenMin) && screen.max().equals(screenMax)) return false;

        screenMin = screen.min();
        screenMax = screen.max();
        renderBox = null;
        return true;
    }

    @Override
    protected void onPowerChanged() {
        cachedLines = null;
        // Beim Einschalten sofort messen, nicht erst nach dem naechsten vollen Takt.
        if(isPowered()) updateTicker = 0;
    }

    /**
     * Laesst die Karte ihr Ziel auslesen und schickt das Ergebnis an die Clients. Die
     * Karte schreibt ihre Messwerte in den eigenen Datenspeicher, der mit dem Gegenstand
     * im Fach uebertragen wird -- deshalb reicht ein Blockabgleich, und der Renderer
     * braucht keine eigene Leitung.
     */
    private boolean updateCards() {
        ItemStack stack = getItem(SLOT_CARD);
        if(!ItemCardBase.isCard(stack)) return false;

        ItemCardReader reader = new ItemCardReader(stack);
        applyTitleFromName(stack, reader);

        IItemCard card = (IItemCard) stack.getItem();
        int range = card.isRemoteCard(stack) ? getRange() : -1;

        CardState state;
        try {
            state = card.update(level, reader, range, worldPosition);
        } catch(Exception e) {
            // Eine fremde Karte darf die Tafel nicht mit in den Abgrund reissen.
            state = CardState.CUSTOM_ERROR;
        }
        reader.setState(state);
        cachedLines = null;
        return true;
    }

    /**
     * Die Ueberschrift einer Karte kommt aus ihrem Namen. Im Original gab es dafuer ein
     * Textfeld in der Oberflaeche der Tafel; hier benennt man die Karte im Amboss, und
     * das gilt auch fuer die Textkarte, deren ganzer Zweck eine feste Zeile ist.
     */
    private void applyTitleFromName(ItemStack stack, ItemCardReader reader) {
        if(!stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) return;
        String name = stack.getHoverName().getString();
        if(!name.equals(reader.getTitle())) reader.setTitle(name);
    }

    // ------------------------------------------------------------------ Schirm

    public BlockPos getScreenMin() {
        return screenMin != null ? screenMin : worldPosition;
    }

    public BlockPos getScreenMax() {
        return screenMax != null ? screenMax : worldPosition;
    }

    private AABB renderBox;

    /**
     * Der Renderer zeichnet ueber die ganze Flaeche. Bliebe es beim Standardkasten des
     * einzelnen Blocks, verschwaende die Schrift eines grossen Schirms, sobald der Block mit
     * der Tafel aus dem Bild laeuft.
     */
    @Override
    public AABB getRenderBoundingBox() {
        if(renderBox == null) {
            BlockPos min = getScreenMin();
            BlockPos max = getScreenMax();
            renderBox = new AABB(min.getX(), min.getY(), min.getZ(),
                    max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D);
        }
        return renderBox;
    }

    /** Reichweite in Bloecken: Grundwert plus Aufwertungen im zweiten Fach. */
    public int getRange() {
        ItemStack stack = getItem(SLOT_UPGRADE_RANGE);
        if(!ItemUpgrade.is(stack, UpgradeType.RANGE)) return BASE_RANGE;
        return BASE_RANGE + stack.getCount() * ECConfig.rangeUpgradeRange();
    }

    public boolean hasColorUpgrade() {
        return ItemUpgrade.is(getItem(SLOT_UPGRADE_COLOR), UpgradeType.COLOR);
    }

    // --------------------------------------------------------------- Anzeige

    /** Die Zeilen, die auf dem Schirm stehen -- oder null, wenn keine Karte steckt. */
    public List<PanelString> getPanelStringList(boolean labels) {
        ItemStack stack = getItem(SLOT_CARD);
        if(!ItemCardBase.isCard(stack)) return null;

        if(cachedLines != null && ItemStack.isSameItemSameComponents(cachedFor, stack)) return cachedLines;

        ItemCardReader reader = new ItemCardReader(stack);
        CardState state = reader.getState();

        List<PanelString> lines;
        if(state != CardState.OK && state != CardState.CUSTOM_ERROR) {
            lines = ItemCardReader.getStateMessage(state);
        } else {
            int settings = getDisplaySettings(stack);
            lines = ((IItemCard) stack.getItem()).getStringData(settings, reader, labels);
        }

        cachedLines = lines;
        cachedFor = stack.copy();
        return lines;
    }

    // ---------------------------------------------------------------- Eingaben

    @Override
    public void receiveControl(Player player, CompoundTag tag) {
        switch(tag.getString("action")) {
            case "colorText" -> setColorText(tag.getInt("value"));
            case "colorBackground" -> setColorBackground(tag.getInt("value"));
            case "text" -> writeTextCard(tag);
            default -> { }
        }
    }

    /**
     * Schreibt die eingetippten Zeilen in die Textkarte im Fach. Die Karte leert ihren
     * Speicher beim Messen nicht, die Zeilen bleiben also stehen.
     */
    private void writeTextCard(CompoundTag tag) {
        ItemStack stack = getItem(SLOT_CARD);
        if(!(stack.getItem() instanceof ItemCardText)) return;

        ItemCardReader reader = new ItemCardReader(stack);
        ListTag lines = tag.getList("lines", Tag.TAG_STRING);
        int[] colors = tag.getIntArray("colors");

        for(int i = 0; i < ItemCardText.MAX_LINES; i++) {
            String text = i < lines.size() ? lines.getString(i) : "";
            if(text.isEmpty()) {
                reader.removeField("line" + i);
                reader.removeField("color" + i);
                continue;
            }
            reader.setString("line" + i, text);
            reader.setInt("color" + i, i < colors.length ? colors[i] : 0);
        }

        cachedLines = null;
        sync();
    }

    /**
     * Ein Rechtsklick auf den Schirm. Traegt die Tafel die Beruehrungsaufwertung und kann die
     * steckende Karte etwas damit anfangen, wirkt der Klick auf das Ziel der Karte.
     *
     * @return ob der Klick verbraucht wurde
     */
    public boolean tryTouch(Player player) {
        if(level == null || level.isClientSide) return false;
        if(!isPowered()) return false;
        if(!ItemUpgrade.is(getItem(SLOT_UPGRADE_TOUCH), UpgradeType.TOUCH)) return false;

        ItemStack stack = getItem(SLOT_CARD);
        if(!ItemCardBase.isCard(stack)) return false;
        if(!(stack.getItem() instanceof ITouchAction touch) || !touch.enableTouch(stack)) return false;

        ItemCardReader reader = new ItemCardReader(stack);
        if(!touch.runTouchAction(level, player, reader, stack)) return false;

        // Sofort nachmessen, damit der Schirm den neuen Zustand zeigt und nicht den alten.
        updateTicker = 0;
        return true;
    }

    // ---------------------------------------------------------- Einstellungen

    private static String key(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public int getDisplaySettings(ItemStack stack) {
        if(!ItemCardBase.isCard(stack)) return 0;
        Integer value = displaySettings.get(key(stack));
        return value != null ? value : ((IItemCard) stack.getItem()).getDefaultSettings();
    }

    private void setColorText(int value) {
        if(!hasColorUpgrade()) return;
        colorText = value & 0xFFFFFF;
        sync();
    }

    private void setColorBackground(int value) {
        if(!hasColorUpgrade()) return;
        colorBackground = value & 0xFFFFFF;
        sync();
    }

    public void setDisplaySettings(ItemStack stack, int value) {
        if(!ItemCardBase.isCard(stack)) return;
        displaySettings.put(key(stack), value);
        cachedLines = null;
        sync();
    }

    /** Schaltet ein Ankreuzfeld der steckenden Karte um. */
    public void toggleSetting(int bit) {
        ItemStack stack = getItem(SLOT_CARD);
        if(!ItemCardBase.isCard(stack)) return;
        setDisplaySettings(stack, getDisplaySettings(stack) ^ bit);
    }

    public List<PanelSetting> getSettingsList() {
        ItemStack stack = getItem(SLOT_CARD);
        if(!ItemCardBase.isCard(stack)) return new ArrayList<>();
        return ((IItemCard) stack.getItem()).getSettingsList(stack);
    }

    public boolean getShowLabels() {
        return showLabels;
    }

    public void toggleShowLabels() {
        showLabels = !showLabels;
        cachedLines = null;
        sync();
    }

    public int getTickRate() {
        return tickRate;
    }

    /** Schaltet auf die naechste Taktstufe weiter. */
    public void cycleTickRate() {
        int index = 0;
        for(int i = 0; i < TICK_RATES.length; i++) {
            if(TICK_RATES[i] == tickRate) {
                index = i;
                break;
            }
        }
        tickRate = TICK_RATES[(index + 1) % TICK_RATES.length];
        updateTicker = 0;
        sync();
    }

    public int getColorBackground() {
        return colorBackground;
    }

    public int getColorText() {
        return colorText;
    }

    /** Die Farbleiste des Originals: die sechzehn Farben der Wolle, in derselben Folge. */
    public static final int[] COLORS = {
            0x000000, 0xFFFFFF, 0xCCCCCC, 0x999999, 0x00CC00, 0x00FF00, 0x00FFFF, 0x0000FF,
            0x7F00FF, 0xFF00FF, 0xFF0000, 0xFF7F00, 0xFFFF00, 0x7F3F00, 0x007F00, 0x00007F
    };

    // ------------------------------------------------------------- Speichern

    @Override
    protected void readProperties(CompoundTag tag, HolderLookup.Provider registries) {
        super.readProperties(tag, registries);
        showLabels = !tag.contains("showLabels") || tag.getBoolean("showLabels");
        colorBackground = tag.contains("colorBackground") ? tag.getInt("colorBackground") : DEFAULT_BACKGROUND;
        colorText = tag.getInt("colorText");
        tickRate = tag.contains("tickRate") ? Math.max(1, tag.getInt("tickRate")) : ECConfig.infoPanelRefreshPeriod();

        displaySettings.clear();
        CompoundTag settings = tag.getCompound("displaySettings");
        for(String name : settings.getAllKeys()) displaySettings.put(name, settings.getInt(name));

        int[] screen = tag.getIntArray("screen");
        if(screen.length == 6) {
            screenMin = new BlockPos(screen[0], screen[1], screen[2]);
            screenMax = new BlockPos(screen[3], screen[4], screen[5]);
        } else {
            screenMin = null;
            screenMax = null;
        }
        renderBox = null;

        cachedLines = null;
    }

    @Override
    protected void writeProperties(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeProperties(tag, registries);
        tag.putBoolean("showLabels", showLabels);
        tag.putInt("colorBackground", colorBackground);
        tag.putInt("colorText", colorText);
        tag.putInt("tickRate", tickRate);

        CompoundTag settings = new CompoundTag();
        displaySettings.forEach(settings::putInt);
        tag.put("displaySettings", settings);

        BlockPos min = getScreenMin();
        BlockPos max = getScreenMax();
        tag.putIntArray("screen", new int[] {
                min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ() });
    }

    // -------------------------------------------------------------- Oberflaeche

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.energycontrol.info_panel");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new InfoPanelMenu(id, inventory, this);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch(slot) {
            case SLOT_CARD -> ItemCardBase.isCard(stack);
            case SLOT_UPGRADE_RANGE -> ItemUpgrade.is(stack, UpgradeType.RANGE);
            case SLOT_UPGRADE_COLOR -> ItemUpgrade.is(stack, UpgradeType.COLOR);
            case SLOT_UPGRADE_TOUCH -> ItemUpgrade.is(stack, UpgradeType.TOUCH);
            default -> false;
        };
    }

    @Override
    public void setChanged() {
        super.setChanged();
        cachedLines = null;
    }
}
