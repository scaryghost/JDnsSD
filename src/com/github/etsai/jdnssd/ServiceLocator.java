/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.jdnssd;

import java.util.Calendar;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;

/**
 *
 * @author etsai
 */
public class ServiceLocator {
    private final String queryString, txtRecord;
    private final TreeMap<Integer, SortedSet<SRVRecord>> srvRecords;
    
    public ServiceLocator(String service, NetProtocol protocol, String domain) throws NamingException {
        queryString= String.format("_%s._%s.%s", service, protocol, domain);
        srvRecords= new TreeMap<>();
        
        DirContext ctx= new InitialLdapContext();
        ctx.addToEnvironment("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        ctx.addToEnvironment("java.naming.provider.url", "dns:");
        
        Attributes attrs= (Attributes) ctx.getAttributes(queryString, new String[] { "SRV", "TXT" });
        for(NamingEnumeration<?> e= attrs.get("SRV").getAll(); e.hasMoreElements();) {
            SRVRecord record= new SRVRecord((String)e.nextElement());
            if (!srvRecords.containsKey(record.getPriority())) {
                srvRecords.put(record.getPriority(), new TreeSet());
            }
            srvRecords.get(record.getPriority()).add(record);
        }
        txtRecord= (String)attrs.get("TXT").get(0);
    }
    
    public String getQueryString() {
        return queryString;
    }
    
    public String getTXTRecord() {
        return txtRecord;
    }
    
    public SRVRecord getNextSRVRecord() {
        if (srvRecords.isEmpty()) {
            throw new RuntimeException("All SRV records have been used");
        }
        
        Map.Entry<Integer, SortedSet<SRVRecord>> firstEntry= srvRecords.firstEntry();
        
        int totalWeight= 0;
        for(SRVRecord record: firstEntry.getValue()) {
            totalWeight+= record.getWeight();
        }
        
        Random r= new Random();
        r.setSeed(Calendar.getInstance().getTimeInMillis());
        int targetWeight= r.nextInt(totalWeight + 1);
        
        int accumWeight= 0;
        SRVRecord it= null;
        for(SRVRecord record: firstEntry.getValue()) {
            accumWeight+= record.getWeight();
            if (accumWeight >= targetWeight) {
                it= record;
                break;
            }
        }
        
        firstEntry.getValue().remove(it);
        if (firstEntry.getValue().isEmpty()) {
            srvRecords.remove(firstEntry.getKey());
        }
        
        return it;
    }
}
