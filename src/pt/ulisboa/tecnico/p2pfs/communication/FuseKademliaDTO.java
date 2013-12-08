package pt.ulisboa.tecnico.p2pfs.communication;

import net.tomp2p.storage.Data;

public class FuseKademliaDTO {
	
	private Data data;

	public FuseKademliaDTO(Data data) {
		
		this.data = data;
		
	}
	
	
	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
	

}
