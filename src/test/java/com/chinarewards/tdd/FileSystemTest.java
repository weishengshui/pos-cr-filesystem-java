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
		llio = new FileBasedLowLevelIO("/home/wss/disk/system.vdk");
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
		assertEquals(3460, fs.getFatOneLength());
		assertEquals(3460, fs.getFatTwoLength());
		assertEquals(155700, fs.getMetaDataLength());
		assertEquals(885760, fs.getDataBlockLength());
		assertEquals(148, fs.getWasteBlockLength());
	}

	// case: format the disk and check the available space of disk
	// ��ʽ��������Ƿ����
	public void test_FileSystem_AfterFormat() {
		assertEquals(CRFileSystem.SUCCESS, fs.format(false, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		fs.setFileProperty("newfile");
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
		assertEquals(newfileBuffer.length,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename2"));

		assertEquals(885760 - 1536, fs.getAvailableSpace());

		assertEquals(CRFileSystem.SUCCESS, fs.format(false, 512));
		assertEquals(885760, fs.getAvailableSpace());
	}

	/**
	 * �½�һ���ļ���д����һ���صĴ�С���������ӵ�����Allocation Table Index
	 */
	public void test_FileSystem_moreThanOneDatablock() {
		assertEquals(CRFileSystem.SUCCESS, fs.format(false, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		fs.setFileProperty("newfile");
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
		assertEquals(newfileBuffer.length,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		assertEquals(885760 - 1024, fs.getAvailableSpace());
	}

	/**
	 * open ��ִ��һ��open��fileSystemδopen��ִ��close
	 */
	public void test_FileSystem_open_open_close_close() {

		assertEquals(CRFileSystem.SUCCESS, fs.format(false, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		fs.setFileProperty("newfile");

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
		assertEquals(newfileBuffer.length,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename2"));

		fs.close();
		fs.close();
		assertEquals(CRFileSystem.NOT_FORMAT, fs.createFile("filename2"));
		assertEquals(CRFileSystem.NOT_FORMAT,
				fs.write(0, newfileBuffer, newfileBuffer.length));

		fs.open();
		fs.open();

		assertEquals(CRFileSystem.FILE_EXIST, fs.createFile("filename2"));
		// read the whole file
		byte[] buffer = new byte[newfileBuffer.length];
		Stat stat = new Stat();
		fs.setStat(stat);
		assertNotNull(fs.setFileProperty("newfile"));
		assertEquals(buffer.length, fs.read(0, buffer, buffer.length));
		assertTrue(newfileContents.equals(new String(buffer)));
		// read 6 bytes
		buffer = new byte[6];
		assertEquals(buffer.length, fs.read(0, buffer, buffer.length));
		assertTrue("assert".equals(new String(buffer)));

	}

	public void test_FileSystem_Write() {

		assertEquals(CRFileSystem.SUCCESS, fs.format(true, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename1"));
		fs.setFileProperty("filename1");

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

		assertEquals(newfileBuffer.length,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		assertEquals(newfileBuffer.length,
				fs.write(512, newfileBuffer, newfileBuffer.length));

	}

	public void test_FileSystem_Write_Read() {

		assertEquals(CRFileSystem.SUCCESS, fs.format(true, 512));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename1"));
		Stat stat = new Stat();
		fs.setStat(stat);
		fs.setFileProperty("filename1");

		// newfileContents length : 565
		String newfileContents = new String(
				"assertNull(fs.findMetadataByFilename(fileName1));\n"
						+ "ssertEquals(0, fs.format(true, 512));\n"
						+ "assertNull(fs.findMetadataByFilename(fileName1));\n"
						+ "		assertEquals(0, fs.createFile(fileName1));		\n"
						+ "assertNotNull(fs.findMetadataByFilename(fileName1));		\n"
						+ "assertEquals(0, fs.createFile(fileName2));\n"
						+ "assertNull(fs.findMetadataByFilename(fileName1));\n"
						+ "ssertEquals(0, fs.format(true, 512));\n"
						+ "assertNull(fs.findMetadataByFilename(fileName1));\n"
						+ "		assertEquals(0, fs.createFile(fileName1));		\n"
						+ "assertNotNull(fs.findMetadataByFilename(fileName1));		\n"
						+ "assertEquals(0, fs.createFile(fileName2));");

		byte[] newfileBuffer = newfileContents.getBytes();
		byte[] readBuffer = new byte[newfileContents.length()];

		// totalFileLength=565;fileOffset = 0, length = 565；分配两个fat索引
		assertEquals(newfileBuffer.length,
				fs.write(0, newfileBuffer, newfileBuffer.length));

		// *** ***
		// totalFileLength=565;fileOffset = 0, length = 565
		readBuffer = new byte[newfileContents.length()];
		assertEquals(readBuffer.length,
				fs.read(0, readBuffer, readBuffer.length));
		assertTrue(newfileContents.equals(new String(readBuffer)));

		// 0** ***
		// totalFileLength=565;fileOffset = 1, length = 564
		readBuffer = new byte[newfileContents.length() - 1];
		assertEquals(readBuffer.length,
				fs.read(1, readBuffer, readBuffer.length));
		assertTrue(newfileContents.substring(1).equals(
				new String(readBuffer, 0, newfileContents.length() - 1)));

		// 000 00*
		// totalFileLength=565; fileOffset = 555, length = 10
		readBuffer = new byte[10];
		assertEquals(10, fs.read(555, readBuffer, 10));
		assertTrue(newfileContents.substring(555, 565).equals(
				(new String(readBuffer, 0, 10))));

		// 000 00*
		// totalFileLength=565; fileOffset = 555, length = 11
		readBuffer = new byte[11];
		assertEquals(10, fs.read(555, readBuffer, 11));
		assertTrue(newfileContents.substring(555, 565).equals(
				(new String(readBuffer, 0, 10))));

		// 000 0*0
		// totalFileLength=565; fileOffset = 554, length = 10
		readBuffer = new byte[10];
		assertEquals(10, fs.read(554, readBuffer, 10));
		assertTrue(newfileContents.substring(554, 564).equals(
				(new String(readBuffer, 0, 10))));

		// 000 ***
		// totalFileLength=565; fileOffset = 512, length = 53
		readBuffer = new byte[53];
		assertEquals(53, fs.read(512, readBuffer, 53));
		assertTrue(newfileContents.substring(512, 565).equals(
				(new String(readBuffer, 0, 53))));

		// 00* ***
		// totalFileLength=565; fileOffset = 511, length = 54
		readBuffer = new byte[54];
		assertEquals(54, fs.read(511, readBuffer, 54));
		assertTrue(newfileContents.substring(511, 565).equals(
				(new String(readBuffer, 0, 54))));

		// 00* **0
		// totalFileLength=565; fileOffset = 511, length = 53
		readBuffer = new byte[53];
		assertEquals(53, fs.read(511, readBuffer, 53));
		assertTrue(newfileContents.substring(511, 564).equals(
				(new String(readBuffer, 0, 53))));

		// 00* 000
		// totalFileLength=565; fileOffset = 511, length = 1
		readBuffer = new byte[1];
		assertEquals(1, fs.read(511, readBuffer, 1));
		assertTrue(newfileContents.substring(511, 512).equals(
				(new String(readBuffer, 0, 1))));

		// 0*0 000
		// totalFileLength=565; fileOffset = 501, length = 10
		readBuffer = new byte[10];
		assertEquals(10, fs.read(501, readBuffer, 10));
		assertTrue(newfileContents.substring(501, 511).equals(
				(new String(readBuffer, 0, 10))));

		// *00 000
		// totalFileLength=565; fileOffset = 0, length = 10
		readBuffer = new byte[10];
		assertEquals(10, fs.read(0, readBuffer, 10));
		assertTrue(newfileContents.substring(0, 10).equals(
				(new String(readBuffer, 0, 10))));

		// *00 000
		// totalFileLength=565; fileOffset = 0, length = 512
		readBuffer = new byte[512];
		assertEquals(512, fs.read(0, readBuffer, 512));
		assertTrue(newfileContents.substring(0, 512).equals(
				(new String(readBuffer, 0, 512))));

		// *****************************************************************************************************************
		// test after second write
		// after write, the file length is 1077, 文件占有3个fat index
		assertEquals(newfileBuffer.length,
				fs.write(512, newfileBuffer, newfileBuffer.length));

		// after second write ,the file content is fileContent
		String fileContent = newfileContents.substring(0, 512)
				+ newfileContents;

		// *** *** ***
		// totalFileLength=1077;fileOffset = 0, length = 1077
		readBuffer = new byte[fileContent.length()];
		assertEquals(readBuffer.length,
				fs.read(0, readBuffer, readBuffer.length));
		assertTrue(fileContent.equals(new String(readBuffer))); // FIXME

		// *** 000 000
		// totalFileLength=1077;fileOffset = 0, length = 512
		readBuffer = new byte[512];
		assertEquals(readBuffer.length,
				fs.read(0, readBuffer, readBuffer.length));
		assertTrue(fileContent.substring(0, 512).equals(new String(readBuffer)));

		// 0** *** ***
		// totalFileLength=1077;fileOffset = 1, length = 1076
		readBuffer = new byte[1076];
		assertEquals(readBuffer.length,
				fs.read(1, readBuffer, readBuffer.length));
		assertTrue(fileContent.substring(1).equals(new String(readBuffer)));

		// 000 00* ***
		// totalFileLength=1077; fileOffset = 555, length = 522
		readBuffer = new byte[522];
		assertEquals(522, fs.read(555, readBuffer, 522));
		assertTrue(fileContent.substring(555, 1077).equals(
				(new String(readBuffer))));

		// 000 00* ***
		// totalFileLength=1077; fileOffset = 555, length = 523, but the real
		// read length is 522
		readBuffer = new byte[523];
		assertEquals(522, fs.read(555, readBuffer, 523));
		assertTrue(fileContent.substring(555, 1077).equals(
				(new String(readBuffer, 0, 522))));

		// 000 000 0*0
		// totalFileLength=1077; fileOffset = 1030, length = 10
		readBuffer = new byte[10];
		assertEquals(10, fs.read(1030, readBuffer, 10));
		assertTrue(fileContent.substring(1030, 1040).equals(
				(new String(readBuffer))));

		// 000 000 ***
		// totalFileLength=1077; fileOffset = 1024, length = 53
		readBuffer = new byte[53];
		assertEquals(53, fs.read(1024, readBuffer, 53));
		assertTrue(fileContent.substring(1024, 1077).equals(
				(new String(readBuffer, 0, 53))));

		// 00* *** ***
		// totalFileLength=1077; fileOffset = 511, length = 566
		readBuffer = new byte[566];
		assertEquals(566, fs.read(511, readBuffer, 566));
		assertTrue(fileContent.substring(511, 1077).equals(
				(new String(readBuffer))));

		// 00* *** **0
		// totalFileLength=1077; fileOffset = 511, length = 520
		readBuffer = new byte[520];
		assertEquals(520, fs.read(511, readBuffer, 520));
		assertTrue(fileContent.substring(511, 1031).equals(
				(new String(readBuffer))));

		// 000 0*0 000
		// totalFileLength=1077; fileOffset = 513, length = 200
		readBuffer = new byte[200];
		assertEquals(200, fs.read(513, readBuffer, 200));
		assertTrue(fileContent.substring(513, 713).equals(
				(new String(readBuffer))));

		// 000 000 0*0
		// totalFileLength=1077; fileOffset = 1025, length = 30
		readBuffer = new byte[30];
		assertEquals(30, fs.read(1025, readBuffer, 30));
		assertTrue(fileContent.substring(1025, 1055).equals(
				(new String(readBuffer))));

		// 000 000 ***
		// totalFileLength=1077; fileOffset = 1024, length = 53
		readBuffer = new byte[53];
		assertEquals(53, fs.read(1024, readBuffer, 53));
		assertTrue(fileContent.substring(1024, 1077).equals(
				(new String(readBuffer))));

		// *** 000 000
		// totalFileLength=1077; fileOffset = 0, length = 512
		readBuffer = new byte[512];
		assertEquals(512, fs.read(0, readBuffer, 512));
		assertTrue(fileContent.substring(0, 512).equals(
				(new String(readBuffer, 0, 512))));

		// ***************************************************************************************************************************
		// write the file in different offset

		// (000 000 00)* *** ***，文件占有5个fat index
		readBuffer = new byte[fileContent.length()];
		newfileBuffer = fileContent.getBytes();

		assertEquals(fileContent.length(),
				fs.write(1077, newfileBuffer, newfileBuffer.length));
		assertEquals(fileContent.length(),
				fs.read(1077, readBuffer, readBuffer.length));
		assertTrue(fileContent.equals(new String(readBuffer)));

		// (000 000 0*)* *** ***，文件占有5个fat index
		readBuffer = new byte[fileContent.length()];
		newfileBuffer = fileContent.getBytes();

		assertEquals(fileContent.length(),
				fs.write(1076, newfileBuffer, newfileBuffer.length));
		assertEquals(fileContent.length(),
				fs.read(1076, readBuffer, readBuffer.length));
		assertTrue(fileContent.equals(new String(readBuffer)));
		assertEquals(1076, fs.read(0, readBuffer, 1076));
		assertTrue(fileContent.substring(0, 1076).equals(
				new String(readBuffer, 0, 1076)));

		// (*** *** **)，文件占有3个fat index
		readBuffer = new byte[fileContent.length()];
		newfileBuffer = fileContent.getBytes();

		assertEquals(fileContent.length(),
				fs.write(0, newfileBuffer, newfileBuffer.length));
		assertEquals(fileContent.length(),
				fs.read(0, readBuffer, readBuffer.length));
		assertTrue(fileContent.equals(new String(readBuffer)));

		// (000 *** ***) **，文件占有4个fat index
		readBuffer = new byte[fileContent.length()];
		newfileBuffer = fileContent.getBytes();

		assertEquals(fileContent.length(),
				fs.write(512, newfileBuffer, newfileBuffer.length));
		assertEquals(fileContent.length(),
				fs.read(512, readBuffer, readBuffer.length));
		assertTrue(fileContent.equals(new String(readBuffer)));
		// 读取文件的前半部分，判断是否被写的动作破坏
		readBuffer = new byte[512];
		assertEquals(512, fs.read(0, readBuffer, readBuffer.length));
		assertTrue(fileContent.substring(0, 512).equals(new String(readBuffer)));

		String thirdWriteContent = "Third write content";
		// the filename2 file length is 565,and allocate two fat index for it
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename2"));
		Stat stat2 = new Stat();
		fs.setStat(stat2);
		assertNotNull(fs.setFileProperty("filename2"));
		assertEquals(
				newfileContents.length(),
				fs.write(0, newfileContents.getBytes(),
						newfileContents.length()));

		// write something at the end of the file
		assertEquals(
				thirdWriteContent.length(),
				fs.write(565, thirdWriteContent.getBytes(),
						thirdWriteContent.length()));
		readBuffer = new byte[thirdWriteContent.length()];
		assertEquals(thirdWriteContent.length(),
				fs.read(565, readBuffer, readBuffer.length));
		assertTrue(thirdWriteContent.equals(new String(readBuffer)));

		assertEquals(
				thirdWriteContent.length(),
				fs.write(560, thirdWriteContent.getBytes(),
						thirdWriteContent.length()));
		readBuffer = new byte[thirdWriteContent.length()];
		assertEquals(thirdWriteContent.length(),
				fs.read(560, readBuffer, readBuffer.length));
		assertTrue(thirdWriteContent.equals(new String(readBuffer)));

		// write something in the middle of the file
		assertEquals(
				thirdWriteContent.length(),
				fs.write(300, thirdWriteContent.getBytes(),
						thirdWriteContent.length()));
		readBuffer = new byte[thirdWriteContent.length()];
		assertEquals(thirdWriteContent.length(),
				fs.read(300, readBuffer, readBuffer.length));
		assertTrue(thirdWriteContent.equals(new String(readBuffer)));

		// write something at the begin of file
		assertEquals(
				thirdWriteContent.length(),
				fs.write(0, thirdWriteContent.getBytes(),
						thirdWriteContent.length()));
		readBuffer = new byte[thirdWriteContent.length()];
		assertEquals(thirdWriteContent.length(),
				fs.read(0, readBuffer, readBuffer.length));
		assertTrue(thirdWriteContent.equals(new String(readBuffer)));

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
		fs.setFileProperty("firstFile");

		byte[] datablockBuffer = new byte[885760];// 885760 is data block length
		for (int i = 0; i < datablockBuffer.length; i++) {
			datablockBuffer[i] = 100;
		}

		assertEquals(datablockBuffer.length, fs.write(0, datablockBuffer, datablockBuffer.length));
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
		fs.setFileProperty("newfile");

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
		assertEquals(newfileBuffer.length,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("filename2"));
		fs.close();
		assertEquals(CRFileSystem.NOT_FORMAT, fs.createFile("filename2"));
		assertEquals(CRFileSystem.NOT_FORMAT,
				fs.write(0, newfileBuffer, newfileBuffer.length));
		fs.open();
		assertEquals(CRFileSystem.FILE_EXIST, fs.createFile("filename2"));
		// read the whole file
		byte[] buffer = new byte[newfileBuffer.length];

		assertEquals(buffer.length, fs.read(0, buffer, buffer.length));
		assertTrue(newfileContents.equals(new String(buffer)));
		// read 6 bytes
		buffer = new byte[6];
		assertEquals(buffer.length, fs.read(0, buffer, buffer.length));
		assertTrue("assert".equals(new String(buffer)));

	}

	/**
	 * 
	 */
	public void test_FileSystem_deleteFile() {

		assertEquals(CRFileSystem.SUCCESS, fs.format(true, 512));

		assertEquals(CRFileSystem.FILE_NOT_EXIST, fs.deleteFileByFileName("newfile"));

		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		assertEquals(885760-512,fs.getAvailableSpace());
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile2"));
		assertEquals(885760-1024,fs.getAvailableSpace());
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile3"));
		assertEquals(885760-1536,fs.getAvailableSpace());
		
		assertEquals(CRFileSystem.SUCCESS, fs.deleteFileByFileName("newfile"));
		assertEquals(885760-1024,fs.getAvailableSpace());
		assertEquals(CRFileSystem.SUCCESS, fs.deleteFileByFileName("newfile2"));
		assertEquals(885760-512,fs.getAvailableSpace());
		assertEquals(CRFileSystem.SUCCESS, fs.deleteFileByFileName("newfile3"));
		assertEquals(885760,fs.getAvailableSpace());
		
		assertEquals(CRFileSystem.FILE_NOT_EXIST, fs.deleteFileByFileName("newfile"));
		assertEquals(CRFileSystem.FILE_NOT_EXIST, fs.deleteFileByFileName("newfile2"));

		//
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile"));
		fs.setFileProperty("newfile");
		byte[] datablockBuffer = new byte[885760];// 885760 is data block length
		for (int i = 0; i < datablockBuffer.length; i++) {
			datablockBuffer[i] = 100;
		}
		assertEquals(datablockBuffer.length, fs.write(0, datablockBuffer, datablockBuffer.length));
		assertEquals(0,fs.getAvailableSpace());
		assertEquals(CRFileSystem.DISK_FULL, fs.createFile("newfile2"));

		assertEquals(CRFileSystem.SUCCESS, fs.deleteFileByFileName("newfile"));
		assertEquals(CRFileSystem.SUCCESS, fs.createFile("newfile2"));

	}
}
