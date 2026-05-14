package in2000.prosjekt.in2000project.ui.screen.map

import in2000.prosjekt.in2000project.domain.model.LocationClimateData
import java.time.MonthDay
import in2000.prosjekt.in2000project.domain.model.Coordinate

/**
 * UI state for the map screen.
 *
 * @property overlayState The current state of the overlay.
 * @property climateState The current state of the climate data.
 * @property climateCoordinate The coordinate of the climate data.
 * @property startDate The start date of the climate data.
 * @property endDate The end date of the climate data.
 * @property currentYear The current year.
 * @property selectedYear The selected year.
 * @property isSheetVisible Whether the sheet is visible.
 */
data class MapScreenState(
    val overlayState: OverlayState = OverlayState.Loading,
    val climateState: ClimateState = ClimateState.Idle,
    val climateCoordinate: Coordinate? = null,
    val startDate: MonthDay,
    val endDate: MonthDay,
    val currentYear: Int,
    val selectedYear: Int,
    val isSheetVisible: Boolean = false
)

/**
 * Represents the state of the overlay.
 *
 * @property Loading The overlay is currently loading.
 * @property Success The overlay has successfully loaded.
 * @property Error The overlay has encountered an error.
 */
sealed interface OverlayState {
    object Loading : OverlayState
    object Success : OverlayState
    object Error : OverlayState
}

/**
 * Represents the state of the climate data.
 *
 * @property Idle The climate data is not currently available.
 * @property Loading The climate data is currently loading.
 * @property Success The climate data has successfully loaded.
 * @property Error The climate data has encountered an error.
 *
 */
sealed interface ClimateState {
    object Idle : ClimateState
    object Loading : ClimateState
    data class Success(val data: LocationClimateData) : ClimateState
    object Error : ClimateState
}