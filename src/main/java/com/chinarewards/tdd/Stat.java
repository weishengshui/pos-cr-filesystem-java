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
		st_ctime i-node最近一次被更改的时间，此参数会在文件所有者、组、权限被更改时更新先前所描述的st_mode。
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
	
	/**
	 * st_mode: The following flags are defined for the st_mode field
	 */
	public static final long S_IFMT  = 0170000;		/* bit mask for the file type bit fields */
	public static final long S_IFSOCK = 0140000;	/* socket */
	public static final long S_IFLNK = 	0120000;	/* symbolic link */
	public static final long S_IFREG = 	0100000;	/* regular file */
	public static final long S_IFBLK = 	0060000;	/* block device */
	public static final long S_IFDIR = 	0040000;	/* directory */
	public static final long S_IFCHR = 	0020000;	/* character device */
	public static final long S_IFIFO = 	0010000;	/* FIFO */
	public static final long S_ISUID = 	0004000;	/* set UID bit */
	public static final long S_ISGID = 	0002000;	/* set-group-ID bit (see below) */
	public static final long S_ISVTX = 	0001000;	/* sticky bit (see below) */
	public static final long S_IRWXU = 	00700;	/* mask for file owner permissions */
	public static final long S_IRUSR = 	00400;	/* owner has read permission */
	public static final long S_IWUSR = 	00200;	/* owner has write permission */
	public static final long S_IXUSR = 	00100;	/* owner has execute permission */
	public static final long S_IRWXG = 	00070;	/* mask for group permissions */
	public static final long S_IRGRP = 	00040;	/* group has read permission */
	public static final long S_IWGRP = 	00020;	/* group has write permission */
	public static final long S_IXGRP = 	00010;	/* group has execute permission */
	public static final long S_IRWXO = 	00007;	/* mask for permissions for others (not in group) */
	public static final long S_IROTH = 	00004;	/* others have read permission */
	public static final long S_IWOTH = 	00002;	/* others have write permission */
	public static final long S_IXOTH = 	00001;	/* others have execute permission */
}
