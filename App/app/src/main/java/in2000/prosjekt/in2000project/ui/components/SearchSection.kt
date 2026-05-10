package in2000.prosjekt.in2000project.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.NamedLocation
import kotlin.collections.forEach
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.platform.LocalWindowInfo
import in2000.prosjekt.in2000project.ui.viewmodels.SearchUiState
import in2000.prosjekt.in2000project.ui.viewmodels.SearchViewModel
import kotlin.math.exp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSection(
    modifier: Modifier = Modifier,
    searchState: SearchUiState,
    onQueryChange: (query: String) -> Unit,
    onSelectResult: (location: NamedLocation) -> Unit,
    onNavigateToMap: (coordinate: Coordinate) -> Unit,
    onActiveChange: (isActive: Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var expanded by rememberSaveable { mutableStateOf(true) }

    val isShowingRecents = searchState.query.isBlank()
    val displayResults = if (isShowingRecents) searchState.recentSearches else searchState.searchResults
    val isNorwegian = LocalConfiguration.current.locales[0].language in listOf("no", "nb", "nn")

    DockedSearchBar(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            //set the height of the container when expanded
            .heightIn(min = if (expanded) LocalWindowInfo.current.containerDpSize.height * 0.4f else 0.dp)
            .onFocusChanged { focusState ->
                if (!focusState.hasFocus) {
                    expanded = false
                    onActiveChange(false)
                } else {
                    onActiveChange(true)
                }
            },
        onExpandedChange = {
            expanded = it
        },
        inputField = {
            SearchBarDefaults.InputField(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.open_menu)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.open_search)
                    )
                },
                query = searchState.query,
                onQueryChange = {
                    onQueryChange(it)
                },
                onSearch = {
                    searchState.searchResults.firstOrNull()?.let { location ->
                        onSelectResult(location)
                        onNavigateToMap(location.coordinate)
                    }
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = {
                    Text(stringResource(R.string.search_placeholder))
                }
            )
        },
        expanded = expanded,
        content = {
            Box(
                //make sure the container's height is consistent when recentsearches is empty vs filled
                modifier = Modifier
                    .height(LocalWindowInfo.current.containerDpSize.height * 0.4f - SearchBarDefaults.InputFieldHeight)
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    displayResults.forEach { result ->
                        ListItem(
                            headlineContent = { Text(result.name) },
                            trailingContent = {
                                Text(
                                    if (isNorwegian) {
                                        result.locationTypeNo
                                    } else {
                                        result.locationTypeEn
                                    }
                                )
                            },
                            modifier = Modifier
                                .clickable {
                                    onNavigateToMap(result.coordinate)
                                    onSelectResult(result)
                                    expanded = false
                                    focusManager.clearFocus()
                                }
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}
