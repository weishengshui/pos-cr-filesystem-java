package com.chinarewards.tdd;

public class File implements IFile {
	private byte[] buffer; // 内容变量，用于read操作后，存放读取后的数据
	private long position;// 文件指针，当open操作后，该偏移量的值为0，当read或write后会自动加读或写的长度
	private String fileName;

	@Override
	public static IFile open(String fileName) {
		// TODO Auto-generated method stub
		return new File();
	}

	@Override
	public boolean createNewFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int write(byte[] b, int off, int len) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte[] read(int length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long seek(long pos) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean delete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long lastModified() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] list() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long length() {
		// TODO Auto-generated method stub
		return 0;
	}

}
