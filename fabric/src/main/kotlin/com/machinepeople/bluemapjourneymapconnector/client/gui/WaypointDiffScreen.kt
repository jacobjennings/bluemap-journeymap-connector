package com.machinepeople.bluemapjourneymapconnector.client.gui

import com.machinepeople.bluemapjourneymapconnector.BlueMapJourneyMapConnectorMod
import com.machinepeople.bluemapjourneymapconnector.data.*
import com.machinepeople.bluemapjourneymapconnector.network.WaypointDataCache
import com.machinepeople.bluemapjourneymapconnector.network.BlueMapJourneyMapConnectorNetworking
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

import net.minecraft.client.input.MouseButtonEvent

/**
 * Main GUI screen for comparing and synchronizing waypoints
 * between JourneyMap (client) and BlueMap (server).
 *
 * Layout:
 * ┌────────────────────────────────────────────────────────────┐
 * │                    BlueMap JourneyMap Connector                           │
 * ├─────────────────────┬──────┬───────────────────────────────┤
 * │  JourneyMap (Local) │      │    BlueMap (Server)           │
 * ├─────────────────────┤      ├───────────────────────────────┤
 * │ ▪ Waypoint 1    [→] │      │ ▪ Waypoint A    [←]           │
 * │ ▪ Waypoint 2    [→] │      │ ▪ Waypoint B    [←]           │
 * │ ▪ Waypoint 3    [→] │      │                               │
 * ├─────────────────────┴──────┴───────────────────────────────┤
 * │  [Sync All →]   [← Sync All]   [Refresh]   [Close]         │
 * └────────────────────────────────────────────────────────────┘
 */
@Environment(EnvType.CLIENT)
class WaypointDiffScreen : Screen(Component.translatable("gui.bluemap-journeymap-connector.title")) {

    companion object {
        // Layout constants
        private const val PANEL_WIDTH = 300
        private const val PANEL_HEIGHT = 320
        private const val PANEL_MARGIN = 10
        private const val ENTRY_HEIGHT = 28
        private const val BUTTON_WIDTH = 24
        private const val BUTTON_HEIGHT = 20

        // Colors
        private const val PANEL_BG_COLOR = 0xDD000000.toInt()
        private const val HEADER_COLOR = 0xFF4488FF.toInt()
        private const val ENTRY_BG_COLOR = 0x88222222.toInt()
        private const val ENTRY_BG_HOVER_COLOR = 0x88444444.toInt()
        private const val TEXT_COLOR = 0xFFFFFFFF.toInt()
        private const val SECONDARY_TEXT_COLOR = 0xFFAAAAAA.toInt()
        private const val SYNCED_COLOR = 0xFF44FF44.toInt()
        private const val CONFLICT_COLOR = 0xFFFF8844.toInt()
    }

    private var diff: WaypointDiff = WaypointDiff.empty()
    private var scrollOffsetJM = 0
    private var scrollOffsetBM = 0
    private var isLoading = true

    // GUI dimensions calculated on init
    private var leftPanelX = 0
    private var rightPanelX = 0
    private var panelY = 0

