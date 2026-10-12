# Дополнительные данные о проекте
## AI-setup
Для работы использовалась среда разработки IntelliJ Idea 2026.3, плагин Kilo Code. Также использована внутренняя 
корпоративная модель "copilot-code-large" на базе Qwen. Данная модель выбрана т.к. она предназначена именно для
кодинга и отделов разработки нашей компании. Плагин изначально использовался Continue, но он устарел, т.к. больше предназначен
для использования в режиме ассистента и чата нежели полноценного интерфейса с агентом. Использую данный инструмент в рабочих целях, 
он полностью меня устраивает и позволяет выполнять все рабочие задачи. В процессе работы возникла проблема на уровне
разрешений в системе, агенту не хватало прав. Пришлось выдать права на группу докера чтобы он смог завершить процесс
разработки.

# REPORT.md — Мини-анкета

## 11.1. Цель проекта

Разработать минимальное full-stack веб-приложение «Мини-анкета»: backend на Java с REST API
(`GET /questions`, `POST /answers`), frontend на HTML + JavaScript, хранение ответов только
в памяти процесса, запуск всего приложения локально через Docker Compose.

## 11.2. Требования

Функциональные:

- `GET /questions` — возвращает 4 фиксированных вопроса (имя/фамилия, возраст, пол (`М`/`Ж`), опыт разработки);
- `POST /answers` — принимает ответы в JSON, хранит их только в памяти (после перезапуска ответы исчезают);
- базовая валидация: имя/фамилия не пустые, возраст — число, пол — `М` или `Ж`, опыт — число;
  при ошибке — HTTP 4xx и понятное сообщение;
- frontend: загружает вопросы через API, отображает форму (пол — выбор из `М`/`Ж`),
  отправляет ответы, после успешной отправки показывает «Спасибо!», при ошибке — понятное сообщение;
- без БД, Redis, авторизации, регистрации и других избыточных технологий.

Технические:

- backend на Java (предпочтительно Spring Boot);
- frontend на HTML + JavaScript;
- Dockerfile для backend и frontend, `docker-compose.yml` для запуска всего приложения;
- `REPORT.md` с протоколом разработки и инструкцией по запуску в конце файла.

## 11.3. Принятые решения

| Решение | Выбор | Обоснование |
|---|---|---|
| Версия Java | 21 (LTS) | Актуальный LTS, поддерживается Spring Boot 3.x; Docker-образ `eclipse-temurin:21` |
| Backend framework | Spring Boot 3.5.x, стартер `spring-boot-starter-web` | Требование предпочитает распространённый фреймворк; для REST API достаточно только Web-стартера |
| Build tool | Maven | Стандарт для Spring Boot, простая сборка в multi-stage Dockerfile |
| Frontend | HTML + чистый JavaScript (без сборки) | Проект из одной страницы; TypeScript добавил бы шаг сборки без пользы для такого объёма |
| Node.js | не используется | Frontend статический, нет ни сборки, ни dev-сервера Node |
| Frontend-сервер в Docker | `nginx:alpine` | Раздаёт статику и проксирует `/questions`, `/answers` на backend — заодно решает CORS (запросы идут на тот же origin) |
| Порты | frontend: **3000** → 80 (nginx), backend: **8080** → 8080 | 3000 — привычный порт для frontend, 8080 — стандартный порт Spring Boot (открыт наружу только для удобства проверки через curl) |
| Взаимодействие frontend/backend | Относительные URL `/questions` и `/answers`, проксирование через nginx | Нет захардкоженных URL, нет CORS |
| Хранение ответов в памяти | `List<Answer>` (`ArrayList`) в Spring-бине `AnswerStore` | Требование допускает стандартные структуры; простейший вариант |
| Формат JSON | `GET /questions` → массив объектов `{id, text, type, options?}`; `POST /answers` → `{"answers": [{"questionId": ..., "value": "..."}]}` | Формат из ТЗ; ответы передаются строками, backend приводит типы при валидации |
| CORS | Не настраивается в Spring — не нужен благодаря nginx-проксированию | Меньше конфигурации; CORS был бы нужен только при обращении с другого origin |

## 11.4. Структура проекта

