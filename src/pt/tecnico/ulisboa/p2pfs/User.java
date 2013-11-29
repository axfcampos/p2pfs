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
	private String userLoginName;

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
    	
    	
    	User usr = new User(create_user());
    	usr.userLoginName = usr.retrieveLoginName();
    	usr.retrieveMetadata();
    	usr.shell_loop();
    	
    	//System.out.println("No Exception");

    
//////////////    	
//    	
//        if (args.length == 3) {
//            usr.store(args[1], args[2]);
//        }
//        if (args.length == 2) {
//            System.out.println("Key:" + args[1] + " Value:" + usr.get(args[1]));
//        }
		
    }
    
    
    private static int create_user(){
    	System.out.println("Inser user id (this is the ID that will decide your 'place' on the P2PFS network):");
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	String userID = null;
    	try {
    		userID = br.readLine();
    	} catch (IOException ioe) {
    		System.out.println("IO error trying to read your name!");
    		System.exit(1);
    	}
    	return Integer.parseInt(userID);
    }
    
    private String retrieveLoginName(){
    	
    	System.out.println("Insert your P2PFS login name (this name will find your files in the network):");
    	String userName = null;
    	try {
    		userName = (new BufferedReader(new InputStreamReader(System.in))).readLine();
    	} catch (IOException ioe) {
    		System.out.println("IO error trying to read your name!");
    		System.exit(1);
    	}
    	System.out.println("NAME: " + userName);
    	return userName;
    }
    
    private void retrieveMetadata(){
    	try {
    		//Verificar se já existe ficheiro de metadaddos	

    		Object obj = this.get(this.userLoginName); 
    		if(obj == "not found"){
    			System.out.println("No filesystem found, starting a new one.");

    			Directory root = new Directory(this.userLoginName + " RootDir");
    			root.addFile("FileA");
    			root.addFile("FileB");

    			this.store(this.userLoginName,root);	 
    		}else{
    			Directory roott =  (Directory) obj;
    			System.out.println( "Root found: " + roott.getDirName());

    			for(String S:roott.getFilesList()){
    				System.out.println( "File" + S ); 
    			}
    		}		 
    	}catch (Exception e){

    		System.out.println("Exception caught " + e.toString());
    	}

    	
    }
    
    
    private void shell_loop() throws IOException{
    	
    	System.out.println("Welcome to the P2PFS shell (type 'help' for list of commands)");
    	String input;
    	while(!(input = (new BufferedReader(new InputStreamReader(System.in))).readLine() ).equals("quit")){
    		
    		if(input.equals("help")){
    			System.out.println("Command list: \n help \n put \'key\' \'value\' \n get \'key\'");
    		}else{
    		if((input.split(" "))[0].equals("put") && (input.split(" ")).length == 3){
    			
    			//faz put
    			
    		}else{
    		if((input.split(" "))[0].equals("get") && (input.split(" ")).length == 2){
    			
    			//faz get
    			
    		}else{
    			System.out.println("Error: malformed input, type 'help' for commands");
    		}}}
    	}
    	
    	this.remove(this.userLoginName);
    	System.out.println("bye.");
    	
    }
    
}
