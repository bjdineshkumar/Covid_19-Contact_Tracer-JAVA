import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MobileDevice {



    Map<String,ArrayList<Individual>> recordIndividual = new HashMap<>(); //Hashmap to record the individuals who were near us
    List<String> testHashes = new ArrayList<>();                         //ArrayList to store the positive testHashes of the individual
    String Address = null;                                              //To save the device network address
    String DeviceName=null;                                            //To save the device name
    Government contact = null;                                        //To store the Government class object


    /** Method to read the config file and Government object */
    /** In case of error, the error is printed to the screen */
    MobileDevice(String configFile, Government contactTracer) {
        
        //Reading the file and getting the network address and the device name
        //Try and Catch block is used to catch the File not found exception
        try {
            FileReader fr = new FileReader(configFile);
            Scanner scanner = new Scanner(fr);


            //Preliminary check to find the lines in the file
            int noOfLine = checkEmpty(configFile);
            //Validation to check if the file is not empty and does not contain more than 2 line
            if (noOfLine == 2) {

                while(scanner.hasNextLine()) {
                    String temp = scanner.nextLine();

                    if (temp.contains("address")) {
                        String[] NetAddress = temp.split("=");
                        Address = NetAddress[1];
                    }
                    if (temp.contains("deviceName")) {
                        String[] MobileName = temp.split("=");
                        DeviceName = MobileName[1];
                    }

                }

                contact = contactTracer; //Saves the Government class object
            } else {
                System.out.println("Please check the Configuration file");
            }

            }catch(IOException ex){
                System.out.println("The file does not exist!");
            }


    }

    /** Method to record the people who came in contact with the Mobile device */
    boolean recordContact(String individual, int date, int duration){


        if(individual.length() >0 ) {
            individual = getSHA(individual);
        }

        long a = 0;
        Date today = new Date(); //Getting today's date
        Date date1;
        String sDate1="01-01-2021";
        try {
            date1=new SimpleDateFormat("dd-MM-yyyy").parse(sDate1);
            long diff = today.getTime() - date1.getTime();
            a = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            return false;
        }

        //Input validation for individual, date and duration
        /** If we encounter the same individual again we record both the individuals */
        if(individual.matches("[a-zA-Z0-9]+") && date > 0 && duration >0 && individual.length() >0 && date <= a) {

            ArrayList<Individual> array = new ArrayList<>();

            if (!recordIndividual.containsKey(individual)) {


                Individual TempIndividual = new Individual(date, duration);
                array.add(TempIndividual);
                recordIndividual.put(individual,array);

            } else {

                Individual TempIndividual = new Individual(date, duration);
                recordIndividual.get(individual).add(TempIndividual);
            }

        } else{
            return false;
        }

        return true;
    }

    /** Method to record the positive test of the individual */
    boolean positiveTest(String testHash){

        //Testing if the testHash is already in the database (We don't add duplicate test hashes)
        boolean iftestpresent = contact.ifTestHashPresent(testHash);


        //Validating if the input is an alphanumeric string
        if(testHash.matches("[a-zA-Z0-9]+") && !iftestpresent) {

            testHashes.add(testHash);
            return true;
        } else return false;

    }

    /** Method which package's all of the information in the mobile device into a formatted string and send
     that information to the government through the Government object’s mobileContact method */
    /** The format of the string is explained in the document */
    boolean synchronizeData(){

        //Hashing the Network address and the device name
        String initiator = getSHA(Address+DeviceName);
        System.out.println(initiator);

        //Formatting the string of the contacts to send to the government
        StringBuilder formattedtemp = new StringBuilder();

        //Adding the test hashes to the arraylist
        for (String testHash : testHashes) {

            formattedtemp.append("individualtesthash=").append(testHash).append("\n");

        }

        /** Testing for adding duplicate contacts */
        for (Map.Entry map : recordIndividual.entrySet()) {
            String key = (String)map.getKey();

            ArrayList<Individual> m = (ArrayList<Individual>) map.getValue();
            for(int i = 0 ; i< m.size() ; i++) {

                Individual temp = m.get(i);

                formattedtemp.append("\n<contact>" + "\n\tname=").append(key).append("\n\tdays=").append(temp.date).append("\n\tduration=").append(temp.duration).append("\n</contact>\n");

            }
        }

        //Calling the method in government to detect if they have been near any covid positive person
        boolean contacted = contact.mobileContact(initiator, formattedtemp.toString());

        // After we have passed the data from Mobile Device to Government we clear the data from Mobile Device

        testHashes.clear();
        recordIndividual.clear();

        if(contacted){
            System.out.println("Omg i have corona");
        }

        return contacted;
    }

    /** NOTE: All the methods below are helper methods */


    /** Helper method for hashing the details of the mobile device*/
    private String getSHA(String input)
    {

        try {

            // GetInstance method is called with hashing of SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Using the digest() method to calculate message digest of the given (Address+DeviceName) and return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum (Sign Function) representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert the signum message digest into hex value
            String hashtext = no.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            return hashtext; //Return the converted hashtext
        }

        // To catch wrong specification of the algorithm
        catch (NoSuchAlgorithmException e) {
            System.out.println("Exception:"
                    + " incorrect algorithm: " + e);

            return "";
        }
    }

    /** Helper method of MobileDevice to get the number of lines in the given file */
    private int checkEmpty(String fr) {
        int lines = 0;
        File file = new File(fr);
        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                sc.nextLine();
                lines++;
            }
            sc.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        return lines;
    }

    /** Custom class helper for recording the details of individual */
    private class Individual{
        int date;
        int duration;
        Individual(int date, int duration){
            this.date = date;
            this.duration = duration;
        }
    }
}
