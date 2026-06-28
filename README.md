# maxigram

Социальная сеть с упором на **осознанное общение**: лента с подписками, анонимные чаты со
взаимной деанонимизацией, подбор собеседника по «похожести» (возраст + общие интересы +
схожесть активности) и инструменты цифрового благополучия (самоограничения).

Переработка дипломного проекта 2024 года в качественный инженерный проект: вместо четырёх
gRPC-микросервисов на JPA — **модульный монолит** на Spring Modulith с jOOQ, встроенным
OAuth2 Authorization Server и SPA-фронтендом.

## Стек

**Бэкенд:** Kotlin 2.3 · Spring Boot 4.1 (Java 25) · Spring Modulith 2.0 · jOOQ 3.21 + Flyway ·
Spring Authorization Server (OIDC/JWT, пароли Argon2id) · WebSocket/STOMP · PostgreSQL 16.

**Фронтенд:** React 18 · Vite 6 · TypeScript · oidc-client-ts (Authorization Code + PKCE) ·
`@stomp/stompjs` · Vitest + Testing Library + MSW.

## Архитектура

Модульный монолит. Границы модулей проверяются `ApplicationModules.verify()`, межмодульное
взаимодействие — через доменные события Spring (`SubscriptionCreated`, `PostLiked`,
`PostCommented`, `MessageSent`, `MatchFound`).

| Модуль | Назначение |
|--------|------------|
| `common` | Кросс-модульные примитивы (`CurrentUser` — identity из JWT, отправка email) |
| `identity` | Authorization Server, регистрация, верификация/сброс пароля, CORS, security |
| `profile` | Профили, интересы, таймзона, подписки, история активности |
| `feed` | Посты, лайки, комментарии (без N+1, агрегаты коррелированными подзапросами) |
| `chat` | Чаты, сообщения, real-time (STOMP), анонимные чаты + деанонимизация |
| `notification` | In-app уведомления (push по STOMP) и email на доменных событиях |
| `matching` | Подбор собеседника по похожести; очередь в БД, анонимный чат при матче |
| `wellbeing` | Самоограничения (окно полного блока `/api/**`, кроме `wellbeing`) |

Доступ к данным — jOOQ; типобезопасный код генерируется из Flyway-миграций (`DDLDatabase`,
без живой БД) в `org.maxizenit.maxigram.jooq`. **JPA не используется сознательно** — чтобы
исключить класс багов v1 (cascade/lazy/N+1/неверная владеющая сторона).

## Запуск

### Полный стек в Docker

```bash
docker compose up --build -d      # backend :8080 + PostgreSQL :5432
```

Приложение применяет миграции Flyway на старте. Проверка: `curl localhost:8080/actuator/health`.

### Фронтенд (dev)

```bash
cd frontend
npm install
npm run dev -- --port 5173        # порт фиксирован: redirect_uri клиента = http://localhost:5173/callback
```

Дефолты SPA (`VITE_API_URL`, `VITE_OIDC_AUTHORITY` = `http://localhost:8080`) совпадают с
докер-стеком, поэтому `.env` для локального запуска не нужен.

### Первый вход

1. Открыть http://localhost:5173 → «Войти» (редирект на форму Authorization Server).
2. Аккаунта ещё нет — зарегистрировать через API:
   ```bash
   curl -X POST http://localhost:8080/api/identity/registrations \
     -H 'Content-Type: application/json' \
     -d '{"email":"me@example.com","password":"Password123!"}'
   ```
3. Войти этими данными → SPA вернётся с токеном. Профиля нет (404 → пустая форма) — заполнить
   и пользоваться. Для проверки чатов/подбора нужно ≥2 аккаунта.

## Тесты

```bash
./gradlew test                    # бэкенд: модульность + Testcontainers (нужен Docker)
cd frontend && npm test           # фронт: Vitest + MSW
```

## Переменные окружения (бэкенд)

| Переменная | Назначение | Дефолт |
|-----------|------------|--------|
| `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | Подключение к PostgreSQL | — (задаётся в compose) |
| `MAXIGRAM_CORS_ALLOWED_ORIGIN` | Origin SPA для CORS | `http://localhost:5173` |
| `MAXIGRAM_MATCHING_MIN_SIMILARITY` | Порог похожести для матча (0..1) | `0.5` |

## Статус

Бэкенд (этапы 0–8) и фронтенд готовы и покрыты тестами (бэкенд — Testcontainers + проверка
модульности; фронт — Vitest и Playwright e2e против живого стека). Стек поднимается в Docker и
проверен живым end-to-end сценарием: подбор → анонимный чат → real-time → деанонимизация.
Полный журнал работ — [docs/plans/completed/20260628-maxigram-v2.md](docs/plans/completed/20260628-maxigram-v2.md).
Backlog (по желанию): отдельный экран списка подписок, e2e на деанонимизацию/уведомления.
