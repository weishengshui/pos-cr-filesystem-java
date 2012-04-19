package com.chinarewards.tdd;

import java.util.Arrays;
import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	FileBasedLowLevelIO llio;
	CRFileSystem fs;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		llio = new FileBasedLowLevelIO("E:\\fileSystem\\system.vdk");
		fs = new CRFileSystem(llio);
	}

	@Override
	protected void tearDown() throws Exception {
		llio = null;
		super.tearDown();
	}

	// case: format disk
	public void test_FileSystem_Format() {
		assertEquals(0, fs.format(true, 512));
		assertTrue(fs.isFormat());
		assertEquals(3506, fs.getFatOneLength());
		assertEquals(3506, fs.getFatTwoLength());
		assertEquals(143746, fs.getMetaDataLength());
		assertEquals(897536, fs.getDataBlockLength());
		assertEquals(234, fs.getWasteBlockLength());
	}

	// case: create file
	public void test_FileSystem_CreateFile() {
		assertEquals(0, fs.format(true, 512));
		// case one:the filename is empty
		assertEquals(-1, fs.createFile(null));
		assertEquals(-1, fs.createFile(""));
		// case two: the file name is two long(65 bytes)
		assertEquals(
				-3,
				fs.createFile("12345678901234567890123456789012345678901234567890123456789012345"));
		assertFalse(fs.isExists("filename1"));
		assertEquals(0, fs.createFile("filename1"));
		assertTrue(fs.isExists("filename1"));
		assertEquals(0, fs.createFile("filename2"));
		assertTrue(fs.isExists("filename2"));
		assertEquals(0, fs.createFile("filename3"));
		assertTrue(fs.isExists("filename3"));
		// 64 bytes file name
		assertEquals(
				0,
				fs.createFile("1234567890123456789012345678901234567890123456789012345678901234"));
		assertTrue(fs
				.isExists("1234567890123456789012345678901234567890123456789012345678901234"));
		// 63 bytes file name
		assertFalse(fs
				.isExists("123456789012345678901234567890123456789012345678901234567890123"));
	}

	// case: list file
	public void test_FileSystem_ListFiles() {
		assertEquals(0, fs.format(true, 512));
		String[] files = fs.listFiles();
		assertNotNull(files);
		assertEquals(0, files.length);
		assertEquals(0, fs.createFile("filename1"));
		assertEquals(0, fs.createFile("filename2"));
		assertEquals(0, fs.createFile("filename3"));
		// 64 bytes file name
		assertEquals(
				0,
				fs.createFile("1234567890123456789012345678901234567890123456789012345678901234"));
		files = fs.listFiles();
		assertNotNull(files);
		assertEquals(4, files.length);
		assertTrue(Arrays
				.equals(new String[] { "filename1", "filename2", "filename3",
						"1234567890123456789012345678901234567890123456789012345678901234" },
						files));
	}

	// case:create first file and write full disk; create second file fail.
	public void test_FileSystem_FirstFull_SecondFail() {
		assertEquals(0, fs.format(true, 512));
		assertEquals(0, fs.createFile("firstFile"));
		byte[] datablockBuffer = new byte[897536];//897536 is data block length
		for (int i = 0; i < datablockBuffer.length; i++) {
			datablockBuffer[i] = 100;
		}
		assertEquals(0,fs.write(0, datablockBuffer, datablockBuffer.length));
		// create file fail
		assertEquals(-6, fs.createFile("secondFile"));
	}
	
	

	// case:test findMetadataByFilename
	public void test_FileSystem_findMetadataByFilename() {
		assertNull(fs.findMetadataByFilename("fileName1"));
		assertEquals(0, fs.format(true, 512));
		assertNull(fs.findMetadataByFilename("fileName1"));
		assertEquals(0, fs.createFile("fileName1"));
		assertNotNull(fs.findMetadataByFilename("fileName1"));
	}

	// case: write from addr #0, length: 5 bytes, read from #1, length: 4
	public void test_LowLevelIO_Write0_5Read1_4() {
		byte[] buffer = { 1, 2, 3, 4, 5 };
		assertEquals(0, llio.write(0, buffer, 5));
		byte[] buffer2 = new byte[4];
		assertEquals(0, llio.read(1, buffer2, 4));
		assertTrue(Arrays.equals(new byte[] { 2, 3, 4, 5 }, buffer2));
	}

	// case: write from addr #-1 (bad, minimum addr = 0), return error code = 1
	// (out of bound)
	public void test_LowLevelIO_Write_1() {
		byte[] buffer = { 1, 2, 3, 4, 5 };
		assertEquals(1, llio.write(-1, buffer, 5));
	}

	// case: erased all bytes to 1, write from addr # 18, length = 5 byte,
	// return error code = 1, read from #18, #19 == 1
	public void test_LowLevelIO_WriteAll1Write18_5Read18_2() {
		byte[] buffer = new byte[1024 * 1024];
		for (int index = 0; index < buffer.length; index++) {
			buffer[index] = 1;
		}
		assertEquals(0, llio.write(0, buffer, 1024 * 1024));
		byte[] buffer2 = { 2, 3, 4, 5, 6 };
		assertEquals(1024 * 1024, llio.getSize());
		assertEquals(1, llio.write(1024 * 1024 - 2, buffer2, 5));
		byte[] buffer3 = new byte[2];
		assertEquals(0, llio.read(1024 * 1024 - 2, buffer3, 2));
		assertTrue(Arrays.equals(new byte[] { 1, 1 }, buffer3));

	}

	// case: write at addr #19 , length = 1 (OK)_
	public void test_LowLevelIO_Write19_1() {
		assertEquals(0, llio.write(1024 * 1024 - 1, new byte[] { 22 }, 1));
	}

	// case: write at addr #20, length =1 (BAD)
	public void test_LowLevelIO_Wite20_1() {
		assertEquals(1, llio.write(1024 * 1024, new byte[] { 2 }, 1));
	}

	// case: check the disk size
	public void test_LowLevelIO_Size() {
		assertEquals(1024 * 1024, llio.getSize());
	}

	// case: after close. read(BAD),write(BAD)
	public void test_LowLevelIO_Close() {
		llio.close();
		assertEquals(1, llio.read(0, new byte[2], 2));
		assertEquals(1, llio.write(5, new byte[] { 3, 2, 3, 4 }, 4));
	}
}
