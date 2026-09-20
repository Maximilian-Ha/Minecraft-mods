package com.hbm.world;

import com.hbm.blockentity.PedestalBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.RedBrickBlock;
import com.hbm.blocks.states.BrickFace;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsRedRoom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Portiert aus 1.7.10: die generateRoom-Methoden von BlockKeyhole und BlockRedBrickKeyhole.
 *
 * DAS ROTE ZIMMER. Neun mal neun Bloecke, fuenf hoch, aus rotem Ziegel; in der Mitte ein
 * Sockel mit einem Beutestueck. Beide Schluessellochbloecke heben dasselbe Zimmer aus, und
 * darum steht es hier einmal statt zweimal -- im Original ist der Erzeuger in beiden
 * Klassen abgeschrieben, mit ein paar Unterschieden, die unten stehen.
 *
 * DIE UNTERSCHIEDE ZWISCHEN DEN BEIDEN, gemessen und nicht geglaettet:
 *
 *   STEINERNES SCHLUESSELLOCH -- die Waende tragen das Ziegelbild nach innen (Metadaten 2
 *   bis 5, je nach Wand), Boden und Decke ebenso (eins und null). Dazu Fackeln, mit je
 *   einem Viertel Wahrscheinlichkeit Spinnweben, Saeulen, Feuer, ein Kreis und Lava, und
 *   mit einem Zwanzigstel eine Beutekiste statt des Sockels.
 *
 *   ZIEGEL-SCHLUESSELLOCH -- alles Metadatum sechs, also ringsum schlichter Untergrund, kein
 *   Schmuck, dafuer bis zu fuenf Sockel: einer in der Mitte mit der Tontafel und vier
 *   ringsum, jeder mit halber Wahrscheinlichkeit. Und die Wand, durch die man eintritt,
 *   bleibt offen.
 *
 * NICHT UEBERNOMMEN: die Errungenschaft achRedRoom, die das Original dem Spieler zuschreibt.
 * Der Port hat kein Errungenschaftssystem -- dasselbe Loch wie bei statLegendary am Sockel.
 */
public final class RedRoomGenerator {

    private RedRoomGenerator() { }

    public static final int SIZE = 9;
    public static final int HEIGHT = 5;
    private static final int WIDTH = SIZE / 2;

    /** Das geschmueckte Zimmer des steinernen Schluessellochs. */
    public static void generateStoneRoom(Level level, BlockPos mitte) {

        schale(level, mitte, true, null);
        fackeln(level, mitte);

        if(level.random.nextInt(4) == 0) spinnweben(level, mitte);
        if(level.random.nextInt(4) == 0) saeulen(level, mitte);
        if(level.random.nextInt(4) == 0) feuer(level, mitte);
        if(level.random.nextInt(4) == 0) kreis(level, mitte);
        if(level.random.nextInt(4) == 0) lava(level, mitte);

        /* Einer von zwanzig: eine Beutekiste statt des Sockels. Der Inhalt sind die beiden
         * Panzerruestungssaetze -- mit einem Fuenftel die NCRPA, sonst der Grabenmeister. */
        if(level.random.nextInt(20) == 0) {
            beutekiste(level, mitte.above());
        } else {
            sockelMitBeute(level, mitte.above(), ItemPoolsRedRoom.POOL_RED_PEDESTAL);
        }

        raeumeAbgeworfenesAuf(level, mitte);
    }

    /** Das schmucklose Zimmer des Ziegel-Schluessellochs, mit bis zu fuenf Sockeln. */
    public static void generateBrickRoom(Level level, BlockPos mitte, Direction eingang) {

        schale(level, mitte, false, eingang);

        sockelMitBeute(level, mitte.above(), ItemPoolsRedRoom.POOL_BLACK_SLAB);
        if(level.random.nextBoolean()) sockelMitBeute(level, mitte.above().east(2), ItemPoolsRedRoom.POOL_BLACK_PART);
        if(level.random.nextBoolean()) sockelMitBeute(level, mitte.above().west(2), ItemPoolsRedRoom.POOL_BLACK_PART);
        if(level.random.nextBoolean()) sockelMitBeute(level, mitte.above().south(2), ItemPoolsRedRoom.POOL_BLACK_PART);
        if(level.random.nextBoolean()) sockelMitBeute(level, mitte.above().north(2), ItemPoolsRedRoom.POOL_BLACK_PART);

        raeumeAbgeworfenesAuf(level, mitte);
    }

