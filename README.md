# Throne

Throne este un joc narativ de conducere a unui regat medieval, construit în Java cu libGDX. Joci rolul unui conducător care răspunde în scris cererilor curții, bisericii, armatei și poporului. Hotărârile schimbă echilibrul regatului și influențează evenimentele viitoare. Scopul este să rămâi pe tron cât mai mult timp.

Documentul descrie codul existent la **2 noiembrie 2026**. Stadiul actual este de **prototip cu bucla principală implementată**, cu funcționalități secundare încă în dezvoltare. Modulul `core` și testele logicii jocului au fost compilate direct cu JDK 21. Build-ul Gradle este blocat în acest mediu de configurarea Android (`C:\.android`); fluxul vizual complet necesită verificare în joc.

## Cum se joacă

1. Începi o partidă și introduci numele conducătorului.
2. Citești evenimentul și scrii hotărârea ta, în maximum 120 de caractere.
3. Trimiți răspunsul cu **Enter** sau cu butonul de trimitere.
4. AI-ul îl clasifică în varianta **A**, **B** sau **C** a evenimentului. C reprezintă o hotărâre neclară sau nepotrivită contextului.
5. Citești consecința și continui cu **Space** sau click. O tură încheiată reprezintă o jumătate de an de domnie.

Regatul pornește cu patru statistici la 50: **religie, populație, armată și bani**. Domnia se încheie când oricare ajunge la 0 sau 100. Există opt finaluri: invazie, lovitură militară, faliment, oligarhie, revoltă religioasă, teocrație, depopulare și colaps prin suprapopulare.

AI-ul interpretează textul și formulează o scurtă narațiune. Efectele numerice sunt predefinite în eveniment; modelul nu inventează valorile lor. Promptul cere momentan narațiuni în română.

## Ce poate face acum

- Flux complet în cod: eveniment → răspuns → consecință → actualizarea regatului → următorul eveniment sau game over.
- **72 de evenimente și 17 arcuri narative**, în `assets/data/events.json`, despre biserică, nobili, populație, armată, trezorerie și diplomație.
- Selecție ponderată după starea regatului, raritate, condiții, evenimente recente și cooldown.
- Flag-uri și progresie prin etapele `SEED`, `COMMITMENT`, `ESCALATION`, `CRISIS`, `OUTCOME`; pornirea unor noi arcuri este limitată când sunt deja trei active.
- Escaladarea unor consecințe după turele 40 și 80, prin `DoomService`.
- Meniu principal, numele conducătorului, finaluri, restart și clasament local cu cele mai bune 20 de domnii.
- Setări persistente pentru afișare, VSync, sunet și limbă; resurse UI în română, engleză și rusă.
- Cărți animate, indicatori pentru statistici, fundal cu blur, muzică și efecte sonore.
- Turneul este momentan ascuns din interfața partidei; codul și resursele sunt păstrate pentru o etapă ulterioară. Evenimentele narative care pomenesc turnee rămân în catalog.
- Clienți AI pentru OpenAI și Ollama; furnizorul se alege din Opțiuni > Setări AI și poate fi schimbat în timpul unei domnii.

**Esc** deschide meniul din partidă. „Continue” reia partida curentă sau salvarea locală după repornire, inclusiv o bătălie neterminată. Setările și recordurile folosesc libGDX `Preferences`, cu numele `empire-settings` și `empire_records`. Proiectul nu include un server propriu sau o bază de date externă.

## Arhitectură

Proiectul este organizat în module Gradle. Configurația declară **libGDX 1.14.0**, **Gradle 8.11.1** și **JDK 21 pentru daemonul Gradle**. Sursele core/desktop au compatibilitate Java 8; modulul HTML este configurat pentru Java 11.

```text
Throne/
├── core/       Logica jocului și interfața comună
├── lwjgl3/     Lansator desktop și împachetare JAR
├── android/    Lansator și configurare Android
├── ios/        Lansator RoboVM și resurse iOS
├── html/       Lansator GWT/WebGL
├── assets/     Evenimente, traduceri, imagini, fonturi și sunete
└── gradle/     Wrapper și configurarea JVM
```

