package webcrawler.processors;

import com.mongodb.DBObject;
import databaseMongo.model.ProfessionHint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
*/
public class ProfessionHintProcessor {

    public static void processProfessionHint(DBObject o, int i, int count, 
        HashMap<String, ProfessionHint> professions) 
    {
        String p = o.get("professionHint").toString();
        if (!professions.containsKey(p)) {
            ProfessionHint pv;
            pv = new ProfessionHint();
            pv.setApareancesCount(1);
            pv.setContent(p);
            professions.put(p, pv);
        } else {
            professions.get(p).incrementCount();
        }
    }

    public static void reportResultingProfessionHints(
            HashMap<String, ProfessionHint> professionHints) 
    {
        System.out.println("Cantidad de profesiones encontradas: " + professionHints.size());
        ArrayList<ProfessionHint> orderedSet;
        orderedSet = new ArrayList<>();
        int rareProfessions = 0;
        int threshold = 10;
        for (String si : professionHints.keySet()) {
            ProfessionHint ph = professionHints.get(si);
            if (ph.getApareancesCount() >= threshold) {
                orderedSet.add(ph);
            } else {
                rareProfessions++;
            }
        }
        Collections.sort(orderedSet);
        int i;
        System.out.println("  - Profesiones extra\u00f1as, con menos de " + threshold + " personas en cada una (no mostradas): " + rareProfessions);
        int n = 0;
        for (i = 0; i < orderedSet.size(); i++) {
            n += orderedSet.get(i).getApareancesCount();
        }
        System.out.println("  - Profesiones comunes, con " + threshold + " o m\u00e1s personas en cada una (mostradas a continuaci\u00f3n): " + n);
        for (i = 0; i < orderedSet.size(); i++) {
            System.out.println("  - " + orderedSet.get(i));
        }
    }

}
