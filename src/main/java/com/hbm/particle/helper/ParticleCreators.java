package com.hbm.particle.helper;

import java.util.HashMap;

public class ParticleCreators {
    public static HashMap<String, IParticleCreator> particleCreators = new HashMap<>();

    static {
        particleCreators.put("explosionLarge", new ExplosionCreator());
        particleCreators.put("casingNT", new CasingCreator());
        particleCreators.put("flamethrower", new FlameCreator());
        particleCreators.put("explosionSmall", new ExplosionSmallCreator());
//        particleCreators.put("blackPowder", new BlackPowderCreator());
        particleCreators.put("ashes", new AshesCreator());
        particleCreators.put("skeleton", new SkeletonCreator());
        particleCreators.put("nuke", new NukeTorexCreator());
        particleCreators.put("cloud", new CloudCreator());
        // Kein Partikel, sondern ein Rahmen mit Beschriftung -- haengt aber am selben Verteiler.
        particleCreators.put("marker", new MarkerCreator());
        particleCreators.put("foundry", new FoundryCreator());
    }
}