În `core/src/main/java/com/lazar/`:

| Componentă | Responsabilitate |
| --- | --- |
| `StartGame` | Inițializează setările, limba, sunetul și primul ecran. |
| `screens` | Meniuri, input, partida principală, turneu și finalul domniei. |
| `engine/GameEngine` | Coordonează partida, evenimentul curent și durata domniei. |
| `engine/GameRunState` | Statistici, ture, flag-uri, cooldown-uri și starea arcurilor. |
| `engine/spawn`, `engine/conditions` | Eligibilitatea și alegerea ponderată a evenimentelor. |
| `engine/EventResolutionProcessor` | Aplică efectele, flag-urile și progresia arcurilor. |
| `engine/DoomService`, `GameOverEvaluator` | Escaladarea efectelor și evaluarea finalului. |
| `model`, `logic/GameStats` | Structurile evenimentelor, deciziilor și statisticilor. |
| `ai/model`, `ai/service` | DTO-uri, prompturi, apeluri HTTP și clasificarea răspunsului. |
| `config` | Contractul `DecisionResolver`, mapări, traduceri, fonturi și sunet. |
| `data` | Încărcarea evenimentelor și persistența recordurilor. |
| `ui/card`, `ui/background` | Prezentarea și desenarea cărților și a fundalului. |

Fluxul unei hotărâri:

```text
GameScreen → GameEngine.submitPlayerText → DecisionResolver
  → AiDecisionResolver → GameDecisionService → client AI
  → clasificare A/B/C + narațiune
  → EventResolutionProcessor → GameRunState / GameStats
  → GameOverEvaluator → consecință afișată / finalul domniei
```

Apelul AI rulează pe un thread separat. Rezultatul revine pe thread-ul aplicației prin `Gdx.app.postRunnable`. `GameDecisionService` selectează consecința numerică predefinită. O eroare de conexiune, credit sau răspuns invalid nu produce o decizie: jucătorul poate reîncerca fără penalizare. Varianta C se aplică numai când modelul returnează efectiv C.

## Stadiu actual și limite

| Zonă | Stadiu în cod |
| --- | --- |
| Partida narativă | Bucla principală implementată; necesită validare prin rulare și echilibrare. |
| Conținut | 72 de evenimente; textele lor sunt în română, separat de traducerile UI. |
| AI | Două variante configurabile din interfață: cheia OpenAI a jucătorului sau Ollama local. Implicit: Ollama. |
| Configurare AI | Valorile implicite sunt citite din `assets/ai-config.json`; furnizorul și modelele se salvează local, cheia rămâne doar în sesiune. |
| Echilibrare | Multiplicatorul de debug ×33 a fost eliminat; efectele se aplică direct, iar echilibrarea necesită playtesting. |
| Turneu | Accesul din interfață este dezactivat temporar; implementarea și resursele sunt păstrate. |
| Război | Declanșat de alegerea apărării în cartea de invazie; 10 ture, timp aleator de 1,8-3,4 secunde pe alegere și efecte distincte pentru victorie, egalitate sau înfrângere. |
| Salvare | O salvare automată locală, reluabilă după repornire, inclusiv progresul războiului; se elimină la game over. |
| Platforme | Module desktop, Android, iOS și HTML prezente; funcționarea pe fiecare nu este confirmată. |
| Testare | Există verificări de regresie pentru regulile războiului; restul experienței de joc necesită validare. |

Următorii pași firești sunt testarea AI pe configurații reale, echilibrarea consecințelor, verificarea desktop, apoi validarea platformelor secundare, testarea experienței complete.

## Cum îl rulăm pe desktop

### 1. Pregătirea mediului

Instalează **JDK 21**, setează `JAVA_HOME` către directorul JDK și adaugă `%JAVA_HOME%\bin` în PATH. Deschide un terminal nou și verifică din rădăcina repository-ului:

```powershell
java -version
.\gradlew.bat --version
```

Wrapper-ul descarcă Gradle și dependențele la prima utilizare, deci este necesar acces la internet. Nu trebuie instalat Gradle separat.

