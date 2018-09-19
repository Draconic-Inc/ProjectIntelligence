package com.brandon3055.projectintelligence.docmanagement;

import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import static com.brandon3055.projectintelligence.docmanagement.ContentRelation.Type.*;

/**
 * Created by brandon3055 on 7/24/2018.
 */
public class ContentRelation {

    public final Type type;
    public final String contentString;
    public final boolean ignoreMeta;
    public final boolean includeNBT;
    private Object content = null;
    private boolean unavalible = false;

    public ContentRelation(Type type, String contentString, boolean ignoreMeta, boolean includeNBT) {
        this.type = type;
        this.contentString = contentString;
        this.ignoreMeta = ignoreMeta;
        this.includeNBT = includeNBT;
    }

    ContentRelation(JsonObject object) {
        type = Type.valueOf(object.get("type").getAsString());
        contentString = object.get("content").getAsString();
        ignoreMeta = JsonUtils.getBoolean(object, "ignore_meta", false);
        includeNBT = JsonUtils.getBoolean(object, "include_nbt", false);
    }

    @Nullable
    public Fluid getFluid() {
        if (unavalible || type != FLUID) return null;

        if (content == null) {
            content = FluidRegistry.getFluid(contentString);
            if (content == null) {
                unavalible = true;
            }
        }

        return (Fluid) content;
    }

    @Nullable
    public ItemStack getStack() {
        if (unavalible || type != STACK) return null;

        if (content == null) {
            loadStack();
        }

        return (ItemStack) content;
    }

    @Nullable
    public EntityEntry getEntity() {
        if (unavalible || type != ENTITY) return null;

        if (content == null) {
            content = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(contentString));
            if (content == null) {
                unavalible = true;
            }
        }

        return (EntityEntry) content;
    }

    private void loadStack() {
        StackReference reference = StackReference.fromString(contentString);
        if (reference != null) {
            content = reference.createStack();
            if (((ItemStack)content).isEmpty()) {
                unavalible = true;
            }
        }
        else {
            unavalible = true;
        }
    }

    public boolean isMatch(ItemStack test) {
        if (type == STACK && !unavalible) {
            ItemStack stack = getStack();
            if (stack != null) {
                if (stack.getItem() != test.getItem() || (!ignoreMeta && stack.getItemDamage() != test.getItemDamage())) {
                    return false;
                }
                else return !includeNBT || !ItemStack.areItemStackTagsEqual(stack, test);
            }
        }
        return false;
    }

    public boolean isMatch(Fluid test) {
        if (type == FLUID && !unavalible) {
            Fluid fluid = getFluid();
            if (fluid != null) {
                return fluid.getName().equals(test.getName());
            }
        }
        return false;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type.name());
        obj.addProperty("content", contentString);
        if (ignoreMeta) {
            obj.addProperty("ignore_meta", true);
        }
        if (includeNBT) {
            obj.addProperty("include_nbt", true);
        }
        return obj;
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        if (unavalible) return "";

        switch (type) {
            case STACK:
                ItemStack stack = getStack();
                if (stack != null) {
                    return stack.getDisplayName();
                }
                break;
            case ENTITY:
                EntityEntry entry = getEntity();
                if (entry != null){
                    String name = EntityList.getTranslationName(entry.getRegistryName());
                    if (name != null) {
                        return I18n.format(name);
                    }
                }
                break;
            case FLUID:
                Fluid fluid = getFluid();
                if (fluid != null) {
                    return fluid.getLocalizedName(new FluidStack(fluid, 1000));
                }
                break;
        }
        return "";
    }

    @Nullable
    public static ContentRelation fromJson(JsonObject object) {
        try {
            return new ContentRelation(object);
        }
        catch (Throwable e) {
            e.printStackTrace();
            PIGuiHelper.displayError("Failed to load content relation from: " + object + "\nSee console for stacktrace");
            return null;
        }
    }

    @Override
    public String toString() {
        return type.name().toLowerCase() + "|" + contentString + (type == STACK ? (", ignMeta: " + ignoreMeta + ", incNBT: " + includeNBT) : "");
    }

    public enum Type {
        STACK,
        ENTITY,
        FLUID
    }
}
