package com.chinarewards.tdd;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import junit.framework.TestCase;

public class CRFileTest extends TestCase {
	FileBasedLowLevelIO llio;
	CRFileSystem fs;
	CRFile file;

	protected void setUp() throws Exception {
		super.setUp();
		llio = new FileBasedLowLevelIO("/home/wss/disk/system.vdk");
		fs = new CRFileSystem(llio);
		file = new CRFile(fs);
	}

	protected void tearDown() throws Exception {
		llio = null;
		fs = null;
		file = null;
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

		// 往文件file1写入961个字节
		assertEquals(961, file.fwrite(buffer, 1, buffer.length, fileObject1));

		// 以追加的方式打开文件file1，获得一个文件对象
		fileObject1 = (CRFile) file.fopen("file1", "a");
		assertNotNull(fileObject1);
		// 查看文件位置指针值
		assertTrue(961 == file.ftell(fileObject1));
		// 以读的方式打开文件file1，得到一个文件对象
		fileObject1 = (CRFile) file.fopen("file1", "r");
		assertNotNull(fileObject1);
		// 查看文件位置指针值
		assertTrue(0 == file.ftell(fileObject1));

		// 以写的方式打开文件file1，此时将清空文件file1的内容，文件长度为0
		fileObject1 = (CRFile) file.fopen("file1", "w");
		assertNotNull(fileObject1);
		//读取文件属性
		Stat stat = new Stat();
		assertTrue(0 == file.stat("file1", stat));
		assertTrue(0 == stat.st_size);

	}

