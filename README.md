# Waypoint Sync

A Fabric mod that synchronizes waypoints between [JourneyMap](https://www.curseforge.com/minecraft/mc-mods/journeymap) and [BlueMap](https://bluemap.bluecolored.de/).

## Features

- **Diff View**: See which waypoints exist only in JourneyMap, only in BlueMap, or both
- **Bi-directional Sync**: Copy waypoints from JourneyMap → BlueMap or BlueMap → JourneyMap
- **Individual or Bulk Sync**: Sync single waypoints or all at once
- **Real-time Updates**: Refresh to see current state of both systems

## How It Works

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT                                │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │ JourneyMap      │←──→│ Waypoint Sync Mod               │ │
│  │ Waypoints       │    │ - Diff GUI                      │ │
│  │ (Local)         │    │ - Network Communication         │ │
│  └─────────────────┘    └───────────────┬─────────────────┘ │
└─────────────────────────────────────────┼───────────────────┘
                                          │
┌─────────────────────────────────────────┼───────────────────┐
│                        SERVER           │                    │
│  ┌─────────────────┐    ┌───────────────▼─────────────────┐ │
│  │ BlueMap         │←──→│ Waypoint Sync Mod               │ │
│  │ Markers         │    │ - Marker Management             │ │
│  │ (Server)        │    └─────────────────────────────────┘ │
│  └─────────────────┘                                        │
└─────────────────────────────────────────────────────────────┘
```

## Requirements

- Minecraft 1.21.4+
- Fabric Loader 0.16.0+
- Fabric API
- Fabric Language Kotlin
- JourneyMap (client-side, for waypoint access)
- BlueMap (server-side, for marker management)

## Installation

1. Install Fabric Loader and Fabric API
2. Install Fabric Language Kotlin
3. Install JourneyMap on the client
4. Install BlueMap on the server
5. Install this mod on both client and server

## Usage

1. Press `Shift+J` (default keybind) to open the Waypoint Sync GUI
2. The left panel shows JourneyMap waypoints
3. The right panel shows BlueMap POI markers
4. Click the `→` button next to a JourneyMap waypoint to copy it to BlueMap
5. Click the `←` button next to a BlueMap marker to copy it to JourneyMap
6. Use "Sync All" buttons to copy all waypoints in one direction

## Configuration

The keybind can be changed in Minecraft's Controls menu under the "Waypoint Sync" category.

## Building

```bash
./gradlew build
```

The built JAR will be in `fabric/build/libs/`.

## Development

This mod is built with:
- Kotlin
- Fabric API
- BlueMap API (`de.bluecolored:bluemap-api`)
- JourneyMap API (`info.journeymap:journeymap-api-fabric`)

### Project Structure

```
waypoint-sync/
├── core/                    # Shared data models and sync logic
│   └── src/main/kotlin/
│       └── com/example/waypointsync/
│           ├── data/        # SyncableWaypoint, WaypointDiff, etc.
│           └── sync/        # DiffCalculator
├── fabric/                  # Fabric mod implementation
│   └── src/main/kotlin/
│       └── com/example/waypointsync/
│           ├── bluemap/     # BlueMap API integration
│           ├── journeymap/  # JourneyMap API integration
│           ├── client/      # Client-side code (GUI, keybinds)
│           └── network/     # Client-server networking
└── build.gradle.kts         # Root build configuration
```

## License

MIT License

## Credits

- [BlueMap](https://bluemap.bluecolored.de/) by Blue (Lukas Rieger)
- [JourneyMap](https://journeymap.info/) by Techbrew
- [Fabric](https://fabricmc.net/) modding toolchain

