package org.vangel.modeling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import org.vangel.modeling.theme.AppTheme

@Composable
internal fun App() = AppTheme {
    val viewModel: AppViewModel = getViewModel(Unit, viewModelFactory { AppViewModel() })
    val uiState by viewModel.uiState.collectAsState()

    var text1Width by remember { mutableStateOf(0.dp) }

    var text2Width by remember { mutableStateOf(0.dp) }

    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    val symbolsInput = listOf(
        Symbol("(", SymbolType.OPEN_PARENTHESES),
        Symbol(")", SymbolType.CLOSE_PARENTHESES),
        Symbol("C", SymbolType.CLEAR),
        Symbol("DEL", SymbolType.DELETE),
        Symbol("^", SymbolType.OPERATION),
        Symbol("a", SymbolType.VARIABLE),
        Symbol("b", SymbolType.VARIABLE),
        Symbol("c", SymbolType.VARIABLE),
        Symbol("/", SymbolType.OPERATION),
        Symbol("abs", SymbolType.FUNCTION),
        Symbol("d", SymbolType.VARIABLE),
        Symbol("e", SymbolType.VARIABLE),
        Symbol("f", SymbolType.VARIABLE),
        Symbol("+", SymbolType.OPERATION),
        Symbol("sin", SymbolType.FUNCTION),
        Symbol("g", SymbolType.VARIABLE),
        Symbol("h", SymbolType.VARIABLE),
        Symbol("i", SymbolType.VARIABLE),
        Symbol("-", SymbolType.OPERATION),
        Symbol("cos", SymbolType.FUNCTION),
        Symbol("", SymbolType.EMPTY),
        Symbol("j", SymbolType.VARIABLE),
        Symbol("", SymbolType.EMPTY),
        Symbol("*", SymbolType.OPERATION),
        Symbol("tg", SymbolType.FUNCTION)
    )

    val tableApplyElements = listOf(
        listOf(" ", "\$", "+", "-", "*", "/", "^", "(", ")", "F", "P"),
        listOf("\$", "4", "1", "1", "1", "1", "1", "1", "5", "1", "6"),
        listOf("+", "2", "2", "2", "1", "1", "1", "1", "2", "1", "6"),
        listOf("-", "2", "2", "2", "1", "1", "1", "1", "2", "1", "6"),
        listOf("*", "2", "2", "2", "2", "2", "1", "1", "2", "1", "6"),
        listOf("/", "2", "2", "2", "2", "2", "1", "1", "2", "1", "6"),
        listOf("^", "2", "2", "2", "2", "2", "2", "1", "2", "1", "6"),
        listOf("(", "5", "1", "1", "1", "1", "1", "1", "3", "1", "6"),
        listOf("F", "2", "2", "2", "2", "2", "2", "1", "2", "5", "6")
    )

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
                "Преобразование выражений в постфиксной форме",
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Входная строка (в инфиксной форме)",
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
                        MasterFunctions(
                            modifier = Modifier.width(text1Width),
                            onSymbolClick = { symbol ->
                                viewModel.onEvent(UiEvent.OnSymbolClick(symbol, scope, snackbarHostState))
                            },
                            symbolElements = symbolsInput,
                        )
                    }
                }

                AnimatedVisibility(uiState.automaticMode) {
                    FilledTonalIconButton(onClick = {
                        if (ExpressionModel.checkInput(viewModel.uiState.value.inputTextState, snackbarHostState, scope)) {
                            viewModel.onEvent(
                                UiEvent.FormatToPostfix(
                                    tableApplyElements,
                                    scope,
                                    snackbarHostState,
                                    Mods.AUTOMATIC_MODE
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
                            "Выходная строка (в постфиксной форме)",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                text2Width = with(density) { coordinates.size.width.toDp() }
                            }
                        )
                        TextField(
                            value = uiState.outputTextField,
                            onValueChange = {},
                            modifier = Modifier.width(text2Width),
                            readOnly = true
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModel.onEvent(UiEvent.ChangeMode(Mods.AUTOMATIC_MODE, scope, snackbarHostState))
                            }.width(text2Width)
                        ) {
                            RadioButton(
                                selected = uiState.automaticMode,
                                onClick = {
                                    viewModel.onEvent(UiEvent.ChangeMode(Mods.AUTOMATIC_MODE, scope, snackbarHostState))
                                }
                            )
                            Text(text = "Автоматический режим")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModel.onEvent(UiEvent.ChangeMode(Mods.TACT_MODE, scope, snackbarHostState))
                            }.width(text2Width)
                        ) {
                            RadioButton(
                                selected = !uiState.automaticMode,
                                onClick = {
                                    viewModel.onEvent(UiEvent.ChangeMode(Mods.TACT_MODE, scope, snackbarHostState))
                                }
                            )
                            Text(text = "Ручной режим")
                        }
                        AnimatedVisibility(!uiState.automaticMode) {
                            Column {
                                ElevatedButton(onClick = {
                                    viewModel.onEvent(
                                        UiEvent.FormatToPostfix(
                                            tableApplyElements,
                                            scope,
                                            snackbarHostState,
                                            Mods.TACT_MODE
                                        )
                                    )
                                }, modifier = Modifier.width(text2Width)) {
                                    Text("Такт")
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(60.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Spacer(modifier = Modifier.width(85.dp))
                ElevatedCard {
                    StackOutput(uiState.stack)
                }
                ElevatedCard {
                    TableApply(tableApplyElements, uiState.operationIndexes)
                }
                ElevatedCard(

                ) {
                    FilledTonalButton(onClick = {

                    }, modifier = Modifier.width(300.dp).padding(12.dp)) {
                        Text("Преобразование выражений по их постфиксной форме", textAlign = TextAlign.Center)
                    }
                    FilledTonalButton(onClick = {

                    }, modifier = Modifier.width(300.dp).padding(12.dp)) {
                        Text("Вычисление выражений по их постфиксной форме", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun StackOutput(
    stack: List<Symbol>
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .height(355.dp)
            .padding(start = 6.dp, top = 6.dp, end = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Стек",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Divider(modifier = Modifier.width(100.dp))
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            stack.forEach {
                Text(
                    it.value,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MasterFunctions(
    modifier: Modifier = Modifier,
    onSymbolClick: (Symbol) -> Unit,
    symbolElements: List<Symbol>,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(symbolElements) { symbol ->
            Box(
                modifier = Modifier
                    .border(0.5.dp, Color.Black, RoundedCornerShape(12.dp)).clickable {
                        onSymbolClick(symbol)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    symbol.value,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
                )
            }
        }
    }
}


@Composable
fun TableApply(
    tableApplyElements: List<List<String>> = listOf(),
    operationIndexes: Pair<Int, Int>
) {
    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(11),
            modifier = Modifier.width(350.dp),
            userScrollEnabled = false
        ) {
            header {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            0.5.dp,
                            Color.Black,
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        "Таблица принятия решений",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            tableApplyElements.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, item ->
                    item {
                        val isSelected = operationIndexes.first == rowIndex && operationIndexes.second == colIndex
                        Box(
                            modifier = Modifier
                                .border(
                                    0.5.dp,
                                    if (rowIndex == 0 || colIndex == 0) Color.Black else Color.Gray
                                )
                                .background(if (isSelected) Color.Green else Color.Transparent)
                                .padding(6.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
        HelpersTextTable()
    }
}


@Composable
fun HelpersTextTable() {
    Column {
        Text("1. Поместить символ из входной строки в стек")
        Text("2. Извлечь символ из стека и отправить его в выходную строку")
        Text("3. Удалить \")\" из входной строки и \"(\" из стека")
        Text("4. Успешное окончание преобразования")
        Text("5. Ошибка")
        Text("6. Переслать символ из входной строки в выходную")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Условные обозначения", fontWeight = FontWeight.Bold)
        Text("$ - символ пустой строки или стека")
        Text("F - функция;")
        Text("P - переменная")
        Text("^ - бинарная операция возведения в степень")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Столбцам таблицы соответствуют элементы входной строки")
        Text("Строкам - элементы, находящиеся на вершине стека")
    }
}

fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

internal expect fun openUrl(url: String?)