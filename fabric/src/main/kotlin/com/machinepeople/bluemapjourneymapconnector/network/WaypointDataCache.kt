package com.machinepeople.bluemapjourneymapconnector.network

import com.machinepeople.bluemapjourneymapconnector.data.SyncableWaypoint
import com.machinepeople.bluemapjourneymapconnector.data.WaypointDiff
import com.machinepeople.bluemapjourneymapconnector.journeymap.JourneyMapIntegration
import com.machinepeople.bluemapjourneymapconnector.sync.DiffCalculator
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

/**
 * Client-side cache for waypoint data.
 *
 * This cache holds the BlueMap waypoints received from the server
 * and provides diff calculation with JourneyMap waypoints.
 */
@Environment(EnvType.CLIENT)
object WaypointDataCache {

    private var blueMapWaypoints: List<SyncableWaypoint> = emptyList()
    private var lastUpdateTime: Long = 0

    /**
     * Update the cached BlueMap waypoints
     */
    fun updateBlueMapWaypoints(waypoints: List<SyncableWaypoint>) {
        blueMapWaypoints = waypoints
        lastUpdateTime = System.currentTimeMillis()
    }

    /**
     * Get the cached BlueMap waypoints
     */
    fun getBlueMapWaypoints(): List<SyncableWaypoint> = blueMapWaypoints

    /**
     * Get JourneyMap waypoints (live from the API)
     */
    fun getJourneyMapWaypoints(): List<SyncableWaypoint> {
        return JourneyMapIntegration.getInstance()?.getAllWaypoints() ?: emptyList()
    }

    /**
     * Calculate the diff between JourneyMap and BlueMap waypoints
     */
    fun calculateDiff(): WaypointDiff {
        val jmWaypoints = getJourneyMapWaypoints()
        val bmWaypoints = getBlueMapWaypoints()

        return DiffCalculator.calculate(jmWaypoints, bmWaypoints)
    }

    /**
     * Check if the cache has been populated
     */
    fun hasCachedData(): Boolean = lastUpdateTime > 0

    /**
     * Get the time since last update in milliseconds
     */
    fun timeSinceLastUpdate(): Long = System.currentTimeMillis() - lastUpdateTime

    /**
     * Clear the cache
     */
    fun clear() {
        blueMapWaypoints = emptyList()
        lastUpdateTime = 0
    }
}

