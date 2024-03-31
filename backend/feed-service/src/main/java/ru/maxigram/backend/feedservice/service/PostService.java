package ru.maxigram.backend.feedservice.service;

import java.util.Collection;
import java.util.Optional;
import ru.maxigram.backend.feedservice.entity.Post;

/** Сервис для работы с постами. */
public interface PostService {

  /**
   * Создаёт пост.
   *
   * @param authorId идентификатор автора
   * @param text текст
   * @return созданный пост
   */
  Post createPost(String authorId, String text);

  /**
   * Ищет пост по идентификатору.
   *
   * @param id идентификатор поста
   * @return {@link Optional} с постом с указанным идентификатором
   */
  Optional<Post> findPostById(Long id);

  /**
   * Ищет посты заданных авторов.
   *
   * @param authorIds идентификаторы авторов
   * @return посты заданных авторов
   */
  Collection<Post> findPostsByAuthorIds(Collection<String> authorIds);
}
