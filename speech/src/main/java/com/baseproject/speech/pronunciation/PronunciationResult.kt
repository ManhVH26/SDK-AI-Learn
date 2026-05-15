package com.baseproject.speech.pronunciation

data class PronunciationResult(
    val score: Float,
    val level: PronunciationLevel,
    val matchedWords: List<String>,
    val unmatchedWords: List<String>,
    val wordDetails: List<WordDetail>,
    val wordMatchScore: Float = score,
    val orderScore: Float = 100f,
)

data class WordDetail(
    val word: String,
    val isMatched: Boolean,
    val similarity: Float = if (isMatched) 1f else 0f,
)

enum class PronunciationLevel(val label: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    TRY_AGAIN("Try Again");

    companion object {
        fun fromScore(score: Float): PronunciationLevel = when {
            score >= 90f -> EXCELLENT
            score >= 70f -> GOOD
            score >= 50f -> FAIR
            else -> TRY_AGAIN
        }
    }
}
