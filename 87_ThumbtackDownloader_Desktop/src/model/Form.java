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
public class Form {
    private ArrayList<FormQuestion> questions;

    public Form()
    {
        questions = new ArrayList<FormQuestion>();
    }
    
    /**
     * @return the questions
     */
    public ArrayList<FormQuestion> getQuestions() {
        return questions;
    }

    /**
     * @param questions the questions to set
     */
    public void setQuestions(ArrayList<FormQuestion> questions) {
        this.questions = questions;
    }

    public void print() {
        System.out.println("Form with " + questions.size() + " questions:");
        int i;
        for ( i = 0; i < questions.size(); i++ ) {
            System.out.println("  - " + questions.get(i));
        }
    }

    public String exportToReport() {
        int i;
        
        String msg = "" + questions.size() + ", ";
        for ( i = 0; i < questions.size(); i++ ) {
            msg += questions.get(i).exportToReport();
        }
        return msg;
    }

    public void insertInMongoDB(DB db, String idser) {
        int i;
        
        DBCollection serviceRequestForm;
        serviceRequestForm = db.getCollection("serviceRequestForm");
        BasicDBObject query;
        query = new BasicDBObject("parentProfessionalServiceId", idser);
        ObjectId id;
        id = new ObjectId();
        query.append("_id", id.toString());

        serviceRequestForm.insert(query);

        DBObject answer;
        answer = serviceRequestForm.findOne(query);

        if ( answer != null && answer.containsField("_id") ) {
            System.out.println("    . Formulario con " + questions.size() + " preguntas");
            for ( i = 0; i < questions.size(); i++ ) {
                questions.get(i).insertInMongoDB(db, answer.get("_id").toString(), (i+1));
            }
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
