package com.example.waypointsync

import com.example.waypointsync.bluemap.BlueMapIntegration
import com.example.waypointsync.network.WaypointSyncNetworking
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.LoggerFactory

/**
 * Main server-side mod initializer for Waypoint Sync.
 *
 * This handles:
 * - BlueMap API integration (server-side markers)
 * - Network packet registration
 * - Server lifecycle events
 */
object WaypointSyncMod : ModInitializer {
    const val MOD_ID = "waypointsync"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    private var blueMapIntegration: BlueMapIntegration? = null

    override fun onInitialize() {
        LOGGER.info("Initializing Waypoint Sync mod...")

        // Register network packets
        WaypointSyncNetworking.registerServerPackets()

        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            LOGGER.info("Server starting, initializing BlueMap integration...")
            blueMapIntegration = BlueMapIntegration()
        }

        ServerLifecycleEvents.SERVER_STOPPED.register { server ->
            LOGGER.info("Server stopping, cleaning up BlueMap integration...")
            blueMapIntegration?.disable()
            blueMapIntegration = null
        }

        LOGGER.info("Waypoint Sync mod initialized!")
    }

    /**
     * Get the BlueMap integration instance, if available
     */
    fun getBlueMapIntegration(): BlueMapIntegration? = blueMapIntegration
}

