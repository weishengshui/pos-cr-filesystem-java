package com.chinarewards.tdd;

public interface ILowLevelIO {
	int read(long offset, byte[] buffer, int length);

	int write(long offset, byte[] buffer, int length);

	void close();

	void initialize();
}
