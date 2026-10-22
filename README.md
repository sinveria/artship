# ArtShip

Стартовый каркас с отдельными `backend` и `frontend`.

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

## Запуск backend

Откройте терминал в корне проекта:

```bash
cd backend
mvn spring-boot:run
```

Backend запустится на `http://localhost:8080`.

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
