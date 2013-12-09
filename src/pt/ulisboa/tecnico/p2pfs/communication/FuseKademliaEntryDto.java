package pt.ulisboa.tecnico.p2pfs.communication;

import java.io.Serializable;

public class FuseKademliaEntryDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static char DIR = 'd';
	public static char FILE = 'f';

	char type;
	
	String name;
	
	public FuseKademliaEntryDto() {}
	
	public FuseKademliaEntryDto(String name, char c) {
		
		this.name = name;
		this.type = c;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
