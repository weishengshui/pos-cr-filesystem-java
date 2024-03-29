package com.chinarewards.tdd;

public interface IFileIO {
	
	/**
	    Constants
		Name 		Notes
		EOF 		a negative integer of type int used to indicate end-of-file conditions
		BUFSIZ 		an integer which is the size of the buffer used by the setbuf() function
		FILENAME_MAX 	the size of a char array which is large enough to store the name of any file that can be opened
		FOPEN_MAX 	the number of files that may be open simultaneously; will be at least 8
		_IOFBF 		an abbreviation for "input/output fully buffered"; it is an integer which may be passed to the setvbuf() function to request block buffered input and output for an open stream
		_IOLBF 		an abbreviation for "input/output line buffered"; it is an integer which may be passed to the setvbuf() function to request line buffered input and output for an open stream
		_IONBF 		an abbreviation for "input/output not buffered"; it is an integer which may be passed to the setvbuf() function to request unbuffered input and output for an open stream
		L_tmpnam 	the size of a char array which is large enough to store a temporary filename generated by the tmpnam() function
		NULL 		a macro expanding to the null pointer constant; that is, a constant representing a pointer value which is guaranteed not to be a valid address of an object in memory
		SEEK_CUR 	an integer which may be passed to the fseek() function to request positioning relative to the current file position
		SEEK_END 	an integer which may be passed to the fseek() function to request positioning relative to the end of the file
		SEEK_SET 	an integer which may be passed to the fseek() function to request positioning relative to the beginning of the file
		TMP_MAX 	the maximum number of unique filenames generable by the tmpnam() function; will be at least 25
	 */
	public static final long EOF = -1;
	public static final long BUFSIZ = 512;
	public static final long FILENAME_MAX = 260;
	public static final long FOPEN_MAX = 20;
	public static final long _IOFBF = 0x0000;
	public static final long _IOLBF = 0x0040;
	public static final long _IONBF = 0x0004;
	public static final long L_tmpnam = 16;
	public static final long NULL = 0;
	public static final long SEEK_CUR = 1;
	public static final long SEEK_END = 2;
	public static final long SEEK_SET = 0;
	public static final long TMP_MAX = 32767;
	
	
	/**
	 *  description：Opens a file indicated by filename and returns a file stream associated with that file. mode is used to determine the file access mode.
	 *  @param filename 	- 	file name to associate the file stream to
	 *  @param mode 		- 	null-terminated character string determining file access mode
	 *		File access mode string 		Meaning 			Explanation 					Action if file already exists 		Action if file does not exist
	 *		"r" 							read 				Open a file for reading 		read from start 					failure to open
	 *		"w" 							write 				Create a file for writing 		destroy contents 					create new
	 *		"a" 							append 				Append to a file 				write to end 						create new
	 *		"r+" 							read extended 		Open a file for read/write 		read from start 					error
	 *		"w+" 							write extended 		Create a file for read/write 	destroy contents 					create new
	 *		"a+" 							append extended 	Open a file for read/write 		write to end 						create new
	 *		File access mode flag "b" can optionally be specified to open a file in binary mode. This flag has effect only on Windows systems.
	 *		On the append file access modes, data is written to the end of the file regardless of the current position of the file position indicator.
	 * @return opened file stream on success, NULL on failure
	 * @time 2012-4-25   上午11:25:11
	 * @author Seek
	 */
	IFileIO fopen(final String filename, final String mode);
	
	/**
	 * description：Closes the given file stream. Any unwritten buffered data are
	 * flushed to the OS. Any unread buffered data are discarded. Whether or not
	 * the operation succeeds, the stream is no longer associated with a file,
	 * and the buffer allocated by setbuf or setvbuf, if any, is also
	 * disassociated and deallocated if automatic allocation was used.
	 * 
	 * @param stream  - the file stream to close
	 * @return value  0​ on success, EOF otherwise 
	 * @time 2012-4-25 上午11:31:20
	 * @author Seek
	 */
	int fclose(IFileIO stream);
	
