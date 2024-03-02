package org.vangel.modeling.to_postfix

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import org.vangel.modeling.core.Mods
import org.vangel.modeling.core.Screens
import org.vangel.modeling.core.Symbol

sealed class UiEventToPostfix {
    data class OnSymbolClick(val symbol: Symbol, val scope: CoroutineScope, val snackbarHostState: SnackbarHostState) : UiEventToPostfix()
    data class FormatToPostfix(
        val tableApplyElements: List<List<String>>,
        val scope: CoroutineScope,
        val snackbarHostState: SnackbarHostState,
        val mode: Mods
    ): UiEventToPostfix()
    data class ChangeMode(val mode: Mods, val scope: CoroutineScope, val snackbarHostState: SnackbarHostState): UiEventToPostfix()
    data class SwitchScreen(val screens: Screens): UiEventToPostfix()
}