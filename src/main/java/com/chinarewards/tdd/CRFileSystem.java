package com.chinarewards.tdd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CRFileSystem implements IFilesystem {
	/**
	 * headerLength: the length of header fatLength: the length of fat
	 * metaDataLength: the length of meta data dataBlockLength: the length of
	 * data block wasteBlockLength: the length of waste block
	 * numberOfEntrysInDatablock: number of data entry in data block
	 * preEntryLengthInDatablock: bytes of pre data entry in data block
	 * preEntryLengthInMetaData: bytes of pre data entry in meta data
	 * preEntryLengthInFat: bytes of pre data entry in fat padded: reserve
	 */
	private FileBasedLowLevelIO llio;
	private static final int headerLength = 48;
	private static final int preEntryLengthInFat = 2;
	private static final int preEntryLengthInMetaData = 82;
	private int metaDataLength;
	private int fatLength;
	private long dataBlockLength;
	private int wasteBlockLength;
	private int nOfDataEntrys;
	private int preEntryLengthInDatablock;

	public CRFileSystem(FileBasedLowLevelIO llio) {
		this.llio = llio;
	}

	public CRFileSystem() {
	}

	@Override
	public int format(boolean quick, int sizeOfEntry) {

		if (sizeOfEntry < 0) {
			return 1;
		}
		preEntryLengthInDatablock = sizeOfEntry;
		nOfDataEntrys = (int) (llio.getSize() - headerLength)
				/ (preEntryLengthInFat * 2 + preEntryLengthInMetaData + preEntryLengthInDatablock);
		if (nOfDataEntrys < 1) {
			return 1;
		}
		fatLength = nOfDataEntrys * preEntryLengthInFat;
		metaDataLength = nOfDataEntrys * preEntryLengthInMetaData;
		dataBlockLength = nOfDataEntrys * preEntryLengthInDatablock;
		wasteBlockLength = (int) (llio.getSize() - headerLength - fatLength * 2
				- metaDataLength - dataBlockLength);

		if (!formatFsInformation()) {
			return 1;
		} else if (!formatFileAllocationTable(1)) {
			return 1;
		} else if (!formatFileAllocationTable(2)) {
			return 1;
		} else if (!formatMetaData()) {
			return 1;
		} else if (!quick) {
			if (!formatDataBlock()) {
				return 1;
			}
		}

		return 0;
	}

	private boolean formatFsInformation() {
		byte[] buffer = new byte[headerLength];// the file system information
		// block length
		byte FSType_is_Format = 0x04;// the file system type and dormat bit
		long offset = 0;
		int position = 0;
		/**
		 * To set the file system type To set the file system is format
		 */
		buffer[0] = FSType_is_Format;
		position += 1;
		/**
		 * To set the unmber of data entry
		 */
		byte[] header_nOfDataEntrys = convertIntTo_4_Bytes(nOfDataEntrys);
		for (int i = 0; i < header_nOfDataEntrys.length; i++) {
			buffer[position] = header_nOfDataEntrys[i];
			position += 1;
		}
		/**
		 * To set the bytes of per data entry
		 */
		byte[] header_preEntryLengthInDatablock = convertIntTo_2_Bytes(preEntryLengthInDatablock);
		for (int i = 0; i < header_preEntryLengthInDatablock.length; i++) {
			buffer[position] = header_preEntryLengthInDatablock[i];
			position += 1;
		}
		/**
		 * To set the first allocation table length
		 */
		byte[] header_fatLength = convertIntTo_4_Bytes(fatLength);
		for (int i = 0; i < header_fatLength.length; i++) {
			buffer[position] = header_fatLength[i];
			position += 1;
		}
		/**
		 * To set the second allocation table length
		 */
		for (int i = 0; i < header_fatLength.length; i++) {
			buffer[position] = header_fatLength[i];
			position += 1;
		}
		/**
		 * To set the Meta Data Length
		 */
		byte[] header_metaDataLength = convertIntTo_4_Bytes(metaDataLength);
		for (int i = 0; i < header_metaDataLength.length; i++) {
			buffer[position] = header_metaDataLength[i];
			position += 1;
		}
		/**
		 * To set the data block length
		 */
		byte[] header_dataBlockLength = convertLongTo_8_Bytes(dataBlockLength);
		for (int i = 0; i < header_dataBlockLength.length; i++) {
			buffer[position] = header_dataBlockLength[i];
			position += 1;
		}
		/**
		 * To set the waste block length
		 */
		byte[] header_wasteBlockLength = convertIntTo_4_Bytes(wasteBlockLength);
		for (int i = 0; i < header_wasteBlockLength.length; i++) {
			buffer[position] = header_wasteBlockLength[i];
			position += 1;
		}
		if (llio.write(offset, buffer, buffer.length) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param fatNumber
	 * @return 1 format the file allocation table one; 2 format the file
	 *         allocation table two;other return false
	 * 
	 */
	private boolean formatFileAllocationTable(int fatNumber) {
		int offset;
		byte free = 0x00;
		if (fatNumber == 1) {
			offset = headerLength;
		} else if (fatNumber == 2) {
			offset = headerLength + fatLength;
		} else {
			return false;
		}
		byte[] buffer = new byte[fatLength];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = free;
		}
		if (llio.write(offset, buffer, buffer.length) == 0) {
			return true;
		}
		return false;
	}

	private boolean formatMetaData() {
		byte free = 0x00;
		int offset = headerLength + fatLength * 2;
		byte[] buffer = new byte[metaDataLength];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = free;
		}
		if (llio.write(offset, buffer, buffer.length) == 0) {
			return true;
		}
		return false;
	}

	private boolean formatDataBlock() {
		byte free = 0x00;
		long offset = headerLength + fatLength * 2 + metaDataLength;
		byte[] buffer = new byte[preEntryLengthInDatablock];
		for (int j = 0; j < preEntryLengthInDatablock; j++) {
			buffer[j] = free;
		}
		for (int i = 0; i < nOfDataEntrys; i++) {
			if (llio.write(offset, buffer, buffer.length) != 0) {
				return false;
			}
			offset += preEntryLengthInDatablock;
		}
		return true;
	}

	@Override
	public boolean isFormat() {
		byte[] fsType_and_is_Format = new byte[1];
		if (llio.read(0, fsType_and_is_Format, fsType_and_is_Format.length) == 0) {
			if ((fsType_and_is_Format[0] >> 2) == 0x01) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param fileName
	 * @return 0 create file success; -1 if the filename is empty; -2 if the
	 *         filename already exists; -3 if the length of filename is more
	 *         than 64 bytes;-4 if the disk is not format;-5 create file fail
	 *         when there is no meta data;-6 when the disk is full; -7 I/O wrong
	 */
	@Override
	public int createFile(String fileName) {
		if (null == fileName || "".equals(fileName)) {
			return -1;
		}
		if (isExists(fileName)) {
			return -2;
		}
		byte[] fileNameByte = fileName.getBytes();
		if (fileNameByte.length > 64) {
			return -3;
		}
		if (!isFormat()) {
			return -4;
		}
		int metadataNumber = getMetaDataNumber();
		if (metadataNumber == -1) {
			return -5;
		}
		int offset = headerLength + fatLength * 2;
		offset += metadataNumber * preEntryLengthInMetaData;
		byte[] buffer = new byte[preEntryLengthInMetaData];
		int position = 0;
		for (int i = 0; i < fileNameByte.length; i++) {
			buffer[position] = fileNameByte[i];
			position++;
		}
		position = 64;
		Date now = new Date();
		long date = now.getTime();
		byte[] dateBytes = convertLongTo_8_Bytes(date);
		for (int i = 0; i < dateBytes.length; i++) {
			buffer[position] = dateBytes[i];
			position++;
		}
		position = 72;
		for (int i = 0; i < dateBytes.length; i++) {
			buffer[position] = dateBytes[i];
			position++;
		}
		position = 80;
		int fatNumber = getFatNumber();
		if (fatNumber == -1) {
			return -6;
		}
		byte[] startIndex = convertIntTo_2_Bytes(fatNumber);
		buffer[position++] = startIndex[0];
		buffer[position++] = startIndex[1];
		byte[] startNode = new byte[2];
		startNode[0] = (byte) 0xff;
		startNode[1] = (byte) 0xff;
		int fatOffset = headerLength + preEntryLengthInFat * fatNumber;
		int writeMetadata = llio.write(offset, buffer, buffer.length);
		int writeFatOne = llio.write(fatOffset, startNode, startNode.length);
		int writeFatTwo = llio.write(fatOffset + fatLength, startNode,
				startNode.length);
		if ((writeMetadata != 0) || (writeFatOne != 0) || (writeFatTwo != 0)) {
			return -7;
		}
		return 0;
	}

	/**
	 * Get a meta data number that's available
	 * 
	 * @return return the number of metadata begin from 0; when there is no
	 *         available metadata return -1
	 * 
	 */
	private int getMetaDataNumber() {
		if (isFormat()) {
			int offset = headerLength + fatLength * 2;
			for (int index = 0; index < nOfDataEntrys; index++) {
				byte[] buffer = new byte[preEntryLengthInMetaData];
				if (llio.read(offset, buffer, buffer.length) == 0) {
					if (isEntryFree(buffer)) {
						return index;
					}
				}
				offset += preEntryLengthInMetaData;
			}
			return -1;
		}
		return -1;
	}

	public long getAvailableSpace() {
		int offset = headerLength;
		long availeableSpace;
		int fatCount = 0;
		byte[] fatEntry = new byte[2];
		for (int index = 0; index < nOfDataEntrys; index++) {
			if (llio.read(offset, fatEntry, fatEntry.length) == 0) {
				if (isEntryFree(fatEntry)) {
					fatCount++;
				}
			}
			offset += preEntryLengthInFat;
		}
		availeableSpace = fatCount * preEntryLengthInDatablock;
		return availeableSpace;
	}

	/**
	 * Get a fat number that's available
	 * 
	 * @return return the fat number begin from 0
	 * @return where there is no available fat return -1
	 */
	private int getFatNumber() {
		if (isFormat()) {
			int offset = headerLength;
			byte[] buffer = new byte[preEntryLengthInFat];
			for (int index = 0; index < nOfDataEntrys; index++) {
				if (llio.read(offset, buffer, buffer.length) == 0) {
					if (isEntryFree(buffer)) {
						return index;
					}
				} else {
					return -1;
				}
				offset += preEntryLengthInFat;
			}
			return -1;
		}
		return -1;
	}

	@Override
	public int deleteFile(String fileName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int read(long offset, byte[] buffer, int length) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	/**
	 * @return 	-1 param invalid
	 * @return 	-2 not enough space
	 * @return	-3 I/O wrong
	 * @return -4 other errors
	 * @return	0 write success
	 */
	public int write(long offset, byte[] buffer, int length) {

		if (offset < 0 || null == buffer || length < 1
				|| length > buffer.length || offset > dataBlockLength
				|| (length + offset) > dataBlockLength) {
			return -1;
		}

		if (length > (getAvailableSpace() + preEntryLengthInDatablock)) {
			return -2;
		}

		int needFatCount = length / preEntryLengthInDatablock;
		int last = length % preEntryLengthInDatablock;
		if (last != 0) {
			needFatCount += 1;
		}

		long dataBlockStart = headerLength + fatLength * 2 + metaDataLength;

		long dataBlockOffset = 0;

		byte[] subbuffer;
		int bufferOffset = 0;
		byte[] EOF = new byte[2];
		EOF[0] = (byte) 0xff;
		EOF[1] = (byte) 0xff;

		int fatIndex = (int) offset / preEntryLengthInDatablock;
		byte[] fatEntryBuffer;
		int fatNum;

		long times = 0;
		for (int index = 0; index < needFatCount; index++) {
			dataBlockOffset = dataBlockStart + fatIndex
					* preEntryLengthInDatablock;
			bufferOffset = index * preEntryLengthInDatablock;

			if (index == (needFatCount - 1)) {
				if (last == 0) {
					subbuffer = subBuffer(buffer, bufferOffset,
							preEntryLengthInDatablock);
				} else {
					subbuffer = subBuffer(buffer, bufferOffset, last);
				}
				fatEntryBuffer = EOF;
				if (llio.write(dataBlockOffset, subbuffer, subbuffer.length) != 0) {
					return -3;
				}
				System.out.println(times);
				return 0; // normally:end of for
			} else {
				long time1 = System.currentTimeMillis();
				fatNum = getFatNumber();
				times += System.currentTimeMillis() - time1;
				subbuffer = subBuffer(buffer, bufferOffset,
						preEntryLengthInDatablock);
				fatEntryBuffer = convertIntTo_2_Bytes(fatNum);
				if (llio.write(dataBlockOffset, subbuffer, subbuffer.length) != 0
						|| writeEntryInFat(fatIndex, fatEntryBuffer) != 0
						|| writeEntryInFat(fatNum, EOF) != 0) {
					return -3;
				}

				fatIndex = fatNum;
			}
		}
		return -4;
	}

	/**
	 * 
	 * @param fatIndex
	 * @param buffer
	 * @return -1 param invalid
	 * @return -2 I/O wrong
	 * @return 0 success
	 */
	private int writeEntryInFat(int fatIndex, byte[] buffer) {
		if (fatIndex < 0 || fatIndex >= nOfDataEntrys || null == buffer
				|| buffer.length != 2) {
			return -1;
		}
		int fatOffset = headerLength + fatIndex * preEntryLengthInFat;
		if (llio.write(fatOffset, buffer, buffer.length) == 0
				&& llio.write(fatOffset + fatLength, buffer, buffer.length) == 0) {
			return 0;
		} else {
			return -2;
		}
	}

	/**
	 * 
	 * @param buffer
	 * 
	 * @param offset
	 *            the start index of the buffer
	 * @param length
	 *            the length of the subBuffer
	 * @return null if the param invalid
	 * @return byte[] success
	 */
	private byte[] subBuffer(byte[] buffer, int offset, int length) {
		if (null == buffer || offset < 0 || offset > buffer.length
				|| (offset + length) > buffer.length || length < 1) {
			return null;
		}
		byte[] subbuffer = new byte[length];
		for (int i = 0; i < length; i++) {
			subbuffer[i] = buffer[offset + i];
		}
		return subbuffer;
	}

	@Override
	public boolean isExists(String fileName) {
		if (isFormat()) {
			String[] files = listFiles();
			if (null == files || files.length == 0) {
				return false;
			}
			for (int index = 0; index < files.length; index++) {
				if (fileName.equals(files[index])) {
					return true;
				}
			}
			return false;
		}
		return false;
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

	@Override
	public char[] errorReference(int errorCode, String methodName) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Convert a int type data into a byte array, and the byte array length is 4
	 * 
	 * @param intNumber
	 * @return
	 */
	private byte[] convertIntTo_4_Bytes(int intNumber) {
		byte[] bytes = new byte[4];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((intNumber >> (24 - i * 8)) & 0xFF);
		}
		return bytes;
	}

	/**
	 * Convert a int type data into a byte array, and the byte array length is 2
	 * 
	 * @param intNumber
	 * @return
	 */
	private byte[] convertIntTo_2_Bytes(int intNumber) {
		byte[] bytes = new byte[2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((intNumber >> (8 - i * 8)) & 0xFF);
		}
		return bytes;
	}

	/**
	 * Convert a long type data into a byte array, and the byte array length is
	 * 8
	 * 
	 * @param longNumber
	 * @return
	 */
	private byte[] convertLongTo_8_Bytes(long longNumber) {
		byte[] bytes = new byte[8];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((longNumber >> (56 - i * 8)) & 0xFF);
		}
		return bytes;
	}

	/**
	 * Read the first file allocation table length from disk
	 * 
	 * @return file allocation table length
	 */
	public int getFatOneLength() {
		if (!isFormat()) {
			return -1;
		}
		int offset = 7;
		byte[] buffer = new byte[4];
		llio.read(offset, buffer, buffer.length);
		int fatOneLength = 0;
		for (int i = 0; i < buffer.length; i++) {
			fatOneLength += (buffer[i] & 0xff) << ((buffer.length - 1) * 8 - i * 8);
		}
		return fatOneLength;
	}

	/**
	 * Read the second file allocation table length from disk
	 * 
	 * @return file allocation table length
	 */

	public int getFatTwoLength() {
		if (!isFormat()) {
			return -1;
		}
		int offset = 11;
		byte[] buffer = new byte[4];
		llio.read(offset, buffer, buffer.length);
		int fatTwoLength = 0;
		for (int i = 0; i < buffer.length; i++) {
			fatTwoLength += (buffer[i] & 0xff) << ((buffer.length - 1) * 8 - i * 8);
		}
		return fatTwoLength;
	}

	/**
	 * Read the meta data length from disk
	 * 
	 * @return meta data length
	 */
	public int getMetaDataLength() {
		if (!isFormat()) {
			return -1;
		}
		int offset = 15;
		byte[] buffer = new byte[4];
		llio.read(offset, buffer, buffer.length);
		int metadataLength = 0;
		for (int i = 0; i < buffer.length; i++) {
			metadataLength += (buffer[i] & 0xff) << ((buffer.length - 1) * 8 - i * 8);
		}
		return metadataLength;
	}

	/**
	 * Read the data block length from disk
	 * 
	 * @return data block length
	 */
	public long getDataBlockLength() {
		if (!isFormat()) {
			return -1;
		}
		int offset = 19;
		byte[] buffer = new byte[8];
		llio.read(offset, buffer, buffer.length);
		long datablockLength = 0;
		for (int i = 0; i < buffer.length; i++) {
			datablockLength += (buffer[i] & 0xff) << ((buffer.length - 1) * 8 - i * 8);
		}
		return datablockLength;
	}

	/**
	 * Read the waste block length from disk
	 * 
	 * @return waste block length
	 */
	public int getWasteBlockLength() {
		if (!isFormat()) {
			return -1;
		}
		int offset = 27;
		byte[] buffer = new byte[4];
		llio.read(offset, buffer, buffer.length);
		int wasteblockLength = 0;
		for (int i = 0; i < buffer.length; i++) {
			wasteblockLength += (buffer[i] & 0xff) << ((buffer.length - 1) * 8 - i * 8);
		}
		return wasteblockLength;
	}

	@Override
	public String[] listFiles() {
		if (isFormat()) {
			List<String> files = new ArrayList<String>();
			// The max file name length
			int maxFileNameLength = 64;
			int offset = headerLength + fatLength * 2;
			byte[] fileNameBytes = new byte[maxFileNameLength];
			String fileName;
			String charset = "ISO-8859-1";
			for (int index = 0; index < nOfDataEntrys; index++) {
				if (llio.read(offset, fileNameBytes, maxFileNameLength) == 0) {
					if (!isEntryFree(fileNameBytes)) {
						fileName = byteToString(fileNameBytes, 0,
								maxFileNameLength, charset);
						files.add(fileName);
					}
				}
				offset += preEntryLengthInMetaData;
			}
			return files.toArray(new String[0]);
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

	public byte[] findMetadataByFilename(String fileName) {
		if (null != fileName) {
			if (isExists(fileName)) {
				int maxFileNameLength = 64;
				int offset = headerLength + fatLength * 2;
				byte[] metadataBytes = new byte[preEntryLengthInMetaData];
				String charset = "ISO-8859-1";
				for (int index = 0; index < nOfDataEntrys; index++) {
					if (llio.read(offset, metadataBytes, metadataBytes.length) == 0) {
						if (!isEntryFree(metadataBytes)) {
							if (fileName.equals(byteToString(metadataBytes, 0,
									maxFileNameLength, charset))) {
								return metadataBytes;
							}
						}
					}
					offset += preEntryLengthInMetaData;
				}
			}
			return null;
		}
		return null;
	}

	private boolean isEntryFree(byte[] buffer) {
		byte free = 0x00;
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] != free) {
				return false;
			}
		}
		return true;
	}
}
