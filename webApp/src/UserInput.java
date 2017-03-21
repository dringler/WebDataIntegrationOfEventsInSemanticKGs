import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Daniel on 10/02/17.
 */
public class UserInput {

    /**
     * Get user input
     * @param q Question to show user
     * @param t value for true
     * @param f value for false
     * @return boolean
     * @throws IOException
     */
    private boolean getBooleanUserInput(String q, String t, String f) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //init parameters
        boolean b = true;
        boolean newInput = true;
        System.out.print(q + " Enter '"+t+"' or '"+f+"': ");
        //get user input from console
        while (newInput) {
            String userInput = br.readLine();
            if (userInput.equals(t)) {
                newInput = false;
            } else if (userInput.equals(f)) {
                b = false;
                newInput = false;
            } else {
                System.out.print("Please enter '"+t+"' or '"+f+"': ");
            }
        }
        return b;
    }

    /**
     * Get integer user input greater or equal to 0
     * @param q Question to show user
     * @return int
     * @throws IOException
     */
    public int getIntUserInput(String q) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int s = 0;
        boolean newInput = true;
        System.out.print(q);
        while (newInput) {
            String userInput = br.readLine();
            try {
                s = Integer.parseInt(userInput);
                if (s>=0) {
                    newInput = false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number: ");
            }

        }
        return s;
    }

    /**
     * Get double user input between 0.0 and 1.0
     * @param q Question to show user
     * @return double
     * @throws IOException
     */
    public double getDoubleUserInput(String q) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        double s = 0;
        boolean newInput = true;
        System.out.print(q);
        while (newInput) {
            String userInput = br.readLine();
            try {
                s = Double.parseDouble(userInput);
                if (s>=0 && s<=1) {
                    newInput = false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number between 0 and 1: ");
            }

        }
        return s;
    }



    /**
     * Get user input: Declare if testing is true (use sample data set) or false (use full data set)
     * @return testing
     * @throws IOException
     */
    public boolean getDatasetUserInput() throws IOException {
        return getBooleanUserInput("Test on sample set?", "y", "n");
    }
    /**
     * Get user input: Sample size
     * @return s sample size (0 if no sampling should be used)
     * @throws IOException
     */
    public int getSampleSizeUserInput() throws IOException {
        return getIntUserInput("Sample data set size (enter '0' fo no sampling): ");
        /*BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int s = 0;
        boolean newInput = true;
        System.out.print("Sample data set size (enter '0' fo no sampling): ");
        while (newInput) {
            String userInput = br.readLine();
            try {
                s = Integer.parseInt(userInput);
                if (s>=0) {
                    newInput = false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }

        }
        return s;*/
    }

    /**
     * Get user input whether to get the best parameter or to measure the runtime for a blocking method
     * @return
     * @throws IOException
     */
    public boolean getBestParameter() throws IOException {
        return getBooleanUserInput("Get best parameter or measure runtime", "p", "r");
    }

    /**
     * Get user input for blocking steps.
     * 0: Just BlockBuilding, 1: BlockBuilding and BlockFiltering, 2: BlockBuilding, BlockFiltering, and MetaBlocking
     * @return
     * @throws IOException
     */
    public int getBlockingStep() throws IOException {
        ArrayList<Integer> validOptions = new ArrayList<>();
        validOptions.add(0);//BlBu
        validOptions.add(1);//BlFi
        validOptions.add(2);//MeBl
        boolean newInput = true;
        System.out.print("Please enter a number. BlockBuilding: 0 , BlockFiltering: 1, MetaBlocking: 2: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (newInput) {
            try {
                int userInput = Integer.parseInt(br.readLine());
                if (validOptions.contains(userInput)) {
                    newInput = false;
                    return userInput;
                } else {
                    System.out.print("Please enter a number between 0 and 2: ");
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Please enter a valid number.");
            }

        }
        return -1;
    }

    /**
     * Load gold standard files.
     * @return
     * @throws IOException
     */
    public boolean getGsUserInput() throws IOException {
        return getBooleanUserInput("Get gold standard files?", "y", "n");
    }

    /**
     * Get user input for blocking method
     * 0: StandardBlocking, 1: AttributeClustering
     * @return
     * @throws IOException
     */
    public int getBlockingMethod() throws IOException {
        ArrayList<Integer> validOptions = new ArrayList<>();
        validOptions.add(0);//StBl
        validOptions.add(1);//ACl
        boolean newInput = true;
        System.out.print("Please enter a number. StandardBlocking: 0, AttributeClustering: 1: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (newInput) {
            try {
                int userInput = Integer.parseInt(br.readLine());
                if (validOptions.contains(userInput)) {
                    newInput = false;
                    return userInput;
                } else {
                    System.out.print("Please enter a number between 0 and 1.");
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Please enter a valid number.");
            }
        }
        return -1;
    }

    public double getThreshold() throws IOException {
        return getDoubleUserInput("Please enter the threshold for the scaled Levenshtein MR: ");
    }
}
