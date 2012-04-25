package com.chinarewards.tdd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CRFileSystem implements IFilesystem {

	/**
	 * constants
	 */
	public static final int FAILURE = 1;
	public static final int SUCCESS = 0;
	public static final int PARAM_INVALID = -1;
	public static final int FILE_EXIST = -2;
	public static final int NOT_FORMAT = -3;
	public static final int DISK_FULL = -4;
	public static final int IO_WRONG = -5;
	public static final int FILE_NOT_EXIST = -6;
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
	private byte[] fatBitMap;

	public CRFileSystem(FileBasedLowLevelIO llio) {
		this.llio = llio;
	}

	public CRFileSystem() {
	}

	public int format(boolean quick, int sizeOfEntry) {
		if (sizeOfEntry < 0) {
			return PARAM_INVALID;
		}
		preEntryLengthInDatablock = sizeOfEntry;
		nOfDataEntrys = (int) (llio.getSize() - headerLength)
				/ (preEntryLengthInFat * 2 + preEntryLengthInMetaData + preEntryLengthInDatablock);
		if (nOfDataEntrys < 1) {
			return PARAM_INVALID;
		}
		fatLength = nOfDataEntrys * preEntryLengthInFat;
		metaDataLength = nOfDataEntrys * preEntryLengthInMetaData;
		dataBlockLength = nOfDataEntrys * preEntryLengthInDatablock;
		wasteBlockLength = (int) (llio.getSize() - headerLength - fatLength * 2
				- metaDataLength - dataBlockLength);
		fatBitMap = new byte[nOfDataEntrys];
		for (int i = 0; i < fatBitMap.length; i++) {
			fatBitMap[i] = 0;
		}
		if (!formatFsInformation()) {
			return FAILURE;
		} else if (!formatFileAllocationTable(1)) {
			return FAILURE;
		} else if (!formatFileAllocationTable(2)) {
			return FAILURE;
		} else if (!formatMetaData()) {
			return FAILURE;
		} else if (quick) {
			if (!formatDataBlock()) {
				return FAILURE;
			}
		}

		return SUCCESS;
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
	 *            1 format the file allocation table one; 2 format the file
	 *            allocation table two;
	 * @return
	 * 
	 */
	private boolean formatFileAllocationTable(int fatNumber) {
		int offset;
		byte free = (byte) 0xfe;
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

	public int createFile(String fileName) {
		if (null == fileName || "".equals(fileName.trim())) {
			return PARAM_INVALID;
		}
		if (isExists(fileName)) {
			return FILE_EXIST;
		}
		byte[] fileNameByte = fileName.trim().getBytes();
		if (fileNameByte.length > 64) {
			return PARAM_INVALID;
		}
		if (!isFormat()) {
			return NOT_FORMAT;
		}
		int metadataNumber = getMetaDataNumber();
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
			return DISK_FULL;
		}
		byte[] startIndex = convertIntTo_2_Bytes(fatNumber);
		buffer[position++] = startIndex[0];
		buffer[position++] = startIndex[1];
		byte[] startNode = new byte[preEntryLengthInFat];
		for (int i = 0; i < preEntryLengthInFat; i++) {
			startNode[i] = (byte) 0xff;
		}
		int writeMetadata = llio.write(offset, buffer, buffer.length);
		int writeStartFat = writeEntryInFat(fatNumber, startNode);
		if ((writeMetadata != 0) || (writeStartFat != 0)) {
			return IO_WRONG;
		}
		return SUCCESS;
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
		long availeableSpace;
		int fatCount = 0;
		for (int index = 0; index < nOfDataEntrys; index++) {
			if (isFatEntryFree(index) == 0) {
				fatCount++;
			}
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
			for (int index = 0; index < nOfDataEntrys; index++) {
				if (fatBitMap[index] == 0) {
					return index;
				}
			}
			return -1;
		}
		return -1;
	}

	public long findDatablockOffsetByFileName(String fileName) {
		if (!isFormat()) {
			return NOT_FORMAT;
		}
		if (isRightFilename(fileName)) {
			if (isExists(fileName)) {
				byte[] metadataEntry = findMetadataEntryByFilename(fileName);
				return (((metadataEntry[80] & 0xff) << 8) + (metadataEntry[81] & 0xff))
						* preEntryLengthInDatablock;
			} else {
				return FILE_NOT_EXIST;
			}
		} else {
			return PARAM_INVALID;
		}
	}

	public int deleteFile(String fileName) {
		if (!isFormat()) {
			return NOT_FORMAT;
		}
		if (isRightFilename(fileName)) {
			if (isExists(fileName)) {
				int metadataEntryIndex = findMetadataEntryIndexByFilename(fileName);
				byte[] metadataEntry = findMetadataEntryByFilename(fileName);
				byte[] startIndexBytes = new byte[preEntryLengthInFat];
				startIndexBytes[0] = metadataEntry[preEntryLengthInMetaData - 2];
				startIndexBytes[1] = metadataEntry[preEntryLengthInMetaData - 1];
				int startIndexInt = ((startIndexBytes[0] & 0xff) << 8)
						+ (startIndexBytes[1] & 0xff);
				emptyFileContent(startIndexInt * preEntryLengthInDatablock);
				for (int i = 0; i < metadataEntry.length; i++) {
					metadataEntry[i] = 0x00;
				}
				if (llio.write(headerLength + fatLength * 2
						+ metadataEntryIndex * preEntryLengthInMetaData,
						metadataEntry, metadataEntry.length) != 0) {
					return IO_WRONG;
				}
				return SUCCESS;
			} else {
				return FILE_NOT_EXIST;
			}
		} else {
			return PARAM_INVALID;
		}
	}

	private boolean isRightFilename(String fileName) {
		if (null == fileName || "".equals(fileName.trim())) {
			return false;
		}
		if (fileName.getBytes().length > 64) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param offset
	 *            : data block offset begin 0
	 * 
	 * @return -1 param invalid
	 * @return -2 Read more than the length of the disk space
	 * @return -3 the offset is not the begin of a file
	 * @return -4 Read more than the length of the file
	 * @return -5 I/O wrong
	 * @return 0 write success
	 */
	public int read(long offset, byte[] buffer, int length) {
		if (offset < 0 || null == buffer || length < 1
				|| length > buffer.length || offset > dataBlockLength
				|| (length + offset) > dataBlockLength
				|| offset % preEntryLengthInDatablock != 0) {
			return PARAM_INVALID;
		}
		if (length > (getAvailableSpace() + preEntryLengthInDatablock)) {
			return PARAM_INVALID;
		}

		if (!isFileBegin(offset)) {
			return PARAM_INVALID;
		}
		if (length > getFileLength(offset)) {
			return PARAM_INVALID;
		}
		int last = length % preEntryLengthInDatablock;
		int readFatEntryCount = length / preEntryLengthInDatablock;
		readFatEntryCount = (last == 0) ? (readFatEntryCount)
				: (readFatEntryCount + 1);
		int fatIndex = (int) offset / preEntryLengthInDatablock;
		int fatOffset = headerLength + fatIndex * preEntryLengthInFat;
		int datablockBegin = headerLength + fatLength * 2 + metaDataLength;
		int datablockOffset = datablockBegin + fatIndex
				* preEntryLengthInDatablock;
		byte[] fatEntryContent = new byte[preEntryLengthInFat];
		for (int i = 0; i < readFatEntryCount; i++) {
			byte[] oneDatablockEntry;
			if (i == (readFatEntryCount - 1)) {

				if (last == 0) {
					oneDatablockEntry = new byte[preEntryLengthInDatablock];
					if (llio.read(datablockOffset, oneDatablockEntry,
							oneDatablockEntry.length) != 0) {
						return IO_WRONG;
					}
				} else {
					oneDatablockEntry = new byte[last];
					if (llio.read(datablockOffset, oneDatablockEntry,
							oneDatablockEntry.length) != 0) {
						return IO_WRONG;
					}
				}
			} else {
				oneDatablockEntry = new byte[preEntryLengthInDatablock];
				if (llio.read(datablockOffset, oneDatablockEntry,
						oneDatablockEntry.length) != 0) {
					return IO_WRONG;
				}
			}
			for (int fileContentIndex = i * preEntryLengthInDatablock, index = 0; index < oneDatablockEntry.length; index++) {
				buffer[fileContentIndex++] = oneDatablockEntry[index];
			}
			if (llio.read(fatOffset, fatEntryContent, fatEntryContent.length) == 0) {
				if (fatEntryContent[0] == (byte) 0xff
						&& fatEntryContent[1] == (byte) 0x00) {
					return SUCCESS; // Normal exit ***************************
				} else {
					fatIndex = ((fatEntryContent[0] & 0xff) << 8)
							+ (fatEntryContent[1] & 0xff);
					fatOffset = headerLength + fatIndex * preEntryLengthInFat;
					datablockOffset = datablockBegin + fatIndex
							* preEntryLengthInDatablock;
				}
			} else {
				return IO_WRONG;
			}
		}

		// int
		return SUCCESS;
	}

	/**
	 * 
	 * @param offset
	 * @return -1 I/O wrong
	 */
	private long getFileLength(long offset) {
		if (!isFileBegin(offset)) {
			return PARAM_INVALID;
		}
		int fatStartIndex = (int) offset / preEntryLengthInDatablock;

		int nextFatEntryIndex = -1;
		long fileLength = 0;

		byte[] fatEntryBytes = new byte[preEntryLengthInFat];
		int fatOffset = headerLength + fatStartIndex * preEntryLengthInFat;
		int fatCount = 1;
		int realLengthOfLastDatablockEntryInFile = -1;
		if (llio.read(fatOffset, fatEntryBytes, fatEntryBytes.length) == 0) {
			while (!Arrays.equals(fatEntryBytes, new byte[] { (byte) 0xff,
					(byte) 0xff })) {
				fatCount++;
				nextFatEntryIndex = ((fatEntryBytes[0] & 0xff) << 8)
						+ (fatEntryBytes[1] & 0xff);
				fatOffset = headerLength + nextFatEntryIndex * 2;
				if (llio.read(fatOffset, fatEntryBytes, fatEntryBytes.length) != 0) {
					return -1;
				}
			}
			long lastDatablockEntryOfFileIndex;
			if (fatCount == 1) {
				lastDatablockEntryOfFileIndex = headerLength + fatLength * 2
						+ metaDataLength + fatStartIndex
						* preEntryLengthInDatablock;
			} else {
				lastDatablockEntryOfFileIndex = headerLength + fatLength * 2
						+ metaDataLength + nextFatEntryIndex
						* preEntryLengthInDatablock;
			}
			byte[] lastDatablockEntryOfFileBytes = new byte[preEntryLengthInDatablock];
			if (llio.read(lastDatablockEntryOfFileIndex,
					lastDatablockEntryOfFileBytes,
					lastDatablockEntryOfFileBytes.length) == 0) {
				for (realLengthOfLastDatablockEntryInFile = 0; realLengthOfLastDatablockEntryInFile < lastDatablockEntryOfFileBytes.length; realLengthOfLastDatablockEntryInFile++) {
					if (lastDatablockEntryOfFileBytes[realLengthOfLastDatablockEntryInFile] == (byte) 0x00) {
						break;
					}
				}
			} else {
				return -1;
			}
		} else {
			return -1;
		}
		fileLength = (fatCount - 1) * preEntryLengthInDatablock
				+ realLengthOfLastDatablockEntryInFile;
		return fileLength;
	}

	public long getFileLength(String fileName) {
		return getFileLength(findDatablockOffsetByFileName(fileName));
	}

	/**
	 * @param offset
	 *            : data block offset begin 0
	 */
	public int write(long offset, byte[] buffer, int length) {

		if (offset < 0 || null == buffer || length < 1
				|| length > buffer.length || offset > dataBlockLength
				|| offset % preEntryLengthInDatablock != 0) {
			return PARAM_INVALID;
		}

		if (emptyFileContent(offset) != 0) {
			return PARAM_INVALID;
		}
		if (length > (getAvailableSpace())) {
			return PARAM_INVALID;
		}
		if (!isFileBegin(offset)) {
			return PARAM_INVALID;
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

		int currentIndex = (int) offset / preEntryLengthInDatablock;

		if (writeEntryInFat(currentIndex, EOF) != 0) {
			return IO_WRONG;
		}
		Date now = new Date();
		byte[] modifiedDate = convertLongTo_8_Bytes(now.getTime());
		int metadataEntryIndex = findMetadataEntryIndexByFatIndex(currentIndex);
		if (llio.write(headerLength + fatLength * 2 + metadataEntryIndex
				* preEntryLengthInMetaData + 72, modifiedDate,
				8) != 0) {
			return IO_WRONG;
		}
		byte[] fatEntryBuffer;
		int nextIndex;

		for (int index = 0; index < needFatCount; index++) {
			dataBlockOffset = dataBlockStart + currentIndex
					* preEntryLengthInDatablock;
			bufferOffset = index * preEntryLengthInDatablock;

			if (index == (needFatCount - 1)) {
				if (last == 0) {
					subbuffer = subBuffer(buffer, bufferOffset,
							preEntryLengthInDatablock);
					if ((llio.write(dataBlockOffset, subbuffer,
							subbuffer.length) != 0)
							|| (writeEntryInFat(currentIndex, EOF) != 0)) {
						return IO_WRONG;
					}
				} else {
					subbuffer = subBuffer(buffer, bufferOffset, last);
					if (llio.write(dataBlockOffset, subbuffer, subbuffer.length) != 0
							|| writeEntryInFat(currentIndex, EOF) != 0) {
						return IO_WRONG;
					}
					// end of the file when last datablock entry
					if (llio.write(dataBlockOffset + subbuffer.length,
							new byte[] { 0x00 }, 1) != 0) {
						return IO_WRONG;
					}
				}
				return SUCCESS; // normally:end of for
			} else {
				nextIndex = getFatNumber();
				fatBitMap[nextIndex] = 1;
				subbuffer = subBuffer(buffer, bufferOffset,
						preEntryLengthInDatablock);
				fatEntryBuffer = convertIntTo_2_Bytes(nextIndex);
				if (llio.write(dataBlockOffset, subbuffer, subbuffer.length) != 0
						|| writeEntryInFat(currentIndex, fatEntryBuffer) != 0
						|| writeEntryInFat(nextIndex, EOF) != 0) {
					return IO_WRONG;
				}
				currentIndex = nextIndex;
			}
		}
		return PARAM_INVALID;
	}

	private int findMetadataEntryIndexByFatIndex(int fatIndex) {
		if (!isFormat()) {
			return NOT_FORMAT;
		}

		if (fatIndex > -1 && fatIndex < nOfDataEntrys) {
			byte[] startFatIndexBytes = new byte[preEntryLengthInFat];
			int metadataOffset = headerLength + fatLength * 2;
			for (int index = 0; index < nOfDataEntrys; index++) {
				if (llio.read(metadataOffset + 80, startFatIndexBytes,
						startFatIndexBytes.length) == 0) {
					if (fatIndex == (((startFatIndexBytes[0] & 0xff) << 8) + (startFatIndexBytes[1] & 0xff))) {
						return index;
					}
				} else {
					return IO_WRONG;
				}
				metadataOffset += preEntryLengthInMetaData;
			}
			return FAILURE;
		} else {
			return PARAM_INVALID;
		}
	}

	private int emptyFileContent(long offset) {
		if (!isFileBegin(offset)) {
			return PARAM_INVALID;
		}
		int currentIndex = (int) offset / preEntryLengthInDatablock;
		int nextIndex;
		byte[] free = new byte[preEntryLengthInFat];
		for (int i = 0; i < free.length; i++) {
			free[i] = (byte) 0xfe;
		}
		byte[] indexBuffer = new byte[preEntryLengthInFat];
		if (llio.read(headerLength + currentIndex * preEntryLengthInFat,
				indexBuffer, indexBuffer.length) == 0) {
			while (indexBuffer[0] != (byte) 0xff
					|| indexBuffer[1] != (byte) 0xff) {
				nextIndex = ((indexBuffer[0] & 0xff) << 8)
						+ (indexBuffer[1] & 0xff);
				if (writeEntryInFat(currentIndex, free) != 0) {
					return FAILURE;
				}
				currentIndex = nextIndex;
				if (llio.read(
						headerLength + currentIndex * preEntryLengthInFat,
						indexBuffer, indexBuffer.length) != 0) {
					return FAILURE;
				}
			}
			if (writeEntryInFat(currentIndex, free) != 0) {
				return FAILURE;
			}
		}
		return SUCCESS;
	}

	/**
	 * check the offset is the file begin
	 */
	private boolean isFileBegin(long offset) {
		String[] files = listFiles();
		if (files == null) {
			return false;
		}
		int fatIndex = (int) offset / preEntryLengthInDatablock;
		for (int i = 0; i < files.length; i++) {
			byte[] metadataEntry = findMetadataEntryByFilename(files[i]);
			byte[] fileStartIndexByte = subBuffer(metadataEntry, 80, 2);
			int fileStartIndexInt = ((fileStartIndexByte[0] & 0xff) << 8)
					+ (fileStartIndexByte[1] & 0xff);
			if (fatIndex == fileStartIndexInt) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param fatIndex
	 *            fat entry index
	 * @param buffer
	 * @return -1 param invalid
	 * @return -2 I/O wrong
	 * @return 0 success
	 */
	private int writeEntryInFat(int fatIndex, byte[] buffer) {
		if (fatIndex < 0 || fatIndex >= nOfDataEntrys || null == buffer
				|| buffer.length != preEntryLengthInFat) {
			return -1;
		}
		byte[] free = new byte[preEntryLengthInFat];
		for (int i = 0; i < preEntryLengthInFat; i++) {
			free[i] = (byte) 0xfe;
		}
		int fatOffset = headerLength + fatIndex * preEntryLengthInFat;
		if (llio.write(fatOffset, buffer, buffer.length) == 0
				&& llio.write(fatOffset + fatLength, buffer, buffer.length) == 0) {
			if (buffer[0] == free[0] && buffer[1] == free[1]) {
				fatBitMap[fatIndex] = 0;
			} else {
				fatBitMap[fatIndex] = 1;
			}
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

	public boolean isExists(String fileName) {
		if (isFormat()) {
			if (null == fileName || "".equals(fileName.trim())) {
				return false;
			}
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
		byte[] bytes = new byte[preEntryLengthInFat];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((intNumber >> (8 - i * 8)) & 0xff);
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
			bytes[i] = (byte) (((longNumber >>> (56 - i * 8))) & 0xff);
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
			return NOT_FORMAT;
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
			return NOT_FORMAT;
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
			return NOT_FORMAT;
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
			return NOT_FORMAT;
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
			return NOT_FORMAT;
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
				times<<=8;
				times ^= (long)bytes[i] & 0xff;
			}
			return times;
		}
		return -1;
	}

	public Object[] getFileProperty(String fileName) {
		byte[] metadata = findMetadataEntryByFilename(fileName);
		if (null != metadata) {
			String filename = byteToString(metadata, 0, 64, "ISO-8859-1");
			List<Object> fileProperty = new ArrayList<Object>();
			byte[] createdDate = new byte[8];
			byte[] modifiedDate = new byte[8];
			for (int index = 64; index < 72; index++) {
				createdDate[index - 64] = metadata[index];
				modifiedDate[index - 64] = metadata[index + 8];
			}
			Date cDate = new Date(dateByteToLong(createdDate));
			Date mDate = new Date(dateByteToLong(modifiedDate));
			byte[] inodeBytes = new byte[2];
			inodeBytes[0] = metadata[80];
			inodeBytes[1] = metadata[81];
			int inode = ((inodeBytes[0] & 0xff) << 8) + (inodeBytes[1] & 0xff);
			Integer Inode = new Integer(inode);
			fileProperty.add(filename);
			fileProperty.add(cDate);
			fileProperty.add(mDate);
			fileProperty.add(Inode);
			return fileProperty.toArray();
		}
		return null;
	}
	
	public byte[] findMetadataEntryByFilename(String fileName) {
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

	private int findMetadataEntryIndexByFilename(String filename) {
		if (isRightFilename(filename)) {
			if (isExists(filename)) {
				int maxFileNameLength = 64;
				int offset = headerLength + fatLength * 2;
				byte[] metadataBytes = new byte[preEntryLengthInMetaData];
				String charset = "ISO-8859-1";
				for (int index = 0; index < nOfDataEntrys; index++) {
					if (llio.read(offset, metadataBytes, metadataBytes.length) == 0) {
						if (!isEntryFree(metadataBytes)) {
							if (filename.equals(byteToString(metadataBytes, 0,
									maxFileNameLength, charset))) {
								return index;
							}
						}
					}
					offset += preEntryLengthInMetaData;
				}
				return PARAM_INVALID;
			} else {
				return FILE_NOT_EXIST;
			}
		} else {
			return PARAM_INVALID;
		}
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

	private int getFatLength() {
		return getFatOneLength();
	}

	private void initFileSystem() {
		if (isFormat()) {
			fatLength = getFatLength();
			metaDataLength = getMetaDataLength();
			dataBlockLength = getDataBlockLength();
			nOfDataEntrys = fatLength / preEntryLengthInFat;
			preEntryLengthInDatablock = (int) dataBlockLength / nOfDataEntrys;
			wasteBlockLength = (int) (llio.getSize() - headerLength - fatLength
					* 2 - metaDataLength - dataBlockLength);
			fatBitMap = new byte[nOfDataEntrys];
			for (int fatIndex = 0; fatIndex < nOfDataEntrys; fatIndex++) {
				if (isFatEntryFree(fatIndex) == 0) {
					fatBitMap[fatIndex] = 0;
				} else {
					fatBitMap[fatIndex] = 1;
				}
			}
		}
	}

	private int isFatEntryFree(int fatIndex) {
		int fatOffset = headerLength + fatIndex * preEntryLengthInFat;
		byte free = (byte) 0xfe;
		byte[] fatEntry = new byte[preEntryLengthInFat];
		if (llio.read(fatOffset, fatEntry, fatEntry.length) == 0) {
			for (int i = 0; i < fatEntry.length; i++) {
				if (fatEntry[i] != free) {
					return 1;
				}
			}
			return 0;
		}
		return IO_WRONG;
	}

	public void open() {
		llio.initialize();
		initFileSystem();
	}

	public void close() {
		llio.close();
		fatBitMap = null;
	}

}