    /**
     * Die Huelle: Kanten, Waende, Boden, Decke, und der Innenraum wird leergeraeumt.
     *
     * @param gemustert ob die Innenflaechen das Ziegelbild nach innen tragen sollen
     * @param eingang   die Wand, die offen bleibt -- null heisst: alle vier zumauern
     */
    private static void schale(Level level, BlockPos mitte, boolean gemustert, Direction eingang) {

        for(int i = -WIDTH; i <= WIDTH; i++) {
            for(int y : new int[] { 0, HEIGHT - 1 }) {
                setzeZiegel(level, mitte.offset(i, y, WIDTH), BrickFace.NONE);
                setzeZiegel(level, mitte.offset(i, y, -WIDTH), BrickFace.NONE);
                setzeZiegel(level, mitte.offset(WIDTH, y, i), BrickFace.NONE);
                setzeZiegel(level, mitte.offset(-WIDTH, y, i), BrickFace.NONE);
            }
        }

        for(int i = 1; i <= HEIGHT - 2; i++) {
            setzeZiegel(level, mitte.offset(WIDTH, i, WIDTH), BrickFace.NONE);
            setzeZiegel(level, mitte.offset(WIDTH, i, -WIDTH), BrickFace.NONE);
            setzeZiegel(level, mitte.offset(-WIDTH, i, WIDTH), BrickFace.NONE);
            setzeZiegel(level, mitte.offset(-WIDTH, i, -WIDTH), BrickFace.NONE);

            for(int j = -WIDTH + 1; j <= WIDTH - 1; j++) {
                if(eingang != Direction.EAST)  setzeZiegel(level, mitte.offset(WIDTH, i, j), gemustert ? BrickFace.WEST : BrickFace.NONE);
                if(eingang != Direction.WEST)  setzeZiegel(level, mitte.offset(-WIDTH, i, j), gemustert ? BrickFace.EAST : BrickFace.NONE);
                if(eingang != Direction.SOUTH) setzeZiegel(level, mitte.offset(j, i, WIDTH), gemustert ? BrickFace.NORTH : BrickFace.NONE);
                if(eingang != Direction.NORTH) setzeZiegel(level, mitte.offset(j, i, -WIDTH), gemustert ? BrickFace.SOUTH : BrickFace.NONE);
            }
        }

        /* Mit einem Viertel ein zweites Schluesselloch in einer der vier Waende -- von dort
         * geht es weiter. Nur das steinerne Zimmer hat das. */
        if(gemustert) {
            switch(level.random.nextInt(4)) {
            case 0 -> setzeSchluesselziegel(level, mitte.offset(WIDTH, 2, 0), BrickFace.WEST);
            case 1 -> setzeSchluesselziegel(level, mitte.offset(-WIDTH, 2, 0), BrickFace.EAST);
            case 2 -> setzeSchluesselziegel(level, mitte.offset(0, 2, WIDTH), BrickFace.NORTH);
            default -> setzeSchluesselziegel(level, mitte.offset(0, 2, -WIDTH), BrickFace.SOUTH);
            }
        }

        for(int i = -WIDTH + 1; i <= WIDTH - 1; i++) {
            for(int j = -WIDTH + 1; j <= WIDTH - 1; j++) {
                setzeZiegel(level, mitte.offset(i, 0, j), gemustert ? BrickFace.UP : BrickFace.NONE);
                setzeZiegel(level, mitte.offset(i, HEIGHT - 1, j), gemustert ? BrickFace.DOWN : BrickFace.NONE);

                for(int k = 1; k <= HEIGHT - 2; k++) {
                    level.setBlock(mitte.offset(i, k, j), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void fackeln(Level level, BlockPos mitte) {
        int weit = WIDTH - 1;
        int nah = weit - 1;
        BlockState fackel = Blocks.TORCH.defaultBlockState();
        for(int[] p : new int[][] { {weit, nah}, {weit, -nah}, {-weit, nah}, {-weit, -nah},
                                    {nah, weit}, {-nah, weit}, {nah, -weit}, {-nah, -weit} }) {
            level.setBlock(mitte.offset(p[0], 2, p[1]), fackel, 3);
        }
    }

    private static void spinnweben(Level level, BlockPos mitte) {
        for(int i = -WIDTH + 1; i <= WIDTH - 1; i++) {
            for(int j = -WIDTH + 1; j <= WIDTH - 1; j++) {
                if(level.random.nextBoolean()) level.setBlock(mitte.offset(i, HEIGHT - 2, j), Blocks.COBWEB.defaultBlockState(), 3);
            }
        }
    }

    /**
     * Die Saeulen. Das Original nimmt dafuer concrete_colored mit Metadatum 14 -- seinen
     * eigenen roten Farbbeton.
     *
     * DEN GIBT ES IM PORT NICHT, und ein sechzehnfarbiger Betonblock waere eine eigene
     * Runde. Hier steht darum Minecrafts rotes Beton: dieselbe Farbe, dieselbe Rolle im
     * Zimmer, nur ein anderes Bild. Kommt concrete_colored spaeter nach, gehoeren diese
     * beiden Stellen (Saeulen und Kreis) berichtigt.
     */
    private static void saeulen(Level level, BlockPos mitte) {
        BlockState beton = Blocks.RED_CONCRETE.defaultBlockState();
        int d = WIDTH - 2;
        for(int i = 1; i <= HEIGHT - 2; i++) {
            level.setBlock(mitte.offset(d, i, d), beton, 3);
            level.setBlock(mitte.offset(d, i, -d), beton, 3);
            level.setBlock(mitte.offset(-d, i, d), beton, 3);
            level.setBlock(mitte.offset(-d, i, -d), beton, 3);
        }
    }

    private static void feuer(Level level, BlockPos mitte) {
        int d = WIDTH - 1;
        for(int[] p : new int[][] { {d, d}, {d, -d}, {-d, d}, {-d, -d} }) {
            level.setBlock(mitte.offset(p[0], 0, p[1]), Blocks.NETHERRACK.defaultBlockState(), 3);
            level.setBlock(mitte.offset(p[0], 1, p[1]), Blocks.FIRE.defaultBlockState(), 3);
        }
    }

    /** Der Kreis um den Sockel; dasselbe Ersatzmaterial wie bei den Saeulen, siehe dort. */
    private static void kreis(Level level, BlockPos mitte) {
        BlockState beton = Blocks.RED_CONCRETE.defaultBlockState();
        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                if(i != 0 || j != 0) level.setBlock(mitte.offset(i, 0, j), beton, 3);
            }
        }
    }

    private static void lava(Level level, BlockPos mitte) {
        BlockState lava = Blocks.LAVA.defaultBlockState();
        for(int d : new int[] { WIDTH - 2, WIDTH - 3 }) {
            for(int rand : new int[] { WIDTH - 1, -WIDTH + 1 }) {
                level.setBlock(mitte.offset(d, 0, rand), lava, 3);
                level.setBlock(mitte.offset(-d, 0, rand), lava, 3);
                level.setBlock(mitte.offset(rand, 0, d), lava, 3);
                level.setBlock(mitte.offset(rand, 0, -d), lava, 3);
            }
        }
    }

    private static void setzeZiegel(Level level, BlockPos pos, BrickFace face) {
        level.setBlock(pos, NtmBlocks.BRICK_RED.get().defaultBlockState().setValue(RedBrickBlock.FACE, face), 3);
    }

    private static void setzeSchluesselziegel(Level level, BlockPos pos, BrickFace face) {
        level.setBlock(pos, NtmBlocks.STONE_KEYHOLE_META.get().defaultBlockState().setValue(RedBrickBlock.FACE, face), 3);
    }

    /** Ein Sockel mit einem Zug aus dem genannten Vorrat darauf. */
    private static void sockelMitBeute(Level level, BlockPos pos, String vorrat) {

        level.setBlock(pos, NtmBlocks.PEDESTAL.get().defaultBlockState(), 3);

        ItemPool pool = ItemPool.get(vorrat);
        if(pool == null) return;

        ItemStack beute = pool.draw(level.random);
        if(beute.isEmpty()) return;

        if(level.getBlockEntity(pos) instanceof PedestalBlockEntity sockel) {
            sockel.item = beute;
            sockel.setChanged();
        }
    }

    private static void beutekiste(Level level, BlockPos pos) {
        level.setBlock(pos, NtmBlocks.DECO_LOOT.get().defaultBlockState(), 3);
        ItemPoolsRedRoom.fuelleBeutekiste(level, pos);
    }

    /**
     * Was beim Ausheben herausgefallen ist, verschwindet wieder -- sonst liegt der halbe
     * Berg als Gegenstand im neuen Zimmer.
     */
    private static void raeumeAbgeworfenesAuf(Level level, BlockPos mitte) {
        AABB raum = new AABB(mitte.getX() + 0.5, mitte.getY(), mitte.getZ() + 0.5,
                             mitte.getX() + 0.5, mitte.getY() + HEIGHT, mitte.getZ() + 0.5)
                .inflate(SIZE / 2D, 0, SIZE / 2D);
        List<ItemEntity> gegenstaende = level.getEntitiesOfClass(ItemEntity.class, raum);
        for(ItemEntity gegenstand : gegenstaende) gegenstand.discard();
    }
}
