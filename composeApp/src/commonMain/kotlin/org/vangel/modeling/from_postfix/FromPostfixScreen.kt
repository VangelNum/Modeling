package org.vangel.modeling.from_postfix

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.vangel.modeling.ChangeCurrentScreen
import org.vangel.modeling.core.Mods
import org.vangel.modeling.core.Screens
import org.vangel.modeling.core.Symbol
import org.vangel.modeling.core.SymbolType
import org.vangel.modeling.to_postfix.ToPostfixViewModel
import org.vangel.modeling.to_postfix.UiStateToPostfix

@Composable
fun FromPostfixScreen(
    viewModelToPostfix: ToPostfixViewModel,
    uiStateToPostfix: UiStateToPostfix
) {
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    val density = LocalDensity.current

    var text1Width by remember { mutableStateOf(0.dp) }

    var text2Width by remember { mutableStateOf(0.dp) }

    val viewModelFromPostfix: FromPostfixViewModel =
        getViewModel(Unit, viewModelFactory { FromPostfixViewModel() })
    val uiState by viewModelFromPostfix.uiFromPostfixState.collectAsState()

    LaunchedEffect(uiStateToPostfix.currentScreen) {
        if (uiStateToPostfix.currentScreen == Screens.FROM_POSTFIX_SCREEN) {
            viewModelFromPostfix.onEvent(UiEventFromPostfix.ParsePostfixString(uiStateToPostfix.outputTextField))
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = 8.dp, end = 8.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Вычисление выражений по их постфиксной форме",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ElevatedCard() {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Исходное выражение в постфиксной форме",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                text1Width = with(density) { coordinates.size.width.toDp() }
                            }
                        )
                        TextField(
                            value = uiState.inputTextState.joinToString("") { it.value },
                            onValueChange = {},
                            modifier = Modifier.width(text1Width),
                            label = { Text("Входная строка") },
                            readOnly = true
                        )
                        TextField(
                            value = uiState.receivedString.joinToString(" ") { it.value },
                            onValueChange = {},
                            modifier = Modifier.width(text1Width),
                            label = { Text("Полученная выходная строка ") },
                            readOnly = true
                        )
                        Text("Исходная полученная выходная строка:")
                        Box(
                            modifier = Modifier.width(text1Width)
                        ) {
                            Text(uiState.copyReceivedString.joinToString(" ") { it.value })
                        }
                        Row {
                            Text("Полученный результат: ")
                            Text(uiState.finalResult.toString())
                        }
                    }
                }

                AnimatedVisibility(uiState.automaticMode) {
                    FilledTonalIconButton(onClick = {
                        val isOkayInput =
                            checkInputFromPostfix(uiState.symbolValues, scope, snackbarHostState)
                        if (isOkayInput) {
                            viewModelFromPostfix.onEvent(
                                UiEventFromPostfix.CalculateInput(
                                    uiStateToPostfix.inputTextState
                                )
                            )
                        }
                    }, modifier = Modifier.padding(start = 60.dp, end = 60.dp, top = 32.dp)) {
                        Icon(imageVector = Icons.Outlined.ArrowForward, contentDescription = null)
                    }
                }

                AnimatedVisibility(!uiState.automaticMode) {
                    Spacer(modifier = Modifier.padding(start = 12.dp, end = 12.dp))
                }

                ElevatedCard() {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Исходное выражение в инфиксной форме",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                text2Width = with(density) { coordinates.size.width.toDp() }
                            }
                        )
                        TextField(
                            value = uiState.originalInInfixForm.joinToString("") { it.value },
                            onValueChange = {},
                            modifier = Modifier.width(text2Width),
                            readOnly = true
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModelFromPostfix.onEvent(
                                    UiEventFromPostfix.ChangeMode(
                                        Mods.AUTOMATIC_MODE,
                                        scope,
                                        snackbarHostState
                                    )
                                )
                            }.width(text2Width)
                        ) {
                            RadioButton(
                                selected = uiState.automaticMode,
                                onClick = {
                                    viewModelFromPostfix.onEvent(
                                        UiEventFromPostfix.ChangeMode(
                                            Mods.AUTOMATIC_MODE,
                                            scope,
                                            snackbarHostState
                                        )
                                    )
                                }
                            )
                            Text(text = "Автоматический режим")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModelFromPostfix.onEvent(
                                    UiEventFromPostfix.ChangeMode(
                                        Mods.TACT_MODE,
                                        scope,
                                        snackbarHostState
                                    )
                                )
                            }.width(text2Width)
                        ) {
                            RadioButton(
                                selected = !uiState.automaticMode,
                                onClick = {
                                    viewModelFromPostfix.onEvent(
                                        UiEventFromPostfix.ChangeMode(
                                            Mods.TACT_MODE,
                                            scope,
                                            snackbarHostState
                                        )
                                    )
                                }
                            )
                            Text(text = "Ручной режим")
                        }
                        AnimatedVisibility(!uiState.automaticMode) {
                            Column {
                                ElevatedButton(onClick = {
                                    viewModelFromPostfix.onEvent(
                                        UiEventFromPostfix.CalculateInputForTactMode(
                                            uiState.receivedString,
                                            scope,
                                            snackbarHostState,
                                            uiStateToPostfix.inputTextState
                                        )
                                    )
                                }, modifier = Modifier.width(text2Width)) {
                                    Text("Такт")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    ElevatedCard(
                        modifier = Modifier.width(text2Width)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "Поместить первый символ входной строки в стек",
                                color = if (uiState.currentStep == 1) Color.Green else Color.Black // Change currentStep accordingly
                            )
                            Text(
                                "Поместить результат выполнения операции (функции) в стек",
                                color = if (uiState.currentStep == 2) Color.Green else Color.Black
                            )
                            Text(
                                "Извлечь символ из стека",
                                color = if (uiState.currentStep == 3) Color.Green else Color.Black
                            )
                            Text(
                                "Вычислить значение функции",
                                color = if (uiState.currentStep == 4) Color.Green else Color.Black
                            )
                            Text(
                                "Выполнить бинарную операцию",
                                color = if (uiState.currentStep == 5) Color.Green else Color.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ElevatedCard(
                        modifier = Modifier.width(text2Width)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("Тип первого символа входной строки")
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.symbolType.type == SymbolType.OPERATION,
                                    onClick = {}
                                )
                                Text("Бинарная операция")
                                Spacer(modifier = Modifier.width(8.dp))
                                if (uiState.symbolType.type == SymbolType.OPERATION) {
                                    Card {
                                        Text(
                                            uiState.symbolType.value,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.symbolType.type == SymbolType.FUNCTION,
                                    onClick = {

                                    }
                                )
                                Text("Функция")
                                Spacer(modifier = Modifier.width(8.dp))
                                if (uiState.symbolType.type == SymbolType.FUNCTION) {
                                    Card {
                                        Text(
                                            uiState.symbolType.value,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.symbolType.type == SymbolType.VARIABLE,
                                    onClick = {}
                                )
                                Text("Число")
                                Spacer(modifier = Modifier.width(8.dp))
                                if (uiState.symbolType.type == SymbolType.VARIABLE) {
                                    Card {
                                        Text(
                                            uiState.symbolType.value,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                ElevatedCard {
                    StackOutput(uiState.stack)
                }
                ElevatedCard {
                    NumbersMarks(
                        uiState.inputTextState,
                        uiState.symbolValues,
                        viewModelFromPostfix
                    )
                }
                ElevatedCard {
                    FunctionsMarks(
                        uiState.inputTextState
                    )
                }
                ChangeCurrentScreen(
                    viewModelToPostfix, uiStateToPostfix
                )
            }
        }
    }
}


@Composable
fun StackOutput(
    stack: MutableList<Double>
) {
    Column(
        modifier = Modifier
            .width(210.dp)
            .height(400.dp)
            .padding(start = 6.dp, top = 6.dp, end = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Стек",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Divider(modifier = Modifier.width(190.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            stack.reversed().forEach {
                Card(
                    modifier = Modifier.width(190.dp)
                ) {
                    Text(
                        it.toString(),
                        modifier = Modifier.padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

fun checkInputFromPostfix(
    symbolValues: MutableMap<String, Int?>,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState
): Boolean {
    val allLettersHaveValues = symbolValues.keys.all { symbol ->
        symbolValues[symbol] != null
    }
    if (!allLettersHaveValues) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = "Не все буквы имеют значения",
            )
        }
    }
    return allLettersHaveValues
}

@Composable
fun NumbersMarks(
    inputTextState: MutableList<Symbol>,
    symbolValues: MutableMap<String, Int?>,
    viewModelFromPostfix: FromPostfixViewModel,
) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .height(400.dp)
            .padding(start = 6.dp, top = 6.dp, end = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Обозначение чисел",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Divider(modifier = Modifier.width(180.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            inputTextState.filter { it.type == SymbolType.VARIABLE }
                .forEachIndexed { index, symbol ->
                    Card() {
                        Row(
                            modifier = Modifier.padding(
                                start = 12.dp,
                                end = 12.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            ),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = symbol.value)
                            Text(" = ")
                            BasicTextField(
                                value = if (symbolValues[symbol.value] == null) "" else symbolValues[symbol.value].toString(),
                                onValueChange = { newValue ->
                                    if (newValue.isNotBlank()) {
                                        newValue.toIntOrNull()?.let { intValue ->
                                            viewModelFromPostfix.onEvent(
                                                UiEventFromPostfix.ChangeSymbolValues(
                                                    newValue = intValue,
                                                    key = symbol.value
                                                )
                                            )
                                        }
                                    } else {
                                        viewModelFromPostfix.onEvent(
                                            UiEventFromPostfix.ChangeSymbolValues(
                                                newValue = null,
                                                key = symbol.value
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.width(120.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                            )
                        }
                    }
                }
        }
    }
}

@Composable
fun FunctionsMarks(inputTextState: MutableList<Symbol>) {
    Column(
        modifier = Modifier
            .width(320.dp)
            .height(400.dp)
            .padding(start = 6.dp, top = 6.dp, end = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Обозначение функций",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Divider(modifier = Modifier.width(280.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            inputTextState.filter { it.type == SymbolType.FUNCTION }
                .forEachIndexed { index, symbol ->
                    Card() {
                        Row(
                            modifier = Modifier.padding(
                                start = 12.dp,
                                end = 12.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            ),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "\"${symbol.value}\"")
                            Text(text = " - функция ")
                            when (symbol.value) {
                                "т" -> Text(text = " \"abs\" - модуль")
                                "в" -> Text("\"sin\" - синус")
                                "г" -> Text("\"cos\" - косинус")
                                "и" -> Text(" = \"ln\" - н. логарифм")
                            }
                        }
                    }
                }
        }
    }
}