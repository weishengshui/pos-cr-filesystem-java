package com.chinarewards.tdd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class File implements IFile {
	private String fileName;
	private CRFileSystem crfs;

	@Override
	public int open(String fileName, String contents) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int save(String fileName, String contents) {
		// TODO Auto-generated method stub
		return 0;
	}

	public File() {
		crfs = new CRFileSystem();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public int delete() {
		// TODO Auto-generated method stub
		return 0;
	}
	public Object[] getFileProperty() {
		byte[] metadata = crfs.findMetadataByFilename(fileName);
		if (null != metadata) {
			String filename = byteToString(metadata, 0, 64, "ISO-8859-1");
			List<Object> fileProperty = new ArrayList<Object>();
			byte[] createdDate = new byte[8];
			byte[] modifiedDate = new byte[8];
			for (int index = 64; index < 82; index++) {
				createdDate[index - 64] = metadata[index];
				modifiedDate[index - 64] = metadata[index + 8];
			}
			Date cDate = new Date(dateByteToLong(createdDate));
			Date mDate = new Date(dateByteToLong(modifiedDate));
			byte[] inodeBytes = new byte[2];
			inodeBytes[0] = metadata[80];
			inodeBytes[1] = metadata[81];
			int inode = (inodeBytes[0] & 0xff) << 8 + (inodeBytes[1] & 0xff);
			Integer Inode = new Integer(inode);
			fileProperty.add(filename);
			fileProperty.add(cDate);
			fileProperty.add(mDate);
			fileProperty.add(Inode);
			return fileProperty.toArray();
		}
		return null;
	}
	
	private long dateByteToLong(byte[] bytes) {
		int length = bytes.length;
		long times = 0;
		if (length == 8) {
			for (int i = 0; i < length; i++) {
				times += (bytes[i] & 0xff) << ((length - 1) * 8 - i * 8);
			}
			return times;
		}
		return -1;
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
