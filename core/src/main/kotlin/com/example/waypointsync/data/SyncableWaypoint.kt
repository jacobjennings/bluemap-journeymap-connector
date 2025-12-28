package com.example.waypointsync.data

import kotlinx.serialization.Serializable

/**
 * A unified waypoint representation that can be converted to/from
 * both JourneyMap waypoints and BlueMap POI markers.
 *
 * This is the common data model used for synchronization.
 */
@Serializable
data class SyncableWaypoint(
    /** Unique identifier for this waypoint (used for sync tracking) */
    val id: String,

    /** Display name of the waypoint */
    val name: String,

    /** X coordinate in the world */
    val x: Int,

    /** Y coordinate in the world */
    val y: Int,

    /** Z coordinate in the world */
    val z: Int,

    /** Dimension identifier (e.g., "minecraft:overworld") */
    val dimension: String,

    /** Color as RGB integer (e.g., 0xFF0000 for red) */
    val color: Int = 0xFFFFFF,

    /** Optional icon path/URL */
    val icon: String? = null,

    /** Whether this waypoint is enabled/visible */
    val enabled: Boolean = true,

    /** Source of this waypoint */
    val source: WaypointSource,

    /** Original ID from the source system (for updates) */
    val sourceId: String? = null
) {
    companion object {
        /**
         * Generate a unique sync ID from waypoint properties
         */
        fun generateSyncId(name: String, x: Int, y: Int, z: Int, dimension: String): String {
            return "${name.lowercase().replace(" ", "_")}_${x}_${y}_${z}_${dimension.substringAfter(":")}"
        }
    }

    /**
     * Check if two waypoints represent the same location
     */
    fun isSameLocation(other: SyncableWaypoint): Boolean {
        return x == other.x && y == other.y && z == other.z && dimension == other.dimension
    }

    /**
     * Check if this waypoint matches another by name and location
     */
    fun matches(other: SyncableWaypoint): Boolean {
        return name.equals(other.name, ignoreCase = true) && isSameLocation(other)
    }
}

/**
 * Enum representing the source of a waypoint
 */
@Serializable
enum class WaypointSource {
    JOURNEYMAP,
    BLUEMAP,
    SYNCED  // Created by this mod during sync
}

