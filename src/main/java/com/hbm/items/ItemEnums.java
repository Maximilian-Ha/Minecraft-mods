package com.hbm.items;

public class ItemEnums {

    public enum CasingType {
        SMALL, LARGE, SMALL_STEEL, LARGE_STEEL, SHOTSHELL, BUCKSHOT, BUCKSHOT_ADVANCED
    }

    public enum CapType {
        NUKA, QUANTUM, SPARKLE, RAD, KORL, FRITZ
    }

    /**
     * Die drei Stufen der Legendenteile. Sie sind Zutat, nicht Werkzeug: die zweite Stufe
     * steckt in der Brustplatte der Remnant-Panzerruestung. Untereinander lassen sie sich in
     * beide Richtungen tauschen -- drei der einen ergeben eine der naechsten und umgekehrt.
     */
    public enum LegendaryType {
        TIER1, TIER2, TIER3
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

    /**
     * Die vier Brocken. Der erste ist die allgemeine Seltenheit, die drei anderen sind
     * Mineralien. Die Reihenfolge ist die des Originals und ist der Metadatenwert.
     */
    public enum ChunkType {
        RARE, MALACHITE, CRYOLITE, MOONSTONE
    }

    /**
     * Die Geheimstuecke. Fuenf Sachen, die es nicht zu bauen gibt -- sie liegen in den
     * Bauwerken, im Sockel des Skeletthalters, in Beutetoepfen. Im Original tragen sie
     * setCreativeTab(null) und stehen auf der NEI-Ausschlussliste: man soll nicht sehen,
     * wozu sie gut sind, bevor man eines in der Hand hat.
     *
     * Die Reihenfolge ist die des Originals und darf nicht wandern -- sie ist der
     * Metadatenwert, auf den Rezepte und der Dungeon-Spawner zeigen.
     */
    public enum SecretType {
        CANISTER, CONTROLLER, SELENIUM_STEEL, ABERRATOR, FOLLY
    }
}
