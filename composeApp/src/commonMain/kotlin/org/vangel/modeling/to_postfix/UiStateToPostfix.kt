package org.vangel.modeling.to_postfix

import org.vangel.modeling.core.Screens
import org.vangel.modeling.core.Symbol
import org.vangel.modeling.core.SymbolType

data class UiStateToPostfix(
    val operationIndexes: Pair<Int, Int> = Pair(0, 0),
    val inputTextState: MutableList<Symbol> = mutableListOf(),
//    val inputTextState: MutableList<Symbol> = mutableListOf(
//        Symbol("A", SymbolType.VARIABLE),
//        Symbol("+", SymbolType.OPERATION),
//        Symbol("B", SymbolType.VARIABLE),
//        Symbol("*", SymbolType.OPERATION),
//        Symbol("C", SymbolType.VARIABLE),
//        Symbol("+", SymbolType.OPERATION),
//        Symbol("sin", SymbolType.FUNCTION),
//        Symbol("(", SymbolType.OPEN_PARENTHESES),
//        Symbol("E", SymbolType.VARIABLE),
//        Symbol(")", SymbolType.CLOSE_PARENTHESES),
//    ),
    val automaticMode: Boolean = true,
    val lastOperation: Boolean = false,
    val isTactWorkStarted: Boolean = false,
    val stack: MutableList<Symbol> = mutableListOf(),
    val outputTextField: MutableList<Symbol> = mutableListOf(),
    val inputStringChecked: Boolean = false,
    val currentScreen: Screens = Screens.TO_POSTFIX_SCREEN
)