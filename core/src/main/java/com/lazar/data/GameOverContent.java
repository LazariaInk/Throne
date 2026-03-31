package com.lazar.data;

import com.lazar.engine.GameOverType;

public class GameOverContent {

    public static String title(GameOverType type) {
        switch (type) {
            case INVASION:
                return "Regatul Cade";
            case MILITARY_COUP:
                return "Corona este Capturată";
            case BANKRUPTCY:
                return "Tezaurul este Gol";
            case OLIGARCHY:
                return "Aurul Stăpânește Regatul";
            case RELIGIOUS_REVOLT:
                return "Credința Se Transformă în Furie";
            case THEOCRACY:
                return "Biserica Preia Tronul";
            case DEAD_KINGDOM:
                return "Un Regat Tăcut";
            case OVERCROWDED_COLLAPSE:
                return "Regatul Nu Poate Suporta Mai Mult";
            default:
                return "Sfârșitul Jocului";
        }
    }

    public static String body(GameOverType type) {
        switch (type) {
            case INVASION:
                return "Armatele tale s-au stins până când granițele au rămas deschise. "
                    + "Steagurile străine se ridică acum deasupra orașelor tale, iar numele tău este amintit "
                    + "nu ca un conducător, ci ca regele care a lăsat porțile nepăzite.";

            case MILITARY_COUP:
                return "Armata a devenit prea puternică pentru a asculta coroana. "
                    + "Generalii tăi nu mai cereau ordine — ei le dădeau. "
                    + "Oțelul a decis ce lege nu putea, iar domnia ta s-a încheiat sub pașii soldaților.";

            case BANKRUPTCY:
                return "Tezaurul s-a epuizat. Datoriile au înghițit promisiunile, soldații nu au mai fost plătiți, "
                    + "iar comercianții au abandonat drumurile tale. Un regat poate supraviețui foametei sau războiului pentru o vreme, "
                    + "dar nu golirii visteriei pentru totdeauna.";

            case OLIGARCHY:
                return "Bogăția s-a adunat în prea puține mâini, iar tronul a devenit o decorare. "
                    + "Cei bogați comandă acum regatul prin monedă, contract și amenințări tăcute. "
                    + "Tu încă purtai coroana, dar puterea fusese deja vândută.";

            case RELIGIOUS_REVOLT:
                return "Credința a căzut în furie. Templele s-au transformat în locuri de adunare, "
                    + "preoții în agitatori, iar oamenii s-au ridicat împotriva unui conducător pe care l-au crezut părăsit de divinitate. "
                    + "Regatul a ars de furia sacră.";

            case THEOCRACY:
                return "Biserica a crescut dincolo de sfat și dincolo de restricții. "
                    + "Scriptura a înlocuit decretul, clergia a înlocuit miniștri, iar tronul a îngenuncheat înaintea altarului. "
                    + "Regatul tău a supraviețuit, dar nu mai aparținea regilor.";

            case DEAD_KINGDOM:
                return "Prea mulți au murit, prea mulți au fugit, și prea puțini au rămas. "
                    + "Câmpiile s-au golit, piețele au căzut tăcute, iar chiar și victoria a pierdut sens într-o țară fără oameni. "
                    + "O coroană nu conduce nimic atunci când regatul însuși a dispărut.";

            case OVERCROWDED_COLLAPSE:
                return "Regatul a crescut dincolo de limitele sale. Foametea s-a răspândit, străzile s-au umplut, "
                    + "iar ordinea a crăpat sub greutatea a prea multor vieți și prea puține structuri. "
                    + "Ceea ce părea a fi prosperitate s-a transformat în colaps.";

            default:
                return "Domnia ta a ajuns la sfârșit.";
        }
    }
}
