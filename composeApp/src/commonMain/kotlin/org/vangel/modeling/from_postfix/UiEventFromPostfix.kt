package org.vangel.modeling.from_postfix

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import org.vangel.modeling.core.Mods
import org.vangel.modeling.core.Symbol

sealed class UiEventFromPostfix {
    data class ChangeMode(val mode: Mods, val scope: CoroutineScope, val snackbarHostState: SnackbarHostState): UiEventFromPostfix()
    data class ParsePostfixString(val postfixList: MutableList<Symbol>): UiEventFromPostfix()
    data class ChangeSymbolValues(val key: String, val newValue: Int?) : UiEventFromPostfix()
    data class CalculateInput(val input: List<Symbol>): UiEventFromPostfix()
    data class CalculateInputForTactMode(
        val input: List<Symbol>,
        val scope: CoroutineScope,
        val snackbarHostState: SnackbarHostState,
        val inputTextState: MutableList<Symbol>
    ): UiEventFromPostfix()
}