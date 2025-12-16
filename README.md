# Sistem de Management al Energiei

Acest proiect este o aplicație web completă, construită pe o arhitectură de microservicii, destinată managementului utilizatorilor și al dispozitivelor de monitorizare a energiei. Aplicația este complet containerizată folosind Docker și orchestrată cu Docker Compose.

## Arhitectură

Sistemul este compus dintr-un frontend (React), un API Gateway (Traefik) și șase microservicii backend distincte (Spring Boot), fiecare cu propria sa bază de date PostgreSQL dedicată (unde este cazul), plus un sistem de mesagerie asincronă (RabbitMQ).

* **Frontend**: O aplicație React (CRA) servită printr-un container Nginx.
* **API Gateway**: Traefik gestionează toate cererile primite, acționând ca un reverse proxy. Acesta rutează traficul către serviciul corespunzător și utilizează `AuthenticationService` ca middleware pentru a valida token-urile JWT (Forward Authentication).
* **Backend (Microservicii)**:
    * **AuthenticationService**: Gestionează înregistrarea, autentificarea (login) și generarea/validarea token-urilor JWT.
    * **UserService**: Gestionează datele de profil ale utilizatorilor (nume, adresă, email etc.).
    * **DeviceService**: Gestionează dispozitivele (senzori, contoare) și asocierea acestora cu utilizatorii. Trimite evenimente de sincronizare către `MonitoringService` la crearea sau ștergerea dispozitivelor.
    * **MonitoringService**: Colectează și stochează datele de consum energetic. Ascultă măsurătorile venite de la senzori și evenimentele de sincronizare a dispozitivelor prin RabbitMQ. Scalabil orizontal cu 4 instanțe.
    * **LoadBalancingService**: Distribuie mesajele de monitorizare de la simulator către cele 4 instanțe de MonitoringService pe baza ID-ului dispozitivului (hash-based routing).
    * **ChatService**: Oferă suport prin chat pentru utilizatori prin:
        - **Chatbot simplu** bazat pe reguli pentru întrebări frecvente
        - **Integrare LLM** (Groq API) pentru răspunsuri complexe
        - **Chat cu administrator** în timp real prin WebSocket
        - **Notificări automate** pentru depășirea limitelor de consum
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
| **MonitoringService** | Spring Boot | `8084` | (via Traefik `81`) | Gestionează datele de consum și istoricul măsurătorilor (4 instanțe). |
| **LoadBalancingService** | Spring Boot | `8086` | `8096` | Distribuie mesajele către instanțele MonitoringService. |
| **ChatService** | Spring Boot | `8085` | (via Traefik `81`) | Oferă suport prin chat și notificări în timp real. |
| **Auth DB** | PostgreSQL | `5432` | `5435` | Baza de date pentru `AuthenticationService`. |
| **User DB** | PostgreSQL | `5432` | `5433` | Baza de date pentru `UserService`. |
| **Device DB** | PostgreSQL | `5432` | `5434` | Baza de date pentru `DeviceService`. |
| **Monitoring DB** | PostgreSQL | `5432` | `5436` | Baza de date pentru `MonitoringService`. |

---

## Sincronizare și Comunicare (RabbitMQ)

Sistemul utilizează RabbitMQ pentru decuplarea serviciilor și procesarea asincronă a datelor. Configurația include următoarele cozi și mecanisme:

### 1. Sincronizarea Dispozitivelor
Pentru a menține consistența datelor, `DeviceService` notifică `MonitoringService` atunci când un dispozitiv este creat, actualizat sau șters.
* **Exchange**: `sd_sync_exchange` (Tip: Topic)
* **Routing Key**: `device.*` (ex: `device.insert`, `device.update`, `device.delete`)
* **Queue**: `monitoring_sync_queue`
* **Flux**: Când un dispozitiv este adăugat/modificat/șters în `DeviceService`, un mesaj este publicat. `MonitoringService` ascultă pe această coadă și actualizează propria tabelă de mapare a dispozitivelor.

### 2. Sincronizarea Utilizatorilor
`AuthenticationService` notifică `DeviceService` și `UserService` la crearea sau ștergerea utilizatorilor.
* **Exchange**: `sd_sync_exchange` (Tip: Topic)
* **Routing Keys**: `user.insert`, `user.delete`
* **Queues**: `device_sync_queue`, `user_sync_queue`
* **Flux**: Când un utilizator este creat/șters, mesaje sunt publicate pentru sincronizarea datelor între servicii.

