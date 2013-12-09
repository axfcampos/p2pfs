package pt.ulisboa.tecnico.p2pfs.fuse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.fusejna.DirectoryFiller;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;

public final class MemoryDirectory extends MemoryPath implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public List<MemoryPath> contents = new ArrayList<MemoryPath>();

	public MemoryDirectory(){
		super();
	}
	
	public MemoryDirectory(final String name) {
		super(name);
	}

	public MemoryDirectory(final String name, final MemoryDirectory parent) {
		super(name, parent);
	}

	public void add(final MemoryPath p) {
		contents.add(p);
		p.parent = this;
	}

	@Override
	public MemoryPath find(String path) {
		if (super.find(path) != null) {
			return super.find(path);
		}
		while (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (!path.contains("/")) {
			for (final MemoryPath p : contents) {
				if (p.name.equals(path)) {
					return p;
				}
			}
			return null;
		}
		final String nextName = path.substring(0, path.indexOf("/"));
		final String rest = path.substring(path.indexOf("/"));
		for (final MemoryPath p : contents) {
			if (p.name.equals(nextName)) {
				return p.find(rest);
			}
		}
		return null;
	}

	@Override
	public void getattr(final StatWrapper stat) {
		stat.setMode(NodeType.DIRECTORY);
	}

	public void mkdir(final String lastComponent) {
		contents.add(new MemoryDirectory(lastComponent, this));
	}

	public void mkfile(final String lastComponent) {
		contents.add(new MemoryFile(lastComponent, this));
	}

	public void read(final DirectoryFiller filler) {
		for (final MemoryPath p : contents) {
			filler.add(p.name);
		}
	}
	
	public int size() {
		return contents.size();
	}
}
