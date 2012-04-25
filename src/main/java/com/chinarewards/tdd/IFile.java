package com.chinarewards.tdd;

public interface IFile {

	/**
	 * 描述：用于打开一个文件 。
	 * 
	 * @param fileName
	 *            文件名
	 * @return 若该文件名存在则返回IFile；否则返回空
	 */
	IFile open(String fileName);

	/**
	 * 描述：创建一个新文件
	 * 当且仅当不存在具有此抽象路径名指定的名称的文件时，原子地创建由此抽象路径名指定的一个新的空文件。检查文件是否存在，如果不存在则创建该文件，
	 * 这是单个操作，对于其他所有可能影响该文件的文件系统活动来说，该操作是原子的。
	 * 
	 * @return 如果指定的文件不存在并成功地创建，则返回 true；如果指定的文件已经存在，则返回 false
	 */
	boolean createNewFile();

	/**
	 * 描述：用于写入内容到打开的文件。
	 * 
	 * @param b
	 *            写入的文件内容
	 * @param off
	 *            写入的字节数组的开始位置
	 * @param len
	 *            写入的字节数组元素个数
	 * @return 0 则写入成功；-1 非法参数；如果为-2，I/O错误
	 */
	int write(byte[] b, int off, int len);

	/**
	 * 描述：读取文件
	 * 
	 * @param length
	 *            读取的内容长度
	 * @return 读取成功返回byte[]，byte array的长度是实际读取的长度。否则返回null
	 */
	byte[] read(int length);

	/**
	 * 描述：移动文件指针
	 * 
	 * @param pos
	 *            文件指针的位置
	 * @return 文件指针的实际位置；-1，非法参数
	 */
	long seek(long pos);

	/**
	 * 关闭文件
	 */
	void close();

	/**
	 * 删除此抽象路径名表示的文件
	 * 
	 * @return 当且仅当成功删除文件时，返回 true；否则返回 false
	 */
	boolean delete();

	/**
	 * 测试此抽象路径名表示的文件是否存在。
	 * 
	 * @return 当且仅当此抽象路径名表示的文件存在时，返回 true；否则返回 false
	 */
	boolean exists();

	/**
	 * 返回由此抽象路径名表示的文件的名称
	 * 
	 * @return 此抽象路径名表示的文件的名称，如果路径名的名称序列为空，则返回空字符串
	 */
	String getName();

	/**
	 * 返回此抽象路径名表示的文件最后一次被修改的时间
	 * 
	 * @return 表示文件最后一次被修改的时间的 long 值，用该时间与历元（1970 年 1 月 1 日，00:00:00
	 *         GMT）的时间差来计算此值（以毫秒为单位）。如果该文件不存在，或是发生 I/O 错误，则返回 0L
	 */
	long lastModified();

	/**
	 * 
	 * @return 所有文件名
	 */
	String[] list();

	/**
	 * 
	 * @return 文件长度
	 */
	long length();
}