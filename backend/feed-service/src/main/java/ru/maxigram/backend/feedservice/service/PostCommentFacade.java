package ru.maxigram.backend.feedservice.service;

import java.util.Collection;
import ru.maxigram.backend.feedservice.entity.Comment;

/** Сервис-фасад, объединяющий {@link PostService} и {@link CommentService}. */
public interface PostCommentFacade {

  /**
   * Создаёт комментарий.
   *
   * @param authorId идентификатор автора
   * @param text текст
   * @param postId идентификатор поста
   * @return созданный комментарий
   */
  Comment createComment(String authorId, String text, Long postId);

  /**
   * Возвращает все комментарии к посту.
   *
   * @param postId идентификатор поста
   * @return все комментарии к посту
   */
  Collection<Comment> findCommentsByPostId(Long postId);
}
