package in2000.prosjekt.in2000project.ui.reusables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.NamedLocation
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSection(
    textFieldState : TextFieldState,
    searchResults: List<NamedLocation>,
    recentSearches: List<NamedLocation>,
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit,
    onSelectResult: (location: NamedLocation) -> Unit,
    onNavigateToMap: (coordinate: Coordinate) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var expanded by rememberSaveable { mutableStateOf(false) }

    val query = textFieldState.text.toString()
    val isShowingRecents = query.isBlank() && recentSearches.isNotEmpty()
    val displayResults = if (isShowingRecents) recentSearches else searchResults

    //We should add some rules to the searchSection, like restrictions on type of input and input length
    DockedSearchBar(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .onFocusChanged { focusState ->
                if (!focusState.hasFocus) {
                    expanded = false
                }
            },
        inputField = {
            SearchBarDefaults.InputField(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                query = textFieldState.text.toString(),
                onQueryChange = {
                    textFieldState.edit { replace(0, length, it) }
                    expanded = true
                },
                onSearch = {
                    onSearch(textFieldState.text.toString())
                    expanded = true
                    focusManager.clearFocus()
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = {
                    Text(stringResource(R.string.search_placeholder))
                }
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            displayResults.forEach { result ->
                ListItem(
                    headlineContent = { Text(result.name) },
                    modifier = Modifier
                        .clickable {
                            onNavigateToMap(result.coordinate)
                            onSelectResult(result)
                            textFieldState.edit { replace(0, length, "") }
                            expanded = false
                            focusManager.clearFocus()
                        }
                        .fillMaxWidth()
                )
            }
        }
    }
}