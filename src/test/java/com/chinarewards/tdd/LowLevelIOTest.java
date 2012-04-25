package com.chinarewards.tdd;

import java.util.Arrays;

import junit.framework.TestCase;

public class LowLevelIOTest extends TestCase {
	FileBasedLowLevelIO llio;

	protected void setUp() throws Exception {
		super.setUp();
		llio = new FileBasedLowLevelIO("E:\\fileSystem\\system.vdk");
	}

	protected void tearDown() throws Exception {
		llio = null;
		super.tearDown();
	}

	// case: write from addr #0, length: 5 bytes, read from #1, length: 4
	public void test_LowLevelIO_Write0_5Read1_4() {
		byte[] buffer = { 1, 2, 3, 4, 5 };
		assertEquals(FileBasedLowLevelIO.SUCCESS, llio.write(0, buffer, 5));
		byte[] buffer2 = new byte[4];
		assertEquals(FileBasedLowLevelIO.SUCCESS, llio.read(1, buffer2, 4));
		assertTrue(Arrays.equals(new byte[] { 2, 3, 4, 5 }, buffer2));
	}

	// case: write from addr #-1 (bad, minimum addr = 0), return error code = 1
	// (out of bound)
	public void test_LowLevelIO_Write_1() {
		byte[] buffer = { 1, 2, 3, 4, 5 };
		assertEquals(FileBasedLowLevelIO.FAILURE, llio.write(-1, buffer, 5));
	}

	// case: erased all bytes to 1, write from addr # 18, length = 5 byte,
	// return error code = 1, read from #18, #19 == 1
	public void test_LowLevelIO_WriteAll1Write18_5Read18_2() {
		byte[] buffer = new byte[1024 * 1024];
		for (int index = 0; index < buffer.length; index++) {
			buffer[index] = 1;
		}
		assertEquals(FileBasedLowLevelIO.SUCCESS,
				llio.write(0, buffer, 1024 * 1024));
		byte[] buffer2 = { 2, 3, 4, 5, 6 };
		assertEquals(1024 * 1024, llio.getSize());
		assertEquals(FileBasedLowLevelIO.FAILURE,
				llio.write(1024 * 1024 - 2, buffer2, 5));
		byte[] buffer3 = new byte[2];
		assertEquals(FileBasedLowLevelIO.SUCCESS,
				llio.read(1024 * 1024 - 2, buffer3, 2));
		assertTrue(Arrays.equals(new byte[] { 1, 1 }, buffer3));

	}

	// case: write at addr #19 , length = 1 (OK)_
	public void test_LowLevelIO_Write19_1() {
		assertEquals(FileBasedLowLevelIO.SUCCESS,
				llio.write(1024 * 1024 - 1, new byte[] { 22 }, 1));
	}

	// case: write at addr #20, length =1 (BAD)
	public void test_LowLevelIO_Wite20_1() {
		assertEquals(FileBasedLowLevelIO.FAILURE,
				llio.write(1024 * 1024, new byte[] { 2 }, 1));
	}

	// case: get the disk size
	public void test_LowLevelIO_Size() {
		assertEquals(1024 * 1024, llio.getSize());
	}

	// case: after close. read(BAD),write(BAD)
	public void test_LowLevelIO_Close() {
		llio.close();
		assertEquals(FileBasedLowLevelIO.FAILURE, llio.read(0, new byte[2], 2));
		assertEquals(FileBasedLowLevelIO.FAILURE,
				llio.write(5, new byte[] { 3, 2, 3, 4 }, 4));
	}

}
