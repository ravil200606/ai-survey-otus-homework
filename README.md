# Мини-анкета

Минимальное full-stack веб-приложение: анкета из 4 вопросов (имя и фамилия, возраст, пол, опыт разработки).

- **Backend** — Java 21 + Spring Boot 3.5.x, REST API (`GET /questions`, `POST /answers`), ответы хранятся только в памяти процесса (без БД);
- **Frontend** — HTML + JavaScript без сборки, раздаётся nginx'ом, который также проксирует запросы к API (без CORS);
- **Запуск** — Docker Compose.

Подробный отчёт о разработке: [REPORT.md](REPORT.md).

## Структура проекта

```text
├── backend/                  # Spring Boot (Java 21, Maven)
│   ├── src/main/java/com/example/questionnaire/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                 # статический HTML/JS/CSS + nginx
│   ├── src/
│   ├── nginx.conf
│   └── Dockerfile
├── docker-compose.yml
└── REPORT.md
```

## API

| Метод | Путь          | Описание |
|-------|---------------|----------|
| GET   | `/questions`  | Список из 4 вопросов (для пола варианты `М`/`Ж`) |
| POST  | `/answers`    | Приём ответов, валидация, хранение в памяти. Успех — `201`, ошибка валидации — `400` + `{"message": "..."}` |

Пример запроса:

```json
{
  "answers": [
    { "questionId": 1, "value": "Иван Иванов" },
    { "questionId": 2, "value": "35" },
    { "questionId": 3, "value": "М" },
    { "questionId": 4, "value": "10" }
  ]
}
```

# Запуск

## Требования

- Docker
- Docker Compose

## Запуск

Из корня проекта выполнить:

```bash
docker compose up --build
```

Сервисы:

- **frontend** — http://localhost:3000 (страница анкеты, прокси на backend);
- **backend** — http://localhost:8080 (открыт для удобства проверки API).

## Использование

После успешного запуска открыть в браузере:

http://localhost:3000

Заполнить форму и нажать «Отправить» — при успехе появится «Спасибо!».

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
