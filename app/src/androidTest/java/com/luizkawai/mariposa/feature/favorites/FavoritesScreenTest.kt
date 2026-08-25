package com.luizkawai.mariposa.feature.favorites

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.luizkawai.mariposa.core.designsystem.theme.MariposaTheme
import org.junit.Rule
import org.junit.Test

class FavoritesScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun empty_state_is_displayed_when_there_are_no_favorites() {
        composeTestRule.setContent {
            MariposaTheme {
                FavoritesScreen(
                    uiState = FavoritesUiState(isLoading = false),
                    onAction = {},
                    onBack = {},
                    onCharacterClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Lista de favoritos vacía")
            .assertIsDisplayed()
    }
}
