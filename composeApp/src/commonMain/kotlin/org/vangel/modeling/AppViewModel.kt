package org.vangel.modeling

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onEvent(event: UiEvent) {
        when (event) {
            is UiEvent.OnSymbolClick -> {
                _uiState.update { currentState ->
                    when (event.symbol.type) {
                        SymbolType.CLEAR -> currentState.copy(
                            inputTextState = mutableListOf()
                        )

                        SymbolType.DELETE -> currentState.copy(
                            inputTextState = currentState.inputTextState.dropLast(1).toMutableList()
                        )

                        else -> {
                            if (_uiState.value.isTactWorkStarted) {
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

            is UiEvent.FormatToPostfix -> {
                when (event.mode) {
                    Mods.AUTOMATIC_MODE -> {
                        handleAutomaticMode(event)
                    }

                    Mods.TACT_MODE -> {
                        handleTactMode(event)
                    }
                }
            }

            is UiEvent.ChangeMode -> {
                when (event.mode) {
                    Mods.AUTOMATIC_MODE -> {
                        if (_uiState.value.isTactWorkStarted) {
                            event.scope.launch {
                                event.snackbarHostState.showSnackbar(
                                    message = "Переключение на автоматический режим невозможно до окончания тактового режима",
                                )
                            }
                        } else {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    automaticMode = true
                                )
                            }
                        }
                    }

                    Mods.TACT_MODE -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                automaticMode = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleTactMode(event: UiEvent.FormatToPostfix) {

        if (!_uiState.value.inputStringChecked) {
            _uiState.update { currentState ->
                currentState.copy(
                    inputStringChecked = ExpressionModel.checkInput(
                        _uiState.value.inputTextState,
                        event.snackbarHostState,
                        event.scope
                    )
                )
            }
        }

        if (!_uiState.value.inputStringChecked) {
            return
        }

        val firstRow = event.tableApplyElements.first()
        val firstColumn = Array(event.tableApplyElements.size) {
            event.tableApplyElements[it][0]
        }

        if (_uiState.value.lastOperation) {
            _uiState.update { currentState ->
                currentState.copy(
                    outputTextField = ""
                )
            }
        }

        _uiState.update { currentState ->
            currentState.copy(
                lastOperation = false,
                isTactWorkStarted = true
            )
        }

        if ((!_uiState.value.automaticMode && (_uiState.value.inputTextState.isNotEmpty() || _uiState.value.stack.isNotEmpty()) || !_uiState.value.lastOperation) && _uiState.value.inputStringChecked) {
            val symbol = _uiState.value.inputTextState.firstOrNull() ?: Symbol("$", SymbolType.EMPTY)
            val stackOperationIndex = _uiState.value.stack.firstOrNull() ?: Symbol("$", SymbolType.EMPTY)

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

            _uiState.update { currentState ->
                currentState.copy(
                    operationIndexes = Pair(indexInColumn, indexInRow)
                )
            }

            when (event.tableApplyElements[indexInColumn][indexInRow]) {
                "1" -> {
                    _uiState.value.stack.add(0, symbol)
                    _uiState.value.inputTextState.removeFirst()
                }

                "2" -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            outputTextField = currentState.outputTextField + _uiState.value.stack.removeFirst().value
                        )
                    }
                }

                "3" -> {
                    _uiState.value.inputTextState.removeFirst()
                    _uiState.value.stack.removeFirst()
                }

                "4" -> {
                    _uiState.update { currentState ->
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
                    _uiState.update { currentState ->
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
                    _uiState.value.inputTextState.removeFirst()
                    _uiState.update { currentState ->
                        currentState.copy(
                            outputTextField = currentState.outputTextField + symbol.value
                        )
                    }
                }
            }
        }
    }

    private fun handleAutomaticMode(event: UiEvent.FormatToPostfix) {

        val inputTextCopy = _uiState.value.inputTextState.toMutableList()

        val firstRow = event.tableApplyElements.first()
        val firstColumn = Array(event.tableApplyElements.size) {
            event.tableApplyElements[it][0]
        }
        _uiState.update { currentState ->
            currentState.copy(
                lastOperation = false,
                outputTextField = ""
            )
        }
        while (
            (_uiState.value.automaticMode && (inputTextCopy.isNotEmpty() || _uiState.value.stack.isNotEmpty()) || !_uiState.value.lastOperation)
        ) {
            val symbol = inputTextCopy.firstOrNull() ?: Symbol("$", SymbolType.EMPTY)
            val stackOperationIndex =
                _uiState.value.stack.firstOrNull() ?: Symbol("$", SymbolType.EMPTY)

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

            _uiState.update { currentState ->
                currentState.copy(
                    operationIndexes = Pair(indexInColumn, indexInRow)
                )
            }

            when (event.tableApplyElements[indexInColumn][indexInRow]) {
                "1" -> {
                    _uiState.value.stack.add(0, symbol)
                    inputTextCopy.removeFirst()
                }

                "2" -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            outputTextField = currentState.outputTextField + _uiState.value.stack.removeFirst().value
                        )
                    }
                }

                "3" -> {
                    inputTextCopy.removeFirst()
                    _uiState.value.stack.removeFirst()
                }

                "4" -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            lastOperation = true
                        )
                    }

                    event.scope.launch {
                        event.snackbarHostState.showSnackbar("Преобразование успешно завершено")
                    }
                }

                "5" -> {
                    _uiState.update { currentState ->
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
                    _uiState.update { currentState ->
                        currentState.copy(
                            outputTextField = currentState.outputTextField + symbol.value
                        )
                    }
                }
            }
        }
    }
}