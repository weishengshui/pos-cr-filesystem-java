package com.chinarewards.tdd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CRFileSystem implements IFilesystem {

	/**
	 * constants
	 */
	public static final int FAILURE = -8;
	public static final int SUCCESS = 0;
	public static final int PARAM_INVALID = -1;
	public static final int FILE_EXIST = -2;
	public static final int NOT_FORMAT = -3;
	public static final int DISK_FULL = -4;
	public static final int IO_WRONG = -5;
	public static final int FILE_NOT_EXIST = -6;
	public static final int OPERATING_ERROR = -7;
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
	private static final int preEntryLengthInMetaData = 90;
	private int metaDataLength;
	private int fatLength;
	private long dataBlockLength;
	private int wasteBlockLength;
	private int nOfDataEntrys;
	private int preEntryLengthInDatablock;
	private byte[] fatBitMap;

	/**
	 * File stat
	 */
	private Stat stat = null;

	public CRFileSystem(FileBasedLowLevelIO llio) {
		this.llio = llio;
		initFileSystem();
	}

	public int getPreEntryLengthInDatablock() {
		return preEntryLengthInDatablock;
	}

	public CRFileSystem() {
		llio = new FileBasedLowLevelIO("/home/wss/disk/system.vdk");
		initFileSystem();
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
		byte[] header_dataBlockLength = LongTo_8_Bytes(dataBlockLength);
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
		int metadataIndex = getMetaDataIndex();
		int offset = headerLength + fatLength * 2;
		offset += metadataIndex * preEntryLengthInMetaData;
		byte[] buffer = new byte[preEntryLengthInMetaData];
		int position = 0;
		for (int i = 0; i < fileNameByte.length; i++) {
			buffer[position] = fileNameByte[i];
			position++;
		}
		position = 64;
		Date now = new Date();
		long date = now.getTime();
		byte[] dateBytes = LongTo_8_Bytes(date);
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
		position = 82;
		byte[] fileLength = LongTo_8_Bytes(0);
		for (int i = 0; i < fileLength.length; i++) {
			buffer[position++] = fileLength[i];
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
	private int getMetaDataIndex() {
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
			if (fatBitMap[index] == 0) {
				fatCount++;
			} else {
				System.out.println("fatBitMap[index] == 1:" + index);
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

	public int deleteFileByFileName(String fileName) {
		if (!isFormat()) {
			return NOT_FORMAT;
		}
		if (isRightFilename(fileName)) {
			if (isExists(fileName)) {
				setFileProperty(fileName);
				int metadataEntryIndex = findMetadataEntryIndexByFilename(fileName);
				byte[] metadataEntry = new byte[preEntryLengthInMetaData];
				if (emptyFileContent() != 0) {
					return FAILURE;
				}
				System.out
						.println("deleteFile(String fileName) metadataEntryIndex:"
								+ metadataEntryIndex);
				byte[] free = new byte[preEntryLengthInFat];
				for (int i = 0; i < free.length; i++) {
					free[i] = (byte) 0xfe;
				}
				for (int i = 0; i < metadataEntry.length; i++) {
					metadataEntry[i] = 0x00;
				}
				System.out.println("deleteFile(String fileName) stat.st_ino:"
						+ stat.st_ino);
				if ((writeEntryInFat((int) stat.st_ino, free) != 0)
						|| (llio.write(
								headerLength + fatLength * 2
										+ metadataEntryIndex
										* preEntryLengthInMetaData,
								metadataEntry, metadataEntry.length) != 0)) {
					return IO_WRONG;
				}
				System.out
						.println("deleteFile(String fileName) getAvailableSpace():"
								+ getAvailableSpace());
				return SUCCESS;
			} else {
				return FILE_NOT_EXIST;
			}
		} else {
			return PARAM_INVALID;
		}
	}

	public int deleteFileByFatStat(Stat stat) {
		if (!isFormat()) {
			return NOT_FORMAT;
		}
		if (null == stat) {
			return PARAM_INVALID;
		}
		setStat(stat);
		int metadataEntryIndex = findMetadataEntryIndexByFatIndex((int) stat.st_ino);
		byte[] metadataEntry = new byte[preEntryLengthInMetaData];
		if (emptyFileContent() != 0) {
			return FAILURE;
		}
		System.out
				.println("deleteFileByFatIndex(Stat stat) metadataEntryIndex:"
						+ metadataEntryIndex);
		byte[] free = new byte[preEntryLengthInFat];
		for (int i = 0; i < free.length; i++) {
			free[i] = (byte) 0xfe;
		}
		for (int i = 0; i < metadataEntry.length; i++) {
			metadataEntry[i] = 0x00;
		}
		System.out.println("deleteFileByFatIndex(Stat stat) stat.st_ino:"
				+ stat.st_ino);
		if ((writeEntryInFat((int) stat.st_ino, free) != 0)
				|| (llio.write(headerLength + fatLength * 2
						+ metadataEntryIndex * preEntryLengthInMetaData,
						metadataEntry, metadataEntry.length) != 0)) {
			return IO_WRONG;
		}
		System.out
				.println("deleteFileByFatIndex(Stat stat) getAvailableSpace():"
						+ getAvailableSpace());
		return SUCCESS;
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
	 * @param fileOffset
	 */
	public int write(long fileOffset, byte[] buffer, int length) {

		if (fileOffset < 0 || null == buffer || length < 1
				|| length > buffer.length || fileOffset > dataBlockLength) {
			return PARAM_INVALID;

		}
		if (!isFormat()) {
			return NOT_FORMAT;
		}
		if (null == stat) {
			return OPERATING_ERROR;
		}
		if (fatBitMap[(int) stat.st_ino] == 0) {
			return PARAM_INVALID;
		}
		if (fileOffset > stat.st_size) {
			return PARAM_INVALID;
		}
		int needSize = (int) ((fileOffset + length) - stat.st_size);
		System.out.println("stat.st_size:" + stat.st_size);
		if (stat.st_size == 0) {
			needSize -= preEntryLengthInDatablock;
		} else {
			needSize -= (preEntryLengthInDatablock - stat.st_size
					% preEntryLengthInDatablock);
		}
		System.out.println("getAvailableSpace():" + getAvailableSpace());
		System.out.println("needSize:" + needSize);
		if (needSize > getAvailableSpace()) {
			// return PARAM_INVALID;
			return -100;
		}

		long datablockOffset0 = getDatablockOffsetByFileOffset(fileOffset);

		/****
		 * *********************************************************************
		 * *****************
		 */
		System.out.println("stat.st_ino:" + stat.st_ino);
		System.out.println("stat.st_size:" + stat.st_size);
		System.out.println("datablockOffset0:" + datablockOffset0);

		int currentFatIndex = (int) (datablockOffset0 / preEntryLengthInDatablock);

		/**
		 * 
		 */
		System.out.println("currentFatIndex:" + currentFatIndex);

		int newFatIndex;
		int alreadyWriteLength = 0;
		byte[] onceWriteBuffer;
		byte[] fatEntryBuf = new byte[preEntryLengthInFat];
		byte[] EOF = new byte[preEntryLengthInFat];
		EOF[0] = (byte) 0xff;
		EOF[1] = (byte) 0xff;

		if (llio.read(headerLength + currentFatIndex * preEntryLengthInFat,
				fatEntryBuf, fatEntryBuf.length) == 0) {

			while (true) {

				if (Arrays.equals(EOF, fatEntryBuf)) {
					if (stat.st_size % preEntryLengthInDatablock == 0
							&& fileOffset == stat.st_size && stat.st_size != 0) {
						newFatIndex = getFatNumber();
						fatEntryBuf = convertIntTo_2_Bytes(newFatIndex);
						for (int i = 0; i < fatEntryBuf.length; i++) {
							System.out.println("3 fatEntryBuf[" + i + "]:"
									+ fatEntryBuf[i]);
						}
						System.out.println("3 currentFatIndex:"
								+ currentFatIndex);
						if (writeEntryInFat(currentFatIndex, fatEntryBuf) != 0
								|| writeEntryInFat(newFatIndex, EOF) != 0) {
							return IO_WRONG;
						}
						int onceWriteLength;
						if ((length - alreadyWriteLength) >= preEntryLengthInDatablock) {
							onceWriteLength = preEntryLengthInDatablock;
						} else {
							onceWriteLength = length - alreadyWriteLength;
						}

						onceWriteBuffer = Arrays.copyOfRange(buffer,
								alreadyWriteLength, alreadyWriteLength
										+ onceWriteLength);

						if (llio.write(headerLength + fatLength * 2
								+ metaDataLength + newFatIndex
								* preEntryLengthInDatablock, onceWriteBuffer,
								onceWriteBuffer.length) != 0) {
							return IO_WRONG;
						}

						fileOffset += onceWriteLength;
						alreadyWriteLength += onceWriteLength;
						currentFatIndex = newFatIndex;

						if (fileOffset > stat.st_size) {
							stat.st_size = fileOffset;
						}

						if (alreadyWriteLength == length) {
							int metadataIndex = findMetadataEntryIndexByFatIndex((int) stat.st_ino);
							byte[] fileLength = LongTo_8_Bytes(stat.st_size);
							if (llio.write(headerLength + fatLength * 2
									+ metadataIndex * preEntryLengthInMetaData
									+ 82, fileLength, fileLength.length) == 0) {
								System.out
										.println("final file length stat.st_size:"
												+ stat.st_size);
								return length;
							}
							return IO_WRONG;
						}
						if (llio.read(headerLength + currentFatIndex
								* preEntryLengthInFat, fatEntryBuf,
								fatEntryBuf.length) != 0) {
							return IO_WRONG;
						}
					} else {
						int restLength;
						int onceWriteLength;
						restLength = (int) (preEntryLengthInDatablock - fileOffset
								% preEntryLengthInDatablock);

						if (restLength >= (length - alreadyWriteLength)) {
							onceWriteLength = length - alreadyWriteLength;
						} else {
							onceWriteLength = restLength;
						}

						/**
						 * *****************************************************
						 * ***********************************************
						 */
						System.out.println("1 alreadyWriteLength:"
								+ alreadyWriteLength);
						System.out
								.println("1 alreadyWriteLength+onceWriteLength:"
										+ (alreadyWriteLength + onceWriteLength));

						onceWriteBuffer = Arrays.copyOfRange(buffer,
								alreadyWriteLength, alreadyWriteLength
										+ onceWriteLength);
						if (llio.write(headerLength + fatLength * 2
								+ metaDataLength + currentFatIndex
								* preEntryLengthInDatablock + fileOffset
								% preEntryLengthInDatablock, onceWriteBuffer,
								onceWriteBuffer.length) != 0) {
							return IO_WRONG;
						}
						fileOffset += onceWriteLength;
						alreadyWriteLength += onceWriteLength;
						if (fileOffset > stat.st_size) {
							stat.st_size = fileOffset;
						}
						if (alreadyWriteLength == length) {
							int metadataIndex = findMetadataEntryIndexByFatIndex((int) stat.st_ino);
							byte[] fileLength = LongTo_8_Bytes(stat.st_size);
							if (llio.write(headerLength + fatLength * 2
									+ metadataIndex * preEntryLengthInMetaData
									+ 82, fileLength, fileLength.length) == 0) {
								return length;
							}
							return IO_WRONG;
						}
					}
				} else {
					int restLength;
					int onceWriteLength;

					restLength = (int) (preEntryLengthInDatablock - fileOffset
							% preEntryLengthInDatablock);
					if (restLength >= (length - alreadyWriteLength)) {
						onceWriteLength = length - alreadyWriteLength;
					} else {
						onceWriteLength = restLength;
					}

					System.out.println("onceWriteLength:" + onceWriteLength);

					onceWriteBuffer = Arrays.copyOfRange(buffer,
							alreadyWriteLength, alreadyWriteLength
									+ onceWriteLength);
					if (llio.write(headerLength + fatLength * 2
							+ metaDataLength + currentFatIndex
							* preEntryLengthInDatablock + fileOffset
							% preEntryLengthInDatablock, onceWriteBuffer,
							onceWriteBuffer.length) != 0) {
						return IO_WRONG;
					}
					fileOffset += onceWriteLength;
					alreadyWriteLength += onceWriteLength;

					if (fileOffset >= stat.st_size) {
						stat.st_size = fileOffset;
					}

					if (alreadyWriteLength == length) {
						int metadataIndex = findMetadataEntryIndexByFatIndex((int) stat.st_ino);
						byte[] fileLength = LongTo_8_Bytes(stat.st_size);
						if (llio.write(
								headerLength + fatLength * 2 + metadataIndex
										* preEntryLengthInMetaData + 82,
								fileLength, fileLength.length) == 0) {
							return length;
						}
						return IO_WRONG;
					} else {
						currentFatIndex = 0;
						for (int i = 0; i < fatEntryBuf.length; i++) {
							currentFatIndex <<= 8;
							currentFatIndex ^= (int) fatEntryBuf[i] & 0xff;
						}
						if (llio.read(headerLength + currentFatIndex
								* preEntryLengthInFat, fatEntryBuf,
								fatEntryBuf.length) != 0) {
							return IO_WRONG;
						}
					}

				}
			}
		} else {
			return IO_WRONG;
		}

	}

	/**
	 * 
	 * @param fileOffset
	 *            : data block offset begin 0
	 * 
	 * @return -1 param invalid
	 * @return -2 Read more than the length of the disk space
	 * @return -3 the offset is not the begin of a file
	 * @return -4 Read more than the length of the file
	 * @return -5 I/O wrong
	 * @return length number of read bytes
	 */
	public int read(long fileOffset, byte[] buffer, int length) {
		if (fileOffset < 0 || null == buffer || length < 1
				|| length > buffer.length || fileOffset > dataBlockLength
				|| (length + fileOffset) > dataBlockLength) {
			return PARAM_INVALID;

		}
		if (null == stat) {
			return OPERATING_ERROR;
		}
		if (fileOffset > stat.st_size) {
			return PARAM_INVALID;
		}

		// 读取了0个byte
		if (fileOffset == stat.st_size) {
			return 0;
		}
		// 没有内容可读取
		if (stat.st_size == 0) {
			return 0;
		}
		if ((length + fileOffset) > stat.st_size) {
			length = (int) (stat.st_size - fileOffset);
		}

		long datablockOffset0 = getDatablockOffsetByFileOffset(fileOffset);

		System.out.println("read stat.st_ino:" + stat.st_ino);
		System.out.println("read stat.st_size:" + stat.st_size);
		System.out.println("read datablockOffset0:" + datablockOffset0);

		int currentFatIndex = (int) (datablockOffset0 / preEntryLengthInDatablock);

		/**
		 * 
		 */
		System.out.println("read currentFatIndex:" + currentFatIndex);

		int nextFatIndex;
		int alreadyReadLength = 0;
		byte[] onceReadBuffer;
		byte[] fatEntryBuf = new byte[preEntryLengthInFat];
		byte[] EOF = new byte[2];
		EOF[0] = (byte) 0xff;
		EOF[1] = (byte) 0xff;

		if (llio.read(headerLength + currentFatIndex * preEntryLengthInFat,
				fatEntryBuf, fatEntryBuf.length) == 0) {
			while (true) {
				if (Arrays.equals(EOF, fatEntryBuf)) {
					int onceReadLength = 0;
					if ((fileOffset + (length - alreadyReadLength)) < stat.st_size) {
						onceReadLength = length - alreadyReadLength;
					} else {
						onceReadLength = (int) (stat.st_size - fileOffset);
					}

					onceReadBuffer = new byte[onceReadLength];

					if (llio.read(headerLength + fatLength * 2 + metaDataLength
							+ currentFatIndex * preEntryLengthInDatablock
							+ fileOffset % preEntryLengthInDatablock,
							onceReadBuffer, onceReadBuffer.length) != 0) {
						return IO_WRONG;
					}
					for (int i = alreadyReadLength, j = 0; j < onceReadBuffer.length; i++, j++) {
						buffer[i] = onceReadBuffer[j];
					}
					System.out.println("3 read currentFatIndex:"
							+ currentFatIndex);
					alreadyReadLength += onceReadLength;
					fileOffset += onceReadLength;
					return length;
				} else {
					int onceReadLength;
					int restLength = (int) (preEntryLengthInDatablock - fileOffset
							% preEntryLengthInDatablock);
					if (restLength <= (length - alreadyReadLength)) {
						onceReadLength = restLength;
					} else {
						onceReadLength = length - alreadyReadLength;
					}

					onceReadBuffer = new byte[onceReadLength];

					if (llio.read(headerLength + fatLength * 2 + metaDataLength
							+ currentFatIndex * preEntryLengthInDatablock
							+ fileOffset % preEntryLengthInDatablock,
							onceReadBuffer, onceReadBuffer.length) != 0) {
						return IO_WRONG;
					}
					System.out.println("2 read currentFatIndex:"
							+ currentFatIndex);
					for (int i = alreadyReadLength, j = 0; j < onceReadBuffer.length; i++, j++) {
						buffer[i] = onceReadBuffer[j];
					}

					alreadyReadLength += onceReadLength;
					fileOffset += onceReadLength;

					if (alreadyReadLength == length) {

						return length;
					} else {
						nextFatIndex = 0;
						for (int i = 0; i < fatEntryBuf.length; i++) {
							nextFatIndex <<= 8;
							nextFatIndex ^= (int) fatEntryBuf[i] & 0xff;
						}
						currentFatIndex = nextFatIndex;
						if (llio.read(headerLength + currentFatIndex
								* preEntryLengthInFat, fatEntryBuf,
								fatEntryBuf.length) != 0) {
							return IO_WRONG;
						}
					}
				}
			}
		}

		return -100;
	}

	/**
	 * 
	 * @param fatIndex
	 *            start from 0
	 * @param fileOffset
	 *            file offset that begin 0
	 * @return datablock offset start from 0
	 */
	private long getDatablockOffsetByFileOffset(long fileOffset) {

		int fatIndex = (int) stat.st_ino;

		if (fileOffset >= 0 && fileOffset < preEntryLengthInDatablock) {
			return fatIndex * preEntryLengthInDatablock + fileOffset
					% preEntryLengthInDatablock;
		}
		byte[] fatEntryBuffer = new byte[preEntryLengthInFat];
		if (fileOffset >= preEntryLengthInDatablock) {
			int whichFat = (int) (fileOffset / preEntryLengthInDatablock);

			int i = 0;
			int currentIndex = fatIndex;
			int nextIndex;

			for (; i < whichFat; i++) {
				if (llio.read(
						headerLength + currentIndex * preEntryLengthInFat,
						fatEntryBuffer, fatEntryBuffer.length) != 0) {
					return -1;
				}
				nextIndex = 0;
				if (Arrays.equals(new byte[] { (byte) 0xfe, (byte) 0xfe },
						fatEntryBuffer)) {
					break;
				}
				for (int j = 0; j < fatEntryBuffer.length; j++) {
					nextIndex <<= 8;
					nextIndex ^= (int) fatEntryBuffer[j] & 0xff;
				}
				currentIndex = nextIndex;
			}

			return currentIndex * preEntryLengthInDatablock;
		}
		return -1;
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

	/**
	 * 清空文件内容，但为文件保留一个簇，文件长度为0
	 * 
	 * @return 成功返回0
	 */
	public int emptyFileContent() {

		int currentIndex = (int) stat.st_ino;
		System.out.println("emptyFileContent() stat.st_ino:" + stat.st_ino);
		int nextIndex;

		byte[] free = new byte[preEntryLengthInFat];
		for (int i = 0; i < free.length; i++) {
			free[i] = (byte) 0xfe;
		}
		byte[] EOF = new byte[preEntryLengthInFat];
		for (int i = 0; i < free.length; i++) {
			EOF[i] = (byte) 0xff;
		}
		byte[] indexBuffer = new byte[preEntryLengthInFat];

		if (llio.read(headerLength + currentIndex * preEntryLengthInFat,
				indexBuffer, indexBuffer.length) == 0) {
			while (!Arrays.equals(EOF, indexBuffer)) {
				nextIndex = 0;
				for (int i = 0; i < indexBuffer.length; i++) {
					nextIndex <<= 8;
					nextIndex ^= (int) indexBuffer[i] & 0xff;
				}
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

			if (writeEntryInFat(currentIndex, free) == 0
					&& writeEntryInFat((int) stat.st_ino, EOF) == 0) {
				stat.st_size = 0;
				Date now = new Date();
				byte[] modifiedTime = LongTo_8_Bytes(now.getTime());
				int metadataIndex = findMetadataEntryIndexByFatIndex((int) stat.st_ino);
				byte[] fileLength = LongTo_8_Bytes(0);
				if (llio.write(headerLength + fatLength * 2 + metadataIndex
						* preEntryLengthInMetaData + 82, fileLength,
						fileLength.length) != 0
						|| llio.write(
								headerLength + fatLength * 2 + metadataIndex
										* preEntryLengthInMetaData + 72,
										modifiedTime, modifiedTime.length) != 0) {
					return IO_WRONG;
				}
				return SUCCESS;
			} else {
				return IO_WRONG;
			}
		}

		return FAILURE;
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
			if (Arrays.equals(free, buffer)) {
				System.out.println("fatBitMap[fatIndex]=0:" + fatIndex);
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
	private byte[] LongTo_8_Bytes(long longNumber) {
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

	private long eightByte_To_Long(byte[] bytes) {
		int length = bytes.length;
		long times = 0;
		if (length == 8) {
			for (int i = 0; i < length; i++) {
				times <<= 8;
				times ^= (long) bytes[i] & 0xff;
			}
			return times;
		}
		return -1;
	}

	public Object[] setFileProperty(String fileName) {
		if (null == stat) {
			stat = new Stat();
		}

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
			Date cDate = new Date(eightByte_To_Long(createdDate));
			Date mDate = new Date(eightByte_To_Long(modifiedDate));
			byte[] inodeBytes = new byte[2];
			inodeBytes[0] = metadata[80];
			inodeBytes[1] = metadata[81];
			byte[] fileLengthBytes = new byte[8];
			for (int i = 82; i < preEntryLengthInMetaData; i++) {
				fileLengthBytes[i - 82] = metadata[i];
			}
			int inode = 0;
			for (int i = 0; i < inodeBytes.length; i++) {
				inode <<= 8;
				inode ^= (int) inodeBytes[i] & 0xff;
			}
			stat.st_ino = inode;
			stat.st_mtime = mDate.getTime();
			stat.st_size = eightByte_To_Long(fileLengthBytes);
			stat.st_blksize = preEntryLengthInDatablock;

			if (stat.st_size == 0) {
				stat.st_blocks = 1;
			} else {
				stat.st_blocks = (stat.st_size % preEntryLengthInDatablock == 0) ? (stat.st_size / preEntryLengthInDatablock)
						: (stat.st_size / preEntryLengthInDatablock + 1);
			}
			Integer Inode = new Integer(inode);
			fileProperty.add(filename);
			fileProperty.add(cDate);
			fileProperty.add(mDate);
			fileProperty.add(Inode);
			fileProperty.add(new Long(eightByte_To_Long(fileLengthBytes)));
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

	public Stat getStat() {
		return stat;
	}

	public void setStat(Stat stat) {
		this.stat = stat;
	}

}
