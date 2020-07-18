package io.github.vampirestudios.molecularcraft.recipes.molecularinfo;

import io.github.vampirestudios.molecularcraft.molecules.MoleculeStack;
import io.github.vampirestudios.molecularcraft.registries.ItemMolecules;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class CraftingMolecularInfoSetter extends AbstractMolecularInfoSetter {
    public CraftingMolecularInfoSetter(RecipeType recipeType, Identifier identifier) {
        super(recipeType, identifier);
    }

    @Override
    public void setMolecularInfo(MinecraftServer minecraftServer) {
        RecipeManager recipeManager = minecraftServer.getRecipeManager();
        List<String> list = new ArrayList<>();
        System.out.println("Parsing recipe to set molecular composition of outputs");
        for (Recipe recipe : recipeManager.values()) {
            if (recipe.getType() == this.getRecipeType() && (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)) {
                ItemStack output = recipe.getOutput();
                DefaultedList<Ingredient> inputs = recipe.getPreviewInputs();
                ItemMolecules itemMolecule = new ItemMolecules();
                boolean incomplete = false;
                for (Ingredient ingredient : inputs) {
                    for (ItemStack input : ingredient.getMatchingStacksClient()) {
                        Identifier identifier = Registry.ITEM.getId(input.getItem());
                        if (ItemMolecules.registry.containsKey(identifier.toString())) {
                            ItemMolecules itemMolecules = ItemMolecules.registry.get(identifier.toString());
                            itemMolecule.addMoleculeStacks(itemMolecules.getList());
                        } else {
                            incomplete = true;
                            list.add(recipe.getId().toString());
                        }
                        break;
                    }
                    if (incomplete) break;
                }
                if (incomplete) continue;
                ItemMolecules itemMolecule1 = new ItemMolecules();
                for (MoleculeStack stack : itemMolecule.getList()) {
                    itemMolecule1.addMoleculeStack(stack.setAmount(stack.getAmount()/output.getCount()));
                }
                for (MoleculeStack stack : itemMolecule1.getList()) {
                    if (stack.getAmount() == 0) {
                        System.out.println("---------");
                        System.out.println("Recipe id : " + recipe.getId().toString());
                        System.out.println("Error, null amount of molecule stack : " + stack.getFormula());
                        System.out.println("---------");
                    }
                }
                if (!itemMolecule1.getList().isEmpty()) {
                    if (!ItemMolecules.registry.containsKey(Registry.ITEM.getId(output.getItem()).toString())) {
                        ItemMolecules.register(Registry.ITEM.getId(output.getItem()).toString(), itemMolecule1);
                    }
                } else {
                    if (!ItemMolecules.registry.containsKey(Registry.ITEM.getId(output.getItem()).toString())) {
                        list.add(recipe.getId().toString());
                    }
                }
            }
        }
        System.out.println("Parsing remaining recipes");
        while (!list.isEmpty()) {
            List<String> toRemove = new ArrayList<>();
            for (String id : list) {
                Recipe recipe = recipeManager.get(new Identifier(id)).get();
                ItemStack output = recipe.getOutput();
                DefaultedList<Ingredient> inputs = recipe.getPreviewInputs();
                ItemMolecules itemMolecule = new ItemMolecules();
                boolean incomplete = false;
                for (Ingredient ingredient : inputs) {
                    for (ItemStack input : ingredient.getMatchingStacksClient()) {
                        Identifier identifier = Registry.ITEM.getId(input.getItem());
                        if (ItemMolecules.registry.containsKey(identifier.toString())) {
                            ItemMolecules itemMolecules = ItemMolecules.registry.get(identifier.toString());
                            itemMolecule.addMoleculeStacks(itemMolecules.getList());
                        } else {
                            incomplete = true;
                        }
                        break;
                    }
                    if (incomplete) break;
                }
                if (incomplete) continue;
                ItemMolecules itemMolecule1 = new ItemMolecules();
                for (MoleculeStack stack : itemMolecule.getList()) {
                    itemMolecule1.addMoleculeStack(stack.setAmount(stack.getAmount()/output.getCount()));
                }
                for (MoleculeStack stack : itemMolecule1.getList()) {
                    if (stack.getAmount() == 0) {
                        System.out.println("---------");
                        System.out.println("Recipe id : " + recipe.getId().toString());
                        System.out.println("Error, null amount of molecule stack : " + stack.getFormula());
                        System.out.println("---------");
                    }
                }
                if (!itemMolecule1.getList().isEmpty()) {
                    toRemove.add(id);
                    ItemMolecules.register(Registry.ITEM.getId(output.getItem()).toString(), itemMolecule1);
                }
            }
            for (String id : toRemove)
                list.remove(id);
            if (toRemove.isEmpty()) break;
        }
        System.out.println("Could not define molecular composition from those recipes:");
        for (String id : list) {
            Recipe recipe = recipeManager.get(new Identifier(id)).get();
            ItemStack output = recipe.getOutput();
            DefaultedList<Ingredient> inputs = recipe.getPreviewInputs();
            StringBuilder inputbuilder = new StringBuilder();
            for (Ingredient ingredient : inputs) {
                for (ItemStack input : ingredient.getMatchingStacksClient()) {
                    Identifier identifier = Registry.ITEM.getId(input.getItem());
                    inputbuilder.append(identifier.toString()).append("\n");
                    break;
                }
            }
            String builder = "____________\n" +
                    "Recipe ID: " + id + "\n" +
                    "  \n" +
                    "Output: " + Registry.ITEM.getId(output.getItem()).toString() + "\n" +
                    "  \nInputs:\n" +
                    inputbuilder.toString() +
                    "\n___________";
            System.out.println(builder);
        }
    }
}
