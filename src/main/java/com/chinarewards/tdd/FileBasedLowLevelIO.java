package com.chinarewards.tdd;

import java.io.File;
import java.io.RandomAccessFile;

public class FileBasedLowLevelIO implements ILowLevelIO {
	/**
	 * constants
	 */
	public static final int SUCCESS = 0;
	public static final int FAILURE = 1;
	private File disk;
	private long size;
	private RandomAccessFile raf;

	public FileBasedLowLevelIO(String disk) {
		this.disk = new File(disk);
		initialize();
	}

	public void initialize() {
		size = this.disk.length();
		try {
			raf = new RandomAccessFile(this.disk, "rw");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param offset
	 *            buffer length
	 * @return -1 if offset < 0; -2 if the buffer is null; -3 if length < 1; -4
	 *         if offset >= size; -5 if length > buffer.length 0 operation
	 *         success
	 */

	public int read(long offset, byte[] buffer, int length) {
		
		try {
			if (offset < 0) {
				return -1;
			}
			if (null == buffer) {
				return -2;
			}
			if (length < 1) {
				return -3;
			}
			if (this.size <= offset) {
				return -4;
			}
			if (this.size >= (offset + length)) {
				raf.seek(offset);
				if (length > buffer.length) {
					return -5;
				} else
					raf.read(buffer, 0, length);
				return 0;
			} else {
				return 1;
			}
		} catch (Exception e) {
			return 1;
		}

	}

	public int write(long offset, byte[] buffer, int length) {
		try {
			if (offset < 0 || length < 1) {
				return 1;
			} else if (this.size >= (offset + length)) {
				raf.seek(offset);
				if (length > buffer.length) {
					return 1;
				} else
					raf.write(buffer, 0, length);
				return 0;
			} else {
				return 1;
			}
		} catch (Exception e) {
			return 1;
		}
	}

	public void close() {
		try {
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long getSize() {
		return this.size;
	}

}
