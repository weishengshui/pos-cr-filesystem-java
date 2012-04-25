package com.chinarewards.tdd;

public class Stat {
	
	/**
		st_dev 文件的设备编号
		st_ino 文件的i-node
		st_mode 文件的类型和存取的权限
		st_nlink 连到该文件的硬连接数目，刚建立的文件值为1。
		st_uid 文件所有者的用户识别码
		st_gid 文件所有者的组识别码
		st_rdev 若此文件为装置设备文件，则为其设备编号
		st_size 文件大小，以字节计算
		st_blksize 文件系统的I/O 缓冲区大小。
		st_blcoks 占用文件区块的个数，每一区块大小为512 个字节。
		st_atime 文件最近一次被存取或被执行的时间，一般只有在用mknod、utime、read、write与tructate时改变。
		st_mtime 文件最后一次被修改的时间，一般只有在用mknod、utime和write时才会改变
		st_ctime i-node最近一次被更改的时间，此参数会在文件所有者、组、权限被更改时更新先前所描述的st_mode 则定义了下列数种情况
	 */
	public long st_dev; /*device*/
	public long st_ino; /*inode*/
	public long st_mode; /*protection*/
	public long st_nlink; /*number of hard links */
	public long st_uid; /*user ID of owner*/
	public long st_gid; /*group ID of owner*/
	public long st_rdev; /*device type */
	public long st_size; /*total size, in bytes*/
	public long st_blksize; /*blocksize for filesystem I/O */
	public long st_blocks; /*number of blocks allocated*/
	public long st_atime; /* time of lastaccess*/
	public long st_mtime; /* time of last modification */
	public long st_ctime; /* time of last change */
	
}
