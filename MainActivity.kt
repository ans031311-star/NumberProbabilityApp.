package com.example.numberprobability

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.numberprobability.ui.theme.NumberProbabilityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NumberProbabilityTheme { App() } }
    }
}

@Composable
fun App() {
    var tab by remember { mutableIntStateOf(0) }
    var draws by remember { mutableStateOf(SampleData.draws) }
    val scores = remember(draws) { AnalysisEngine.analyze(draws) }
    val combos = remember(draws) { AnalysisEngine.generateCombos(draws, 20) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf("首頁", "排名", "組合", "新增").forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Text(listOf("⌂","#","★","+")[i]) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> HomeScreen(draws.size, scores)
                1 -> RankingScreen(scores)
                2 -> ComboScreen(combos)
                3 -> AddDrawScreen { newDraw -> draws = listOf(newDraw) + draws }
            }
        }
    }
}

@Composable
private fun HomeScreen(drawCount: Int, scores: List<NumberScore>) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("01～39 號碼機率分析", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("目前資料：$drawCount 期｜第一筆視為最新一期")
        }
        item {
            ElevatedCard {
                Column(Modifier.padding(16.dp)) {
                    Text("高評分號碼", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(scores.take(8).joinToString("　") { "%02d".format(it.number) }, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        items(scores.take(5)) { s -> ScoreRow(s) }
        item { Text("※ 分數是歷史資料的統計模型指標，不代表真實抽取機率。", style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun RankingScreen(scores: List<NumberScore>) {
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("01～39 完整排名", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        items(scores) { ScoreRow(it) }
    }
}

@Composable
private fun ScoreRow(s: NumberScore) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("%02d".format(s.number), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("模型分數 ${"%.1f".format(s.score)}")
                Text("總出現 ${s.totalCount} 次｜近10期 ${s.recent10}｜近5期 ${s.recent5}｜遺漏 ${s.missing} 期", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ComboScreen(combos: List<ComboScore>) {
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("TOP 20 候選組合", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        items(combos) { c ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(c.numbers.joinToString("、") { "%02d".format(it) }, fontWeight = FontWeight.Bold)
                    Text("${"%.1f".format(c.score)} 分")
                }
            }
        }
    }
}

@Composable
private fun AddDrawScreen(onAdd: (List<Int>) -> Unit) {
    var text by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("新增最新一期", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("例如：7,12,19,28,32") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            val nums = text.split(',', '，', ' ').mapNotNull { it.trim().toIntOrNull() }
            if (nums.size == 5 && nums.distinct().size == 5 && nums.all { it in 1..39 }) {
                onAdd(nums.sorted())
                text = ""
                message = "已加入最新一期，分析已更新。"
            } else message = "請輸入 5 個不重複的 01～39 號碼。"
        }) { Text("加入並重新分析") }
        Text(message)
    }
}
