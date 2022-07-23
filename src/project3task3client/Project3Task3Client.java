/**
 * @classname: Project3Task3Client.Java
 * @author: Rofin A
 */
/*-----------------------------------------------------------------------------* 
*  Purpose: This class serves as rest webservice client for the blockchain service*
* it sends HTTP request to server and receieve the response regarding transactions
*-------------------------------------------------------------------------------*/

package project3task3client;
//import libraries
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rofin
 */
public class Project3Task3Client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean is_true = true;
        int uChoice;
        int status = 0;
        Scanner uinp = new Scanner(System.in);
        //---- Display the menu option
        while (is_true) {
            System.out.println();// add an empty line (for output formatting)
            System.out.println("1. Add a transaction to the blockchain.");
            System.out.println("2. Verify the blockchain.");
            System.out.println("3. View the blockchain.");
            System.out.println("4. Exit.");
            //--- As per the user choice make calls to appropriate operation
            uChoice = uinp.nextInt();
            switch (uChoice) {
                case 1: {
                    //--- Read user inputs
                    System.out.println("Enter difficulty :");
                    int d = uinp.nextInt();
                    System.out.println("Enter transaction :");
                    uinp.nextLine();
                    String transaction = uinp.nextLine();
                    long start = System.currentTimeMillis();
                    //-- sign the transaction
                    String signed_tr = transaction + "#" + sign(transaction);
                    // Add the block to block chain by calling webservice operation

                    //----- Send HTTP request to server
                    try {
                        // Make an HTTP request using particular URL- this request will hit the doPost method
                        // in server, which will verify the sign and then if found valid add the block to the
                        //chain
                        URL url = new URL("http://localhost:35039/Project3Task3Server/blockchain/");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        // set request method to POST 
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        try ( // Pass the difficulty and signed transaction as paramater to the server
                                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream())) {
                            out.write(d + "," + signed_tr);
                        }

                        // Recieve HTTP response code from server
                        status = conn.getResponseCode();
                        //close the connection
                        conn.disconnect();
                    } // handle exceptions // handle exceptions
                    catch (MalformedURLException ex) {
                        System.out.println("Error accesing the URL:" + ex.getMessage());
                    } catch (IOException ex) {
                        System.out.println("Error communicating with server :" + ex.getMessage());
                    }
                    // Check the response code to detect problem, in case of failure report message
                    if (status != 200) {
                        System.out.println(status);
                        System.out.println("Transaction corrupted and could not be completed");
                        break;
                    }
                    //log the execution time
                    long stop = System.currentTimeMillis();
                    System.out.println("Total execution time to add this block was " + (stop - start) + " milliseconds");
                    break;
                }
                //-- Check the chain for validity 
                case 2: {
                    BufferedReader br = null;
                    long start = System.currentTimeMillis();
                    //-- Call the doGet method with isValid as the parameter,
                    //-- with isValid as the path server will invoke the isChainValid
                    //-- method to asses the validit of the block
                    doGet("isValid");
                    long stop = System.currentTimeMillis();
                    System.out.println("Total execution time to add this block was " + (stop - start) + " milliseconds");
                    break;
                }
                case 3: {
                    //-- Call the doGet method with blank parameter,which will send
                    //-- a HTTP request with no path variable, server will inturn 
                    //-- return details of all the blocks in the chain
                    doGet("");
                    break;
                }
                case 4: {
                    is_true = false;
                    System.exit(0);
                }
            }
        }
    }

/*---------------------------------------------------------------------------
Method - Sign
--- Signature:
--- Return:
 String cipher - Signature of the transaction data
--- Purpose :
This method computes the hash of the transaction data and encypts with the private
Key 
----------------------------------------------------------------------------*/
    private static String sign(String transaction) {
        //--- Private key and exponent for signing
        BigInteger d = new BigInteger("339177647280468990599683753475404338964037287357290649639740920420195763493261892674937712727426153831055473238029100340967145378283022484846784794546119352371446685199413453480215164979267671668216248690393620864946715883011485526549108913");
        BigInteger n = new BigInteger("2688520255179015026237478731436571621031218154515572968727588377065598663770912513333018006654248650656250913110874836607777966867106290192618336660849980956399732967369976281500270286450313199586861977623503348237855579434471251977653662553");
        BigInteger cipher = null;
        try {
            //--- get the byte value of the transaction
            byte[] bytesOfTransaction = transaction.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            //Generate hash
            byte[] bigDigest = md.digest(bytesOfTransaction);

            byte[] sign = new byte[bigDigest.length + 1];
            sign[0] = 0;
            System.arraycopy(bigDigest, 0, sign, 1, sign.length - 1);
            // From the digest, create a BigInteger
            BigInteger msg = new BigInteger(sign);
            // encrypt the digest with the private key
            cipher = msg.modPow(d, n);
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Error in signing :" + ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Byte conversion of transaction :" + ex.getMessage());
        }
        // return this as a big integer string
        return cipher.toString();
    }
/*---------------------------------------------------------------------------
 Method - doGet
 --- Signature:
    String path - Variable attached to the URL for the server to determine 
    the course of action
 --- Return:
 --- Purpose :
This method creates an HTTP request for the server and sent it across. Receives 
the resonse from the server and renders it in the console.    
 ----------------------------------------------------------------------------*/
    public static void doGet(String path) {
        BufferedReader br = null;
        try {
            long start = System.currentTimeMillis();
            // Make an http request to invoke isChainValid() method from server (doGet)
            URL url = new URL("http://localhost:35039/Project3Task3Server/blockchain/"+path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Set request method to Get
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/plain");
            String output = "";

            // Read any msgs passed by the server and display it in the console
            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
            // Terminate the established connection
            conn.disconnect();
        } catch (IOException ex) {
            System.out.println("Error getting response from server:" + ex.getMessage());
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(Project3Task3Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
