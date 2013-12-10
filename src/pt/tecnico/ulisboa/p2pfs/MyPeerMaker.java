package pt.tecnico.ulisboa.p2pfs;

import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;

public class MyPeerMaker extends PeerMaker{

	public MyPeerMaker(Number160 peerId) {
		super(peerId);
		super.setStorage(new MyStorageMemory(peerId));
		// TODO Auto-generated constructor stub
	}

	
	
	
}
