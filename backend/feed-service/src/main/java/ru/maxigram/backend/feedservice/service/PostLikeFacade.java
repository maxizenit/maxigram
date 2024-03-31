package ru.maxigram.backend.feedservice.service;

import ru.maxigram.backend.feedservice.entity.Like;

/** Сервис-фасад, объединяющий {@link PostService} и {@link LikeService}. */
public interface PostLikeFacade {

  /**
   * Ставит лайк на пост.
   *
   * @param postId идентификатор поста
   * @param authorId идентификатор автора
   * @return лайк, поставленный на пост
   */
  Like likePost(Long postId, String authorId);

  /**
   * Возвращает {@code true}, если пост лайкнут пользователем.
   *
   * @param postId идентификатор поста
   * @param userId идентификатор пользователя
   * @return {@code true}, если пост лайкнут пользователем
   */
  Boolean checkPostIsLikedByUser(Long postId, String userId);

  /**
   * Возвращает количество лайков поста.
   *
   * @param postId идентификатор поста
   * @return количество лайков поста
   */
  Integer getPostLikesCount(Long postId);
}
