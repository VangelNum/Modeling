package org.vangel.modeling.from_postfix

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.vangel.modeling.core.Mods
import org.vangel.modeling.core.Symbol
import org.vangel.modeling.core.SymbolType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin

class FromPostfixViewModel: ViewModel() {
    private val _uiFromPostfixState = MutableStateFlow(UiStateFromPostfix())
    val uiFromPostfixState: StateFlow<UiStateFromPostfix> = _uiFromPostfixState.asStateFlow()

    fun onEvent(event: UiEventFromPostfix) {
        when(event) {
            is UiEventFromPostfix.ChangeMode -> {
                when(event.mode) {
                    Mods.AUTOMATIC_MODE -> {
                        if (_uiFromPostfixState.value.isTactWorkStarted) {
                            event.scope.launch {
                                event.snackbarHostState.showSnackbar(
                                    message = "Переключение на автоматический режим невозможно до окончания тактового режима",
                                )
                            }
                        } else {
                            _uiFromPostfixState.update { currentState ->
                                currentState.copy(
                                    automaticMode = true,
                                    stack = mutableListOf()
                                )
                            }
                        }
                    }

                    Mods.TACT_MODE -> {
                        _uiFromPostfixState.update { currentState ->
                            currentState.copy(
                                automaticMode = false,
                                stack = mutableListOf()
                            )
                        }
                    }
                }
            }

            is UiEventFromPostfix.ParsePostfixString -> {
                val parsedSymbols = event.postfixList.map { symbol ->
                    when (symbol.type) {
                        SymbolType.FUNCTION -> {
                            when (symbol.value) {
                                "sin" -> Symbol("в", SymbolType.FUNCTION)
                                "cos" -> Symbol("г", SymbolType.FUNCTION)
                                "abs" -> Symbol("т", SymbolType.FUNCTION)
                                "ln" -> Symbol("и", SymbolType.FUNCTION)
                                else -> Symbol("", SymbolType.EMPTY)
                            }
                        }
                        else -> symbol
                    }
                }.toMutableList()

                _uiFromPostfixState.update { currentState ->
                    currentState.copy(
                        inputTextState = parsedSymbols
                    )
                }

                _uiFromPostfixState.update { currentState ->
                    currentState.copy(
                        symbolValues = event.postfixList
                            .filter { it.type == SymbolType.VARIABLE }
                            .associate { it.value to null }
                            .toMutableMap()
                    )
                }
            }

            is UiEventFromPostfix.ChangeSymbolValues -> {
                _uiFromPostfixState.update { currentState ->
                    currentState.copy(
                        symbolValues = currentState.symbolValues.toMutableMap().apply {
                            put(event.key, event.newValue)
                        }
                    )
                }
            }

            is UiEventFromPostfix.CalculateInput -> {
                val updatedSymbols = event.input.map { symbol ->
                    when (symbol.type) {
                        SymbolType.VARIABLE -> {
                            val substitutedValue = _uiFromPostfixState.value.symbolValues[symbol.value]?.toString() ?: ""
                            Symbol(substitutedValue, symbol.type)
                        }
                        else -> symbol
                    }
                }

                _uiFromPostfixState.update { currentState ->
                    currentState.copy(
                        originalInInfixForm = updatedSymbols.toMutableList()
                    )
                }

                val updatedSymbolsInput = _uiFromPostfixState.value.inputTextState.map { symbol ->
                    when (symbol.type) {
                        SymbolType.VARIABLE -> {
                            val substitutedValue = _uiFromPostfixState.value.symbolValues[symbol.value]?.toString() ?: ""
                            Symbol(substitutedValue, symbol.type)
                        }
                        SymbolType.FUNCTION -> {
                            when (symbol.value) {
                                "в" -> Symbol("sin", SymbolType.FUNCTION)
                                "г" -> Symbol("cos", SymbolType.FUNCTION)
                                "т" -> Symbol("abs", SymbolType.FUNCTION)
                                "и" -> Symbol("ln", SymbolType.FUNCTION)
                                else -> Symbol("", SymbolType.EMPTY)
                            }
                        }
                        else -> symbol
                    }
                }

                _uiFromPostfixState.update { currentState ->
                    currentState.copy(
                        receivedString = updatedSymbolsInput.toMutableList()
                    )
                }
                handleAutomatic()
            }

            is UiEventFromPostfix.CalculateInputForTactMode -> {
                if (!_uiFromPostfixState.value.isTactWorkStarted) {
                    val isOkayInput = checkInputFromPostfix(_uiFromPostfixState.value.symbolValues, event.scope, event.snackbarHostState)
                    if (!isOkayInput) {
                        return
                    }

                    val updatedSymbols = event.inputTextState.map { symbol ->
                        when (symbol.type) {
                            SymbolType.VARIABLE -> {
                                val substitutedValue = _uiFromPostfixState.value.symbolValues[symbol.value]?.toString() ?: ""
                                Symbol(substitutedValue, symbol.type)
                            }
                            else -> symbol
                        }
                    }

                    _uiFromPostfixState.update { currentState ->
                        currentState.copy(
                            originalInInfixForm = updatedSymbols.toMutableList()
                        )
                    }

                    val updatedSymbolsInput = _uiFromPostfixState.value.inputTextState.map { symbol ->
                        when (symbol.type) {
                            SymbolType.VARIABLE -> {
                                val substitutedValue = _uiFromPostfixState.value.symbolValues[symbol.value]?.toString() ?: ""
                                Symbol(substitutedValue, symbol.type)
                            }
                            SymbolType.FUNCTION -> {
                                when (symbol.value) {
                                    "в" -> Symbol("sin", SymbolType.FUNCTION)
                                    "г" -> Symbol("cos", SymbolType.FUNCTION)
                                    "т" -> Symbol("abs", SymbolType.FUNCTION)
                                    "и" -> Symbol("ln", SymbolType.FUNCTION)
                                    else -> Symbol("", SymbolType.EMPTY)
                                }
                            }
                            else -> symbol
                        }
                    }

                    _uiFromPostfixState.update { currentState ->
                        currentState.copy(
                            receivedString = updatedSymbolsInput.toMutableList(),
                            finalResult = 0.0,
                            copyReceivedString = updatedSymbolsInput.toMutableList()
                        )
                    }
                }
                _uiFromPostfixState.update {
                    it.copy(
                        isTactWorkStarted = true
                    )
                }
                executeSingleStep()
            }
        }
    }

