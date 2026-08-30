package com.example.numberprobability

import kotlin.math.roundToInt
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

        val total = IntArray(40)
        val r5 = IntArray(40)
        val r10 = IntArray(40)
        val r20 = IntArray(40)

        draws.forEachIndexed { index, draw ->
            draw.forEach { n ->
                if (n in 1..39) {
                    total[n]++
                    if (index < 5) r5[n]++
                    if (index < 10) r10[n]++
                    if (index < 20) r20[n]++
                }
            }
        }

        val maxTotal = (1..39).maxOf { total[it] }.coerceAtLeast(1)
        val max5 = (1..39).maxOf { r5[it] }.coerceAtLeast(1)
        val max10 = (1..39).maxOf { r10[it] }.coerceAtLeast(1)
        val max20 = (1..39).maxOf { r20[it] }.coerceAtLeast(1)

        return (1..39).map { n ->

            val missing = draws.indexOfFirst { n in it }
                .let { if (it == -1) draws.size else it }

            val totalPart =
                total[n].toDouble() / maxTotal * 30.0

            val recent20Part =
                r20[n].toDouble() / max20 * 20.0

            val recent10Part =
                r10[n].toDouble() / max10 * 25.0

            val recent5Part =
                r5[n].toDouble() / max5 * 25.0

            val score =
                totalPart +
                recent20Part +
                recent10Part +
                recent5Part

            NumberScore(
                number = n,
                totalCount = total[n],
                recent5 = r5[n],
                recent10 = r10[n],
                recent20 = r20[n],
                missing = missing,
                score = score
            )
        }.sortedByDescending { it.score }
    }

    fun generateCombinations(
        draws: List<List<Int>>,
        amount: Int = 20
    ): List<ComboScore> {

        val ranking = analyze(draws)

        if (ranking.isEmpty()) return emptyList()

        val candidates =
            ranking.take(18).map { it.number }

        val scoreMap =
            ranking.associate { it.number to it.score }

        val pairCounts =
            mutableMapOf<Pair<Int, Int>, Int>()

        draws.forEach { draw ->
            val sorted = draw.sorted()

            for (i in sorted.indices) {
                for (j in i + 1 until sorted.size) {
                    val pair =
                        sorted[i] to sorted[j]

                    pairCounts[pair] =
                        (pairCounts[pair] ?: 0) + 1
                }
            }
        }

        val results =
            mutableMapOf<List<Int>, Double>()

        repeat(5000) {

            val combo =
                candidates
                    .shuffled(Random.Default)
                    .take(5)
                    .sorted()

            if (combo.size != 5) return@repeat

            val baseScore =
                combo.sumOf {
                    scoreMap[it] ?: 0.0
                } / 5.0

            var pairScore = 0.0

            for (i in combo.indices) {
                for (j in i + 1 until combo.size) {
                    pairScore +=
                        pairCounts[
                            combo[i] to combo[j]