    override fun init() {
        super.init()

        // Calculate panel positions (centered)
        val totalWidth = PANEL_WIDTH * 2 + PANEL_MARGIN * 3
        leftPanelX = (width - totalWidth) / 2 + PANEL_MARGIN
        rightPanelX = leftPanelX + PANEL_WIDTH + PANEL_MARGIN
        panelY = 50

        // Add bottom buttons
        val buttonY = height - 35
        val buttonWidth = 80
        val buttonSpacing = 10
        val totalButtonWidth = buttonWidth * 4 + buttonSpacing * 3
        var buttonX = (width - totalButtonWidth) / 2

        // Sync All JM → BM
        addRenderableWidget(Button.builder(Component.literal("Sync All →")) { syncAllToBlueMap() }
            .bounds(buttonX, buttonY, buttonWidth, 20)
            .build())
        buttonX += buttonWidth + buttonSpacing

        // Sync All BM → JM
        addRenderableWidget(Button.builder(Component.literal("← Sync All")) { syncAllToJourneyMap() }
            .bounds(buttonX, buttonY, buttonWidth, 20)
            .build())
        buttonX += buttonWidth + buttonSpacing

        // Refresh
        addRenderableWidget(Button.builder(Component.literal("Refresh")) { refreshData() }
            .bounds(buttonX, buttonY, buttonWidth, 20)
            .build())
        buttonX += buttonWidth + buttonSpacing

        // Close
        addRenderableWidget(Button.builder(Component.literal("Close")) { onClose() }
            .bounds(buttonX, buttonY, buttonWidth, 20)
            .build())

        // Initial data load
        refreshData()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // Render background and widgets (buttons)
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        // Render title
        guiGraphics.drawCenteredString(
            font,
            title,
            width / 2,
            20,
            TEXT_COLOR
        )

        if (isLoading) {
            guiGraphics.drawCenteredString(
                font,
                "Loading waypoints...",
                width / 2,
                height / 2,
                TEXT_COLOR
            )
        } else {
            // Render left panel (JourneyMap)
            renderPanel(
                guiGraphics,
                leftPanelX,
                panelY,
                "JourneyMap (Local)",
                diff.journeyMapOnly + diff.synced.filter { it.source == WaypointSource.JOURNEYMAP } + diff.conflicts.map { it.journeyMapVersion },
                mouseX,
                mouseY,
                true  // Show "→" buttons
            )

            // Render right panel (BlueMap)
            renderPanel(
                guiGraphics,
                rightPanelX,
                panelY,
                "BlueMap (Server)",
                diff.blueMapOnly + diff.synced.filter { it.source == WaypointSource.BLUEMAP } + diff.conflicts.map { it.blueMapVersion },
                mouseX,
                mouseY,
                false  // Show "←" buttons
            )

            // Render stats
            val statsY = panelY + PANEL_HEIGHT + 10
            val stats = buildString {
                append("JM Only: ${diff.journeyMapOnly.size}")
                append(" | BM Only: ${diff.blueMapOnly.size}")
                append(" | Synced: ${diff.synced.size}")
                if (diff.conflicts.isNotEmpty()) {
                    append(" | Conflicts: ${diff.conflicts.size}")
                }
            }
            guiGraphics.drawCenteredString(font, stats, width / 2, statsY, TEXT_COLOR)
        }
    }

    private fun renderPanel(
        guiGraphics: GuiGraphics,
        x: Int,
        y: Int,
        title: String,
        waypoints: List<SyncableWaypoint>,
        mouseX: Int,
        mouseY: Int,
        isLeftPanel: Boolean
    ) {
        // Panel background
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, PANEL_BG_COLOR)

