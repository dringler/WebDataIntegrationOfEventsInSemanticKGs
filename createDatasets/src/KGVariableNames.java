/**
 * Created by curtis on 09/12/16.
 */
public class KGVariableNames {

    private String eventClass = "";
    private String dateVar = "";
    private String latVar = "";
    private String longVar = "";
    //private String label2Var = "";
    private String locationVar = "";
    //private String place2Var = "";
    private String enVar = "";
    //private String otherKgURLstart = "";

    public  KGVariableNames(int k) {
        if (k==0) { //dbpedia
            this.eventClass = "dbo:Event";
            this.dateVar = "dbo:date";
            this.latVar = "geo:lat";
            this.longVar = "geo:long";
            //this.label2Var = "foaf:name";
            this.locationVar = "dbo:place";
            //this.place2Var = "dbo:location";
            this.enVar = "EN";
            //this.otherKgURLstart = "http://yago-knowledge.org";
        } else if (k==1) { //yago
            this.eventClass = "yago:wordnet_event_100029378";
            this.dateVar = "yago:happenedOnDate";
            this.latVar = "yago:hasLatitude";
            this.longVar = "yago:hasLongitude";
           // this.label2Var = "skos:prefLabel";
            this.locationVar = "yago:isLocatedIn";
            //this.locationVar = "yago:happenedIn";
            //this.place2Var = "yago:isLocatedIn";
            this.enVar = "ENG";
            //this.otherKgURLstart = "http://dbpedia.org";
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
    public String getLocationVar() {
        return locationVar;
    }
    public String getEnVar() {
        return enVar;
    }
    /*
    public String getLabel2Var() { return label2Var; }
    public String getPlace2Var() {
        return place2Var;
    }
    public String getOtherKgURL() { return otherKgURLstart; }
    */
    public String getPlaceVar(String propertyName) {
        if (propertyName.equals("place") ||
                propertyName.equals("location") ||
                propertyName.equals("city") ||
                propertyName.equals("territory")
                ) {return "dbo:"+propertyName;}

        if (propertyName.equals("happenedIn") ||
                propertyName.equals("isLocatedIn")) {return "yago:"+propertyName;}

    return null;
    }
}
