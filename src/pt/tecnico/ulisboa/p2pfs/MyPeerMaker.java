package pt.tecnico.ulisboa.p2pfs;

import java.io.IOException;
import java.security.KeyPair;

import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

public class MyPeerMaker extends PeerMaker{

	public MyPeerMaker(Number160 peerId) {
		super(peerId);
		super.setStorage(new MyStorageMemory());
		// TODO Auto-generated constructor stub
	}

	public MyPeerMaker(KeyPair keyPair) {
		super(keyPair);
		super.setStorage(new MyStorageMemory());
		// TODO Auto-generated constructor stub
	}
	
	
}
