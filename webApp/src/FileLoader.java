/**
 * Created by Daniel on 10/02/17.
 */
public class FileLoader {
    /**
     * Get the file paths
     * @param testing true: use sample file paths for testing
     * @param gsFiles
     * @return paths String array: [0]=dbpedia, [1]=yago, [2]=gold standard
     */
    public String[] getPaths(boolean testing, boolean gsFiles) {
        String[] paths = new String[3];
        if (testing) {
            paths[0] = "./data/dbpedia_events_s_5.xml";
            paths[1] = "./data/yago_events_s_5.xml";
            paths[2] = "./data/dbpedia_2_yago_s.tsv";
        } else {
            if (gsFiles) {
                paths[0] = "./data/dbpedia_events_gs_100.xml";
                paths[1] = "./data/yago_events_gs_100.xml";
                paths[2] = "./data/sameAs_combined_with_negative_100_all.tsv";
            } else {
                paths[0] = "./data/dbpedia_events.xml";
                paths[1] = "./data/yago_events.xml";
                paths[2] = "./data/sameAs_combined.tsv";
            }
        }
        return paths;
    }
}
