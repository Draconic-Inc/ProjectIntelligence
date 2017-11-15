package com.brandon3055.projectintelligence.client;


import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Random;

/**
 * Created by Brandon on 28/10/2014.
 */
public class ClientEventHandler {
    public static volatile int elapsedTicks;
    public static Minecraft mc;
    private static Random rand = new Random();

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.type != TickEvent.Type.CLIENT || event.side != Side.CLIENT) {
            return;
        }

        elapsedTicks++;
    }
}