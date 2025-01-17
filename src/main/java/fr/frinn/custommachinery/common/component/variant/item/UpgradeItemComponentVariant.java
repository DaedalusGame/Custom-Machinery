package fr.frinn.custommachinery.common.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.apiimpl.component.variant.ItemComponentVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class UpgradeItemComponentVariant extends ItemComponentVariant {

    public static final UpgradeItemComponentVariant INSTANCE = new UpgradeItemComponentVariant();
    private static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "upgrade");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public boolean isItemValid(IMachineComponentManager manager, ItemStack stack) {
        return CustomMachinery.UPGRADES.stream().anyMatch(upgrade -> upgrade.getItem() == stack.getItem() && upgrade.getMachines().contains(manager.getTile().getMachine().getId()));
    }
}
