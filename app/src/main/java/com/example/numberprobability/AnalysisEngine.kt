package com.example.numberprobability

import kotlin.random.Random

data class NumberScore(
    val number: Int,
    val totalCount: Int,
    val recent5: Int,
    val recent10: Int,
    val recent20: Int,
    val missing: Int,
    val score: Double
)

data class ComboScore(
    val numbers: List<Int>,
    val score: Double
)

object AnalysisEngine {

    fun analyze(draws: List<List<Int>>): List<NumberScore> {
        if (draws.isEmpty()) return emptyList()

        val totalDraws = draws.size
        val recent5Draws = draws.take(5)
        val recent10Draws = draws.take(10)
        val recent20Draws = draws.take(20)

        val totalCounts = IntArray(40)
        val recent5Counts = IntArray(40)
        val recent10Counts = IntArray(40)
        val recent20Counts = IntArray(40)

        draws.forEach { draw ->
            draw.forEach { number ->
                if (number in 1..39) {
                    totalCounts[number]++
                }
            }
        }

        recent5Draws.forEach { draw ->
            draw.forEach { number ->
                if (number in 1..39) {
                    recent5Counts[number]++
                }
            }
        }

        recent10Draws.forEach { draw ->
            draw.forEach { number ->
                if (number in 1..39) {
                    recent10Counts[number]++
                }
            }
        }

        recent20Draws.forEach { draw ->
            draw.forEach { number ->
                if (number in 1..39) {
                    recent20Counts[number]++
                }
            }
        }

        val maxTotal = totalCounts.drop(1).maxOrNull()?.coerceAtLeast(1) ?: 1
        val maxRecent5 = recent5Counts.drop(1).maxOrNull()?.coerceAtLeast(1) ?: 1
        val maxRecent10 = recent10Counts.drop(1).maxOrNull()?.coerceAtLeast(1) ?: 1
        val maxRecent20 = recent20Counts.drop(1).maxOrNull()?.coerceAtLeast(1) ?: 1

        return (1..39).map { number ->

            var missing = totalDraws

            for (index in draws.indices) {
                if (number in draws[index]) {
                    missing = index
                    break
                }
            }

            val totalScore =
                totalCounts[number].toDouble() / maxTotal.toDouble() * 30.0

            val recent5Score =
                recent5Counts[number].toDouble() / maxRecent5.toDouble() * 10.0

            val recent10Score =
                recent10Counts[number].toDouble() / maxRecent10.toDouble() * 15.0

            val recent20Score =
                recent20Counts[number].toDouble() / maxRecent20.toDouble() * 20.0

            var weightedRecent = 0.0

            draws.forEachIndexed { index, draw ->
                if (number in draw) {
                    val weight = (draws.size - index).toDouble() / draws.size.toDouble()
                    weightedRecent += weight
                }
            }

            val maxPossibleWeight =
                (1..draws.size).sum().toDouble() / draws.size.toDouble()

            val weightedScore =
                if (maxPossibleWeight > 0.0) {
                    weightedRecent / maxPossibleWeight * 25.0
                } else {
                    0.0
                }

            val finalScore =
                totalScore +
                recent5Score +
                recent10Score +
                recent20Score +
                weightedScore

            NumberScore(
                number = number,
                totalCount = totalCounts[number],
                recent5 = recent5Counts[number],
                recent10 = recent10Counts[number],
                recent20 = recent20Counts[number],
                missing = missing,
                score = finalScore
            )
        }.sortedByDescending { it.score }
    }

    fun generateCombinations(
        draws: List<List<Int>>,
        ranking: List<NumberScore>,
        count: Int
    ): List<ComboScore> {

        if (ranking.isEmpty() || count <= 0) {
            return emptyList()
        }

        val scoreMap = ranking.associate {
            it.number to it.score
        }

        val pairCounts = mutableMapOf<Pair<Int, Int>, Int>()

        draws.forEach { draw ->
            val sorted = draw
                .filter { it in 1..39 }
                .distinct()
                .sorted()

            for (i in sorted.indices) {
                for (j in i + 1 until sorted.size) {
                    val pair = sorted[i] to sorted[j]
                    pairCounts[pair] = (pairCounts[pair] ?: 0) + 1
                }
            }
        }

        val pool = ranking
            .take(18)
            .map { it.number }

        val candidates = mutableMapOf<List<Int>, Double>()

        repeat(3000) {

            if (pool.size < 5) {
                return@repeat
            }

            val combo = pool
                .shuffled(Random.Default)
                .take(5)
                .sorted()

            if (combo.size != 5) {
                return@repeat
            }

            val baseScore =
                combo.sumOf { number ->
                    scoreMap[number] ?: 0.0
                } / 5.0

            var pairScore = 0.0

            for (i in combo.indices) {
                for (j in i + 1 until combo.size) {
                    val pair = combo[i] to combo[j]
                    pairScore += (pairCounts[pair] ?: 0).toDouble()
                }
            }

            val oddCount = combo.count { it % 2 != 0 }
            val balanceBonus =
                if (oddCount == 2 || oddCount == 3) 4.0 else 0.0

            val lowCount = combo.count { it <= 13 }
            val midCount = combo.count { it in 14..26 }
            val highCount = combo.count { it >= 27 }

            val zoneBonus =
                if (lowCount > 0 && midCount > 0 && highCount > 0) {
                    4.0
                } else {
                    0.0
                }

            val finalScore =
                baseScore * 0.75 +
                pairScore * 0.8 +
                balanceBonus +
                zoneBonus

            val oldScore = candidates[combo]

            if (oldScore == null || finalScore > oldScore) {
                candidates[combo] = finalScore
            }
        }

        return candidates
            .entries
            .sortedByDescending { it.value }
            .take(count)
            .map { entry ->
                ComboScore(
                    numbers = entry.key,
                    score = entry.value
                )
            }
    }
}