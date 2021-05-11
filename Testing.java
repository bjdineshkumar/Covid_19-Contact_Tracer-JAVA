import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.Assert.assertEquals;


public class Testing {


    /**
     * Table of CONTACT used for findGatherings() testing to detect gathering
     * All the expected output for findGatherings() is based on this table
     *
     | Individual Name | Contact Name | Date of Contact | Duration |
     |-----------------|--------------|-----------------|----------|
     | a               | b            | 20              | 2        |
     | a               | c            | 20              | 2        |
     | a               | d            | 20              | 2        |
     | b               | a            | 20              | 2        |
     | b               | c            | 20              | 2        |
     | c               | d            | 20              | 2        |
     | d               | c            | 20              | 2        |


     Make sure the created table "TESTTABLE" for adding the testhash is empty or the
     results will be returned as False for some input validations beacuse
     duplicate entries won't be allowed

     */


    /** Test cases for recordContact() method in MobileDevice class */

    @Nested
    @DisplayName("Ability to add the contacts")
    class RecordContacts {

        /** Creating the Government object */
        Government G = new Government("databaseconfigfile.txt");

        @Nested
        @DisplayName("Input Validation")
        class Input {

            @Test
            @DisplayName("Normal execution of the method")
            public void normalRecordContact(){
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(true,m.recordContact(("10.21.32.4.5"+"iphone11"),70 ,20));
            }

            @Test
            @DisplayName("The date is 0 ")
            public void dateZero(){
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(false,m.recordContact(("10.21.32.4.5"+"iphone12"),0 ,20));
            }

            @Test
            @DisplayName("The date is negative")
            public void dateNegative(){
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(false,m.recordContact(("10.21.32.4.5"+"iphone12"),-20 ,20));
            }

            @Test
            @DisplayName("The duration is 0")
            public void durationZero(){
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(false,m.recordContact(("10.21.32.4.5"+"iphone12"),70 ,0));
            }

            @Test
            @DisplayName("The duration is negative")
            public void durationNegative(){
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(false,m.recordContact(("10.21.32.4.5"+"iphone12"),70 ,-20));
            }

        }

        @Nested
        @DisplayName("Boundary cases")
        class Boundaries {

            @Test
            @DisplayName("The date is greater than the current date")
            public void dateGreater(){
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(false,m.recordContact(("10.21.32.4.5"+"iphone12"),200 ,20));
            }

            @Test
            @DisplayName("The date is a small integer")
            public void dateSmall(){
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(true,m.recordContact(("10.21.32.4.5"+"iphone12"),1 ,20));
            }


        }
    }


    /** Test case for positiveTest() method in MobileDevice Class */

    @Nested
    @DisplayName("Ability to save the positive TestHash of the individual")
    class PositiveTest {

        /** Creating the Government object */
        Government G = new Government("databaseconfigfile.txt");

        @Nested
        @DisplayName("Input Validation")
        class Input {
            @Test
            @DisplayName("Normal execution of the method")
            public void normalPositiveRecord(){
                //Make sure the testhash inputed is not in the database already or false would be returned
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(true,m.positiveTest("2021PCR0012"));
            }

            @Test
            @DisplayName("Positive test hash is not alphanumeric")
            public void notAlphanumericPositiveRecord(){
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(false,m.positiveTest("#abcd1234"));
            }

            @Test
            @DisplayName("Positive test hash is already in the databse")
            public void alreadyPresent(){
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(false,m.positiveTest("2021PCR001234"));
            }

        }

        @Nested
        @DisplayName("Boundary cases")
        class Boundaries {

            @Test
            @DisplayName("Input Positive test hash is a small or large alphanumeric string")
            public void lengthofPositiveTest(){
                String LongAlpha = "ABNBSHSYHjbjbjsgdjsbdsigdbkdj27382739827382838792638726873bdbdbhbjkbdaknlkjdgieaokho";
                String SmallAlpha = "a1";
                MobileDevice m  = new MobileDevice("configfile.txt",G);
                assertEquals(true,m.positiveTest(LongAlpha));
                assertEquals(true,m.positiveTest(SmallAlpha));
            }

        }
    }


    /** Test cases for recordTestResult() method in Government class */

    @Nested
    @DisplayName("Ability to save the positive TestHash of the individual")
    class recordTestResult {

        /** Creating the Government object */
        Government G = new Government("databaseconfigfile.txt");