```text
. (корень репозитория)
├── backend/
│   ├── src/main/java/com/example/questionnaire/
│   │   ├── QuestionnaireApplication.java   # точка входа Spring Boot
│   │   ├── Question.java                   # модель вопроса
│   │   ├── QuestionController.java         # GET /questions
│   │   ├── Answer.java                     # модель одного ответа
│   │   ├── AnswersRequest.java             # тело запроса POST /answers
│   │   ├── AnswerStore.java                # хранилище ответов в памяти (ArrayList)
│   │   ├── AnswerController.java           # POST /answers + валидация
│   │   └── ApiExceptionHandler.java        # единый формат ошибок
│   ├── src/main/resources/application.properties
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── index.html
│   │   ├── app.js
│   │   └── styles.css
│   ├── nginx.conf
│   └── Dockerfile
├── docker-compose.yml
├── REPORT.md
└── .gitignore
```

## 11.5. Ход разработки

### Шаг 1 — Анализ требований и фиксация решений

Проанализированы требования, выбран стек и структура проекта, решения зафиксированы
в разделе 11.3 настоящего файла. Данный файл создаётся в начале работы и дополняется
по ходу разработки.

### Шаг 2 — Реализация backend

Создан Maven-проект Spring Boot 3.5.6 (Java 21), единственная зависимость — `spring-boot-starter-web`.

Добавлены классы:

- `QuestionnaireApplication` — точка входа;
- `Question` — record-модель вопроса (id, text, type, options);
- `QuestionController` — `GET /questions`, 4 фиксированных вопроса, для пола варианты `М`/`Ж`;
- `Answer`, `AnswersRequest` — модели запроса `POST /answers`;
- `AnswerStore` — Spring-бин с `ArrayList<Answer>` (хранилище в памяти, синхронизированный доступ);
- `AnswerController` — `POST /answers` с валидацией (непустые имя/фамилия из двух слов,
  возраст — число 1..150, пол — `М`/`Ж`, опыт — число 0..100, все 4 вопроса присутствуют,
  без дублей); при успехе — HTTP 201;
- `ValidationException` + `ApiExceptionHandler` — ошибки возвращаются как HTTP 400
  с JSON `{"message": "..."}`; некорректный JSON — тоже 400; прочее — 500.

Проект скомпилирован локально (`mvn package`) без ошибок.

### Шаг 3 — Реализация frontend

Созданы статические файлы `frontend/src/`:

- `index.html` — каркас страницы (карточка, контейнеры для формы/ошибки/«Спасибо!»);
- `app.js` — при загрузке вызывает `GET /questions`, строит форму (text → input,
  number → input type=number, select → выпадающий список `М`/`Ж`), по submit собирает
  ответы и отправляет `POST /answers`; при успехе прячет форму и показывает «Спасибо!»,
  при ошибке выводит сообщение из ответа API;
- `styles.css` — минимальное оформление без UI-фреймворков.

«Спасибо!» изначально скрыто и показывается только после успешного ответа сервера.

### Шаг 4 — Docker

Созданы:

- `backend/Dockerfile` — multi-stage: `maven:3.9-eclipse-temurin-21` собирает jar,
  `eclipse-temurin:21-jre` запускает;
- `frontend/Dockerfile` — `nginx:alpine`, раздаёт статику, `nginx.conf` проксирует
  `/questions` и `/answers` на `http://backend:8080` (решение CORS за счёт единого origin);
- `docker-compose.yml` — сервисы `backend` (8080:8080) и `frontend` (3000:80).

### Шаг 5 — Сборка и запуск Docker

Первая попытка `docker compose up --build` завершилась ошибкой:
`target backend: failed to read dockerfile: open Dockerfile: no such file or directory`.

Причина: файл `backend/Dockerfile` не был создан при генерации проекта.
Решение: Dockerfile создан, сборка повторена.

Вторая проблема — при сборке backend внутри контейнера Maven не мог разрешить зависимости:
`Non-resolvable parent POM ... Temporary failure in name resolution`.

Причина: DNS-сервер корпоративной сети (nameserver из `resolv.conf` хоста) недоступен
из docker-bridge-сети, поэтому build-контейнер не имел доступа к repo.maven.apache.org.
Решение: для сборки backend в `docker-compose.yml` задан `build.network: host`
(сборка использует сетевой стек хоста, где DNS работает). На runtime это не влияет —
контейнеры общаются между собой через внутреннюю сеть compose.

Третья проблема — при создании compose-сети: `all predefined address pools have been
fully subnetted`.

