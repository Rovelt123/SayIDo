# Teststrategi

Dette dokument beskriver hvordan vi tester Say I Do i sprint 4, hvad vi prioriterer, og hvorfor.

## Hvad vi vurderer som risikabelt

Vi har taget udgangspunkt i de steder hvor en fejl gør mest skade for en bruger der planlægger et bryllup.

Login og adgangskontrol. Hvis en bruger kan se eller rette i en andens bryllup, er det den værste fejl systemet kan lave. Det er bygget op om JWT, og tokenet afgør hvem man er.

Password og nulstilling af password. Her er der både validering af krav til koden og et link der kun må virke én gang og kun indenfor tidsgrænsen.

Budget. Summer regnes ud flere steder, og hvis delsummerne for kategorier ikke stemmer med totalen, mister tallene deres værdi.

Datoer. En bryllupsdato må ikke kunne ligge i fortiden, og den validering skal ligge i backend og ikke kun i frontend.

Databasen. Vi kører PostgreSQL i produktion og H2 under test, og de to opfører sig ikke altid ens.

## Testniveauer

Unit test. Vi tester services og validering isoleret uden database. Det er her vi kommer bredt rundt om regler og grænseværdier, fordi testene er hurtige og nemme at køre mange gange.

Integrationstest. Vi starter Javalin op med en H2 database og kalder API'et over HTTP med rigtige requests og tokens. Det er her vi fanger de fejl der først opstår når lagene sættes sammen. Et eksempel er at JWTTokenGenerator holdt en statisk DAO med en entity manager der var lukket, hvilket ingen unit test kunne vise.

Manuel test. Flow i frontend og det visuelle tester vi i hånden. Vi har ikke sat Selenium op i denne sprint.

## Testtyper

Vi tester funktionalitet, altså om systemet gør det det skal. Vi tester validering og negative tilfælde, altså at systemet afviser det det skal afvise, og at det giver den rigtige fejl og den rigtige statuskode. Vi tester adgangskontrol ved at kalde endpoints uden token og med en anden brugers token. Vi tester ikke ydeevne eller load i denne sprint.

## Automatisk og manuelt

Alt der kan køres af en maskine, kører af en maskine. Unit test kører med Surefire og integrationstest kører med Failsafe på navnemønsteret IT. Vores workflow kører mvn verify på pull requests mod Review, Development og Main, så begge dele kører før vi kan merge. Vi har 184 test der kører grønt, 169 unit test og 15 integrationstest.

Vi har flest unit test, færre integrationstest og ingen UI test automatiseret. Det følger testpyramiden, hvor de hurtige og billige test ligger nederst og de dyre ligger øverst.

## Dækning

Vi har valgt 60 procent som vores minimum for linjedækning, og det er håndhævet i buildet med JaCoCo på verify. Grænsen er noget vi selv sætter. Den er ikke et krav udefra, men et niveau vi mener er realistisk for projektets størrelse.

Grænsen gælder BUNDLE, altså projektet som helhed, og ikke pakke for pakke. Det er et bevidst valg, fordi dækningen er meget ujævn fordelt. Målt den 23. oktober ligger de enkelte pakker fra 7,7 procent til 100 procent, og en grænse per pakke ville derfor stoppe buildet på kode vi ikke har prioriteret.

Da vi målte, lå den samlede dækning på 61,3 procent når unit test og integrationstest tælles sammen. Kører vi kun unit test, falder den til 35,6 procent. Vores definition of done taler om unit test coverage, men den grænse vi håndhæver i buildet måler hele den automatiserede suite, og derfor skriver vi tallet eksplicit her så det ikke bliver læst som noget det ikke er. Forskellen mellem de to tal siger samtidig noget vigtigt om hvor dækningen kommer fra. Controllers og routes bliver dækket gennem integrationstestene, ikke gennem unit test, og derfor er integrationstestene i buildet en forudsætning for at tallet holder.

## Hvorfor strategien ser sådan ud

Say I Do er et system hvor det meste logik ligger i backend, hvor data skal være rigtige over tid, og hvor flere brugere har hver deres bryllup. Derfor vægter vi backend højere end frontend, validering højere end visning, og adgangskontrol højest. Frontend tester vi manuelt, fordi fejl der er synlige for det blotte øje er billigere at finde i hånden end at automatisere i den tid vi har.
