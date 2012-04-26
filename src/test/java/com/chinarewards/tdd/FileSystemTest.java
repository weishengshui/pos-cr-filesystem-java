package com.chinarewards.tdd;

import java.util.Arrays;
import junit.framework.TestCase;

//
/**
 * Unit test for FileSystem.
 */
public class FileSystemTest extends TestCase {
	FileBasedLowLevelIO llio;
	CRFileSystem fs;

	protected void setUp() throws Exception {
		super.setUp();
		llio = new FileBasedLowLevelIO("E:\\fileSystem\\system.vdk");
		fs = new CRFileSystem(llio);
	}

	protected void tearDown() throws Exception {
		llio = null;
		fs = null;
		super.tearDown();
	}

	// case: format disk
	public void test_FileSystem_Format() {
		assertEquals(CRFileSystem.SUCCESS, fs.format(true, 512));
		assertTrue(fs.isFormat());
		assertEquals(3506, fs.getFatOneLength());
		assertEquals(3506, fs.getFatTwoLength());
		assertEquals(143746, fs.getMetaDataLength());
		assertEquals(897536, fs.getDataBlockLength());
		assertEquals(234, fs.getWasteBlockLength());
	}
	
	// case: format the disk and check the available space of disk
	//格式化后数据是否被清除？
	public void test_FileSystem_AfterFormat(){
		assertEquals(CRFileSystem.SUCCESS, fs.format(false, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		String newfileContents = new String(
				"assertNull(fs.findMetadataByFilename(fileName1));"
						+ "ssertEquals(0, fs.format(true, 512));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "		assertEquals(0, fs.createFile(fileName1));		"
						+ "assertNotNull(fs.findMetadataByFilename(fileName1));		"
						+ "assertEquals(0, fs.createFile(fileName2));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "ssertEquals(0, fs.format(true, 512));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "		assertEquals(0, fs.createFile(fileName1));		"
						+ "assertNotNull(fs.findMetadataByFilename(fileName1));		"
						+ "assertEquals(0, fs.createFile(fileName2));");
		byte[] newfileBuffer = newfileContents.getBytes();
		// write full the disk,so there is no space to create new file
		assertEquals(CRFileSystem.SUCCESS,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename2"));

		assertEquals(897536-1536, fs.getAvailableSpace());
		
		assertEquals(CRFileSystem.SUCCESS, fs.format(false, 512));
		assertEquals(897536, fs.getAvailableSpace());
	}
	
	/**
	 * 新建一个文件，写超过一个簇的大小，即会连接到两个Allocation Table Index
	 */
	public void test_FileSystem_moreThanOneDatablock(){
		assertEquals(CRFileSystem.SUCCESS, fs.format(false, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		String newfileContents = new String(
				"assertNull(fs.findMetadataByFilename(fileName1));"
						+ "ssertEquals(0, fs.format(true, 512));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "		assertEquals(0, fs.createFile(fileName1));		"
						+ "assertNotNull(fs.findMetadataByFilename(fileName1));		"
						+ "assertEquals(0, fs.createFile(fileName2));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "ssertEquals(0, fs.format(true, 512));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "		assertEquals(0, fs.createFile(fileName1));		"
						+ "assertNotNull(fs.findMetadataByFilename(fileName1));		"
						+ "assertEquals(0, fs.createFile(fileName2));");
		byte[] newfileBuffer = newfileContents.getBytes();
		// write full the disk,so there is no space to create new file
		assertEquals(CRFileSystem.SUCCESS,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		assertEquals(897536-1024, fs.getAvailableSpace());
	}
	
	
	/**
	 * open 再执行一次open。fileSystem未open，执行close
	 */
	public void test_FileSystem_open_open_close_close(){
		assertEquals(CRFileSystem.SUCCESS, fs.format(false, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		String newfileContents = new String(
				"assertNull(fs.findMetadataByFilename(fileName1));"
						+ "ssertEquals(0, fs.format(true, 512));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "		assertEquals(0, fs.createFile(fileName1));		"
						+ "assertNotNull(fs.findMetadataByFilename(fileName1));		"
						+ "assertEquals(0, fs.createFile(fileName2));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "ssertEquals(0, fs.format(true, 512));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "		assertEquals(0, fs.createFile(fileName1));		"
						+ "assertNotNull(fs.findMetadataByFilename(fileName1));		"
						+ "assertEquals(0, fs.createFile(fileName2));");
		byte[] newfileBuffer = newfileContents.getBytes();
		// write full the disk,so there is no space to create new file
		assertEquals(CRFileSystem.SUCCESS,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename2"));
		fs.close();
		fs.close();
		assertEquals(CRFileSystem.NOT_FORMAT, fs.createFile("filename2"));
		assertEquals(CRFileSystem.PARAM_INVALID,
				fs.write(0, newfileBuffer, newfileBuffer.length));

		fs.open();
		fs.open();
		
		assertEquals(CRFileSystem.FILE_EXIST, fs.createFile("filename2"));
		// read the whole file
		byte[] buffer = new byte[newfileBuffer.length];
		assertEquals(CRFileSystem.SUCCESS, fs.read(0, buffer, buffer.length));
		assertTrue(newfileContents.equals(new String(buffer)));
		// read 6 bytes
		buffer = new byte[6];
		assertEquals(CRFileSystem.SUCCESS, fs.read(0, buffer, buffer.length));
		assertTrue("assert".equals(new String(buffer)));

	}
	
	// case: create file before the disk format: fail
	// case: create file after the disk format: success
	public void test_FileSystem_CreateFile() {

		// create file before the disk format
		llio.write(0, new byte[] { 1 }, 1); // let the format not format
		assertEquals(CRFileSystem.NOT_FORMAT, fs.createFile("filename1"));
		assertFalse(fs.isExists("filename1"));

		// create file after the disk format
		assertEquals(CRFileSystem.SUCCESS, fs.format(true, 512));
		// case one:the filename is empty
		assertEquals(CRFileSystem.PARAM_INVALID, fs.createFile(null));
		assertEquals(CRFileSystem.PARAM_INVALID, fs.createFile(""));
		assertEquals(CRFileSystem.PARAM_INVALID, fs.createFile("  "));
		// case two: the file name is two long(65 bytes)
		assertEquals(
				CRFileSystem.PARAM_INVALID,
				fs.createFile("12345678901234567890123456789012345678901234567890123456789012345"));
		assertFalse(fs.isExists("filename1"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename1"));
		assertTrue(fs.isExists("filename1"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename2"));
		assertTrue(fs.isExists("filename2"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename3"));
		assertTrue(fs.isExists("filename3"));
		// 64 bytes file name
		assertEquals(
				CRFileSystem.SUCCESS,
				fs.createFile("1234567890123456789012345678901234567890123456789012345678901234"));
		assertTrue(fs
				.isExists("1234567890123456789012345678901234567890123456789012345678901234"));
		// 63 bytes file name
		assertFalse(fs
				.isExists("123456789012345678901234567890123456789012345678901234567890123"));
	}

	// case: list file
	public void test_FileSystem_ListFiles() {
		assertEquals(CRFileSystem.SUCCESS, fs.format(true, 512));
		String[] files = fs.listFiles();
		assertNotNull(files);
		assertEquals(0, files.length);
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename1"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename2"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename3"));
		// 64 bytes file name
		assertEquals(
				CRFileSystem.SUCCESS,
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

		assertEquals(CRFileSystem.SUCCESS, fs.format(true, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("firstFile"));

		byte[] datablockBuffer = new byte[897533];// 897536 is data block length
		for (int i = 0; i < datablockBuffer.length; i++) {
			datablockBuffer[i] = 100;
		}

		assertEquals(0, fs.write(0, datablockBuffer, datablockBuffer.length));
		assertEquals(0, fs.write(0, datablockBuffer, datablockBuffer.length));
		// create file fail
		assertEquals(CRFileSystem.DISK_FULL, fs.createFile("secondFile"));
	}

	// case:test findMetadataByFilename
	public void test_FileSystem_findMetadataByFilename() {
		assertNull(fs.findMetadataEntryByFilename("fileName1"));
		assertEquals(CRFileSystem.SUCCESS, fs.format(true, 512));
		assertNull(fs.findMetadataEntryByFilename("fileName1"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("fileName1"));
		assertNotNull(fs.findMetadataEntryByFilename("fileName1"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("fileName2"));
	}

	/**
	 * 1: format the disk. 2: write some thing to the disk. 3: close the file
	 * system and open it. 4: read something from the disk
	 */
	public void test_FileSystem_Format_Write_Close_Open_Read() {
		assertEquals(CRFileSystem.SUCCESS, fs.format(false, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		String newfileContents = new String(
				"assertNull(fs.findMetadataByFilename(fileName1));"
						+ "ssertEquals(0, fs.format(true, 512));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "		assertEquals(0, fs.createFile(fileName1));		"
						+ "assertNotNull(fs.findMetadataByFilename(fileName1));		"
						+ "assertEquals(0, fs.createFile(fileName2));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "ssertEquals(0, fs.format(true, 512));"
						+ "assertNull(fs.findMetadataByFilename(fileName1));"
						+ "		assertEquals(0, fs.createFile(fileName1));		"
						+ "assertNotNull(fs.findMetadataByFilename(fileName1));		"
						+ "assertEquals(0, fs.createFile(fileName2));");
		byte[] newfileBuffer = newfileContents.getBytes();
		// write full the disk,so there is no space to create new file
		assertEquals(CRFileSystem.SUCCESS,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename2"));
		fs.close();
		assertEquals(CRFileSystem.NOT_FORMAT, fs.createFile("filename2"));
		assertEquals(CRFileSystem.PARAM_INVALID,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		fs.open();
		assertEquals(CRFileSystem.FILE_EXIST, fs.createFile("filename2"));
		// read the whole file
		byte[] buffer = new byte[newfileBuffer.length];
		assertEquals(CRFileSystem.SUCCESS, fs.read(0, buffer, buffer.length));
		assertTrue(newfileContents.equals(new String(buffer)));
		// read 6 bytes
		buffer = new byte[6];
		assertEquals(CRFileSystem.SUCCESS, fs.read(0, buffer, buffer.length));
		assertTrue("assert".equals(new String(buffer)));

	}
	
	public void test_FileSystem_Write_Read() {

		assertEquals(CRFileSystem.SUCCESS, fs.format(true, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("file_One"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("file_Two"));
		String fileOneContent = "This is the first file.";
		String fileTwoContent = "This is the second file.";
		byte[] fileOneBytes = fileOneContent.getBytes();
		byte[] fileTwoBytes = fileTwoContent.getBytes();

		assertEquals(CRFileSystem.SUCCESS,
				fs.write(0, fileOneBytes, fileOneBytes.length));
		assertEquals(CRFileSystem.SUCCESS,
				fs.write(512, fileTwoBytes, fileTwoBytes.length));

		assertEquals(CRFileSystem.SUCCESS,
				fs.write(512, fileTwoBytes, fileTwoBytes.length));

		fileOneBytes = new byte[fileOneContent.length()];
		fileTwoBytes = new byte[fileTwoContent.length()];

		assertEquals(CRFileSystem.SUCCESS,
				fs.read(0, fileOneBytes, fileOneBytes.length));
		assertEquals(CRFileSystem.SUCCESS,
				fs.read(512, fileTwoBytes, fileTwoBytes.length));

		assertTrue(fileOneContent.equals(new String(fileOneBytes)));
		assertTrue(fileTwoContent.equals(new String(fileTwoBytes)));

	}

	/**
	 * 
	 */
	public void test_FileSystem_deleteFile() {

		assertEquals(CRFileSystem.SUCCESS, fs.format(true, 512));

		assertEquals(CRFileSystem.FILE_NOT_EXIST, fs.deleteFile("newfile"));

		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile2"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile3"));

		assertEquals(CRFileSystem.SUCCESS, fs.deleteFile("newfile"));
		assertEquals(CRFileSystem.SUCCESS, fs.deleteFile("newfile2"));
		assertEquals(CRFileSystem.SUCCESS, fs.deleteFile("newfile3"));

		assertEquals(CRFileSystem.FILE_NOT_EXIST, fs.deleteFile("newfile"));
		assertEquals(CRFileSystem.FILE_NOT_EXIST, fs.deleteFile("newfile2"));

		//
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		byte[] datablockBuffer = new byte[897533];// 897536 is data block length
		for (int i = 0; i < datablockBuffer.length; i++) {
			datablockBuffer[i] = 100;
		}
		assertEquals(0, fs.write(0, datablockBuffer, datablockBuffer.length));
		assertEquals(CRFileSystem.DISK_FULL, fs.createFile("newfile2"));

		assertEquals(CRFileSystem.SUCCESS, fs.deleteFile("newfile"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile2"));

	}
}
