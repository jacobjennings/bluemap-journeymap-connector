package com.machinepeople.bluemapjourneymapconnector.client

import com.machinepeople.bluemapjourneymapconnector.BlueMapJourneyMapConnectorMod
import com.machinepeople.bluemapjourneymapconnector.client.gui.WaypointDiffScreen
import com.machinepeople.bluemapjourneymapconnector.network.BlueMapJourneyMapConnectorNetworking
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
 * Client-side mod initializer for BlueMap JourneyMap Connector.
 *
 * This handles:
 * - JourneyMap API integration (client-side waypoints)
 * - Keybinding for opening the diff GUI
 * - Client-side network packet handling
 */
@Environment(EnvType.CLIENT)
object BlueMapJourneyMapConnectorClient : ClientModInitializer {

    private val OPEN_SYNC_GUI_KEY: KeyMapping = KeyMapping(
        "key.bluemap-journeymap-connector.open_gui",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_J,
        KeyMapping.Category(Identifier.tryParse("category.bluemap-journeymap-connector")!!)
    )

    override fun onInitializeClient() {
        BlueMapJourneyMapConnectorMod.LOGGER.info("Initializing BlueMap JourneyMap Connector client...")

        // Register keybindings
        KeyBindingHelper.registerKeyBinding(OPEN_SYNC_GUI_KEY)

        // Register client network packet handlers
        BlueMapJourneyMapConnectorNetworking.registerClientPackets()

        // Handle keybinding press
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (OPEN_SYNC_GUI_KEY.consumeClick()) {
                openSyncGui(client)
            }
        }

        BlueMapJourneyMapConnectorMod.LOGGER.info("BlueMap JourneyMap Connector client initialized!")
    }

    private fun openSyncGui(client: Minecraft) {
        // Only open if in game
        if (client.level == null) return

        BlueMapJourneyMapConnectorMod.LOGGER.info("Opening BlueMap JourneyMap Connector GUI...")

        // Request waypoint data from server (BlueMap markers)
        BlueMapJourneyMapConnectorNetworking.requestBlueMapWaypoints()

        // Open the diff screen
        client.setScreen(WaypointDiffScreen())
    }
}

