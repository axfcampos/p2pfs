package pt.ulisboa.tecnico.p2pfs.kademlia;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.ShortString;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import pt.tecnico.ulisboa.p2pfs.Directory;
import pt.ulisboa.tecnico.p2pfs.MyPeerMaker;
import pt.ulisboa.tecnico.p2pfs.MyStorageMemory;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaDto;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaEntryDto;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaFileDto;
import pt.ulisboa.tecnico.p2pfs.fuse.P2PFilesystem;
import pt.ulisboa.tecnico.p2pfs.gossip.Gossip;
import pt.ulisboa.tecnico.p2pfs.gossip.GossipDTO;

public class Kademlia {
	
	private static final int PORT = 9102;
	
//	private static final Number160 ID = new Number160(1);
	
	private static final String HOST = "planetlab-1.tagus.ist.utl.pt";
//	private static final String HOST = "localhost";
	
	private static final int CONTENT_MAX_SIZE = 999999999;
	
	String username;
	long myId;
	Peer me;
	private P2PFilesystem p2pfs;
	private MyStorageMemory myStorageMemory;
	public static 	Gossip gossip;
	
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
		gossip = new Gossip(this,me,myStorageMemory,myId);
		
		
		me.setObjectDataReply( new ObjectDataReply()
		{

			//SetupReply
			@Override
			public Object reply( PeerAddress sender, Object request )throws Exception{

				if (request.getClass() == GossipDTO.class){
					GossipDTO dto = (GossipDTO) request;

					
//					System.out.println("I'm "+ myId + " and I just got the message From nodeID " + dto.getNodeId());
					
					if (dto.getNodeId() == myId){
						//TODO dava jeito...........
//						System.out.println("1");
						return "receive from myshelf dont count";
					}

					
					if((gossip.gossipStart == false)){
						//System.out.println("3");
						
						
						
						gossip.gossipStart = true;
						//gossipRound = dto.getRoundId();
						gossip.starterId = dto.getStarterId();
					}
					
//					if(dto.getNodeId() > getPeerID() && dto.getRoundId() == gossipRound ){
//						System.out.println("4");
//						//TODO enviar DTO com estado actual para nó iniciar contagem
//						
//					return gossip;
//					}
					
					gossip.lock.lock();
					gossip.gossipList.add(dto);
					gossip.lock.unlock();
//					System.out.println("I'm "+ myId + " and I just got the message From nodeID " + dto.getNodeId());
					return "--AKC--";
				}

				return "--AKC -- Not receive a GossipDTO";
			}

			
		} );
		
	
		
		Thread t = new Thread(gossip);
		t.start();
//	gossip.gossipStart();
		
		
		
		InetAddress address = Inet4Address.getByName(HOST);

//		FutureDiscover futureDiscover = me.discover().setInetAddress( address ).setPorts( PORT ).start();
//		futureDiscover.awaitUninterruptibly();
		
		FutureBootstrap futureBootstrap = me.bootstrap().setInetAddress( address ).setPorts( PORT ).start();
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
	
	public P2PFilesystem getFuse(){
		
		return this.p2pfs;
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
		
		return get(username + "-" + path);
	}

	public void createFile(String path) throws IOException {
		
		FutureDHT futureDHT = me.put(Number160.createHash(username + "-" + path + ":part1"))
				.setData(new Data(new FuseKademliaFileDto()))
					.start().awaitUninterruptibly();
		futureDHT.awaitUninterruptibly();
		
	}
	
	public void removeFile(String path) throws ClassNotFoundException, IOException {
		
		remove(username + "-" + path);
		
		String parentDir;
		
		if(path.substring(0, path.lastIndexOf("/") + 1).equals("/"))
			parentDir = "/";
		else
			parentDir = path.substring(0, path.lastIndexOf("/"));
		
		System.out.println(parentDir);
		
		FutureDHT futureDHT = me.get(Number160.createHash(username + "-file-" + parentDir)).start();
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
		
		store(username + "-" + path, dto);
		
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
		
		store(username + "-" + newPath, (FuseKademliaFileDto) futureDHT.getObject());
		
		
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
	public FuseKademliaFileDto get(String name) throws ClassNotFoundException, IOException {
		
		FutureDHT futureDHT = me.get(Number160.createHash(name + ":part1")).start();
	    futureDHT.awaitUninterruptibly();
	    
	    if (futureDHT.isSuccess()) {
	    	
	    	FuseKademliaFileDto finalDto = (FuseKademliaFileDto) futureDHT.getData().getObject();
	    	
	    	int totalParts = finalDto.getTotalNumberParts();
	    	int i;
	    	
	    	finalDto.setPartNumber(1);
	    	finalDto.setTotalNumberParts(1);
	    	
	    	for (i = 2; i < (totalParts + 1); i++) {
	    		
	    		futureDHT = me.get(Number160.createHash(name + ":part" + i)).start();
	    	    futureDHT.awaitUninterruptibly();
	    	    
	    	    if (futureDHT.isSuccess())
	    	    	finalDto.setContent(finalDto.getContent() +
	    	    				((FuseKademliaFileDto)futureDHT.getData().getObject()).getContent());
	    		
	    	}
	    	
	    	
	
	    	return finalDto;
	    }

	    return null;
    }

	// Just works with files!!!
	public void store(String key, FuseKademliaFileDto value) throws IOException {
		
		int contentLength = value.getContent().length();
		int aux = contentLength;
		int i;
		int total = contentLength / CONTENT_MAX_SIZE + 1;
		
		String subString;
		
		FutureDHT futureDHT;
		
		for(i = 0; contentLength > 0; i++, aux -= CONTENT_MAX_SIZE) {

			if((i + 1) * CONTENT_MAX_SIZE < contentLength)
				subString = value.getContent().substring(i * CONTENT_MAX_SIZE, ( i + 1) * CONTENT_MAX_SIZE);
			else
				if(i * CONTENT_MAX_SIZE < contentLength)
					subString = value.getContent().substring(i * CONTENT_MAX_SIZE, contentLength - 1);
				else
					break;
			
			futureDHT = me.put(Number160.createHash(key + ":part" + (i+1)))
							.setData(new Data(new FuseKademliaFileDto(i + 1, total, subString)))
								.start().awaitUninterruptibly();
			futureDHT.awaitUninterruptibly();
		}
	}
    
    public void remove(String name) throws IOException, ClassNotFoundException {
    	FutureDHT futureDHT = me.get(Number160.createHash(name + ":part1")).start();
	    futureDHT.awaitUninterruptibly();
	    
	    FuseKademliaFileDto dto = (FuseKademliaFileDto) futureDHT.getData().getObject();
	    
	    int totalParts = dto.getTotalNumberParts();
    	int i;
    	
    	for (i = 1; i < (totalParts + 1); i++) {
    		
    		futureDHT = me.remove(Number160.createHash(name + ":part" + i)).start();
    	    futureDHT.awaitUninterruptibly();
    	    
    	}
    	
    }

}
