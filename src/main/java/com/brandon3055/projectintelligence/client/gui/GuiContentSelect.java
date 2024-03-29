package com.brandon3055.projectintelligence.client.gui;

import codechicken.lib.math.MathHelper;
import codechicken.lib.util.ArrayUtils;
import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElementManager;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.*;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.integration.JeiHelper;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.lib.DLResourceLocation;
import com.brandon3055.brandonscore.lib.StringyStacks;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.client.PITextures;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;


import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.*;
import net.minecraftforge.registries.ForgeRegistries;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.brandon3055.projectintelligence.client.gui.ContentInfo.ContentType.*;

/**
 * Created by brandon3055 on 20/11/2017.
 */
public class GuiContentSelect extends ModularGuiContainer<Container> {

    private Screen parant;
    private Set<ContentInfo.ContentType> allowedTypes;
    private ContentInfo.ContentType selectedType;
    private boolean allowCustomSize = true;
    private boolean allowLinking = true;
    private boolean entityInvalid = false;
    private SelectMode selectMode;
    private final ContentInfo contentInfo;
    private GuiStackIcon stackRenderer;
    private GuiEntityRenderer entityRenderer;
    private GuiTexture imageRenderer;
    private GuiTextField stackString;
    private GuiTextField entityString;
    private GuiTextField itemSizeField;
    private GuiTextField entitySizeField;
    private GuiTextField scaleField;
    private GuiTextField imgURLField;
    private GuiTextField imgWidthField;
    private GuiTextField imgHeightField;
    private Consumer<ContentInfo> selectCallBack;
    private DLResourceLocation imgResource;


    //TODO test all the text field linked value stuff
    public GuiContentSelect(Screen parent, SelectMode selectMode, @Nullable ContentInfo contentInfo, ContentInfo.ContentType... selectableTypes) {
        super(new DummyContainer(null), Minecraft.getInstance().player.inventory, new StringTextComponent(""));
        this.parant = parent;
        if (selectableTypes.length == 0) {
            throw new RuntimeException("Must specify at least 1 selectable type");
        }
        this.allowedTypes = Sets.newHashSet(selectableTypes);
        this.selectedType = contentInfo != null && ArrayUtils.contains(selectableTypes, contentInfo.type) ? contentInfo.type : selectableTypes[0];
        this.contentInfo = contentInfo == null ? new ContentInfo(selectedType) : contentInfo;
        this.imageWidth = 224;
        this.imageHeight = 230;
        this.selectMode = selectMode;
        if (selectMode == SelectMode.ICON && contentInfo == null) {
            this.contentInfo.drawSlot = true;
        }
    }

    public GuiContentSelect(Screen parent, SelectMode selectMode, ContentInfo.ContentType... selectableTypes) {
        this(parent, selectMode, null, selectableTypes);
    }

    /**
     * @param selectCallBack a callback that is fired when a selection is made. The ContentInfo provided will be null if the selection was canceled.
     */
    public GuiContentSelect setSelectCallBack(Consumer<ContentInfo> selectCallBack) {
        this.selectCallBack = selectCallBack;
        return this;
    }

    //region Construction

