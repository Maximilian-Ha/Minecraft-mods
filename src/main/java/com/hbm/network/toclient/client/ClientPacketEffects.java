package com.hbm.network.toclient.client;

import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.main.NuclearTechModClient;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.network.toclient.ParticleBurst;
import com.hbm.network.toclient.VanillaExplosionLike;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Was die Pakete an den Client beim Empfang AUSFUEHREN, liegt hier und nicht bei ihnen selbst.
 *
 * Der Grund ist der dedizierte Server. Er registriert dieselben Pakete wie der Client -- sonst
 * koennte er sie nicht senden --, und dabei laedt er die Paketklassen. Stuende Minecraft oder
 * ClientLevel in einer ihrer Methoden, liefe die Pruefung der Klasse darauf und der Dist-Cleaner
 * braeche den Start ab. Die Methode einfach mit @OnlyIn(Dist.CLIENT) zu kennzeichnen geht hier
 * NICHT: NtmNetwork verweist mit einer Methodenreferenz darauf, und die wird beim Registrieren
 * aufgeloest -- auf dem Server waere sie dann weg (NoSuchMethodError).
 *
 * Also bleibt handleClient dort stehen, ruft aber nur noch hierher. Der Aufruf steht in einem
 * Lambda und nennt ausschliesslich gemeinsame Typen; damit braucht die Pruefung diese Klasse
 * nicht zu laden. Geladen wird sie erst, wenn wirklich jemand das Lambda ausfuehrt -- und das
 * tut nur der Client.
 */
@OnlyIn(Dist.CLIENT)
public class ClientPacketEffects {

    public static void vanillaExplosion(VanillaExplosionLike packet) {
        ExplosionEffectStandard.performClient(Minecraft.getInstance().level,
                packet.x(), packet.y(), packet.z(), packet.size(), packet.affectedBlocks());
    }

    public static void auxParticle(AuxParticle packet) {
        Minecraft mc = Minecraft.getInstance();
        packet.nbt().putDouble("posX", packet.x());
        packet.nbt().putDouble("posY", packet.y());
        packet.nbt().putDouble("posZ", packet.z());
        if(mc.level != null) NuclearTechModClient.effectNT(packet.nbt());
    }

    public static void particleBurst(ParticleBurst packet) {
        BlockState state = packet.block().defaultBlockState();
        Minecraft.getInstance().particleEngine.destroy(packet.pos(), state);
    }
}
