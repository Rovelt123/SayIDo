# Design af testcases

Dette dokument beskriver hvordan vi finder frem til hvilke testcases vi skriver, så det ikke bliver tilfældigt hvad der bliver testet.

## Ækvivalensklasser

I stedet for at skrive en test per tænkelig værdi deler vi input op i klasser hvor systemet forventes at opføre sig ens, og tester så én værdi fra hver klasse. For et password er der en klasse af koder der opfylder alle krav, og en klasse per krav der ikke er opfyldt. For et budgetbeløb er der gyldige beløb, negative beløb og ingen værdi. Ideen er at hvis en værdi i en klasse virker, gør de andre i samme klasse det også, og vi får derfor dækning uden at skrive hundredvis af test.

## Grænseværdier

Fejl ligger typisk i grænserne, ikke midt i en klasse. Derfor tester vi på begge sider af hver grænse og på grænsen selv. Det gælder længdekrav på password, hvor vi tester en værdi under grænsen, grænsen selv og en over, og det gælder datoer, hvor dagen i dag, dagen før og dagen efter er de interessante værdier.

## Beslutningstabeller

Hvor flere betingelser spiller sammen, stiller vi dem op i en tabel med kombinationerne og hvad resultatet skal være. Det bruger vi til validering af password, hvor flere krav skal være opfyldt samtidig, og til nulstilling af password, hvor både tokenets gyldighed, dets udløbstid og om det er brugt før er med til at afgøre svaret. Tabellen gør det tydeligt om der er kombinationer vi har glemt at tage stilling til.

## Datadrevne test

Når det samme stykke logik skal testes med mange forskellige input, skriver vi testen én gang og lægger inputtene i en csv fil. Testen er annoteret med ParameterizedTest og CsvFileSource, og filerne ligger under src/test/resources.

Vi har tre filer. password-test-cases.csv med fjorten rækker dækker validering af password. budget-test-cases.csv med elleve rækker dækker budgetbeløb. category-budget-test-cases.csv med ni rækker dækker summen af kategoribudgetter mod totalen.

Hver fil har en sidste kolonne der hedder story og beskriver hvad rækken dækker i ord. Den bliver brugt i testens navn, så når en test fejler, står der i outputtet hvilket tilfælde der fejlede og ikke kun et nummer.

En detalje der er værd at kende i csv filerne er at to anførselstegn betyder en tom streng, mens et felt der bare er tomt bliver læst som null. De to ting er ikke det samme i koden, og vi tester begge.

## Hvad vi ikke tester

Vi skriver ikke test på getters, settere og rene datamodeller, fordi der ikke er logik at få galt i halsen. Vi skriver heller ikke test på tredjeparts biblioteker. Vi tester vores egen brug af dem.
