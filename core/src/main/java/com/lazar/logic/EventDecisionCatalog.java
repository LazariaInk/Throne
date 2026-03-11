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
                "Ai poruncit sa fie ridicata arena chiar in piata cetatii. Nobilii aplauda, oamenii se aduna in jurul turnirului, iar soldatii isi recapata mandria. Episcopul priveste totul ca pe o risipa lumeasca si mustra curtea pentru trufie.",
                -1, 2, 2, -2
            ),
            new ConsequenceDto(
                "Regatul intra in post",
                "Ai ascultat glasul episcopului si ai chemat regatul la post si rugaciune. Biserica iti binecuvanteaza tronul, iar vistieria este crutata de cheltuieli, dar soldatii murmura, iar poporul ramane fara sarbatoarea pe care o astepta.",
                2, -1, -2, 1
            ),
            new ConsequenceDto(
                "Majestatea divagheaza",
                "Curtea ramane uluita de raspunsul tau, care nu lamureste deloc daca turnirul va avea loc sau daca regatul va intra in post. Nobilii, clerul si poporul pleaca mai nelinistiti decat au venit, iar nehotararea incepe sa slabeasca autoritatea tronului.",
                -1, -1, -1, -1
            )
        ));

        decisions.put("event_002", new EventDecisionSet(
            new ConsequenceDto(
                "Taxele scad",
                "Ai redus birurile pentru bresle, iar targurile s-au umplut repede de marfuri si calatori. Orasenii te lauda pentru intelepciune, dar aurul intra mai greu in visterie, iar capitanul cere amanarea unor plati catre garnizoane.",
                0, 2, -1, -2
            ),
            new ConsequenceDto(
                "Taxele raman mari",
                "Ai pastrat taxele ridicate si vistieria respira usurata. Totusi, negustorii isi ascund marfurile, oamenii se plang de preturi, iar prin hanuri se vorbeste ca tronul strange aur, dar uita foamea oraselor.",
                -1, -2, 0, 2
            ),
            new ConsequenceDto(
                "Negustorii pleaca nedumeriti",
                "Le-ai raspuns negustorilor cu vorbe fara legatura, iar sfatul nu intelege ce hotarare ai luat. Breslele amana caravanele, orasenii se tem de lipsuri, iar vistiernicul ofteaza vazand cum incertitudinea apasa peste targuri.",
                -1, -1, 0, -1
            )
        ));

        decisions.put("event_003", new EventDecisionSet(
            new ConsequenceDto(
                "Granarele se deschid",
                "Ai deschis granarele regale si carele cu grau au pornit spre sud. Taranii iti rostesc numele in rugaciuni, iar satele raman credincioase coroanei. Totusi, rezervele scad primejdios, iar armata primeste portii mai mici.",
                1, 3, -1, -2
            ),
            new ConsequenceDto(
                "Granarele raman inchise",
                "Ai pastrat granarele inchise pentru iarna ce va veni. Sfetnicii lauda prudenta, iar tezaurul nu este impovarat de transporturi, dar satele din sud simt ca tronul le-a intors spatele si tulburarea incepe sa creasca.",
                -1, -3, 0, 1
            ),
            new ConsequenceDto(
                "Satele nu primesc raspuns limpede",
                "Ai rostit cuvinte care nu lamuresc daca granarele se deschid ori raman zavorate. Taranii se intorc acasa fara nadejde, sfetnicii se cearta intre ei, iar tulburarea creste in tinuturile lovite de seceta.",
                0, -2, 0, 0
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
