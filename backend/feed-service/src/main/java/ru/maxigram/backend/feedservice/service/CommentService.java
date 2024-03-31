package ru.maxigram.backend.feedservice.service;

import java.util.Collection;
import java.util.Optional;
import ru.maxigram.backend.feedservice.entity.Comment;
import ru.maxigram.backend.feedservice.entity.Post;

public interface CommentService {

  Comment createComment(String authorId, String text, Post post);

  Optional<Comment> findCommentById(Long id);

  Collection<Comment> findCommentsByPost(Post post);

  Collection<Long> getPostIdsCommentedByAuthor(String authorId);
}