        // Panel header
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + 20, HEADER_COLOR)
        guiGraphics.drawCenteredString(font, title, x + PANEL_WIDTH / 2, y + 6, TEXT_COLOR)

        // Panel content
        val contentY = y + 22
        val contentHeight = PANEL_HEIGHT - 24
        val visibleEntries = contentHeight / ENTRY_HEIGHT

        val scrollOffset = if (isLeftPanel) scrollOffsetJM else scrollOffsetBM
        val displayWaypoints = waypoints.drop(scrollOffset).take(visibleEntries)

        displayWaypoints.forEachIndexed { index, waypoint ->
            val entryY = contentY + index * ENTRY_HEIGHT
            val isHovered = mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                    mouseY >= entryY && mouseY <= entryY + ENTRY_HEIGHT

            // Entry background
            val bgColor = if (isHovered) ENTRY_BG_HOVER_COLOR else ENTRY_BG_COLOR
            guiGraphics.fill(x + 2, entryY, x + PANEL_WIDTH - 2, entryY + ENTRY_HEIGHT - 2, bgColor)

            // Color indicator (moved to left)
            guiGraphics.fill(x + 4, entryY + 4, x + 8, entryY + ENTRY_HEIGHT - 6, waypoint.color or 0xFF000000.toInt())

            // Waypoint name (start after color)
            val displayName = if (waypoint.name.length > 30) waypoint.name.take(27) + "..." else waypoint.name
            guiGraphics.drawString(font, displayName, x + 12, entryY + 4, TEXT_COLOR, false)

            // Coordinates (smaller text)
            val coords = "(${waypoint.x}, ${waypoint.y}, ${waypoint.z})"
            guiGraphics.drawString(font, coords, x + 12, entryY + 15, SECONDARY_TEXT_COLOR, false)

            // Sync button (far right)
            val buttonX = x + PANEL_WIDTH - BUTTON_WIDTH - 6
            val buttonLabel = if (isLeftPanel) "→" else "←"
            val buttonHovered = mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH &&
                    mouseY >= entryY + 4 && mouseY <= entryY + BUTTON_HEIGHT + 4

            val buttonColor = if (buttonHovered) 0xFF66AAFF.toInt() else 0xFF4488FF.toInt()
            guiGraphics.fill(buttonX, entryY + 4, buttonX + BUTTON_WIDTH, entryY + BUTTON_HEIGHT + 4, buttonColor)
            guiGraphics.drawCenteredString(font, buttonLabel, buttonX + BUTTON_WIDTH / 2, entryY + 7, TEXT_COLOR)
        }

        // Scroll indicators
        if (scrollOffset > 0) {
            guiGraphics.drawCenteredString(font, "▲", x + PANEL_WIDTH / 2, y + 22, TEXT_COLOR)
        }
        if (waypoints.size > scrollOffset + visibleEntries) {
            guiGraphics.drawCenteredString(font, "▼", x + PANEL_WIDTH / 2, y + PANEL_HEIGHT - 12, TEXT_COLOR)
        }
    }

    override fun mouseClicked(event: net.minecraft.client.input.MouseButtonEvent, handled: Boolean): Boolean {
        if (event.button() == 0 && !isLoading) {
            // Check for clicks on connector sync buttons
            if (handleWaypointClick(event.x().toInt(), event.y().toInt())) {
                return true
            }
        }
        return super.mouseClicked(event, handled)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val scrollAmount = -scrollY.toInt()

        // Check which panel the mouse is over
        if (mouseX >= leftPanelX && mouseX <= leftPanelX + PANEL_WIDTH) {
            scrollOffsetJM = (scrollOffsetJM + scrollAmount).coerceIn(0, maxOf(0, getJourneyMapWaypointCount() - 8))
            return true
        } else if (mouseX >= rightPanelX && mouseX <= rightPanelX + PANEL_WIDTH) {
            scrollOffsetBM = (scrollOffsetBM + scrollAmount).coerceIn(0, maxOf(0, getBlueMapWaypointCount() - 8))
            return true
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    private fun handleWaypointClick(mouseX: Int, mouseY: Int): Boolean {
        val contentY = panelY + 22
        val visibleEntries = (PANEL_HEIGHT - 24) / ENTRY_HEIGHT

        // Check left panel (JourneyMap)
        if (mouseX >= leftPanelX && mouseX <= leftPanelX + PANEL_WIDTH) {
            val waypoints = diff.journeyMapOnly + diff.synced.filter { it.source == WaypointSource.JOURNEYMAP } + diff.conflicts.map { it.journeyMapVersion }
            val entryIndex = (mouseY - contentY) / ENTRY_HEIGHT + scrollOffsetJM

            if (entryIndex >= 0 && entryIndex < waypoints.size) {
                val buttonX = leftPanelX + PANEL_WIDTH - BUTTON_WIDTH - 6
                val entryY = contentY + (entryIndex - scrollOffsetJM) * ENTRY_HEIGHT

                if (mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH &&
                    mouseY >= entryY + 4 && mouseY <= entryY + BUTTON_HEIGHT + 4) {
                    val waypoint = waypoints[entryIndex]
                    BlueMapJourneyMapConnectorMod.LOGGER.info("Clicked JM sync button for ${waypoint.name}")
                    syncWaypointToBlueMap(waypoint)
                    return true
                }
            }
        }

        // Check right panel (BlueMap)
        if (mouseX >= rightPanelX && mouseX <= rightPanelX + PANEL_WIDTH) {
            val waypoints = diff.blueMapOnly + diff.synced.filter { it.source == WaypointSource.BLUEMAP } + diff.conflicts.map { it.blueMapVersion }
            val entryIndex = (mouseY - contentY) / ENTRY_HEIGHT + scrollOffsetBM

            if (entryIndex >= 0 && entryIndex < waypoints.size) {
                val buttonX = rightPanelX + PANEL_WIDTH - BUTTON_WIDTH - 6
                val entryY = contentY + (entryIndex - scrollOffsetBM) * ENTRY_HEIGHT

                if (mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH &&
                    mouseY >= entryY + 4 && mouseY <= entryY + BUTTON_HEIGHT + 4) {
                    val waypoint = waypoints[entryIndex]
                    BlueMapJourneyMapConnectorMod.LOGGER.info("Clicked BM sync button for ${waypoint.name}")
                    syncWaypointToJourneyMap(waypoint)
                    return true
                }
            }
        }
        return false
    }

    private fun syncWaypointToBlueMap(waypoint: SyncableWaypoint) {
        BlueMapJourneyMapConnectorMod.LOGGER.info("Syncing '${waypoint.name}' to BlueMap")
        val operation = SyncOperation(
            action = SyncAction.CREATE,
            waypoint = waypoint.copy(source = WaypointSource.SYNCED),
            targetSource = WaypointSource.BLUEMAP
        )
        BlueMapJourneyMapConnectorNetworking.syncToBlueMap(listOf(operation))
        refreshData()
    }

    private fun syncWaypointToJourneyMap(waypoint: SyncableWaypoint) {
        BlueMapJourneyMapConnectorMod.LOGGER.info("Attempting to sync '${waypoint.name}' to JourneyMap")
        val instance = com.machinepeople.bluemapjourneymapconnector.journeymap.JourneyMapIntegration.getInstance()
        if (instance == null) {
            BlueMapJourneyMapConnectorMod.LOGGER.error("JourneyMap integration instance is null!")
            return
        }
        val success = instance.addWaypoint(waypoint)
        BlueMapJourneyMapConnectorMod.LOGGER.info("JourneyMap sync success: $success")
        refreshData()
    }

    private fun syncAllToBlueMap() {
        BlueMapJourneyMapConnectorMod.LOGGER.info("Syncing all JourneyMap waypoints to BlueMap")
        val operations = diff.journeyMapOnly.map { waypoint ->
            SyncOperation(
                action = SyncAction.CREATE,
                waypoint = waypoint.copy(source = WaypointSource.SYNCED),
                targetSource = WaypointSource.BLUEMAP
            )
        }
        if (operations.isNotEmpty()) {
            BlueMapJourneyMapConnectorNetworking.syncToBlueMap(operations)
            refreshData()
        }
    }

    private fun syncAllToJourneyMap() {
        BlueMapJourneyMapConnectorMod.LOGGER.info("Syncing all BlueMap waypoints to JourneyMap")
        val jmIntegration = com.machinepeople.bluemapjourneymapconnector.journeymap.JourneyMapIntegration.getInstance()
        if (jmIntegration != null) {
            diff.blueMapOnly.forEach { waypoint ->
                jmIntegration.addWaypoint(waypoint)
            }
            refreshData()
        }
    }

    private fun refreshData() {
        isLoading = true
        scrollOffsetJM = 0
        scrollOffsetBM = 0

        // Request fresh BlueMap data from server
        BlueMapJourneyMapConnectorNetworking.requestBlueMapWaypoints()

        // Update diff after a short delay (for network response)
        // In a real implementation, this would be callback-based
        Thread {
            Thread.sleep(1000)  // Wait for network response
            diff = WaypointDataCache.calculateDiff()
            isLoading = false
        }.start()
    }

    private fun getJourneyMapWaypointCount(): Int = diff.journeyMapOnly.size
    private fun getBlueMapWaypointCount(): Int = diff.blueMapOnly.size

    override fun isPauseScreen(): Boolean = false
}