### 3. Ingestia Datelor de la Senzori (cu Load Balancing)
Senzorii inteligenți (sau simulatorul) trimit datele de consum în coada principală, care este procesată de `LoadBalancingService`.
* **Queue Principală**: `monitoring_queue`
* **Queues Secundare**: `monitoring_queue_0`, `monitoring_queue_1`, `monitoring_queue_2`, `monitoring_queue_3`
* **Flux**: 
  1. Simulatorul trimite date către `monitoring_queue`
  2. `LoadBalancingService` preia mesajele și le distribuie către cele 4 cozi secundare pe baza formulei: `deviceId % 4`
  3. Fiecare instanță de `MonitoringService` ascultă pe propria coadă dedicată
  4. Acest mecanism permite scalabilitate orizontală și distribuție echilibrată a sarcinii

### 4. Notificări pentru Depășirea Consumului
Când consumul unui dispozitiv depășește limita configurată, `MonitoringService` trimite notificări către `ChatService`.
* **Exchange**: `sd_sync_exchange` (Tip: Topic)
* **Routing Key**: `notification.exceeded`
* **Queue**: `chat_alerts_queue`
* **Flux**: `MonitoringService` detectează depășirea limitei și publică o alertă. `ChatService` ascultă pe această coadă și trimite notificări în timp real utilizatorilor prin WebSocket.

---

## Serviciul de Chat

`ChatService` oferă o interfață completă de comunicare și suport:

### Funcționalități

1. **Chatbot Bazat pe Reguli**
   - Răspunde la întrebări frecvente despre consum, facturi, programe
   - Execuție rapidă, fără apel extern

2. **Integrare LLM (Groq API)**
   - Pentru întrebări complexe care nu se potrivesc regulilor predefinite
   - Model: `openai/gpt-oss-120b`
   - Răspunsuri contextualizate despre energie și sustenabilitate

3. **Chat în Timp Real cu Administrator**
   - Utilizatorii pot comunica direct cu administratorii
   - Implementat prin WebSocket (STOMP/SockJS)
   - Administratorii pot gestiona conversații multiple simultan

4. **Notificări Automate**
   - Alertează utilizatorii când consumul depășește limita configurată
   - Include detalii: device ID, consum actual, limită, timestamp
   - Notificările apar în fereastra de chat

### Configurare

Pentru funcționalitatea LLM, este necesar să configurați cheia API Groq:

```bash
export LLM_API_KEY="your_groq_api_key_here"
```

Sau adăugați în fișierul `.env` din rădăcina proiectului:
```
LLM_API_KEY=your_groq_api_key_here
```

---

## Simulator de Date (Device Data Simulator)

Proiectul include un script Python (`DeviceDataSimulator/script.py`) care simulează comportamentul unui senzor inteligent de energie. Acesta generează date de consum realiste bazate pe un profil de încărcare zilnic.

### Cerințe Simulator
* Python 3.x
* Pachete necesare: `pika`, `numpy`

### Instalare Dependențe
```bash
cd DeviceDataSimulator
pip install pika numpy
```

### Utilizare
1. Navigați în folderul `DeviceDataSimulator`:
   ```bash
   cd DeviceDataSimulator
   ```

2. Rulați scriptul:
   ```bash
   python script.py
   ```

3. Introduceți datele solicitate:
   * **Data**: Format `DD-MM-YYYY` (ziua pentru care se generează datele)
   * **Device ID**: ID-ul dispozitivului (trebuie să existe deja în baza de date, creat prin interfață)

Scriptul va genera măsurători la intervale de 10 minute pentru întreaga zi (144 de puncte de date), simulând vârfuri de consum dimineața și seara, și le va trimite către coada RabbitMQ (`monitoring_queue`).

### Profil de Consum Simulat
Scriptul folosește un profil realist de consum zilnic:
* **00:00-07:00**: Consum minim (0.3 kW - aparate în standby)
* **07:00-09:00**: Vârf dimineață (1.5 kW - duș, cafea, pregătire)
* **09:00-17:00**: Consum moderat (0.8-1.0 kW - activități zilnice)
* **17:00-21:00**: Vârf seară (2.2 kW - gătit, iluminat, divertisment)
* **21:00-24:00**: Scădere treptată (1.8-0.5 kW)

Fiecare măsurătoare include variații aleatoare de ±15% pentru realism.

---

## Cerințe Sistem

* **Docker** (versiunea 20.10 sau mai recentă)
* **Docker Compose** (versiunea 2.0 sau mai recentă)
* **Python 3.x** (pentru simulator)
* **Conexiune la Internet** (pentru a accesa instanța CloudAMQP și Groq API)

---

## Instrucțiuni de Rulare

