package com.brandon3055.projectintelligence.client.gui;

import codechicken.lib.colour.Colour;
import codechicken.lib.util.ArrayUtils;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.projectintelligence.docmanagement.ContentRelation;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;


import java.util.function.Supplier;

//I prefer to just pretend this class does not exist... Just dont look at it and its not a problem!
public class ContentInfo {
    //General
    public ContentType type;

    //Stack
    public StackReference stack = new StackReference(ItemStack.EMPTY);
    public boolean drawHover = true;//draw_hover (Mainly for stacks, Draws normal stack tooltip) //Default True

    //Fluid
    public String fluid = "water";

    //Entity
    public String entity = "minecraft:pig";
    public ItemStack[] entityInventory = new ItemStack[6];//mainHand, offHand, head, chest, legs, boots.
    public int xOffset = 0;//x_offset
    public int yOffset = 0;//y_offset
    public double rotationSpeed = 1;//rotate_speed
    public int rotation = 0;//rotation
    public double scale = 1;//scale
    public boolean trackMouse = false;//track_mouse
    public boolean drawName = false;//draw_name
    public boolean allowDrag = false;//Allow drag //todo

    //Image
    public String imageURL = "http://ss.brandon3055.com/iqx38ra.jpg";
    public Colour borderColour = null;//border_colour
    public Colour borderColourHover = null;//border_colour_hover
    public int padding = 0;//padding
    public int leftPadding = 0;//left_pad
    public int rightPadding = 0;//right_pad
    public int topPadding = 0;//top_pad
    public int bottomPadding = 0;//bottom_pad
    public int width = 18;//width
    public int height = -1;//height


    //General Modifiers
    public int size = 18;
    public boolean sizePercent = false;
    public String hover_text = ""; //(hover text)
    public boolean drawSlot = false; //Default False
    public String linkTarget = "";

    //Relations
    public boolean includeNBT = false;

    public ContentInfo(ContentType type) {
        this.type = type;
        ArrayUtils.fill(entityInventory, ItemStack.EMPTY);
    }

