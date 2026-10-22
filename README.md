# ArtShip — базовая структура проекта

Стартовый каркас с отдельными `backend` и `frontend`. В проекте пока нет бизнес-логики и базы данных.

## Требования

- Java 17
- Maven 3.6+
- Node.js 22.20.0
- npm 9.0.0+

Проверить установленные версии:

```bash
java -version
mvn -version
node -v
npm -v
```

## Структура проекта

```text
artship/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/artship/
│   │   │   │   ├── ArtShipApplication.java
│   │   │   │   └── health/HealthController.java
│   │   │   └── resources/application.properties
│   │   ├── test/java/com/example/artship/
│   │   │   └── ArtShipApplicationTests.java
│   └── pom.xml
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── api/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── contexts/
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── index.css
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── .gitignore
└── README.md
```

## Запуск backend

Откройте терминал в корне проекта:

```bash
cd backend
mvn spring-boot:run
```

Backend запустится на `http://localhost:8080`.

### Проверка health checks

В другом терминале выполните:

```bash
curl -i http://localhost:8080/actuator/health
curl -i http://localhost:8080/api/health
```

Оба запроса должны вернуть HTTP `200 OK`. Для Actuator ожидается JSON с `"status":"UP"`, а для `/api/health` — JSON с `"status":"UP"`.

В Windows PowerShell вместо `curl` можно открыть эти адреса в браузере либо выполнить:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/api/health
```

Проверка тестов:

```bash
mvn test
```

## Запуск frontend

Откройте **новый терминал** в корне проекта:

```bash
cd frontend
npm install
npm run dev
```

Vite выведет локальный адрес, обычно `http://localhost:5173`. Откройте его в браузере.

Frontend использует переменную `VITE_API_BASE_URL` для адреса backend. По умолчанию запросы идут на `http://localhost:8080`. При необходимости создайте `frontend/.env.local`:

```dotenv
VITE_API_BASE_URL=http://localhost:8080
```

## Как создать репозиторий на GitHub и загрузить проект

1. На GitHub нажмите **New repository**.
2. Укажите имя, например `artship`, выберите видимость репозитория.
3. Создайте пустой репозиторий. Если проект уже содержит README, `.gitignore` или лицензию, не добавляйте их на GitHub на этом шаге — они уже есть в локальном проекте.
4. Распакуйте архив проекта в папку `artship` (или переименуйте папку `artship-starter` в `artship`).
5. Откройте терминал **в корне проекта**, где лежит этот `README.md`, и выполните команды ниже. Замените URL на адрес своего репозитория.

```bash
git init
git add .
git commit -m "Initial backend and frontend structure"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/artship.git
git push -u origin main
```

Если Git сообщает, что `origin` уже существует, проверьте адрес:

```bash
git remote -v
```

При необходимости исправьте его:

```bash
git remote set-url origin https://github.com/YOUR_USERNAME/artship.git
git push -u origin main
```

GitHub может запросить авторизацию. Для HTTPS используйте вход через браузер/Git Credential Manager или Personal Access Token — пароль от аккаунта GitHub для Git-операций не используется.

## Как клонировать репозиторий на другой компьютер

```bash
git clone https://github.com/YOUR_USERNAME/artship.git
cd artship
```

После клонирования запускайте backend и frontend по инструкциям выше.

## Важно

- Backend и frontend запускаются в отдельных терминалах.
- Для базового health check база данных не требуется.
- Не коммитьте секреты, пароли и реальные `.env`-файлы.
