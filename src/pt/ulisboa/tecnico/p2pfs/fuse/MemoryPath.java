package pt.ulisboa.tecnico.p2pfs.fuse;

import net.fusejna.StructStat.StatWrapper;


public abstract class MemoryPath {
	protected String name;
	protected MemoryDirectory parent;

	public MemoryPath() {}
	
	public MemoryPath(final String name) {
		this(name, null);
	}

	public MemoryPath(final String name, final MemoryDirectory parent) {
		this.name = name;
		this.parent = parent;
	}

	public void delete() {
		
		if (parent != null) {
			parent.contents.remove(this);
			parent = null;
		}
	}

	public MemoryPath find(String path) {
		while (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (path.equals(name) || path.isEmpty()) {
			return this;
		}
		return null;
	}

	public abstract void getattr(StatWrapper stat);

	public void rename(String newName) {
		while (newName.startsWith("/")) {
			newName = newName.substring(1);
		}
		name = newName;
	}
}