package org.maxizenit.maxigram.feed.web

import org.maxizenit.maxigram.common.CurrentUser
import org.maxizenit.maxigram.feed.FeedService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/feed")
class FeedController(
    private val feedService: FeedService,
    private val currentUser: CurrentUser,
) {

    @GetMapping
    fun feed(): List<PostResponse> = feedService.feedFor(currentUser.id()).map { it.toResponse() }
}
