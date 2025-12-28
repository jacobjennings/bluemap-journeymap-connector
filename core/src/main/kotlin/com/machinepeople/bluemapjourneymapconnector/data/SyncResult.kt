package com.machinepeople.bluemapjourneymapconnector.data

import kotlinx.serialization.Serializable

/**
 * Represents the result of comparing waypoints between two sources
 */
@Serializable
data class WaypointDiff(
    /** Waypoints only in JourneyMap (can be copied to BlueMap) */
    val journeyMapOnly: List<SyncableWaypoint>,

    /** Waypoints only in BlueMap (can be copied to JourneyMap) */
    val blueMapOnly: List<SyncableWaypoint>,

    /** Waypoints that exist in both but with different properties */
    val conflicts: List<WaypointConflict>,

    /** Waypoints that are identical in both */
    val synced: List<SyncableWaypoint>
) {
    companion object {
        fun empty() = WaypointDiff(
            journeyMapOnly = emptyList(),
            blueMapOnly = emptyList(),
            conflicts = emptyList(),
            synced = emptyList()
        )
    }

    val hasChanges: Boolean
        get() = journeyMapOnly.isNotEmpty() || blueMapOnly.isNotEmpty() || conflicts.isNotEmpty()
}

/**
 * Represents a conflict where the same waypoint exists in both systems
 * but with different properties
 */
@Serializable
data class WaypointConflict(
    val journeyMapVersion: SyncableWaypoint,
    val blueMapVersion: SyncableWaypoint,
    val differences: List<String>  // Human-readable list of differences
)

/**
 * Represents a sync operation request
 */
@Serializable
data class SyncOperation(
    val action: SyncAction,
    val waypoint: SyncableWaypoint,
    val targetSource: WaypointSource
)

@Serializable
enum class SyncAction {
    CREATE,
    UPDATE,
    DELETE
}

/**
 * Result of a sync operation
 */
@Serializable
data class SyncOperationResult(
    val operation: SyncOperation,
    val success: Boolean,
    val message: String? = null
)

