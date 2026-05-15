package com.baseproject.speech.pronunciation.simple

import com.baseproject.speech.pronunciation.PronunciationAssessor
import com.baseproject.speech.pronunciation.PronunciationLevel
import com.baseproject.speech.pronunciation.PronunciationResult
import com.baseproject.speech.pronunciation.SttMetadata
import com.baseproject.speech.pronunciation.WordDetail
import java.util.Locale

class SimpleWordMatchAssessor : PronunciationAssessor() {

    override fun assess(
        referenceText: String,
        spokenText: String,
        locale: Locale,
        sttMetadata: SttMetadata?,
    ): PronunciationResult {
        val referenceWords = normalizeText(referenceText)
        val spokenWords = normalizeText(spokenText)

        if (referenceWords.isEmpty()) {
            return PronunciationResult(
                score = 0f,
                level = PronunciationLevel.TRY_AGAIN,
                matchedWords = emptyList(),
                unmatchedWords = emptyList(),
                wordDetails = emptyList(),
            )
        }

        val matched = mutableListOf<String>()
        val unmatched = mutableListOf<String>()
        val details = referenceWords.map { word ->
            val similarity = bestSimilarity(word, spokenWords)
            val isMatched = similarity >= FUZZY_THRESHOLD
            if (isMatched) matched.add(word) else unmatched.add(word)
            WordDetail(word = word, isMatched = isMatched, similarity = similarity)
        }

        val wordMatchScore = if (referenceWords.isEmpty()) 0f else {
            (details.sumOf { it.similarity.toDouble() }.toFloat() / referenceWords.size * 100f)
        }

        val orderScore = calculateOrderScore(referenceWords, spokenWords)

        val confidencePenalty = calculateConfidencePenalty(sttMetadata)
        val autoCorrectPenalty = calculateAutoCorrectPenalty(spokenText, sttMetadata)
        val totalPenalty = (confidencePenalty + autoCorrectPenalty).coerceAtMost(MAX_PENALTY)

        val rawScore = wordMatchScore * WEIGHT_MATCH + orderScore * WEIGHT_ORDER
        val finalScore = (rawScore - totalPenalty).coerceIn(0f, 100f)

        return PronunciationResult(
            score = finalScore,
            level = PronunciationLevel.fromScore(finalScore),
            matchedWords = matched,
            unmatchedWords = unmatched,
            wordDetails = details,
            wordMatchScore = wordMatchScore,
            orderScore = orderScore,
        )
    }

    private fun bestSimilarity(word: String, candidates: List<String>): Float {
        if (candidates.isEmpty()) return 0f
        return candidates.maxOf { candidate ->
            val maxLen = maxOf(word.length, candidate.length)
            if (maxLen == 0) return@maxOf 1f
            1f - levenshteinDistance(word, candidate).toFloat() / maxLen
        }
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1,
                )
            }
        }
        return dp[a.length][b.length]
    }

    private fun calculateOrderScore(reference: List<String>, spoken: List<String>): Float {
        if (reference.isEmpty() || spoken.isEmpty()) return 0f
        val lcsLen = longestCommonSubsequence(reference, spoken)
        return lcsLen.toFloat() / reference.size * 100f
    }

    private fun longestCommonSubsequence(a: List<String>, b: List<String>): Int {
        val dp = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in 1..a.size) {
            for (j in 1..b.size) {
                dp[i][j] = if (fuzzyEquals(a[i - 1], b[j - 1])) {
                    dp[i - 1][j - 1] + 1
                } else {
                    maxOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }
        return dp[a.size][b.size]
    }

    private fun fuzzyEquals(a: String, b: String): Boolean {
        if (a == b) return true
        val maxLen = maxOf(a.length, b.length)
        if (maxLen == 0) return true
        return (1f - levenshteinDistance(a, b).toFloat() / maxLen) >= FUZZY_THRESHOLD
    }

    // Low confidence from STT = user's pronunciation was unclear to the recognizer
    private fun calculateConfidencePenalty(metadata: SttMetadata?): Float {
        if (metadata == null) return 0f
        val confidence = metadata.confidence
        if (confidence < 0f) return 0f
        if (confidence >= CONFIDENCE_GOOD) return 0f
        return (CONFIDENCE_GOOD - confidence) / CONFIDENCE_GOOD * MAX_CONFIDENCE_PENALTY
    }

    // High divergence between best result and alternatives = heavy auto-correction
    private fun calculateAutoCorrectPenalty(spokenText: String, metadata: SttMetadata?): Float {
        if (metadata == null || metadata.alternatives.isEmpty()) return 0f
        val bestWords = normalizeText(spokenText).toSet()
        if (bestWords.isEmpty()) return 0f
        val divergenceScores = metadata.alternatives.map { alt ->
            val altWords = normalizeText(alt).toSet()
            val common = bestWords.intersect(altWords)
            1f - common.size.toFloat() / bestWords.size
        }
        val avgDivergence = divergenceScores.average().toFloat()
        return avgDivergence * MAX_AUTO_CORRECT_PENALTY
    }

    companion object {
        private const val FUZZY_THRESHOLD = 0.75f
        private const val WEIGHT_MATCH = 0.7f
        private const val WEIGHT_ORDER = 0.3f
        private const val CONFIDENCE_GOOD = 0.7f
        private const val MAX_CONFIDENCE_PENALTY = 15f
        private const val MAX_AUTO_CORRECT_PENALTY = 15f
        private const val MAX_PENALTY = 25f
    }
}
