package pt.ulisboa.tecnico.p2pfs.fuse;

import java.nio.ByteBuffer;

import pt.ulisboa.tecnico.p2pfs.kademlia.Kademlia;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.util.FuseFilesystemAdapterAssumeImplemented;

public class P2PFilesystem extends FuseFilesystemAdapterAssumeImplemented {

	private Kademlia kademlia;
	
	/* Based on examples */

	private final MemoryDirectory rootDirectory;

	public P2PFilesystem(String username) {
		// Sprinkle some files around
		
		
		this.kademlia = new Kademlia(username);
		
		rootDirectory = (MemoryDirectory) kademlia.getMyFileData();
		
		rootDirectory.add(new MemoryFile("Sample file.txt", "Hello there, feel free to look around.\n"));
		rootDirectory.add(new MemoryDirectory("Sample directory"));
		final MemoryDirectory dirWithFiles = new MemoryDirectory("Directory with files");
		rootDirectory.add(dirWithFiles);
		dirWithFiles.add(new MemoryFile("hello.txt", "This is some sample text.\n"));
		dirWithFiles.add(new MemoryFile("hello again.txt", "This another file with text in it! Oh my!\n"));
		final MemoryDirectory nestedDirectory = new MemoryDirectory("Sample nested directory");
		dirWithFiles.add(nestedDirectory);
		nestedDirectory.add(new MemoryFile("So deep.txt", "Man, I'm like, so deep in this here file structure.\n"));
		
	}

	@Override
	public int access(final String path, final int access)
	{
		
		return 0;
	}

	@Override
	public int create(final String path, final ModeWrapper mode, final FileInfoWrapper info)
	{
		if (getPath(path) != null) {
			return -ErrorCodes.EEXIST();
		}
		final MemoryPath parent = getParentPath(path);
		if (parent instanceof MemoryDirectory) {
			((MemoryDirectory) parent).mkfile(getLastComponent(path));
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int getattr(final String path, final StatWrapper stat)
	{
		final MemoryPath p = getPath(path);
		if (p != null) {
			p.getattr(stat);
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	private String getLastComponent(String path)
	{
		while (path.substring(path.length() - 1).equals("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if (path.isEmpty()) {
			return "";
		}
		return path.substring(path.lastIndexOf("/") + 1);
	}

	private MemoryPath getParentPath(final String path)
	{
		return rootDirectory.find(path.substring(0, path.lastIndexOf("/")));
	}

	private MemoryPath getPath(final String path)
	{
		return rootDirectory.find(path);
	}

	@Override
	public int mkdir(final String path, final ModeWrapper mode)
	{
		if (getPath(path) != null) {
			return -ErrorCodes.EEXIST();
		}
		final MemoryPath parent = getParentPath(path);
		if (parent instanceof MemoryDirectory) {
			((MemoryDirectory) parent).mkdir(getLastComponent(path));
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int open(final String path, final FileInfoWrapper info)
	{
		return 0;
	}

	@Override
	public int read(final String path, final ByteBuffer buffer, final long size, final long offset, final FileInfoWrapper info)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(p instanceof MemoryFile)) {
			return -ErrorCodes.EISDIR();
		}
		return ((MemoryFile) p).read(buffer, size, offset);
	}

	@Override
	public int readdir(final String path, final DirectoryFiller filler)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(p instanceof MemoryDirectory)) {
			return -ErrorCodes.ENOTDIR();
		}
		((MemoryDirectory) p).read(filler);
		return 0;
	}

	@Override
	public int rename(final String path, final String newName)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		final MemoryPath newParent = getParentPath(newName);
		if (newParent == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(newParent instanceof MemoryDirectory)) {
			return -ErrorCodes.ENOTDIR();
		}
		p.delete();
		p.rename(newName.substring(newName.lastIndexOf("/")));
		((MemoryDirectory) newParent).add(p);
		return 0;
	}

	@Override
	public int rmdir(final String path)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(p instanceof MemoryDirectory)) {
			return -ErrorCodes.ENOTDIR();
		}
		p.delete();
		return 0;
	}

	@Override
	public int truncate(final String path, final long offset)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(p instanceof MemoryFile)) {
			return -ErrorCodes.EISDIR();
		}
		((MemoryFile) p).truncate(offset);
		return 0;
	}

	@Override
	public int unlink(final String path)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		p.delete();
		return 0;
	}

	@Override
	public int write(final String path, final ByteBuffer buf, final long bufSize, final long writeOffset,
			final FileInfoWrapper wrapper)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(p instanceof MemoryFile)) {
			return -ErrorCodes.EISDIR();
		}
		return ((MemoryFile) p).write(buf, bufSize, writeOffset);
	}
	
	public static void main(final String... args) throws FuseException {
		if (args.length != 2) {
			System.err.println("Usage: P2PFilesystem <username> <mountpoint>");
			System.exit(1);
		}
		System.out.println(args[0]);
		new P2PFilesystem(args[0]).log(true).mount(args[1]);
	}

}
