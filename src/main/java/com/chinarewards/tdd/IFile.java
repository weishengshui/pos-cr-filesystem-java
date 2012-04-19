package com.chinarewards.tdd;

public interface IFile {
	int open(String fileName,String contents);
	void close();
	int delete();
	int save(String fileName, String contents);
}
