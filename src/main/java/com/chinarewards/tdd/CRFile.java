package com.chinarewards.tdd;

import java.util.Date;

/**
 * 
 * @author weishengshui
 * 
 */
public class CRFile implements IFileIO {

	private CRFileSystem crfs = null;
	private long postion;
	private Stat stat = null;

	public IFileIO fopen(String filename, String mode) {
		if (null == mode) {
			return null;
		}
		if (null == filename) {
			return null;
		}
		int filenameLength = filename.trim().length();
		if (filenameLength < 1 || filenameLength > FILENAME_MAX) {
			return null;
		}

		CRFileSystem crfs = new CRFileSystem();
		if (!crfs.isFormat()) {
			return null;
		}
		CRFile file = new CRFile();
		Stat stat = new Stat();
		if ("r".equals(mode)) {
			if (!crfs.isExists(filename)) {
				return null;
			}
			file.crfs = crfs;
			if (file.stat(filename, stat) == 0) {
				file.stat = stat;
				file.postion = 0;
				return file;
			}
			return null;
		} else if ("w".equals(mode)) {

			if (!crfs.isExists(filename)) {
				if (crfs.createFile(filename) != 0) {
					return null;
				}
			}
			file.crfs = crfs;
			if (file.stat(filename, stat) != 0) {
				return null;
			}
			if (crfs.emptyFileContent() != 0) {
				return null;
			}
			file.stat = stat;
			file.postion = 0;

			return file;
		} else if ("a".equals(mode)) {
			if (!crfs.isExists(filename)) {
				if (crfs.createFile(filename) != 0) {
					return null;
				}
			}
			file.crfs = crfs;
			if (file.stat(filename, stat) != 0) {
				return null;
			}
			file.stat = stat;
			file.postion = file.stat.st_size;
			return file;
		}
		return null;
	}

	public int fclose(IFileIO stream) {

		if (isAvailableStream(stream)) {
			stream = null;
			return 0; // success
		}

		return (int) CRFile.EOF;//failure
	}

	public long ftell(IFileIO stream) {

		if (isAvailableStream(stream)) {
			return ((CRFile) stream).postion;
		}

		return CRFile.EOF;
	}

	public int fseek(IFileIO stream, long offset, int origin) {

		if (!isAvailableStream(stream)) {
			return (int) CRFile.EOF;
		}

		CRFile file = (CRFile) stream;
		if (origin == CRFile.SEEK_END) {
			file.postion = file.stat.st_size;
			return 0;
		}
		if (origin == CRFile.SEEK_CUR) {
			if (offset < 1) {
				return (int) CRFile.EOF;
			}
			if ((offset + file.postion) > file.stat.st_size) {
				return (int) CRFile.EOF;
			}
			file.postion += offset;
		}
		if (origin == CRFile.SEEK_SET) {
			if (offset < 0 || offset > file.stat.st_size) {
				return (int) CRFile.EOF;
			}
			file.postion = offset;
			return 0;
		}

		return (int) CRFile.EOF;
	}

	public int fflush(IFileIO stream) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long fread(byte[] buffer, long size, long count, IFileIO stream) {

		CRFile file = (CRFile) stream;

		if (!isAvailableStream(stream) || null == buffer) {
			return CRFile.EOF;
		}
		int length = (int) (size * count);
		if (length < 1 || length > buffer.length || buffer.length == 0
				|| size < 1 || count < 1) {
			return CRFile.EOF;
		}
		if (file.postion == file.stat.st_size) {
			return CRFile.EOF;
		}

		long readCount = file.crfs.read(file.postion, buffer, length) / size;
		file.postion += readCount * size;

		return readCount;
	}

	public long fwrite(byte[] buffer, long size, long count, IFileIO stream) {

		CRFile file = (CRFile) stream;

		if (!isAvailableStream(stream) || buffer == null) {
			return CRFile.EOF;
		}
		int length = (int) (size * count);
		if (length < 1 || length > buffer.length || buffer.length == 0
				|| size < 1 || count < 1) {
			return CRFile.EOF;
		}

		long writeCount = file.crfs.write(file.postion, buffer, length) / size;
		file.postion += writeCount * size;

		return writeCount;
	}

	public int stat(String filename, Stat buf) {
		if (!isAvailableStream(this)) {
			return (int) CRFile.EOF; // failure
		}
		crfs.setStat(buf);
		Object[] fileProperty = crfs.getFileProperty(filename);
		if (null == fileProperty) {
			return (int) CRFile.EOF; // failure
		}
		
		return 0; // success
	}

	private boolean isAvailableStream(IFileIO stream) {

		CRFile file = (CRFile) stream;

		if (null == file || null == file.crfs || null == file.stat) {
			return false;
		}

		return true;
	}
}
