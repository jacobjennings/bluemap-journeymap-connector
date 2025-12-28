package com.machinepeople.bluemapjourneymapconnector.journeymap

import com.machinepeople.bluemapjourneymapconnector.BlueMapJourneyMapConnectorMod
import com.machinepeople.bluemapjourneymapconnector.data.SyncableWaypoint
import com.machinepeople.bluemapjourneymapconnector.data.WaypointSource
import journeymap.api.v2.client.IClientAPI
import journeymap.api.v2.client.IClientPlugin
import journeymap.api.v2.client.JourneyMapPlugin
import journeymap.api.v2.common.waypoint.Waypoint
import journeymap.api.v2.common.waypoint.WaypointFactory
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

/**
 * JourneyMap API plugin for BlueMap JourneyMap Connector.
 *
 * This class is automatically discovered and loaded by JourneyMap via the @JourneyMapPlugin annotation.
 * It provides access to JourneyMap's waypoint system.
 */
@JourneyMapPlugin(apiVersion = "2.0.0")
class JourneyMapIntegration : IClientPlugin {

    init {
        println("BlueMapConnector: JourneyMapIntegration class instantiated!")
    }

    companion object {
        private var instance: JourneyMapIntegration? = null

        init {
            println("BlueMapConnector: JourneyMapIntegration companion object initialized!")
        }

        /**
         * Get the singleton instance of this integration
         */
        fun getInstance(): JourneyMapIntegration? = instance

        /**
         * Check if JourneyMap integration is available
         */
        fun isAvailable(): Boolean = instance?.api != null
    }

    private var api: IClientAPI? = null

    override fun initialize(jmClientApi: IClientAPI) {
        println("BlueMapConnector: JourneyMap API initialize() called!")
        BlueMapJourneyMapConnectorMod.LOGGER.info("JourneyMap API initialized for BlueMap JourneyMap Connector!")
        this.api = jmClientApi
        instance = this
    }

    override fun getModId(): String = BlueMapJourneyMapConnectorMod.MOD_ID

    /**
     * Get all waypoints from JourneyMap as SyncableWaypoints
     */
    fun getAllWaypoints(): List<SyncableWaypoint> {
        val jmApi = api ?: run {
            BlueMapJourneyMapConnectorMod.LOGGER.warn("JourneyMap API not available - plugin not initialized")
            return emptyList()
        }

        return try {
            val waypoints = jmApi.allWaypoints
            BlueMapJourneyMapConnectorMod.LOGGER.info("Fetched ${waypoints.size} waypoints from JourneyMap")
            waypoints.map { it.toSyncable() }
        } catch (e: Exception) {
            BlueMapJourneyMapConnectorMod.LOGGER.error("Failed to get waypoints from JourneyMap", e)
            emptyList()
        }
    }

    /**
     * Get waypoints for a specific dimension
     */
    fun getWaypointsForDimension(dimension: ResourceKey<Level>): List<SyncableWaypoint> {
        val jmApi = api ?: return emptyList()

        return try {
            jmApi.getAllWaypoints(dimension).map { it.toSyncable() }
        } catch (e: Exception) {
            BlueMapJourneyMapConnectorMod.LOGGER.error("Failed to get waypoints for dimension $dimension", e)
            emptyList()
        }
    }

    fun addWaypoint(syncable: SyncableWaypoint): Boolean {
        val jmApi = api ?: run {
            BlueMapJourneyMapConnectorMod.LOGGER.warn("JourneyMap API not available - cannot add waypoint")
            return false
        }

        return try {
            BlueMapJourneyMapConnectorMod.LOGGER.info("Creating JourneyMap waypoint: ${syncable.name} in ${syncable.dimension}")
            val waypoint = WaypointFactory.createClientWaypoint(
                BlueMapJourneyMapConnectorMod.MOD_ID,
                BlockPos(syncable.x, syncable.y, syncable.z),
                syncable.name,
                syncable.dimension,
                true  // persistent
            )

            waypoint.color = syncable.color
            waypoint.isEnabled = syncable.enabled

            jmApi.addWaypoint(BlueMapJourneyMapConnectorMod.MOD_ID, waypoint)
            BlueMapJourneyMapConnectorMod.LOGGER.info("Successfully added waypoint '${syncable.name}' to JourneyMap")
            true
        } catch (e: Exception) {
            BlueMapJourneyMapConnectorMod.LOGGER.error("Failed to add waypoint to JourneyMap", e)
            false
        }
    }

    /**
     * Remove a waypoint from JourneyMap
     */
    fun removeWaypoint(syncable: SyncableWaypoint): Boolean {
        val jmApi = api ?: return false

        return try {
            val existingWaypoint = jmApi.getWaypoints(BlueMapJourneyMapConnectorMod.MOD_ID)
                .find { it.guid == syncable.sourceId || matchesLocation(it, syncable) }

            if (existingWaypoint != null) {
                jmApi.removeWaypoint(BlueMapJourneyMapConnectorMod.MOD_ID, existingWaypoint)
                BlueMapJourneyMapConnectorMod.LOGGER.info("Removed waypoint '${syncable.name}' from JourneyMap")
                true
            } else {
                BlueMapJourneyMapConnectorMod.LOGGER.warn("Waypoint '${syncable.name}' not found in JourneyMap")
                false
            }
        } catch (e: Exception) {
            BlueMapJourneyMapConnectorMod.LOGGER.error("Failed to remove waypoint from JourneyMap", e)
            false
        }
    }

    private fun matchesLocation(jmWaypoint: Waypoint, syncable: SyncableWaypoint): Boolean {
        return jmWaypoint.x == syncable.x &&
                jmWaypoint.y == syncable.y &&
                jmWaypoint.z == syncable.z &&
                jmWaypoint.primaryDimension == syncable.dimension
    }

    /**
     * Convert a JourneyMap Waypoint to our SyncableWaypoint format
     */
    private fun Waypoint.toSyncable(): SyncableWaypoint {
        return SyncableWaypoint(
            id = SyncableWaypoint.generateSyncId(name, x, y, z, primaryDimension),
            name = name,
            x = x,
            y = y,
            z = z,
            dimension = primaryDimension,
            color = color,
            icon = null, // iconResourceLocation -> icon
            enabled = isEnabled,
            source = WaypointSource.JOURNEYMAP,
            sourceId = guid
        )
    }
}

