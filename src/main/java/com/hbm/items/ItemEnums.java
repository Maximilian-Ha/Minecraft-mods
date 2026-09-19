package com.hbm.items;

public class ItemEnums {

    public enum CasingType {
        SMALL, LARGE, SMALL_STEEL, LARGE_STEEL, SHOTSHELL, BUCKSHOT, BUCKSHOT_ADVANCED
    }

    public enum CapType {
        NUKA, QUANTUM, SPARKLE, RAD, KORL, FRITZ
    }

    /**
     * Die Spielarten von ingot_u238m2. Nur die erste ist ein Barren; die drei uebrigen sind
     * Ostereier, die der bruechige Spaten aus der Erde der Bauwerke holt. Im Original sind es
     * die Metadaten eins bis drei desselben Gegenstands (ItemUnstable mit drei Ersatzbildern
     * hs-elements, hs-arsenic, hs-vault), und diese Zuordnung bleibt hier erhalten.
     */
    public enum U238M2Type {
        U238M2, ELEMENTS, ARSENIC, VAULT
    }
}
