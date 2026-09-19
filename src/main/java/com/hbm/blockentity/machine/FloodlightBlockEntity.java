package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.FloodlightBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: Floodlight.TileEntityFloodlight.
 *
 * Das Flutlicht zieht Strom aus einem Kabel hinter sich und wirft fuenfzehn Strahlen nach
 * vorn -- fuenf Hoehenlagen zu je drei Seitenlagen. Wo ein Strahl auf etwas Undurchsichtiges
 * trifft, setzt er einen Lichtfleck davor.
 *
 * Die Strahlen werden nicht alle auf einmal nachgezogen, sondern reihum einer alle fuenf
 * Ticks -- so wie im Original. Erst beim Einschalten werden alle fuenfzehn gesetzt.
 */
public class FloodlightBlockEntity extends LoadedBaseBlockEntity implements ITickable, IEnergyReceiverMK2 {

    public static final long MAX_POWER = 5_000;

    /** Verbrauch je Tick, aus dem Original. */
    private static final long VERBRAUCH = 100;

    /** Fuenf Hoehenlagen zu je drei Seitenlagen. */
    private static final int STRAHLEN = 15;

    public float neigung;
    public long power;
    public boolean brennt;

    private int pause;
    private final BlockPos[] lichtOrte = new BlockPos[STRAHLEN];

    private AABB sichtkasten;

