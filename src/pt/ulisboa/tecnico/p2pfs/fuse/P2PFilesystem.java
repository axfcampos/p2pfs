package pt.ulisboa.tecnico.p2pfs.fuse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.util.FuseFilesystemAdapterAssumeImplemented;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaDto;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaEntryDto;
import pt.ulisboa.tecnico.p2pfs.kademlia.Kademlia;

public class P2PFilesystem extends FuseFilesystemAdapterAssumeImplemented {

	private Kademlia kademlia;

	private MemoryDirectory rootDirectory = new MemoryDirectory();

	public P2PFilesystem(String username) {
		// Sprinkle some files around
		
		
		this.kademlia = new Kademlia(username);
		
		rootDirectory.contents = memoryPathFromFuseKadmliaDto((FuseKademliaDto) kademlia.getMyFileData());
		
	}
	
	private List<MemoryPath> memoryPathFromFuseKadmliaDto(FuseKademliaDto dto) {
		
		List<MemoryPath> dir = new ArrayList<MemoryPath>();
		
		for (FuseKademliaEntryDto entry : dto.getContents())
			if (entry.getType() == FuseKademliaEntryDto.DIR)
				dir.add(new MemoryDirectory(entry.getName()));
			else
				dir.add(new MemoryFile(entry.getName()));
		
		return dir;
		
	}

	@Override
	public int access(final String path, final int access)
	{
		
		MemoryPath p = rootDirectory.find(path);
		
		
		if(p instanceof MemoryDirectory) {
		
			MemoryDirectory dir = (MemoryDirectory) p; 
			
			if (dir.size() != 0)
				return 0;
			
			try {
				
				System.out.println(dir.contents.size());
				
				dir.contents = memoryPathFromFuseKadmliaDto(kademlia.getDirectoryObject(path));
				
				return 0;
			
			} catch (ClassNotFoundException e) {
				
				return -ErrorCodes.EEXIST();
				
			} catch (IOException e) {
				
				return -ErrorCodes.EEXIST();
			}
		
		}
		
		return 1;
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
			
			if(path.contains("/."))
				return 0;
			
			try {
				
				String[] splitter = path.split("/");
				
				String name = splitter[splitter.length - 1];
				
				kademlia.createFile(path);
				if(path.equals("/"))
					kademlia.updateDirectory(path.substring(0, path.lastIndexOf('/') + 1)
							, new FuseKademliaEntryDto(name, 'f'));
				else 
					kademlia.updateDirectory(path.substring(0, path.lastIndexOf('/'))
								, new FuseKademliaEntryDto(name, 'f'));
				
			} catch (IOException e) {
				
				System.out.println("Problems creating file...");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				
				System.out.println("Problems creating file...");
				e.printStackTrace();
			}
			
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int getattr(final String path, final StatWrapper stat)
	{
		if(rootDirectory.find(path) instanceof MemoryFile && !path.contains("/.")) {
		
			System.out.println("I'm in open: " + path);
			
			MemoryFile file = (MemoryFile) rootDirectory.find(path);
			
			if(!file.hasContent())
				try {
						String str = (String) kademlia.getFileObject(path);
						
						final byte[] contentBytes = str.getBytes("UTF-8");
						file.setContents(ByteBuffer.wrap(contentBytes));
						
						System.out.println(((MemoryFile)rootDirectory.find(path)).getContents().capacity());
						
				} catch (ClassNotFoundException e) {
					
					System.out.println("Problems openning file...");
				} catch (IOException e) {
					
					System.out.println("Problems openning file...");
				}
			
	
		}
		
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
			

			String[] splitter = path.split("/");
			
			String name = splitter[splitter.length - 1];
			
			try {
			
				kademlia.updateDirectory(path.substring(0, path.lastIndexOf('/') + 1)
								, new FuseKademliaEntryDto(name, 'd'));
				kademlia.createDirFile(path);
			
			} catch (ClassNotFoundException e) {
				
				System.out.println("Problem creating directory...");
				e.printStackTrace();
			} catch (IOException e) {
				
				
				e.printStackTrace();
			}
			
			
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
	
	@Override
	public int release(final String path, final FileInfoWrapper info)
	{
		
		String str = new String( ((MemoryFile)rootDirectory.find(path)).
								getContents().array(), Charset.forName("UTF-8") );
		
		
		try {
			
			kademlia.updateFile(path, str);
		} catch (IOException e) {
			System.out.println("Problems Updating File...");
			e.printStackTrace();
		}
		
		return 0;
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