	 public void test_File_Stat_OK() {
	 // format the disk and create a file object
	 assertEquals(0, fs.format(true, 512));
	 // 创建第一个文件
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
	 // 创建第二个文件
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
		assertEquals(contentBytes.length, file.fwrite(contentBytes, 1,
				contentBytes.length, fileObject1));
		// 查看文件位置指针
		assertEquals(contentBytes.length, file.ftell(fileObject1));

		// 将文件位置指针移至文件开始
		assertEquals(0, file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		// 查看文件位置指针
		assertEquals(0, file.ftell(fileObject1));
		contentBytes = new byte[fileContents.length()];
		// 读取文件，读取长度为文件的总长度，并对内容进行比较
		assertEquals(contentBytes.length, file.fread(contentBytes, 1,
				contentBytes.length, fileObject1));
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
		assertEquals(contentBytes.length, file.fwrite(contentBytes, 1,
				contentBytes.length, fileObject1));
		// 查看文件位置指针
		assertEquals(contentBytes.length * 2, file.ftell(fileObject1));
		// 读取文件，读取长度为文件的总长度，并对内容进行比较
		assertEquals(0, file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		// 查看文件位置指针
		assertEquals(0, file.ftell(fileObject1));
		contentBytes = new byte[fileContents.length() * 2];
		assertEquals(contentBytes.length, file.fread(contentBytes, 1,
				contentBytes.length, fileObject1));
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

	// 对fseek测试
	public void test_File_Fseek() {
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		// 创建文件"file1"
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

		// 测试：CRFile.SEEK_SET
		// 移到文件开始
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		assertTrue(0 == file.ftell(fileObject1));
		// 移到文件末尾
		assertTrue(0 == file.fseek(fileObject1, 961, (int) CRFile.SEEK_SET));
		assertTrue(961 == file.ftell(fileObject1));
		// 移到文件中间某个位置
		assertTrue(0 == file.fseek(fileObject1, 513, (int) CRFile.SEEK_SET));
		assertTrue(513 == file.ftell(fileObject1));
		// 当文件位置偏移量小于0时
		assertTrue(CRFile.EOF == file.fseek(fileObject1, -10,
				(int) CRFile.SEEK_SET));
		assertTrue(513 == file.ftell(fileObject1));
		// 当文件位置偏移量大于文件长度时
		assertTrue(CRFile.EOF == file.fseek(fileObject1, -10,
				(int) CRFile.SEEK_SET));
		assertTrue(513 == file.ftell(fileObject1));

		// 测试：CRFile.SEEK_CUR
		// 当前文件位置指针在513
		assertTrue(513 == file.ftell(fileObject1));
		// 相对当前位置往前移动10个字节
		assertTrue(0 == file.fseek(fileObject1, -10, (int) CRFile.SEEK_CUR));
		assertTrue(503 == file.ftell(fileObject1));
		// 相对当前位置往后移动50个字节
		assertTrue(0 == file.fseek(fileObject1, 50, (int) CRFile.SEEK_CUR));
		assertTrue(553 == file.ftell(fileObject1));
		// 相对当前位置往前移动554个字节，超出范围,文件位置指针值不变
		assertTrue(CRFile.EOF == file.fseek(fileObject1, -554,
				(int) CRFile.SEEK_CUR));
		assertTrue(553 == file.ftell(fileObject1));
		// 相对当前位置往后移动409个字节，超出范围,文件位置指针值不变
		assertTrue(CRFile.EOF == file.fseek(fileObject1, 409,
				(int) CRFile.SEEK_CUR));
		assertTrue(553 == file.ftell(fileObject1));

		// 测试：CRFile.SEEK_END
		// 当前文件位置指针在553
		assertTrue(553 == file.ftell(fileObject1));
		// 移到文件末尾
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_END));
		assertTrue(961 == file.ftell(fileObject1));
		// 移到倒数第50个字节
		assertTrue(0 == file.fseek(fileObject1, -50, (int) CRFile.SEEK_END));
		assertTrue(961 - 50 == file.ftell(fileObject1));
		// 把文件指针移到第一个字节
		assertTrue(0 == file.fseek(fileObject1, -961, (int) CRFile.SEEK_END));
		assertTrue(961 - 961 == file.ftell(fileObject1));
		// 参数非法时，不改变文件位置指针值
		assertTrue(CRFile.EOF == file.fseek(fileObject1, 1,
				(int) CRFile.SEEK_END));
		assertTrue(0 == file.ftell(fileObject1));
		assertTrue(CRFile.EOF == file.fseek(fileObject1, -962,
				(int) CRFile.SEEK_END));
		assertTrue(0 == file.ftell(fileObject1));

	}

	//关闭文件测试
	public void test_File_Fclose() {
		
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		
		//关闭一个空对象
		assertTrue(CRFile.EOF==file.fclose(null));
		
		
		// 创建文件"file1"
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
		//移动文件位置指针
		assertEquals(0, file.fseek(fileObject1, 0, (int)CRFile.SEEK_SET));

		//关闭文件对象
		assertEquals(0, file.fclose(fileObject1));
		//移动文件位置指针
		assertEquals(CRFile.EOF,file.fseek(fileObject1, 0, (int)CRFile.SEEK_SET));
		//关闭文件对象之后读文件
		buffer = new byte[10];
		assertEquals(CRFile.EOF, file.fread(buffer, 1, buffer.length, fileObject1));
		//读取文件位置指针值
		assertEquals(CRFile.EOF, file.ftell(fileObject1));
		//往文件写内容
		buffer = fileContents.substring(0, 10).getBytes();
		assertEquals(CRFile.EOF, file.fwrite(buffer, 1, buffer.length, fileObject1));
		//读取文件位置指针值
		assertEquals(CRFile.EOF, file.ftell(fileObject1));
	}
	// 删除文件测试
	public void test_File_Remove() {
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		// 创建文件"file1"
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
		
		//删除一不存在的文件
		assertEquals(CRFile.EOF, file.remove("file2"));
		
		// 删除存在的文件
		assertEquals(0, file.remove("file1"));


		//删除文件之后读写文件失败
		// 读取文件失败
		buffer = new byte[fileContents.length()];
		assertEquals(-1, file.fread(buffer, 1, buffer.length, fileObject1));
		// 写文件失败
		buffer = fileContents.getBytes();
		assertEquals(-1, file.fwrite(buffer, 1, buffer.length, fileObject1));

		// 现在以写的方式打开文件，文件已经不存在了
		fileObject1 = (CRFile) file.fopen("file1", "r");
		assertNull(fileObject1);

		//再一次删除
		assertEquals(CRFile.EOF, file.remove("file1"));
		
		// 再一次创建文件
		fileObject1 = (CRFile) file.fopen("file1", "w");
		assertEquals(0, file.ftell(fileObject1));

		buffer = fileContents.getBytes();

		// 将961字节的内容写入文件
		assertEquals(961, file.fwrite(buffer, 1, buffer.length, fileObject1));
		// 查看文件位置指针
		assertEquals(961, file.ftell(fileObject1));

		assertEquals(0, file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		// 查看文件位置指针
		assertEquals(0, file.ftell(fileObject1));
		// 读取文件内容，并进行比较
		buffer = new byte[fileContents.length()];
		assertEquals(961, file.fread(buffer, 1, buffer.length, fileObject1));
		assertTrue(fileContents.equals(new String(buffer)));
	}

	// 写满磁盘，因空间不足创建第二个文件失败
	public void test_File_WriteFullDisk() {
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		// 创建文件"file1"
		CRFile fileObject1 = (CRFile) file.fopen("file1", "w");
		assertNotNull(fileObject1);
		// 查看文件位置指针
		assertEquals(0, file.ftell(fileObject1));

		// 硬盘的可用空间为885760字节
		byte[] fileContents = new byte[885760];
		for (int i = 0; i < fileContents.length; i++) {
			fileContents[i] = 100;
		}
		// 将fileContents写入文件
		assertTrue(885760 == file.fwrite(fileContents, 1, fileContents.length,
				fileObject1));

		// 移动文件位置指针
		assertTrue(0 == file.fseek(fileObject1, 0, 0));
		// 查看文件位置指针
		assertEquals(0, file.ftell(fileObject1));

		// 读取文件内容并进行比较
		byte[] readContents = new byte[885760];
		assertTrue(885760 == file.fread(readContents, 1, readContents.length,
				fileObject1));
		assertTrue(Arrays.equals(fileContents, readContents));

		// 创建第二个文件:失败
		fileObject1 = (CRFile) file.fopen("file2", "w");
		assertNull(fileObject1);
	}

	// 写一个文件，内容长度大于硬盘空间：失败
	public void test_File_WriteMoreThanDiskSize() {
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		// 创建文件"file1"
		CRFile fileObject1 = (CRFile) file.fopen("file1", "w");
		assertNotNull(fileObject1);

		// 硬盘的可用空间为885760字节
		byte[] fileContents = new byte[885760 + 1];
		for (int i = 0; i < fileContents.length; i++) {
			fileContents[i] = 100;
		}
		// 将fileContents写入文件：失败
		assertTrue(CRFile.EOF == file.fwrite(fileContents, 1,
				fileContents.length, fileObject1));

		// 查看硬盘可用空间
		assertTrue((885760 - 512) == fs.getAvailableSpace());
	}

	// 在文件的任意位置读
	public void test_File_ReadAtAnyLocationOfFile() {
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		// 创建文件"file1"
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

		// 将961字节的内容写入文件
		byte[] buffer = fileContents.getBytes();
		assertEquals(961, file.fwrite(buffer, 1, buffer.length, fileObject1));

		// 判断文件内容长度
		Stat stat = new Stat();
		file.stat("file1", stat);
		assertEquals(961, fileContents.length());
		assertEquals(fileContents.length(), stat.st_size);

		// 读取文件前10个字节,并比较内容
		buffer = new byte[10];
		assertTrue(0 == file.fseek(fileObject1, 0, 0));
		assertTrue(10 == file.fread(buffer, 1, 10, fileObject1));
		assertTrue(fileContents.substring(0, 10).equals(new String(buffer)));

		// 读取文件第一个簇的最后10个字节
		buffer = new byte[10];
		assertTrue(0 == file.fseek(fileObject1, 502, 0));
		assertTrue(10 == file.fread(buffer, 1, 10, fileObject1));
		assertTrue(fileContents.substring(502, 512).equals(new String(buffer)));

		// 读取文件第二个簇的前10个字节
		buffer = new byte[10];
		assertTrue(0 == file.fseek(fileObject1, 512, 0));
		assertTrue(10 == file.fread(buffer, 1, 10, fileObject1));
		assertTrue(fileContents.substring(512, 522).equals(new String(buffer)));

		// 读取文件第二个簇的后10个字节
		buffer = new byte[10];
		assertTrue(0 == file.fseek(fileObject1, 951, 0));
		assertTrue(10 == file.fread(buffer, 1, 10, fileObject1));
		assertTrue(fileContents.substring(951, 961).equals(new String(buffer)));

		// 读取文件第一个簇与第二个簇搭界的10个字节
		buffer = new byte[10];
		assertTrue(0 == file.fseek(fileObject1, 507, 0));
		assertTrue(10 == file.fread(buffer, 1, 10, fileObject1));
		assertTrue(fileContents.substring(507, 517).equals(new String(buffer)));

		// 当要读取的长度(小于文件长度)超过文件可读取的长度时
		buffer = new byte[10];
		// 移动文件位置指针，离文件结束还有5个字节
		assertTrue(0 == file.fseek(fileObject1, 956, 0));
		// 此时真正读取的只有5个字节
		assertTrue(5 == file.fread(buffer, 1, 10, fileObject1));
		assertTrue(fileContents.substring(956, 961).equals(
				new String(buffer, 0, 5)));
		buffer = new byte[10];
		// 移动文件位置指针到文件末尾
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_END));
		// 读取了0个字节
		assertTrue(0 == file.fread(buffer, 1, 10, fileObject1));

		// 当要读取的长度超过文件的长度时
		buffer = new byte[962];
		// 移动文件位置指针到文件开始
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		// 此时读取的是整个文件的内容
		assertTrue(961 == file.fread(buffer, 1, buffer.length, fileObject1));
		assertTrue(fileContents.equals(new String(buffer, 0, 961)));
		
		// 关闭文件
		assertEquals(0, file.fclose(fileObject1));
	}

