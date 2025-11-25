# Sistem de Management al Energiei

Acest proiect este o aplicație web completă, construită pe o arhitectură de microservicii, destinată managementului utilizatorilor și al dispozitivelor de monitorizare a energiei. Aplicația este complet containerizată folosind Docker și orchestrată cu Docker Compose.

## Arhitectură

Sistemul este compus dintr-un frontend (React), un API Gateway (Traefik) și patru microservicii backend distincte (Spring Boot), fiecare cu propria sa bază de date PostgreSQL dedicată, plus un sistem de mesagerie asincronă (RabbitMQ).

* **Frontend**: O aplicație React (CRA) servită printr-un container Nginx.
* **API Gateway**: Traefik gestionează toate cererile primite, acționând ca un reverse proxy. Acesta rutează traficul către serviciul corespunzător și utilizează `AuthenticationService` ca middleware pentru a valida token-urile JWT (Forward Authentication).
* **Backend (Microservicii)**:
    * **AuthenticationService**: Gestionează înregistrarea, autentificarea (login) și generarea/validarea token-urilor JWT.
    * **UserService**: Gestionează datele de profil ale utilizatorilor (nume, adresă, email etc.).
    * **DeviceService**: Gestionează dispozitivele (senzori, contoare) și asocierea acestora cu utilizatorii. Trimite evenimente de sincronizare către `MonitoringService` la crearea sau ștergerea dispozitivelor.
    * **MonitoringService**: Colectează și stochează datele de consum energetic. Ascultă măsurătorile venite de la senzori și evenimentele de sincronizare a dispozitivelor prin RabbitMQ.
* **Baze de Date**: Fiecare microserviciu are propria sa bază de date PostgreSQL izolată, urmând principiul "o bază de date per serviciu".
* **Broker de Mesaje**: RabbitMQ (CloudAMQP) este utilizat pentru comunicarea asincronă între servicii și pentru preluarea datelor de la senzori.

---

## Serviciile Aplicației

| Serviciu | Tehnologie | Port Intern | Port Host (Expus) | Descriere |
| :--- | :--- | :--- | :--- | :--- |
| **Traefik (Gateway)** | Traefik | `80` | `81` | Punctul unic de intrare. Rutează traficul către servicii. |
| **Traefik Dashboard** | Traefik | `8080` | `8081` | Interfață web pentru monitorizarea Traefik. |
| **Frontend** | React / Nginx | `80` | (via Traefik `81`) | Interfața cu utilizatorul (Admin & Client). |
| **AuthenticationService**| Spring Boot | `8083` | (via Traefik `81`) | Gestionează conturile, rolurile și autentificarea (JWT). |
| **UserService** | Spring Boot | `8081` | (via Traefik `81`) | Gestionează datele de profil ale utilizatorilor (CRUD). |
| **DeviceService** | Spring Boot | `8082` | (via Traefik `81`) | Gestionează dispozitivele și maparea lor (CRUD). |
| **MonitoringService** | Spring Boot | `8084` | (via Traefik `81`) | Gestionează datele de consum și istoricul măsurătorilor. |
| **Auth DB** | PostgreSQL | `5432` | `5435` | Baza de date pentru `AuthenticationService`. |
| **User DB** | PostgreSQL | `5432` | `5433` | Baza de date pentru `UserService`. |
| **Device DB** | PostgreSQL | `5432` | `5434` | Baza de date pentru `DeviceService`. |
| **Monitoring DB** | PostgreSQL | `5432` | `5436` | Baza de date pentru `MonitoringService`. |

---

## Sincronizare și Comunicare (RabbitMQ)

Sistemul utilizează RabbitMQ pentru decuplarea serviciilor și procesarea asincronă a datelor. Configurația include următoarele cozi și mecanisme:

### 1. Sincronizarea Dispozitivelor
Pentru a menține consistența datelor, `DeviceService` notifică `MonitoringService` atunci când un dispozitiv este creat sau șters.
* **Exchange**: `sd_sync_exchange` (Tip: Topic)
* **Routing Key**: `device.*` (ex: `device.insert`, `device.delete`)
* **Queue**: `device_sync_queue`
* **Flux**: Când un dispozitiv este adăugat/șters în `DeviceService`, un mesaj este publicat. `MonitoringService` ascultă pe această coadă și actualizează propria tabelă de mapare a dispozitivelor.

### 2. Ingestia Datelor de la Senzori
Senzorii inteligenți (sau simulatorul) trimit datele de consum direct în coada de mesaje.
* **Queue**: `monitoring_queue`
* **Flux**: Mesajele JSON conținând `timestamp`, `device_id` și `consumption` sunt consumate de `MonitoringService`, care le procesează și le salvează în baza de date `db_monitoring`.

---

## Simulator de Date (Device Data Simulator)

Proiectul include un script Python (`DeviceDataSimulator/script.py`) care simulează comportamentul unui senzor inteligent de energie. Acesta generează date de consum realiste bazate pe un profil de încărcare zilnic.

### Cerințe Simulator
* Python 3.x
* Pachete necesare: `pika`, `numpy`

### Utilizare
1. Navigați în folderul `DeviceDataSimulator`.
2. Rulați scriptul:
   ```bash
   python script.py
   ```
Introduceți datele solicitate:
* **Data**: Format `DD-MM-YYYY` (ziua pentru care se generează datele).
* **Device ID**: ID-ul dispozitivului (trebuie să existe deja în baza de date, creat prin interfață).

Scriptul va genera măsurători la intervale de 10 minute pentru întreaga zi (144 de puncte de date), simulând vârfuri de consum dimineața și seara, și le va trimite către coada RabbitMQ (`monitoring_queue`).

## Cerințe Sistem

* Docker
* Docker Compose
* Conexiune la Internet (pentru a accesa instanța CloudAMQP definită în configurație).

## Instrucțiuni de Rulare

### 1. Crearea Rețelei Docker
Acest proiect necesită o rețea Docker externă numită `SD_Network`.

```bash
docker network create SD_Network
```
### 2. Pornirea Aplicației
Din directorul rădăcină al proiectului:

```bash
docker-compose up -d --build
```
### 3. Oprirea Aplicației
```bash
docker-compose down
```

## Accesarea Aplicației

### Interfețe Principale
* **Aplicația Web (Frontend)**: [http://localhost:81](http://localhost:81)
* **Dashboard Traefik**: [http://localhost:8081](http://localhost:8081)

### Documentație API (Swagger UI)
* **Authentication Service**: [http://localhost:81/api/auth/swagger-ui.html](http://localhost:81/api/auth/swagger-ui.html)
* **User Service**: [http://localhost:81/api/user/swagger-ui.html](http://localhost:81/api/user/swagger-ui.html)
* **Device Service**: [http://localhost:81/api/device/swagger-ui.html](http://localhost:81/api/device/swagger-ui.html)
* **Monitoring Service**: Accesibil la endpoint-urile definite (nu este expus explicit Swagger în gateway implicit, verificați configurarea Traefik).

### Acces Baze de Date (Localhost)
* **Auth DB**: `localhost:5435`
* **User DB**: `localhost:5433`
* **Device DB**: `localhost:5434`
* **Monitoring DB**: `localhost:5436`