    private fun executeSingleStep() {
        val currentState = _uiFromPostfixState.value
        if (currentState.receivedString.isEmpty()) {
            _uiFromPostfixState.update {
                it.copy(
                    isTactWorkStarted = false,
                    stack = mutableListOf(),
                    finalResult = currentState.stack[0]
                )
            }
            return
        }

        if (currentState.receivedString.isNotEmpty()) {
            val symbol = currentState.receivedString.first()

            when (symbol.type) {
                SymbolType.VARIABLE -> {
                    val value = symbol.value.toDouble()
                    _uiFromPostfixState.value.stack.add(value)
                    _uiFromPostfixState.update {
                        it.copy(
                            currentStep = 1,
                            symbolType = (Symbol(value.toString(), SymbolType.VARIABLE))
                        )
                    }
                }
                SymbolType.FUNCTION -> {
                    val operand = currentState.stack.removeAt(currentState.stack.size - 1)
                    val result = when (symbol.value) {
                        "sin" -> sin(operand)
                        "cos" -> cos(operand)
                        "ln" -> ln(operand)
                        "abs" -> abs(operand)
                        else -> throw IllegalArgumentException("Unknown function: ${symbol.value}")
                    }
                    _uiFromPostfixState.update {
                        it.copy(
                            currentStep = 2,
                            symbolType = (Symbol(symbol.value, SymbolType.FUNCTION))
                        )
                    }
                    _uiFromPostfixState.value.stack.add(result)
                }
                SymbolType.OPERATION -> {
                    val operand2 = currentState.stack.removeAt(currentState.stack.size - 1)
                    val operand1 = currentState.stack.removeAt(currentState.stack.size - 1)
                    val result = when (symbol.value) {
                        "+" -> operand1 + operand2
                        "-" -> operand1 - operand2
                        "*" -> operand1 * operand2
                        "/" -> operand1 / operand2
                        "^" -> operand1.pow(operand2)
                        else -> throw IllegalArgumentException("Unknown operation: ${symbol.value}")
                    }
                    _uiFromPostfixState.update {
                        it.copy(
                            currentStep = 5,
                            symbolType = (Symbol(symbol.value, SymbolType.OPERATION))
                        )
                    }
                    _uiFromPostfixState.value.stack.add(result)
                }
                else -> {
                    _uiFromPostfixState.update {
                        it.copy(
                            symbolType = (Symbol("", SymbolType.EMPTY))
                        )
                    }
                }
            }

            _uiFromPostfixState.update {
                it.copy(
                    receivedString = it.receivedString.drop(1).toMutableList()
                )
            }
        }
    }

