package com.brandon3055.projectintelligence.client.keybinding;

import codechicken.lib.fluid.FluidUtils;
import codechicken.lib.raytracer.RayTracer;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.ProjectIntelligence;
import com.brandon3055.projectintelligence.api.PiAPI;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;

import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;


import java.util.List;
import java.util.function.Supplier;


import static net.minecraft.util.math.RayTraceResult.Type.ENTITY;
import static net.minecraft.util.math.RayTraceResult.Type.MISS;
import static net.minecraftforge.client.settings.KeyConflictContext.GUI;
import static net.minecraftforge.client.settings.KeyConflictContext.IN_GAME;

/**
 * Created by Brandon on 14/08/2014.
 */
public class KeyInputHandler {

    public static KeyBinding openPI;
    public static KeyBinding etGUI;
    public static KeyBinding etWorld;

    public static void init() {
        openPI = new KeyBinding("pi.key.open_pi", new CustomContext(IN_GAME, () -> openPI), InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_I, ProjectIntelligence.MODNAME);
        etGUI = new KeyBinding("pi.key.et_gui", new CustomContext(GUI, () -> etGUI), InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_I, ProjectIntelligence.MODNAME);
        etWorld = new KeyBinding("pi.key.et_world", new CustomContext(IN_GAME, () -> etWorld), KeyModifier.CONTROL, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_I, ProjectIntelligence.MODNAME);

        ClientRegistry.registerKeyBinding(openPI);
        ClientRegistry.registerKeyBinding(etGUI);
        ClientRegistry.registerKeyBinding(etWorld);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        onPress();
    }


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        onPress();
    }

    private void onPress() {
        if (etWorld.isDown()) {
            handleWorldInfo();
        } else if (openPI.isDown()) {
            ProjectIntelligence.proxy.openMainGui(null, null, null);
        }
    }

    //TODO Test This
    private void handleWorldInfo() {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        RayTraceResult result;
        RayTraceResult mcResult = mc.hitResult;
        RayTraceResult ccResult = RayTracer.retrace(player, RayTraceContext.BlockMode.OUTLINE, PIConfig.etCheckFluid ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);

        if (mcResult != null && mcResult.getType() == ENTITY) {
            result = mcResult;
        } else if (ccResult != null && ccResult.getType() != MISS) {
            result = ccResult;
        } else if (!PIConfig.etCheckFluid && (ccResult = RayTracer.retrace(player, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY)) != null && ccResult.getType() != MISS) {
            result = ccResult;
        } else {
            result = mcResult;
        }

        if (result == null || result.getType() == MISS) {
            ChatHelper.sendDeDupeIndexedClient(player, new TranslationTextComponent("pi.msg.et_miss"), 3131);
            return;
        }

        switch (result.getType()) {
            case BLOCK:
                BlockState state = mc.level.getBlockState(((BlockRayTraceResult)result).getBlockPos());
                Block block = state.getBlock();
                List<String> pages = null;

                Fluid fluid = Utils.lookupFluidForBlock(block);

                if (fluid != null) {
                    pages = PiAPI.getRelatedPages(fluid);
                } else {
                    ItemStack stack = block.getPickBlock(state, result, mc.level, ((BlockRayTraceResult)result).getBlockPos(), player);
                    if (!stack.isEmpty()) {
                        pages = PiAPI.getRelatedPages(stack);
                    }
                }

                if (pages == null || pages.isEmpty()) {
                    if (fluid != null) {
                        ChatHelper.sendDeDupeIndexedClient(player, new StringTextComponent(I18n.get("pi.msg.no_doc_found_for", I18n.get(fluid.getAttributes().getTranslationKey()))), 3131);
                    } else {
                        ChatHelper.sendDeDupeIndexedClient(player, new StringTextComponent(I18n.get("pi.msg.no_doc_found_for", I18n.get(block.asItem().getDescriptionId()))), 3131);
                    }
                } else {
                    PiAPI.openGui(null, pages);
                }

                break;
            case ENTITY:
                pages = PiAPI.getRelatedPages(((EntityRayTraceResult)result).getEntity().getType().getRegistryName().toString());
                if (pages.isEmpty()) {
                    ChatHelper.sendDeDupeIndexedClient(player, new StringTextComponent(I18n.get("pi.msg.no_doc_found_for", ((EntityRayTraceResult)result).getEntity().getDisplayName().getString())), 3131);
                } else {
                    PiAPI.openGui(null, pages);
                }
                break;
        }

    }

    private static class CustomContext implements IKeyConflictContext {
        private KeyConflictContext context;
        private Supplier<KeyBinding> binding;

        public CustomContext(KeyConflictContext context, Supplier<KeyBinding> binding) {
            this.context = context;
            this.binding = binding;
        }

        @Override
        public boolean isActive() {
            return context.isActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            if (!(other instanceof CustomContext)) {
                return other == context;
            }

            if (((CustomContext) other).context != context) {
                return false;
            }

            KeyBinding otherBind = ((CustomContext) other).binding.get();
            return otherBind.getKey().getNumericKeyValue() == binding.get().getKey().getNumericKeyValue() && otherBind.getKeyModifier() == binding.get().getKeyModifier();
        }
    }
}
