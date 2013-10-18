/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.jdnssd;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 *
 * @author etsai
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java -jar JDnsSD.jar [service] [udp|tcp] [domain]");
            System.exit(1);
        }
        
        try {
            NetProtocol protocol= NetProtocol.valueOf(args[1].toUpperCase());
            ServiceLocator locator= new ServiceLocator(args[0], protocol, args[2]);
            
            System.out.println(String.format("Query: %s", locator.getQueryString()));
            System.out.println(String.format("TXT Record: %s", locator.getTXTRecord()));
            while(true) {
                SRVRecord record= locator.getNextSRVRecord();
                System.out.println(String.format("SRV Record: %s", record));
            }
        } catch (NamingException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
