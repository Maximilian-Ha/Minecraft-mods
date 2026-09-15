package com.hbm.inventory.recipes;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutput;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutputMulti;
import com.hbm.items.NtmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.RockMillRecipes.
 */
public class RockMillRecipes extends GenericRecipes<GenericRecipe> {

    public static final RockMillRecipes INSTANCE = new RockMillRecipes();

    /** Ersatz fuer den Ore-Dict-Schluessel "cobblestone" aus 1.7.10 */
    public static final TagKey<Item> COBBLESTONES = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "cobblestones"));

    @Override public int inputItemLimit() { return 3; }
    @Override public int inputFluidLimit() { return 1; }
    @Override public int outputItemLimit() { return 3; }
    @Override public int outputFluidLimit() { return 1; }

    @Override public String getFileName() { return "hbmRockMill.json"; }
    @Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

    @Override
    public void registerDefaults() {
        if(!this.recipeOrderedList.isEmpty()) return;

        int consumption = 25;
        int duraShort = 100;
        int duraLong = 200;

        String groupCrush = "autoswitch.crushing";

        this.register(new GenericRecipe("rock.cobble").setup(duraShort, consumption).setNameWrapper("rock.crushing")
                .inputItems(new TagStack(COBBLESTONES))
                .inputFluids(new FluidStack(Fluids.WATER, 250))
                .outputFluids(new FluidStack(Fluids.COLLOID, 250))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(new ItemStack(Blocks.GRAVEL), 95),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_QUARTZ.get()), 5)
                )).setIconToFirstIngredient().setGroup(groupCrush, INSTANCE));

        this.register(new GenericRecipe("rock.gravel").setup(duraShort, consumption).setNameWrapper("rock.crushing")
                .inputItems(new ComparableStack(Blocks.GRAVEL))
                .inputFluids(new FluidStack(Fluids.WATER, 250))
                .outputFluids(new FluidStack(Fluids.COLLOID, 250))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(new ItemStack(Blocks.SAND), 75),
                        new ChanceOutput(new ItemStack(Items.FLINT), 20),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_BORON.get()), 5)
                )).setIconToFirstIngredient().setGroup(groupCrush, INSTANCE));

        this.register(new GenericRecipe("rock.sand").setup(duraShort, consumption).setNameWrapper("rock.crushing")
                .inputItems(new TagStack(ItemTags.SAND))
                .inputFluids(new FluidStack(Fluids.WATER, 250))
                .outputFluids(new FluidStack(Fluids.COLLOID, 250))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(new ItemStack(NtmItems.DUST.get()), 90),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_CALCIUM.get()), 5),
                        new ChanceOutput(new ItemStack(NtmItems.FLUORITE.get()), 5)
                )).setIconToFirstIngredient().setGroup(groupCrush, INSTANCE));

        this.register(new GenericRecipe("rock.netherrack").setup(duraShort, consumption).setNameWrapper("rock.crushing")
                .inputItems(new ComparableStack(Blocks.NETHERRACK))
                .inputFluids(new FluidStack(Fluids.WATER, 250))
                .outputFluids(new FluidStack(Fluids.LAVA, 100))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(new ItemStack(Blocks.GRAVEL), 50),
                        new ChanceOutput(new ItemStack(Blocks.SOUL_SAND), 25),
                        new ChanceOutput(new ItemStack(Items.GLOWSTONE_DUST), 15),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_QUARTZ.get()), 10)
                )).setIconToFirstIngredient().setGroup(groupCrush, INSTANCE));

        this.register(new GenericRecipe("rock.soulsand").setup(duraShort, consumption).setNameWrapper("rock.crushing")
                .inputItems(new ComparableStack(Blocks.SOUL_SAND))
                .inputFluids(new FluidStack(Fluids.WATER, 250))
                .outputFluids(new FluidStack(Fluids.LAVA, 100))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(new ItemStack(Blocks.SAND), 50),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_FIRE.get()), 25),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_URANIUM.get()), 15),
                        new ChanceOutput(new ItemStack(Items.BLAZE_POWDER), 5),
                        new ChanceOutput(new ItemStack(Items.NETHER_WART), 5)
                )).setIconToFirstIngredient().setGroup(groupCrush, INSTANCE));

        this.register(new GenericRecipe("rock.schist").setup(duraLong, consumption).setNameWrapper("rock.crushing")
                .inputItems(new ComparableStack(NtmBlocks.STONE_GNEISS.get()))
                .inputFluids(new FluidStack(Fluids.WATER, 250))
                .outputFluids(new FluidStack(Fluids.COLLOID, 250))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(new ItemStack(Blocks.GRAVEL), 50),
                        new ChanceOutput(new ItemStack(Blocks.SAND), 10),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_LITHIUM.get()), 25),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_NIOBIUM.get()), 5),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_URANIUM.get()), 5),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_GOLD.get()), 5)
                )).setIconToFirstIngredient().setGroup(groupCrush, INSTANCE));

        this.register(new GenericRecipe("rock.hematite").setup(duraLong, consumption).setNameWrapper("rock.crushing")
                .inputItems(new ComparableStack(NtmBlocks.RESOURCE_HEMATITE.get()))
                .inputFluids(new FluidStack(Fluids.WATER, 250))
                .outputFluids(new FluidStack(Fluids.COLLOID, 250))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(new ItemStack(Blocks.GRAVEL), 65),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_IRON.get()), 25),
                        new ChanceOutput(new ItemStack(NtmItems.POWDER_TITANIUM.get()), 10)
                )).setIconToFirstIngredient().setGroup(groupCrush, INSTANCE));

        this.register(new GenericRecipe("rock.bauxite").setup(duraLong, consumption).setNameWrapper("rock.crushing")
                .inputItems(new ComparableStack(NtmBlocks.RESOURCE_BAUXITE.get()))
                .inputFluids(new FluidStack(Fluids.WATER, 250))
                .outputFluids(new FluidStack(Fluids.COLLOID, 250))
                .outputItems(new ChanceOutputMulti(
                        new ChanceOutput(new ItemStack(Blocks.GRAVEL), 25),
                        new ChanceOutput(new ItemStack(Items.CLAY_BALL), 25),
                        // stone_resource:2 aus 1.7.10 ist der Haematit-Stein
                        new ChanceOutput(new ItemStack(NtmBlocks.RESOURCE_HEMATITE.get()), 25),
                        new ChanceOutput(new ItemStack(NtmBlocks.ORE_TITANIUM.get()), 25)
                )).setIconToFirstIngredient().setGroup(groupCrush, INSTANCE));

        this.register(new GenericRecipe("rock.clay").setup(duraLong, consumption)
                .inputItems(new TagStack(ItemTags.SAND, 2))
                .inputFluids(new FluidStack(Fluids.COLLOID, 2_500))
                .outputItems(new ItemStack(Items.CLAY_BALL, 4)));
    }
}
