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
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaEntryDto;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaFileDto;

public class Kademlia {
	
	static int PORT = 9102;
	
	static Number160 ID = new Number160(1);
	
	static String HOST = "localhost";
	
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
		
		
		
		FutureDHT futureDHT = getDirFile("/");
        Data data = futureDHT.getData();
        
        if(data == null) {
        	createDirFile("/");
        } else {
        	myFileData = (FuseKademliaDto) data.getObject();
        }
	}
	
	public void createDirFile(String path) throws IOException, ClassNotFoundException {
		
		Data data = new Data(new FuseKademliaDto(path));
		
		FutureDHT futureDHT = me.put(Number160.createHash(username + "-file-" + path))
				 .setRefreshSeconds(2).setData(data).start();
		futureDHT.awaitUninterruptibly();
		
		myFileData = (FuseKademliaDto) data.getObject();
	        
	}
	
	private FutureDHT getDirFile(String path) {
		
		Number160 location = Number160.createHash(username + "-file-" + path);
		
		FutureDHT futureDHT = me.get(location).start();
        futureDHT.awaitUninterruptibly();	
        
        return futureDHT;
	}
	
	public FuseKademliaDto getDirectoryObject(String path) throws ClassNotFoundException, IOException {
		
		System.out.println("DIR OBJ: " + path);
		
		FuseKademliaDto dto = (FuseKademliaDto) getDirFile(path).getData().getObject();
		
		return dto;
		
	}

	public Object getFileObject(String path) throws ClassNotFoundException, IOException {
		
		Number160 location = Number160.createHash(username + "-" + path);
		
		FutureDHT futureDHT = me.get(location).start();
        futureDHT.awaitUninterruptibly();
        
		return futureDHT.getData().getObject();
		
	}

	public void createFile(String path) throws IOException {
		
		FutureDHT futureDHT = me.put(Number160.createHash(username + "-" + path))
				 .setRefreshSeconds(2).setData(new Data(new FuseKademliaFileDto())).start();
		futureDHT.awaitUninterruptibly();
       
	}
	
	public void removeFile(String path) {
		
		FutureDHT futureDHT = me.remove(Number160.createHash(username + "-" + path)).start();
		futureDHT.awaitUninterruptibly();
      
	}

	public void updateDirectory(String path, FuseKademliaEntryDto entry) throws ClassNotFoundException, IOException {
			
		FuseKademliaDto dto = (FuseKademliaDto) getDirectoryObject(path);
		
		dto.addContent(entry);
		
		
		FutureDHT futureDHT = me.put(Number160.createHash(username + "-file-" + path))
				 .setRefreshSeconds(2).setData(new Data(dto)).start();
		futureDHT.awaitUninterruptibly();
      
	}

	public void updateFile(String path, FuseKademliaFileDto dto) throws IOException {
		
		FutureDHT futureDHT = me.put(Number160.createHash(username + "-" + path))
				 .setRefreshSeconds(2).setData(new Data(dto)).start();
		futureDHT.awaitUninterruptibly();
		
	}

	public void removeDir(String path) throws ClassNotFoundException, IOException {
		
		System.out.println("TOTAL: " + path);
		
		FutureDHT futureDHT = me.remove(Number160.createHash(username + "-file-" + path)).start();
		futureDHT.awaitUninterruptibly();
		
		FuseKademliaDto dto;
		
		System.out.println("PAI: "+ path.substring(0, path.lastIndexOf("/")));
		
		if(path.substring(0, path.lastIndexOf("/")+1).equals("/")) {
			dto = getDirectoryObject("/");
			dto.removeContent(path.substring(path.indexOf("/") + 1));
			
			futureDHT = me.put(Number160.createHash(username + "-file-/"))
					 .setRefreshSeconds(2).setData(new Data(dto)).start();
			futureDHT.awaitUninterruptibly();
			
		} else {
			dto = getDirectoryObject(path.substring(0, path.lastIndexOf("/")));
			dto.removeContent(path.substring(path.indexOf("/") + 1));
			
			futureDHT = me.put(Number160.createHash(username + "-file-" + path.substring(0, path.lastIndexOf("/"))))
					 .setRefreshSeconds(2).setData(new Data(dto)).start();
			futureDHT.awaitUninterruptibly();
		}
		
	}


}
