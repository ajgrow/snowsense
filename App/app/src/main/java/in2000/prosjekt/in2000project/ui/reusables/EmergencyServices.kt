package in2000.prosjekt.in2000project.ui.reusables

import androidx.annotation.StringRes
import in2000.prosjekt.in2000project.R

enum class EmergencyServices(
    @StringRes val service: Int,
    @StringRes val number: Int
) {
    POLICE(R.string.police_service, R.string.police_number),
    FIRE(R.string.fire_service, R.string.fire_number),
    AMBULANCE(R.string.ambulance_service, R.string.ambulance_number)
}