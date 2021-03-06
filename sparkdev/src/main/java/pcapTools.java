/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;
import java.lang.NullPointerException;

/*Imports needed to use the pcap4j library*/
import java.io.EOFException;
import java.util.concurrent.TimeoutException;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapHandle.TimestampPrecision;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.*;

/*Imports for filewriter / buffer writer/reader*/
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Shaneka Lewis
 */
public class pcapTools {
    /*Variables to organize data by node and time of capture*/
    String nodeLetter;
    String sessionNum;
    
    /*Global variable that allows all methods to use this scanner object*/
    Scanner userInput = new Scanner(System.in); 
	
    pcapTools() {
        nodeLetter = "Z";
	sessionNum = "999";
    }
	
    pcapTools(String nodeLetter, String sessionNum) {
        this.nodeLetter = nodeLetter;
        this.sessionNum = sessionNum;	
    }

    public void setNodeLetter(String nodeLetter) {
        this.nodeLetter = nodeLetter;
    }

    public void setSessionNum(String sessionNum) {
        this.sessionNum = sessionNum;
    }
    
    public void getAllSessionData() throws PcapNativeException, NotOpenException, IOException {
        String fileName;
        String ans;
        
        ArrayList<String[]> collectedIPs = new ArrayList<String[]>();
        
        do{
            System.out.println("Enter file name: ");
            fileName = userInput.nextLine();
            System.out.println(fileName);
            
            System.out.println("Enter node leter: ");
            nodeLetter = userInput.next();
            System.out.println("Enter session number: ");
            sessionNum = userInput.next();
            
            PcapHandle handle;
            try {
                handle = Pcaps.openOffline(fileName, TimestampPrecision.NANO);
                
            } catch (PcapNativeException e) {
                handle = Pcaps.openOffline(fileName);
            }
            
            for (int i = 0; i < 1000; i++) {
                try {
                    Packet packet = handle.getNextPacketEx();
                    collectedIPs.add(new String[3]);
                    collectedIPs.get(i)[0] = fileName;
                    try {
                        collectedIPs.get(i)[1] = packet.get(EthernetPacket.class).getHeader().getSrcAddr().toString();
                    } catch (java.lang.NullPointerException e) {
                        collectedIPs.get(i)[1] = "-----------";
                    }
                    
                   collectedIPs.get(i)[2] = "no_timestamp";
                    
                    System.out.println(packet);
                } catch (TimeoutException e) {
                } catch (EOFException e) {
                    System.out.println("EOF");
                    break;
                }
            }
            
            System.out.println("Do you want to open another file? y/n");
            ans = userInput.next();
            userInput.nextLine();
            
        } while (ans.equals("y"));
        
        BufferedWriter output = new BufferedWriter(
                    new FileWriter("allSessionData"+ "_session" + sessionNum + ".txt"));
            
        for (int i = 0; i < collectedIPs.size(); i++) {
            output.write(collectedIPs.get(i)[1] + " ");
            output.write(nodeLetter + " " + sessionNum + " ");
            output.write(collectedIPs.get(i)[2] + " ");
            output.write(collectedIPs.get(i)[0] + "\n");
        }

        output.close();
        System.out.println("File created");
    }
    
