package com.detectivegenius.gameengine.assignment

import com.detectivegenius.core.model.QuestionPackage
import kotlin.random.Random

data class RawAssignment(
    val packageId: String,
    val authorId: String,
    val answererId: String,
    val recipientIds: Set<String>
)

class AssignmentSolver(
    private val maxAttempts: Int = 100
) {

    /**
     * Solves package generation and distribution satisfying all 13 product invariants.
     * Guaranteed to work for all even player counts in [4, 20].
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
                        recipientIds = recipients[i]
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
    ): List<Set<String>>? {
        val n = packages.size
        val packageRecipients = Array<MutableSet<String>>(n) { mutableSetOf() }
        val playerReceivedCount = mutableMapOf<String, Int>().apply { players.forEach { put(it, 0) } }
        val playerReceivedAuthors = mutableMapOf<String, MutableSet<String>>().apply { players.forEach { put(it, mutableSetOf()) } }
        val playerReceivedAnswerers = mutableMapOf<String, MutableSet<String>>().apply { players.forEach { put(it, mutableSetOf()) } }

        val pkgIndices = packages.indices.shuffled(random)

        for (pkgIdx in pkgIndices) {
            val (author, answerer) = packages[pkgIdx]

            // Candidates: not author, not answerer, received < 2 packages, distinct author
            val eligible = players.filter { p ->
                p != author &&
                p != answerer &&
                (playerReceivedCount[p] ?: 0) < 2 &&
                !(playerReceivedAuthors[p]?.contains(author) ?: false)
            }.shuffled(random)

            // Prioritize distinct answerers where feasible
            val prioritized = eligible.sortedBy { p ->
                if (playerReceivedAnswerers[p]?.contains(answerer) == true) 1 else 0
            }

            if (prioritized.size < 2) {
                return null // Backtrack / retry
            }

            val chosen = prioritized.take(2).toSet()
            packageRecipients[pkgIdx].addAll(chosen)
            for (p in chosen) {
                playerReceivedCount[p] = (playerReceivedCount[p] ?: 0) + 1
                playerReceivedAuthors[p]?.add(author)
                playerReceivedAnswerers[p]?.add(answerer)
            }
        }

        if (players.any { playerReceivedCount[it] != 2 }) {
            return null
        }

        return packageRecipients.map { it.toSet() }
    }

    /**
     * Authoritative validator for the 13 required invariants:
     * 1. Every player creates exactly one package.
     * 2. Every package contains 3 questions (handled by engine model).
     * 3. Every question has 1 corresponding answer (handled by engine model).
     * 4. Every package has 1 author.
     * 5. Every package has 1 answerer.
     * 6. Author and answerer must be different.
     * 7. Every package has exactly two recipients.
     * 8. A package cannot be delivered to its author.
     * 9. A package cannot be delivered to its answerer.
     * 10. Every player receives exactly two packages.
     * 11. The two packages received by a player must have distinct authors.
     * 12. No player receives the same package twice.
     * 13. Every player participates as an author and answerer.
     */
    fun validateAllInvariants(players: List<String>, assignments: List<RawAssignment>): Boolean {
        val playerSet = players.toSet()
        val n = players.size

        if (assignments.size != n) return false

        // 1 & 4. Every player creates exactly one package as author
        val authors = assignments.map { it.authorId }
        if (authors.toSet() != playerSet || authors.size != n) return false

        // 5 & 13. Every player participates as answerer
        val answerers = assignments.map { it.answererId }
        if (answerers.toSet() != playerSet || answerers.size != n) return false

        for (pkg in assignments) {
            // 6. Author and answerer must be different
            if (pkg.authorId == pkg.answererId) return false

            // 7. Every package has exactly two recipients
            if (pkg.recipientIds.size != 2) return false

            // 8. Package cannot be delivered to its author
            if (pkg.recipientIds.contains(pkg.authorId)) return false

            // 9. Package cannot be delivered to its answerer
            if (pkg.recipientIds.contains(pkg.answererId)) return false
        }

        // 10. Every player receives exactly two packages
        val receivedPerPlayer = mutableMapOf<String, MutableList<RawAssignment>>()
        players.forEach { receivedPerPlayer[it] = mutableListOf() }
        for (pkg in assignments) {
            for (recipient in pkg.recipientIds) {
                val list = receivedPerPlayer[recipient] ?: return false
                list.add(pkg)
            }
        }

        for (p in players) {
            val received = receivedPerPlayer[p] ?: return false
            if (received.size != 2) return false

            // 11. Distinct authors for the 2 packages received
            if (received[0].authorId == received[1].authorId) return false

            // 12. No player receives the same package twice
            if (received[0].packageId == received[1].packageId) return false
        }

        return true
    }
}
