package fr.frinn.custommachinery.common.integration.buildinggadgets;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.registry.TopologicalRegistryBuilder;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.TileDataSerializerReference;
import com.google.common.base.Suppliers;
import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BuildingGadgetsIntegration {

    public static final DeferredRegister<ITileDataSerializer> TILE_SERIALIZERS = DeferredRegister.create(TileDataSerializerReference.REGISTRY_ID_TILE_DATA_SERIALIZER, CustomMachinery.MODID);

    public static final RegistryObject<ITileDataSerializer> MACHINE_TILE_DATA_SERIALIZER = TILE_SERIALIZERS.register("custom_machine_tile", CustomMachineTileDataSerializer::new);

    public static void init(final IEventBus MOD_BUS) {
        TILE_SERIALIZERS.register(MOD_BUS);
    }

    public static void sendIMC() {
        TopologicalRegistryBuilder<ITileDataFactory> factory = TopologicalRegistryBuilder.create();
        factory.addValue(new ResourceLocation(CustomMachinery.MODID, "custom_machine_data_factory"), new CustomMachineTileDataFactory());
        InterModComms.sendTo("buildinggadgets", "imc_tile_data_factory", () -> Suppliers.ofInstance(factory));
    }
}
