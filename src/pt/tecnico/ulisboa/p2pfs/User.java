package pt.tecnico.ulisboa.p2pfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureBootstrap; 
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public class User {

	private Peer peer;

    public User(int peerId) throws Exception {
    //   peer = new PeerMaker(Number160.createHash(peerId)).setPorts(4000 + peerId).makeAndListen();
      peer = new PeerMaker(Number160.createHash(peerId)).setPorts(5000 + peerId).setEnableIndirectReplication(true)
    		  .setReplicationRefreshMillis(10000).setEnableTracker(true).makeAndListen();
//       peer1 = new PeerMaker(new Number160(nr1)).setPorts(port1).setEnableIndirectReplication(true)
//                .makeAndListen();
        
        FutureBootstrap fb = peer.bootstrap().setBroadcast().setPorts(5001).start();
        fb.awaitUninterruptibly();
        if (fb.getBootstrapTo() != null) {
            peer.discover().setPeerAddress(fb.getBootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
    }
    
//    public User(){
//    	
//    }

    private Object get(String name) throws ClassNotFoundException, IOException {
        FutureDHT futureDHT = peer.get(Number160.createHash(name)).start();
        futureDHT.awaitUninterruptibly();
        if (futureDHT.isSuccess()) {
    
        	if (Directory.class == futureDHT.getData().getObject().getClass()){
        		System.out.println("Its a directory");
        		return futureDHT.getData().getObject();
        	}
        	return futureDHT.getData().getObject();
        }
        
        return "not found";
    }

    private void store(String key, Object value) throws IOException {
        peer.put(Number160.createHash(key)).setData(new Data(value)).start().awaitUninterruptibly();
    }
    
    private void remove(String key) throws IOException {
        peer.remove(Number160.createHash(key));
    }
    
    public static void main(String[] args) throws NumberFormatException, Exception {
    	/**
    	 * To run this example, you first have to start the well known peer on port 4001:
    	 * >> java User 1 chave valor
    	 * Then you can add as many other clients as you want:
    	 * >> java User 2 chave
    	 * The output should look something like
    	 * >> Chave:chave Valor:valor
    	 * */ 
		
//    	User usr = new User();
//		LocalFsManager lfsm = new LocalFsManager();
 //		lfsm.mounts();
 
    	// Este valor 
    	System.out.print("Please insert your ID (provisório)");
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	String userID = null;
    	try {
    		userID = br.readLine();
    	} catch (IOException ioe) {
    		System.out.println("IO error trying to read your name!");
    		System.exit(1);
    	}
    	System.out.println("USERID -" + userID);
    	
       	User usr = new User(Integer.parseInt(userID));
  	
    	System.out.print("Please insert your P2P login Name: ");
    	String userName = null;
    	try {
    		userName = br.readLine();
    	} catch (IOException ioe) {
    		System.out.println("IO error trying to read your name!");
    		System.exit(1);
    	}
    	System.out.println("NAME, " + userName);

    	try {
    	//Verificar se já existe ficheiro de metadaddos	
    	
    	 
    	 if(usr.get(userName) == "not found"){
    	System.out.println("not found é preciso criar");
    	
    	Directory root = new Directory(userName + " RootDir");
    	root.addFile("FileA");
    	root.addFile("FileB");
    	
    	usr.store(userName,root);	 
    	 }else{
    		 System.out.println( "Root founded -" + usr.get(userName));
    		 Directory roott =  (Directory) usr.get(userName);
    		 System.out.println( "Root founded -" + roott.getDirName());
    		 
    		 for(String S:roott.getFilesList()){
    			 System.out.println( "File" + S ); 
    		 }
    	 }		 
    	}catch (Exception e){
    		
    	System.out.println("Exception caught " + e.toString());
    	}

    	//System.out.println("No Exception");
    	
    	br.readLine();  
      	System.out.println("Try remove" + userName);
    	usr.remove(userName);
    	
//////////////    	
//    	
//        if (args.length == 3) {
//            usr.store(args[1], args[2]);
//        }
//        if (args.length == 2) {
//            System.out.println("Key:" + args[1] + " Value:" + usr.get(args[1]));
//        }
		
    }
}
