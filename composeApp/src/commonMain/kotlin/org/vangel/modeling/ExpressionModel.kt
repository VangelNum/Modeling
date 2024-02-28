package org.vangel.modeling

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


object ExpressionModel {
    fun checkInput(
        value: SnapshotStateList<Symbol>,
        snackbarHostState: SnackbarHostState,
        scope: CoroutineScope
    ): Boolean {
        // Переменные для отслеживания состояния проверок
        var isValid = true
        var errorMessage = ""

        // Проверка на наличие недопустимых комбинаций
        for (i in 0 until value.size - 1) {
            val currentSymbol = value[i]
            val nextSymbol = value[i + 1]

            // Проверка на наличие двух переменных подряд
            if (currentSymbol.type == SymbolType.VARIABLE && nextSymbol.type == SymbolType.VARIABLE) {
                isValid = false
                errorMessage = "Две переменные не могут идти подряд"
                break
            }

            // Проверка на наличие двух операций подряд
            if (currentSymbol.type == SymbolType.OPERATION && nextSymbol.type == SymbolType.OPERATION) {
                isValid = false
                errorMessage = "Две операции не могут идти подряд"
                break
            }

            // Проверка наличия скобки после функции
            if (currentSymbol.type == SymbolType.FUNCTION && nextSymbol.type != SymbolType.OPEN_PARENTHESES) {
                isValid = false
                errorMessage =
                    "После функции '${currentSymbol.value}' должна следовать открывающая скобка"
                break
            }

            // Проверка наличия скобки перед закрывающей скобкой
            if (currentSymbol.type == SymbolType.OPEN_PARENTHESES && nextSymbol.type == SymbolType.CLOSE_PARENTHESES) {
                isValid = false
                errorMessage = "Формат скобок () невозможен"
                break
            }

            if (currentSymbol.type == SymbolType.CLOSE_PARENTHESES && nextSymbol.type == SymbolType.OPEN_PARENTHESES) {
                isValid = false
                errorMessage = "Формат скобок )( невозможен"
                break
            }

            if (currentSymbol.type == SymbolType.VARIABLE && nextSymbol.type == SymbolType.OPEN_PARENTHESES) {
                isValid = false
                errorMessage = "Перед скобкой должна быть либо операция либо функция"
                break
            }
            if (currentSymbol.type == SymbolType.VARIABLE && nextSymbol.type == SymbolType.FUNCTION) {
                isValid = false
                errorMessage = "После переменной не может быть функции"
                break
            }
            if (currentSymbol.type == SymbolType.OPEN_PARENTHESES && (nextSymbol.value == "*" || nextSymbol.value == "/"|| nextSymbol.value == "^")) {
                isValid = false
                errorMessage = "После открытой скобки не может быть *, /, ^"
                break
            }
            if (currentSymbol.type == SymbolType.OPERATION && nextSymbol.type == SymbolType.CLOSE_PARENTHESES) {
                isValid = false
                errorMessage = "Перед закрытой скобкой не может быть операции"
                break
            }
        }

        // Проверка на соответствие количества открывающих и закрывающих скобок
        val openingBrackets = value.count { it.type == SymbolType.OPEN_PARENTHESES }
        val closingBrackets = value.count { it.type == SymbolType.CLOSE_PARENTHESES }

        if (openingBrackets != closingBrackets) {
            isValid = false
            errorMessage = "Количество открывающих и закрывающих скобок должно быть одинаковым"
        }

        // Вывод сообщения об ошибке, если таковая имеется
        if (!isValid) {
            scope.launch {
                snackbarHostState.showSnackbar(errorMessage)
            }
        }

        return isValid
    }


    fun formatToPostfix(
        symbolsInput: SnapshotStateList<Symbol>,
        onOutputChange: (String) -> Unit,
        stack: SnapshotStateList<Symbol>,
        tableApplyElements: Array<Array<String>>,
        operationIndexes: MutableState<Pair<Int, Int>>,
        shouldBeContinue: MutableState<Boolean>,
        automaticMode: Boolean,
        lastOperation: MutableState<Boolean>,
        isTactWorkStarted: MutableState<Boolean>,
        textOutputIsClear: MutableState<Boolean>,
        snackbarHostState: SnackbarHostState,
        scope: CoroutineScope,
        isInputChecked: MutableState<Boolean>,
        canBeShownInfixForm: MutableState<Boolean>
    ) {
        val firstRow = tableApplyElements.first()
        val firstColumn = Array(tableApplyElements.size) {
            tableApplyElements[it][0]
        }

        while (
            (automaticMode && (symbolsInput.isNotEmpty() || stack.isNotEmpty() || !lastOperation.value)) ||
            (!automaticMode && shouldBeContinue.value)
        ) {
            val symbol = symbolsInput.firstOrNull() ?: Symbol("$", SymbolType.EMPTY)
            val stackOperationIndex = stack.firstOrNull() ?: Symbol("$", SymbolType.EMPTY)
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
            operationIndexes.value = Pair(indexInColumn, indexInRow)
            when (tableApplyElements[indexInColumn][indexInRow]) {
                "1" -> {
                    stack.add(0, symbol)
                    symbolsInput.removeFirst()
                }

                "2" -> {
                    onOutputChange(stack.removeFirst().value)
                }

                "3" -> {
                    symbolsInput.removeFirst()
                    stack.removeFirst()
                }

                "4" -> {
                    lastOperation.value = true
                    isTactWorkStarted.value = false
                    textOutputIsClear.value = false
                    isInputChecked.value = false
                    canBeShownInfixForm.value = true
                    scope.launch {
                        snackbarHostState.showSnackbar("Преобразование успешно завершено")
                    }
                }

                "5" -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Произошла ошибка")
                    }
                    lastOperation.value = true
                    isTactWorkStarted.value = false
                    textOutputIsClear.value = false
                    isInputChecked.value = false
                    break
                    // throw Error("Error")
                }

                "6" -> {
                    symbolsInput.removeFirst()
                    onOutputChange(symbol.value)
                }
            }
            shouldBeContinue.value = false
        }
    }
}