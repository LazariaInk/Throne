package com.lazar.logic;

import com.lazar.dto.ConsequenceDto;

import java.util.HashMap;
import java.util.Map;

public class EventDecisionCatalog {

    private final Map<String, EventDecisionSet> decisions = new HashMap<>();

    public EventDecisionCatalog() {
        decisions.put("event_001", new EventDecisionSet(
            new ConsequenceDto(
                "Turnirul are loc",
                "Ai poruncit sa fie ridicata arena chiar in piata cetatii. Nobilii aplauda, oamenii se aduna, iar soldatii isi recapata mandria. Episcopul mustra curtea pentru trufie.",
                -1, 1, 2, -2
            ),
            new ConsequenceDto(
                "Regatul intra in post",
                "Ai ascultat glasul episcopului si ai chemat regatul la post si rugaciune. Biserica iti binecuvanteaza tronul, dar soldatii murmura, iar curtea ramane fara sarbatoare.",
                2, -1, -1, 0
            ),
            new ConsequenceDto(
                "Majestatea ramane neclara",
                "Curtea ramane uluita de raspunsul tau nehotarat. Nobilii, clerul si poporul pleaca mai nelinistiti decat au venit.",
                -1, -1, -1, -1
            )
        ));

        decisions.put("event_002", new EventDecisionSet(
            new ConsequenceDto(
                "Privilegiile sunt acordate",
                "Ai intarit pozitia Bisericii. Clerul te binecuvanteaza, dar vistieria simte imediat povara concesiilor.",
                2, 0, 0, -2
            ),
            new ConsequenceDto(
                "Privilegiile sunt refuzate",
                "Ai aparat tezaurul si autoritatea coroanei, dar episcopii vorbesc deja despre mandria lumii.",
                -2, 0, 1, 1
            ),
            new ConsequenceDto(
                "Biserica pleaca nelamurita",
                "Raspunsul tau a fost atat de alunecos incat nimeni nu stie daca ai promis ceva sau ai refuzat totul.",
                -1, -1, 0, -1
            )
        ));

        decisions.put("event_003", new EventDecisionSet(
            new ConsequenceDto(
                "Predicatorii sunt redusi la tacere",
                "Garzile i-au imprastiat, iar curtea rasufla usurata. Totusi, unii ii socotesc deja martiri.",
                -1, -1, 1, 0
            ),
            new ConsequenceDto(
                "Predicatorii sunt tolerati",
                "Ai lasat cuvintele sa curga. Unii te cred intelept, altii slab.",
                1, 1, -1, 0
            ),
            new ConsequenceDto(
                "Curtea raspunde in dodii",
                "Nici predicatorii, nici episcopii, nici poporul nu stiu ce ai vrut sa spui.",
                -1, -1, -1, 0
            )
        ));

        decisions.put("event_004", new EventDecisionSet(
            new ConsequenceDto(
                "Erezia este zdrobita",
                "Ai trimis sabia acolo unde cuvantul nu a mai fost ascultat. Linistea se intoarce, dar satele nu uita.",
                1, -2, 1, -1
            ),
            new ConsequenceDto(
                "Erezia este tolerata",
                "Ai preferat pacea de moment in locul sabiei. Tensiunea scade acum, dar unitatea credintei se fisureaza.",
                -2, 1, 0, 0
            ),
            new ConsequenceDto(
                "Nehotararea hraneste erezia",
                "Lipsa unei directii limpezi face ca glasurile neascultarii sa para mai puternice in fiecare sat.",
                -2, -1, -1, -1
            )
        ));

        decisions.put("event_101", new EventDecisionSet(
            new ConsequenceDto(
                "Granarele se deschid",
                "Ai deschis hambarele coroanei si ai trimis care cu grau spre sud. Taranii se roaga pentru tine, dar rezervele scad.",
                0, 2, 0, -2
            ),
            new ConsequenceDto(
                "Granarele raman inchise",
                "Ai ales prudenta pentru iarna ce vine. Satele simt insa ca au fost parasite.",
                0, -2, 0, 1
            ),
            new ConsequenceDto(
                "Sudul ramane fara raspuns limpede",
                "Ai rostit cuvinte care nu lamuresc daca hambarele se deschid ori raman zavorate.",
                -1, -1, 0, 0
            )
        ));

        decisions.put("event_102", new EventDecisionSet(
            new ConsequenceDto(
                "Pretul painii este plafonat",
                "Ai lovit specula, iar orasenii te lauda. Breslele privesc insa hotararea ca pe un atac.",
                0, 2, 0, -2
            ),
            new ConsequenceDto(
                "Piata ramane libera",
                "Ai aparat comertul, dar poporul simte greutatea preturilor in fiecare zi.",
                0, -2, 0, 1
            ),
            new ConsequenceDto(
                "Negustorii pleaca fara raspuns clar",
                "Nimeni nu intelege daca ai amenintat, ai promis sau doar ai amanat hotararea.",
                -1, -1, 0, -1
            )
        ));

        decisions.put("event_103", new EventDecisionSet(
            new ConsequenceDto(
                "Migratii sunt asezati cu forta",
                "Ai redirectionat oamenii si ai impus ordine. Drumurile se limpezesc, dar nemultumirea ramane.",
                0, 1, 1, -1
            ),
            new ConsequenceDto(
                "Ii lasi sa plece unde pot",
                "Ai evitat constrangerea, dar ogoarele raman goale si ordinea se subtiaza.",
                0, -1, -1, 0
            ),
            new ConsequenceDto(
                "Nehotararea imprastie si mai mult lumea",
                "Satele se golesc, orasele se umplu, iar nimeni nu stie cine raspunde pentru haos.",
                -1, -2, -1, 0
            )
        ));

        decisions.put("event_104", new EventDecisionSet(
            new ConsequenceDto(
                "Jafurile sunt zdrobite prin forta",
                "Ai trimis soldatii pe drumuri si ai spanzurat talharii. Linistea revine, dar foametea ramane.",
                0, -2, 1, -1
            ),
            new ConsequenceDto(
                "Trimiti ajutor si paza",
                "Ai incercat sa hranesti si sa protejezi in acelasi timp. Curtea ofteaza, dar oamenii traiesc inca.",
                0, 2, -1, -2
            ),
            new ConsequenceDto(
                "Regatul priveste, iar drumurile ard",
                "Neclaritatea ta lasa loc foamei si sabiei sa lucreze impreuna.",
                -1, -2, -1, -1
            )
        ));

        decisions.put("event_201", new EventDecisionSet(
            new ConsequenceDto(
                "Taxele cresc",
                "Ai strans aur pentru coroana, dar targurile si hanurile murmura.",
                0, -1, 0, 2
            ),
            new ConsequenceDto(
                "Cheltuielile sunt taiate",
                "Ai salvat tezaurul pe moment, dar oamenii curtii si garnizoanele simt imediat lipsurile.",
                0, 0, -1, 1
            ),
            new ConsequenceDto(
                "Vistieria ramane in suspin",
                "Raspunsul tau nu a multumit pe nimeni si nu a inchis nicio gaura in tezaur.",
                -1, -1, 0, -1
            )
        ));

        decisions.put("event_202", new EventDecisionSet(
            new ConsequenceDto(
                "Imprumutul este acceptat",
                "Aurul intra in tezaur, dar pretul politic se va simti curand.",
                0, 0, 0, 2
            ),
            new ConsequenceDto(
                "Imprumutul este refuzat",
                "Ai pastrat demnitatea coroanei, dar vistieria ramane goala.",
                0, 0, 0, -2
            ),
            new ConsequenceDto(
                "Breslele pleaca ofensate",
                "Au venit cu aur si au plecat cu dispret.",
                -1, -1, 0, -1
            )
        ));

        decisions.put("event_203", new EventDecisionSet(
            new ConsequenceDto(
                "Le dai porturi si garantii",
                "Coroana castiga timp, dar pierde o parte din puterea ei.",
                0, 0, 0, 2
            ),
            new ConsequenceDto(
                "Creditorii sunt refuzati",
                "Ai aparat autoritatea tronului, dar conflictul financiar se ascute.",
                0, -1, 0, -2
            ),
            new ConsequenceDto(
                "Datoria intra in ceata",
                "Nimeni nu stie daca ai promis sau ai amenintat, iar creditorii se pregatesc de orice.",
                -1, -1, 0, -1
            )
        ));

        decisions.put("event_204", new EventDecisionSet(
            new ConsequenceDto(
                "Poporul plateste pretul",
                "Ai strans cu forta tot ce se mai putea strange. Tezaurul respira, dar strazile fierb.",
                0, -2, 0, 2
            ),
            new ConsequenceDto(
                "Armata ramane neplatita",
                "Ai salvat aur pentru administratia civila, dar sabia coroanei devine nesigura.",
                0, 0, -2, 1
            ),
            new ConsequenceDto(
                "Falimentul este negat",
                "Ai preferat cuvintele in locul calculelor, iar realitatea se apropie si mai rece.",
                -1, -1, -1, -2
            )
        ));
    }

    public EventDecisionSet getFor(String eventId) {
        EventDecisionSet set = decisions.get(eventId);
        if (set != null) {
            return set;
        }

        return new EventDecisionSet(
            new ConsequenceDto(
                "Optiunea A",
                "Hotararea regelui a inclinat balanta intr-o directie clara.",
                0, 0, 0, 0
            ),
            new ConsequenceDto(
                "Optiunea B",
                "Curtea observa schimbarea si fiecare tabara reactioneaza dupa interes.",
                0, 0, 0, 0
            ),
            new ConsequenceDto(
                "Hotarare neclara",
                "Raspunsul tau i-a lasat pe toti in ceata, iar lipsa unei directii limpezi slabeste increderea in tron.",
                -1, -1, -1, -1
            )
        );
    }
}
