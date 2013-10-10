package com.shvid.memcast.bigmem;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class Start {

	private static final int ITERATIONS = 100000;

	public static void main(String[] args) {
		
		System.out.println("BigMemory");
		
		NamedCache cache = CacheFactory.getCache("hello-world");
		
		long  time0 = System.currentTimeMillis();
		
		 for (int i = 0; i != ITERATIONS; ++i) {
			   
		        // create a key to map the data to
		        String key = "greeting" + i;
		
		        // Create a data element
		        String putGreeting = "Hello, World!";
		
		        // Put the element into the data store
		        cache.put(key, putGreeting);
		
		        // Retrieve the data element
		        String getGreeting = (String) cache.get(key);
			 
			 
		 }
		
	        long diff = System.currentTimeMillis() - time0;
	        
	        System.out.println("coherence durability = " + diff);
		
        // Create a cache manager
        CacheManager cacheManager = new CacheManager();

        // create the data store called "hello-world"
        Cache dataStore = cacheManager.getCache("hello-world");

        time0 = System.currentTimeMillis();
        
        for (int i = 0; i != ITERATIONS; ++i) {
        
	        // create a key to map the data to
	        String key = "greeting" + i;
	
	        // Create a data element
	        Element putGreeting = new Element(key, "Hello, World!");
	
	        // Put the element into the data store
	        dataStore.put(putGreeting);
	
	        // Retrieve the data element
	        Element getGreeting = dataStore.get(key);

        }
        
        diff = System.currentTimeMillis() - time0;
        
        System.out.println("ehcache durability = " + diff);
        
        
         time0 = System.currentTimeMillis();
        
         
         Map<String, String> map = new HashMap<String, String>();
         
        for (int i = 0; i != ITERATIONS; ++i) {
        
	        // create a key to map the data to
	        String key = "greeting" + i;
	
	        // Create a data element
	        String putGreeting = "Hello, World!";
	
	        // Put the element into the data store
	        map.put(key, putGreeting);
	
	        // Retrieve the data element
	        String getGreeting = map.get(key);

        }
        
        diff = System.currentTimeMillis() - time0;
        
        System.out.println("hashmap durability = " + diff);
        
        
        
        // Print the value
        //System.out.println(getGreeting.getObjectValue());
		
	}
	
}
