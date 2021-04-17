package com.pjt.service;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.pjt.model.FileType;

public class SteganographyTest {

	Steganography st;
	@Before
	public void setUp() throws Exception {
		st = new Steganography();
	}

	//@Test
	public void testHide() throws IOException {
		//System.out.println(System.getProperty("user.dir"));
		File imageFile = new File("image.txt");
		FileInputStream in = new FileInputStream(imageFile);
		BufferedInputStream bis = new BufferedInputStream(in);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("outimage.txt"));
		String hideText = "I Love Shraddha";
		//System.out.println(hideText.getBytes().length);

		st.hide(bis, imageFile.length(), hideText.getBytes(), FileType.TXT, FileType.Dummy, bos);
		
		bos.close();
		bis.close();

	}
	
	@Test
	public void testProcess() throws IOException {
		st.process(new String[]{"1.bmp","myoutfile.bmp","Ticket.pdf"});
	}
	
	//@Test
	public void testtoBytes() {
		byte[] bytes = st.toBytes(FileType.TXT.getExtension());
		System.out.println("Bytes" + bytes.length);
		
		byte[] stringBytes = FileType.TXT.getExtension().getBytes();
		//System.out.println(stringBytes);
		for(int i=1; i<bytes.length; i++) {
			System.out.println(i + "," + bytes[i] + ", " + stringBytes[i-1]);
			assertEquals(bytes[i], stringBytes[i-1]);
		}
		System.out.println(new String(bytes));
		bytes = st.toBytes(FileType.JPEG.getExtension());
		System.out.println("Bytes" + bytes.length);
		
		stringBytes = FileType.JPEG.getExtension().getBytes();
		//System.out.println(stringBytes);
		for(int i=0; i<bytes.length; i++) {
			System.out.println(i + "," + bytes[i] + ", " + stringBytes[i]);
			assertEquals(bytes[i], stringBytes[i]);
		}
		
		System.out.println(new String(bytes));
	}
	
	//@Test
	public void testOrBits() {
		System.out.println(st.orBits(new byte[] {1,0,1,0,1,0,1,0}));
	}

	@Test
	public void testRetrieve() throws IOException {
		//System.out.println(System.getProperty("user.dir"));
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream("myoutfile.bmp"));
		//BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("decypimage.txt"));

		st.retrieve(bis, FileType.BMP,"decyp");
		
		//bos.close();
		bis.close();

	}
	
	@Test
	public void testhideNthbitonLastBitinImageByte() {
		byte imageByte = 23;//10111
		int pos = 1;
		byte hideByte = 13;//1101

		assertEquals(st.hideNthbitonLastBitinImageByte(imageByte, pos, hideByte),22);
	}
}
