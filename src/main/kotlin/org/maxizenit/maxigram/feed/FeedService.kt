package org.maxizenit.maxigram.feed

import org.maxizenit.maxigram.profile.SubscriptionService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FeedService(
    private val posts: PostRepository,
    private val subscriptions: SubscriptionService,
) {

    /** Posts from the authors the user follows, plus the user's own, newest first. */
    fun feedFor(requesterId: UUID): List<PostView> {
        val authors = subscriptions.authorsFollowedBy(requesterId).toMutableSet()
        authors.add(requesterId)
        return posts.feedView(authors, requesterId)
    }
}
