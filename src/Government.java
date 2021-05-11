import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyStore;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Government {

    String Url = null;
    String UserName= null;
    String Password=null;


    /** Method to read the config file to get the database, username and password */
    Government(String configFile) {

        try {
            //Reading the file and getting the database, username and password
            FileReader fr = new FileReader(configFile);
            Scanner sc = new Scanner(fr);
            //Checking the number of lines in the file
            int noOfLine = checkEmpty(configFile);

            //Checking if the file contains only 3 lines
            if(noOfLine==3) {
                while (sc.hasNextLine()) {
                    String temp = sc.nextLine();
                    if (temp.contains("database")) {
                        String[] DatabaseUrl = temp.split("=");
                        Url = DatabaseUrl[1];
                    } else if (temp.contains("user")) {
                        String[] TempUserName = temp.split("=");
                        UserName = TempUserName[1];
                    } else if(temp.contains("password")){
                        String[] TempPassword = temp.split("=");
                        Password = TempPassword[1];
                    }
                }
            }
        }catch (IOException a) {
            System.out.println("File does not exist");
        }


    }


    /** Method called my MobileDevice's synchronizeData() method */
    boolean mobileContact(String initiator, String contactInfo){

        String IndividualTestHash;
        boolean contact = false;

        //Parsing the contact information to get the individual results from them

        String delimSpace = "</contact>"; //String used for splitting
        String[] arr1  = contactInfo.split(delimSpace);
        for (String uniqVal1 : arr1) {
            Scanner scanner = new Scanner(uniqVal1);

            //Variable to store teh contact information
            String Name = null;
            int Days = 0;
            int Duration = 0;
            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();
                //Getting the positive testhash of the individual of the mobile device
                if (line.contains("individualtesthash")) {
                    String[] Temp = line.split("=");
                    IndividualTestHash = Temp[1];

                    //Inserting the test hash to the individual in the database
                    //Checking if the test hash is not null
                    if (IndividualTestHash != null) {
                        updateData(initiator, IndividualTestHash); //Updates the database with the Individual name to the respective test hash
                    }

                }
                if (line.contains("name")) {
                    String[] NetAddress = line.split("=");
                    Name = NetAddress[1];

                } else if (line.contains("days")) {
                    String[] NetAddress = line.split("=");
                    Days = Integer.parseInt(NetAddress[1]);

                } else if (line.contains("duration")) {
                    String[] NetAddress = line.split("=");
                    Duration = Integer.parseInt(NetAddress[1]);
                }

                //Record the contact in the database to survive restarts of the program
                if (Name != null && Days != 0 && Duration != 0) {

                    //Checking if the particular pair of contact is already present
                    int tempDuration = CheckifContactPresent(initiator,Name,Days);

                    if(tempDuration!=0){

                        //We will just update the duration of the contact in the table
                        Duration = Duration+tempDuration;
                        updateContactDuration(initiator,Name,Days,Duration);

                    } else insertData(initiator, Name, Days, Duration);


                }

            }
            scanner.close();

        }
        //Finding if the individual has been in contact with someone who has tested positive for covid

        ArrayList<RecordContact> TempHM = GCI(initiator); //Arraylist to store the contacts of the individuals
        //Iterate through the array list and see if that person had covid and was in contact within 14 days of contact
        for (RecordContact TempRC : TempHM) {

            String ContactNames = TempRC.ContactName;
            int DateOfContact = TempRC.Date;
            int lowerLimit = 1;

            //Checking if the contact for the current individual is already reported
            boolean ContactCheck = checkIfContactReported(initiator, ContactNames, DateOfContact);

            if (!ContactCheck) {


                if (DateOfContact > 14) {
                    lowerLimit = DateOfContact - 14;
                }

                ArrayList<String> hash = new ArrayList<>();
                boolean contact1 = getData(ContactNames, DateOfContact, lowerLimit, hash);

                if (contact1) {
                    String tesTHash = hash.get(0);
                    //Update the contact has been reported on that day for that individual
                    updateContactonDay(initiator, ContactNames, DateOfContact);
                    return true;
                }
            }

        }

        return contact;
    }


    /** Method to record the test results */
    public boolean recordTestResult(String testHash, int date, boolean result) {

        //Checking if the test hash is already present in the database
        boolean testPresent = ifTestHashPresent(testHash);


        Statement statement = null;
        Connection connection = null;


        Date today = new Date(); //Getting today's date
        long a = 0;

        Date date1= null;

        String sDate1="01-01-2021";
        try {
            date1=new SimpleDateFormat("dd-MM-yyyy").parse(sDate1);
            long diff = today.getTime() - date1.getTime();
            a = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        } catch (ParseException e) {
            return false;
        }


        //Validation for testHash and date
        if(testHash.matches("[a-zA-Z0-9]+") && date > 0 && date <= a && !testPresent) {

            // Load a connection library between Java and the database
            try {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            } catch (Exception ex) {
                System.out.println("Error connecting to jdbc");
            }

            try {
                // Connect to the Dal database
                connection = DriverManager.getConnection(Url, UserName, Password);


                // Create a statement
                statement = connection.createStatement();


                // Query to insert the test details in the database
                statement.executeUpdate("insert into TESTTABLE (testhash,dates,result) " + "values ('" + testHash + "','" + date + "','" + result + "')");


            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else{
            return false;
        }
        return true;
    }


    /** Method to find the number of gathering in a day which are large */
    int findGatherings(int date, int minSize, int minTime, float density){

        int gathering = 0;

        //To find the current date in days
        long currentDay = 0;
        Date today = new Date(); //Getting today's date
        Date date1;
        String sDate1="01-01-2021";
        try {
            date1=new SimpleDateFormat("dd-MM-yyyy").parse(sDate1);
            long diff = today.getTime() - date1.getTime();
            currentDay = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            return 0;
        }

        if(date > 0 && date <= currentDay && minSize >= 0 && minTime >= 0  && density >= 0) {

            ArrayList<String> individuals = new ArrayList<>();
            ArrayList<pairs> FinalmatchedPairs = new ArrayList<>();
            Set<pairs> FinalEG = new HashSet<>();
            Map<String, String> pairs = new HashMap<>();
            ArrayList<pairs> Pairs = new ArrayList<>();
            uniquePair(individuals, date);

            //ArrayList to help find out the duplicate gatherings
            ArrayList<Set<String>> FinalGathering = new ArrayList<>();

            //Finding the unique pairs using the individuals
            for (int i = 0; i < individuals.size(); i++) {

                for (int j = i + 1; j < individuals.size(); j++) {
                    pairs p = new pairs(individuals.get(i), individuals.get(j), 0);
                    Pairs.add(p);
                }

            }

            for (int k = 0; k < Pairs.size(); k++) {

                pairs pp = Pairs.get(k);
                String Contact1 = pp.IndividualName;
                String Contact2 = pp.ContactName;

                //We are finding if the two individuals have been in contact
                boolean bothcontacted = wasInContact(Contact1, Contact2, date);
                if (bothcontacted) {


                    //To store the pairs of each individuals
                    ArrayList<pairs> matchedPairs = new ArrayList<>();
                    ArrayList<pairs> matchedPairs1 = new ArrayList<>();

                    //To store the contact individuals of each individual
                    ArrayList<String> pairsofindividual = new ArrayList<>();
                    ArrayList<String> pairsofindividual1 = new ArrayList<>();


                    //Getting the unique pairs which match with the date given
                    getContactPair(date, matchedPairs, pairsofindividual, Contact1);
                    getContactPair(date, matchedPairs1, pairsofindividual1, Contact2);


                    ArrayList<pairs> matchedPairsFinal = new ArrayList<>();

                    ArrayList<String> contactsofindividuals = new ArrayList<>(pairsofindividual);

                    contactsofindividuals.retainAll(pairsofindividual1);


                    // Find common individuals both contacted and finding the set
                    for (pairs temp : matchedPairs) {
                        String a = temp.IndividualName;
                        String b = temp.ContactName;

                        //For adding individual 1
                        if (a.contains(Contact1)) {
                            for (int i = 0; i < contactsofindividuals.size(); i++) {
                                if (b.contains(contactsofindividuals.get(i))) {
                                    matchedPairsFinal.add(temp);
                                }
                            }
                        }

                        for (pairs temp1 : matchedPairs1) {

                            String a1 = temp1.IndividualName;
                            String b1 = temp1.ContactName;

                            if (a.contains(a1)) {

                                if (b.contains(b1)) {

                                    matchedPairsFinal.add(temp1);
                                }
                            }
                            if (a.contains(b1)) {

                                if (b.contains(a1)) {

                                    matchedPairsFinal.add(temp1);
                                }
                            }

                        }
                    }

                    //Final non-duplicate set of the pair
                    for (pairs temp1 : matchedPairs1) {

                        String a1 = temp1.IndividualName;
                        String b1 = temp1.ContactName;

                        //For adding individual 2
                        if (a1.contains(Contact2)) {
                            for (int i = 0; i < contactsofindividuals.size(); i++) {
                                if (b1.contains(contactsofindividuals.get(i))) {
                                    matchedPairsFinal.add(temp1);
                                }
                            }
                        }
                    }

                    //Using a set to find the number of individuals in the set
                    Set<String> set = new HashSet<>();

                    for (pairs temp1 : matchedPairsFinal) {

                        String a1 = temp1.IndividualName;
                        String b1 = temp1.ContactName;

                        set.add(a1);
                        set.add(b1);
                    }

                    boolean value = false;

                    //Checking if the gathering is a duplicate
                    for (Set<String> tempSet : FinalGathering) {

                        //Comparing if the gathering is a duplicate
                         if(tempSet.equals(set)){
                             value = true;
                         }

                    }

                    if(!value){

                    //Checking if the set has minSize individuals
                    if (set.size() >= minSize) {

                        int c = 0;

                        //Finding if pair of individuals  contacted one another on the given date for at least minTime minutes
                        for (pairs temp1 : matchedPairsFinal) {

                            String a1 = temp1.IndividualName;
                            String b1 = temp1.ContactName;
                            int duration = temp1.duration;
                            if (duration >= minTime) {
                                c++;

                            }
                        }

                        int n = set.size();
                        int n1 = n - 1;

                        float m = (float) (n * n1) / 2;

                        if ((c / m) > density) {

                            gathering++;

                            FinalGathering.add(set);

                        }
                    }
                }
                }
            }
        }
        System.out.println(gathering);
         return gathering;
    }


    /** NOTE: All the methods below are helper methods */


    /** Method to get the number of lines in the given file */
    private int checkEmpty(String fr){
        int lines =0;
        File file = new File(fr);
        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                sc.nextLine();
                lines++;
            }
            sc.close();
        }catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

        return lines;
    }


    /** Method to check if the pair of contact is aldready present */
    private int CheckifContactPresent(String Individual, String Contact, int DateofContact){

        Statement statement = null;
        ResultSet resultSet = null;
        Connection connection = null;

        int dur = 0;


        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Query to find the contacts of the particular individual
            resultSet = statement.executeQuery("select duration from CONTACT where IndividualName='"+Individual+"' and ContactName='"+Contact+"' and DateofContact='"+DateofContact+"'");

            while(resultSet.next()){

                //If there is a contact we get the duration of the contact
                dur = resultSet.getInt("duration");

            }

            resultSet.close();
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return dur;
    }

    /** Method to update the duration of the contacts */
    private void updateContactDuration(String Individual, String Contact, int Days , int Duration){
        Statement statement = null;
        Connection connection = null;


        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Query to update the duration of the contact
            statement.executeUpdate("update CONTACT set duration ='"+Duration+"' where IndividualName='"+Individual+"' and ContactName='"+Contact+"' and DateofContact='"+Days+"'");

            connection.close();


        }

        catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }


    /** Record the contact data of the individual in the database */
    private void insertData(String initiator,String name, int date,int duration){
        Statement statement = null;
        Connection connection = null;


        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Query to insert the contact information of the individual
            statement.executeUpdate("insert into CONTACT (IndividualName,ContactName,DateofContact,duration,Reported) "+"values ('"+initiator+"','"+name+"','"+date+"','"+duration+"','false')");

            connection.close();


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /** Update data for the individual name with the respective test hash*/
    private void updateData(String individual,String individualtesthash){
        Statement statement = null;
        Connection connection = null;


        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Updates the Individual name with the respective test hash
            statement.executeUpdate("update TESTTABLE set individualname ='"+individual+"' where testhash='"+individualtesthash+"'");

            connection.close();


        }

        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    /** Method used to update the reported column to true when we report about a contact on the given date*/

   private void updateContactonDay(String Indiviudal,String Contact,int DateOfContact){
       Statement statement = null;
       Connection connection = null;


       // Load a connection library between Java and the database
       try {
           Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
       } catch (Exception ex) {
           System.out.println("Error connecting to jdbc");
       }

       try {
           // Connect to the Dal database
           connection = DriverManager.getConnection(Url, UserName, Password);


           // Create a statement
           statement = connection.createStatement();


           // Run the query to update the table with the results
           statement.executeUpdate("update CONTACT set Reported ='true' where IndividualName='"+Indiviudal+"' and ContactName='"+Contact+"' and DateofContact='"+DateOfContact+"'");

           connection.close();

       }
       catch (SQLException throwables) {
           throwables.printStackTrace();
       }

   }

    /** Method to check if the contact is already reported */

    boolean checkIfContactReported(String Individual, String Contact, int DateofContact){

        Statement statement = null;
        ResultSet resultSet = null;
        Connection connection = null;

        HashMap<String,Integer> hm = new HashMap<>();


        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Query to find the contacts of the particular individual
            resultSet = statement.executeQuery("select Reported from CONTACT where IndividualName='"+Individual+"' and ContactName='"+Contact+"' and DateofContact='"+DateofContact+"'");

            while(resultSet.next()){

                boolean report = resultSet.getBoolean("Reported");
                //Returning true if the contact has been reported
                if(report){
                    return true;
                }

            }

            resultSet.close();
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }

    /** Method to get the contact of the individuals [CGI-Get contact individuals] */
    private ArrayList<RecordContact> GCI(String initiator){
        Statement statement = null;
        ResultSet resultSet = null;
        Connection connection = null;

        ArrayList<RecordContact> hm = new ArrayList<>();


        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Query to find the contacts of the particular individual
            resultSet = statement.executeQuery("select ContactName,DateofContact from CONTACT where IndividualName='"+initiator+"'");

            while(resultSet.next()){


                String name =  resultSet.getString("ContactName");
                int ContactDate = resultSet.getInt("DateofContact");
                RecordContact RC = new RecordContact(name,ContactDate);
                hm.add(RC);

            }

            resultSet.close();
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return hm;
    }



    /** Get the contacts from the database */
    private boolean getData(String name,int contactDate,int limit, ArrayList<String> Hash){
        Statement statement = null;
        ResultSet resultSets = null;
        Connection connection = null;


        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Query to get the testhash, date and result of the contact ( If there are two test hash it will consider both of them )
            resultSets = statement.executeQuery("select testhash,dates,result from TESTTABLE where individualname='"+name+"'");

            if(resultSets.next()){


                String test =  resultSets.getString("testhash");
                int days = resultSets.getInt("dates");
                boolean testresult = resultSets.getBoolean("result");
                //We are checking if the contact has been tested positive before and after 14 days of contact
                //We are doing 14 days after contact since the contact could have had Covid when we were in contact and tested postive the next day or so
                if(days >= limit && days <= contactDate+14){


                    Hash.add(test);
                    if(testresult){
                        return true;
                    }
                }
            }

            resultSets.close();
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }


    /** Finding individuals who had a contact on the given date  */
    private void uniquePair(ArrayList<String> individuals, int date){
        Statement statement = null;
        ResultSet resultSet = null;
        Connection connection = null;



        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Query to get the individual if they have had contact of the given date
            resultSet = statement.executeQuery("select distinct individualname from CONTACT where DateofContact='"+date+"'");

            while(resultSet.next()){

                String ind = resultSet.getString("individualname");


                individuals.add(ind);
            }

            resultSet.close();
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    /** Method to get the contacts of the individual on the given date */

    private void getContactPair(int date,ArrayList<pairs> matchedPairs, ArrayList<String> pairscontacted, String contact1){
        Statement statement = null;
        ResultSet resultSet = null;
        Connection connection = null;



        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Query to get the contacted individuals of the given individual on that date
            resultSet = statement.executeQuery("select IndividualName,ContactName,DateofContact,duration from CONTACT where DateofContact='"+date+"' and IndividualName='"+contact1+"'");

            while(resultSet.next()){

                String individual = resultSet.getString("IndividualName");
                String contact    = resultSet.getString("ContactName");
                int duration = resultSet.getInt("duration");
                pairs p = new pairs(individual,contact,duration);
                matchedPairs.add(p);
                //Adding to the linked list
                pairscontacted.add(contact);

            }

            resultSet.close();
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }

    /** Class to find if the two individuals were in contact on the given date*/
    private boolean wasInContact(String Contact1, String Contact2, int date ){

        Statement statement = null;
        ResultSet resultSet = null;
        Connection connection = null;



        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Run the query to update the table with the results
            resultSet = statement.executeQuery("select duration from CONTACT where DateofContact='"+date+"' and IndividualName='"+Contact1+"' and ContactName='"+Contact2+"'");

            while(resultSet.next()){


                int duration = resultSet.getInt("duration");
                if(duration > 0){
                    return true;
                }


            }

            resultSet.close();
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }


    /** Class to record information about pairs */
    private class pairs{

        String IndividualName;
        String ContactName;
        int duration;
        pairs(String IndividualName, String ContactName,int duration){
            this.IndividualName = IndividualName;
            this.ContactName = ContactName;
            this.duration = duration;

        }
    }

    /** Class to record the infomration of the contact */
    private class RecordContact{

        String ContactName;
        int Date;
        RecordContact(String ContactName, int Date){
            this.ContactName = ContactName;
            this.Date = Date;

        }

    }



    /** Helper method for postitiveTestHash() in MobileDevice class to find id the test hash is already in the table */

    public boolean ifTestHashPresent(String testHash){

        Statement statement = null;
        ResultSet resultSet = null;
        Connection connection = null;



        // Load a connection library between Java and the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error connecting to jdbc");
        }

        try {
            // Connect to the Dal database
            connection = DriverManager.getConnection(Url, UserName, Password);


            // Create a statement
            statement = connection.createStatement();


            // Query to help find if the testhash is already in the database
            resultSet = statement.executeQuery("select dates from TESTTABLE where testhash = '"+testHash+"'");
            int date = 0;

            while(resultSet.next()){

                date = resultSet.getInt("dates");
                if(date != 0 ){
                    return true;
                }

            }

            resultSet.close();
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;

    }

}
