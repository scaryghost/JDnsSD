/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.jdnssd;

/**
 * Stores information about an SRV record
 * @author etsai
 */
public class SRVRecord implements Comparable {
    private final int port, priority, weight;
    private final String hostname;
    
    public SRVRecord(String data) {
        String[] properties= data.split(" ");
        
        priority= Integer.valueOf(properties[0]);
        weight= Integer.valueOf(properties[1]);
        port= Integer.valueOf(properties[2]);
        hostname= properties[3].substring(0, properties[3].length() - 1);
    }
    
    public int getPort() {
        return port;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    @Override
    public int compareTo(Object o) {
        SRVRecord r= (SRVRecord)o;
        
        int sum= 0;
        Integer[] values= {Integer.compare(priority, r.priority), Integer.compare(weight, r.weight), 
                Integer.compare(port, r.port), hostname.compareTo(r.hostname)};
        Integer[] weights= {8, 4, 2, 1};
        for(int i= 0; i < weights.length; i++) {
            sum+= values[i] * weights[i];
        }
        
        return sum;
    }
    
    @Override
    public String toString() {
        return String.format("{priority:%d, weight:%d, port:%d, hostname:%s}", priority, weight, port, hostname);
    }
}
