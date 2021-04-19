import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

public class Government {
    String db;
    String userName;
    String password;

    class contact {
        String individual;
        int contactDate;
        int duration;
    }

    Government(String configFile){
        /**
         * This method works with the file reading.
         * It reads the database , user and password from the config file.
         * */
        File file = new File(configFile);
        Scanner scan;
        try {
            scan = new Scanner(file);
            while (scan.hasNextLine()){
                String str[] = scan.nextLine().split("=");
                if (str[0].equalsIgnoreCase("database"))
                    this.db = str[1];
                if (str[0].equalsIgnoreCase("user"))
                    this.userName = str[1];
                if (str[0].equalsIgnoreCase("password"))
                    this.password = str[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean mobileContact(String initiator, String contactInfo) {
        /**
         * This function deals with the majority of the database work going into the system
         * It inserts the data into the info_person database with the query.
         * It inserts the data into the info_contacts database with the query.
         * With the help of test_date we could check for the 14 days weather they are over or not
         * */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder govBuilder = null;
        Connection conn = null;
        Statement s = null ;
        ResultSet r = null;
        boolean criticalContact = false;
        ArrayList<contact> retcontacts = new ArrayList<contact>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306/doshi", this.userName, this.password);
            s=conn.createStatement();
            govBuilder = factory.newDocumentBuilder();
            Document doc = govBuilder.parse(new InputSource(new StringReader(contactInfo)));
            doc.getDocumentElement().normalize();
            String id = doc.getElementsByTagName("id").item(0).getTextContent();
            if (!id.equalsIgnoreCase(initiator)){
                return false;
            }
            NodeList testList = doc.getElementsByTagName("testlist");
            PreparedStatement p =null;
            if (testList.getLength() > 0 && testList != null){
                for (int i = 0; i < testList.getLength(); i++) {
                    Node node = testList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE){
                        Element element = (Element) node;
                        String testHash = element.getElementsByTagName("test").item(0).getTextContent();
                        String query = "insert into info_person(id,testHash)"+"values(?,?)";
                        p= conn.prepareStatement(query);
                        p.setString(1, id);
                        p.setString(2, testHash);
                        p.executeUpdate();
                        p.close();
                    }
                }
            }
            NodeList nodeList = doc.getElementsByTagName("contact_details");
            PreparedStatement p1 = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE){
                    Element element = (Element) node;
                    contact c = new contact();
                    String contactId = element.getElementsByTagName("contact_id").item(0).getTextContent();
                    int date = Integer.parseInt(element.getElementsByTagName("contact_date").item(0).getTextContent());
                    int duration = Integer.parseInt(element.getElementsByTagName("contact_duration").item(0).getTextContent());
                    String query1 = "insert into info_contacts(id_initiator,id,date,duration)"+"values(?,?,?,?)";
                    p1=conn.prepareStatement(query1);
                    p1.setString(1,id);
                    p1.setString(2,contactId);
                    p1.setInt(3,date);
                    p1.setInt(4,duration);
                    c.individual = contactId;
                    c.contactDate = date;
                    c.duration = duration;
                    retcontacts.add(c);
                    p1.executeUpdate();
                    p1.close();
                }
            }
            for (contact cid:retcontacts) {
                r = s.executeQuery("select * from test, info_person where test.test_hash = info_person.test_hash and test.test_result = '"+true+"' and info_person.id = '"+cid.individual+"'");
                while (r.next()) {
                    int cdate = r.getInt("test_date");
                    if (Math.max(cid.contactDate, cdate) - Math.min(cid.contactDate, cdate) <= 14) {
                        criticalContact = true;
                    }
                }
            }
            r.close();
            s.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return criticalContact;
    }
    private @NotNull
    String bytesToHex(byte[] hash) {
        /**
         * This function is specifically created to convert bytes to Hex code
         * Reference had been taken from,
         * https://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal/50846880
         * */
        int n = hash.length;
        StringBuilder hexString = new StringBuilder(n*2);
        for (int i = 0; i < n; i++){
            String byteHex = Integer.toHexString(0xff & hash[i]);
            if (hexString.length() == 1) {
                hexString.append('0');
            }
            hexString.append(byteHex);
        }
        return hexString.toString();
    }
    public void recordTestResult(String test_Hash, int date, boolean result){
        /**
         * This functions runs the specific queries to add the test record weather the test is positive or not
         * We are using query to fed the data into the database
         * */
        if (date > 0 || !(test_Hash == null)){
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
                final byte[] hashBytes = digest.digest(test_Hash.getBytes(StandardCharsets.UTF_8));
                test_Hash = bytesToHex(hashBytes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            String testResult;
            if (result = true){
                testResult = "true";
            }
            else {
                testResult = "false";
            }
            Connection conn = null;
            Statement s = null;
            PreparedStatement ps = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306/doshi", this.userName, this.password);
                s = conn.createStatement();
                String query = "insert into test(test_hash, test_date, test_result)" + "values(?,?,?)";
                ps = conn.prepareStatement(query);
                ps.setString(1,test_Hash);
                ps.setInt(2,date);
                ps.setString(3,testResult);
                ps.executeUpdate();
                ps.close();
                s.close();;
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public int findGatherings (int date, int min_Size, int min_Time, float density) {
        /**
         * This function finds the gathering number
         * date is counted from 1st january 2021.
         * min_Size is the size of the cluster
         * min_time is the time spent with the potential possitive person
         * density is derived from special methematic equations n(n-1)/2 where colculating c/m
         * Here we are using two sets to save the information and compute on that specific information
         * IF the duration is more than the min_time then the cluster is added into the adjecency list
         * At the end we will be comparing the output with the density given according to the function
         *
         * */
        if (date < 0 || min_Size<0 || min_Time<0 || density<0.0f){
            return 0;
        }
        int final_numberofGatherings = 0;
        ArrayList<ArrayList<String>> final_adjecency_List = new ArrayList<ArrayList<String>>();
        Set<String> contact_Set = new HashSet<String>();
        ArrayList<String> contact_List = null;
        Connection conn = null;
        try{
            Statement s = null;
            PreparedStatement ps = null;
            ResultSet r = null;
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306/doshi", this.userName, this.password);
            s = conn.createStatement();
            r= s.executeQuery("Select * from info_contacts where date = '"+date+"'");
            while (r.next()){
                contact_Set.add(r.getString("id_initiator"));
            }
            contact_List = new ArrayList<String>(contact_Set);
            r.close();
            s.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i =0; i<contact_List.size(); i++) {
            final_adjecency_List.add(new ArrayList<String>());
        }
        for (String c:contact_List) {
            try {
                Statement s1 = null;
                ResultSet r1 = null;
                s1 = conn.createStatement();
                r1 = s1.executeQuery("Select * from info_contacts where date = '"+date+"' and id_initiator = '"+c+"'");
                while(r1.next()) {
                    if (r1.getInt("duration") > min_Time){
                        final_adjecency_List.get(contact_List.indexOf(c)).add(r1.getString("id"));
                    }
                }
                r1.close();
                s1.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        Set<Set<String>> evaluated_Set = new HashSet<Set<String>>();
        for (int i = 0; i < contact_List.size(); i++) {
//            System.out.println("Evaluator Set " +evaluated_Set);
//            System.out.println("Initiator" +contact_List.get(i));
            Set<String> set1 = new HashSet<String>(final_adjecency_List.size());
            set1.add(contact_List.get(i));
            for (int j = 0; j < contact_List.size(); j++) {
                if (contact_List.get(i).equalsIgnoreCase(contact_List.get(j)))
                    continue;
//                System.out.println("Contact" +contact_List.get(j));
                Set<String> set2 = new HashSet<String>(final_adjecency_List.get(j));
                set2.add(contact_List.get(j));
//                System.out.println(set2);
                set2.retainAll(set1);
//                System.out.println("After" +set2);
                int gathering_Size = set2.size();
//                System.out.println("Gathering Size" +gathering_Size);
                if (gathering_Size < min_Size)
                    continue;
                ArrayList<String> setList = new ArrayList<String>(set2);
                int pair = 0;
                if (!evaluated_Set.contains(set2)) {
                    for (int l = 0; l < setList.size(); l++) {
                        for (int k = 0; k < setList.size(); k++) {
                            if (setList.get(l).equalsIgnoreCase(setList.get(k)))
                                continue;
                            if (final_adjecency_List.get(contact_List.indexOf(setList.get(l))) != null && final_adjecency_List.get(contact_List.indexOf(setList.get(l))).contains(setList.get(k))){
                                pair = pair + 1;
                            }
                        }
                    }
                }
//                System.out.println("paira"+pair);
                float c2 = (gathering_Size*(gathering_Size-1));
                float c = c2/2;
//                System.out.println(pair/c);
                if ((pair/c) < density) {
                    final_numberofGatherings = final_numberofGatherings + 1;
//                    System.out.println("Number" +final_numberofGatherings);
                    evaluated_Set.add(set2);
                }

            }
        }
        return final_numberofGatherings;
    }
}
