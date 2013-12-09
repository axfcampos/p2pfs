package pt.ulisboa.tecnico.p2pfs.kademlia;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.ShortString;
import net.tomp2p.storage.Data;

import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaDto;
import pt.ulisboa.tecnico.p2pfs.fuse.MemoryDirectory;

public class Kademlia {
	
	static int PORT = 9102;
	
	static Number160 ID = new Number160(1);
	
	static String HOST = "188.140.5.88";
	
	String username;
	
	Peer me;
	
	FuseKademliaDto myFileData;
	
	public Kademlia(String username) {
		
		this.username = username;
		
		try {
			
			initPeer();
		
		} catch (IOException e) {
			
			System.out.println("Failed initiating peer : " + e.toString());
			System.exit(0);
			
		} catch (ClassNotFoundException e) {

			System.out.println("Failed initiating peer : " + e.toString());
			System.exit(0);
		}
	}
	
	public FuseKademliaDto getMyFileData() {
		return myFileData;
	}	
	
	private void initPeer() throws IOException, ClassNotFoundException {
		
		
		me = new PeerMaker(new Number160(new ShortString(username))).setPorts(PORT)
				.setEnableIndirectReplication(true).setEnableTracker(true).makeAndListen();
		
		InetAddress address = Inet4Address.getByName(HOST);
		
		FutureDiscover futureDiscover = me.discover().setInetAddress( address ).setPorts( 9101 ).start();
		futureDiscover.awaitUninterruptibly();
		
		FutureBootstrap futureBootstrap = me.bootstrap().setInetAddress( address ).setPorts( 9101 ).start();
		futureBootstrap.awaitUninterruptibly();
		
		
		
		FutureDHT futureDHT = getMyFile("/");
        Data data = futureDHT.getData();
        
        if(data == null) {
        	createMyFile("/");
        } else {
        	myFileData = (FuseKademliaDto) data.getObject();
        }
	}
	
	private void createMyFile(String path) throws IOException, ClassNotFoundException {
		
		Data data = new Data(new FuseKademliaDto());
		
		FutureDHT futureDHT = me.put(new Number160(new ShortString(username + "-file-" + path)))
				 .setRefreshSeconds(2).setData(data).start();
		futureDHT.awaitUninterruptibly();
		
		myFileData = (FuseKademliaDto) data.getObject();
	        
	}
	
	private FutureDHT getMyFile(String path) {
		Number160 location = new Number160(new ShortString(username + "-file-" + path));
		
		FutureDHT futureDHT = me.get(location).start();
        futureDHT.awaitUninterruptibly();
        
        return futureDHT;
	}
	
	public FuseKademliaDto getDirectoryObject(String path) throws ClassNotFoundException, IOException {
		
		return (FuseKademliaDto) getMyFile(path).getData().getObject();
		
	}


}
