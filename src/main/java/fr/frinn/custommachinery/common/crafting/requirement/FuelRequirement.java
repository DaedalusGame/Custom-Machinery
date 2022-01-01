package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.wrapper.FuelItemIngredientWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;

public class FuelRequirement extends AbstractRequirement<FuelMachineComponent> implements ITickableRequirement<FuelMachineComponent>, IJEIIngredientRequirement<ItemStack> {

    public static final Codec<FuelRequirement> CODEC = RecordCodecBuilder.create(fuelRequirementInstance ->
            fuelRequirementInstance.group(
                    CodecLogger.loggedOptional(Codec.intRange(0, Integer.MAX_VALUE),"amount", 1).forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codec.BOOL,"jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(fuelRequirementInstance, (amount, jei) -> {
                    FuelRequirement requirement = new FuelRequirement(amount);
                    //requirement.setJeiVisible(jei);
                    return requirement;
            })
    );

    private final int amount;
    private boolean jeiVisible = true;
    private final FuelItemIngredientWrapper wrapper;

    public FuelRequirement(int amount) {
        super(RequirementIOMode.INPUT);
        this.amount = amount;
        this.wrapper = new FuelItemIngredientWrapper(this.amount);
    }

    public int getAmount() {
        return this.amount;
    }

    @Override
    public RequirementType<FuelRequirement> getType() {
        return Registration.FUEL_REQUIREMENT.get();
    }

    @Override
    public boolean test(FuelMachineComponent component, ICraftingContext context) {
        return true;
    }

    @Override
    public CraftingResult processStart(FuelMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(FuelMachineComponent component, ICraftingContext context) {
        if(component.burn(this.amount))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fuel.error"));
    }

    @Override
    public CraftingResult processEnd(FuelMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<FuelMachineComponent> getComponentType() {
        return Registration.FUEL_MACHINE_COMPONENT.get();
    }

    /*
    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.setVisible(this.jeiVisible)
                .addTooltip(new TranslationTextComponent("custommachinery.requirements.fuel.info"))
                .setItemIcon(Items.COAL);
    }*/

    @Override
    public IJEIIngredientWrapper<ItemStack> getJEIIngredientWrapper() {
        return this.wrapper;
    }
}