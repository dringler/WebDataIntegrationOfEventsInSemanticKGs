import java.lang.reflect.Array;

/**
 * Created by curtis on 13/10/16.
 */
public class QueryProcessor {
    public class DataArray {
        public boolean d;
        public boolean w;
        public boolean y;
        public String cat;
        public int fY;
        public int tY;
    }

    public String getData(boolean d, boolean w, boolean y, String cat, int fY, int tY) {
        System.out.println("DATA RECEIVED");
        System.out.println(cat);
        return "returnData";
    }
    public String getData(String test) {
        System.out.println("TEST SUCCESSFUL");
        return "test successful";
    }

}
