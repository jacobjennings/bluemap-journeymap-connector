package com.example.waypointsync.network

import com.example.waypointsync.WaypointSyncMod
import com.example.waypointsync.data.SyncAction
import com.example.waypointsync.data.SyncOperation
import com.example.waypointsync.data.SyncableWaypoint
import com.example.waypointsync.data.WaypointSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
// import net.minecraft.resources.ResourceLocation // Try to find it via IDE or assume core
import net.minecraft.resources.Identifier

/**
 * Network packet handling for Waypoint Sync.
 *
 * Handles client <-> server communication for:
 * - Requesting BlueMap waypoints (client -> server)
 * - Sending BlueMap waypoints to client (server -> client)
 * - Sync operations (client -> server for BlueMap, server -> client for JourneyMap)
 */
object WaypointSyncNetworking {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Packet IDs
    val REQUEST_BLUEMAP_WAYPOINTS_ID = Identifier.tryParse("${WaypointSyncMod.MOD_ID}:request_bm_waypoints")!!
    val BLUEMAP_WAYPOINTS_RESPONSE_ID = Identifier.tryParse("${WaypointSyncMod.MOD_ID}:bm_waypoints_response")!!
    val SYNC_TO_BLUEMAP_ID = Identifier.tryParse("${WaypointSyncMod.MOD_ID}:sync_to_bluemap")!!
    val SYNC_TO_JOURNEYMAP_ID = Identifier.tryParse("${WaypointSyncMod.MOD_ID}:sync_to_journeymap")!!

    // Packet payloads
    data class RequestBlueMapWaypointsPayload(val dimension: String?) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

