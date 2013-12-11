package pt.ulisboa.tecnico.p2pfs.kademlia;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.ShortString;
import net.tomp2p.storage.Data;
import pt.tecnico.ulisboa.p2pfs.Directory;
import pt.ulisboa.tecnico.p2pfs.MyPeerMaker;
import pt.ulisboa.tecnico.p2pfs.MyStorageMemory;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaDto;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaEntryDto;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaFileDto;
import pt.ulisboa.tecnico.p2pfs.fuse.P2PFilesystem;

public class Kademlia {
	
	static int PORT = 9102;
	
	static Number160 ID = new Number160(1);
	
	static String HOST = "localhost";
	
	String username;
	long myId;
	Peer me;
	private P2PFilesystem p2pfs;
	private MyStorageMemory myStorageMemory;
	
	FuseKademliaDto myFileData;
	
	public Kademlia(long id, P2PFilesystem p2pfs) {
		
//		this.username = username;
		this.p2pfs = p2pfs;
		this.myId = id;
		
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
	
	public MyStorageMemory getStorageMemory(){
		return myStorageMemory;
	}
	
	public boolean isMounted(){
		return p2pfs.isMounted();
	}
	
	public void setUserName(String username){
		this.username = username;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public FuseKademliaDto getMyFileData() {
		return myFileData;
	}	
	
	private void initPeer() throws IOException, ClassNotFoundException {
		
		MyPeerMaker peerMaker = new MyPeerMaker(Number160.createHash(myId));
		
		
		me = peerMaker.setPorts(PORT)
				.setEnableIndirectReplication(true).setEnableTracker(true).makeAndListen();
		myStorageMemory = (MyStorageMemory) peerMaker.getStorage();
		
		//associar/lancar classes de gossip
		
		
		InetAddress address = Inet4Address.getByName(HOST);
		
		FutureDiscover futureDiscover = me.discover().setInetAddress( address ).setPorts( 9101 ).start();
		futureDiscover.awaitUninterruptibly();
		
		FutureBootstrap futureBootstrap = me.bootstrap().setInetAddress( address ).setPorts( 9101 ).start();
		futureBootstrap.awaitUninterruptibly();
	}
	
	public void getMetadata() throws IOException, ClassNotFoundException{
		
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
	
	public void removeFile(String path) throws ClassNotFoundException, IOException {
		
		FutureDHT futureDHT = me.remove(Number160.createHash(username + "-" + path)).start();
		futureDHT.awaitUninterruptibly();
		
		String parentDir;
		
		if(path.substring(0, path.lastIndexOf("/") + 1).equals("/"))
			parentDir = "/";
		else
			parentDir = path.substring(0, path.lastIndexOf("/"));
		
		System.out.println(parentDir);
		
		futureDHT = me.get(Number160.createHash(username + "-file-" + parentDir)).start();
		futureDHT.awaitUninterruptibly();
		
		FuseKademliaDto dto = (FuseKademliaDto) futureDHT.getData().getObject();
		
		dto.removeContent(path.substring(path.lastIndexOf("/") + 1));
		
		futureDHT = me.put(Number160.createHash(username + "-file-" + parentDir))
				 .setRefreshSeconds(2).setData(new Data(dto)).start();
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
		
		FutureDHT futureDHT = me.remove(Number160.createHash(username + "-file-" + path)).start();
		futureDHT.awaitUninterruptibly();
		
		FuseKademliaDto dto;
		
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

	public void renameFile(String oldPath, String newPath) throws ClassNotFoundException, IOException {
		
		FutureDHT futureDHT = me.get(Number160.createHash(username + "-" + oldPath)).start();
		futureDHT.awaitUninterruptibly();
		
		futureDHT = me.put(Number160.createHash(username + "-" + newPath))
		 			.setRefreshSeconds(2).setData(new Data((FuseKademliaFileDto) futureDHT.getObject())).start();
		
		futureDHT = me.remove(Number160.createHash(username + "-" + oldPath)).start();
		futureDHT.awaitUninterruptibly();
		
		String parentDir;
		
		if(oldPath.substring(0, oldPath.lastIndexOf("/") + 1).equals("/"))
			parentDir = "/";
		else
			parentDir = oldPath.substring(0, oldPath.lastIndexOf("/"));
		
		System.out.println(parentDir);
		
		futureDHT = me.get(Number160.createHash(username + "-file-" + parentDir)).start();
		futureDHT.awaitUninterruptibly();
		
		FuseKademliaDto dto = (FuseKademliaDto) futureDHT.getData().getObject();
		
		dto.removeContent(oldPath.substring(oldPath.lastIndexOf("/") + 1));
		
		futureDHT = me.put(Number160.createHash(username + "-file-" + parentDir))
				 .setRefreshSeconds(2).setData(new Data(dto)).start();
		futureDHT.awaitUninterruptibly();
		
		
		updateDirectory(newPath.substring(0, newPath.lastIndexOf("/")),
							new FuseKademliaEntryDto(newPath.substring(newPath.lastIndexOf("/") + 1), 'f'));
		
		
	}

	
	//Para quando nao temos o FUSE a funcionar
	public Object get(String name) throws ClassNotFoundException, IOException {
	    FutureDHT futureDHT = me.get(Number160.createHash(name)).start();
	    futureDHT.awaitUninterruptibly();
	    if (futureDHT.isSuccess()) {
	
	    	return futureDHT.getData().getObject();
	    }

	    return "not found";
    }

	public void store(String key, Object value) throws IOException {
        me.put(Number160.createHash(key)).setData(new Data(value)).start().awaitUninterruptibly();
	}
    
    public void remove(String key) throws IOException {
        me.remove(Number160.createHash(key));
    }

}
