package com.example.numberprobability

import kotlin.math.max

data class NumberScore(
    val number: Int,
    val score: Double,
    val totalCount: Int,
    val recent10: Int,
    val recent5: Int,
    val missing: Int
)

data class ComboScore(val numbers: List<Int>, val score: Double)

object AnalysisEngine {
    fun analyze(draws: List<List<Int>>): List<NumberScore> {
        if (draws.isEmpty()) return emptyList()
        val total = IntArray(40)
        val r10 = IntArray(40)
        val r5 = IntArray(40)
        val weighted = DoubleArray(40)
        val missing = IntArray(40) { draws.size }

        draws.forEachIndexed { idx, draw ->
            val weight = 1.0 / (1.0 + idx * 0.08)
            draw.forEach { n ->
                if (n in 1..39) {
                    total[n]++
                    if (idx < 10) r10[n]++
                    if (idx < 5) r5[n]++
                    weighted[n] += weight
                    if (missing[n] == draws.size) missing[n] = idx
                }
            }
        }

        val raw = (1..39).associateWith { n ->
            weighted[n] * 0.45 + total[n] * 0.30 + r10[n] * 0.15 + r5[n] * 0.10
        }
        val min = raw.values.minOrNull() ?: 0.0
        val max = raw.values.maxOrNull() ?: 1.0

        return (1..39).map { n ->
            val normalized = if (max == min) 50.0 else ((raw.getValue(n) - min) / (max - min)) * 100.0
            NumberScore(n, normalized, total[n], r10[n], r5[n], missing[n])
        }.sortedByDescending { it.score }
    }

    fun generateCombos(draws: List<List<Int>>, count: Int = 20): List<ComboScore> {
        val scores = analyze(draws).associateBy { it.number }
        val topPool = scores.values.sortedByDescending { it.score }.take(18).map { it.number }

        val pairCount = mutableMapOf<Pair<Int, Int>, Int>()
        draws.forEach { d ->
            val s = d.sorted()
            for (i in s.indices) for (j in i + 1 until s.size) {
                val p = s[i] to s[j]
                pairCount[p] = (pairCount[p] ?: 0) + 1
            }
        }

        val candidates = mutableListOf<ComboScore>()
        fun rec(start: Int, picked: MutableList<Int>) {
            if (picked.size == 5) {
                val sorted = picked.sorted()
                val base = sorted.sumOf { scores[it]?.score ?: 0.0 } / 5.0
                var pair = 0.0
                for (i in sorted.indices) for (j in i + 1 until sorted.size) {
                    pair += (pairCount[sorted[i] to sorted[j]] ?: 0) * 1.2
                }
                val odd = sorted.count { it % 2 == 1 }
                val oddBonus = if (odd in 2..3) 5.0 else 0.0
                val low = sorted.count { it <= 13 }
                val mid = sorted.count { it in 14..26 }
                val high = 5 - low - mid
                val zoneBonus = if (maxOf(low, mid, high) <= 3) 4.0 else 0.0
                candidates += ComboScore(sorted, base * 0.84 + pair + oddBonus + zoneBonus)
                return
            }
            for (i in start until topPool.size) {
                picked += topPool[i]
                rec(i + 1, picked)
                picked.removeAt(picked.lastIndex)
            }
        }
        rec(0, mutableListOf())

        val selected = mutableListOf<ComboScore>()
        for (c in candidates.sortedByDescending { it.score }) {
            val tooSimilar = selected.any { old -> old.numbers.intersect(c.numbers.toSet()).size >= 4 }
            if (!tooSimilar) selected += c
            if (selected.size >= count) break
        }
        return selected
    }
}
