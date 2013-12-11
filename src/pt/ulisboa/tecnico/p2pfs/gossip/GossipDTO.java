package pt.ulisboa.tecnico.p2pfs.gossip;

import java.io.Serializable;

public class GossipDTO implements Serializable {

	

	private static final long serialVersionUID = 1L;
	
	public GossipClass Nnode;
	public GossipClass Nusers;
	public GossipClass NactivU;
	public GossipClass Avgfiles;
	public GossipClass AvgData;
		
//	public int roundId;
	public long nodeId;
	public int iterc;
	
//	public GossipDTO(int roundId, long l) {
	public GossipDTO(long l) {
		super();
	//	this.roundId = roundId;
		this.nodeId = l;
		
		Nnode = new GossipClass(0,0);
		Nusers = new GossipClass(0,0);
		NactivU = new GossipClass(0,0);
		Avgfiles = new GossipClass(0,0);
		AvgData = new GossipClass(0,0);
	}

	
//	public int getRoundId() {
//		return roundId;
//	}
//
//	public void setRoundId(int roundId) {
//		this.roundId = roundId;
//	}

	public int getIterc() {
		return iterc;
	}


	public void setIterc(int iterc) {
		this.iterc = iterc;
	}


	public GossipClass getNnode() {
		return Nnode;
	}

	public void setNnode(GossipClass nnode) {
		Nnode = nnode;
	}

	public GossipClass getNusers() {
		return Nusers;
	}

	public void setNusers(GossipClass nusers) {
		Nusers = nusers;
	}

	public GossipClass getNactivU() {
		return NactivU;
	}

	public void setNactivU(GossipClass nactivU) {
		NactivU = nactivU;
	}

	public GossipClass getAvgfiles() {
		return Avgfiles;
	}

	public void setAvgfiles(GossipClass avgfiles) {
		Avgfiles = avgfiles;
	}

	public GossipClass getAvgData() {
		return AvgData;
	}

	public void setAvgData(GossipClass avgData) {
		AvgData = avgData;
	}


	public long getNodeId() {
		return nodeId;
	}


	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}
}