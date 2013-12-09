package pt.ulisboa.tecnico.p2pfs.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FuseKademliaDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final List<FuseKademliaEntryDto> contents = new ArrayList<FuseKademliaEntryDto>();
	
	public FuseKademliaDto() {}

	public List<FuseKademliaEntryDto> getContents() {
		return contents;
	}
	
	public void addContent(FuseKademliaEntryDto dto) {
		contents.add(dto);
	}
	

}
