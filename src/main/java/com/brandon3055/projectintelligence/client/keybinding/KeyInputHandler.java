package com.brandon3055.projectintelligence.client.keybinding;

import codechicken.lib.raytracer.RayTracer;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.projectintelligence.ProjectIntelligence;
import com.brandon3055.projectintelligence.api.PiAPI;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

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
        openPI = new KeyBinding("pi.key.open_pi", new CustomContext(IN_GAME, () -> openPI), Keyboard.KEY_I, ProjectIntelligence.MODNAME);
        etGUI = new KeyBinding("pi.key.et_gui", new CustomContext(GUI, () -> etGUI), Keyboard.KEY_I, ProjectIntelligence.MODNAME);
        etWorld = new KeyBinding("pi.key.et_world", new CustomContext(IN_GAME, () -> etWorld), KeyModifier.CONTROL, Keyboard.KEY_I, ProjectIntelligence.MODNAME);

        ClientRegistry.registerKeyBinding(openPI);
        ClientRegistry.registerKeyBinding(etGUI);
        ClientRegistry.registerKeyBinding(etWorld);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        onPress();
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        onPress();
    }

    private void onPress() {
        if (etWorld.isPressed()) {
            handleWorldInfo();
        }
        else if (openPI.isPressed()) {
            ProjectIntelligence.proxy.openMainGui(null, null, null);
        }
    }

    private void handleWorldInfo() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        RayTraceResult result;
        RayTraceResult mcResult = mc.objectMouseOver;
        RayTraceResult ccResult = RayTracer.retrace(player, PIConfig.etCheckFluid);

        if (mcResult != null && mcResult.typeOfHit == ENTITY) {
            result = mcResult;
        }
        else if (ccResult != null && ccResult.typeOfHit != MISS) {
            result = ccResult;
        }
        else if (!PIConfig.etCheckFluid && (ccResult = RayTracer.retrace(player, true)) != null && ccResult.typeOfHit != MISS) {
            result = ccResult;
        }
        else {
            result = mcResult;
        }

        if (result == null || result.typeOfHit == MISS) {
            ChatHelper.indexedMsg(player, I18n.format("pi.msg.et_miss"));
            return;
        }

        switch (result.typeOfHit) {
            case BLOCK:
                IBlockState state = mc.world.getBlockState(result.getBlockPos());
                Block block = state.getBlock();
                List<String> pages = null;

                Fluid fluid = FluidRegistry.lookupFluidForBlock(block);

                if (fluid != null) {
                    pages = PiAPI.getRelatedPages(fluid);
                }
                else {
                    ItemStack stack = block.getPickBlock(state, result, mc.world, result.getBlockPos(), player);
                    if (!stack.isEmpty()) {
                        pages = PiAPI.getRelatedPages(stack);
                    }
                }

                if (pages == null || pages.isEmpty()) {
                    if (fluid != null) {
                        ChatHelper.indexedMsg(player, I18n.format("pi.msg.no_doc_found_for", I18n.format(fluid.getUnlocalizedName())));
                    }
                    else {
                        ChatHelper.indexedMsg(player, I18n.format("pi.msg.no_doc_found_for", I18n.format(Item.getItemFromBlock(block).getUnlocalizedName() + ".name")));
                    }
                }
                else {
                    PiAPI.openGui(null, pages);
                }

                break;
            case ENTITY:
                pages = PiAPI.getRelatedPages(EntityList.getKey(result.entityHit) + "");
                if (pages.isEmpty()) {
                    ChatHelper.indexedMsg(player, I18n.format("pi.msg.no_doc_found_for", result.entityHit.getDisplayName().getFormattedText()));
                }
                else {
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
            return otherBind.getKeyCode() == binding.get().getKeyCode() && otherBind.getKeyModifier() == binding.get().getKeyModifier();
        }
    }
}
