import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Scanner;

public class finalTest {
    /**
     * This is the test file made with multiple assertions to check the functionality of the code.
     * */
    Government g = new Government("govermentconfig.txt");
    MobileDevice device1 = new MobileDevice("device1.txt", g);
    MobileDevice device2 = new MobileDevice("device2.txt", g);
    MobileDevice device3 = new MobileDevice("device3.txt", g);
    MobileDevice device4 = new MobileDevice("device4.txt", g);
    MobileDevice device5 = new MobileDevice("device5.txt", g);
    MobileDevice device6 = new MobileDevice("device6.txt", g);

    String d1 = read("device1.txt");
    String d2 = read("device2.txt");
    String d3 = read("device3.txt");
    String d4 = read("device4.txt");
    String d5 = read("device5.txt");
    String d6 = read("device6.txt");

    private static String read(String confifile) {
        String address = null;
        String device_name = null;
        Scanner scan;
        try {
            File file = new File(confifile);
            scan = new Scanner(file);
            while (scan.hasNext()) {
                String s[] = scan.nextLine().split("=");
                if (s[0].equalsIgnoreCase("address"))
                    address = s[1];
                if (s[0].equalsIgnoreCase("deviceName"))
                    device_name = s[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return device_name + address;
    }
    @Test
    public void testRecordTestResult(){
        g.recordTestResult("test0", 40, true);
        g.recordTestResult("test9", 40, true);
    }
    @Test
    public void test1(){
        device1.recordContact("device2address2", 20 ,15);
        device2.recordContact("device1address1" ,20, 15);

        device1.recordContact("device3address3", 20 ,15);
        device3.recordContact("device1address1" ,20, 15);

        device1.recordContact("device4address4", 20 ,14);
        device4.recordContact("device1address1" ,20, 14);

        device2.recordContact("device3address3", 20 ,15);
        device3.recordContact("device2address2" ,20, 15);

        device5.recordContact("device6address6", 20 ,10);
        device6.recordContact("device5address5" ,20, 10);

        g.recordTestResult("tes1", 30, true);
        device1.positiveTest("test1");


        assertFalse(device1.synchronizeData());
        assertTrue(device2.synchronizeData());
        assertTrue(device3.synchronizeData());
        assertTrue(device4.synchronizeData());
        assertFalse(device5.synchronizeData());
        assertFalse(device5.synchronizeData());

        assertEquals(g.findGatherings(20,2,5,0.5f), 1);
    }


    @Test
    public void test2(){
        device1.recordContact("",0,0);
        assertFalse(device1.synchronizeData());
    }

    @Test
    public void testgathering(){
        assertEquals(g.findGatherings(25,0,0,0),-1);
    }

    @Test
    public void test4(){
        g.recordTestResult("test8", 25, true);
        device1.positiveTest("test8");
        assertFalse(device1.synchronizeData());
    }

    @Test
    public void test5(){
        device2.positiveTest("test9");
        device2.synchronizeData();
        g.recordTestResult("test9", 30, true);
    }

    @Test
    public void test6(){
        Government g2 = new Government("");
        g2.findGatherings(40,3,5,0.5f);
        g2.recordTestResult("test91", 46, true);
    }
    @Test
    public void test7(){
        MobileDevice device150 = new MobileDevice("",g);
        device150.positiveTest("test91");
        device150.recordContact(d1,0,0);
    }
    @Test
    public void test8(){
        device1.recordContact(d2,15,20);
        device2.recordContact(d1,15,20);

        device1.recordContact(d3,15,20);
        device3.recordContact(d1,15,20);

        device1.recordContact(d4,15,20);
        device4.recordContact(d1,15,20);

        device1.recordContact(d5,15,20);
        device5.recordContact(d1,15,20);

        device2.recordContact(d3,15,20);
        device3.recordContact(d2,15,20);

        device2.recordContact(d5,15,20);
        device5.recordContact(d2,15,20);

        device3.recordContact(d4,15,20);
        device4.recordContact(d3,15,20);

        device5.recordContact(d6,15,20);
        device6.recordContact(d5,15,20);

        g.recordTestResult("test79", 15,true );
        device1.positiveTest("test79");
        assertFalse(device1.synchronizeData());
        assertTrue(device2.synchronizeData());
        assertTrue(device3.synchronizeData());
        assertTrue(device4.synchronizeData());
        assertTrue(device5.synchronizeData());
        assertFalse(device6.synchronizeData());
        assertEquals(g.findGatherings(15,3,15, 0.7f), 3);
    }
}
