package com.shvid.memcast.unsafe;

public class Customer {

	private static long offset = 0;
	private static final long idOffset = offset += 0;
	private static final long amountOffset = offset += 4;
	
	private int id;
	private float amount;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public float getAmount() {
		return amount;
	}
	
	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	public static int getId(long address) {
		return DirectMemoryAccess.getUnsafe().getInt(address + idOffset);
	}
	
	public static void setId(long address, int value) {
		DirectMemoryAccess.getUnsafe().putInt(address + idOffset, value);
	}
	
	public static float getAmount(long address) {
		return DirectMemoryAccess.getUnsafe().getFloat(address + amountOffset);
	}
	
	public static void setAmount(long address, float value) {
		DirectMemoryAccess.getUnsafe().putFloat(address + amountOffset, value);
	}
	
}