Причина: VPN-туннель хоста (`cscotun0`) маршрутизирует все стандартные приватные
диапазоны (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16), и Docker не мог выделить подсеть
из своих пулов по умолчанию. Решение: в `docker-compose.yml` для сети по умолчанию
явно задана подсеть `198.18.0.0/24` (диапазон вне VPN-маршрутов, не пересекается
с локальной сетью).

Также отмечена нестабильность доступа к registry Docker Hub на хосте
(`TLS handshake timeout` при `docker pull`) — устраняется повторной попыткой;
после успешного скачивания образов проблема на сборку не влияет.

## 11.6. Тестирование

Выполненные проверки:

1. **Сборка backend** — `mvn package` локально и в Docker (multi-stage) — успешно.
2. **Сборка frontend** — образ `nginx:alpine` со статикой собирается — успешно.
3. **Запуск Docker Compose** — `docker compose up -d`, оба контейнера в статусе `Up`:
   - backend: `0.0.0.0:8080->8080`;
   - frontend: `0.0.0.0:3000->80`.
4. **`GET /questions`** — через frontend (порт 3000) и напрямую через backend (порт 8080):
   HTTP 200, возвращаются ровно 4 вопроса, для пола варианты `["М","Ж"]`.
5. **Страница frontend** — `GET http://localhost:3000/` возвращает `index.html` (HTTP 200).
6. **`POST /answers` через frontend-прокси:**
   - корректные данные → **HTTP 201**;
   - пол `X` → HTTP 400, сообщение `Пол должен иметь значение «М» или «Ж».`;
   - возраст `abc` → HTTP 400, сообщение `Возраст должен быть числом от 1 до 150.`;
   - пустое имя → HTTP 400, сообщение `Не указано значение для вопроса id=1.`;
   - некорректный JSON → HTTP 400, сообщение `Некорректный формат JSON.`
     (проверено ранее локально, тем же кодом).
7. **Хранение в памяти** — в compose-стеке нет БД/Redis; хранилище — `ArrayList`
   внутри JVM (`AnswerStore.java`). Контейнер backend перезапущен (`docker compose restart
   backend`) — приложение продолжает работать (HTTP 200), накопленные ответы при этом
   теряются, внешнего хранилища нет.
8. **Полный сценарий в браузере** (страница → форма → отправка → «Спасибо!») —
   выполняется вручную пользователем на http://localhost:3000; серверная часть
   сценария (загрузка вопросов, приём ответов, 201) проверена curl-запросами выше.

## 11.7. Обнаруженные проблемы

| # | Проблема | Причина | Решение | Результат |
|---|---|---|---|---|
| 1 | `docker compose up --build` — `failed to read dockerfile` для backend | Файл `backend/Dockerfile` не был создан при генерации | Создан `backend/Dockerfile` | Сборка прошла |
| 2 | Maven в build-контейнере: `Temporary failure in name resolution` | DNS корпоративной сети недоступен из docker-bridge | `build.network: host` для сервиса backend в `docker-compose.yml` | Сборка в Docker прошла |
| 3 | `all predefined address pools have been fully subnetted` при создании сети | VPN на хосте маршрутизирует все стандартные приватные диапазоны | Явная подсеть `198.18.0.0/24` для compose-сети | Сеть создана, контейнеры запущены |
| 4 | Нестабильный `docker pull` (`TLS handshake timeout`) | Ограничения корпоративной сети | Повторные попытки скачивания | Образы скачаны |

# Запуск приложения

## Требования

- Docker
- Docker Compose

## Запуск

Из корня проекта выполнить:

```bash
docker compose up --build
```

Сервисы:

- **frontend** — http://localhost:3000 (nginx: статика + прокси `/questions`, `/answers` на backend);
- **backend** — http://localhost:8080 (Spring Boot, открыт наружу только для удобства проверки API).

## Использование

После успешного запуска открыть в браузере:

http://localhost:3000

Проверка API через curl (опционально):

```bash
curl http://localhost:3000/questions

curl -X POST http://localhost:3000/answers \
  -H 'Content-Type: application/json' \
  -d '{"answers":[{"questionId":1,"value":"Иван Иванов"},{"questionId":2,"value":"35"},{"questionId":3,"value":"М"},{"questionId":4,"value":"10"}]}'
```

## Остановка

```bash
docker compose down
```
