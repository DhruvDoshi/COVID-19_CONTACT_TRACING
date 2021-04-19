import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class MobileDevice {
    /*
     *
     * */

    String id;
    String address;
    String deviceName;
    Set<String> positiveTestHash = new HashSet<String>();
    List<contact> contactList = new ArrayList<contact>();

    Government contactTracer;
    class contact{
        String individual;
        int contactDate;
        int duration;
    }

    MobileDevice (String configFile, Government contactTracer){
        /**
         * This method works with the file reading.
         * If the structure of the scanned file is as per the regulation then this will save them
         * info address and devicename as the global variabes.
         * This function will return the id which is concatination of deviceName and address
         * */
        File file = new File(configFile);
        Scanner scan;
        try{
            scan = new Scanner(file);
            while (scan.hasNextLine()){
                String s[] = scan.nextLine().split("=");
                if (s[0].equalsIgnoreCase("address")){
                    this.address = s[1];
                }
                if (s[0].equalsIgnoreCase("deviceName")){
                    this.deviceName = s[1];
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.id = this.deviceName + this.address;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashBytes = digest.digest(this.id.getBytes(StandardCharsets.UTF_8));
            this.id = bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.contactTracer = contactTracer;
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
    public void recordContact(String individual, int date, int duration) {
        /**
         * This functions records or track the user for each and every contact they made with teh nearest device
         * This will record then locally
         * and each time it will add the duration on to it
         * */
        if (!(date<0 || duration<0.0f)) {
            boolean found = false;
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
                final byte[] hashBytes = digest.digest(this.id.getBytes(StandardCharsets.UTF_8));
                this.id = bytesToHex(hashBytes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            if (this.contactList.size() > 0)
                for (contact x: this.contactList){
                    if (x.individual.equalsIgnoreCase(individual) && x.contactDate == date){
                        found = true;
                        x.duration = x.duration + duration;
                    }
                }
            if (found == false) {
                contact c = new contact();
                c.contactDate = date;
                c.duration = duration;
                c.individual = individual;
            }
        }
    }

    public void positiveTest(String testHash){
        /**
         * IF the user is updated with the covid possitive then the entry needed to be updated
         * this single function will update the user specific information to the possitive
         * */
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashBytes = digest.digest(this.id.getBytes(StandardCharsets.UTF_8));
            this.id = bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.positiveTestHash.add(testHash);
    }
    public boolean synchronizeData(){
        /**
         * This method is used to synchronise the data at the end by creating the XML file which would be further used
         * This XML file holds all the required data for the upcomming functions
         * */
        String xmldata = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBouilder;
        boolean contactValue = false;
        try {
            dBouilder =dbFactory.newDocumentBuilder();
            Document doc = dBouilder.newDocument();
            Element root = doc.createElement("data");
            doc.appendChild(root);
            Element mobileId = doc.createElement("id");
            mobileId.appendChild(doc.createTextNode(this.id));
            root.appendChild(mobileId);
            if (this.positiveTestHash.size() > 0) {
                Element testList = doc.createElement("testlist");
                root.appendChild(testList);
                for (int i = 0; i < this.positiveTestHash.size(); i++) {
                    Element tests = doc.createElement("test");
                    tests.appendChild(doc.createTextNode(new ArrayList<String>(this.positiveTestHash).get(i)));
                    testList.appendChild(tests);
                }
            }
            int n = this.contactList.size();
            Element contacts = doc.createElement("contact_list");
            root.appendChild(contacts);
            if (n>0) {
                /**
                 * This loop puts the information for whole n into the xml formated file which could be further used to retrive the data

                 */
                for (int i = 0; i < n; i++) {
                    contact c = this.contactList.get(i);
                    Element contactDetails = doc.createElement("contact_details");
                    contacts.appendChild(contactDetails);
                    Element contactId = doc.createElement("contact_id");
                    contactId.appendChild(doc.createTextNode(c.individual));
                    contactDetails.appendChild(contactDetails);
                    Element contactDate = doc.createElement("contact_date");
                    contactDate.appendChild(doc.createTextNode(String.valueOf(c.contactDate)));
                    contactDetails.appendChild(contactDate);
                    Element contactDuration = doc.createElement("contact_duration");
                    contactDuration.appendChild(doc.createTextNode(String.valueOf(c.duration)));
                    contactDetails.appendChild(contactDuration);
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StringWriter result = new StringWriter();
            transformer.transform(source, new StreamResult(result));
            xmldata = result.getBuffer().toString();
//            System.out.println(xmldata);
            contactValue = this.contactTracer.mobileContact(this.id,xmldata);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactValue;
    }
}