	// 在文件的任意位置写
	public void test_File_WriteAtAnyLocationOfFile() {
		// format the disk and create a file object
		assertEquals(0, fs.format(true, 512));
		// 创建文件"file1"
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

		// 将961字节的内容写入文件,961就是文件长度
		byte[] buffer = fileContents.getBytes();
		assertEquals(961, file.fwrite(buffer, 1, buffer.length, fileObject1));

		String fileContents2 = "1234567890";
		byte[] buffer2 = fileContents2.getBytes();

		// 从文件开始写10个字节
		file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET);
		assertEquals(10, file.fwrite(buffer2, 1, buffer2.length, fileObject1));
		// 写后读取比较验证
		buffer = new byte[961];
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		assertTrue(961 == file.fread(buffer, 1, buffer.length, fileObject1));
		assertTrue((fileContents2 + fileContents.substring(10, 961))
				.equals(new String(buffer)));

		// 在文件的第一个簇的最后写10个字节
		assertTrue(0 == file.fseek(fileObject1, 502, (int) CRFile.SEEK_SET));
		assertTrue(10 == file.fwrite(buffer2, 1, buffer2.length, fileObject1));
		// 写后读取比较验证
		buffer = new byte[961];
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		assertTrue(961 == file.fread(buffer, 1, buffer.length, fileObject1));
		assertTrue((fileContents2 + fileContents.substring(10, 502)
				+ fileContents2 + fileContents.substring(512, 961))
				.equals(new String(buffer)));

