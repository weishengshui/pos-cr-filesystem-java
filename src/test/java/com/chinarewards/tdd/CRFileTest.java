package com.chinarewards.tdd;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class CRFileTest extends TestCase {
	FileBasedLowLevelIO llio;
	CRFileSystem fs;
	CRFile file = new CRFile();

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

	public void test_File_Open() {
		// open before the disk format
		llio.write(0, new byte[] { 1 }, 1);
		CRFile fileObject1 = (CRFile) file.fopen("file1", "r");
		assertNull(fileObject1);
		fileObject1 = (CRFile) file.fopen("file1", "w");
		assertNull(fileObject1);
		fileObject1 = (CRFile) file.fopen("file1", "a");
		assertNull(fileObject1);

		// open after the disk format with "r" mode, but the file is not exists
		assertEquals(0, fs.format(true, 512));

		fileObject1 = (CRFile) file.fopen("file1", "r");
		assertNull(fileObject1);

		fileObject1 = (CRFile) file.fopen("file1", "w");
		assertNotNull(fileObject1);

		fileObject1 = (CRFile) file.fopen("file1", "a");
		assertNotNull(fileObject1);

		fileObject1 = (CRFile) file.fopen("file1", "r");
		assertNotNull(fileObject1);
	}

	public void test_File_Stat() {
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		CRFile fileObject1 = (CRFile) file.fopen("file1", "w");
		assertNotNull(fileObject1);

		// 查看文件的属性信息
		Stat stat = new Stat();
		assertEquals(0, file.stat("file1", stat));
		assertEquals(0, stat.st_size);
		assertEquals(0, stat.st_ino);
		assertEquals(512, stat.st_blksize);
		assertEquals(1, stat.st_blocks);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		System.out.println(dateFormat.format(new Date(stat.st_mtime)));

		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fileObject1 = (CRFile) file.fopen("file2", "w");
		assertNotNull(fileObject1);

		// 查看文件的属性信息
		stat = new Stat();
		assertEquals(0, file.stat("file2", stat));
		assertEquals(0, stat.st_size);
		assertEquals(0, stat.st_ino);
		assertEquals(512, stat.st_blksize);
		assertEquals(1, stat.st_blocks);
		System.out.println(dateFormat.format(new Date(stat.st_mtime)));

		// 查看文件的属性信息
		stat = new Stat();
		assertEquals(0, file.stat("file1", stat));
		assertEquals(0, stat.st_size);
		assertEquals(0, stat.st_ino);
		assertEquals(512, stat.st_blksize);
		assertEquals(1, stat.st_blocks);
		System.out.println(dateFormat.format(new Date(stat.st_mtime)));

	}
}
