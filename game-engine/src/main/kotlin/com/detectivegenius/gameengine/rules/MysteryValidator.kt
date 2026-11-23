package com.detectivegenius.gameengine.rules

import com.detectivegenius.core.model.ChallengeType
import com.detectivegenius.core.model.GuessValidationResult
import com.detectivegenius.core.model.PlayerProgress
import com.detectivegenius.core.model.QuestionPackage

class MysteryValidator {

    data class CorrectAnswersForPlayer(
        val q1AnswererOfMyQuestions: String,
        val q2AuthorOfQuestionsIAnswered: String,
        val q3AuthorsOfPackagesIHeld: Set<String>,
        val q4AnswerersOfPackagesIHeld: Set<String>
    )

    fun deriveCorrectAnswers(
        playerId: String,
        allPackages: List<QuestionPackage>
    ): CorrectAnswersForPlayer {
        val packageIAuthored = allPackages.first { it.authorId == playerId }
        val packageIAnswered = allPackages.first { it.answererId == playerId }
        val packagesIReceived = allPackages.filter { it.recipientIds.contains(playerId) }

        require(packagesIReceived.size == 2) {
            "Player $playerId must have received exactly 2 packages, but found ${packagesIReceived.size}"
        }

        return CorrectAnswersForPlayer(
            q1AnswererOfMyQuestions = packageIAuthored.answererId,
            q2AuthorOfQuestionsIAnswered = packageIAnswered.authorId,
            q3AuthorsOfPackagesIHeld = packagesIReceived.map { it.authorId }.toSet(),
            q4AnswerersOfPackagesIHeld = packagesIReceived.map { it.answererId }.toSet()
        )
    }

    fun evaluateGuess(
        currentProgress: PlayerProgress,
        challengeType: ChallengeType,
        singleSuspectId: String?,
        suspectPair: Set<String>?,
        correctAnswers: CorrectAnswersForPlayer
    ): GuessValidationResult {
        if (currentProgress.isEliminated) {
            return GuessValidationResult.PlayerEliminated
        }

        if (currentProgress.solvedChallenges.contains(challengeType)) {
            return GuessValidationResult.AlreadySolved(challengeType)
        }

        val isCorrect: Boolean = when (challengeType) {
            ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS -> {
                if (singleSuspectId.isNullOrBlank()) {
                    return GuessValidationResult.InvalidSubmission("Single suspect required for Q1")
                }
                singleSuspectId == correctAnswers.q1AnswererOfMyQuestions
            }
            ChallengeType.Q2_WHOSE_QUESTIONS_DID_I_ANSWER -> {
                if (singleSuspectId.isNullOrBlank()) {
                    return GuessValidationResult.InvalidSubmission("Single suspect required for Q2")
                }
                singleSuspectId == correctAnswers.q2AuthorOfQuestionsIAnswered
            }
            ChallengeType.Q3_WHOSE_QUESTIONS_AM_I_HOLDING -> {
                if (suspectPair == null || suspectPair.size != 2) {
                    return GuessValidationResult.InvalidSubmission("Exactly 2 distinct suspects required for Q3")
                }
                suspectPair == correctAnswers.q3AuthorsOfPackagesIHeld
            }
            ChallengeType.Q4_WHOSE_ANSWERS_AM_I_HOLDING -> {
                if (suspectPair == null || suspectPair.size != 2) {
                    return GuessValidationResult.InvalidSubmission("Exactly 2 distinct suspects required for Q4")
                }
                suspectPair == correctAnswers.q4AnswerersOfPackagesIHeld
            }
        }

        return if (isCorrect) {
            GuessValidationResult.Correct(challengeType)
        } else {
            val newMistakes = currentProgress.mistakes + 1
            val eliminated = newMistakes >= ValidationRules.ELIMINATION_MISTAKES_THRESHOLD
            GuessValidationResult.Incorrect(
                challengeType = challengeType,
                currentMistakes = newMistakes,
                isEliminated = eliminated
            )
        }
    }
}