        @Nested
        @DisplayName("Input Validation")
        class Input {
            @Test
            @DisplayName("Normal execution of the method")
            public void normalRecordTestResult(){

                assertEquals(true,G.recordTestResult("2021PCR001234",90,false));
            }

            @Test
            @DisplayName("The date is 0 ")
            public void dateZero(){
                assertEquals(false,G.recordTestResult("2021PCR001234",0,false));
            }

            @Test
            @DisplayName("The date is negative")
            public void dateNegative(){

                assertEquals(false,G.recordTestResult("2021PCR001234",-30,false));
            }

        }

        @Nested
        @DisplayName("Boundary cases")
        class Boundaries {

            @Test
            @DisplayName("Input  test hash is a small or large alphanumeric string")
            public void lengthofPositiveTest(){
                String LongAlpha = "ABNBSHSYHjbjbjsgdjsbdsigdbkdj27382739827382838792638726873bdbdbhbjkbdaknlkjdgieaokho";
                String SmallAlpha = "a1";
                assertEquals(true,G.recordTestResult(LongAlpha,30,false));
                assertEquals(true,G.recordTestResult(SmallAlpha,30,false));
            }

            @Test
            @DisplayName("The date is greater than the current date")
            public void dateGreater(){
                assertEquals(false,G.recordTestResult("2021PCR001234",200,false));
            }

            @Test
            @DisplayName("The date is a small integer")
            public void dateSmall(){
                assertEquals(true,G.recordTestResult("2021PCR001234",1,false));
            }

        }
    }



    /** Test cases for findGatherings() method in Government class */

    @Nested
    @DisplayName("Ability to detect the gathering on given date")
    class findLargeGatherings {

        /** Creating the Government object */
        Government G = new Government("databaseconfigfile.txt");

        @Nested
        @DisplayName("Input Validation")
        class Input {
            @Test
            @DisplayName("Normal execution of the method")
            public void normalFindGatherings(){

                assertEquals(2,G.findGatherings(20,3,1,0.5f));
            }

            @Test
            @DisplayName("The date is 0 ")
            public void dateZero(){
                assertEquals(0,G.findGatherings(0,3,1,0.5f));
            }

            @Test
            @DisplayName("The date is negative")
            public void dateNegative(){

                assertEquals(0,G.findGatherings(-1,3,1,0.5f));
            }

            @Test
            @DisplayName("The size is 0")
            public void minSizeZero(){

                assertEquals(3,G.findGatherings(20,0,1,0.5f));
            }

            @Test
            @DisplayName("The size is negative")
            public void minSizeNegative(){

                assertEquals(0,G.findGatherings(20,-3,1,0.5f));
            }

            @Test
            @DisplayName("The time is negative")
            public void minTimeNegative(){

                assertEquals(0,G.findGatherings(20,3,-1,0.5f));
            }

            @Test
            @DisplayName("The density is 0")
            public void densityZero(){

                assertEquals(2,G.findGatherings(20,3,1,0));
            }

            @Test
            @DisplayName("The density is negative")
            public void densityNegative(){

                assertEquals(0,G.findGatherings(20,3,1,-10));
            }

        }

        @Nested
        @DisplayName("Boundary cases")
        class Boundaries {

            @Test
            @DisplayName("Input date is a small number")
            public void dateSmallNumber(){

                assertEquals(0,G.findGatherings(1,3,1,0.5f));
            }

            @Test
            @DisplayName("Input date is greater than current date")
            public void dateGreaterthanCurrent(){

                assertEquals(0,G.findGatherings(200,3,1,0.5f));
            }

            @Test
            @DisplayName("Input size is small and a very large number")
            public void minSizeTest(){

                assertEquals(3,G.findGatherings(20,1,1,0.5f));
                assertEquals(0,G.findGatherings(20,1000000000,1,0.5f));

            }

            @Test
            @DisplayName("Input time is small and very large number")
            public void minTimeTest(){

                assertEquals(3,G.findGatherings(20,1,1,0.5f));
                assertEquals(0,G.findGatherings(20,1,1000000000,0.5f));

            }

            @Test
            @DisplayName("Input density is small and max which is 1 in our case")
            public void densityTest(){

                assertEquals(3,G.findGatherings(20,1,1,0.1f));
                assertEquals(0,G.findGatherings(20,1,1,1.0f));

            }


        }
    }

}
