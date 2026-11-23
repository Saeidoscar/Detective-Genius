package com.detectivegenius.gameengine.rules

object ValidationRules {
    const val MIN_PLAYERS = 4
    const val MAX_PLAYERS = 20
    const val QUESTIONS_PER_PACKAGE = 3
    const val ANSWERS_PER_PACKAGE = 3
    const val PACKAGES_RECEIVED_PER_PLAYER = 2
    const val MAX_TOLERATED_MISTAKES = 2
    const val ELIMINATION_MISTAKES_THRESHOLD = 3

    const val MIN_QUESTION_LENGTH = 5
    const val MAX_QUESTION_LENGTH = 300
    const val MAX_ANSWER_LENGTH = 500

    sealed class QuestionValidationResult {
        data object Valid : QuestionValidationResult()
        data class Error(val message: String) : QuestionValidationResult()
    }

    sealed class AnswerValidationResult {
        data object Valid : AnswerValidationResult()
        data class Error(val message: String) : AnswerValidationResult()
    }

    fun validateQuestions(questions: List<String>): QuestionValidationResult {
        if (questions.size != QUESTIONS_PER_PACKAGE) {
            return QuestionValidationResult.Error("Exactly $QUESTIONS_PER_PACKAGE questions are required, got ${questions.size}.")
        }

        for ((idx, q) in questions.withIndex()) {
            val trimmed = q.trim()
            if (trimmed.length < MIN_QUESTION_LENGTH) {
                return QuestionValidationResult.Error("Question ${idx + 1} is too short (minimum $MIN_QUESTION_LENGTH characters).")
            }
            if (trimmed.length > MAX_QUESTION_LENGTH) {
                return QuestionValidationResult.Error("Question ${idx + 1} exceeds maximum of $MAX_QUESTION_LENGTH characters.")
            }
        }

        val uniqueCount = questions.map { it.trim().lowercase() }.distinct().size
        if (uniqueCount != QUESTIONS_PER_PACKAGE) {
            return QuestionValidationResult.Error("Duplicate questions from the same author are forbidden.")
        }

        return QuestionValidationResult.Valid
    }

    fun validateAnswers(answers: Map<String, String>, expectedQuestionIds: List<String>): AnswerValidationResult {
        if (answers.size != expectedQuestionIds.size) {
            return AnswerValidationResult.Error("All ${expectedQuestionIds.size} questions must be answered.")
        }

        for (qId in expectedQuestionIds) {
            val ans = answers[qId]?.trim()
            if (ans.isNullOrEmpty()) {
                return AnswerValidationResult.Error("Missing answer for question $qId.")
            }
            if (ans.length > MAX_ANSWER_LENGTH) {
                return AnswerValidationResult.Error("Answer exceeds maximum of $MAX_ANSWER_LENGTH characters.")
            }
        }

        return AnswerValidationResult.Valid
    }

    fun canStartGame(playerCount: Int, allReady: Boolean): Pair<Boolean, String?> {
        if (playerCount < MIN_PLAYERS) {
            return false to "At least $MIN_PLAYERS players required."
        }
        if (playerCount > MAX_PLAYERS) {
            return false to "Maximum $MAX_PLAYERS players allowed."
        }
        if (playerCount % 2 != 0) {
            return false to "Player count must be an even number."
        }
        if (!allReady) {
            return false to "All players must mark themselves ready."
        }
        return true to null
    }
}