Toate modulele sunt incluse în `settings.gradle`, inclusiv Android. Dacă o comandă desktop se oprește la configurarea Android din cauza SDK-ului, configurează Android SDK 35 în `local.properties`, adaptând calea:

```properties
sdk.dir=C:/Users/NUME/AppData/Local/Android/Sdk
```

Pentru lucru exclusiv desktop se pot exclude temporar din `settings.gradle` modulele `android`, `ios` și `html`, păstrând `core` și `lwjgl3`. Aceasta este o modificare locală, nu o opțiune deja implementată.

### 2. Configurarea AI

În meniul unic **Opțiuni**, derulează până la secțiunea **Setări AI** și alege furnizorul din dropdown. Jocul nu folosește un server al dezvoltatorului și nu include o cheie API comună.

Ecranul de configurare AI, butoanele, instrucțiunile și mesajele de succes / eroare urmează limba interfeței: română, rusă sau engleză. Cheile AI / setări și parametrii mesajelor sunt verificați cu `.\gradlew.bat core:aiLocalizationTest`, inclus în `core:check`. Traducerea setărilor nu schimbă limba evenimentelor sau a narațiunilor AI.

**Ollama local (implicit)**

Instalează Ollama separat, descarcă un model și pornește serviciul local:

```powershell
ollama pull qwen2.5:7b-instruct
ollama serve
```

Dacă serviciul rulează deja, nu porni încă o instanță. În joc alegi „Ollama local”, introduci modelul instalat și adresa (implicit `http://localhost:11434`), apoi apeși „Aplică”. Se acceptă doar adrese loopback, inclusiv `127.0.0.1` și `::1`, fără redirectare către alte servere. Viteza și necesarul de memorie depind de model și de PC. Modelul nu este inclus în distribuția jocului.

**OpenAI cu cheia jucătorului**

În același meniu alegi „OpenAI - cheia ta” din dropdown, selectezi modelul și introduci cheia API, apoi apeși „Aplică”. Modelul implicit din proiect rămâne `gpt-4.1-mini`; disponibilitatea și creditul API țin de contul jucătorului. Cererile merg direct la endpoint-ul oficial OpenAI folosind cheia lui; costul este suportat de jucător.

Cheia este mascată în interfață și păstrată numai în memoria procesului, nu în Preferences, salvarea domniei sau loguri. La repornire trebuie reintrodusă. Pentru a elimina cheia din sesiune, golește câmpul și apasă „Aplică”. Pentru dezvoltare este acceptată și variabila `OPENAI_API_KEY` la pornire. Nu este necesară modificarea surselor Java.

**Verificare și erori**

„Verifică” trimite o cerere mică folosind datele introduse, fără să le aplice; pentru OpenAI aceasta poate consuma credit API. Butonul nu avansează partida. Dacă un apel din joc eșuează, hotărârea nu se aplică, statisticile și tura nu se schimbă, iar textul rămâne disponibil pentru retrimitere cu Enter / butonul de trimitere. Nu există fallback automat C la erori tehnice.

Valorile implicite sunt în `assets/ai-config.json`; alegerea furnizorului, modelele și adresa locală se păstrează în Preferences `throne-ai`. Salvarea domniei rămâne independentă de furnizorul AI. Schimbarea furnizorului se aplică la următoarea cerere.

### 3. Pornire și împachetare

Din rădăcina proiectului, în PowerShell:

```powershell
.\gradlew.bat lwjgl3:run
```

Task-ul setează directorul de lucru la `assets`. În IntelliJ IDEA poți importa proiectul Gradle și executa același task. Dacă lansezi direct clasa `com.lazar.lwjgl3.Lwjgl3Launcher`, setează directorul de lucru la `assets`.

Pentru compilarea desktop și generarea unui JAR cu dependențe și assets incluse:

```powershell
.\gradlew.bat lwjgl3:jar
java -jar .\lwjgl3\build\libs\Throne-1.0.0.jar
```

Pe macOS, lansarea directă a JAR-ului poate necesita `java -XstartOnFirstThread -jar ...`; task-ul Gradle `run` adaugă deja argumentul. Pe Linux/macOS folosește `./gradlew` în loc de `.\gradlew.bat` și acordă permisiune de execuție wrapper-ului dacă este necesar.

