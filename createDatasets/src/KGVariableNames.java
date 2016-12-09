/**
 * Created by curtis on 09/12/16.
 */
public class KGVariableNames {

    private  String eventClass = "";
    private String dateVar = "";
    private String latVar = "";
    private String longVar = "";
    private String label2Var = "";
    private String placeVar = "";
    private String enVar = "";
    private String otherKgURLstart = "";

    public  KGVariableNames(int k) {
        if (k==0) { //dbpedia
            this.eventClass = "dbo:Event";
            this.dateVar = "dbo:date";
            this.latVar = "geo:lat";
            this.longVar = "geo:long";
            this.label2Var = "foaf:name";
            this.placeVar = "dbo:place";
            this.enVar = "EN";
            this.otherKgURLstart = "http://yago-knowledge.org";
        } else if (k==1) { //yago
            this.eventClass = "yago:wordnet_event_100029378";
            this.dateVar = "yago:happenedOnDate";
            this.latVar = "yago:hasLatitude ";
            this.longVar = "yago:hasLongitude";
            this.label2Var = "skos:prefLabel";
            this.placeVar = "yago:happenedIn";
            this.enVar = "ENG";
            this.otherKgURLstart = "http://dbpedia.org";
        }
    }

    public String getEventClass() {
        return eventClass;
    }
    public String getDateVar() {
        return dateVar;
    }

    public String getLatVar() {
        return latVar;
    }

    public String getLongVar() {
        return longVar;
    }

    public String getLabel2Var() {
        return label2Var;
    }

    public String getPlaceVar() {
        return placeVar;
    }

    public String getEnVar() {
        return enVar;
    }

    public String getOtherKgURL() {
        return otherKgURLstart;
    }
}
