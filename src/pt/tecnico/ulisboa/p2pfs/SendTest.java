package pt.tecnico.ulisboa.p2pfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureBootstrap; 
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;


public class SendTest {

	private static Peer peer;

	private static Lock lock= new ReentrantLock();
	public static List<GossipDTO> gossipList = new ArrayList<GossipDTO>();

	public static float avg ;
	public static Integer peerID = null;
	
	private static final int NITERACOES = 3;
	

	public static float getAvg() {
		return avg;
	}


	public static void setAvg(float avg) {
		SendTest.avg = avg;
	}


	public static Integer getPeerID() {
		return peerID;
	}


	public static void setPeerID(Integer peerID) {
		SendTest.peerID = peerID;
	}


	public SendTest(final int peerId) throws Exception {

		//Make Peer	
		peer = new PeerMaker(Number160.createHash(peerId)).setPorts(6000 + peerId).setEnableIndirectReplication(true)
				.setReplicationRefreshMillis(10000).setEnableTracker(true).makeAndListen();

		FutureBootstrap fb = peer.bootstrap().setBroadcast().setPorts(6001).start();
		fb.awaitUninterruptibly();

		peer.setObjectDataReply( new ObjectDataReply()
		{

			//setupReply -- Handler resposta não interessa mas para guardar dados recebidos.
			@Override
			public Object reply( PeerAddress sender, Object request )throws Exception{

				if (request.getClass() == GossipDTO.class){
					GossipDTO dto = (GossipDTO) request;
					lock.lock();
					gossipList.add(dto);
					lock.unlock();
					System.out.println("I'm "+ peerId +" and I just got the message From nodeID " + dto.getNodeID() + " with value= " + 
							dto.getValor() +"and weight -" +dto.getPeso());

					return "--AKC--";
				}
				return "--AKC--";
			}
		} );

		if (fb.getBootstrapTo() != null) {
			peer.discover().setPeerAddress(fb.getBootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}
	}


	private Object get(final int userID) throws ClassNotFoundException, IOException {
		FutureDHT futureDHT = peer.get(Number160.createHash(userID)).start();
		futureDHT.awaitUninterruptibly();

		if (futureDHT.isSuccess()) {

			// System.out.println("we found the data on " + futureDHT.getRawDigest().size() + " peers"); 
			return futureDHT.getData().getObject();
		}

		return "not found";
	}

	private void store(int userID, String value) throws IOException {
		peer.put(Number160.createHash(userID)).setData(new Data(value)).start().awaitUninterruptibly();
	}

	private void remove(String key) throws IOException {
		peer.remove(Number160.createHash(key));
	}

	public static void main(String[] args) throws NumberFormatException, Exception {

		int userID = 0;
		BufferedReader br = null;

		try {
			System.out.print("Please insert your ID");
			br = new BufferedReader(new InputStreamReader(System.in));

			String userId = br.readLine(); 		
			System.out.println("USERID -" + userId);

			System.out.print("Please insert your AVG value");        	
			String AVG = br.readLine();

			userID = Integer.parseInt(userId);

			setPeerID(userID);
			setAvg(Integer.parseInt(AVG));


			System.out.println("AVG--, " + avg);

		} catch (IOException ioe) {
			System.out.println("IO error trying to read your name!");
			System.exit(1);
		}

		SendTest usr = new SendTest(userID);

		System.out.println("Try first getjust because");

		FutureDHT futureDHT = peer.get(Number160.createHash(userID)).setDigest().start();
		futureDHT.awaitUninterruptibly();
		System.out.println("We found the data on " + futureDHT.getRawDigest().size() + " peers");


		br.readLine();
		System.out.println("------------Start----------");

		float peso= 1;
		Thread.sleep(5000);

		for (int a = 0 ; a < NITERACOES; a ++ ){
			
			lock.lock();
			GossipDTO dto = new GossipDTO(userID , getAvg() , peso);	
			gossipList.add(dto);
			
			float avg = 0;
			
			peso = 0;
			for (int b = 0; b < gossipList.size(); b++){
				//fax calculos e guarda novos valores  
				System.out.println("Somando--avg" + gossipList.get(b).getValor());
				avg = avg + gossipList.get(b).getValor();
				System.out.println("Somando--peso" + gossipList.get(b).getPeso());
				peso = peso + gossipList.get(b).getPeso();

			}
			System.out.println("gossip size - "+ gossipList.size());
		
			setAvg (avg);
			System.out.println("New avg--" + getAvg());
			System.out.println("New peso--" + peso);

			gossipList.clear();
			lock.unlock();

			dto = new GossipDTO(userID , getAvg()/2 , peso/2);	
			//usr.SendMySelf(dto);
			usr.SendOne(dto);
			//poem gossip a zeros 
			Thread.sleep(5000);
		}

		System.out.println( "Estimativa media - " + ( getAvg() / peso));

		br.readLine();
		peer.shutdown();
	}



	private void SendOne(GossipDTO dto)
	{
		RequestP2PConfiguration requestP2PConfiguration = new RequestP2PConfiguration( 1, 10, 0 );
		Random RND = new Random();
		FutureDHT futureDHT = peer.send(Number160.createHash(RND.nextInt() ) ) 
				.setObject(dto).setRequestP2PConfiguration( requestP2PConfiguration ).start();
		futureDHT.awaitUninterruptibly();
		for(Object object:futureDHT.getRawDirectData2().values())
		{
			System.out.println("got:"+ object);
		}
	}


	private void SendMySelf(GossipDTO dto)
	{
		RequestP2PConfiguration requestP2PConfiguration = new RequestP2PConfiguration( 1, 10, 0 );

		FutureDHT futureDHT = peer.send(Number160.createHash(getPeerID()) ) 
				.setObject(dto).setRequestP2PConfiguration( requestP2PConfiguration ).start();
		futureDHT.awaitUninterruptibly();
		for(Object object:futureDHT.getRawDirectData2().values())
		{
			System.out.println("got:"+ object);
		}
	}
}

