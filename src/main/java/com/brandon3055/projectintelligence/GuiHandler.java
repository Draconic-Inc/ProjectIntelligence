//package com.brandon3055.projectintelligence;
//
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//import net.minecraftforge.fml.common.network.IGuiHandler;
//import net.minecraftforge.fml.common.network.NetworkRegistry;
//
//public class GuiHandler implements IGuiHandler {
//
//    public static final GuiHandler instance = new GuiHandler();
//
//    public static void initialize() {
//        NetworkRegistry.INSTANCE.registerGuiHandler(ProjectIntelligence.instance, instance);
//    }
//
//    @Override
//    public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
//        BlockPos pos = new BlockPos(x, y, z);
//        TileEntity tile = world.getTileEntity(pos);
//        return null;
//    }
//
//    @Override
//    public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
//        BlockPos pos = new BlockPos(x, y, z);
//        TileEntity tile = world.getTileEntity(pos);
//        return null;
//    }
//
//}
