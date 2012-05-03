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

		// 以读的方式打开一个不存在的文件，得到一个null对象
		fileObject1 = (CRFile) file.fopen("file1", "r");
		assertNull(fileObject1);

		// 以写的方式打开一个不存在的文件，创建这个文件，获得一个文件对象
		fileObject1 = (CRFile) file.fopen("file1", "w");
		assertNotNull(fileObject1);

		// 以追加的方式打开一个文件，获得一个文件对象
		fileObject1 = (CRFile) file.fopen("file1", "a");
		assertNotNull(fileObject1);
		// 以读的方式打开一个存在的文件，得到一个文件对象
		fileObject1 = (CRFile) file.fopen("file1", "r");
		assertNotNull(fileObject1);
	}

	public void test_File_Stat_OK() {
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		//创建第一个文件
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
		//创建第二个文件
		fileObject1 = (CRFile) file.fopen("file2", "w");
		assertNotNull(fileObject1);

		// 查看文件的属性信息
		stat = new Stat();
		assertEquals(0, file.stat("file2", stat));
		assertEquals(0, stat.st_size);
		assertEquals(1, stat.st_ino);
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

	public void test_File_Fwrite_Fseek_Fread() {
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		CRFile fileObject1 = (CRFile) file.fopen("file1", "w");
		assertNotNull(fileObject1);
		// 查看文件位置指针
		assertEquals(0, file.ftell(fileObject1));

		// 往空文件中写入一段内容
		String fileContents = "First time to write the file";
		byte[] contentBytes = fileContents.getBytes();
		assertEquals(contentBytes.length,
				file.fwrite(contentBytes, 1, contentBytes.length, fileObject1));
		// 查看文件位置指针
		assertEquals(contentBytes.length, file.ftell(fileObject1));

		// 将文件位置指针移至文件开始
		assertEquals(0, file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		// 查看文件位置指针
		assertEquals(0, file.ftell(fileObject1));
		contentBytes = new byte[fileContents.length()];
		// 读取文件，读取长度为文件的总长度，并对内容进行比较
		assertEquals(contentBytes.length,
				file.fread(contentBytes, 1, contentBytes.length, fileObject1));
		assertTrue(fileContents.equals(new String(contentBytes)));
		// 查看文件位置指针
		assertEquals(contentBytes.length, file.ftell(fileObject1));
		// 关闭文件对象
		assertEquals(0, file.fclose(fileObject1));

		// 已追加的方式打开文件
		fileObject1 = (CRFile) file.fopen("file1", "a");
		assertNotNull(fileObject1);
		// 查看文件位置指针
		assertEquals(contentBytes.length, file.ftell(fileObject1));

		// 第二次向文件写入内容
		contentBytes = fileContents.getBytes();
		assertEquals(contentBytes.length,
				file.fwrite(contentBytes, 1, contentBytes.length, fileObject1));
		// 查看文件位置指针
		assertEquals(contentBytes.length * 2, file.ftell(fileObject1));
		// 读取文件，读取长度为文件的总长度，并对内容进行比较
		assertEquals(0, file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		// 查看文件位置指针
		assertEquals(0, file.ftell(fileObject1));
		contentBytes = new byte[fileContents.length() * 2];
		assertEquals(contentBytes.length,
				file.fread(contentBytes, 1, contentBytes.length, fileObject1));
		// 查看文件位置指针
		assertEquals(contentBytes.length, file.ftell(fileObject1));
		assertTrue((fileContents + fileContents)
				.equals(new String(contentBytes)));

	}

	// 写一个文件，长度超过一个簇
	public void test_File_Fwrite_MoreThanOneDatablockEntry() {
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		CRFile fileObject1 = (CRFile) file.fopen("file1", "w");
		assertNotNull(fileObject1);
		// 查看文件位置指针
		assertEquals(0, file.ftell(fileObject1));
		String fileContents = "/2/12 1:33:44 PM CST: Eclipse is running in a JRE, but a JDK is required"
				+ "Some Maven plugins may not work when importing projects or updating source folders."
				+ "5/2/12 1:33:47 PM CST: Updating index central|http://repo1.maven.org/maven2"
				+ "5/2/12 1:33:51 PM CST: Downloading d9d714e11cb097b3ffcec91cccc65d3e : nexus-maven-repository-index.properties"
				+ "5/2/12 1:33:51 PM CST: Downloaded Repository[d9d714e11cb097b3ffcec91cccc65d3e|http://repo1.maven.org/maven2/.index]/nexus-maven-repository-index.properties"
				+ "5/2/12 1:35:27 PM CST: Unable to download Repository[d9d714e11cb097b3ffcec91cccc65d3e|http://repo1.maven.org/maven2/.index]/nexus-maven-repository-index.gz: java.io.IOException: The server did not respond within the configured timeout."
				+ "5/2/12 1:35:27 PM CST: Unable to update index for central|http://repo1.maven.org/maven2"
				+ "5/2/12 1:47:47 PM CST: Maven Builder: AUTO_BUILD"
				+ "5/2/12 1:51:33 PM CST: Maven Builder: AUTO_BUILD"
				+ "5/2/12 1:54:57 PM CST: Maven Builder: AUTO_BUILD ";
		assertEquals(961, fileContents.length());
		byte[] buffer = fileContents.getBytes();

		// 将961字节的内容写入文件
		assertEquals(961, file.fwrite(buffer, 1, buffer.length, fileObject1));
		// 查看文件位置指针
		assertEquals(961, file.ftell(fileObject1));

		assertEquals(0, file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		// 查看文件位置指针
		assertEquals(0, file.ftell(fileObject1));
		buffer = new byte[fileContents.length()];
		// 读取文件内容
		assertEquals(961, file.fread(buffer, 1, buffer.length, fileObject1));
		assertTrue(fileContents.equals(new String(buffer)));
		// 查看文件位置指针
		assertEquals(961, file.ftell(fileObject1));

	}
}
