package org.vangel.modeling

data class UiState(
    val operationIndexes: Pair<Int, Int> = Pair(0, 0),
    val inputTextState: MutableList<Symbol> = mutableListOf(),
    val automaticMode: Boolean = true,
    val lastOperation: Boolean = false,
    val isTactWorkStarted: Boolean = false,
    val stack: MutableList<Symbol> = mutableListOf(),
    val outputTextField: String = "",
    val inputStringChecked: Boolean = false
)