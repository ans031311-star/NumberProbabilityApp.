package com.example.numberprobability

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                NumberProbabilityApp()
            }
        }
    }
}

@Composable
fun NumberProbabilityApp() {

    val draws = remember {
        mutableStateListOf<List<Int>>().apply {
            addAll(SampleData.draws)
        }
    }

    var tab by remember {
        mutableIntStateOf(0)
    }

    val rankings = AnalysisEngine.analyze(draws.toList())

    val combinations =
        AnalysisEngine.generateCombinations(
            draws = draws.toList(),
            count = 20
        )

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Text("⌂") },
                    label = { Text("首頁") }
                )

                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Text("#") },
                    label = { Text("排名") }
                )

                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Text("★") },
                    label = { Text("組合") }
                )

                NavigationBarItem(
                    selected = tab == 3,
                    onClick = { tab = 3 },
                    icon = { Text("+") },
                    label = { Text("新增") }
                )
            }
        }

    ) { paddingValues ->

        when (tab) {

            0 -> HomeScreen(
                drawCount = draws.size,
                rankings = rankings,
                modifier = Modifier.padding(paddingValues)
            )

            1 -> RankingScreen(
                rankings = rankings,
                modifier = Modifier.padding(paddingValues)
            )

            2 -> CombinationScreen(
                combinations = combinations,
                modifier = Modifier.padding(paddingValues)
            )

            3 -> AddDrawScreen(
                modifier = Modifier.padding(paddingValues),
                onAdd = { numbers ->

                    draws.add(
                        index = 0,
                        element = numbers
                    )

                    tab = 0
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    drawCount: Int,
    rankings: List<NumberScore>,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        item {

            Text(
                text = "號碼機率分析",
                style =
                    MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "目前資料：$drawCount 組"
            )

            Text(
                text =
                    "模型分數為歷史統計指標，不代表下一組號碼的實際發生機率。",
                style =
                    MaterialTheme.typography.bodySmall
            )
        }

        item {

            Text(
                text = "目前排名前 10",
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(
            rankings.take(10)
        ) { score ->

            NumberScoreCard(
                score = score
            )
        }
    }
}

@Composable
fun RankingScreen(
    rankings: List<NumberScore>,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        item {

            Text(
                text = "01～39 完整排名",
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(rankings) { score ->

            NumberScoreCard(
                score = score
            )
        }
    }
}

@Composable
fun NumberScoreCard(
    score: NumberScore
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "%02d".format(score.number),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "出現 ${score.totalCount} 次",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column {
                Text(
                    text = "分數 %.1f".format(score.score),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "近5期 ${score.recent5}｜近10期 ${score.recent10}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "遺漏 ${score.missing} 期",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun CombinationScreen(
    combinations: List<ComboScore>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "候選組合",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "依歷史統計與號碼共現關係排序。",
                style = MaterialTheme.typography.bodySmall
            )
        }

        items(combinations) { combo ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = combo.numbers.joinToString("  ") {
                            "%02d".format(it)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "模型分數 %.1f".format(combo.score),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun AddDrawScreen(
    modifier: Modifier = Modifier,
    onAdd: (List<Int>) -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("請輸入 5 個不重複的 01～39 號碼")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "新增最新一組",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("例如：11,12,18,20,29")
            }
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = {
                val numbers = text
                    .replace("，", ",")
                    .replace(" ", ",")
                    .split(",")
                    .mapNotNull {
                        it.trim().toIntOrNull()
                    }
                    .distinct()

                if (
                    numbers.size == 5 &&
                    numbers.all { it in 1..39 }
                ) {
                    onAdd(numbers.sorted())
                    text = ""
                    message = "新增成功，已重新分析"
                } else {
                    message = "請輸入 5 個不重複的 01～39 號碼"
                }
            }
        ) {
            Text("加入並重新分析")
        }
    }
}    