## Celelalte platforme

- **Android:** necesită Android SDK 35, platform-tools și un dispozitiv / emulator. Configurația declară minimum API 21 și target API 35. Comenzi: `.\gradlew.bat android:assembleDebug`, apoi `.\gradlew.bat android:installDebug android:run`. APK-ul este produs în `android/build/outputs/apk/debug/`. Integrarea AI și inputul pe mobil necesită validare.
- **HTML:** există task-urile `.\gradlew.bat html:superDev` și `.\gradlew.bat html:dist`, cu distribuția în `html/build/dist`. Fluxul curent folosește `Thread`, `HttpURLConnection` și fonturi FreeType; compatibilitatea GWT necesită verificări și adaptări înainte de a considera versiunea web utilizabilă.
- **iOS:** modul RoboVM inclus; necesită un mediu macOS cu Xcode și configurarea aferentă. Nu este validat în această documentare.

## Modificarea conținutului

### Război: contraatac rapid

Războiul este declanșat de cartea „O armată la porțile castelului”, eligibilă începând cu tura 3 (apariția rămâne aleatorie, cu cooldown de 12 ture). Răspunsul clasificat A plătește 12 aur și evită lupta; B pregătește apărarea; C aplică pierderile ezitării și declanșează tot un asalt. După consecința B sau C, Space sau click deschide bătălia de 10 ture. Butonul lateral de război a fost eliminat. Apasă Enter / Space sau „Începe apărarea”. Inamicul își dezvăluie cartea în 0,4 secunde, apoi pornește o bară cu durată aleatoare de 1,8-3,4 secunde. Cele patru cărți sunt amestecate la fiecare tură; tastele 1-4 aleg pozițiile actuale din mână, nu unități fixe. Poți alege și prin click. Numai contraatacul corect câștigă tura; orice altă alegere sau expirarea timpului o pierde. După fiecare tură este afișată soluția corectă. Peste 5 victorii câștigă bătălia, 5 înseamnă egalitate. La final, Enter / Space sau butonul de revenire aplică rezultatul o singură dată. Esc înainte de final înseamnă retragere și înfrângere. Într-un război narativ nu există rejucare. Rezultatul este afișat pe carte, apoi partida continuă fără o tură narativă suplimentară. Pierderile pot încheia domnia dacă o statistică atinge 0 sau 100. Mini-jocul nu folosește AI; clasificarea hotărârii inițiale folosește resolverul AI configurat.

Reguli: arcașul este învins de scut și sabie; scutul și sabia de cavalerie; cavaleria de suliță; sulița de arcaș. Lupta folosește patru ilustrații medievale, un fundal de asediu cu mișcare discretă și fum, animații de distribuire / atac / recul, marcaje pentru rezultate și efecte sonore originale. Proveniența și prompturile imaginilor sunt în `assets/images/war/ARTWORK.md`; sunetele PCM pot fi regenerate cu `scripts/generate-war-audio.ps1`. Verificările pentru toate cele 16 combinații, timeout, input repetat, progresie și restart se execută cu `.\gradlew.bat core:warBattleTest` și sunt incluse în `core:check`.

Evenimentele se editează în `assets/data/events.json`. Fiecare definește ID-ul, titlul, descrierea, imaginea, categoria, raritatea, condițiile `requires` / `blockedBy`, cooldown-ul și consecințele A/B/C. Câmpurile `onA`, `onB`, `onC` controlează flag-urile și progresia arcului narativ. Imaginile referite trebuie să existe în `assets`.

Textele interfeței sunt în `assets/i18n/strings_{ro,en,ru}.properties`. Schimbarea limbii interfeței nu traduce automat evenimentele JSON sau narațiunile cerute de promptul AI.

Pentru efecte narative care pornesc o bătălie, adaugă `"action": "START_WAR"` în `onA`, `onB` sau `onC`, împreună cu obiectul `war` și cele trei consecințe `victory`, `draw`, `defeat`. Exemplul complet este `event_castle_invasion`. Victoria acordă +4 populație, +6 armată, +8 aur; egalitatea costă 2 populație, 4 armată, 5 aur; înfrângerea costă 8 populație, 12 armată, 15 aur. Tributul costă 12 aur, reduce armata cu 2 și crește populația cu 2.

