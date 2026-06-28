# Конвенции проекта maxigram

Гайд для работы над кодовой базой. Подробный план и история решений —
[docs/plans/20260628-maxigram-v2.md](docs/plans/20260628-maxigram-v2.md).

## Архитектура

- **Модульный монолит** на Spring Modulith. Базовый пакет `org.maxizenit.maxigram`,
  один пакет верхнего уровня на модуль: `common, identity, profile, feed, chat,
  notification, matching, wellbeing`.
- Границы модулей обязаны проходить `ApplicationModules.verify()` (см. `ModularityTest`).
  Межмодульная связь — только через **доменные события** Spring (`@EventListener`),
  не через прямые вызовы сервисов чужого модуля.
- Сгенерированный jOOQ-пакет `org.maxizenit.maxigram.jooq` исключён из проверки модульности
  предикатом в тесте.

## Доступ к данным

- **Только jOOQ. JPA/Hibernate не добавлять** — это сознательное решение (в v1 именно JPA дала
  основной класс багов: cascade/lazy/N+1/неверная владеющая сторона).
- Источник правды схемы — **Flyway-миграции** (`src/main/resources/db/migration`). jOOQ-классы
  генерируются из них через `DDLDatabase` (без живой БД). Поменял схему → новая миграция `V{n}__*.sql`,
  затем `./gradlew generateJooq` (или просто сборка).
- Избегать N+1: агрегаты (счётчики лайков/комментариев, «лайкнул ли я») — коррелированными
  подзапросами в одном запросе, как в модуле `feed`.

## Identity и безопасность

- Identity берётся **из JWT**, никогда из query/path-параметров. В коде — `CurrentUser.id()`
  (читает `sub` из токена). Это ключевое исправление дефекта v1.
- API — stateless OAuth2 Resource Server (`/api/**`). Authorization Server (OIDC, PKCE,
  Argon2id) живёт в `identity`. Публичные эндпоинты (регистрация, верификация) — явный
  `permitAll` в `WebSecurityConfig`.
- CORS для SPA — `CorsConfig` (origin из `MAXIGRAM_CORS_ALLOWED_ORIGIN`), включён на API- и
  authorization-server-цепочках.

## Real-time (WebSocket/STOMP)

- STOMP-сессия аутентифицируется JWT в заголовке `Authorization` на **CONNECT**
  (`StompAuthChannelInterceptor`); SUBSCRIBE на `/topic/chats/{id}` авторизуется по участию.
- В анонимных чатах broadcast маскирует `senderId` в `null` для всех. Ответ `POST /messages`
  отдаёт реальный (свой) `senderId` и **авторитетен для своих сообщений** на клиенте
  (фронт делает upsert по id — см. фикс гонки в `ChatConversation`).

## Тестирование

- **TDD**. Каждая задача завершается тестами (успех + ошибки) и зелёным прогоном.
- Бэкенд: JUnit 5 + Testcontainers (**singleton-контейнер** PostgreSQL, без рестарта между
  классами). `./gradlew test` (нужен Docker).
- Фронт: Vitest + Testing Library + **MSW** (моки REST), `user-event` для интеракций. STOMP в
  компонентных тестах мокается. `cd frontend && npm test`.

## Стиль и процесс

- Kotlin: 4 пробела, явные типы в публичном API, data-классы для DTO, KDoc одной строкой на «почему».
- Комментарии и сообщения коммитов — на английском; объясняют намерение, а не пересказывают код.
- Каждая логическая задача — отдельный коммит. Новую ветку перед коммитом, если на `main`.
- Свежие, но взаимно совместимые версии зависимостей.

## Запуск

`docker compose up --build -d` (backend :8080 + postgres). Фронт: `cd frontend && npm run dev --
--port 5173` (порт фиксирован под `redirect_uri`). Node не в PATH сессии — `C:\Program Files\nodejs`.
