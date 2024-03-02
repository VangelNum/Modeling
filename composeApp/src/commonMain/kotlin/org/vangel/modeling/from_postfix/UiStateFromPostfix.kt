package org.vangel.modeling.from_postfix

import org.vangel.modeling.core.Symbol
import org.vangel.modeling.core.SymbolType

data class UiStateFromPostfix(
    val inputTextState: MutableList<Symbol> = mutableListOf(),
    val automaticMode: Boolean = true,
    val stack: MutableList<Double> = mutableListOf(),
    val symbolValues: MutableMap<String, Int?> = mutableMapOf(),
    val originalInInfixForm: MutableList<Symbol> = mutableListOf(),
    val receivedString: MutableList<Symbol> = mutableListOf(),
    val finalResult: Double = 0.0,
    val isTactWorkStarted: Boolean = false,
    val currentStep: Int = 0,
    val symbolType: Symbol = (Symbol("", SymbolType.EMPTY)),
    val copyReceivedString: MutableList<Symbol> = mutableListOf()
)
