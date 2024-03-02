package org.vangel.modeling.to_postfix

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.vangel.modeling.core.Mods
import org.vangel.modeling.core.Screens
import org.vangel.modeling.core.Symbol
import org.vangel.modeling.core.SymbolType

class ToPostfixViewModel : ViewModel() {
    private val _uiToPostfixState = MutableStateFlow(UiStateToPostfix())
    val uiToPostfixState: StateFlow<UiStateToPostfix> = _uiToPostfixState.asStateFlow()

    fun onEvent(event: UiEventToPostfix) {
        when (event) {
            is UiEventToPostfix.OnSymbolClick -> {
                _uiToPostfixState.update { currentState ->
                    when (event.symbol.type) {
                        SymbolType.CLEAR -> currentState.copy(
                            inputTextState = mutableListOf()
                        )

                        SymbolType.DELETE -> currentState.copy(
                            inputTextState = currentState.inputTextState.dropLast(1).toMutableList()
                        )

                        else -> {
                            if (_uiToPostfixState.value.isTactWorkStarted) {
                                event.scope.launch {
                                    event.snackbarHostState.showSnackbar(
                                        message = "Изменить выражение нельзя до окончания тактового режима",
                                    )
                                }
                                currentState
                            } else {
                                currentState.copy(
                                    inputTextState = currentState.inputTextState.toMutableList()
                                        .apply { add(event.symbol) }
                                )
                            }
                        }
                    }
                }
            }

            is UiEventToPostfix.FormatToPostfix -> {
                when (event.mode) {
                    Mods.AUTOMATIC_MODE -> {
                        handleAutomaticMode(event)
                    }

                    Mods.TACT_MODE -> {
                        handleTactMode(event)
                    }
                }
            }

            is UiEventToPostfix.ChangeMode -> {
                when (event.mode) {
                    Mods.AUTOMATIC_MODE -> {
                        if (_uiToPostfixState.value.isTactWorkStarted) {
                            event.scope.launch {
                                event.snackbarHostState.showSnackbar(
                                    message = "Переключение на автоматический режим невозможно до окончания тактового режима",
                                )
                            }
                        } else {
                            _uiToPostfixState.update { currentState ->
                                currentState.copy(
                                    automaticMode = true
                                )
                            }
                        }
                    }

                    Mods.TACT_MODE -> {
                        _uiToPostfixState.update { currentState ->
                            currentState.copy(
                                automaticMode = false
                            )
                        }
                    }
                }
            }

            is UiEventToPostfix.SwitchScreen -> {
                _uiToPostfixState.update { currentState->
                    when (event.screens) {
                        Screens.FROM_POSTFIX_SCREEN -> {
                            currentState.copy(
                                currentScreen = Screens.FROM_POSTFIX_SCREEN
                            )
                        }
                        Screens.TO_POSTFIX_SCREEN -> {
                            currentState.copy(
                                currentScreen = Screens.TO_POSTFIX_SCREEN
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleTactMode(event: UiEventToPostfix.FormatToPostfix) {

        if (!_uiToPostfixState.value.inputStringChecked) {
            _uiToPostfixState.update { currentState ->
                currentState.copy(
                    inputStringChecked = ExpressionModelToPostfix.checkInput(
                        _uiToPostfixState.value.inputTextState,
                        event.snackbarHostState,
                        event.scope
                    )
                )
            }
        }

        if (!_uiToPostfixState.value.inputStringChecked) {
            return
        }

        val firstRow = event.tableApplyElements.first()
        val firstColumn = Array(event.tableApplyElements.size) {
            event.tableApplyElements[it][0]
        }

        if (_uiToPostfixState.value.lastOperation) {
            _uiToPostfixState.update { currentState ->
                currentState.copy(
                    outputTextField = mutableListOf()
                )
            }
        }

        _uiToPostfixState.update { currentState ->
            currentState.copy(
                lastOperation = false,
                isTactWorkStarted = true
            )
        }

        if ((!_uiToPostfixState.value.automaticMode && (_uiToPostfixState.value.inputTextState.isNotEmpty() || _uiToPostfixState.value.stack.isNotEmpty()) || !_uiToPostfixState.value.lastOperation) && _uiToPostfixState.value.inputStringChecked) {
            val symbol = _uiToPostfixState.value.inputTextState.firstOrNull() ?: Symbol("$", SymbolType.EMPTY)
            val stackOperationIndex = _uiToPostfixState.value.stack.firstOrNull() ?: Symbol("$", SymbolType.EMPTY)

            val indexInRow = when (symbol.type) {
                SymbolType.VARIABLE -> firstRow.indexOf("P")
                SymbolType.FUNCTION -> firstRow.indexOf("F")
                else -> {
                    firstRow.indexOf(symbol.value)
                }
            }

            val indexInColumn = when (stackOperationIndex.type) {
                SymbolType.FUNCTION -> firstColumn.indexOf("F")
                else -> firstColumn.indexOf(stackOperationIndex.value)
            }

            _uiToPostfixState.update { currentState ->
                currentState.copy(
                    operationIndexes = Pair(indexInColumn, indexInRow)
                )
            }

            when (event.tableApplyElements[indexInColumn][indexInRow]) {
                "1" -> {
                    _uiToPostfixState.value.stack.add(0, symbol)
                    _uiToPostfixState.value.inputTextState.removeFirst()
                }

                "2" -> {
                    _uiToPostfixState.value.outputTextField.add(_uiToPostfixState.value.stack.removeFirst())
                }

                "3" -> {
                    _uiToPostfixState.value.inputTextState.removeFirst()
                    _uiToPostfixState.value.stack.removeFirst()
                }

                "4" -> {
                    _uiToPostfixState.update { currentState ->
                        currentState.copy(
                            lastOperation = true,
                            isTactWorkStarted = false,
                            inputStringChecked = false
                        )
                    }

                    event.scope.launch {
                        event.snackbarHostState.showSnackbar("Преобразование успешно завершено")
                    }
                }

                "5" -> {
                    _uiToPostfixState.update { currentState ->
                        currentState.copy(
                            lastOperation = true,
                            isTactWorkStarted = false,
                            inputStringChecked = false
                        )
                    }
                    event.scope.launch {
                        event.snackbarHostState.showSnackbar("Произошла ошибка")
                    }
                }

                "6" -> {
                    _uiToPostfixState.value.inputTextState.removeFirst()
                    _uiToPostfixState.value.outputTextField.add(symbol)
                }
            }
        }
    }

    private fun handleAutomaticMode(event: UiEventToPostfix.FormatToPostfix) {

        val inputTextCopy = _uiToPostfixState.value.inputTextState.toMutableList()

        val firstRow = event.tableApplyElements.first()
        val firstColumn = Array(event.tableApplyElements.size) {
            event.tableApplyElements[it][0]
        }
        _uiToPostfixState.update { currentState ->
            currentState.copy(
                lastOperation = false,
                outputTextField = mutableListOf()
            )
        }
        while (
            (_uiToPostfixState.value.automaticMode && (inputTextCopy.isNotEmpty() || _uiToPostfixState.value.stack.isNotEmpty()) || !_uiToPostfixState.value.lastOperation)
        ) {
            val symbol = inputTextCopy.firstOrNull() ?: Symbol("$", SymbolType.EMPTY)
            val stackOperationIndex =
                _uiToPostfixState.value.stack.firstOrNull() ?: Symbol("$", SymbolType.EMPTY)

            val indexInRow = when (symbol.type) {
                SymbolType.VARIABLE -> firstRow.indexOf("P")
                SymbolType.FUNCTION -> firstRow.indexOf("F")
                else -> {
                    firstRow.indexOf(symbol.value)
                }
            }

            val indexInColumn = when (stackOperationIndex.type) {
                SymbolType.FUNCTION -> firstColumn.indexOf("F")
                else -> firstColumn.indexOf(stackOperationIndex.value)
            }

            _uiToPostfixState.update { currentState ->
                currentState.copy(
                    operationIndexes = Pair(indexInColumn, indexInRow)
                )
            }

            when (event.tableApplyElements[indexInColumn][indexInRow]) {
                "1" -> {
                    _uiToPostfixState.value.stack.add(0, symbol)
                    inputTextCopy.removeFirst()
                }

                "2" -> {
                    _uiToPostfixState.value.outputTextField.add(_uiToPostfixState.value.stack.removeFirst())
                }

                "3" -> {
                    inputTextCopy.removeFirst()
                    _uiToPostfixState.value.stack.removeFirst()
                }

                "4" -> {
                    _uiToPostfixState.update { currentState ->
                        currentState.copy(
                            lastOperation = true
                        )
                    }

                    event.scope.launch {
                        event.snackbarHostState.showSnackbar("Преобразование успешно завершено")
                    }
                }

                "5" -> {
                    _uiToPostfixState.update { currentState ->
                        currentState.copy(
                            lastOperation = true
                        )
                    }
                    event.scope.launch {
                        event.snackbarHostState.showSnackbar("Произошла ошибка")
                    }
                    break
                }

                "6" -> {
                    inputTextCopy.removeFirst()
                    _uiToPostfixState.value.outputTextField.add(symbol)
                }
            }
        }
    }
}