package com.machinepeople.bluemapjourneymapconnector

import com.machinepeople.bluemapjourneymapconnector.bluemap.BlueMapIntegration
import com.machinepeople.bluemapjourneymapconnector.network.BlueMapJourneyMapConnectorNetworking
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.LoggerFactory

/**
 * Main server-side mod initializer for BlueMap JourneyMap Connector.
 *
 * This handles:
 * - BlueMap API integration (server-side markers)
 * - Network packet registration
 * - Server lifecycle events
 */
object BlueMapJourneyMapConnectorMod : ModInitializer {
    const val MOD_ID = "bluemap-journeymap-connector"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    private var blueMapIntegration: BlueMapIntegration? = null

    override fun onInitialize() {
        LOGGER.info("Initializing BlueMap JourneyMap Connector mod...")

        // Register network packets
        BlueMapJourneyMapConnectorNetworking.registerServerPackets()

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

        LOGGER.info("BlueMap JourneyMap Connector mod initialized!")
    }

    /**
     * Get the BlueMap integration instance, if available
     */
    fun getBlueMapIntegration(): BlueMapIntegration? = blueMapIntegration
}

