package com.chinarewards.tdd;

public interface IFilesystem {
	int format(boolean quick, int sizeOfEntry);

	boolean isFormat();

	int createFile(String fileName);

	int deleteFile(String fileName);

	int read(long offset, byte[] buffer, int length);

	/**
	 * 
	 * @param offset
	 *            the data block index
	 * @param buffer
	 *            the contents that to write
	 * @param length
	 *            the length of the contents
	 * @return -1 param invalid
	 * @return -2 not enough space
	 * @return -3 I/O wrong
	 * @return -4 other errors
	 * @return 0 write success
	 */
	int write(long offset, byte[] buffer, int length);

	boolean isExists(String path);

	/**
	 * open disk
	 */
	void open();

	/**
	 * close disk
	 */
	void close();

	char[] errorReference(int errorCode, String methodName);

	String[] listFiles();
}