    private fun handleAutomatic() {
        _uiFromPostfixState.update { currentState->
            currentState.copy(
                stack = mutableListOf()
            )
        }
        for (symbol in _uiFromPostfixState.value.receivedString) {
            when (symbol.type) {
                SymbolType.VARIABLE -> {
                    val value = symbol.value.toDouble()
                    _uiFromPostfixState.value.stack.add(value)
                    _uiFromPostfixState.update {
                        it.copy(
                            currentStep = 1,
                            symbolType = (Symbol(value.toString(), SymbolType.VARIABLE))
                        )
                    }
                }
                SymbolType.FUNCTION -> {
                    val operand = _uiFromPostfixState.value.stack.removeAt(_uiFromPostfixState.value.stack.size - 1)
                    val result = when (symbol.value) {
                        "sin" -> sin(operand)
                        "cos" -> cos(operand)
                        "ln" -> if (operand > 0) ln(operand) else {
                            // Set a special value to denote an error
                            Double.NaN
                        }
                        "abs" -> abs(operand)
                        else -> throw IllegalArgumentException("Unknown function: ${symbol.value}")
                    }
                    _uiFromPostfixState.update {
                        it.copy(
                            currentStep = 2,
                            symbolType = (Symbol(symbol.value, SymbolType.FUNCTION))
                        )
                    }
                    _uiFromPostfixState.value.stack.add(result)
                }
                SymbolType.OPERATION -> {
                    val operand2 = if (_uiFromPostfixState.value.stack.isNotEmpty()) {
                        _uiFromPostfixState.value.stack.removeAt(_uiFromPostfixState.value.stack.size - 1)
                    } else {
                        // Что делать, если стек пустой? Вернуть некий дефолтный операнд или обработать исключение
                        // В данном случае я просто возвращаю ноль
                        0.0
                    }

                    val operand1 = if (_uiFromPostfixState.value.stack.isNotEmpty()) {
                        _uiFromPostfixState.value.stack.removeAt(_uiFromPostfixState.value.stack.size - 1)
                    } else {
                        // Аналогично, что делать, если стек пустой?
                        0.0
                    }

                    val result = when (symbol.value) {
                        "+" -> operand1 + operand2
                        "-" -> operand1 - operand2
                        "*" -> operand1 * operand2
                        "/" -> operand1 / operand2
                        "^" -> operand1.pow(operand2)
                        else -> throw IllegalArgumentException("Unknown operation: ${symbol.value}")
                    }
                    _uiFromPostfixState.update {
                        it.copy(
                            currentStep = 5,
                            symbolType = (Symbol(symbol.value, SymbolType.OPERATION))
                        )
                    }
                    _uiFromPostfixState.value.stack.add(result)
                }
                else -> {
                    _uiFromPostfixState.update {
                        it.copy(
                            symbolType = (Symbol("", SymbolType.EMPTY))
                        )
                    }
                }
            }
        }
        _uiFromPostfixState.update {
            it.copy(
                finalResult = _uiFromPostfixState.value.stack[0]
            )
        }
    }
}