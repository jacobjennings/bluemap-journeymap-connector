package com.example.waypointsync.client

import com.example.waypointsync.WaypointSyncMod
import com.example.waypointsync.client.gui.WaypointDiffScreen
import com.example.waypointsync.network.WaypointSyncNetworking
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

import net.minecraft.resources.Identifier

/**
 * Client-side mod initializer for Waypoint Sync.
 *
 * This handles:
 * - JourneyMap API integration (client-side waypoints)
 * - Keybinding for opening the diff GUI
 * - Client-side network packet handling
 */
@Environment(EnvType.CLIENT)
object WaypointSyncClient : ClientModInitializer {

    private val OPEN_SYNC_GUI_KEY: KeyMapping = KeyMapping(
        "key.waypointsync.open_gui",
        GLFW.GLFW_KEY_J,
        KeyMapping.Category(Identifier.tryParse("category.waypointsync")!!)
    )

    override fun onInitializeClient() {
        WaypointSyncMod.LOGGER.info("Initializing Waypoint Sync client...")

        // Register keybindings
        KeyBindingHelper.registerKeyBinding(OPEN_SYNC_GUI_KEY)

        // Register client network packets
        WaypointSyncNetworking.registerClientPackets()

        // Handle keybinding press (requires Shift to be held)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (OPEN_SYNC_GUI_KEY.consumeClick()) {
                // Only open if Shift is held
                if (hasShiftDown()) {
                    openSyncGui(client)
                }
            }
        }

        WaypointSyncMod.LOGGER.info("Waypoint Sync client initialized!")
    }

    private fun openSyncGui(client: Minecraft) {
        // Only open if in game
        if (client.level == null) return

        WaypointSyncMod.LOGGER.info("Opening Waypoint Sync GUI...")

        // Request waypoint data from server (BlueMap markers)
        WaypointSyncNetworking.requestBlueMapWaypoints()

        // Open the diff screen
        client.setScreen(WaypointDiffScreen())
    }

    /**
     * Check if Shift key is currently held down
     */
    private fun hasShiftDown(): Boolean {
        val window = Minecraft.getInstance().window
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT)
    }
}

