package fr.frinn.custommachinery.apiimpl;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.utils.ICMConfig;
import fr.frinn.custommachinery.common.config.CMConfigImpl;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.CMLogger;
import fr.frinn.custommachinery.common.util.RegistryCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.Logger;

public class CustomMachineryAPI implements ICustomMachineryAPI {

    @Override
    public String modid() {
        return CustomMachinery.MODID;
    }

    @Override
    public ResourceLocation rl(String path) {
        return ResourceLocation.tryParse(modid() + ":" + path);
    }

    @Override
    public Logger logger() {
        return CMLogger.INSTANCE;
    }

    @Override
    public ICMConfig config() {
        return CMConfigImpl.INSTANCE;
    }

    @Override
    public IForgeRegistry<MachineComponentType<?>> componentRegistry() {
        return Registration.MACHINE_COMPONENT_TYPE_REGISTRY.get();
    }

    @Override
    public IForgeRegistry<GuiElementType<?>> guiElementRegistry() {
        return Registration.GUI_ELEMENT_TYPE_REGISTRY.get();
    }

    @Override
    public IForgeRegistry<RequirementType<?>> requirementRegistry() {
        return Registration.REQUIREMENT_TYPE_REGISTRY.get();
    }

    @Override
    public IForgeRegistry<MachineAppearanceProperty<?>> appearancePropertyRegistry() {
        return Registration.APPEARANCE_PROPERTY_REGISTRY.get();
    }

    @Override
    public IForgeRegistry<DataType<?, ?>> dataRegistry() {
        return Registration.DATA_REGISTRY.get();
    }

    @Override
    public <V extends IForgeRegistryEntry<V>> Codec<V> registryCodec(IForgeRegistry<V> registry, boolean isCM) {
        return RegistryCodec.of(registry, isCM);
    }
}
