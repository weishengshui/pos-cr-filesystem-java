package com.chinarewards.tdd;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Temp  {
	/**
	 * constants
	 */
	public static final int FAILURE = 1;
	public static final int SUCCESS = 0;
	public static final int INVALID_FILE_NAME = -1;
	public static final int FILE_EXIST = -2;
	public static final int NOT_FORMAT = -3;
	public static final int DISK_FULL = -4;
	public static final int IO_WRONG = -5;
	public static final int FILE_NOT_EXIST = -6;
	public static final int EMPTY_FILE = -7;
	public static final int FILE_CLOSED = -8;
	public static final int PARAM_INVALID = -9;

	private String fileName;
	private CRFileSystem crfs;
	private boolean isOpen = false;

	public Temp(CRFileSystem crfs) {
		isOpen = true;
		this.crfs = crfs;
	}

	public int open(String fileName, StringBuffer contents) {

		crfs.open();
		isOpen = true;

		this.fileName = fileName;
		if (crfs.isFormat()) {
			if (!crfs.isExists(fileName)) {
				return FILE_NOT_EXIST;
			}
			long offset = crfs.findDatablockOffsetByFileName(fileName);
			byte[] contentsBytes = new byte[(int) length()];
			if (contentsBytes.length == 0) {
				return EMPTY_FILE;
			}
			int result = crfs.read(offset, contentsBytes, contentsBytes.length);
			contents.append(byteToString(contentsBytes, 0,
					contentsBytes.length, "ISO-8859-1"));
			return result;
		}
		return NOT_FORMAT;

	}

	public int save(String fileName, String contents) {
		if (!isOpen) {
			return FILE_CLOSED;
		}
		if (crfs.isFormat()) {
			if (!crfs.isExists(fileName)) {
				return FILE_NOT_EXIST;
			}
			if (null == contents || contents.length() == 0) {
				return PARAM_INVALID;
			}
			long offset = crfs.findDatablockOffsetByFileName(fileName);
			return crfs.write(offset, contents.getBytes(), contents.length());
		}
		return NOT_FORMAT;
	}

	public int createFile(String fileName) {
		this.fileName = fileName;
		return crfs.createFile(fileName);
	}

	public void close() {
		isOpen = false;
		crfs.close();
	}

	public boolean delete() {
		if (!isOpen) {
			return false;
		}
		if (!crfs.isExists(fileName)) {
			return false;
		}
		return true;

	}

	public long length() {
		if (!isOpen) {
			return FILE_CLOSED;
		}
		return 0;
	}

	public Object[] getFileProperty(String fileName) {
		if (!isOpen) {
			return null;
		}
		if (!crfs.isFormat()) {
			return null;
		}
		if (!crfs.isExists(fileName)) {
			return null;
		}
		return crfs.setFileProperty(fileName);
	}

	public String[] listFiles() {
		return crfs.listFiles();
	}

	private String byteToString(byte[] bytes, int offset, int length,
			String charset) {
		try {
			int position = 0;
			for (; position < length; position++) {
				if (bytes[position] == 0x00) {
					break;
				}
			}
			if (position > 0) {
				String fileName = new String(bytes, offset, position, charset);
				return fileName;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}
}
