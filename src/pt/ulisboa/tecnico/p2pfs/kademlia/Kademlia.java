package pt.ulisboa.tecnico.p2pfs.kademlia;

import java.io.IOException;
import java.net.Inet4Address;

import pt.tecnico.ulisboa.p2pfs.communication.FuseKademliaDTO;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

public class Kademlia {
	
	static int PORT = 9000;
	
	static Number160 ID = new Number160(10);
	
	static String HOST = "172.20.10.2";
	
	String username;
	
	Peer me;
	
	Data myFileData;
	
	public Kademlia(String username) {
		
		this.username = username;
		
		try {
			
			initPeer();
		
		} catch (IOException e) {
			
			System.out.println("Failed initiating peer");
		}
	}
	
	public Data getMyFileData() {
		return myFileData;
	}	
	
	private void initPeer() throws IOException {
		
		me = new PeerMaker(new Number160(this.username)).setPorts(PORT)
				.setEnableIndirectReplication(true).setEnableTracker(true).makeAndListen();
		
		me.bootstrap().setPeerAddress(new PeerAddress(ID, Inet4Address.getByName(HOST))).start();
		
		FutureDHT futureDHT = me.get(new Number160(username + "-file")).setDigest().start();
        futureDHT.awaitUninterruptibly();
        
        myFileData = futureDHT.getData();
	}


}
