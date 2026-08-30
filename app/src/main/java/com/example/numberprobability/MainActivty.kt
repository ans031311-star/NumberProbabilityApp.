package com.example.numberprobability

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    var draws by remember { mutableStateOf(SampleData.draws) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val ranking = remember(draws) {
        AnalysisEngine.analyze(draws)
    }

    val combinations = remember(draws) {
        AnalysisEngine.generateCombinations(draws, 20)
    }
        Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("⌂") },
                    label = { Text("首頁") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("#") },
                    label = { Text("排名") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("★") },
                    label = { Text("組合") }
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Text("+") },
                    label = { Text("新增") }
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> HomeScreen(draws.size, ranking)
                1 -> RankingScreen(ranking)
                2 -> CombinationScreen(combinations)
                3 -> AddDrawScreen(
                    onAdd = { newDraw ->
                        draws = listOf(newDraw) + draws
                        selectedTab = 0
                    }
                )
            }
        }
    }
}
@Composable
fun HomeScreen(
    drawCount: Int,
    ranking: List<NumberScore>
) {
    val topNumbers = ranking.take(10)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "號碼機率分析",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "目前資料",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "已分析 $drawCount 組歷史號碼")
                    Text(text = "每組 5 個號碼・範圍 01～39")
                }
            }
        }

        item {
            Text(
                text = "目前綜合排名 TOP 10",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(topNumbers) { item ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.number.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            text = "模型分數 %.1f".format(item.score),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "歷史出現 ${item.totalCount} 次"
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "※ 分數是依歷史資料與近期趨勢計算，不代表未來實際中獎機率。",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
@Composable
fun RankingScreen(
    ranking: List<NumberScore>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "01～39 完整排名",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "依歷史次數、近期趨勢與加權分數排序",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        itemsIndexed(ranking) { index, item ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        modifier = Modifier.width(42.dp),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = item.number.toString().padStart(2, '0'),
                        modifier = Modifier.width(55.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "模型分數 %.1f".format(item.score),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "出現 ${item.totalCount} 次",
style = MaterialTheme.typography.bodySmall
)
}
}
}
}
}

item {
    Text(
        text = "※ 排名是統計模型結果，不代表下一組號碼的實際發生機率。",
        style = MaterialTheme.typography.bodySmall
    )
}
}
}  
@Composable
fun CombinationScreen(
    combinations: List<ComboScore>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "⭐ 組合推薦",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "依照歷史資料、近期趨勢與號碼搭配計算",
                style = MaterialTheme.typography.bodyMedium
            )
        } 
                itemsIndexed(combinations) { index, combo ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "第 ${index + 1} 組",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = combo.numbers
                            .joinToString("  ") {
                                "%02d".format(it)
                            },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "模型分數：%.1f".format(combo.score),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Text(
                text = "※ 組合為統計模型分析結果，不代表實際發生機率。",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}  
@Composable
fun AddDrawScreen(
    onAdd: (List<Int>) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "＋ 新增一期號碼",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "請輸入 5 個 01～39 的號碼",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("例如：07, 12, 19, 28, 32") },
            singleLine = true
        )    
        Button(
            onClick = {
                val numbers = text
                    .replace("，", ",")
                    .split(",", " ")
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.trim().toIntOrNull() }

                when {
                    numbers.size != 5 -> {
                        message = "請輸入剛好 5 個號碼"
                    }

                    numbers.distinct().size != 5 -> {
                        message = "5 個號碼不能重複"
                    }

                    numbers.any { it !in 1..39 } -> {
                        message = "號碼必須介於 01～39"
                    }

                    else -> {
                        onAdd(numbers.sorted())
                        text = ""
                        message = "新增成功，已重新分析"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("加入並重新分析")
        } 
        if (message.isNotEmpty()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = "新增後會自動重新計算 01～39 排名與推薦組合。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}              