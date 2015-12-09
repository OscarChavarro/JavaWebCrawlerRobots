package model;

//===========================================================================
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.util.ArrayList;
import org.bson.types.ObjectId;

/**
*/
public class FormQuestion {
    private ArrayList<FormQuestionElement> elements;

    public FormQuestion()
    {
        elements = new ArrayList<FormQuestionElement>();
    }

    /**
    @return the elements
    */
    public ArrayList<FormQuestionElement> getElements() {
        return elements;
    }

    /**
    @param elements the elements to set
    */
    public void setElements(ArrayList<FormQuestionElement> elements) {
        this.elements = elements;
    }
    
    private void validate()
    {
        int i;
        for ( i = 0; i < elements.size(); i++ ) {
            if ( elements.get(i).isEmpty() ) {
                elements.remove(i);
                i--;
            }
        }
        
        for ( i = 0; i < elements.size(); i++ ) {
            elements.get(i).removeEnters();
        }
    }
    
    public String exportToReport()
    {
        String msg = "Q, ";
        
        if ( elements.isEmpty() ) {
            msg += "vacio, ";
        }
        
        int i;
        for ( i = 0; i < elements.size(); i++ ) {
            msg += elements.get(i).exportToReport();
        }

        return msg;
    }
    
    @Override
    public String toString()
    {
        String msg;
        
        validate();
        
        msg = "Question with " + elements.size() + " elements";
        int i;
        for ( i = 0; i < elements.size(); i++ ) {
            msg = msg + "\n    . " + elements.get(i);
        }
        if ( elements.size() > 0 ) {
            msg = msg + "\n";
        }
        
        return msg;
    }

    public void insertInMongoDB(DB db, String formId, int orderIndex) {
        validate();

        int i;
        int count = 0;
        String c;
        String t;
        String a;
        String lastC = "";
        String lastL = null;
        ArrayList<String> options;
        options = new ArrayList<String>();
        boolean hasSelects = false;
        boolean hasChecks = false;
        boolean hasSubmit = false;
        for ( i = 0; i < elements.size(); i++ ) {
            t = elements.get(i).getType();
            c = elements.get(i).getContent();
            a = elements.get(i).getClassName();

            if ( t != null && t.equals("label") &&
                 c != null && c.length() > 0 ) {
                count++;
                lastC = c;
            }
            if ( t != null && t.equals("legend") &&
                 a != null && a.length() > 0 ) {
                lastL = a;
            }
            if ( t != null && t.equals("select_option") &&
                 c != null && c.length() > 0 ) {
                options.add(c);
                hasSelects = true;
            }
            if ( t != null && t.equals("checkbox") &&
                 c != null && c.length() > 0 ) {
                options.add(c);
                hasChecks = true;
            }
            if ( t != null && t.equals("submit") &&
                 c != null && c.length() > 0 ) {
                hasSubmit = true;
            }
        }
        String questionName;

        if ( count == 1 ) {
            questionName = lastC;
        }
        else if ( count == 0 ) {
            if ( hasSubmit ) {
                return;
            }
            //System.out.println("----------------");
            //System.out.println("Pregunta con error: " + this.toString());
            //System.out.println("----------------");
            // OJO: Por revisar qué tipos de preguntas se están saltando.
            return;
        }
        else {
            questionName = "PREGUNTA INVALIDA: MULTIPLES ETIQUETAS";
        }
        
        if ( hasChecks && hasSelects ) {
            questionName += " (PREGUNTA INCOHERENTE, DE TIPO DUAL CHECK Y SELECT)";
        }
        
        DBCollection serviceRequestFormQuery;

        serviceRequestFormQuery = db.getCollection("serviceRequestFormQuery");
        
        BasicDBObject query;
        ObjectId id;
        id = new ObjectId();

        query = new BasicDBObject("_id", id.toString());
        query.append("descriptionEng", questionName);
        query.append("descriptionSpa", questionName);
        if ( lastL != null ) {
            query.append("legend", lastL);
        }

        // Determine question type
        String queryTypeId;
        if ( hasChecks ) {
            queryTypeId = getQueryType(db, "MULTIPLE_SELECTION_CUSTOM");
        }
        else if ( hasSelects ) {
            queryTypeId = getQueryType(db, "SINGLE_SELECTION_CUSTOM");
            
        }
        else {
            queryTypeId = getQueryType(db, "FREE_TEXT_SINGLE_LINE");            
        }
        query.append("queryTypeId", queryTypeId);
        
        serviceRequestFormQuery.insert(query);
        String queryId = query.get("_id").toString();
        
        //---
        DBCollection serviceRequestForm2Query;
        serviceRequestForm2Query = db.getCollection("serviceRequestForm2Query");
        id = new ObjectId();

        query = new BasicDBObject("_id", id.toString());
        query.append("formId", formId);
        query.append("queryId", queryId);
        query.append("order", orderIndex);
        serviceRequestForm2Query.insert(query);

        //---
        DBCollection serviceRequestAnswerOption;
        serviceRequestAnswerOption = db.getCollection("serviceRequestAnswerOption");

        if ( hasChecks || hasSelects ) {
            for ( i = 0; i < options.size(); i++ ) {
                id = new ObjectId();
                query = new BasicDBObject("_id", id);
                query.append("nameEng", options.get(i));
                query.append("nameSpa", options.get(i));
                query.append("parentServiceRequestFormQueryId", queryId);
                query.append("order", i);
                serviceRequestAnswerOption.insert(query);
            }
        }
    }

    private String getQueryType(DB db, String name) {
        DBCollection serviceRequestQueryType;
        serviceRequestQueryType = db.getCollection("serviceRequestQueryType");
        
        BasicDBObject query;
        query = new BasicDBObject("nameC", name);
        DBObject x = serviceRequestQueryType.findOne(query);
        if ( x == null || !x.containsField("nameSpa") ) {
            return "NOT_IN_DATABASE";
        }
        return x.get("_id").toString();
    }
    
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