Testul `.\gradlew.bat core:warEncounterTest` încarcă catalogul real și verifică ramurile A/B/C, eligibilitatea, cooldown-ul, rezultatele, aplicarea unică a efectelor și posibilitatea unui game over după război. Este inclus în `core:check`.

## Salvare automată și continuare

Există un singur slot local de salvare, în libGDX Preferences `throne-save`. Pornirea efectivă a unei domnii noi înlocuiește salvarea precedentă; simpla deschidere a ecranului pentru nume nu o șterge. Nu este configurată sincronizarea Steam Cloud.

Jocul salvează după cartea inițială, după fiecare hotărâre aplicată, la trecerea la o carte nouă, după rezultatul războiului și la pauză / ieșire. Sunt păstrate numele conducătorului, statisticile, turele, cartea curentă și consecința afișată, flag-urile, arcurile narative, cooldown-urile și istoricul recent. Reluarea unei consecințe deja aplicate nu dublează efectele și nu adaugă o tură.

Războiul salvează cartea inamică, ordinea mâinii, alegerea, tura, scorul, istoricul rezultatelor, faza, durata aleasă și timpul rămas la alegeri și schimbări de fază, precum și la pauză / închidere normală. După repornire se reia bătălia salvată; una terminată rămâne pe rezultat, fără rejucare. O închidere forțată poate reveni la ultimul checkpoint, dar nu elimină războiul obligatoriu. Rezultatul consumat nu poate fi aplicat din nou prin restart.

La game over se elimină salvarea activă; clasamentul rămâne în `empire_records`. Datele corupte, versiunile incompatibile și salvările cu statistici de game over nu sunt oferite pentru continuare. Un răspuns AI încă în curs și textul netrimis nu sunt salvate: reluarea revine la cartea fără hotărâre aplicată.

Verificare: `.\gradlew.bat core:saveGameTest` (inclusă în `core:check`). Testele serializează și reîncarcă starea reală a unei domnii, inclusiv arcuri, cooldown-uri, invazie, progresul bătăliei, consecințe aplicate o singură dată, înlocuirea slotului și date invalide.

Verificarea `.\gradlew.bat core:aiConfigurationTest` (inclusă în `core:check`) testează selecția furnizorului, păstrarea setărilor fără cheie pe disc, validarea adreselor locale și erorile fără penalizarea partidei, cu clienți simulați și fără costuri API. Noul meniu a fost verificat vizual în aplicația desktop pentru toate cele trei limbi, inclusiv schimbarea furnizorului, dropdown-ul modelului și păstrarea cheii doar în sesiune. Integrarea cu servicii AI reale necesită o probă cu configurația jucătorului.

### Meniul unic de opțiuni

Afișarea, sunetul, limba și AI-ul sunt grupate într-un singur panou de pergament cu scroll, în paleta medievală a meniurilor existente. Doar revenirea rămâne fixă jos; cele două butoane mici „Verifică” și „Aplică” și mesajele aferente sunt în interiorul secțiunii AI. Scroll-ul se face cu rotița, gesturi de derulare sau bara laterală. Afișarea, volumele și limba se aplică și se salvează imediat. Modificările AI au un indicator separat și se confirmă cu „Aplică”.

Furnizorul și modelul sunt dropdown-uri. Pentru OpenAI se oferă `gpt-4.1-mini`, `gpt-4.1-nano`, `gpt-4.1`, plus „Alt model” pentru un nume personalizat. Disponibilitatea depinde de contul jucătorului. Pentru Ollama există presetări și aceeași opțiune de model personalizat. Câmpul de cheie apare doar la OpenAI; adresa locală apare doar la Ollama. Câmpurile Scene2D acceptă selecție, navigare și lipire cu Ctrl+V; cheia este mascată și nu poate fi copiată din câmpul protejat. Esc închide întâi dropdown-ul deschis, apoi permite revenirea în meniu.

