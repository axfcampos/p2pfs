package pt.tecnico.ulisboa.p2pfs;

import java.io.IOException;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureBootstrap; 
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public class User {

	private Peer peer;

    public User(int peerId) throws Exception {
        peer = new PeerMaker(Number160.createHash(peerId)).setPorts(4000 + peerId).makeAndListen();
        FutureBootstrap fb = peer.bootstrap().setBroadcast().setPorts(4001).start();
        fb.awaitUninterruptibly();
        if (fb.getBootstrapTo() != null) {
            peer.discover().setPeerAddress(fb.getBootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
    }
    
    public User(){
    	
    
    }

    private String get(String name) throws ClassNotFoundException, IOException {
        FutureDHT futureDHT = peer.get(Number160.createHash(name)).start();
        futureDHT.awaitUninterruptibly();
        if (futureDHT.isSuccess()) {
            return futureDHT.getData().getObject().toString();
        }
        return "not found";
    }

    private void store(String key, String value) throws IOException {
        peer.put(Number160.createHash(key)).setData(new Data(value)).start().awaitUninterruptibly();
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
		
    	User usr = new User();
		LocalFsManager lfsm = new LocalFsManager();
		lfsm.mounts();
//    	User usr = new User(Integer.parseInt(args[0]));
//        if (args.length == 3) {
//            usr.store(args[1], args[2]);
//        }
//        if (args.length == 2) {
//            System.out.println("Key:" + args[1] + " Value:" + usr.get(args[1]));
//        }
		
    }
}