### 1. Crearea Rețelei Docker
Acest proiect necesită o rețea Docker externă numită `SD_Network`.

```bash
docker network create SD_Network
```

### 2. Configurarea Variabilelor de Mediu
Creați un fișier `.env` în directorul rădăcină cu următorul conținut:

```env
LLM_API_KEY=your_groq_api_key_here
```

**Notă**: Puteți obține o cheie API gratuită de la [Groq Cloud](https://console.groq.com/).

### 3. Pornirea Aplicației
Din directorul rădăcină al proiectului:

```bash
docker-compose up -d --build
```

Acest comandă va:
- Construi toate imaginile Docker
- Porni toate containerele în background
- Crea bazele de date necesare
- Configura rețeaua și rutarea Traefik

### 4. Verificarea Stării Serviciilor
```bash
docker-compose ps
```

Toate serviciile ar trebui să fie în starea "Up".

### 5. Oprirea Aplicației
```bash
docker-compose down
```

Pentru a șterge și volumele (baze de date):
```bash
docker-compose down -v
```

---

## Accesarea Aplicației

### Interfețe Principale
* **Aplicația Web (Frontend)**: [http://localhost:81](http://localhost:81)
* **Dashboard Traefik**: [http://localhost:8081](http://localhost:8081)

### Conturi Implicite
La prima rulare, sistemul creează automat un cont de administrator:
* **Username**: `admin`
* **Password**: `admin`

**Recomandare**: Schimbați parola după prima autentificare!

### Documentație API (Swagger UI)
* **Authentication Service**: [http://localhost:81/api/auth/swagger-ui.html](http://localhost:81/api/auth/swagger-ui.html)
* **User Service**: [http://localhost:81/api/user/swagger-ui.html](http://localhost:81/api/user/swagger-ui.html)
* **Device Service**: [http://localhost:81/api/device/swagger-ui.html](http://localhost:81/api/device/swagger-ui.html)
* **Monitoring Service**: [http://localhost:81/api/monitoring/swagger-ui.html](http://localhost:81/api/monitoring/swagger-ui.html)

### Acces Baze de Date (Localhost)
Puteți conecta clientul PostgreSQL favorit la:
* **Auth DB**: `localhost:5435` (user: `postgres`, password: `root`, db: `authentication_service_db`)
* **User DB**: `localhost:5433` (user: `postgres`, password: `root`, db: `user_service_db`)
* **Device DB**: `localhost:5434` (user: `postgres`, password: `root`, db: `device_service_db`)
* **Monitoring DB**: `5436` (user: `postgres`, password: `root`, db: `monitoring_service_db`)

---

## Utilizare

### Pentru Administratori

1. **Autentificare**: Logați-vă cu contul de admin
2. **Gestionare Utilizatori**:
   - Creați conturi noi (USER sau ADMIN)
   - Editați informațiile utilizatorilor existenți
   - Ștergeți utilizatori (împreună cu device-urile asociate)

3. **Gestionare Dispozitive**:
   - Adăugați dispozitive noi și alocați-le utilizatorilor
   - Configurați limita maximă de consum pentru fiecare device
   - Actualizați sau ștergeți dispozitive existente

4. **Monitorizare Proprie**:
   - Administratorii pot avea propriile dispozitive
   - Vizualizare grafică a consumului istoric

5. **Suport Chat**:
   - Răspundeți la întrebările utilizatorilor în timp real
   - Gestionați conversații multiple simultan
   - Vedeți notificările de depășire a consumului

### Pentru Utilizatori (Clienți)

1. **Înregistrare și Autentificare**:
   - Creați un cont nou direct din pagina de login
   - Sau folosiți credențialele primite de la administrator

2. **Vizualizare Dispozitive**:
   - Vedeți toate dispozitivele alocate contului dvs.
   - Verificați limitele de consum configurate

3. **Analiză Consum**:
   - Selectați un dispozitiv și o dată
   - Vizualizați consumul orar într-un grafic interactiv
   - Identificați perioadele cu consum ridicat

4. **Suport Chat**:
   - **Chatbot**: Pentru întrebări rapide despre consum, facturi, etc.
   - **LLM**: Pentru răspunsuri detaliate și contextualizate
   - **Administrator**: Pentru probleme complexe sau solicitări speciale
   - Primiți notificări automate când consumul depășește limita

---

## Arhitectura Tehnică Detaliată

### Flow de Autentificare și Autorizare

1. **Autentificare**:
   - Utilizatorul trimite username și password către `/api/auth/login`
   - `AuthenticationService` validează credențialele și generează un JWT
   - Frontend-ul stochează token-ul și îl trimite în header-ul `app-auth`

2. **Autorizare**:
   - Traefik interceptează toate cererile către API
   - Middleware-ul Forward Auth trimite token-ul către `/validate`
   - `AuthenticationService` verifică token-ul și rolul utilizatorului
   - `PolicyService` evaluează dacă utilizatorul are acces la resursa solicitată
   - Cererea este permisă sau respinsă (403 Forbidden)

### Politici de Acces (PolicyService)

Regulile de acces sunt definite în `PolicyService` și evaluează combinația:
- **Metoda HTTP** (GET, POST, PUT, DELETE, etc.)
- **Path-ul URI** (cu suport pentru wildcard-uri)
- **Rolul utilizatorului** (ADMIN, USER)

Exemple de reguli:
```java
// Doar ADMIN poate vedea toate device-urile
new Rule("GET", "/api/device", Set.of(Roles.ADMIN))

// USER și ADMIN pot vedea device-urile unui utilizator specific
new Rule("GET", "/api/device/*", Set.of(Roles.USER, Roles.ADMIN))

// Doar ADMIN poate crea/actualiza/șterge
new Rule("POST", "/api/device", Set.of(Roles.ADMIN))
new Rule("PUT", "/api/device/*", Set.of(Roles.ADMIN))
new Rule("DELETE", "/api/device/*", Set.of(Roles.ADMIN))
```

### Scalabilitate MonitoringService

Sistemul suportă scalare orizontală pentru procesarea datelor de monitorizare:

1. **Distribuție**: `LoadBalancingService` distribuie mesajele pe baza formulei `deviceId % 4`
2. **Procesare Paralelă**: Fiecare instanță procesează independent propria coadă
3. **Bază de Date Partajată**: Toate instanțele scriu în aceeași bază de date PostgreSQL
4. **Agregare**: Consumul este agregat pe ore pentru eficiență
5. **Detectare Depășiri**: Fiecare instanță verifică independent limitele și trimite notificări

---

## Troubleshooting

### Containerele nu pornesc
```bash
# Verificați log-urile
docker-compose logs <service_name>

# Exemple:
docker-compose logs authenticationservice
docker-compose logs frontend
```

### Erori de conectare la baza de date
- Asigurați-vă că toate containerele PostgreSQL sunt pornite
- Verificați că porturile nu sunt deja ocupate pe sistemul host
- Așteptați câteva secunde după pornire pentru inițializarea bazelor de date

### Chatbot-ul nu răspunde
- Verificați că `ChatService` este pornit: `docker-compose ps chatservice`
- Verificați conexiunea WebSocket în consolă browser (F12)
- Asigurați-vă că `LLM_API_KEY` este configurat corect pentru funcționalitatea LLM

### Simulatorul nu trimite date
- Verificați conexiunea la RabbitMQ (credențiale în `script.py`)
- Asigurați-vă că device ID-ul există în baza de date
- Verificați că `LoadBalancingService` și `MonitoringService` sunt pornite

### Datele nu apar în grafice
- Verificați că există date pentru data selectată
- Datele sunt agregate pe ore - măsurătorile la minute se combină
- Verificați log-urile `MonitoringService` pentru erori de procesare

---

## Tehnologii Utilizate

### Backend
- **Java 21** - Limbaj de programare
- **Spring Boot 3.5.x** - Framework pentru microservicii
- **Spring Security** - Autentificare și autorizare
- **JWT (jjwt 0.11.5)** - Token-uri de autentificare
- **Spring Data JPA** - Persistență și ORM
- **MapStruct** - Mapping între entități și DTO-uri
- **Spring AMQP** - Integrare RabbitMQ
- **Springdoc OpenAPI** - Documentație API automată
- **WebSocket (STOMP/SockJS)** - Comunicare real-time (ChatService)

### Frontend
- **React 19.2.0** - Librărie UI
- **React Router 7.9.5** - Rutare în aplicație
- **Axios 1.13.1** - Client HTTP
- **Recharts 3.4.1** - Grafice interactive
- **SockJS + STOMP** - Client WebSocket pentru chat
- **jwt-decode 4.0.0** - Decodare token-uri JWT

### Infrastructure
- **Traefik 3.0** - API Gateway și reverse proxy
- **Docker & Docker Compose** - Containerizare și orchestrare
- **PostgreSQL 17** - Bază de date relațională
- **RabbitMQ (CloudAMQP)** - Message broker
- **Nginx (Alpine)** - Server web pentru frontend

### External APIs
- **Groq Cloud API** - LLM pentru chat inteligent (model: `openai/gpt-oss-120b`)