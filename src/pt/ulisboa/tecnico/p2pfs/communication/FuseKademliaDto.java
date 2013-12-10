package pt.ulisboa.tecnico.p2pfs.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FuseKademliaDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private boolean root;
	
	private final List<FuseKademliaEntryDto> contents = new ArrayList<FuseKademliaEntryDto>();
	
	public FuseKademliaDto() {}
	
	public FuseKademliaDto(String path) {
		
		if(path.equals("/"))
			root = true;
		
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	public List<FuseKademliaEntryDto> getContents() {
		return contents;
	}
	
	public void addContent(FuseKademliaEntryDto dto) {
		contents.add(dto);
	}
	
	public void removeContent(String name) {
		
		for(FuseKademliaEntryDto e : contents) {
			System.out.println("CONT RM: " + e.getName() + ":" + name);
			if(e.getName().equals(name)) {
				contents.remove(e);
				break;
			}
		}
	}

}
