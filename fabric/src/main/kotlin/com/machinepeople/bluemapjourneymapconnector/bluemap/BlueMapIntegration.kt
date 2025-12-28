package com.machinepeople.bluemapjourneymapconnector.bluemap

import com.machinepeople.bluemapjourneymapconnector.BlueMapJourneyMapConnectorMod
import com.machinepeople.bluemapjourneymapconnector.data.SyncableWaypoint
import com.machinepeople.bluemapjourneymapconnector.data.WaypointSource
import com.flowpowered.math.vector.Vector3d
import de.bluecolored.bluemap.api.BlueMapAPI
import de.bluecolored.bluemap.api.BlueMapMap
import de.bluecolored.bluemap.api.markers.MarkerSet
import de.bluecolored.bluemap.api.markers.POIMarker
import java.awt.Color
import java.util.function.Consumer

/**
 * BlueMap API integration for BlueMap JourneyMap Connector.
 *
 * This class handles server-side marker management in BlueMap.
 * BlueMap API uses a consumer-based initialization pattern.
 */
class BlueMapIntegration {

    companion object {
        const val MARKER_SET_ID = "bluemap-journeymap-connector"
        const val MARKER_SET_LABEL = "Synced Waypoints"

        // Default POI icon (can be customized)
        const val DEFAULT_ICON = "assets/poi.svg"
    }

    private var api: BlueMapAPI? = null
    private val onEnableConsumer: Consumer<BlueMapAPI>
    private val onDisableConsumer: Consumer<BlueMapAPI>

    init {
        onEnableConsumer = Consumer { blueMapApi ->
            BlueMapJourneyMapConnectorMod.LOGGER.info("BlueMap API enabled, connecting...")
            api = blueMapApi
            ensureMarkerSets()
        }

        onDisableConsumer = Consumer { _ ->
            BlueMapJourneyMapConnectorMod.LOGGER.info("BlueMap API disabled")
            api = null
        }

        BlueMapAPI.onEnable(onEnableConsumer)
        BlueMapAPI.onDisable(onDisableConsumer)
    }

    /**
     * Check if BlueMap integration is available
     */
    fun isAvailable(): Boolean = api != null

    /**
     * Disable the integration and unregister listeners
     */
    fun disable() {
        BlueMapAPI.unregisterListener(onEnableConsumer)
        BlueMapAPI.unregisterListener(onDisableConsumer)
        api = null
    }

    /**
     * Ensure marker sets exist on all maps
     */
    private fun ensureMarkerSets() {
        val blueMapApi = api ?: return

        for (map in blueMapApi.maps) {
            getOrCreateMarkerSet(map)
        }
    }

    /**
     * Get or create the marker set for synced waypoints
     */
    private fun getOrCreateMarkerSet(map: BlueMapMap): MarkerSet {
        val existingSet = map.markerSets[MARKER_SET_ID]
        if (existingSet != null) {
            return existingSet
        }

        val newSet = MarkerSet.builder()
            .label(MARKER_SET_LABEL)
            .toggleable(true)
            .defaultHidden(false)
            .build()

        map.markerSets[MARKER_SET_ID] = newSet
        BlueMapJourneyMapConnectorMod.LOGGER.info("Created marker set '$MARKER_SET_ID' on map '${map.id}'")
        return newSet
    }

    /**
     * Get all POI markers from BlueMap as SyncableWaypoints
     */
    fun getAllWaypoints(): List<SyncableWaypoint> {
        val blueMapApi = api ?: run {
            BlueMapJourneyMapConnectorMod.LOGGER.warn("BlueMap API not available")
            return emptyList()
        }

        val waypoints = mutableListOf<SyncableWaypoint>()

        for (map in blueMapApi.maps) {
            val dimension = extractDimension(map)
            
            // Get markers from all marker sets (not just ours)
            for ((setId, markerSet) in map.markerSets) {
                for ((markerId, marker) in markerSet.markers) {
                    if (marker is POIMarker) {
                        waypoints.add(marker.toSyncable(markerId, dimension, setId))
                    }
                }
            }
        }

        return waypoints
    }

    /**
     * Get waypoints for a specific dimension
     */
    fun getWaypointsForDimension(dimension: String): List<SyncableWaypoint> {
        return getAllWaypoints().filter { it.dimension == dimension }
    }

