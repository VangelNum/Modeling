package org.vangel.modeling

data class UiState(
    val operationIndexes: Pair<Int, Int> = Pair(0, 0),
    val shouldBeContinue: Boolean = false,
    val textState: List<Symbol> = emptyList(),
    val automaticMode: Boolean = true,
    val lastOperation: Boolean = false,
    val textStateCopy: List<Symbol> = emptyList(),
    val textStateCopyForTactInput: List<Symbol> = emptyList(),
    val isTactWorkStarted: Boolean = false,
    val textOutputIsClear: Boolean = false,
    val isInputChecked: Boolean = false,
    val canBeShownInfixForm: Boolean = false,
    val stack: List<Symbol> = emptyList(),
    val outputTextField: String = "",
)