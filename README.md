# LipariBank Day 09 — Caching & Performance

Benvenuto nel progetto **liparibank-day09-broken**.

Applicazione Spring Boot 3.3.4 (Java 21) con sistema di caching tramite
`@EnableCaching`, `@Cacheable`, `@CachePut`, `@CacheEvict`, Caffeine come provider,
HikariCP per il connection pool, e Micrometer/Actuator per il monitoraggio.

Il progetto contiene **3 bug intenzionali** da trovare e correggere.

---

## Come avviare

```bash
./mvnw spring-boot:run
```

Su Windows:
```cmd
mvnw.cmd spring-boot:run
```

Swagger UI disponibile su: `http://localhost:8080/swagger-ui.html`
H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:liparibankdb`)
Actuator: `http://localhost:8080/actuator`

---

## Curl di test per ogni scenario

### Scenario Missione 1 — @CachePut non aggiorna la cache

```bash
# Leggi una filiale (popola la cache)
curl -s http://localhost:8080/api/v1/branches/1 | jq .

# Aggiorna la filiale
curl -s -X PUT http://localhost:8080/api/v1/branches/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Filiale Palermo Ballarò","city":"Palermo"}' | jq .

# Rileggi la filiale — il nome è ancora quello vecchio nei log?
curl -s http://localhost:8080/api/v1/branches/1 | jq .
# Osserva nei log: viene loggato "Cache miss"? O viene servita la cache?
# E se viene servita, riflette i nuovi dati?
```

### Scenario Missione 2 — Avvio applicazione

```bash
# Prova ad avviare l'applicazione e osserva l'output
./mvnw spring-boot:run
# Osserva l'errore durante la fase di startup
```

### Scenario Missione 3 — @Cacheable silenziosamente ignorato

```bash
# Fai due richieste identiche e osserva i log
curl -s http://localhost:8080/api/v1/branches | jq .
curl -s http://localhost:8080/api/v1/branches | jq .

# Quante volte vedi il messaggio "Cache miss — caricamento filiali attive dal database"?
# Dovrebbe apparire solo la prima volta. Appare anche la seconda?
```

---

## Le 3 Missioni

### Missione 1 — @CachePut non aggiorna la voce in cache

Dopo aver aggiornato una filiale tramite `PUT /api/v1/branches/{id}`, una successiva
`GET /api/v1/branches/{id}` continua a restituire i dati vecchi dalla cache.

**Sintomo:** i log mostrano nessun "Cache miss" sulla GET successiva all'aggiornamento,
ma i dati restituiti sono quelli pre-aggiornamento. Il `@CachePut` non ha sostituito
la voce nella cache con la chiave corretta.

---

### Missione 2 — Errore durante lo startup dell'applicazione

L'applicazione non si avvia correttamente. Nello stack trace di startup è visibile
un errore di tipo `IllegalArgumentException` o simile legato all'iniezione di un valore
di configurazione.

**Sintomo:** `BeanCreationException` durante l'avvio, causata da un componente che
tenta di iniettare una proprietà numerica ma riceve un valore non convertibile.

---

### Missione 3 — Il caching non funziona in nessun profilo attivo

Le annotazioni `@Cacheable`, `@CachePut` e `@CacheEvict` sembrano presenti e corrette,
ma ogni richiesta raggiunge sempre il database. Non c'è nessun errore — il caching
è semplicemente inattivo.

**Sintomo:** nei log di livello TRACE per `org.springframework.cache`, non compare
mai il messaggio di hit della cache. Il messaggio "Cache miss" appare ad ogni
chiamata, anche ripetuta.

---

## Struttura del progetto

```
src/main/java/com/lipari/bank/
├── LipariBankApplication.java
├── branch/
│   ├── model/Branch.java
│   ├── dto/BranchResponse.java
│   ├── dto/BranchUpdateRequest.java
│   ├── BranchRepository.java
│   ├── BranchService.java          ← @Cacheable / @CachePut / @CacheEvict
│   └── BranchController.java
└── shared/
    ├── config/
    │   ├── CacheConfig.java         ← @EnableCaching + Caffeine
    │   ├── OpenApiConfig.java
    │   └── DataInitializer.java
    └── monitor/
        └── HikariPoolMonitor.java   ← @Value + @PostConstruct
src/main/resources/
└── application.yml
```

---

## Bonus Mission — Feature da Implementare (opzionale, ~1 ora)

Una volta risolti i 3 bug, implementa la seguente feature per consolidare i concetti del giorno.

### Endpoint statistiche con cache dedicata e metrica Micrometer

Il sistema gestisce le filiali ma non espone nessun dato aggregato, e non tiene traccia delle disattivazioni con metriche osservabili.

**Cosa implementare:**

Aggiungi un endpoint `GET /api/v1/branches/stats` che restituisce un oggetto JSON con:
- numero totale di filiali
- numero di filiali attive
- numero di filiali inattive
- lista delle città distinte in cui sono presenti filiali attive

Il risultato di questo endpoint deve essere memorizzato in una cache separata chiamata `branchStats`. Quando una filiale viene disattivata tramite `DELETE /api/v1/branches/{id}`, entrambe le cache `branches` e `branchStats` devono essere svuotate.

In `BranchService`, ogni volta che il metodo `deactivate` viene invocato con successo, incrementa un contatore Micrometer con nome `branch.deactivations.total`. Il contatore deve essere consultabile tramite `GET /actuator/metrics/branch.deactivations.total`.

**Criteri di accettazione:**

- `GET /api/v1/branches/stats` restituisce i dati corretti.
- Due chiamate consecutive a `/stats` producono un solo accesso al database (la seconda viene servita dalla cache) — verificabile nei log con il messaggio di cache miss/hit.
- Dopo `DELETE /api/v1/branches/1`, la successiva GET a `/stats` produce un nuovo accesso al database (la cache è stata svuotata).
- `GET /actuator/metrics/branch.deactivations.total` mostra un valore incrementale per ogni disattivazione effettuata.

---

*LipariBank Prompt Bootcamp — Caching & Performance — Day 09*
