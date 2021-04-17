package com.pjt.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.pjt.model.CONSTANTS;
import com.pjt.model.FileType;

public class Steganography {

	public static void main(String[] args) throws FileNotFoundException {

	}

	public void process(String[] args) throws IOException {
		String imageLocation = args[0];
		String outputFile = args[1];
		String dataTohideFile = args[2];//"I Love Shraddha";
		
		
		File imageFile = new File(imageLocation);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(imageFile));
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));

		File fileToHide = new File(dataTohideFile);
		
		hide(bis, imageFile.length(), Files.readAllBytes(fileToHide.toPath()), getExtensionFromFilePath(dataTohideFile), getExtensionFromFilePath(imageLocation), bos);
		
		bos.close();
		bis.close();

	}

	private FileType getExtensionFromFilePath(String imageLocation) {
		System.out.println(imageLocation.substring(imageLocation.lastIndexOf(".")+1, imageLocation.length()));
		return FileType.getFileType(imageLocation.substring(imageLocation.lastIndexOf(".")+1, imageLocation.length()));
	}

	public boolean canImageHideData(long imageFileSize, byte[] hide, FileType imageFileType) {
		// 0. Check if file can accommodate hide
		System.out.println(" imageFileSize : " + imageFileSize + " ,hideData : " + hide.length);
		return (imageFileSize - (hide.length * 8  + imageFileType.getHeaderByteLength() + CONSTANTS._SIZE_BYTES * 8 + CONSTANTS._EXT_BYTES * 8)) > 0;
	}
	
	byte[] toBytes(int i)
	{
	  byte[] result = new byte[CONSTANTS._SIZE_BYTES];

	  result[0] = (byte) (i >> 24);
	  result[1] = (byte) (i >> 16);
	  result[2] = (byte) (i >> 8);
	  result[3] = (byte) (i /*>> 0*/);

	  return result;
	}

	public byte[] toBytes(String i)
	{
	  byte[] result = new byte[CONSTANTS._EXT_BYTES];
	  if(i.length() > CONSTANTS._EXT_BYTES) {
		  throw new RuntimeException("File extension is bigger than "+ CONSTANTS._EXT_BYTES +" bytes : " + i);
	  }
	  
	  byte[] imageBytes = i.getBytes();
	  if(imageBytes.length <= result.length) {
		  int byteSizeDiff = result.length-imageBytes.length;
		for(int impos = result.length-1; impos>=byteSizeDiff; impos--) {
			  result[impos] = imageBytes[impos - byteSizeDiff];
		  }
	  }
	  return result;
	}
	
	public void hide(BufferedInputStream bis, long imageFileSize, byte[] hide, FileType datafileType, FileType imageFileType, BufferedOutputStream bos)
			throws IOException {
		// Start
		int length = hide.length;
		if(!canImageHideData(imageFileSize, hide, imageFileType)) {
			throw new RuntimeException("Cannot hide data, imageSize : " + imageFileSize + ", hideData : " + length);
		}

		// 1. Skip header
		byte[] b = new byte[imageFileType.getHeaderByteLength()];
		bis.read(b);
		bos.write(b);

		// Add size information
		// a. Add 4 bytes to store extension
		// image used = 4 * 8 = 32 bytes
		hideBits(bis, toBytes(datafileType.getExtension()), bos);

		// b. Add 4 bytes to store hide data size
		// imageFile used = 4 * 8 = 32 bytes
		//System.out.println("Size to hide :: " + ByteBuffer.wrap(toBytes(length)).getInt());
		hideBits(bis, toBytes(length), bos);
		
		
		// Add file extension info
		// 2. Split the hide text into bits
		hideBits(bis, hide, bos);

		int read;
		while(!((read = bis.read()) == -1)) {
			bos.write(read);
		}
	}

	private void hideBits(BufferedInputStream bis, byte[] hide, BufferedOutputStream bos) throws IOException {
		for (byte hideByte : hide) {
			
			byte[] imageByte = new byte[8];
			bis.read(imageByte); // read next byte from image
			for(int pos=0 ;pos<8; pos++) {
				int value = hideNthbitonLastBitinImageByte(imageByte[pos], pos, hideByte);
				bos.write(value);
			}
		}
	}

	public void retrieve(BufferedInputStream bis, FileType imageFileType, String outFileName)
			throws IOException {
		// Start
		
		// 1. Skip header
		byte[] b = new byte[imageFileType.getHeaderByteLength()];
		bis.read(b);

		//get extension
		byte[] extBytes = getHiddenByte(bis, CONSTANTS._EXT_BYTES);
		String extension = new String(extBytes);
		System.out.println("Extension : " + extension);
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFileName+"."+extension.trim()));
		//get hidden data size
		byte[] sizeBytes = getHiddenByte(bis, CONSTANTS._SIZE_BYTES);
		
		//System.out.println(new String(sizeBytes));
		int numBytes = fromByteArray(sizeBytes);
		System.out.println("Hidden data size : " + numBytes);

		// 2. Split the hide text into bits
		bos.write(getHiddenByte(bis, numBytes));
		bos.flush();

		bos.close();
	}

	private byte[] getHiddenByte(BufferedInputStream bis, int byteSize) throws IOException {
		byte[] bytes = new byte[byteSize];
		for(int pos = 0; pos < byteSize; pos++) {
			byte[] val = getByte(bis);
			bytes[pos] = orBits(val);
		}
		
		return bytes;
	}

	int fromByteArray(byte[] bytes) {
	     return ((bytes[0] & 0xFF) << 24) | 
	            ((bytes[1] & 0xFF) << 16) | 
	            ((bytes[2] & 0xFF) << 8 ) | 
	            ((bytes[3] & 0xFF) << 0 );
	}
	
	private byte[] getByte(BufferedInputStream bis) throws IOException {
		byte[] imageByte = new byte[8];
		bis.read(imageByte); // read next byte from image
		byte val[] = new byte[8];
		byte b1 = 1;
		for(int pos =0 ;pos<8; pos++) {
			val[pos] = (byte)(imageByte[pos] & b1);
		}
		return val;
	}

	public byte orBits(byte[] val) {
		byte value = 0; 
		for(int pos = 0 ; pos < 8 ; pos++) {
			int shiftVal = val[pos] << pos;
			value = (byte)(value | shiftVal);
		}
		
		return value;
	}

	public int hideNthbitonLastBitinImageByte(byte imageByte, int pos, byte hideByte) {
		int maskedLastBitOfImageByte = imageByte & ~1;

		//System.out.println("ImageByte :" +imageByte + ", maskingLastBit " + maskedLastBitOfImageByte);

		int mask = hideByte >> pos;
		int bitatPos = mask & 1;

		//System.out.println("Hide Byte : " + hideByte + ", bit at postion : " + pos  + ", " + bitatPos);

		int value = maskedLastBitOfImageByte | bitatPos;// 10110
		return value;
	}


}
