package com.chinarewards.tdd;

/**
 * 
 * @author weishengshui
 * 
 */
public class CRFile implements IFile {

	private CRFileSystem crfs = null;
	private long postion;
	private Stat stat = null;

	public CRFile(CRFileSystem crfs) {
		this.crfs = crfs;
	}

	public CRFile() {

	}

	public IFile fopen(String filename, String mode) {
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

		if (!crfs.isFormat()) {
			return null;
		}

		CRFile file = new CRFile();
		Stat stat = new Stat();

		if ("r".equals(mode)) {
			if (!crfs.isExists(filename)) {
				return null;
			}

			if (this.stat(filename, stat) == 0) {
				file.stat = stat;
				file.postion = 0;
				return file;
			}
			return null;
		} else if ("w".equals(mode)) {
			if (!crfs.isExists(filename)) {

				if ((crfs.createFile(filename)) != 0) {
					return null;
				}
			}
			if ((this.stat(filename, stat)) != 0) {
				return null;
			}
			crfs.setStat(stat);
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

			if (this.stat(filename, stat) != 0) {
				return null;
			}
			file.stat = stat;
			file.postion = file.stat.st_size;
			return file;
		}
		return null;
	}

	public int fclose(IFile stream) {

		if (isAvailableStream(stream)) {
			CRFile file = (CRFile) stream;
			file.postion = -1;
			file.stat = null;
			return 0; // success
		}

		return (int) CRFile.EOF;// failure
	}

	public long ftell(IFile stream) {

		if (isAvailableStream(stream)) {
			return ((CRFile) stream).postion;
		}

		return CRFile.EOF;
	}

	public int fseek(IFile stream, long offset, int origin) {

		if (!isAvailableStream(stream)) {
			return (int) CRFile.EOF;
		}

		CRFile file = (CRFile) stream;
		if (origin == CRFile.SEEK_END) {
			if (offset > 0) {
				return (int) CRFile.EOF;
			}
			if ((offset + file.stat.st_size) < 0) {
				return (int) CRFile.EOF;
			}
			file.postion = file.stat.st_size + offset;
			return 0;
		}
		if (origin == CRFile.SEEK_CUR) {
			if (((offset + file.postion) < 0)
					|| ((offset + file.postion) > file.stat.st_size)) {
				return (int) CRFile.EOF;
			}
			file.postion += offset;
			return 0;
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

	public int fflush(IFile stream) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long fread(byte[] buffer, long size, long count, IFile stream) {

		CRFile file = (CRFile) stream;

		if (!isAvailableStream(stream) || null == buffer) {
			return CRFile.EOF;
		}
		int length = (int) (size * count);
		if (length < 1 || length > buffer.length || buffer.length == 0
				|| size < 1 || count < 1) {
			return CRFile.EOF;
		}
		crfs.setStat(file.stat);
		long readCount = crfs.read(file.postion, buffer, length) / size;
		if (readCount < 0) {
			return IFile.EOF;
		}
		if (readCount != 0) {
			file.postion += readCount * size;
		}
		return readCount;
	}

	public long fwrite(byte[] buffer, long size, long count, IFile stream) {

		CRFile file = (CRFile) stream;

		if (!isAvailableStream(stream) || buffer == null) {
			return CRFile.EOF;
		}
		int length = (int) (size * count);
		if (length < 1 || length > buffer.length || buffer.length == 0
				|| size < 1 || count < 1) {
			return CRFile.EOF;
		}
		crfs.setStat(file.stat);
		long writeCount = crfs.write(file.postion, buffer, length) / size;
		if (writeCount < 0) {
			return IFile.EOF;
		}
		file.postion += writeCount * size;

		return writeCount;
	}

	public int stat(String filename, Stat buf) {
		crfs.setStat(buf);
		Object[] fileProperty = crfs.setFileProperty(filename);
		if (null == fileProperty) {
			return (int) CRFile.EOF; // failure
		}

		return 0; // success
	}

	private boolean isAvailableStream(IFile stream) {

		CRFile file = (CRFile) stream;

		if (null == file || null == file.stat || file.postion < 0) {
			return false;
		}

		return true;
	}

	public int remove(String pathname) {
		int result = crfs.deleteFileByFileName(pathname);
		if (result != 0) {
			return (int) IFile.EOF;
		}
		return result;
	}

	public int remove(IFile stream) {
		if (!isAvailableStream(stream)) {
			return (int) IFile.EOF;
		}
		CRFile file = (CRFile) stream;

		int result = crfs.deleteFileByFatStat(file.stat);
		fclose(file);
		if (result == 0) {
			return result;
		} else {
			return (int) IFile.EOF;
		}
	}

}
