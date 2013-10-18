/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.jdnssd;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;

/**
 *
 * @author etsai
 */
public class ServiceLocator {
    public class SRVRecordIterator implements Iterator<SRVRecord> {
        private TreeMap<Integer, TreeSet<SRVRecord>> srvRecords;
        private HashSet<SRVRecord> usedRecords;
        
        public SRVRecordIterator(TreeMap<Integer, TreeSet<SRVRecord>> records) {
            usedRecords= new HashSet<>();
            srvRecords= records;
        }
        
        
        @Override
        public boolean hasNext() {
            if (srvRecords.isEmpty()) {
                for(SRVRecord record: usedRecords) {
                    if (!srvRecords.containsKey(record.getPriority())) {
                        srvRecords.put(record.getPriority(), new TreeSet());
                    }
                    srvRecords.get(record.getPriority()).add(record);
                }
                return false;
            }
            return true;
        }

        @Override
        public SRVRecord next() {
            Map.Entry<Integer, TreeSet<SRVRecord>> firstEntry= srvRecords.firstEntry();
        
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

            usedRecords.add(it);
            firstEntry.getValue().remove(it);
            if (firstEntry.getValue().isEmpty()) {
                srvRecords.remove(firstEntry.getKey());
            }

            return it;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported");
        }
        
    }
    
    public class SRVRecordIterable implements Iterable<SRVRecord> {
        private Iterator<SRVRecord> iterator;
        
        public SRVRecordIterable(Attribute srvRecordsData) throws NamingException {
            TreeMap<Integer, TreeSet<SRVRecord>> srvRecords= new TreeMap<>();
            for(NamingEnumeration<?> e= srvRecordsData.getAll(); e.hasMoreElements();) {
                SRVRecord record= new SRVRecord((String)e.nextElement());
                if (!srvRecords.containsKey(record.getPriority())) {
                    srvRecords.put(record.getPriority(), new TreeSet());
                }
                srvRecords.get(record.getPriority()).add(record);
            }
            iterator= new SRVRecordIterator(srvRecords);
        }
        @Override
        public Iterator<SRVRecord> iterator() {
            return iterator;
        }
        
    }
    private final String queryString, txtRecord;
    private final Iterable<SRVRecord> srvRecords;
    
    public ServiceLocator(String service, NetProtocol protocol, String domain) throws NamingException {
        queryString= String.format("_%s._%s.%s", service, protocol, domain);
        
        DirContext ctx= new InitialLdapContext();
        ctx.addToEnvironment("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        ctx.addToEnvironment("java.naming.provider.url", "dns:");
        
        Attributes attrs= (Attributes) ctx.getAttributes(queryString, new String[] { "SRV", "TXT" });
        txtRecord= (String)attrs.get("TXT").get(0);
        srvRecords= new SRVRecordIterable(attrs.get("SRV"));
    }
    
    public String getQueryString() {
        return queryString;
    }
    
    public String getTXTRecord() {
        return txtRecord;
    }
    
    public Iterable<SRVRecord> getSRVRecords() {
        return srvRecords;
    }
}
