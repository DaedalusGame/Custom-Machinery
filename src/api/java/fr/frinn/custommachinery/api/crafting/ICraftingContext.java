package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.requirement.IRequirement;

import javax.annotation.Nullable;

/**
 * Provide various information about the actual crafting process, like the current IMachineRecipe or the MachineTile executing this recipe.
 * This is passed to each IRequirement of the IMachineRecipe when they are executed.
 */
public interface ICraftingContext {

    /**
     * @return The MachineTile currently processing the recipe.
     */
    MachineTile getMachineTile();

    /**
     * @return The IMachineRecipe currently processed by the machine.
     */
    IMachineRecipe getRecipe();

    /**
     * This time is usually in ticks, but may vary depending on what is returned by {@link ICraftingContext#getModifiedSpeed} return.
     * @return The remaining time before the end of the crafting process.
     */
    double getRemainingTime();

    /**
     * By default, the recipe processing speed is 1 per tick, but can be speeded up or slowed down if the machine have some upgrades modifiers.
     * @return The speed of the crafting process.
     */
    double getModifiedSpeed();


    /**
     * Used to apply all currently active machine upgrades to an IRequirement value.
     * @param value The value to modify (example an amount of item, energy etc...).
     * @param requirement The requirement the value depends, because machine upgrades can target a specific RequirementType.
     * @param target The name of the value to modify, or null, because machine upgrades can target a specific value of a requirement.
     * @return The modified value, or the same value if no upgrades could be applied.
     */
    double getModifiedValue(double value, IRequirement<?> requirement, @Nullable String target);

    /**
     * Used to apply all currently active machine upgrades to an ITickableRequirement value.
     * Use this method only for requirements that will be executed every tick of the crafting process.
     * @param value The value to modify (example an amount of item, energy etc...).
     * @param requirement The requirement the value depends, because machine upgrades can target a specific RequirementType.
     * @param target The name of the value to modify, or null, because machine upgrades can target a specific value of a requirement.
     * @return The modified value, or the same value if no upgrades could be applied.
     */
    double getPerTickModifiedValue(double value, IRequirement<?> requirement, @Nullable String target);
}