    public JsonObject getAsIconObj() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type.name);

        if (drawSlot) object.addProperty("draw_slot", true);
        if (!drawHover) object.addProperty("draw_hover", false);
        if (!hover_text.isEmpty()) object.addProperty("hover_text", hover_text);

        switch (type) {
            case ITEM_STACK:
                object.addProperty("icon_string", stack.toString());
                break;
            case ENTITY:
                object.addProperty("icon_string", entity);
                if (trackMouse) {
                    object.addProperty("track_mouse", true);
                }

                LogHelper.dev(entityInventory[0]);

                JsonObject equip = new JsonObject();
                if (!entityInventory[0].isEmpty())
                    equip.addProperty("main_hand", StackReference.stackString(entityInventory[0]));
                if (!entityInventory[1].isEmpty())
                    equip.addProperty("off_hand", StackReference.stackString(entityInventory[1]));
                if (!entityInventory[2].isEmpty())
                    equip.addProperty("head", StackReference.stackString(entityInventory[2]));
                if (!entityInventory[3].isEmpty())
                    equip.addProperty("chest", StackReference.stackString(entityInventory[3]));
                if (!entityInventory[4].isEmpty())
                    equip.addProperty("legs", StackReference.stackString(entityInventory[4]));
                if (!entityInventory[5].isEmpty())
                    equip.addProperty("feet", StackReference.stackString(entityInventory[5]));

                if (!equip.entrySet().isEmpty()) {
                    object.add("equipment", equip);
                }

                break;
            case IMAGE:
                object.addProperty("icon_string", imageURL);
                break;
        }

        return object;
    }

    public static ContentInfo fromIconObj(JsonObject iconObj) {
        ContentInfo ci = new ContentInfo(ContentType.getByName(JSONUtils.getAsString(iconObj, "type", "stack")));

        ci.drawSlot = JSONUtils.getAsBoolean(iconObj, "draw_slot", ci.drawSlot);
        ci.drawHover = JSONUtils.getAsBoolean(iconObj, "draw_hover", ci.drawHover);
        ci.hover_text = JSONUtils.getAsString(iconObj, "hover_text", "");

        switch (ci.type) {
            case ITEM_STACK:
                if (JSONUtils.isValidNode(iconObj, "icon_string")) {
                    ci.stack = StackReference.fromString(JSONUtils.getAsString(iconObj, "icon_string", ""));
                }
                break;
            case ENTITY:
                ci.trackMouse = JSONUtils.getAsBoolean(iconObj, "track_mouse", ci.trackMouse);
                if (JSONUtils.isValidNode(iconObj, "equipment") && iconObj.get("equipment").isJsonObject()) {
                    JsonObject equip = iconObj.get("equipment").getAsJsonObject();

                    if (JSONUtils.isValidNode(equip, "main_hand"))
                        ci.entityInventory[0] = getStack(JSONUtils.getAsString(equip, "main_hand", ""));
                    if (JSONUtils.isValidNode(equip, "off_hand"))
                        ci.entityInventory[1] = getStack(JSONUtils.getAsString(equip, "off_hand", ""));
                    if (JSONUtils.isValidNode(equip, "head"))
                        ci.entityInventory[2] = getStack(JSONUtils.getAsString(equip, "head", ""));
                    if (JSONUtils.isValidNode(equip, "chest"))
                        ci.entityInventory[3] = getStack(JSONUtils.getAsString(equip, "chest", ""));
                    if (JSONUtils.isValidNode(equip, "legs"))
                        ci.entityInventory[4] = getStack(JSONUtils.getAsString(equip, "legs", ""));
                    if (JSONUtils.isValidNode(equip, "feet"))
                        ci.entityInventory[5] = getStack(JSONUtils.getAsString(equip, "feet", ""));
                }

                if (JSONUtils.isValidNode(iconObj, "icon_string")) {
                    ci.entity = JSONUtils.getAsString(iconObj, "icon_string", "");
                }
                break;
            case IMAGE:
                if (JSONUtils.isValidNode(iconObj, "icon_string")) {
                    ci.imageURL = JSONUtils.getAsString(iconObj, "icon_string", "");
                }
                break;
        }

        return ci;
    }

    public ContentRelation asRelation() {
        String contentString = "";
        switch (type) {
            case ITEM_STACK:
                if (!includeNBT) {
                    stack.setNbt(null);
                }
                contentString = stack.toString();
                break;
            case ENTITY:
                contentString = entity;
                includeNBT = false;
                break;
            case FLUID:
                contentString = fluid;
                includeNBT = false;
                break;
        }

        return new ContentRelation(ContentType.toRelationType(type), contentString, includeNBT);
    }

    public static ContentInfo fromRelation(ContentRelation relation) {
        ContentInfo info = new ContentInfo(ContentType.fromRelationType(relation.type));
        switch (relation.type) {
            case STACK:
                info.stack = StackReference.fromString(relation.contentString);
                if (info.stack == null) {
                    info.stack = new StackReference(ItemStack.EMPTY);
                }
                info.includeNBT = relation.includeNBT;
                break;
            case ENTITY:
                info.entity = relation.contentString;
                break;
            case FLUID:
                info.fluid = relation.contentString;
                break;
        }
        return info;
    }

    public String toMDTag() {
        String tag = "\u00a7" + type.name;
        String ops = "";
        switch (type) {
            case ITEM_STACK:
                tag += "[" + stack.toString() + "]";
                ops = addIf(ops, "size:" + size + (sizePercent ? "%" : ""), () -> true);
                ops = addIf(ops, "draw_slot:true", () -> drawSlot);
                ops = addIf(ops, "enable_tooltip:false", () -> !drawHover);
                ops = addIf(ops, "tooltip:\"" + hover_text.replace("\n", "\\n") + "\"", () -> !hover_text.isEmpty());
                break;
            case ENTITY:
                tag += "[" + entity + "]";
                ops = addIf(ops, "size:" + size + (sizePercent ? "%" : ""), () -> true);
                ops = addIf(ops, "x_offset:" + xOffset, () -> xOffset > 0);
                ops = addIf(ops, "y_offset:" + yOffset, () -> yOffset > 0);
                ops = addIf(ops, "rotate_speed:" + rotationSpeed, () -> rotationSpeed != 0);
                ops = addIf(ops, "rotation:" + rotation, () -> rotation != 0);
                ops = addIf(ops, "scale:" + scale, () -> scale != 1);

                ops = addIf(ops, "tooltip:\"" + hover_text.replace("\n", "\\n") + "\"", () -> !hover_text.isEmpty());
                ops = addIf(ops, "track_mouse:true", () -> trackMouse);
                ops = addIf(ops, "draw_name:true", () -> drawName);

                ops = addIf(ops, "main_hand:\"" + getEquipString(0) + "\"", () -> !getEquipString(0).isEmpty());
                ops = addIf(ops, "off_hand:\"" + getEquipString(1) + "\"", () -> !getEquipString(1).isEmpty());
                ops = addIf(ops, "head:\"" + getEquipString(2) + "\"", () -> !getEquipString(2).isEmpty());
                ops = addIf(ops, "chest:\"" + getEquipString(3) + "\"", () -> !getEquipString(3).isEmpty());
                ops = addIf(ops, "legs:\"" + getEquipString(4) + "\"", () -> !getEquipString(4).isEmpty());
                ops = addIf(ops, "boots:\"" + getEquipString(5) + "\"", () -> !getEquipString(5).isEmpty());

                break;
            case IMAGE:
                tag = "\u00a7img[" + imageURL + "]";

                ops = addIf(ops, "border_colour:0x" + Integer.toHexString(borderColour == null ? 0 : borderColour.rgb()), () -> borderColour != null);
                ops = addIf(ops, "border_colour_hover:0x" + Integer.toHexString(borderColourHover == null ? 0 : borderColourHover.rgb()), () -> borderColourHover != null);
                if ((leftPadding == rightPadding) && (topPadding == bottomPadding) && (leftPadding == topPadding)) {
                    padding = leftPadding;
                    leftPadding = rightPadding = topPadding = bottomPadding = 0;
                }
                ops = addIf(ops, "padding:" + padding, () -> padding > 0);
                ops = addIf(ops, "left_pad:" + leftPadding, () -> leftPadding > 0);
                ops = addIf(ops, "right_pad:" + rightPadding, () -> rightPadding > 0);
                ops = addIf(ops, "top_pad:" + topPadding, () -> topPadding > 0);
                ops = addIf(ops, "bottom_pad:" + bottomPadding, () -> bottomPadding > 0);
                ops = addIf(ops, "width:" + width + (sizePercent ? "%" : ""), () -> width > 0);
                ops = addIf(ops, "height:" + height + (sizePercent ? "%" : ""), () -> height > 0);
                ops = addIf(ops, "tooltip:\"" + hover_text.replace("\n", "\\n") + "\"", () -> !hover_text.isEmpty());
                ops = addIf(ops, "link_to:\"" + linkTarget + "\"", () -> !linkTarget.isEmpty());

                break;
        }

        return tag + (ops.isEmpty() ? "" : "{" + ops + "}");
    }

    private String getEquipString(int index) {
        if (!entityInventory[index].isEmpty()) {
            return StackReference.stackString(entityInventory[index]);
        }
        return "";
    }

    public boolean hasEquipment() {
        for (ItemStack stack : entityInventory) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String getContentString() {
        switch (type) {
            case ENTITY:
                return entity;
            case IMAGE:
                return imageURL;
            case ITEM_STACK:
            default:
                return stack.toString();
        }
    }

    private static ItemStack getStack(String stackString) {
        StackReference ref = StackReference.fromString(stackString);
        if (ref == null) {
            return ItemStack.EMPTY;
        }
        return ref.createStack();
    }

    private String addIf(String ops, Object add, Supplier<Boolean> check) {
        return ops + (check.get() ? (ops.isEmpty() ? "" : ",") + add : "");
    }

    @Override
    public String toString() {
        return type.name + "|" + getContentString();
    }

    public enum ContentType {
        ITEM_STACK("stack"),
        ENTITY("entity"),
        IMAGE("image"),
        FLUID("fluid");

        private final String name;

        ContentType(String name) {this.name = name;}

        public static ContentType getByName(String name) {
            switch (name) {
                case "entity":
                    return ENTITY;
                case "image":
                    return IMAGE;
                case "fluid":
                    return FLUID;
                default:
                    return ITEM_STACK;
            }
        }

        public static ContentType fromRelationType(ContentRelation.Type type) {
            switch (type) {
                case ENTITY:
                    return ENTITY;
                case FLUID:
                    return FLUID;
                default:
                    return ITEM_STACK;
            }
        }

        public static ContentRelation.Type toRelationType(ContentType type) {
            switch (type) {
                case ITEM_STACK:
                    return ContentRelation.Type.STACK;
                case ENTITY:
                    return ContentRelation.Type.ENTITY;
                case FLUID:
                    return ContentRelation.Type.FLUID;
            }
            return ContentRelation.Type.ENTITY;
        }
    }
}