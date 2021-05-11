/** Main function used for creating the Government and MobileDevice object.
 *  It is also used for manual testing of the Government() and MobileDevice() method
 */

public class Main {

    public static void main(String args[]) {



        // Creating the government object
        Government G = new Government("databaseconfigfile.txt");



        //Creating mobile device objects
        MobileDevice m  = new MobileDevice("configfile.txt",G);
        MobileDevice m1 = new MobileDevice("configfile1.txt",G);
        MobileDevice m2 = new MobileDevice("configfile2.txt",G);
        MobileDevice m3 = new MobileDevice("configfile3.txt",G);
        MobileDevice m4 = new MobileDevice("configfile4.txt",G);


        m.synchronizeData();


        //Testing
        m.recordContact(("10.21.32.4.5"+"iphone11"),5 ,20);
        m.recordContact(("10.2.3.42.51"+"iphone12"),20,10);  //For testing positive
        m.recordContact(("10.22.32.42.5"+"samsungS7"),40 ,5);
        m.recordContact(("10.27.38.54.5"+"samsungs11"),40 ,20);



        m.synchronizeData();

//        m1.positiveTest("pcr12");
//        G.recordTestResult("pcr12",14,true);
//        m1.synchronizeData();
//
//        m2.positiveTest("pcr22");
//        G.recordTestResult("pcr22",25,true);
//        m2.synchronizeData();
//
//        m3.positiveTest("pcr32");
//        G.recordTestResult("pcr32",35,true);
//        m3.synchronizeData();
//
//        m4.positiveTest("pcr42");
//        G.recordTestResult("pcr42",30,true);
//        m4.synchronizeData();

        m.recordContact(("10.21.32.4.5"+"iphone11"),6 ,20);
        m.recordContact(("10.2.3.42.51"+"iphone12"),21,10);  //For testing positive
        m.recordContact(("10.22.32.42.5"+"samsungS7"),41 ,5);
        m.recordContact(("10.27.38.54.5"+"samsungs11"),42 ,20);


//        m.synchronizeData();
//        m.synchronizeData();
//        m.synchronizeData();
//        m.synchronizeData();


        //Recording for device 1 & synchronizing
         m.recordContact(("10.21.32.4.5"+"iphone11"),5 ,20);
        m.recordContact(("10.2.3.42.51"+"iphone12"),15 ,10);  //For testing positive
        m.recordContact(("10.22.32.42.5"+"samsungS7"),45 ,5);
        m.recordContact(("10.27.38.54.5"+"samsungs11"),5 ,20);

        m.synchronizeData();


        //Recording for device 2 & synchronizing
        m1.recordContact(("10.2.3.4.5"+"iphone10"),5 ,20);
        m1.recordContact(("10.2.3.42.51"+"iphone12"),63 ,10);
        m1.recordContact(("10.22.32.42.5"+"samsungS7"),49 ,2); //For testing positive
        m1.recordContact(("10.27.38.54.5"+"samsungs11"),5 ,30);
        m1.synchronizeData();


        //Recording for device 3 & synchronizing
        m2.recordContact(("10.2.3.4.5"+"iphone10"),15 ,10);
        m2.recordContact(("10.21.32.4.5"+"iphone11"),63 ,10);
        m2.recordContact(("10.22.32.42.5"+"samsungS7"),45 ,10);
        m2.recordContact(("10.27.38.54.5"+"samsungs11"),6 ,32);
        m2.positiveTest("2021PCR001111");
        G.recordTestResult("2021PCR001111",3,true);
        m2.synchronizeData();


        //Recording for device 4 & synchronizing
        m3.recordContact(("10.2.3.4.5"+"iphone10"),45,40);
        m3.recordContact(("10.21.32.4.5"+"iphone11"),79 ,90);
        m3.recordContact(("10.2.3.42.51"+"iphone12"),45 ,10);
        m3.recordContact(("10.27.38.54.5"+"samsungs11"),5,20);
        m3.positiveTest("2021PCR002222");
        G.recordTestResult("2021PCR002222",40,true);
        m3.synchronizeData();


        //Recording for device 5 & synchronizing
        m4.recordContact(("10.2.3.4.5"+"iphone10"),5 ,19); //Below the minTime
        m4.recordContact(("10.21.32.4.5"+"iphone11"),5 ,30);
        m4.recordContact(("10.2.3.42.51"+"iphone12"),6 ,32); // For testing positive
        m4.recordContact(("10.22.32.42.5"+"samsungS7"),5 ,20);
        m4.synchronizeData();








    }

}
