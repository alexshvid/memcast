package com.shvid.memcast;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

import javassist.NotFoundException;

import com.shvid.memcast.perm.InMemoryClassLoader;

public class MemcastMain {

	private static final boolean CREATE_INSTANCE = true;
	private static final boolean SAFE_101_OBJECT = false;
	
	private static List<Object> list = new ArrayList<Object>();
	
	public static void main(String[] args) {
				
		System.out.println("Memcast started");
		pause();


		try {
			
			while(true) {
			
				loadClasses(5000);
				System.gc();
				
				System.out.println("Loaded 5000 classes.");
				
				sleep();
			
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("done");
		
	}

	private static void sleep() {
		
		try {
			Thread.currentThread().sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void pause() {
		System.out.println("Enter a string to continue:");
		DataInputStream din = new DataInputStream(System.in);
		try {
			din.readLine();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void loadClasses(int num) throws NotFoundException,
			ClassNotFoundException {
		InMemoryClassLoader classLoader = new InMemoryClassLoader();
		
		
		for (int i = 1; i != num; ++i) {
			
			Class<?> cls = classLoader.loadClass("com.shvid.memcast.perm.PrimitiveClass$" + i);
			
			if (CREATE_INSTANCE) {
				try {
					Object obj = cls.newInstance();
					
					if (SAFE_101_OBJECT && i == 101) {
						list.add(obj);
					}
					
					
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		  //Class<?> cls = ClassMigration.migrate(i);
			  if (i % 1000 == 0) {
			     System.out.println("cls = " + cls);
			  }
		}
	}
	
}