		// 在文件的第二个簇的开始写10个字节
		assertTrue(0 == file.fseek(fileObject1, 512, (int) CRFile.SEEK_SET));
		assertTrue(10 == file.fwrite(buffer2, 1, buffer2.length, fileObject1));
		// 写后读取比较验证
		buffer = new byte[961];
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		assertTrue(961 == file.fread(buffer, 1, buffer.length, fileObject1));
		assertTrue((fileContents2 + fileContents.substring(10, 502)
				+ fileContents2 + fileContents2 + fileContents.substring(522,
				961)).equals(new String(buffer)));

		// 从文件的倒数第10个字节开始写，写10个字节
		assertTrue(0 == file.fseek(fileObject1, 951, (int) CRFile.SEEK_SET));
		assertTrue(10 == file.fwrite(buffer2, 1, buffer2.length, fileObject1));
		// 写后读取比较验证
		buffer = new byte[961];
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		assertTrue(961 == file.fread(buffer, 1, buffer.length, fileObject1));
		assertTrue((fileContents2 + fileContents.substring(10, 502)
				+ fileContents2 + fileContents2
				+ fileContents.substring(522, 951) + fileContents2)
				.equals(new String(buffer)));

		// 在文件的第一个簇与第二个簇搭界处写10个字节
		assertTrue(0 == file.fseek(fileObject1, 507, (int) CRFile.SEEK_SET));
		assertTrue(10 == file.fwrite(buffer2, 1, buffer2.length, fileObject1));
		// 写后读取比较验证
		buffer = new byte[961];
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		assertTrue(961 == file.fread(buffer, 1, buffer.length, fileObject1));
		assertTrue((fileContents2 + fileContents.substring(10, 502)
				+ fileContents2.substring(0, 5) + fileContents2
				+ fileContents2.substring(5, 10)
				+ fileContents.substring(522, 951) + fileContents2)
				.equals(new String(buffer)));

		fileContents = new String(buffer);

		// 从文件的倒数第5个字节开始写，写10个字节。
		assertTrue(0 == file.fseek(fileObject1, 956, (int) CRFile.SEEK_SET));
		assertTrue(10 == file.fwrite(buffer2, 1, buffer2.length, fileObject1));
		// 写后读取比较验证
		buffer = new byte[966];
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		assertTrue(966 == file.fread(buffer, 1, buffer.length, fileObject1));
		assertTrue((fileContents.substring(0, 956) + fileContents2)
				.equals(new String(buffer)));
		// 读取文件属性，验证文件长度信息
		Stat stat = new Stat();
		file.stat("file1", stat);
		assertTrue(966 == stat.st_size);

		fileContents = new String(buffer);

		// 从文件末尾开始写，写10个字节
		assertTrue(0 == file.fseek(fileObject1, 966, (int) CRFile.SEEK_SET));
		assertTrue(10 == file.fwrite(buffer2, 1, buffer2.length, fileObject1));
		// 写后读取比较验证
		buffer = new byte[976];
		assertTrue(0 == file.fseek(fileObject1, 0, (int) CRFile.SEEK_SET));
		assertTrue(976 == file.fread(buffer, 1, buffer.length, fileObject1));
		assertTrue((fileContents + fileContents2).equals(new String(buffer)));
		// 读取文件属性，验证文件长度信息
		file.stat("file1", stat);
		assertTrue(976 == stat.st_size);

		// 关闭文件
		assertEquals(0, file.fclose(fileObject1));
	}

}
