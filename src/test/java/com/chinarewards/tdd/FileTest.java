package com.chinarewards.tdd;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class FileTest extends TestCase {
	FileBasedLowLevelIO llio;
	CRFileSystem fs;
	Temp file;

	protected void setUp() throws Exception {
		super.setUp();
		llio = new FileBasedLowLevelIO("E:\\fileSystem\\system.vdk");
		fs = new CRFileSystem(llio);
	}

	protected void tearDown() throws Exception {
		fs = null;
		llio = null;
		super.tearDown();

	}
	
	public void test_File_createFile(){
		fs.open();
		fs.format(true, 512);
		Temp file = new Temp(fs);
		assertEquals(Temp.SUCCESS, file.createFile("file1"));
		assertEquals(Temp.SUCCESS, file.createFile("file2"));
		assertEquals(Temp.FILE_EXIST, file.createFile("file2"));
	}
	public void test_File_Open(){
		fs.open();
		fs.format(true, 512);
		Temp file = new Temp(fs);
		
		assertEquals(Temp.SUCCESS, file.createFile("file1"));
		assertEquals(Temp.SUCCESS, file.createFile("file2"));
		assertEquals(Temp.EMPTY_FILE, file.open("file1", new StringBuffer()));
		assertEquals(Temp.EMPTY_FILE, file.open("file2", new StringBuffer()));
		
		assertEquals(Temp.FILE_NOT_EXIST, file.open("file3", new StringBuffer()));
	}
	
	public void test_File_Save(){
		
		// XXX extract this method.
		fs.open();
		fs.format(true, 512);
		Temp file = new Temp(fs);
		
		byte[] p = new byte[10];

		Temp file2 = Temp.open("c:/foobar.txt");
		file2.read(p);
		file2.read(p, 5);
		file2.close();
		
		
		assertEquals(Temp.SUCCESS, file.createFile("file1"));
		assertEquals(Temp.SUCCESS, file.save("file1","This is tne first file!"));
		assertEquals(Temp.FILE_NOT_EXIST, file.save("file2", "dsdsdsdsdsdsds"));
	}
	
	public void test_File_Length(){
		fs.open();
		fs.format(true, 512);
		Temp file = new Temp(fs);
		
		assertEquals(Temp.SUCCESS, file.createFile("file1"));
		String contents = "This is tne first file!ddddddddddddddddddddddddddddd" +
				"ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
				"ddddddddddddddddddewewedsdsdsdsdddddddddddddddads222222222222222222" +
				"222222222222222222222222222222222222222ddddddddddddddddddddddddddddd" +
				"ddddddddsdsdddddddddddddddddddddddddddddddddddddddddddddddddewewewew ewewe we" +
				" e ewe we we e efwfsfd sfs fs sdf dfsdfsfs343sdf df sdfsdf ?sdf sdf s dsdsd ds " +
				"ddddddddddddddddddewewedsdsdsdsdddddddddddddddads222222222222222222" +
				"222222222222222222222222222222222222222ddddddddddddddddddddddddddddd" +
				"dsdsdf sd fsdf sdfds fsdf wewewe";
		assertEquals(Temp.SUCCESS, file.save("file1",contents));
		assertEquals(contents.length(), file.length());
		assertEquals(Temp.SUCCESS, file.createFile("file2"));
		assertEquals(0, file.length());
	}
	
	public void test_File_Delete(){
		fs.open();
		fs.format(true, 512);
		Temp file = new Temp(fs);
		
		assertEquals(Temp.FILE_NOT_EXIST, file.delete());
		
		assertEquals(Temp.SUCCESS, file.createFile("file1"));
		assertEquals(Temp.SUCCESS, file.save("file1","This is tne first file!"));
		assertEquals(Temp.SUCCESS, file.delete());
	
		assertEquals(Temp.FILE_NOT_EXIST, file.delete());
		assertEquals(Temp.FILE_NOT_EXIST, file.save("file1","This is tne first file!"));
	}
	
	public void test_File_Save_Open(){
		fs.open();
		fs.format(true, 512);
		Temp file = new Temp(fs);
		
		String contents = "This is tne first file!ddddddddddddddddddddddddddddd" +
				"ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
				"ddddddddddddddddddewewedsdsdsdsdddddddddddddddads222222222222222222" +
				"222222222222222222222222222222222222222ddddddddddddddddddddddddddddd" +
				"ddddddddsdsdddddddddddddddddddddddddddddddddddddddddddddddddewewewew ewewe we" +
				" e ewe we we e efwfsfd sfs fs sdf dfsdfsfs343sdf df sdfsdf ?sdf sdf s dsdsd ds " +
				"ddddddddddddddddddewewedsdsdsdsdddddddddddddddads222222222222222222" +
				"222222222222222222222222222222222222222ddddddddddddddddddddddddddddd" +
				"dsdsdf sd fsdf sdfds fsdf wewewe";
		String contents2="This is the second file";
		String contents3 = "This is the third file";
		
		assertEquals(Temp.SUCCESS, file.createFile("file1"));
		assertEquals(Temp.SUCCESS, file.createFile("file2"));
		assertEquals(Temp.SUCCESS, file.createFile("file3"));
		
		assertEquals(Temp.SUCCESS, file.save("file3",contents3));
		assertEquals(Temp.SUCCESS, file.save("file1",contents));
		assertEquals(Temp.SUCCESS, file.save("file2",contents2));
		assertEquals(Temp.SUCCESS, file.save("file2",contents3));
		
		StringBuffer fileContents1 = new StringBuffer();
		StringBuffer fileContents2 = new StringBuffer();
		StringBuffer fileContents3 = new StringBuffer();
		assertEquals(Temp.SUCCESS, file.open("file1", fileContents1));
		assertTrue(contents.equals(fileContents1.toString()));
		assertEquals(Temp.SUCCESS, file.open("file2", fileContents2));
		assertTrue(contents3.equals(fileContents2.toString()));
		assertEquals(Temp.SUCCESS, file.open("file3", fileContents3));
		assertTrue(contents3.equals(fileContents3.toString()));
	}
	
	public void test_File_GetFileProperty(){
		fs.open();
		fs.format(true, 512);
		Temp file = new Temp(fs);
		
		assertEquals(Temp.SUCCESS, file.createFile("file1"));
		Object[] property = file.getFileProperty("file1");
		String fileName = (String)property[0];
		Date createdDate = (Date)property[1];
		Date modifiedDate = (Date)property[2];
		int Inode = ((Integer)property[3]).intValue();
		assertTrue(fileName.equals("file1"));
		assertEquals(0, Inode);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		System.out.println("createdDate:"+dateFormat.format(createdDate));
		System.out.println("modifiedDate:"+dateFormat.format(modifiedDate));
		try{
			Thread.sleep(10000);
		}catch (Exception e) {
			
		}
		assertEquals(Temp.SUCCESS, file.save("file1", "ddddddddddddddddddddddddddddddddd"));
		property = file.getFileProperty("file1");
		createdDate=(Date)property[1];
		modifiedDate = (Date)property[2];
		assertTrue(fileName.equals("file1"));
		assertEquals(0, Inode);
		System.out.println("createdDate:"+dateFormat.format(createdDate));
		System.out.println("modifiedDate:"+dateFormat.format(modifiedDate));
		
	}
	
	public void test_File_ListFiles(){
		fs.open();
		fs.format(true, 512);
		Temp file = new Temp(fs);
		String[] filenames = new String[]{"file1","file2","fiiile3","file4"};
		for(int i=0;i<filenames.length;i++){
			assertEquals(Temp.SUCCESS, file.createFile(filenames[i]));
		}
		String[] files = file.listFiles();
		for(int i=0;i<files.length;i++){
			assertTrue(filenames[i].equals(files[i]));
		}
		
		
	}
	
}
