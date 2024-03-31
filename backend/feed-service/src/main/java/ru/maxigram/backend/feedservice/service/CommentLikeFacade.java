package ru.maxigram.backend.feedservice.service;

import ru.maxigram.backend.feedservice.entity.Like;
import ru.maxigram.backend.rabbitmqcommons.model.UserActionsFromFeedServiceRequest;

/** Сервис-фасад, объединяющий {@link CommentService} и {@link LikeService}. */
public interface CommentLikeFacade {

  /**
   * Ставит лайк на комментарий.
   *
   * @param commentId идентификатор комментария
   * @param authorId идентификатор автора
   * @return лайк, поставленный на комментарий
   */
  Like likeComment(Long commentId, String authorId);

  /**
   * Возвращает {@code true}, если комментарий лайкнут пользователем.
   *
   * @param commentId идентификатор комментария
   * @param userId идентификатор пользователя
   * @return {@code true}, если комментарий лайкнут пользователем
   */
  Boolean checkCommentIsLikedByUser(Long commentId, String userId);

  /**
   * Возвращает количество лайков комментария.
   *
   * @param commentId идентификатор комментария
   * @return количество лайков комментария
   */
  Integer getCommentLikesCount(Long commentId);

  /**
   * Отправляет действия пользователя в сервисе в очередь RabbitMQ по запросу.
   *
   * @param request запрос
   */
  void sendUserActionsForCalculate(UserActionsFromFeedServiceRequest request);
}
