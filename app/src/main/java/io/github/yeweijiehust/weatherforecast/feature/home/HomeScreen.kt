package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onManageCitiesClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Project foundation is ready.",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Weather dashboard will be implemented in the next steps.",
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onManageCitiesClick) {
            Text(text = "Open City Management")
        }
        Button(onClick = onSettingsClick) {
            Text(text = "Open Settings Screen")
        }
    }
}