    @Override
    public void addElements(GuiElementManager manager) {
        GuiTexture background = GuiTexture.newDynamicTexture(BCSprites.themedGetter("background_dynamic")).setSize(imageWidth, imageHeight).setPos(getGuiLeft(), getGuiTop());
        manager.addChild(background);
        if (allowedTypes.size() > 1) {
            int nextButtonPos = guiLeft() + 5;
            GuiButton button;
            if (allowedTypes.contains(ITEM_STACK)) {
                manager.addChild(button = new GuiButton("Item Stack").setVanillaButtonRender(true).setSize(70, 16).setPos(nextButtonPos, guiTop() + 5));
                button.setToggleMode(true).setToggleStateSupplier(() -> selectedType == ITEM_STACK);
                button.onPressed(() -> changeType(ITEM_STACK));
                nextButtonPos = button.maxXPos() + 2;
            }
            if (allowedTypes.contains(ENTITY)) {
                manager.addChild(button = new GuiButton("Entity").setVanillaButtonRender(true).setSize(70, 16).setPos(nextButtonPos, guiTop() + 5));
                button.setToggleMode(true).setToggleStateSupplier(() -> selectedType == ENTITY);
                button.onPressed(() -> changeType(ENTITY));
                nextButtonPos = button.maxXPos() + 2;
            }
            if (allowedTypes.contains(IMAGE)) {
                manager.addChild(button = new GuiButton("Image").setVanillaButtonRender(true).setSize(70, 16).setPos(nextButtonPos, guiTop() + 5));
                button.setToggleMode(true).setToggleStateSupplier(() -> selectedType == IMAGE);
                button.onPressed(() -> changeType(IMAGE));
            }
            if (allowedTypes.contains(FLUID)) {
                manager.addChild(button = new GuiButton("Fluid").setVanillaButtonRender(true).setSize(70, 16).setPos(nextButtonPos, guiTop() + 5));
                button.setToggleMode(true).setToggleStateSupplier(() -> selectedType == FLUID);
                button.onPressed(() -> changeType(FLUID));
            }
        }

        { //Item
            GuiElement container = new GuiElement().addToGroup(ITEM_STACK.name());
            manager.addChild(container);

            Consumer<GuiButton> action = guiButton -> {
                contentInfo.drawHover = !contentInfo.drawHover;
                stackRenderer.setToolTip(contentInfo.drawHover);
            };
            container.addChild(newButton("Draw Vanilla Tool Tip", action, () -> contentInfo.drawHover, 0).setEnabled(!selectMode.isBasic()));
            Consumer<GuiTextField> change = textField -> {
                textField.setText(textField.getText().replace("\\n", "\n"));
                contentInfo.hover_text = textField.getText();
                stackRenderer.setToolTipOverride(contentInfo.hover_text.isEmpty() ? null : Lists.newArrayList(contentInfo.hover_text.split("\n")));
            };
            container.addChild(newTextField("Override Vanilla Tool Tip", change, 0.75, 0).setText(contentInfo.hover_text)).setEnabled(!selectMode.isBasic()).setHoverText("Allows you to specify a custom tool tip for the item. Accepts \\n for new lines and the select character \\\u00a7 for formatting");
            container.addChild(newButton("Draw Slot", guiButton -> contentInfo.drawSlot = !contentInfo.drawSlot, () -> contentInfo.drawSlot, 2.75).setEnabled(!selectMode.isBasic()));
            change = textField -> itemStackSelected(StringyStacks.fromString(textField.getText()), false);
            container.addChild(stackString = newTextField("Stack String", change, 4, 0).setText(StringyStacks.toStringNoCaps(contentInfo.stack))).setHoverText("Format is: " + TextFormatting.GOLD + "registry:name{nbt},stackSize");
            container.addChild(itemSizeField = newSizeField("Size:", guiTextField -> {}, 6, 0).setLinkedValue(() -> "" + contentInfo.size, s -> contentInfo.size = Math.max(4, Utils.parseInt(s))).setEnabled(selectMode.hasSizePos()));//TODO Test
            addInventorySelection(container, guiLeft() + ((xSize() - 206) / 2), guiTop() + ySize() - 82);
            container.addChild(new GuiLabel("Select item from your inventory" + (JeiHelper.jeiAvailable() ? " or JEI" : "")).setTextColour(0).setShadow(false).setAlignment(GuiAlign.LEFT).setWrap(true).setPos(guiLeft() + 105, guiTop() + 93).setSize(xSize() - 110, 14));

            container.addChild(new GuiButton("OK").setSize(40, 14).setVanillaButtonRender(true).setPos(guiLeft() + xSize() - 108, guiTop() + 128).onPressed(() -> finished(false)));
            container.addChild(new GuiButton("Cancel").setSize(60, 14).setVanillaButtonRender(true).setPos(guiLeft() + xSize() - 65, guiTop() + 128).onPressed(() -> finished(true)));

            if (selectMode == SelectMode.RELATION) {
//                container.addChild(new GuiButton("Use Meta").setSize(50, 14).setTrim(false).setToggleMode(true).setToggleStateSupplier(() -> !contentInfo.ignoreMeta).setVanillaButtonRender(true).setPos(guiLeft() + 5, guiTop() + 128).setButtonListener((guiButton, pressed) -> contentInfo.ignoreMeta = !contentInfo.ignoreMeta));
                container.addChild(new GuiButton("Use NBT").setSize(50, 14).setTrim(false).setToggleMode(true).setToggleStateSupplier(() -> contentInfo.includeNBT).setVanillaButtonRender(true).setPos(guiLeft() + 57, guiTop() + 128).onPressed(() -> contentInfo.includeNBT = !contentInfo.includeNBT));
            }

            container.addChild(new GuiLabel("Preview").setSize(200, 12).setTrim(false).setPos(guiLeft() - 205, guiTop()).setAlignment(GuiAlign.RIGHT));
            stackRenderer = new GuiStackIcon(contentInfo.stack);
            stackRenderer.setXPosMod((guiStackIcon, integer) -> guiLeft() - 5 - guiStackIcon.xSize()).setYPos(guiTop() + 12);
            stackRenderer.setSizeModifiers((guiStackIcon, integer) -> contentInfo.size, (guiStackIcon, integer) -> contentInfo.size);
            stackRenderer.setToolTip(contentInfo.drawHover);
            stackRenderer.addSlotBackground();
            stackRenderer.getBackground().setEnabledCallback(() -> contentInfo.drawSlot).setXPosMod((o, o2) -> stackRenderer.xPos());
            container.addChild(stackRenderer);
        }

        { //Entity
            GuiElement<?> container = new GuiElement<>().addToGroup(ENTITY.name());
            manager.addChild(container);

            if (selectMode != SelectMode.RELATION) {
                Consumer<GuiTextField> change = textField -> {
                    if (textField.getText().contains("\n")) {
                        textField.setText(textField.getText().replace("\\n", "\n"));
                    }
                    contentInfo.hover_text = textField.getText();
                    entityRenderer.setHoverText(element -> contentInfo.hover_text.isEmpty() ? new String[]{} : contentInfo.hover_text.split("\n"));
                };
                container.addChild(newTextField("Hover text", change, 0, 0).setText(contentInfo.hover_text)).setHoverText("Allows you to add mouse hover text to this entity. Accepts \\n for new lines and the select character \\\u00a7 for formatting");
                container.addChild(newButton("Entity tracks mouse movement", (guiButton) -> entityRenderer.setTrackMouse(contentInfo.trackMouse = !contentInfo.trackMouse), () -> contentInfo.trackMouse, 2));
                container.addChild(newButton("Draw Player Name (for player only)", (guiButton) -> entityRenderer.setDrawName((contentInfo.drawName = !contentInfo.drawName) && entityString.getText().startsWith("player:")), () -> contentInfo.drawName, 3));
                container.addChild(entitySizeField = newSizeField("Size:", guiTextField -> {}, 4, 0).setLinkedValue(() -> "" + contentInfo.size, s -> contentInfo.size = Math.max(4, Utils.parseInt(s))).setEnabled(selectMode.hasSizePos()));
                container.addChild(scaleField = newDoubleField("Scale:", value -> {}, 4, 115).setLinkedValue(() -> "" + contentInfo.scale, s -> contentInfo.scale = MathHelper.clip(Utils.parseDouble(s), 0.01, 100)).setEnabled(selectMode.hasSizePos()).setHoverText("Sets the scale of the entity relative to the size of the renderer element (not shown in the preview)"));
                container.addChild(newIntField("X Offset:", value -> contentInfo.xOffset = value, 5, 0).setText("" + contentInfo.xOffset).setEnabled(selectMode.hasSizePos()).setHoverText("Offsets the rendered x position of an entity.\nUseful for fine tuning an entities position or having an entity render in a completely different part of the gui. (not shown in the preview)"));
                container.addChild(newIntField("Y Offset:", value -> contentInfo.yOffset = value, 5, 100).setText("" + contentInfo.yOffset).setEnabled(selectMode.hasSizePos()).setHoverText("Offsets the rendered y position of an entity.\nUseful for fine tuning an entities position or having an entity render in a completely different part of the gui. (not shown in the preview)"));

                Consumer<Integer> rotateChanged = value -> {
                    contentInfo.rotation = value;
                    entityRenderer.setLockedRotation((float) contentInfo.rotation).rotationLocked(contentInfo.rotationSpeed == 0);
                    entityRenderer.setRotationSpeedMultiplier((float) contentInfo.rotationSpeed);
                };
                Consumer<Double> speedChanged = value -> {
                    contentInfo.rotationSpeed = value;
                    entityRenderer.setLockedRotation((float) contentInfo.rotation).rotationLocked(contentInfo.rotationSpeed == 0);
                    entityRenderer.setRotationSpeedMultiplier((float) contentInfo.rotationSpeed);
                };
                container.addChild(newIntField("Rotation:", rotateChanged, 6, 2).setText("" + contentInfo.rotation).setHoverText("Sets the fixed rotation of the entity if rotate speed is set to 0 (Not compatible with track mouse mode)"));
                container.addChild(newDoubleField("Speed:", speedChanged, 6, 112).setText("" + contentInfo.rotationSpeed).setHoverText("Sets the rotation speed of the entity (Not compatible with track mouse mode)\nThis is a multiplier for the default speed of 20 degrees per second."));
            }
            container.addChild(entityString = newTextField("Entity String", guiTextField -> updateEntity(), 7, 0).setText(contentInfo.entity)).setHoverText("Format is: " + TextFormatting.GOLD + "modid:entity_name or player:username\n" + TextFormatting.GRAY + "The entity name must be the entities registry name.");
            if (selectMode != SelectMode.RELATION) {
                container.addChild(new GuiLabel("Entity Inventory (right click to clear slot)").setTextColour(0).setShadow(false).setSize(200, 12).setTrim(false).setPos(guiLeft() + 5, guiTop() + 174).setAlignment(GuiAlign.LEFT));
                addEntityInventory(container);
            }

            GuiButton pickEntity = new GuiButton("Find Entity").setVanillaButtonRender(true).setSize(95, 14).setPos(guiLeft() + 124, guiTop() + 187);
            container.addChild(pickEntity);
            pickEntity.onPressed(() -> openEntitySelector(pickEntity));

            container.addChild(new GuiButton("OK").setSize(40, 14).setVanillaButtonRender(true).setPos(guiLeft() + xSize() - 108, guiTop() + ySize() - 20).onPressed(() -> finished(false)));
            container.addChild(new GuiButton("Cancel").setSize(60, 14).setVanillaButtonRender(true).setPos(guiLeft() + xSize() - 65, guiTop() + ySize() - 20).onPressed(() -> finished(true)));

            container.addChild(new GuiLabel("Preview").setSize(200, 12).setTrim(false).setPos(guiLeft() - 205, guiTop()).setAlignment(GuiAlign.RIGHT));
            entityRenderer = new GuiEntityRenderer();
            entityRenderer.setSizeModifiers((guiStackIcon, integer) -> contentInfo.size, (guiStackIcon, integer) -> contentInfo.size);
            entityRenderer.setEntity(new ResourceLocation("minecraft:pig")).setTrackMouse(contentInfo.trackMouse);
            entityRenderer.setXPosMod((guiStackIcon, integer) -> guiLeft() - 12 - guiStackIcon.xSize()).setYPos(guiTop() + 18);
            entityRenderer.silentErrors = true;
            entityRenderer.setLockedRotation((float) contentInfo.rotation).rotationLocked(contentInfo.rotationSpeed == 0);
            entityRenderer.setRotationSpeedMultiplier((float) contentInfo.rotationSpeed);
            container.addChild(new GuiLabel(TextFormatting.RED + "Invalid Entity String").setEnabledCallback(() -> entityInvalid).setSize(50, 12).setWrap(true).setPos(guiLeft() - 55, guiTop() + 20).setAlignment(GuiAlign.RIGHT));
            container.addChild(entityRenderer);
        }
        { //Image
            GuiElement container = new GuiElement().addToGroup(IMAGE.name());
            manager.addChild(container);

            container.addChild(imgURLField = newTextField("Image URL:", guiTextField -> updateImage(), 0, 0).setText(contentInfo.imageURL)).setHoverText("Currently only accepts http links https is not supported (yet)");
            container.addChild(imgWidthField = newSizeField("Width:", value -> setImageSize(Utils.parseInt(value.getText()), -1), 2, 6).setText("" + contentInfo.width).setEnabled(selectMode.hasSizePos()).setHoverText("Sets the width of the image. If width is set height will be automatically updated based on the images aspect ratio"));
            container.addChild(imgHeightField = newSizeField("Height:", value -> setImageSize(-1, Utils.parseInt(value.getText())), 3, 0).setText("" + contentInfo.height).setEnabled(selectMode.hasSizePos()).setHoverText("Sets the height of the image. If width is set width will be automatically updated based on the images aspect ratio"));

            GuiButton borderColour = new GuiButton("Border Colour").setPos(guiLeft() + 119, guiTop() + 62).setSize(100, 14).setWrap(true).setVanillaButtonRender(true).setEnabled(selectMode.hasSizePos());
            borderColour.onPressed(() -> new GuiPickColourDialog(borderColour).setIncludeAlpha(false).setColour(contentInfo.borderColour).setCCColourChangeListener(colour -> contentInfo.borderColour = colour).showCenter());
            container.addChild(borderColour);

            GuiButton hoverColour = new GuiButton("Border Hover").setPos(guiLeft() + 119, guiTop() + 78).setSize(100, 14).setWrap(true).setVanillaButtonRender(true).setEnabled(selectMode.hasSizePos());
            hoverColour.onPressed(() -> new GuiPickColourDialog(hoverColour).setIncludeAlpha(false).setColour(contentInfo.borderColourHover).setCCColourChangeListener(colour -> contentInfo.borderColourHover = colour).showCenter());
            container.addChild(hoverColour);

            container.addChild(newIntField("Padding:", value -> {}, 4, 0).setEnabled(selectMode.hasSizePos()).setLinkedValue(() -> "" + contentInfo.padding, s -> {
                contentInfo.leftPadding = contentInfo.topPadding = contentInfo.bottomPadding = contentInfo.rightPadding = contentInfo.padding = Utils.parseInt(s);
            }).setHoverText("Adds uniform padding (a border) around the image"));
            container.addChild(newIntField("Left:", value -> {}, 5, 11).setLinkedValue(() -> "" + contentInfo.leftPadding, s -> {
                contentInfo.leftPadding = Utils.parseInt(s);
                contentInfo.padding = 0;
            }).setEnabled(selectMode.hasSizePos()).setHoverText("Sets custom padding for the left side of the image"));
            container.addChild(newIntField("Top:", value -> {}, 5, 106).setLinkedValue(() -> "" + contentInfo.topPadding, s -> {
                contentInfo.topPadding = Utils.parseInt(s);
                contentInfo.padding = 0;
            }).setEnabled(selectMode.hasSizePos()).setHoverText("Sets custom padding for the top side of the image"));
            container.addChild(newIntField("Bottom:", value -> {}, 6, 0).setLinkedValue(() -> "" + contentInfo.bottomPadding, s -> {
                contentInfo.bottomPadding = Utils.parseInt(s);
                contentInfo.padding = 0;
            }).setEnabled(selectMode.hasSizePos()).setHoverText("Sets custom padding for the bottom side of the image"));
            container.addChild(newIntField("Right:", value -> {}, 6, 100).setLinkedValue(() -> "" + contentInfo.rightPadding, s -> {
                contentInfo.rightPadding = Utils.parseInt(s);
                contentInfo.padding = 0;
            }).setEnabled(selectMode.hasSizePos()).setHoverText("Sets custom padding for the right side of the image"));

            Consumer<GuiTextField> change = textField -> {
                if (textField.getText().contains("\n")) {
                    textField.setText(textField.getText().replace("\\n", "\n"));
                }
                contentInfo.hover_text = textField.getText();
                entityRenderer.setHoverText(element -> contentInfo.hover_text.isEmpty() ? new String[]{} : contentInfo.hover_text.split("\n"));
            };
            container.addChild(newTextField("Hover text", change, 7, 0).setText(contentInfo.hover_text)).setHoverText("Allows you to add mouse hover text to this entity. Accepts \\n for new lines and the select character \\\u00a7 for formatting");
            container.addChild(newTextField("Link pageURI or web address (optional)", textField -> contentInfo.linkTarget = textField.getText(), 9, 0).setText(contentInfo.hover_text)).setHoverText("Allows you to add a link to be opened when this image is clicked.");

            container.addChild(new GuiLabel("jpg images are preferred due to their smaller file size. Please consider converting your image to jpg format.").setEnabledCallback(() -> imgURLField.getText().endsWith(".png")).setShadow(false).setPos(guiLeft() - 60, guiTop() + ySize() + 5).setSize(xSize() + 120, 20).setWrap(true).setTextColour(0xFF0000));

            container.addChild(new GuiButton("OK").setSize(40, 14).setVanillaButtonRender(true).setPos(guiLeft() + xSize() - 108, guiTop() + ySize() - 20).onPressed(() -> finished(false)));
            container.addChild(new GuiButton("Cancel").setSize(60, 14).setVanillaButtonRender(true).setPos(guiLeft() + xSize() - 65, guiTop() + ySize() - 20).onPressed(() -> finished(true)));

            imageRenderer = new GuiTexture(0, 0, 18, 18, new ResourceLocation(""));
            imageRenderer.setTexSizeOverride(18, 18).setTexSheetSize(18);
            imageRenderer.setXPosMod((guiStackIcon, integer) -> guiLeft() - 5 - guiStackIcon.xSize() - contentInfo.rightPadding).setYPosMod((guiTexture, integer) -> guiTop() + contentInfo.topPadding);
            GuiBorderedRect imgBack = new GuiBorderedRect();
            imgBack.setBorderColourL(hovering -> hovering ? contentInfo.borderColourHover == null ? 0 : contentInfo.borderColourHover.argb() : contentInfo.borderColour == null ? 0 : contentInfo.borderColour.argb());
            imgBack.setXPosMod((guiStackIcon, integer) -> guiLeft() - 5 - guiStackIcon.xSize()).setYPosMod((guiTexture, integer) -> guiTop());
            imgBack.setSizeModifiers((g, i) -> imageRenderer.xSize() + contentInfo.leftPadding + contentInfo.rightPadding, (g, i) -> imageRenderer.ySize() + contentInfo.topPadding + contentInfo.bottomPadding);

            container.addChild(imgBack);
            imgBack.addChild(imageRenderer);
            updateImage();
        }
        //Fluid
        {
            GuiElement container = new GuiElement().addToGroup(FLUID.name());
            manager.addChild(container);

            container.addChild(new GuiButton("OK").setSize(40, 14).setVanillaButtonRender(true).setPos(guiLeft() + xSize() - 108, guiTop() + ySize() - 20).onPressed(() -> finished(false)));
            container.addChild(new GuiButton("Cancel").setSize(60, 14).setVanillaButtonRender(true).setPos(guiLeft() + xSize() - 65, guiTop() + ySize() - 20).onPressed(() -> finished(true)));

            GuiStackIcon icon = new GuiStackIcon(ItemStack.EMPTY).setSize(32, 32);
            container.addChild(icon);
            icon.setToolTip(false);

            Consumer<String> update = s -> {
                contentInfo.fluid = s;
                icon.setHoverText(contentInfo.fluid);
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(contentInfo.fluid));
                if (fluid != null) {
                    ItemStack bucket = FluidUtil.getFilledBucket(new FluidStack(fluid, 1000));
                    if (!bucket.isEmpty()) {
                        icon.setStack(bucket);
                    }
                }
            };
            GuiTextField field;
            container.addChild(field = newTextField("Fluid Name", textField -> {}, 7, 0).setLinkedValue(() -> contentInfo.fluid, update).setText(contentInfo.fluid));
            update.accept(field.getText());

            GuiButton pickFluid = new GuiButton("Find Fluid").setVanillaButtonRender(true).setSize(95, 14).setPos(guiLeft() + 124, guiTop() + 187);
            container.addChild(pickFluid);
            pickFluid.onPressed(() -> openFluidSelector(pickFluid, update));
            icon.setPos(guiLeft() + 5, pickFluid.yPos());
        }
    }

    @Override
    public void reloadGui() {
        super.reloadGui();
        changeType(selectedType);
        updateEntity();
    }

    private GuiButton newButton(String label, @Nonnull Consumer<GuiButton> onClick, @Nullable Supplier<Boolean> stateSupplier, double heightIndex) {
        int elementTop = guiTop() + 30;
        int spacing = 16;
        GuiButton button = new GuiButton(label).setSize(xSize() - 10, 14).setVanillaButtonRender(true).setPos(guiLeft() + 5, (int) (elementTop + (spacing * heightIndex)));
        if (stateSupplier != null) {
            button.setToggleMode(true).setToggleStateSupplier(stateSupplier);
        }
        button.onPressed(() -> onClick.accept(button));
        return button;
    }

    private GuiTextField newTextField(String label, @Nonnull Consumer<GuiTextField> onChange, double heightIndex, int xOffset) {
        int elementTop = guiTop() + 30;
        int spacing = 16;

        GuiLabel fieldLabel = new GuiLabel(label).setWrap(false).setTrim(false).setWidthFromText(14).setPos(guiLeft() + 5 + xOffset, (int) (elementTop + (spacing * heightIndex) + 3));
        fieldLabel.setTextColour(0).setShadow(false);

        GuiTextField textField = new GuiTextField();
        textField.setSize(xSize() - 10, 14).setPos(guiLeft() + 5 + xOffset, (int) (elementTop + (spacing * (heightIndex + 1))));
//        textField.setListener((event, eventSource) -> onChange.accept(textField));
        textField.setChangeListener(() -> onChange.accept(textField));
        textField.addChild(fieldLabel);
        textField.setMaxStringLength(4096);

        return textField;
    }

    private GuiTextField newSizeField(String label, @Nonnull Consumer<GuiTextField> onChange, double heightIndex, int xOffset) {
        int elementTop = guiTop() + 30;
        int spacing = 16;

        GuiLabel fieldLabel = new GuiLabel(label).setWrap(false).setTrim(false).setWidthFromText(14).setPos(guiLeft() + 5 + xOffset, (int) (elementTop + (spacing * heightIndex)));
        fieldLabel.setTextColour(0).setShadow(false);

        GuiTextField textField = new GuiTextField();
        textField.setSize(50, 14).setPos(fieldLabel.maxXPos() + 2, fieldLabel.yPos());
//        textField.setListener((event, eventSource) -> onChange.accept(textField));
        textField.setChangeListener(() -> onChange.accept(textField));
        textField.addChild(fieldLabel);
        textField.setMaxStringLength(8);

        GuiButton percent = new GuiButton("%").setToggleMode(true).setToggleStateSupplier(() -> contentInfo.sizePercent);
        percent.onPressed(() -> {
            contentInfo.sizePercent = !contentInfo.sizePercent;
            textField.setValidator(s -> s.isEmpty() || Utils.validInteger(s));
        });
        percent.setSize(20, 14).setPos(textField.maxXPos() + 2, fieldLabel.yPos());
        percent.setHoverText("If enabled this value becomes a percentage value between 0 and 100 where a value of say 50 would be half of the width of the gui");
        percent.setVanillaButtonRender(true);
        fieldLabel.addChild(percent);

        return textField;
    }

    private GuiTextField newIntField(String label, @Nonnull Consumer<Integer> onChange, double heightIndex, int xOffset) {
        int elementTop = guiTop() + 30;
        int spacing = 16;

        GuiLabel fieldLabel = new GuiLabel(label).setWrap(false).setTrim(false).setWidthFromText(14).setPos(guiLeft() + 5 + xOffset, (int) (elementTop + (spacing * heightIndex)));
        fieldLabel.setTextColour(0).setShadow(false);

        GuiTextField textField = new GuiTextField();
        textField.setSize(50, 14).setPos(fieldLabel.maxXPos() + 2, fieldLabel.yPos());
//        textField.setListener((event, eventSource) -> onChange.accept(Utils.parseInt(textField.getText())));
        textField.setChangeListener(text -> onChange.accept(Utils.parseInt(text)));
        textField.addChild(fieldLabel);
        textField.setMaxStringLength(8);
        textField.setValidator(s -> s.isEmpty() || Utils.validDouble(s));

        return textField;
    }

    private GuiTextField newDoubleField(String label, @Nonnull Consumer<Double> onChange, double heightIndex, int xOffset) {
        int elementTop = guiTop() + 30;
        int spacing = 16;

        GuiLabel fieldLabel = new GuiLabel(label).setWrap(false).setTrim(false).setWidthFromText(14).setPos(guiLeft() + 5 + xOffset, (int) (elementTop + (spacing * heightIndex)));
        fieldLabel.setTextColour(0).setShadow(false);

        GuiTextField textField = new GuiTextField();
        textField.setSize(50, 14).setPos(fieldLabel.maxXPos() + 2, fieldLabel.yPos());
//        textField.setListener((event, eventSource) -> onChange.accept(Utils.parseDouble(textField.getText())));
        textField.setChangeListener(text -> onChange.accept(Utils.parseDouble(text)));
        textField.addChild(fieldLabel);
        textField.setMaxStringLength(8);
        textField.setValidator(s -> s.isEmpty() || Utils.validDouble(s));

        return textField;
    }

    private void updateEntity() {
        contentInfo.entity = entityString.getText();
        entityRenderer.setEnabled(true);
        entityInvalid = false;
        if (contentInfo.entity.startsWith("player:") && !contentInfo.entity.replaceFirst("player:", "").isEmpty()) {
            PlayerEntity player = GuiEntityRenderer.createRenderPlayer(minecraft.level, contentInfo.entity.replaceFirst("player:", ""));
            entityRenderer.setEntity(player);
            entityRenderer.setDrawName(contentInfo.drawName);
            for (int i = 0; i < 6; i++) {
                player.setItemSlot(EquipmentSlotType.values()[i], contentInfo.entityInventory[i > 1 ? 7 - i : i]);
            }
        } else {
            EntityType type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(contentInfo.entity));
            Entity entity = type == null ? null : type.create(minecraft.level);

            if (entity == null) {
                entityRenderer.setEnabled(false);
                entityInvalid = true;
            } else {
                for (int i = 0; i < 6; i++) {
                    entity.setItemSlot(EquipmentSlotType.values()[i], contentInfo.entityInventory[i > 1 ? 7 - i : i]);
                }
                entityRenderer.setEntity(entity);
                entityRenderer.setDrawName(false);
            }
        }
    }

    private void setImageSize(int width, int height) {
        contentInfo.width = width;
        contentInfo.height = height;
        if (width == -1) {
            imgWidthField.setText("-1");
        } else if (height == -1) {
            imgHeightField.setText("-1");
        }
        updateImage();
    }

    private void updateImage() {
        contentInfo.imageURL = imgURLField.getText();
        imgResource = DLRSCache.getResource(contentInfo.imageURL);
        imageRenderer.setTexture(imgResource);
        if (contentInfo.width != -1) {
            imageRenderer.setSize(contentInfo.width, (int) (((double) imgResource.height / (double) imgResource.width) * (double) contentInfo.width));
        } else if (contentInfo.height != -1) {
            imageRenderer.setSize((int) (((double) imgResource.width / (double) imgResource.height) * (double) contentInfo.height), contentInfo.height);
        }
    }

    private void openEntitySelector(GuiElement parent) {
        GuiSelectDialog<String> selector = new GuiSelectDialog<>(parent);
        selector.setSize(134, 200).setInsets(1, 1, 12, 1).setCloseOnSelection(true);
        selector.addChild(new GuiBorderedRect().setPosAndSize(selector).setColours(0xFFFFFFFF, 0xFF000000));
        selector.setRendererBuilder(s -> {
            GuiElement base = new GuiBorderedRect().setColours(0xFF000000, 0xFF707070).setSize(130, 40);
            base.addChild(new GuiEntityRenderer().setTrackMouse(true).setForce2dSize(true).setPosAndSize(7, 11, 24, 24).setEntity(new ResourceLocation(s)).setSilentErrors(true));
            base.addChild(new GuiLabel(s).setShadow(false).setPosAndSize(35, 0, 85, 40).setWrap(true));
            return base;
        });

        GuiTextField filter = new GuiTextField();
        selector.addChild(filter);
        filter.setSize(selector.xSize(), 14).setPos(selector.xPos(), selector.maxYPos() - 12);

        Runnable reload = () -> {
            selector.clearItems();
            String filterText = filter.getText();
            for (EntityType type : ForgeRegistries.ENTITIES.getValues()) {
                String id = type.getRegistryName().toString();
                if (filterText.isEmpty() || id.contains(filterText)) {
                    selector.addItem(id);
                }
            }
        };

        reload.run();
//        filter.setListener((event1, eventSource1) -> reload.run());
        filter.setChangeListener(reload);

        selector.showCenter();
        selector.getScrollElement().setListSpacing(1).reloadElement();
        selector.setSelectionListener(s -> {
            entityString.setText(s);
            updateEntity();
        });
    }

    private void openFluidSelector(GuiElement parent, Consumer<String> update) {
        GuiSelectDialog<Fluid> selector = new GuiSelectDialog<>(parent);
        selector.setSize(134, 200).setInsets(1, 1, 12, 1).setCloseOnSelection(true);
        selector.addChild(new GuiBorderedRect().setPosAndSize(selector).setColours(0xFFFFFFFF, 0xFF000000));
        selector.setRendererBuilder(s -> {
            GuiElement base = new GuiBorderedRect().setColours(0xFF000000, 0xFF707070).setSize(130, 20);
//            Fluid fluid = ForgeRegistries.FLUIDS.getValue(s);
//            if (fluid != null) {
            ItemStack bucket = FluidUtil.getFilledBucket(new FluidStack(s, 1000));
            if (!bucket.isEmpty()) {
                base.addChild(new GuiStackIcon(bucket).setPosAndSize(2, 1, 18, 18));
            }
//            }

            base.addChild(new GuiLabel(new FluidStack(s, 0).getDisplayName().getString()).setShadow(false).setPosAndSize(20, 0, 85, 20).setWrap(true));
            return base;
        });

        GuiTextField filter = new GuiTextField();
        selector.addChild(filter);
        filter.setSize(selector.xSize(), 14).setPos(selector.xPos(), selector.maxYPos() - 12);

        Runnable reload = () -> {
            selector.clearItems();
            String filterText = filter.getText();
            for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
                if (filterText.isEmpty() || new FluidStack(fluid, 0).getDisplayName().getString().contains(filterText)) {
                    selector.addItem(fluid);
                }
            }
        };

        reload.run();
