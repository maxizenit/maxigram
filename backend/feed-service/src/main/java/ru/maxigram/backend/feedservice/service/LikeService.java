package ru.maxigram.backend.feedservice.service;

import java.util.Collection;
import ru.maxigram.backend.feedservice.entity.Comment;
import ru.maxigram.backend.feedservice.entity.Like;
import ru.maxigram.backend.feedservice.entity.Post;

/** Сервис для работы с лайками. */
public interface LikeService {

  /**
   * Ставит лайк на пост.
   *
   * @param post пост
   * @param authorId идентификатор автора
   * @return поставленный лайк
   */
  Like likePost(Post post, String authorId);

  /**
   * Ставит лайк на комментарий.
   *
   * @param comment комментарий
   * @param authorId идентификатор автора
   * @return поставленный лайк
   */
  Like likeComment(Comment comment, String authorId);

  /**
   * Возвращает идентификаторы всех лайкнутых пользователем постов.
   *
   * @param userId идентификатор пользователя
   * @return идентификаторы всех лайкнутых пользователем постов
   */
  Collection<Long> getPostIdsLikedByUser(String userId);

  /**
   * Возвращает идентификаторы всех лайкнутых пользователем комментариев.
   *
   * @param userId идентификатор пользователя
   * @return идентификаторы всех лайкнутых пользователем комментариев
   */
  Collection<Long> getCommentIdsLikedByUser(String userId);
}