    public FloodlightBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FLOODLIGHT.get(), pos, state);
    }

    public boolean brennt() { return this.brennt; }

    public BlockPos lichtOrt(int nummer) {
        return nummer >= 0 && nummer < this.lichtOrte.length ? this.lichtOrte[nummer] : null;
    }

    /** Neue Neigung: die alten Lichtflecke passen dann nicht mehr. */
    public void setzeNeigung(float neigung) {
        this.neigung = neigung;
        if(this.brennt) this.loescheLichter();
        this.setChanged();
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            // FACING ist die angeklickte Seite; der Strom kommt aus dem Block dahinter.
            Direction rueck = this.getBlockState().getValue(FloodlightBlock.FACING).getOpposite();
            this.trySubscribe(this.level, this.worldPosition.relative(rueck), rueck);

            if(this.pause > 0) {
                this.pause--;
            } else if(this.power >= VERBRAUCH) {
                this.power -= VERBRAUCH;

                if(!this.brennt) {
                    this.brennt = true;
                    this.setzeLichter();
                    this.setChanged();
                } else if(this.level.getGameTime() % 5 == 0) {
                    this.setzeLicht((int) Math.abs(this.level.getGameTime() / 5 % STRAHLEN));
                }

            } else if(this.brennt) {
                this.brennt = false;
                this.pause = 60;
                this.loescheLichter();
                this.setChanged();
            }
        }

        this.networkPackNT(50);
    }

    private void setzeLichter() {
        for(int i = 0; i < STRAHLEN; i++) this.setzeLicht(i);
    }

    private void loescheLichter() {
        for(int i = 0; i < STRAHLEN; i++) {
            BlockPos ort = this.lichtOrte[i];
            if(ort != null && this.level.getBlockState(ort).is(NtmBlocks.FLOODLIGHT_BEAM.get())) {
                this.level.setBlock(ort, Blocks.AIR.defaultBlockState(), 2);
            }
            this.lichtOrte[i] = null;
        }
    }

    private void setzeLicht(int nummer) {

        BlockPos neu = this.endpunkt(nummer);
        BlockPos alt = this.lichtOrte[nummer];
        this.lichtOrte[nummer] = null;

        // Der alte Fleck geht nur weg, wenn er wirklich von diesem Flutlicht stammt.
        if(alt != null && !alt.equals(neu)
                && this.level.getBlockEntity(alt) instanceof FloodlightBeamBlockEntity) {
            this.level.setBlock(alt, Blocks.AIR.defaultBlockState(), 2);
        }

        if(neu == null) return;

        BlockState dort = this.level.getBlockState(neu);

        if(dort.isAir()) {
            this.level.setBlock(neu, NtmBlocks.FLOODLIGHT_BEAM.get().defaultBlockState(), 2);
            if(this.level.getBlockEntity(neu) instanceof FloodlightBeamBlockEntity fleck) {
                fleck.setzeQuelle(this, nummer);
            }
            this.lichtOrte[nummer] = neu;

        } else if(dort.is(NtmBlocks.FLOODLIGHT_BEAM.get())) {
            this.lichtOrte[nummer] = neu;
        }
    }

    /**
     * Die Streuung des Strahls: fuenf Hoehenlagen im Abstand von 7,5 Grad, drei Seitenlagen
     * im Abstand von 15 Grad -- genau die Formel des Originals.
     */
    private static float[] streuung(int nummer) {
        return new float[] {
                (((nummer / 3) - 2) * 7.5F) / 180F * (float) Math.PI,
                (((nummer % 3) - 1) * 15F) / 180F * (float) Math.PI
        };
    }

    /**
     * Wohin dieser Strahl zeigt und wo er endet: der letzte freie Platz vor dem ersten
     * undurchsichtigen Block, hoechstens vierundsechzig weit.
     */
    private BlockPos endpunkt(int nummer) {

        if(nummer < 0 || nummer >= STRAHLEN) return null;

        BlockState zustand = this.getBlockState();
        Direction seite = zustand.getValue(FloodlightBlock.FACING);
        boolean gedreht = zustand.getValue(FloodlightBlock.FLIPPED);

        float[] lagen = streuung(nummer);

        float neigung = this.neigung;
        if(seite == Direction.UP || (seite == Direction.DOWN && gedreht)) neigung = 180 - neigung;

        Vec3 richtung = new Vec3(1, 0, 0);
        richtung = dreheUmZ(richtung, (float) (neigung / 180D * Math.PI) + lagen[0]);

        // Die Grundausrichtung: das Original zaehlt in Metadaten, hier in Richtungen.
        double viertel = switch(seite) {
            case DOWN -> gedreht ? Math.PI / 2D : 0D;
            case UP -> gedreht ? Math.PI / 2D : 0D;
            case NORTH -> Math.PI / 2D;
            case SOUTH -> -Math.PI / 2D;
            case WEST -> Math.PI;
            case EAST -> 0D;
        };
        richtung = dreheUmY(richtung, (float) viertel);
        richtung = dreheUmY(richtung, lagen[1]);

        for(int i = 1; i < 64; i++) {
            int x = (int) Math.floor(this.worldPosition.getX() + 0.5 + richtung.x * i);
            int y = (int) Math.floor(this.worldPosition.getY() + 0.5 + richtung.y * i);
            int z = (int) Math.floor(this.worldPosition.getZ() + 0.5 + richtung.z * i);
            BlockPos hier = new BlockPos(x, y, z);

            if(hier.equals(this.worldPosition)) continue;
            if(!this.level.isLoaded(hier)) return null;
            // Das Original laesst alles durch, was weniger als halb undurchsichtig ist
            // (Lichtundurchlaessigkeit < 127 von 255). In 1.21 zaehlt dieselbe Groesse von
            // 0 bis 15, und nur ein voll deckender Block kommt auf 15.
            if(this.level.getBlockState(hier).getLightBlock(this.level, hier) < 15) continue;

            if(i > 1) {
                return new BlockPos(
                        (int) Math.floor(this.worldPosition.getX() + 0.5 + richtung.x * (i - 1)),
                        (int) Math.floor(this.worldPosition.getY() + 0.5 + richtung.y * (i - 1)),
                        (int) Math.floor(this.worldPosition.getZ() + 0.5 + richtung.z * (i - 1)));
            }
            return null;
        }

        return null;
    }

    private static Vec3 dreheUmZ(Vec3 v, float winkel) {
        float c = (float) Math.cos(winkel);
        float s = (float) Math.sin(winkel);
        return new Vec3(v.x * c + v.y * s, v.y * c - v.x * s, v.z);
    }

    private static Vec3 dreheUmY(Vec3 v, float winkel) {
        float c = (float) Math.cos(winkel);
        float s = (float) Math.sin(winkel);
        return new Vec3(v.x * c + v.z * s, v.y, v.z * c - v.x * s);
    }

    @Override
    public void setRemoved() {
        if(this.level != null && !this.level.isClientSide) this.loescheLichter();
        super.setRemoved();
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeFloat(this.neigung);
        buf.writeBoolean(this.brennt);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.neigung = buf.readFloat();
        this.brennt = buf.readBoolean();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.neigung = tag.getFloat("neigung");
        this.power = tag.getLong("power");
        this.brennt = tag.getBoolean("brennt");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("neigung", this.neigung);
        tag.putLong("power", this.power);
        tag.putBoolean("brennt", this.brennt);
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    /* Ohne @Override: getRenderBoundingBox kommt aus der NeoForge-Erweiterung von
     * BlockEntity und gilt dem Uebersetzer nicht als ueberschrieben. */
    public AABB getRenderBoundingBox() {
        if(this.sichtkasten == null) {
            this.sichtkasten = new AABB(this.worldPosition).inflate(1);
        }
        return this.sichtkasten;
    }
}
