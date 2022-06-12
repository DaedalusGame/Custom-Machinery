package fr.frinn.custommachinery.common.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.codec.CodecLogger;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractDelayedRequirement;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.common.component.BlockMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.ingredient.BlockIngredient;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BlockRequirement extends AbstractDelayedRequirement<BlockMachineComponent> implements ITickableRequirement<BlockMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<BlockRequirement> CODEC = RecordCodecBuilder.create(blockRequirementInstance ->
            blockRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    Codecs.BLOCK_REQUIREMENT_ACTION_CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    Codecs.BOX_CODEC.fieldOf("pos").forGetter(requirement -> requirement.pos),
                    CodecLogger.loggedOptional(Codec.INT,"amount", 1).forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codecs.COMPARATOR_MODE_CODEC,"comparator", ComparatorMode.GREATER_OR_EQUALS).forGetter(requirement -> requirement.comparator),
                    CodecLogger.loggedOptional(Codecs.PARTIAL_BLOCK_STATE_CODEC, "block", PartialBlockState.AIR).forGetter(requirement -> requirement.block),
                    CodecLogger.loggedOptional(Codecs.list(IIngredient.BLOCK), "filter", Collections.emptyList()).forGetter(requirement -> requirement.filter),
                    CodecLogger.loggedOptional(Codec.BOOL, "whitelist", false).forGetter(requirement -> requirement.whitelist),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0D, 1.0D), "delay", 0.0D).forGetter(requirement -> requirement.delay)
            ).apply(blockRequirementInstance, (mode, action, pos, amount, comparator, block, filter, whitelist, delay) -> {
                    BlockRequirement requirement = new BlockRequirement(mode, action, pos, amount, comparator, block, filter, whitelist);
                    requirement.setDelay(delay);
                    return requirement;
            })
    );

    private final ACTION action;
    private final AABB pos;
    private final int amount;
    private final ComparatorMode comparator;
    private final PartialBlockState block;
    private final List<IIngredient<PartialBlockState>> filter;
    private final boolean whitelist;
    private double delay;

    public BlockRequirement(RequirementIOMode mode, ACTION action, AABB pos, int amount, ComparatorMode comparator, PartialBlockState block, List<IIngredient<PartialBlockState>> filter, boolean whitelist) {
        super(mode);
        this.action = action;
        this.pos = pos;
        this.amount = amount;
        this.comparator = comparator;
        this.block = block;
        this.filter = filter;
        this.whitelist = whitelist;
    }

    @Override
    public RequirementType<?> getType() {
        return Registration.BLOCK_REQUIREMENT.get();
    }

    @Override
    public boolean test(BlockMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        switch (this.action) {
            case CHECK:
                return this.comparator.compare((int)component.getBlockAmount(this.pos, this.filter, this.whitelist), amount);
            case PLACE:
                return  this.delay != 0 || (int)component.getBlockAmount(this.pos, Collections.singletonList(BlockIngredient.AIR), true) >= amount;
            case BREAK:
            case DESTROY:
            case REPLACE_BREAK:
            case REPLACE_DESTROY:
                return this.delay != 0 || (int)component.getBlockAmount(this.pos, this.filter, this.whitelist) >= amount;
            default:
                return true;
        }
    }

    @Override
    public CraftingResult processStart(BlockMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(this.getMode() == RequirementIOMode.INPUT && this.delay == 0) {
            switch (this.action) {
                case PLACE:
                    if(component.placeBlock(this.pos, this.block, amount))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case REPLACE_BREAK:
                    if(component.replaceBlock(this.pos, this.block, amount, true, this.filter, this.whitelist))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case REPLACE_DESTROY:
                    if(component.replaceBlock(this.pos, this.block, amount, false, this.filter, this.whitelist))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case BREAK:
                    if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
                case DESTROY:
                    if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(BlockMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(this.getMode() == RequirementIOMode.OUTPUT && this.delay == 0) {
            switch (this.action) {
                case PLACE:
                    if(component.placeBlock(this.pos, this.block, amount))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case REPLACE_BREAK:
                    if(component.replaceBlock(this.pos, this.block, amount, true, this.filter, this.whitelist))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case REPLACE_DESTROY:
                    if(component.replaceBlock(this.pos, this.block, amount, false, this.filter, this.whitelist))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case BREAK:
                    if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
                case DESTROY:
                    if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<BlockMachineComponent> getComponentType() {
        return Registration.BLOCK_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(BlockMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getPerTickModifiedValue(this.amount, this, null);
        if(this.action == ACTION.CHECK) {
            long found = component.getBlockAmount(this.pos, this.filter, this.whitelist);
            if(!this.comparator.compare((int)found, amount))
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.check.error", amount, this.pos.toString(), found));
            return CraftingResult.success();
        }
        return CraftingResult.pass();
    }

    @Override
    public void setDelay(double delay) {
        this.delay = Mth.clamp(delay, 0.0, 1.0);
    }

    @Override
    public double getDelay() {
        return this.delay;
    }

    @Override
    public CraftingResult execute(BlockMachineComponent component, ICraftingContext context) {
        switch (this.action) {
            case PLACE:
                if(component.placeBlock(this.pos, this.block, amount))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
            case REPLACE_BREAK:
                if(component.replaceBlock(this.pos, this.block, amount, true, this.filter, this.whitelist))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
            case REPLACE_DESTROY:
                if(component.replaceBlock(this.pos, this.block, amount, false, this.filter, this.whitelist))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
            case BREAK:
                if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, true))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
            case DESTROY:
                if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, false))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
        }
        return CraftingResult.pass();
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        MutableComponent action = null;
        switch (this.action) {
            case CHECK:
                action = new TranslatableComponent("custommachinery.requirements.block.check.info");
                break;
            case BREAK:
                if(this.getMode() == RequirementIOMode.INPUT)
                    action = new TranslatableComponent("custommachinery.requirements.block.break.info.input");
                else
                    action = new TranslatableComponent("custommachinery.requirements.block.break.info.output");
                break;
            case DESTROY:
                if(this.getMode() == RequirementIOMode.INPUT)
                    action = new TranslatableComponent("custommachinery.requirements.block.destroy.info.input");
                else
                    action = new TranslatableComponent("custommachinery.requirements.block.destroy.info.output");
                break;
            case PLACE:
                if(this.getMode() == RequirementIOMode.INPUT)
                    action = new TranslatableComponent("custommachinery.requirements.block.place.info.input", this.amount, this.block.getName());
                else
                    action = new TranslatableComponent("custommachinery.requirements.block.place.info.output", this.amount, this.block.getName());
                break;
            case REPLACE_BREAK:
            case REPLACE_DESTROY:
                if(this.getMode() == RequirementIOMode.INPUT)
                    action = new TranslatableComponent("custommachinery.requirements.block.replace.info.input", this.amount, this.block.getName());
                else
                    action = new TranslatableComponent("custommachinery.requirements.block.replace.info.output", this.amount, this.block.getName());
                break;
        }
        if(action != null)
            info.addTooltip(action.withStyle(ChatFormatting.AQUA));
        if(this.action != ACTION.PLACE) {
            if(this.action != ACTION.CHECK)
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.block." + (this.whitelist ? "allowed" : "denied")).withStyle(this.whitelist ? ChatFormatting.GREEN : ChatFormatting.RED));
            if(this.whitelist && this.filter.isEmpty())
                info.addTooltip(new TextComponent("-").append(new TranslatableComponent("custommachinery.requirements.block.none")));
            else if(!this.whitelist && this.filter.isEmpty())
                info.addTooltip(new TextComponent("-").append(new TranslatableComponent("custommachinery.requirements.block.all")));
            else
                this.filter.forEach(block -> info.addTooltip(new TextComponent("- " + block.toString())));
        }
        info.addTooltip(new TranslatableComponent("custommachinery.requirements.block.info.box").withStyle(ChatFormatting.GOLD));
        info.setClickAction((machine, mouseButton) -> CustomMachineRenderer.addRenderBox(machine.getId(), this.pos));
        info.setItemIcon(Items.GRASS_BLOCK);
    }

    public enum ACTION {
        CHECK,
        BREAK,
        DESTROY,
        PLACE,
        REPLACE_BREAK,
        REPLACE_DESTROY;

        public static ACTION value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }
    }
}