Testele de război verifică și 300 de ture cu variația timpului, mâini valide cu toate cele patru unități, schimbarea ordinii între ture și selecția numerică după poziție. Salvările vechi de război se pot relua cu ordinea și durata lor veche, iar turele noi folosesc regulile aleatoare. Refacerea vizuală a fost verificată în aplicația desktop pentru română, rusă și engleză, inclusiv selecția prin tastatură și reluarea unui checkpoint.

## Narațiunea hotărârilor

AI-ul clasifică în continuare intenția în A/B/C, dar textul afișat este o scenă medievală de 3-4 fraze, aproximativ 55-85 de cuvinte și maximum 700 de caractere. Explicația internă `reason` și literele opțiunilor nu sunt afișate. Promptul cere reacții concrete ale curții și consecințele deja definite, fără comentarii despre un jucător, clasificare sau răspuns corect / greșit. Narațiunea rămâne în română.

Dacă modelul returnează comentarii tehnice, un text prea scurt / prea lung sau JSON în loc de poveste, jocul afișează textul narativ predefinit pentru aceeași opțiune, păstrând clasificarea. Erorile de conexiune rămân reîncercabile fără penalizare. Consecința are acum o față de carte mai lată, fără caseta decorativă goală, pentru o lectură confortabilă.

Statisticile nu sunt transmise numeric în promptul naratorului și nu sunt citite din răspunsul său. `GameDecisionService` selectează efectele din datele A/B/C, iar `EventResolutionProcessor` le recitește din eveniment, ignorând orice cifre furnizate de resolver. Regulile locale de escaladare rămân în motorul jocului. AI-ul nu poate crea flag-uri, declanșa acțiuni noi sau decide rezultatul unui război.

La `event_castle_invasion`, numai plata tributului (A) evită războiul. Apărarea (B) și eschivarea (C) au `START_WAR` în datele evenimentului. Exemplul „îmi bag picioru, eu mă culc” este explicat în prompt drept abandonarea hotărârii, nu apărare deliberată. C descrie o curte rămasă fără poruncă și un rege vecin care profită de ezitare: se aplică efectele C și apoi începe bătălia, fără a anunța rezultatul ei în avans. Clasificarea inițială C rămâne C și după luptă / salvare.

Testul `.\gradlew.bat core:narrativeDecisionTest`, inclus în `core:check`, verifică filtrarea explicațiilor tehnice, delimitarea mesajului jucătorului, cifrele autoritative, declanșarea războiului din C și reluarea sa. Testele folosesc clienți simulați, fără apeluri API. Tranziția carte C → război → consecință și afișarea textului lung au fost verificate în aplicația desktop; calitatea efectivă a modelului ales necesită o probă cu configurația jucătorului.

## Crizele coroanei și severitatea lui C

Au fost adăugate 18 cărți în trei arcuri cu etape și ramificații: **Coroana amanetată**, **Iarna cenușii** și **Jurământul rupt**. Continuările eligibile primesc prioritate după trei ture; ordinea etapelor și momentul schimbării lor sunt păstrate la salvare. Poveștile încheiate nu repornesc.

Varianta C a fost rescrisă în întregul catalog ca evitare a hotărârii și produce pierderi în toate cele patru statistici, fără bonusuri numerice. În crize pierderile sunt mai mari. La invazie, C costă acum 3 religie, 10 populație, 8 armată și 9 aur înaintea bătăliei. Compromisurile A/B din vechiul catalog au fost întărite în date, păstrând tributul invaziei la 12 aur.

În simulări de 300 de domnii pe strategie, C repetat a redus durata mediană de la 54 la 7 ture. O strategie de echilibrare care produce C în 10% dintre ture ajunge la o mediană de 53 de ture. Acestea sunt simulări fără AI, cu efecte cunoscute și rezultate de război presupuse, nu o estimare a comportamentului jucătorilor reali.

Metodologia, traseele poveștilor și rezultatele sunt în [raportul de echilibrare](docs/balance-2026-11-02.md). Verificările se execută cu `.\gradlew.bat core:crisisArcTest`, inclus în `core:check`.
