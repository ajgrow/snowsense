package in2000.prosjekt.in2000project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Month
import java.time.MonthDay
import java.time.format.TextStyle
import java.util.Calendar
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import in2000.prosjekt.in2000project.R
@Composable
fun DatePicker(
    startDate: MonthDay?,
    endDate: MonthDay?,
    selectedYear: Int?,
    onStartDateChange: (MonthDay?) -> Unit,
    onEndDateChange: (MonthDay?) -> Unit,
    onYearChange: (Int?) -> Unit,
) {
    var showingPicker by remember { mutableStateOf<String?>(null) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateField(
            label = stringResource(R.string.date_start),
            value = startDate,
            onClick = { showingPicker = "start" },
            modifier = Modifier.weight(1f)
        )
        DateField(
            label = stringResource(R.string.date_end),
            value = endDate,
            onClick = { showingPicker = "end" },
            modifier = Modifier.weight(1f)
        )
        YearDropdown(
            label = stringResource(R.string.since_year),
            currentYear = currentYear,
            selected = selectedYear,
            onSelected = { onYearChange(it) },
            modifier = Modifier.weight(1f)
        )
    }

    when (showingPicker) {
        "start" -> CalendarPickerDialog(
            initial = startDate,
            maxDate = endDate,
            onDismiss = { showingPicker = null },
            onConfirm = { onStartDateChange(it); showingPicker = null }
        )
        "end" -> CalendarPickerDialog(
            initial = endDate,
            minDate = startDate,
            onDismiss = { showingPicker = null },
            onConfirm = { onEndDateChange(it); showingPicker = null }
        )
    }
}

fun MonthDay.display(): String {
    val day = dayOfMonth.toString().padStart(2, '0')
    val month = monthValue.toString().padStart(2, '0')
    return "$day.$month"
}

@Composable
fun DateField(
    label: String,
    value: MonthDay?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (value != null) {
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value.display(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CalendarPickerDialog(
    initial: MonthDay?,
    onDismiss: () -> Unit,
    onConfirm: (MonthDay) -> Unit,
    minDate: MonthDay? = null,
    maxDate: MonthDay? = null
) {
    var displayedMonth by remember { mutableIntStateOf(initial?.monthValue ?: 1) }
    var selectedDay by remember { mutableStateOf(initial?.dayOfMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { selectedDay?.let { onConfirm(MonthDay.of(displayedMonth, it)) } },
                enabled = selectedDay != null
            ) { Text(stringResource(R.string.dialog_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
        text = {
            Column {
                MonthNavHeader(
                    month = displayedMonth,
                    onPrevious = {
                        selectedDay = null
                        displayedMonth = if (displayedMonth == 1) 12 else displayedMonth - 1
                    },
                    onNext = {
                        selectedDay = null
                        displayedMonth = if (displayedMonth == 12) 1 else displayedMonth + 1
                    }
                )
                Spacer(Modifier.height(4.dp))
                DayGrid(
                    month = displayedMonth,
                    selectedDay = selectedDay,
                    minDate = minDate,
                    maxDate = maxDate,
                    onDaySelected = { selectedDay = it }
                )
            }
        }
    )
}

@Composable
private fun MonthNavHeader(month: Int, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_month))
        }
        Text(
            text = Month.of(month).getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month))
        }
    }
}

@Composable
private fun DayGrid(
    month: Int,
    selectedDay: Int?,
    minDate: MonthDay?,
    maxDate: MonthDay?,
    onDaySelected: (Int) -> Unit
) {
    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
        items(daysInMonth(month)) { index ->
            val day = index + 1
            val md = MonthDay.of(month, day)
            val isValid = (minDate == null || md > minDate) && (maxDate == null || md < maxDate)
            val isSelected = day == selectedDay

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .then(if (isValid) Modifier.clickable { onDaySelected(day) } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        !isValid -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearDropdown(
    label: String,
    currentYear: Int,
    selected: Int?,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val years = remember(currentYear) { (1900..<currentYear).toList().reversed() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                )
                .heightIn(min = 56.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraSmall)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (selected != null) {
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selected.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = { onSelected(year); expanded = false }
                )
            }
        }
    }
}

fun daysInMonth(month: Int) = when (month) {
    2 -> 28
    4, 6, 9, 11 -> 30
    else -> 31
}