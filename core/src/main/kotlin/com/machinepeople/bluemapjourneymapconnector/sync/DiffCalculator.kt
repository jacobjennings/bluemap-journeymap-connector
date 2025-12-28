package com.machinepeople.bluemapjourneymapconnector.sync

import com.machinepeople.bluemapjourneymapconnector.data.*

/**
 * Calculates the difference between JourneyMap and BlueMap waypoints
 */
object DiffCalculator {

    /**
     * Compare two lists of waypoints and produce a diff
     */
    fun calculate(
        journeyMapWaypoints: List<SyncableWaypoint>,
        blueMapWaypoints: List<SyncableWaypoint>
    ): WaypointDiff {
        val jmOnly = mutableListOf<SyncableWaypoint>()
        val bmOnly = mutableListOf<SyncableWaypoint>()
        val conflicts = mutableListOf<WaypointConflict>()
        val synced = mutableListOf<SyncableWaypoint>()

        // Create lookup maps by sync ID
        val jmByLocation = journeyMapWaypoints.associateBy { locationKey(it) }
        val bmByLocation = blueMapWaypoints.associateBy { locationKey(it) }

        // Find waypoints only in JourneyMap
        for (jm in journeyMapWaypoints) {
            val key = locationKey(jm)
            val bm = bmByLocation[key]

            when {
                bm == null -> jmOnly.add(jm)
                isIdentical(jm, bm) -> synced.add(jm)
                else -> conflicts.add(createConflict(jm, bm))
            }
        }

        // Find waypoints only in BlueMap
        for (bm in blueMapWaypoints) {
            val key = locationKey(bm)
            if (jmByLocation[key] == null) {
                bmOnly.add(bm)
            }
            // Conflicts and synced are already handled above
        }

        return WaypointDiff(
            journeyMapOnly = jmOnly,
            blueMapOnly = bmOnly,
            conflicts = conflicts,
            synced = synced
        )
    }

    /**
     * Create a unique location key for matching waypoints
     */
    private fun locationKey(waypoint: SyncableWaypoint): String {
        return "${waypoint.x}_${waypoint.y}_${waypoint.z}_${waypoint.dimension}"
    }

    /**
     * Check if two waypoints are identical (same name and color)
     */
    private fun isIdentical(jm: SyncableWaypoint, bm: SyncableWaypoint): Boolean {
        return jm.name.equals(bm.name, ignoreCase = true) &&
                jm.color == bm.color &&
                jm.enabled == bm.enabled
    }

    /**
     * Create a conflict record with details about differences
     */
    private fun createConflict(jm: SyncableWaypoint, bm: SyncableWaypoint): WaypointConflict {
        val differences = mutableListOf<String>()

        if (!jm.name.equals(bm.name, ignoreCase = true)) {
            differences.add("Name: '${jm.name}' vs '${bm.name}'")
        }
        if (jm.color != bm.color) {
            differences.add("Color: #${jm.color.toString(16)} vs #${bm.color.toString(16)}")
        }
        if (jm.enabled != bm.enabled) {
            differences.add("Enabled: ${jm.enabled} vs ${bm.enabled}")
        }
        if (jm.icon != bm.icon) {
            differences.add("Icon differs")
        }

        return WaypointConflict(
            journeyMapVersion = jm,
            blueMapVersion = bm,
            differences = differences
        )
    }
}

