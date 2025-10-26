FROM openjdk:17-jdk-slim

WORKDIR /app

# Установка Maven, wget и curl
RUN apt-get update && apt-get install -y maven wget curl && rm -rf /var/lib/apt/lists/*

# Копируем pom.xml и загружаем зависимости
COPY pom.xml .

# Загружаем зависимости
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src src

# Собираем приложение
RUN mvn clean package -DskipTests

# Создаем jar файл
RUN cp target/*.jar app.jar

# Открываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
