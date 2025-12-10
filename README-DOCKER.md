# Weather Adapter - Docker Deployment Guide

Ten dokument zawiera instrukcje dotyczące uruchamiania aplikacji Weather Adapter przy użyciu Dockera.

## Wymagania wstępne

- Docker (wersja 20.10 lub nowsza)
- Docker Compose (wersja 2.0 lub nowsza)
- Weather API key z [weatherapi.com](https://www.weatherapi.com/)

## Konfiguracja

### 1. Utwórz plik .env

Skopiuj plik `.env.example` do `.env`:

```bash
cp .env.example .env
```

### 2. Skonfiguruj zmienne środowiskowe

Edytuj plik `.env` i dodaj swój Weather API key:

```env
WEATHER_API_KEY=twoj_prawdziwy_klucz_api
```

**Uwaga:** Plik `.env` jest ignorowany przez git i nie powinien być commitowany do repozytorium.

## Uruchamianie aplikacji

### Metoda 1: Użycie Docker Compose (zalecane)

1. Zbuduj i uruchom aplikację:

```bash
docker-compose up -d
```

2. Sprawdź logi:

```bash
docker-compose logs -f weather-adapter
```

3. Zatrzymaj aplikację:

```bash
docker-compose down
```

### Metoda 2: Użycie Docker CLI

1. Zbuduj obraz:

```bash
docker build -t weather-adapter:latest .
```

2. Uruchom kontener:

```bash
docker run -d \
  --name weather-adapter \
  -p 8081:8081 \
  -e WEATHER_API_KEY=twoj_klucz_api \
  weather-adapter:latest
```

3. Zatrzymaj i usuń kontener:

```bash
docker stop weather-adapter
docker rm weather-adapter
```

## Dostępne endpointy

Po uruchomieniu aplikacji dostępne są następujące endpointy:

- **API Documentation (Swagger UI)**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/api-docs
- **Health Check**: http://localhost:8081/actuator/health
- **Weather API**:
  - Aktualna pogoda: `GET http://localhost:8081/api/v1/check-weather?city=<miasto>`
  - Historia pogody: `GET http://localhost:8081/api/v1/check-historical-weather`

## Przykłady użycia

### Sprawdzenie statusu aplikacji

```bash
curl http://localhost:8081/actuator/health
```

### Pobranie pogody dla miasta

```bash
curl "http://localhost:8081/api/v1/check-weather?city=Warsaw"
```

### Pobranie historycznych danych pogodowych

```bash
curl "http://localhost:8081/api/v1/check-historical-weather?city=Krakow&fromDate=2024-01-01&toDate=2024-01-31"
```

## Zarządzanie kontenerami

### Zobacz uruchomione kontenery

```bash
docker ps
```

### Zobacz logi kontенera

```bash
docker logs weather-adapter
docker logs -f weather-adapter  # Follow mode
```

### Wejdź do kontenera (debugging)

```bash
docker exec -it weather-adapter sh
```

### Restart kontenera

```bash
docker-compose restart weather-adapter
```

## Budowanie obrazu

### Proces budowania

Dockerfile używa **multi-stage build**:

1. **Stage 1 (Builder)**:
   - Używa obrazu `maven:3.9-eclipse-temurin-21`
   - Pobiera zależności Maven
   - Kompiluje kod źródłowy
   - **Uruchamia wszystkie testy** (unit + integracyjne)
   - Budowanie się nie powiedzie, jeśli testy failują
   - Tworzy JAR z embedded Tomcat

2. **Stage 2 (Runtime)**:
   - Używa obrazu `eclipse-temurin:21-jre-alpine` (mniejszy rozmiar)
   - Kopiuje zbudowany JAR
   - Konfiguruje non-root user dla bezpieczeństwa
   - Ustawia health check

### Czasy budowania

- Pierwsze budowanie: ~5-10 minut (zależnie od połączenia internetowego)
- Kolejne budowania: ~2-3 minuty (jeśli pom.xml się nie zmienił)

### Testy podczas budowania

**UWAGA:** Build uruchamia wszystkie testy automatycznie. Jeśli jakikolwiek test failuje, budowanie obrazu Docker zostanie przerwane. To gwarantuje jakość kodu w obrazie Docker.

## Troubleshooting

### Problem: Kontener nie startuje

Sprawdź logi:
```bash
docker-compose logs weather-adapter
```

### Problem: Health check failuje

Sprawdź, czy aplikacja odpowiada:
```bash
curl http://localhost:8081/actuator/health
```

### Problem: Błąd połączenia z Weather API

Upewnij się, że:
1. Masz prawidłowy API key w pliku `.env`
2. API key jest aktywny na weatherapi.com
3. Kontener ma dostęp do internetu

### Problem: Port 8081 jest zajęty

Zmień mapowanie portu w `docker-compose.yml`:
```yaml
ports:
  - "8082:8081"  # Użyj portu 8082 zamiast 8081
```

## Optymalizacja

### Cache layers

Dockerfile jest zoptymalizowany pod kątem cache:
- Zależności Maven są cachowane (jeśli pom.xml się nie zmienia)
- Kod źródłowy jest kopiowany oddzielnie

### Rozmiar obrazu

- Builder stage: ~800 MB (nie jest w finalnym obrazie)
- Runtime stage: ~200-250 MB (tylko JRE + aplikacja)

## Bezpieczeństwo

- Aplikacja działa jako non-root user (`spring:spring`)
- Secrets (API key) są externalizowane do zmiennych środowiskowych
- Health check monitoruje stan aplikacji
- Alpine Linux minimalizuje surface attack

## Network

Docker Compose tworzy dedykowaną sieć `weather-network`:
- Typ: bridge
- Pozwala na przyszłą rozbudowę (np. dodanie PostgreSQL)

## Informacje techniczne

- **Java Version**: 21
- **Spring Boot**: 3.5.5
- **Packaging**: JAR (embedded Tomcat)
- **Port**: 8081
- **Health check**: `/actuator/health` (sprawdzany co 30s)
- **Restart policy**: unless-stopped

## Dalsze kroki

Jeśli chcesz dodać PostgreSQL do setup'u, możesz rozszerzyć `docker-compose.yml` o serwis bazy danych.
