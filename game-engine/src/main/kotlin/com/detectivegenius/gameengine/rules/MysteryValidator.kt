package com.detectivegenius.gameengine.rules

import com.detectivegenius.core.model.ChallengeType
import com.detectivegenius.core.model.GuessValidationResult
import com.detectivegenius.core.model.PlayerProgress
import com.detectivegenius.core.model.QuestionPackage

class MysteryValidator {

    data class CorrectAnswersForPlayer(
        val q1AnswererOfMyQuestions: String,
        val q2AuthorOfQuestionsIAnswered: String,
        val q3AuthorOfPackageIHeld: String,
        val q4AnswererOfPackageIHeld: String
    ) {
        // Backwards compatibility sets
        val q3AuthorsOfPackagesIHeld: Set<String> get() = setOf(q3AuthorOfPackageIHeld)
        val q4AnswerersOfPackagesIHeld: Set<String> get() = setOf(q4AnswererOfPackageIHeld)
    }

    fun deriveCorrectAnswers(
        playerId: String,
        allPackages: List<QuestionPackage>
    ): CorrectAnswersForPlayer {
        val packageIAuthored = allPackages.first { it.authorId == playerId }
        val packageIAnswered = allPackages.first { it.answererId == playerId }
        val packagesIReceived = allPackages.filter { it.recipientIds.contains(playerId) }

        require(packagesIReceived.size == 1) {
            "Player $playerId must have received exactly 1 package, but found ${packagesIReceived.size}"
        }

        val myHeldPackage = packagesIReceived.first()

        return CorrectAnswersForPlayer(
            q1AnswererOfMyQuestions = packageIAuthored.answererId,
            q2AuthorOfQuestionsIAnswered = packageIAnswered.authorId,
            q3AuthorOfPackageIHeld = myHeldPackage.authorId,
            q4AnswererOfPackageIHeld = myHeldPackage.answererId
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

        val effectiveSuspectId = singleSuspectId ?: suspectPair?.firstOrNull()

        val isCorrect: Boolean = when (challengeType) {
            ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS -> {
                if (effectiveSuspectId.isNullOrBlank()) {
                    return GuessValidationResult.InvalidSubmission("Single suspect required for Q1")
                }
                effectiveSuspectId == correctAnswers.q1AnswererOfMyQuestions
            }
            ChallengeType.Q2_WHOSE_QUESTIONS_DID_I_ANSWER -> {
                if (effectiveSuspectId.isNullOrBlank()) {
                    return GuessValidationResult.InvalidSubmission("Single suspect required for Q2")
                }
                effectiveSuspectId == correctAnswers.q2AuthorOfQuestionsIAnswered
            }
            ChallengeType.Q3_WHOSE_QUESTIONS_AM_I_HOLDING -> {
                if (effectiveSuspectId.isNullOrBlank()) {
                    return GuessValidationResult.InvalidSubmission("Single suspect required for Q3")
                }
                effectiveSuspectId == correctAnswers.q3AuthorOfPackageIHeld
            }
            ChallengeType.Q4_WHOSE_ANSWERS_AM_I_HOLDING -> {
                if (effectiveSuspectId.isNullOrBlank()) {
                    return GuessValidationResult.InvalidSubmission("Single suspect required for Q4")
                }
                effectiveSuspectId == correctAnswers.q4AnswererOfPackageIHeld
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