    /**
     * Add a waypoint to BlueMap from a SyncableWaypoint
     */
    fun addWaypoint(syncable: SyncableWaypoint): Boolean {
        val blueMapApi = api ?: run {
            BlueMapJourneyMapConnectorMod.LOGGER.warn("BlueMap API not available")
            return false
        }

        return try {
            // Find the map for this dimension
            val map = findMapForDimension(blueMapApi, syncable.dimension)
            if (map == null) {
                BlueMapJourneyMapConnectorMod.LOGGER.warn("No BlueMap map found for dimension '${syncable.dimension}'")
                return false
            }

            val markerSet = getOrCreateMarkerSet(map)
            val markerId = generateMarkerId(syncable)

            val marker = POIMarker.builder()
                .label(syncable.name)
                .position(Vector3d(syncable.x.toDouble(), syncable.y.toDouble(), syncable.z.toDouble()))
                .icon(DEFAULT_ICON, 0, 0)  // Default anchor at top-left
                .build()

            markerSet.markers[markerId] = marker
            BlueMapJourneyMapConnectorMod.LOGGER.info("Added marker '${syncable.name}' to BlueMap")
            true
        } catch (e: Exception) {
            BlueMapJourneyMapConnectorMod.LOGGER.error("Failed to add marker to BlueMap", e)
            false
        }
    }

    /**
     * Remove a waypoint from BlueMap
     */
    fun removeWaypoint(syncable: SyncableWaypoint): Boolean {
        val blueMapApi = api ?: return false

        return try {
            val map = findMapForDimension(blueMapApi, syncable.dimension) ?: return false
            val markerSet = map.markerSets[MARKER_SET_ID] ?: return false
            val markerId = syncable.sourceId ?: generateMarkerId(syncable)

            val removed = markerSet.markers.remove(markerId)
            if (removed != null) {
                BlueMapJourneyMapConnectorMod.LOGGER.info("Removed marker '${syncable.name}' from BlueMap")
                true
            } else {
                BlueMapJourneyMapConnectorMod.LOGGER.warn("Marker '${syncable.name}' not found in BlueMap")
                false
            }
        } catch (e: Exception) {
            BlueMapJourneyMapConnectorMod.LOGGER.error("Failed to remove marker from BlueMap", e)
            false
        }
    }

    /**
     * Find the BlueMap map for a given dimension
     */
    private fun findMapForDimension(blueMapApi: BlueMapAPI, dimension: String): BlueMapMap? {
        return blueMapApi.maps.find { map ->
            extractDimension(map) == dimension
        }
    }

    /**
     * Extract the dimension from a BlueMap map
     */
    private fun extractDimension(map: BlueMapMap): String {
        // BlueMap maps are typically named like "world", "world_nether", "world_the_end"
        // We need to convert these to Minecraft dimension IDs
        val worldId = map.world.id

        // Common mappings
        return when {
            worldId.contains("nether") -> "minecraft:the_nether"
            worldId.contains("end") -> "minecraft:the_end"
            else -> "minecraft:overworld"
        }
    }

    /**
     * Generate a unique marker ID for a waypoint
     */
    private fun generateMarkerId(syncable: SyncableWaypoint): String {
        return "wps_${syncable.name.lowercase().replace(" ", "_")}_${syncable.x}_${syncable.y}_${syncable.z}"
    }

    /**
     * Convert a BlueMap POIMarker to our SyncableWaypoint format
     */
    private fun POIMarker.toSyncable(
        markerId: String,
        dimension: String,
        markerSetId: String
    ): SyncableWaypoint {
        val pos = this.position

        return SyncableWaypoint(
            id = SyncableWaypoint.generateSyncId(
                this.label,
                pos.floorX,
                pos.floorY,
                pos.floorZ,
                dimension
            ),
            name = this.label,
            x = pos.floorX,
            y = pos.floorY,
            z = pos.floorZ,
            dimension = dimension,
            color = 0xFFFFFF,  // POI markers don't have built-in color, use default
            icon = this.iconAddress,  // Use the getter method
            enabled = true,
            source = WaypointSource.BLUEMAP,
            sourceId = markerId
        )
    }
}

