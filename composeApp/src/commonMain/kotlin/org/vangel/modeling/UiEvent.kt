package org.vangel.modeling

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope

sealed class UiEvent {
    data class OnSymbolClick(val symbol: Symbol, val scope: CoroutineScope, val snackbarHostState: SnackbarHostState) : UiEvent()
    data class FormatToPostfix(
        val tableApplyElements: List<List<String>>,
        val scope: CoroutineScope,
        val snackbarHostState: SnackbarHostState,
        val mode: Mods
    ): UiEvent()
    data class ChangeMode(val mode: Mods, val scope: CoroutineScope, val snackbarHostState: SnackbarHostState): UiEvent()
}