        companion object {
            val TYPE: CustomPacketPayload.Type<RequestBlueMapWaypointsPayload> =
                CustomPacketPayload.Type(REQUEST_BLUEMAP_WAYPOINTS_ID)

            val CODEC: StreamCodec<FriendlyByteBuf, RequestBlueMapWaypointsPayload> =
                StreamCodec.of(
                    { buf, payload -> buf.writeUtf(payload.dimension ?: "") },
                    { buf -> RequestBlueMapWaypointsPayload(buf.readUtf().takeIf { it.isNotEmpty() }) }
                )
        }
    }

    data class BlueMapWaypointsResponsePayload(val waypoints: List<SyncableWaypoint>) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

        companion object {
            val TYPE: CustomPacketPayload.Type<BlueMapWaypointsResponsePayload> =
                CustomPacketPayload.Type(BLUEMAP_WAYPOINTS_RESPONSE_ID)

            val CODEC: StreamCodec<FriendlyByteBuf, BlueMapWaypointsResponsePayload> =
                StreamCodec.of(
                    { buf, payload ->
                        val jsonStr = Json.encodeToString(payload.waypoints)
                        buf.writeUtf(jsonStr, 262144)  // Max 256KB
                    },
                    { buf ->
                        val jsonStr = buf.readUtf(262144)
                        val waypoints = Json.decodeFromString<List<SyncableWaypoint>>(jsonStr)
                        BlueMapWaypointsResponsePayload(waypoints)
                    }
                )
        }
    }

    data class SyncToBlueMapPayload(val operations: List<SyncOperation>) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

        companion object {
            val TYPE: CustomPacketPayload.Type<SyncToBlueMapPayload> =
                CustomPacketPayload.Type(SYNC_TO_BLUEMAP_ID)

            val CODEC: StreamCodec<FriendlyByteBuf, SyncToBlueMapPayload> =
                StreamCodec.of(
                    { buf, payload ->
                        val jsonStr = Json.encodeToString(payload.operations)
                        buf.writeUtf(jsonStr, 262144)
                    },
                    { buf ->
                        val jsonStr = buf.readUtf(262144)
                        val operations = Json.decodeFromString<List<SyncOperation>>(jsonStr)
                        SyncToBlueMapPayload(operations)
                    }
                )
        }
    }

    data class SyncToJourneyMapPayload(val operations: List<SyncOperation>) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

        companion object {
            val TYPE: CustomPacketPayload.Type<SyncToJourneyMapPayload> =
                CustomPacketPayload.Type(SYNC_TO_JOURNEYMAP_ID)

            val CODEC: StreamCodec<FriendlyByteBuf, SyncToJourneyMapPayload> =
                StreamCodec.of(
                    { buf, payload ->
                        val jsonStr = Json.encodeToString(payload.operations)
                        buf.writeUtf(jsonStr, 262144)
                    },
                    { buf ->
                        val jsonStr = buf.readUtf(262144)
                        val operations = Json.decodeFromString<List<SyncOperation>>(jsonStr)
                        SyncToJourneyMapPayload(operations)
                    }
                )
        }
    }

    /**
     * Register server-side packet handlers
     */
    fun registerServerPackets() {
        // Register payload types
        PayloadTypeRegistry.playC2S().register(RequestBlueMapWaypointsPayload.TYPE, RequestBlueMapWaypointsPayload.CODEC)
        PayloadTypeRegistry.playC2S().register(SyncToBlueMapPayload.TYPE, SyncToBlueMapPayload.CODEC)
        PayloadTypeRegistry.playS2C().register(BlueMapWaypointsResponsePayload.TYPE, BlueMapWaypointsResponsePayload.CODEC)
        PayloadTypeRegistry.playS2C().register(SyncToJourneyMapPayload.TYPE, SyncToJourneyMapPayload.CODEC)

        // Handle request for BlueMap waypoints
        ServerPlayNetworking.registerGlobalReceiver(RequestBlueMapWaypointsPayload.TYPE) { payload, context ->
            val player = context.player()
            WaypointSyncMod.LOGGER.info("Received BlueMap waypoints request from ${player.name.string}")

            val blueMapIntegration = WaypointSyncMod.getBlueMapIntegration()
            if (blueMapIntegration == null || !blueMapIntegration.isAvailable()) {
                WaypointSyncMod.LOGGER.warn("BlueMap integration not available")
                context.responseSender().sendPacket(BlueMapWaypointsResponsePayload(emptyList()))
                return@registerGlobalReceiver
            }

            val waypoints = if (payload.dimension != null) {
                blueMapIntegration.getWaypointsForDimension(payload.dimension)
            } else {
                blueMapIntegration.getAllWaypoints()
            }

            context.responseSender().sendPacket(BlueMapWaypointsResponsePayload(waypoints))
        }

        // Handle sync operations to BlueMap
        ServerPlayNetworking.registerGlobalReceiver(SyncToBlueMapPayload.TYPE) { payload, context ->
            val player = context.player()
            WaypointSyncMod.LOGGER.info("Received sync to BlueMap request from ${player.name.string}")

            val blueMapIntegration = WaypointSyncMod.getBlueMapIntegration()
            if (blueMapIntegration == null || !blueMapIntegration.isAvailable()) {
                WaypointSyncMod.LOGGER.warn("BlueMap integration not available")
                return@registerGlobalReceiver
            }

            for (operation in payload.operations) {
                when (operation.action) {
                    SyncAction.CREATE -> blueMapIntegration.addWaypoint(operation.waypoint)
                    SyncAction.DELETE -> blueMapIntegration.removeWaypoint(operation.waypoint)
                    SyncAction.UPDATE -> {
                        blueMapIntegration.removeWaypoint(operation.waypoint)
                        blueMapIntegration.addWaypoint(operation.waypoint)
                    }
                }
            }
        }
    }

    /**
     * Register client-side packet handlers
     */
    @Environment(EnvType.CLIENT)
    fun registerClientPackets() {
        // Handle BlueMap waypoints response
        ClientPlayNetworking.registerGlobalReceiver(BlueMapWaypointsResponsePayload.TYPE) { payload, context ->
            WaypointSyncMod.LOGGER.info("Received ${payload.waypoints.size} BlueMap waypoints from server")
            WaypointDataCache.updateBlueMapWaypoints(payload.waypoints)
        }

        // Handle sync operations to JourneyMap
        ClientPlayNetworking.registerGlobalReceiver(SyncToJourneyMapPayload.TYPE) { payload, context ->
            WaypointSyncMod.LOGGER.info("Received sync to JourneyMap request from server")

            val jmIntegration = com.example.waypointsync.journeymap.JourneyMapIntegration.getInstance()
            if (jmIntegration == null) {
                WaypointSyncMod.LOGGER.warn("JourneyMap integration not available")
                return@registerGlobalReceiver
            }

            for (operation in payload.operations) {
                when (operation.action) {
                    SyncAction.CREATE -> jmIntegration.addWaypoint(operation.waypoint)
                    SyncAction.DELETE -> jmIntegration.removeWaypoint(operation.waypoint)
                    SyncAction.UPDATE -> {
                        jmIntegration.removeWaypoint(operation.waypoint)
                        jmIntegration.addWaypoint(operation.waypoint)
                    }
                }
            }
        }
    }

    /**
     * Request BlueMap waypoints from the server
     */
    @Environment(EnvType.CLIENT)
    fun requestBlueMapWaypoints(dimension: String? = null) {
        ClientPlayNetworking.send(RequestBlueMapWaypointsPayload(dimension))
    }

    /**
     * Send sync operations to the server (for BlueMap)
     */
    @Environment(EnvType.CLIENT)
    fun syncToBlueMap(operations: List<SyncOperation>) {
        ClientPlayNetworking.send(SyncToBlueMapPayload(operations))
    }
}

