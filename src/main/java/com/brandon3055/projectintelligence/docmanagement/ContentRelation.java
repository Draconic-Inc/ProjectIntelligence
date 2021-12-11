package com.brandon3055.projectintelligence.docmanagement;

import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Locale;

import static com.brandon3055.projectintelligence.docmanagement.ContentRelation.Type.*;

/**
 * Created by brandon3055 on 7/24/2018.
 */
public class ContentRelation {

    public final Type type;
    public final String contentString;
    public final boolean includeNBT;
    private Object content = null;
    private boolean unavalible = false;

    public ContentRelation(Type type, String contentString, boolean includeNBT) {
        this.type = type;
        this.contentString = contentString;
        this.includeNBT = includeNBT;
    }

    ContentRelation(JsonObject object) {
        type = Type.valueOf(object.get("type").getAsString());
        contentString = object.get("content").getAsString();
        includeNBT = JSONUtils.getAsBoolean(object, "include_nbt", false);
    }

    @Nullable
    public Fluid getFluid() {
        if (unavalible || type != FLUID) return null;

        if (content == null) {
            content = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(contentString));
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
    public EntityType getEntity() {
        if (unavalible || type != ENTITY) return null;

        if (content == null) {
            content = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(contentString));
            if (content == null) {
                unavalible = true;
            }
        }

        return (EntityType) content;
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
                if (stack.getItem() != test.getItem()) {
                    return false;
                }
                else return !includeNBT || !ItemStack.matches(stack, test);
            }
        }
        return false;
    }

    public boolean isMatch(Fluid test) {
        if (type == FLUID && !unavalible) {
            Fluid fluid = getFluid();
            if (fluid != null) {
                return fluid.getRegistryName().equals(test.getRegistryName());
            }
        }
        return false;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type.name());
        obj.addProperty("content", contentString);
        if (includeNBT) {
            obj.addProperty("include_nbt", true);
        }
        return obj;
    }

    @OnlyIn(Dist.CLIENT)
    public String getLocalizedName() {
        if (unavalible) return "";

        switch (type) {
            case STACK:
                ItemStack stack = getStack();
                if (stack != null) {
                    return stack.getDisplayName().getString();
                }
                break;
            case ENTITY:
                EntityType type = getEntity();
                if (type != null){
                    String name = type.getDescription().getString();
                    if (name != null) {
                        return I18n.get(name);
                    }
                }
                break;
            case FLUID:
                Fluid fluid = getFluid();
                if (fluid != null) {
                    return new FluidStack(fluid, 1000).getDisplayName().getString();
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
        return type.name().toLowerCase(Locale.ENGLISH) + "|" + contentString + (type == STACK ? (", incNBT: " + includeNBT) : "");
    }

    public enum Type {
        STACK,
        ENTITY,
        FLUID
    }
}
