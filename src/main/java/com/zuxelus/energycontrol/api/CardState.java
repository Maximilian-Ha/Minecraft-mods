package com.zuxelus.energycontrol.api;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.api.CardState.
 *
 * Rueckgabewert von {@link IItemCard#update}. Nur bei OK und CUSTOM_ERROR fragt die Tafel
 * die Karte anschliessend nach Zeilen; sonst zeigt sie die Standardmeldung zum Zustand.
 */
public enum CardState {
    OK(1),
    NO_TARGET(2),
    OUT_OF_RANGE(3),
    INVALID_CARD(4),
    CUSTOM_ERROR(5);

    private final int index;

    CardState(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static CardState fromInteger(int value) {
        return switch(value) {
            case 1 -> OK;
            case 2 -> NO_TARGET;
            case 3 -> OUT_OF_RANGE;
            case 4 -> INVALID_CARD;
            default -> CUSTOM_ERROR;
        };
    }
}
