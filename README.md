# BurgerBanditten

Udviklet af Gruppe 3 - Frederik Ullersted, Isak Alishiri, Nikolaj Pedersen & Sebastian Vijeyaratnam

Dette projekt er en webapplikation til administration og bestilling af burgere, hvor kunder kan se menuen og afgive bestillinger, og administratorer kan administrere produkter, ingredienser og ordrer. Applikationen er bygget i Java Spring Boot med en JavaScript frontend og MySQL som database.

## Kom i gang

For at køre projektet lokalt skal du:

1. Starte MySQL-databasen via Docker med `docker-compose up -d`
2. Udfylde de nødvendige miljøvariabler i IntelliJ (SENDGRID_API_KEY)
3. Køre applikationen med `mvn spring-boot:run`

## Funktioner

* Brugersystem med roller og tilladelser (ADMIN og CUSTOMER)
* Se menu med burgere, drinks og sides
* Opret og administrer produkter med billeder og ingredienser
* Administrer ingredienser med lagerbeholdning
* Bestillingsflow for både gæster og registrerede brugere
* Email-notifikationer ved registrering og ordrebekræftelse

## Teknologier

* Java 25
* Spring Boot 4
* Spring Security
* MySQL
* Docker
* JUnit 5 & Mockito til tests
* GitHub Actions til CI/CD
