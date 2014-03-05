package com.shvid.memcast.utf8;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class StringMain {

	public static void main(String[] args) {
		
		System.out.println("convert string to utf-8");
		
		String str = "Hello world!";
		
		BlobBuilder bb = new BlobBuilder();
		
		writeUtf8(str, 0, str.length(), bb);
		
		try {
			System.out.println("r = " + Arrays.equals(str.getBytes("UTF-8"), bb.getBytes()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder();
		
		readUtf8(bb.getBytes(), 0, bb.getBytes().length, sb);
		
		System.out.println("sb = " + sb.toString());
		
		bb.print();
		
	}

	public static void readUtf8(byte[] blob, int offset, int length, StringBuilder str) {
		
		for (int i = 0; i != length; ++i) {
		
			int b = blob[i + offset] & 0xFF;
			
			int c = b >> 4;

			if (c <= 7) {
				str.append((char) b);
			}
			else if (c == 14) {
				int b2 = blob[++i + offset];
				int b3 = blob[++i + offset];
				str.append( (char)((b & 0x0F) << 12 | (b2 & 0x3F) << 6 | b3 & 0x3F) );
				
			}
			else {
				i++;
				int b2 = blob[++i + offset];
				str.append( (char)((b & 0x1F) << 6 | b2 & 0x3F) );
			}
 			
			
		}
		
	}
	
	public static void writeUtf8(CharSequence value, int offset, int length, BlobBuilder blob) {
		
		for (int i = 0; i != length; ++i) {
			
			int ch = value.charAt(i + offset);
			
			if (ch <= 0x007F) {
				blob.append((byte) ch);
			}
			else if (ch <= 0x07FF) {
				blob.append((byte)(0xC0 | ch >> 6 & 0x1F));
				blob.append((byte)(0x80 | ch & 0x3F));
			}
			else {
				blob.append((byte)(0xE0 | ch >> 12 & 0x0F));;
				blob.append((byte)(0x80 | ch >> 6 & 0x3F));
				blob.append((byte)(0x80 | ch & 0x3F));
			}
			
		}
		
		
		
	}
	
	
	public static class BlobBuilder {
		
		private byte[] blob = new byte[1024];
		private int pos = 0;
		
		public BlobBuilder() {
		}
		
		public void append(byte value) {
			blob[pos++] = value;
		}
		
		public void print() {
			for (int i = 0; i != pos; ++i) {
			System.out.println(blob[i]);
			}
		}
		
		public byte[] getBytes() {
			byte[] result = new byte[pos];
			System.arraycopy(blob, 0, result, 0, pos);
			return result;
		}
	}
}
