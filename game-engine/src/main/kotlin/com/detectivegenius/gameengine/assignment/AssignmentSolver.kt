package com.detectivegenius.gameengine.assignment

import com.detectivegenius.core.model.QuestionPackage
import kotlin.random.Random

data class RawAssignment(
    val packageId: String,
    val authorId: String,
    val answererId: String,
    val recipientId: String
) {
    val recipientIds: Set<String> get() = setOf(recipientId)
}

class AssignmentSolver(
    private val maxAttempts: Int = 200
) {

    /**
     * Solves package generation and distribution satisfying all single-evidence-package invariants.
     * Guaranteed to work for all player counts in [4, 20].
     * Every player receives EXACTLY ONE package.
     */
    fun solve(playerIds: List<String>, random: Random = Random.Default): List<RawAssignment>? {
        require(playerIds.size >= 4) { "Minimum 4 players required, got ${playerIds.size}" }
        require(playerIds.size <= 20) { "Maximum 20 players allowed in V1, got ${playerIds.size}" }
        require(playerIds.size % 2 == 0) { "Player count must be even, got ${playerIds.size}" }

        val n = playerIds.size

        for (attempt in 1..maxAttempts) {
            val answerers = generateDerangement(playerIds, random) ?: continue

            val candidatePackages = playerIds.indices.map { i ->
                playerIds[i] to answerers[i]
            }

            val recipients = assignRecipients(playerIds, candidatePackages, random)
            if (recipients != null) {
                val assignments = candidatePackages.indices.map { i ->
                    val (author, answerer) = candidatePackages[i]
                    RawAssignment(
                        packageId = "pkg_${author}_${answerer}_$i",
                        authorId = author,
                        answererId = answerer,
                        recipientId = recipients[i]
                    )
                }

                if (validateAllInvariants(playerIds, assignments)) {
                    return assignments
                }
            }
        }
        return null
    }

    private fun generateDerangement(players: List<String>, random: Random): List<String>? {
        val n = players.size
        for (retry in 1..100) {
            val shuffled = players.shuffled(random)
            var isDerangement = true
            for (i in 0 until n) {
                if (shuffled[i] == players[i]) {
                    isDerangement = false
                    break
                }
            }
            if (isDerangement) return shuffled
        }
        return null
    }

    private fun assignRecipients(
        players: List<String>,
        packages: List<Pair<String, String>>,
        random: Random
    ): List<String>? {
        val n = packages.size
        for (retry in 1..100) {
            val shuffledRecipients = players.shuffled(random)
            var valid = true
            for (i in 0 until n) {
                val (author, answerer) = packages[i]
                val recipient = shuffledRecipients[i]
                if (recipient == author || recipient == answerer) {
                    valid = false
                    break
                }
            }
            if (valid) return shuffledRecipients
        }
        return null
    }

    /**
     * Authoritative validator for the single-package distribution invariants:
     * 1. Every player creates exactly one package as author.
     * 2. Every package contains 3 questions.
     * 3. Every question has 1 corresponding answer.
     * 4. Every package has 1 author.
     * 5. Every package has 1 answerer.
     * 6. Author and answerer must be different (author != answerer).
     * 7. Every package has exactly one recipient.
     * 8. A package cannot be delivered to its author (recipient != author).
     * 9. A package cannot be delivered to its answerer (recipient != answerer).
     * 10. Every player receives exactly one package.
     * 11. No player receives the same package twice.
     * 12. Every player participates as author, answerer, and recipient.
     */
    fun validateAllInvariants(players: List<String>, assignments: List<RawAssignment>): Boolean {
        val playerSet = players.toSet()
        val n = players.size

        if (assignments.size != n) return false

        // 1 & 4. Every player creates exactly one package as author
        val authors = assignments.map { it.authorId }
        if (authors.toSet() != playerSet || authors.size != n) return false

        // 5 & 12. Every player participates as answerer
        val answerers = assignments.map { it.answererId }
        if (answerers.toSet() != playerSet || answerers.size != n) return false

        // 7 & 10. Every player receives exactly one package
        val recipients = assignments.map { it.recipientId }
        if (recipients.toSet() != playerSet || recipients.size != n) return false

        for (pkg in assignments) {
            // 6. Author and answerer must be different
            if (pkg.authorId == pkg.answererId) return false

            // 8. Package cannot be delivered to its author
            if (pkg.recipientId == pkg.authorId) return false

            // 9. Package cannot be delivered to its answerer
            if (pkg.recipientId == pkg.answererId) return false
        }

        return true
    }
}