    public void getUniqueIPs() throws PcapNativeException, NotOpenException, IOException {
        /*Variable to hold the filepath/filename*/
        String fileName;
        /*Variable to hold user answer*/
        String ans;
        /*ArrayList of string arrays that will hold the desire information*/
        HashMap<String, ArrayList<String>> collectedIPs = new HashMap< String, ArrayList<String>>();
        ArrayList<String> sourceIPs;
        
        /*The do while loop ensure that the code within the loop executes at least once*/
        do{
            System.out.println("Enter file name: ");
            /*To get the entire line of input from user*/
            fileName = userInput.nextLine();
      
            System.out.println(fileName); 
            
            /*The object to open and deal with pcap files*/
            PcapHandle handle;
            try {
                handle = Pcaps.openOffline(fileName, TimestampPrecision.NANO);
                
            } catch (PcapNativeException e) {
                handle = Pcaps.openOffline(fileName);
            }
            
            //Needs to be in for loop so that it can get all the pcaps in a file
            for (int i = 0; i < 1000; i++) {
                //ArrayList that will contain the node letter, time, and file name for some IP
                sourceIPs = new ArrayList<String>();
                try {
                    Packet packet = handle.getNextPacketEx();
           
                    try {
                        sourceIPs.add(packet.get(EthernetPacket.class).getHeader().getSrcAddr().toString());
                    } catch (java.lang.NullPointerException e) {
                        sourceIPs.add("-----------");
                    }
                    
                    sourceIPs.add(nodeLetter);
                    sourceIPs.add("no_timestamp");
                    sourceIPs.add(fileName);
                    collectedIPs.put(sourceIPs.get(0), sourceIPs);
                    
                    System.out.println(packet);
                } catch (TimeoutException e) {
                } catch (EOFException e) {
                    System.out.println("EOF");
                    break;
                }
            }
            
            /*File writer information*/
            BufferedWriter output = new BufferedWriter(
                    new FileWriter("uniqueMAC_session" + sessionNum + ".txt"));
            
            for ( String key : collectedIPs.keySet()) {
                for (int j = 0; j < 4; j++) {
                    output.write(collectedIPs.get(key).get(j) + " ");
                }
                output.write("\n");
            }
            
            output.close();
            System.out.println("File created");   
            
            System.out.println("Do you want to open another file? y/n");
            ans = userInput.next();
            userInput.nextLine();
            
        } while (ans.equals("y"));
    }
    //--------------------------------------------------------------------------
    public void getDestinations() throws PcapNativeException, NotOpenException, IOException{
        //Get unique destinationIPs
        
        String fileName;
        String ans;
        ArrayList<String[]> destIPs = new ArrayList<String[]>();
        
        do{
            System.out.println("Enter file name: ");
            fileName = userInput.nextLine();
            System.out.println(fileName); 
            
            PcapHandle handle;
            try {
                handle = Pcaps.openOffline(fileName, TimestampPrecision.NANO);
                
            } catch (PcapNativeException e) {
                handle = Pcaps.openOffline(fileName);
            }
            
            for (int i = 0; i < 100; i++) {
                try {
                    Packet packet = handle.getNextPacketEx();
                    destIPs.add(new String[2]);
                    destIPs.get(i)[0] = fileName;
                    try {
                        destIPs.get(i)[1] = packet.get(IpV4Packet.class).getHeader().getSrcAddr().toString();
                    } catch (java.lang.NullPointerException e) {
                        destIPs.get(i)[1] = "-----------";
                    }
                    System.out.println(packet);
                } catch (TimeoutException e) {
                } catch (EOFException e) {
                    System.out.println("EOF");
                    break;
                }
            }
            
            BufferedWriter output = new BufferedWriter(new FileWriter("destinations_session" + sessionNum + ".txt"));
            
            for (int i = 0; i < destIPs.size(); i++) {
                output.write(destIPs.get(i)[0] + " ");
                output.write(destIPs.get(i)[1] + "\n");
            }
            
            output.close();
            System.out.println("File created");
            
            System.out.println("Do you want to open another file? y/n");
            ans = userInput.next();
            userInput.nextLine();
            
        } while (ans.equals("y"));
        
        //display destinations here
    }
	
    public void traceIP(String ipAddress) throws FileNotFoundException {

        Scanner s = new Scanner(new File(""));
        String check, mac, node, time, name;
        ArrayList<ArrayList<String>> arr = new ArrayList<ArrayList<String>>();

        while(s.hasNext()){
            check = s.next();

            if(check.equals(ipAddress)){
                ArrayList<String> line = new ArrayList<String>();
                mac = check;
                line.add(mac);
                check = s.next();
                node = check;
                line.add(node);
                check = s.next();
                time = check;
                check = s.next();
                line.add(time);
                name = check;
                line.add(name);
                System.out.println(line);
                arr.add(line);

            } else {
                s.next();
                s.next();
                s.next();
            }
        }

        for(int i = 0; i < arr.size(); i++){
            System.out.println(arr.get(i));
        }


    }
	
    public void getPath(String ipAddress) {
        //method to sort information from traceIP
	//Will list the order in which an IP address was found in the nodes
	//Will have to differentiate between filenames "nodeA_sessionX" - only look charAt(4)
	//output info
        
    }

    public void getPopulationSession(){
            	
    	 
    }
    
    public void getPopulationNode() {
    	
    }

    public void getMostTravelled(){
        
    }

}