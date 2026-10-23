# Testbarhed

Dette dokument handler om hvor nemt eller svært vores kode er at teste, og hvad vi har ændret for at gøre den nemmere.

## Hvorfor det betyder noget

En test kan kun skrives hvis man kan sætte koden i en kendt tilstand, kalde den og se resultatet. Kode der selv henter sine afhængigheder, eller gemmer tilstand statisk, kan man ikke sætte i en kendt tilstand udefra. Så bliver testen enten umulig at skrive eller afhængig af rækkefølgen den kører i.

## Et konkret problem vi har rettet

JWTTokenGenerator havde en statisk DAO som felt, der blev oprettet med entity manageren én gang da klassen blev indlæst. Under test betød det at DAO'en holdt fast i en entity manager der var lukket ned igen, og alle kald der skulle læse en bruger ud fra et token fejlede. Det var ikke synligt i unit test, fordi ingen unit test rører den klasse. Det kom først frem da integrationstestene faktisk begyndte at køre.

Rettelsen var at fjerne det statiske felt og i stedet oprette DAO'en inde i metoden når den skal bruges. Så er der ikke længere tilstand der overlever mellem test.

Det er et godt eksempel på at statiske felter med levetid koster testbarhed, selvom de ser praktiske ud.

## Hvor vi har gjort det rigtigt

EmailService får sin afsender ind som parameter, og afsenderen ligger bag et interface med BrevoMailSender som implementering. Det betyder at man kan teste det der bruger EmailService uden at der bliver sendt rigtige mails. Det er mønsteret vi gerne vil have flere steder.

## Hvor det stadig er svært

UserService opretter selv sin EmailService i feltet. Så længe den gør det, kan man ikke sætte en anden afsender ind udefra, og tests på den klasse bliver afhængige af konfiguration der ellers ikke har noget med testen at gøre. Det samme gælder de steder hvor en service selv finder sin entity manager frem i stedet for at få den ind.

Det næste skridt er at lade de klasser tage deres afhængigheder ind i konstruktøren. Det er en lille ændring per klasse, men den gør at vi kan teste et lag ad gangen.

## Databasen under test

Vi bruger H2 i hukommelsen under test, fordi den starter hurtigt og ikke kræver noget af den der kører testen. Prisen er at H2 og PostgreSQL ikke opfører sig helt ens, og at vi derfor kan have test der er grønne hos os og en fejl i produktion. Testcontainers med en rigtig PostgreSQL ville fjerne den forskel, og det ligger som en opgave vi gerne vil tage.

## Adskillelse af unit test og integrationstest

Unit test kører med Surefire og hedder Test i navnet. Integrationstest kører med Failsafe og hedder IT i navnet. Adskillelsen gør at man kan køre de hurtige test mens man udvikler, og at de langsomme stadig kører i buildet. Det var også den adskillelse der gjorde det tydeligt at integrationstestene aldrig blev kørt, fordi workflowet kørte mvn test og ikke mvn verify. Det er rettet nu.
