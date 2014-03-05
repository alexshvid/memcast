package com.shvid.memcast.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionHelper {

	public final static int DEFAULT = Deflater.DEFAULT_COMPRESSION;
	public final static int BEST = Deflater.BEST_COMPRESSION;
	public final static int MIDDLE = Deflater.DEFLATED;
	public final static int LOW = Deflater.BEST_SPEED;
	public final static int NO = Deflater.NO_COMPRESSION;
	
	public final static int BUFFER_SIZE = 4092;
	
    public static byte[] compress(byte[] data, int level) throws IOException {
    	if (data == null || data.length == 0) {
    		return data;
    	}
        ByteArrayOutputStream bout = new ByteArrayOutputStream(data.length);
        Deflater deflater = new Deflater();
        deflater.setLevel(level);
        deflater.setInput(data);
        deflater.finish();
        byte[] buf = new byte[BUFFER_SIZE];
        while (!deflater.finished()) {
            int count = deflater.deflate(buf);
            bout.write(buf, 0, count);
        }
        deflater.end();
        bout.close();
        return bout.toByteArray();
    }    
    
    public final static byte[] decompress(byte[] input) throws IOException {
        if (input == null || input.length==0) {
        	return input;
        }
        Inflater inflator = new Inflater();
        inflator.setInput(input);
        ByteArrayOutputStream bin = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[BUFFER_SIZE];
        try {
            while (true) {
                int count = inflator.inflate(buf);
                if (count > 0) {
                    bin.write(buf, 0, count);
                } else if (count == 0 && inflator.finished()) {
                    break;
                } else {
                    throw new IOException("bad zip data, size:" + input.length);
                }
            }
        } catch (DataFormatException t) {
            throw new IOException(t);
        } finally {
            inflator.end();
        }
        return bin.toByteArray();
    }
    
    
}