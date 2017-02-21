import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Daniel on 10/02/17.
 */
public class UserInput {

    /**
     * Get user input: Declare if testing is true (use sample data set) or false (use full data set)
     * @return testing
     * @throws IOException
     */
    public boolean getDatasetUserInput() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //init parameters
        boolean testing = true;
        boolean newInput = true;
        System.out.print("Test on sample set? Enter 'y' or 'n': ");
        //get user input from console
        while (newInput) {
            String userInput = br.readLine();
            if (userInput.equals("y")) {
                newInput = false;
            } else if (userInput.equals("n")) {
                testing = false;
                newInput = false;
            } else {
                System.out.print("Please enter 'y' or 'n'.");
            }
        }
        return testing;
    }
    /**
     * Get user input: Sample size
     * @return s sample size (0 if no sampling should be used)
     * @throws IOException
     */
    public int getSampleSizeUserInput() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
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
        return s;
    }
}