//        filter.setListener((event1, eventSource1) -> reload.run());
        filter.setChangeListener(reload);

        selector.showCenter();
        selector.getScrollElement().setListSpacing(1).reloadElement();
        selector.setSelectionListener(s -> {
            contentInfo.fluid = s.getRegistryName().toString();
            update.accept(contentInfo.fluid);
        });
    }

    private void addInventorySelection(GuiElement parent, int invX, int invY) {
        PlayerInventory inv = Minecraft.getInstance().player.inventory;
        GuiSlotRender slot;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 4; y++) {
                int xPos = invX + (x * 18) + (x > 0 ? 4 : 0);
                int yPos = invY + (y * 18) + (x > 0 && y == 3 ? 4 : 0);
                ItemStack stack = x == 0 ? inv.getArmor(3 - y) : y == 3 ? inv.getItem((x - 1)) : inv.getItem((x - 1) + (9 * (y + 1)));
                slot = new GuiSlotRender().setPos(xPos, yPos);

                if (!stack.isEmpty()) {
                    slot.addChild(new GuiStackIcon(stack).setPos(slot));
                    slot.addChild(new GuiButton().setPosAndSize(slot).onPressed(() -> itemStackSelected(stack, true)));
                }

                parent.addChild(slot);
            }
        }
        parent.addChild(slot = new GuiSlotRender().setPos(invX + 188, guiTop() + ySize() - 24));
        if (!inv.offhand.get(0).isEmpty()) {
            slot.addChild(new GuiStackIcon(inv.offhand.get(0)).setPos(slot));
            slot.addChild(new GuiButton().setPosAndSize(slot).onPressed(() -> itemStackSelected(inv.offhand.get(0), true)));
        }
    }

    private void addEntityInventory(GuiElement container) {
        for (int i = 0; i < 6; i++) {
            int slotIndex = i;
            GuiSlotRender slot = new GuiSlotRender();
            slot.setPos(guiLeft() + 5 + (i * 18) + (i > 1 ? 8 : i > 0 ? 4 : 0), guiTop() + 187).setSize(18, 18);
            container.addChild(slot);
            slot.addChild(new GuiTexture(96 + i * 16, 0, 16, 16, PITextures.PI_PARTS).setRelPos(slot, 1, 1).setEnabledCallback(() -> contentInfo.entityInventory[slotIndex].isEmpty()));
            ItemStack stack = contentInfo.entityInventory[i];
            GuiStackIcon stackIcon = new GuiStackIcon(stack).setPos(slot);
            stackIcon.setEnabledCallback(() -> !contentInfo.entityInventory[slotIndex].isEmpty());
            slot.addChild(stackIcon);
            GuiButton button = new GuiButton().setPosAndSize(slot);
            slot.addChild(button);
            button.onButtonPressed((pressed) -> {
                if (pressed == 1) {
                    contentInfo.entityInventory[slotIndex] = ItemStack.EMPTY;
                    stackIcon.setStack(ItemStack.EMPTY);
                    updateEntity();
                } else {
                    GuiContentSelect gui = new GuiContentSelect(this, SelectMode.PICK_STACK, ITEM_STACK);
                    gui.setSelectCallBack(content -> {
                        if (content == null) return;
                        contentInfo.entityInventory[slotIndex] = content.stack;
                        stackIcon.setStack(content.stack);
                        updateEntity();
                    });
                    gui.contentInfo.stack = contentInfo.entityInventory[slotIndex];
                    minecraft.setScreen(gui);
                }
            });
        }
    }

    private void finished(boolean cancel) {
        if (selectCallBack != null) {
            selectCallBack.accept(cancel ? null : contentInfo);
        }
        minecraft.setScreen(parant);
    }

    //endregion

    public void changeType(ContentInfo.ContentType type) {
        manager.setChildGroupEnabled(ITEM_STACK.name(), type == ITEM_STACK);
        manager.setChildGroupEnabled(ENTITY.name(), type == ENTITY);
        manager.setChildGroupEnabled(IMAGE.name(), type == IMAGE);
        manager.setChildGroupEnabled(FLUID.name(), type == FLUID);
        selectedType = type;
        contentInfo.type = type;
//
//        itemSizeField.setText("" + contentInfo.size);
//        scaleField.setText("" + contentInfo.scale);
    }

    @Override
    public void render(MatrixStack mStack, int mouseX, int mouseY, float partialTicks) {
        super.render(mStack, mouseX, mouseY, partialTicks);
//        if (allowedTypes.size() > 1) {
//            drawGradientRect(guiLeft() + 3, guiTop() + 24, guiLeft() + xSize() - 2, guiTop() + 27, 0xFF000000, 0xFF000000);
//        }

        if (imgResource != null && imgResource.dlStateChanged()) {
            updateImage();
        }
    }

    //region User Input Handling

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ItemStack clicked = JeiHelper.getPanelItemUnderMouse();

        if (clicked != null && !clicked.isEmpty()) {
            itemStackSelected(clicked, true);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void itemStackSelected(ItemStack itemStack, boolean updateStackField) {
        if (selectedType == ITEM_STACK) {
            contentInfo.stack = itemStack;
            stackRenderer.setToolTipOverride(contentInfo.hover_text.isEmpty() ? null : Lists.newArrayList(contentInfo.hover_text.split("\n")));
            stackRenderer.setStack(contentInfo.stack);
            if (updateStackField) {
                stackString.setText(StringyStacks.toStringNoCaps(itemStack));
                stackString.setCursorPositionZero();
            }
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (keyCode == 28) {
            finished(false);
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    //endregion

    public enum SelectMode {
        ICON(false, true),
        RELATION(false, false),
        MD_CONTENT(true, true),
        PICK_STACK(false, false);

        private final boolean hasSize;
        private final boolean customizations;

        SelectMode(boolean hasSize, boolean customizations) {
            this.hasSize = hasSize;
            this.customizations = customizations;
        }

        public boolean hasSizePos() {
            return hasSize;
        }

        public boolean isBasic() {
            return !customizations;
        }
    }

    public static class DummyContainer extends Container {

        public DummyContainer(PlayerEntity player) {
            super(new ContainerType<>((p_create_1_, p_create_2_) -> null), 0); //TODO No idea if this will work yet
        }

        @Override
        public void setAll(List<ItemStack> stacks) {}

        @Override
        public boolean stillValid(PlayerEntity p_75145_1_) {
            return true;
        }
    }
}