	/**
	 * description：Returns the file position indicator for the file stream stream. 
	 * @param stream	- 	file stream to examine 
	 * @return file position indicator on success or EOF if failure occurs. 
	 * @time 2012-4-25   上午11:39:15
	 * @author Seek
	 */
	long ftell(IFileIO stream);

	/**
	 * description：Sets the file position indicator for the file stream stream
	 * 				to the value pointed to by pos. This function can be used to set the
	 * 				indicator beyond the actual end of the file, however, negative position
	 * 				values are not accepted.
	 * 
	 * @param stream 	- 	file stream to modify 
	 * @param offset 	- 	number of characters to shift the position relative to origin 
	 * @param origin 	- 	position to which offset is added. It can have one of the following values: SEEK_SET, SEEK_CUR, SEEK_END
	 * @return 0​ upon success, nonzero value otherwise. Associated EOF flag is cleared for the stream and the effect of any ungetc is undone. 
	 * @notes For text streams, the only valid values of offset are ​0​ (applicable to any origin) and a value 
	 * 		  returned by an earlier call to ftell (only applicable to SEEK_SET). 
	 * @time 2012-4-25 上午11:52:50
	 * @author Seek
	 */
	int fseek(IFileIO stream, long offset, int origin);
	
	/**
	 * description：Causes the output file stream to be synchronized with the actual contents of the file. 
	 * 				If the given stream is of the input type, then the behavior of the function is undefined. 
	 * @param stream 	- 	the file stream to synchronize 
	 * @return Returns zero on success. Otherwise EOF is returned and the error indicator of the file stream is set. 
	 * @time 2012-4-25   上午11:57:56
	 * @author Seek
	 */
	int fflush(IFileIO stream);
	
	/**
	 * description：Reads specified number of objects in the array buffer from the given input stream stream. 
	 * 				Objects are not interpreted in any way.
	 * @param buffer 	- 	pointer to the first object object in the array to be read 		[void*]
	 * @param size 		- 	size of each object in bytes 			[size_t]
	 * @param count 	- 	the number of the objects to be read 	[size_t]
	 * @param stream  - the file stream to read
	 * @return number of objects read successfully 			[size_t]
	 * @time 2012-4-25   下午12:02:09
	 * @author Seek
	 */
	long fread(byte[] buffer, long size, long count, IFileIO stream);
	
	/**
	 * description：Writes count of objects in the given array buffer to the output stream stream. 
	 * 				Objects are not interpreted in any way. 
	 * @param buffer 	- 	pointer to the first object object in the array to be written 	[void*]
	 * @param size 		- 	size of each object 					[size_t]
	 * @param count 	- 	the number of the objects to be written [size_t]
	 * @param stream  - the file stream to write
	 * @return number of objects written successfully 		[size_t]
	 * @time 2012-4-25   下午12:04:48
	 * @author Seek
	 */
	long fwrite(final byte[] buffer, long size, long count, IFileIO stream);
	
	/**
	 * description：The stat function returns information about the attributes of
	 * 				the file named by filename in the structure pointed to by buf. If
	 * 				filename is the name of a symbolic link, the attributes you get describe
	 * 				the file that the link points to. If the link points to a nonexistent
	 * 				file name, then stat fails reporting a nonexistent file. 
	 * @param filename - file name to associate the file stream to
	 * @param stat	   - file data structure
	 * @return 	 The return value is 0 if the operation is successful, or -1 on failure. 
	 * @notes 	 When the sources are compiled with _FILE_OFFSET_BITS == 64 this function is in fact stat64
	 * 				since the LFS interface transparently replaces the normal implementation.
	 * @time 2012-4-25 下午01:14:06
	 * @author Seek
	 */
	int stat(final String filename, Stat buf);
	
	
	/**
	 * description：Deletes the file identified by character string pointed to by fname. 
	 * @param pathname	- 	pointer to a null-terminated string containing the path identifying the file to delete 
	 * @return	0 upon success or non-zero value on error. 
	 * @time 2012-5-2   下午01:52:24
	 * @author Seek
	 */
	int remove(final String pathname);
	
}
