📖 Описание проекта

Bank Card Management System — это backend-приложение для управления банковскими картами.
Пользователи могут регистрироваться, авторизовываться, создавать карты,
просматривать баланс, блокировать карты и выполнять переводы между своими картами.
Администратор имеет полный доступ ко всем картам и пользователям.

Проект реализован в рамках тестового задания.

👥 Участник
Элимдар Эсатов — Backend-разработка (написание кода)

🧩 Функциональность

👤 Пользователи
- Регистрация пользователей
- Авторизация (JWT)
- Просмотр своих карт
- Просмотр баланса карты
- Блокировка своей карты
- Переводы между своими картами
- Просмотр общего баланса всех карт

👑 Администратор
- Создание карт для пользователей
- Просмотр всех карт
- Активация карт
- Удаление карт

💳 Карты
- Создание карты (только ADMIN)
- Просмотр карт (с пагинацией)
- Блокировка карты (USER - свою, ADMIN - любую)
- Активация карты (только ADMIN)
- Удаление карты (только ADMIN)
- Маскирование номера карты (**** **** **** 1234)
- Шифрование номера карты в БД

🔐 Безопасность
- JWT аутентификация
- Ролевая модель (USER / ADMIN)
- Шифрование паролей (BCrypt)
- Шифрование номеров карт (AES)

🛠 Стек технологий
| Технология      | Версия  | Назначение |
|-----------------|---------|------------|
| Java            | 17      | Основной язык |
| Spring Boot     | 3.1.5   | Фреймворк |
| Spring Security | 6.1.5   | Безопасность |
| Spring Data JPA | 3.1.5   | Работа с БД |
| PostgreSQL      | 15      | База данных |
| Liquibase       | 4.20.0  | Миграции |
| JWT             | 0.11.5  | Аутентификация |
| Swagger/OpenAPI | 2.2.0   | Документация API |
| Docker          | 24+     | Контейнеризация |
| Lombok          | 1.18.30 | Генерация кода |

🗂 Структура проекта
src/main/java/com/example/bankcards/
├── BankApplication.java          # Точка входа
├── config/                       # Конфигурации
│   └── SecurityConfig.java       # Безопасность + JWT + CORS
├── controller/                   # REST контроллеры
│   ├── AuthController.java       # Регистрация, логин
│   ├── CardController.java       # Карты, переводы
│   └── AdminController.java      # Админские операции
├── dto/                          # DTO объекты
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   ├── RegisterRequest.java
│   ├── CardDTO.java
│   ├── CreateCardRequest.java
│   └── TransferRequest.java
├── entity/                       # JPA сущности
│   ├── User.java
│   ├── Card.java
│   └── Role.java
├── exception/                    # Обработка ошибок
│   └── GlobalExceptionHandler.java
├── repository/                   # Репозитории
│   ├── UserRepository.java
│   └── CardRepository.java
├── security/                     # Безопасность
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── service/                      # Бизнес-логика
│   ├── AuthService.java
│   ├── CardService.java
│   └── TransferService.java
└── util/                         # Утилиты
├── SecurityUtils.java
├── EncryptionUtil.java
├── CardMaskingUtil.java
└── CardNumberGenerator.java
▶ Запуск проекта

Требования
- Java 17+
- PostgreSQL 15+
- Maven 3.9+
- Docker (опционально)


Приложение будет доступно по адресу:
http://localhost:8080
📑 Swagger (OpenAPI)

Swagger UI доступен по адресу:
http://localhost:8080/swagger-ui.html

🔐 Авторизация в Swagger
1. Зарегистрировать пользователя через POST /api/auth/register
2. Войти через POST /api/auth/login → получить JWT токен
3. Нажать кнопку Authorize
4. Ввести: Bearer <ваш_токен>
5. Нажать Authorize

Сервисы:
PostgreSQL — localhost:5432

PgAdmin — http://localhost:5050 (admin@bank.com / admin)

Приложение — http://localhost:8080

📌 Примечания
Пароли пользователей хранятся в зашифрованном виде (BCrypt)

Номера карт хранятся в зашифрованном виде (AES)

Доступ к защищённым эндпоинтам возможен только с JWT токеном

Администратор создаётся вручную через БД (role = 'ADMIN')

По умолчанию все новые пользователи имеют роль USER

Пагинация реализована для всех GET эндпоинтов

Swagger UI доступен без авторизации
