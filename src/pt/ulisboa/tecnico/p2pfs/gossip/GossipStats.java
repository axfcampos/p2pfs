package pt.ulisboa.tecnico.p2pfs.gossip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
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

// Exemplco de calculo da média 
//TODO actualizar

public class GossipStats {

	private static Peer peer;
	public static Integer peerID = null;
	

	private static Lock lock= new ReentrantLock();
	public static List<GossipDTO> gossipList = new ArrayList<GossipDTO>();


	public static GossipDTO gossip;

	public static float estimativa;

	private static final int NITERACOES = 6;
	private static final int ITERACTIME = 10000;
	
	private static final int ROUNDTIME = 5; // MINUTOS
	
	public float um = 1;
	
	
	//public static  int gossipRound = 1;
	public static boolean changeGossipID= false;
	public static boolean gossipStart = false;
	public static boolean iniciator = true;

	public static boolean mounted=false;
	
	public static long starterId;
	public static Integer getPeerID() {
		return peerID;
	}

	public static void setPeerID(Integer peerID) {
		GossipStats.peerID = peerID;
	}

	public GossipStats(final int peerId) throws Exception {

		//Make Peer	
		peer = new PeerMaker(Number160.createHash(peerId)).setPorts(6100 + peerId).setEnableIndirectReplication(true)
				.setReplicationRefreshMillis(10000).setEnableTracker(true).makeAndListen();

		FutureBootstrap fb = peer.bootstrap().setBroadcast().setPorts(6101).start();
		fb.awaitUninterruptibly();

		peer.setObjectDataReply( new ObjectDataReply()
		{

			//SetupReply
			@Override
			public Object reply( PeerAddress sender, Object request )throws Exception{

				if (request.getClass() == GossipDTO.class){
					GossipDTO dto = (GossipDTO) request;

//					System.out.println("I'm "+ getPeerID() + " and I just got the message From nodeID " + dto.getNodeId()+"myGossip-"+gossipRound+ "gossip on DTO"+dto.getRoundId());
					
					System.out.println("I'm "+ getPeerID() + " and I just got the message From nodeID " + dto.getNodeId());
					
					if (dto.getNodeId() == getPeerID()){
						//TODO dava jeito...........
						System.out.println("1");
						return "receive from myshelf dont count";
					}

//					if(dto.getRoundId() < gossipRound ){
//						
//						System.out.println("2");
//						
//						//retorna ronda actual para actualizar
//						//dto.setRoundId(gossipRound);
//						
//						return "Estas Atrazado";
//					}
					
					if((gossipStart == false)){
//						if((gossipStart == false) && (dto.getRoundId() == gossipRound) ){
						System.out.println("3");
						
						if(	dto.getNodeId() > getPeerID())
						{
							System.out.println("3");
						//TODO devia ter sido eu a começar
							return "STOP";
						}
						
						gossipStart = true;
						//gossipRound = dto.getRoundId();
						starterId = dto.getNodeId();
					}
					
//					if(dto.getNodeId() > getPeerID() && dto.getRoundId() == gossipRound ){
//						System.out.println("4");
//						//TODO enviar DTO com estado actual para nó iniciar contagem
//						
//					return gossip;
//					}
					
					lock.lock();
					gossipList.add(dto);
					lock.unlock();
					System.out.println("I'm "+ peerId + " and I just got the message From nodeID " + dto.getNodeId());
					return "--AKC--";
				}

				return "--AKC -- Not receive a GossipDTO";
			}
		} );

		if (fb.getBootstrapTo() != null) {
			peer.discover().setPeerAddress(fb.getBootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}
	}

	public static void main(String[] args) throws NumberFormatException, Exception {

		float files = 0;
		float users = 0;
		float actvUsers = 0;
		float NData = 0;
		
		BufferedReader br = null;
		try {

			br = new BufferedReader(new InputStreamReader(System.in));

			System.out.print("Please insert your ID");		
			String userId = br.readLine(); 		
			System.out.println("USERID -" + userId);
			//userID = Integer.parseInt(userId);
			setPeerID(Integer.parseInt(userId));

			gossip = new GossipDTO(1);

			System.out.print("Please insert your AVG-files value");        	
			String read = br.readLine();

			gossip.getAvgfiles().setValor(Integer.parseInt(read));
			files=	Integer.parseInt(read);
			System.out.println(" AVG-Files --" + gossip.getAvgfiles().getValor());

			System.out.print("Please insert your number of users to sum");        	
			read = br.readLine();

			gossip.getNusers().setValor(Integer.parseInt(read));
			users =	Integer.parseInt(read);
			System.out.println(" N users --" + gossip.getNusers().getValor());
			
			////////////////////////////////////////////////////////////////////////
			System.out.print("User is active??1 or 0");        	
			read = br.readLine();
			gossip.getNactivU().setValor(Integer.parseInt(read));
			actvUsers =	Integer.parseInt(read);
			System.out.println(" active-users --" + gossip.getNactivU().getValor());
			
			if(gossip.getNactivU().getValor()==1){
				mounted=true;
			}
			

		} catch (IOException ioe) {
			System.out.println("IO error trying to read your name!");
			System.exit(1);
		}

		GossipStats usr = new GossipStats(getPeerID());

		System.out.println("Try first get just because");
		
		//TODO Tentar outra solucao, este get permite que nó conheça os vizinhos....Booststrap

		FutureDHT futureDHT = peer.get(Number160.createHash(getPeerID())).setDigest().start();
		futureDHT.awaitUninterruptibly();
		System.out.println(" We found the data on" + futureDHT.getRawDigest().size() + " peers");

		//br.readLine();

		System.out.println("------------Begin gossiping!!----------");		

		//ganda for en que cada um espera um valor dependedo do seu id para 

		float pesoFiles ;
		float avgFiles;

		float pesoUsers;
		float avgUser;
		
		float pesoNode;
		float avgNode;
		
		
		float pesoAUsers ;
		float avgAUsers;


		//TODO set dos pesos
		//SEt dos Pesos

		
		float um =1;
		

		int time;
		// for sobre o mesmo id
		for (int a = 1 ; a < 3; a ++ ){

			//TODO get De novos valores
			System.out.println( "1º - for begin");
			
			///////////////////////////////////////////
			//SINC
			
			Calendar calendar = Calendar.getInstance();

			int minutes = calendar.get(Calendar.MINUTE);
			
			time = minutes % ROUNDTIME;

			System.out.println("Time" + time);
			int diff = ROUNDTIME - time; 
			minutes = calendar.get(Calendar.MINUTE);
			int actual = minutes%ROUNDTIME;
			
			while(diff >=  (ROUNDTIME-actual)){
				Thread.sleep(5000);
				
				calendar = Calendar.getInstance();
				minutes = calendar.get(Calendar.MINUTE);
				
				actual = minutes %ROUNDTIME;
				
//				System.out.println("inside"+ actual);
//				System.out.println("--"+ (ROUNDTIME- actual));
				
				
			}
			
			System.out.println("out");

			//////////////////////////////////////////
			
			
			
			gossip.getNusers().setValor(users);
			gossip.getNusers().setPeso(um);
			
			gossip.getAvgfiles().setValor(files);
			gossip.getAvgfiles().setPeso(um);
			
			gossip.getNnode().setValor(um);
			gossip.getNnode().setPeso(um);
		
			
			gossip.getNactivU().setValor(actvUsers);
			
			if(gossip.getNactivU().getValor() == 0){
				gossip.getNactivU().setPeso(0);
			}else{
				gossip.getNactivU().setPeso(1);
			}
			
			starterId = getPeerID();

			int waitTime = (getPeerID()*4)*1000;
			System.out.println( "I will wait "+ waitTime);
			
			gossipStart = false;
			//TODO tempo de espera depend do id

			for(time = 0 ; time < waitTime;time = time + 2000){
				Thread.sleep(2000);
				System.out.println("time"+ time);
				
				if(gossipStart){
					System.out.println("receive a DTO i will start- valor zero");
					iniciator=false;
					//para os couts
					gossip.getNnode().setValor(0);
					gossip.getNactivU().setValor(0);
					break;
				}
			}
			gossipStart = true;
			
			for (int c = 0 ; c < NITERACOES; c ++ ){

				System.out.println( "2º - for begin");
				
				lock.lock();				
				GossipDTO dto = new GossipDTO(getPeerID());
				//TODO
				
				dto.setNnode(gossip.getNnode());
				dto.setNusers(gossip.getNusers());
				dto.setNactivU(gossip.getNactivU());
				dto.setAvgfiles(gossip.getAvgfiles());
				
				dto.setIterc(c);

				gossipList.add(dto);

				//valor reiniciados para novo somatório
				pesoFiles =0 ;
				avgFiles=0;

				pesoUsers=0;
				avgUser=0;
				
				pesoNode=0;
				avgNode=0;
				
				pesoAUsers=0 ;
				avgAUsers=0;
				
				int b ;

				for (b = 0; b < gossipList.size(); b++){
					
					System.out.println("B-"+ b + "---" + gossipList.get(b).getNodeId());
					
//					if (gossipList.get(b).getRoundId() > gossipRound){
//						System.out.println( "inside firts if");
//						changeGossipID = true;
//						break ;
//					}
//
//					//Não conto com contagens anteriores
//					if (gossipList.get(b).getRoundId() < gossipRound){
//						System.out.println( "inside second if");
//						continue;
//					}
					
					System.out.println("IF---"+ gossipList.get(b).getNodeId() + "---" +starterId);
					if(gossipList.get(b).getNodeId() < starterId){
						//TODO conflito de iniciaçção
						
						System.out.println("Conflito continua apenas o menor id");
						GossipDTO n = gossipList.get(b);
						
						System.out.println("interc"+ n.getIterc()+ "---"+ c);
						int iteracoes= n.getIterc();
						
						gossipList.clear();
						gossipList.add(n);
						//Faz get dos valores de novo.
						
						System.out.println("-----"+( Math.pow(2,iteracoes)));
						System.out.println("---"+users);
						System.out.println("files"+files);
						
						gossip.getNusers().setValor((float) (users/( Math.pow(2,iteracoes))));
						gossip.getNusers().setPeso((float) (um/( Math.pow(2,iteracoes))));
			
						
						gossip.getNnode().setValor(0);
						gossip.getNnode().setPeso((float) (um/( Math.pow(2,iteracoes))));
						
						gossip.getNactivU().setValor(0);
						
						if(mounted){
							gossip.getNactivU().setPeso((float) (um /(Math.pow(2,iteracoes))));
						
						}else{
							gossip.getNactivU().setPeso(0);
						}

						System.out.println("gossip.getNactivU()"+ gossip.getNactivU().getPeso());
						
						System.out.println("interc"+ n.getIterc()+ "---"+ c);
						
						gossip.getAvgfiles().setValor((float) (files/( Math.pow(2,iteracoes))));
						gossip.getAvgfiles().setPeso((float) (um/( Math.pow(2,iteracoes))));
						
						dto = new GossipDTO(getPeerID());
						
						dto.setNnode(gossip.getNnode());
						dto.setNusers(gossip.getNusers());
						dto.setNactivU(gossip.getNactivU());
						dto.setAvgfiles(gossip.getAvgfiles());
						
						gossipList.add(dto);
						
						System.out.println("SUM "+n.getAvgfiles().getValor()+"--"+gossip.getAvgfiles().getValor());
						System.out.println("SUM "+n.getAvgfiles().getPeso()+"--"+gossip.getAvgfiles().getPeso());
						
						avgFiles = n.getAvgfiles().getValor() + gossip.getAvgfiles().getValor();
						pesoFiles = n.getAvgfiles().getPeso() + gossip.getAvgfiles().getPeso();
						
						
						System.out.println("SUM "+n.getNusers().getValor()+"--"+gossip.getNusers().getValor());
						System.out.println("SUM "+n.getNusers().getPeso()+"--"+gossip.getNusers().getPeso());
						
						avgUser = n.getNusers().getValor() + gossip.getNusers().getValor();
						pesoUsers = n.getNusers().getPeso() + gossip.getNusers().getPeso();
						
						
						avgNode = n.getNnode().getValor() + gossip.getNnode().getValor();
						pesoNode = n.getNnode().getPeso() + gossip.getNnode().getPeso();
						
						
						avgAUsers=n.getNactivU().getValor() + gossip.getNactivU().getValor();;
						pesoAUsers=n.getNactivU().getPeso() + gossip.getNactivU().getPeso();
						
						System.out.println("Conflito result"+avgFiles+"--"+avgUser+"--"+avgNode);
						
						System.out.println("Conflito result"+pesoFiles+"--"+pesoUsers+"--"+pesoNode);
						
						starterId = n.getNodeId();
						break;
						
					}
					//TODO ter enconta o node que originou a ronda.

					//C poden ser diferentes
					
					int dif=0;
					System.out.println("-c-"+ c + "cNode"+ gossipList.get(b).getIterc() );
					
					
//					if(c > gossipList.get(b).getIterc()){
//						
//						System.out.println("if----111111111111111");
//						
//						dif = c - gossipList.get(b).getIterc();
//					
//						System.out.println("dif"+ dif);
//						
//						avgFiles = (float) (avgFiles + (gossipList.get(b).getAvgfiles().getValor()/Math.pow(2,dif)));
//						pesoFiles  = (float) (pesoFiles  + (gossipList.get(b).getAvgfiles().getPeso()/Math.pow(2,dif)));
//											
//						avgUser = (float) (avgUser + (gossipList.get(b).getNusers().getValor()/Math.pow(2,dif)));
//						pesoUsers  = (float) (pesoUsers  + (gossipList.get(b).getNusers().getPeso()/Math.pow(2,dif)));
//
//						
//						avgNode = (float) (avgNode + (gossipList.get(b).getNnode().getValor()/Math.pow(2,dif)));
//						pesoNode  = (float) (pesoNode  + (gossipList.get(b).getNnode().getPeso()/Math.pow(2,dif)));
//					
//						continue;
//						
//					}
//						
//					if(c < gossipList.get(b).getIterc()){
//						System.out.println("if----222222222222");
//						dif = gossipList.get(b).getIterc()- c;
//					
//						System.out.println("dif"+ dif);
//					
//						System.out.println("A"+gossipList.get(b).getAvgfiles().getValor()+ "---" +Math.pow(2,dif));
//						System.out.println("A"+gossipList.get(b).getAvgfiles().getPeso()+ "---" +Math.pow(2,dif));
//						
//						System.out.println("A"+gossipList.get(b).getAvgfiles().getValor()*  Math.pow(2,dif));
//						System.out.println("A"+gossipList.get(b).getAvgfiles().getPeso() * Math.pow(2,dif));
//						
//						avgFiles = (float) 	   (avgFiles + (gossipList.get(b).getAvgfiles().getValor() * Math.pow(2,dif)));
//						pesoFiles  = (float) (pesoFiles  + (gossipList.get(b).getAvgfiles().getPeso()  * Math.pow(2,dif)));
//						
//						System.out.println("avgFile"+avgFiles);
//						System.out.println("avgFile"+pesoFiles);
//						
//						
//						avgUser = (float) (avgUser + (gossipList.get(b).getNusers().getValor()*Math.pow(2,dif)));
//						pesoUsers  = (float) (pesoUsers  + (gossipList.get(b).getNusers().getPeso()*Math.pow(2,dif)));
//
//						avgNode = (float) (avgNode + (gossipList.get(b).getNnode().getValor()*Math.pow(2,dif)));
//						pesoNode  = (float) (pesoNode  + (gossipList.get(b).getNnode().getPeso()*Math.pow(2,dif)));
//												
//						continue;
//					}	
					
					
					//TODO
					
					System.out.println( "2º - for begin");
					//faz calculos e guarda novos valores  
					System.out.println("Somando--avg" + gossipList.get(b).getAvgfiles().getValor());
					avgFiles = avgFiles + gossipList.get(b).getAvgfiles().getValor();
					System.out.println("Somando--peso" + gossipList.get(b).getAvgfiles().getPeso());
					pesoFiles  = pesoFiles  + gossipList.get(b).getAvgfiles().getPeso();

					System.out.println("Somando--avg" + gossipList.get(b).getNusers().getValor());
					avgUser = avgUser + gossipList.get(b).getNusers().getValor();
					System.out.println("Somando--peso" + gossipList.get(b).getNusers().getPeso());
					pesoUsers  = pesoUsers  + gossipList.get(b).getNusers().getPeso();

					
					System.out.println("Somando--getNnode" + gossipList.get(b).getNnode().getValor());
					avgNode = avgNode + gossipList.get(b).getNnode().getValor();
					System.out.println("Somando--peso" + gossipList.get(b).getNnode().getPeso());
					pesoNode  = pesoNode  + gossipList.get(b).getNnode().getPeso();
					
					
					System.out.println("Somando--getNactivU" + gossipList.get(b).getNactivU().getValor());
					avgAUsers = avgAUsers + gossipList.get(b).getNactivU().getValor();
					System.out.println("Somando--peso" + gossipList.get(b).getNactivU().getPeso());
					pesoAUsers  = pesoAUsers  + gossipList.get(b).getNactivU().getPeso();
					
					
					//TODO calculos para restantes e para o numero de nós
				}

				if(changeGossipID == true ){
					System.out.println( "Change gossip 2");
					//actualiza valor gossipRound
//					gossipRound = gossipList.get(b).getRoundId() + 1;

					//TODO hum???	
					gossipList.clear();
					lock.unlock();
					//poem gossip a zeros 
					Thread.sleep(5000);

					break;
				}

				gossip.getAvgfiles().setValor(avgFiles/2);
				gossip.getAvgfiles().setPeso(pesoFiles/2);


				gossip.getNusers().setValor(avgUser/2);
				gossip.getNusers().setPeso(pesoUsers/2);

				gossip.getNnode().setValor(avgNode/2);
				gossip.getNnode().setPeso(pesoNode/2);
				
				gossip.getNactivU().setValor(avgAUsers/2);
				gossip.getNactivU().setPeso(pesoAUsers/2);
				
				//TODO
				
				System.out.println("New avg"+ gossip.getAvgfiles().getValor());
				System.out.println("New peso" +gossip.getAvgfiles().getPeso());

				System.out.println("New avg-" + gossip.getNusers().getValor());
				System.out.println("New peso-" + gossip.getNusers().getPeso());

				
				System.out.println("New avg-" + gossip.getNnode().getValor());
				System.out.println("New peso-" + gossip.getNnode().getPeso());
				
				gossipList.clear();
				lock.unlock();

				//Cria novo DTO para enviar de novo 

				dto = new GossipDTO(getPeerID());
				//TODO
				dto.setAvgfiles(gossip.getAvgfiles());
				dto.setNusers(gossip.getNusers());
				dto.setNnode(gossip.getNnode());
				dto.setNactivU(gossip.getNactivU());
				
				dto.setIterc(c+1);
				usr.SendOne(dto);

				Thread.sleep(ITERACTIME);
				System.out.println( "2º - for end");
			}

			if(changeGossipID == true){

				//usada estimativa anterior

				System.out.println( "Change gossip 2");
				System.out.println( "Estimativa usar anterior - " + estimativa);

				//setAvg(( getAvg() / getPesoN() ) + 20);
				changeGossipID = false;
				continue;
			}

//			gossipRound++;

			
			
			
			//TODO Actualiza estimativa
			
			float nNodes = 1/((gossip.getNnode().getValor()) / gossip.getNnode().getPeso());
			System.out.println( "Estimativa n nodes - " +nNodes);
			
			estimativa = (( gossip.getNusers().getValor()) / gossip.getNusers().getPeso())*nNodes;
			System.out.println( "Estimativa Sum - users - " + (estimativa));

			
			estimativa = ( gossip.getAvgfiles().getValor()) / gossip.getAvgfiles().getPeso() ;
			System.out.println( "Estimativa media avg-files - " + estimativa);

			
			estimativa = (gossip.getNactivU().getValor())/ gossip.getNactivU().getPeso() ;
			System.out.println( "Estimativa active users - " + 1/estimativa);
			
			
			System.out.println( "1º - for end");
			
			lock.lock();
			gossipList.clear();
			lock.unlock();
		}

		br.readLine();
		peer.shutdown();
	}


	private void SendOne(GossipDTO dto)
	{
		RequestP2PConfiguration requestP2PConfiguration = new RequestP2PConfiguration( 1, 10, 0 );
		Random RND = new Random();

		//TOOD n ta a dar...
		while(peer.getPeerBean().getStorage().findPeerIDForResponsibleContent(Number160.createHash(RND.nextInt()))== peer.getPeerID()){
			RND = new Random();
		}

		FutureDHT futureDHT = peer.send(Number160.createHash(RND.nextInt())) 
				.setObject(dto).setRequestP2PConfiguration(requestP2PConfiguration).start();

		futureDHT.awaitUninterruptibly();
		for(Object object:futureDHT.getRawDirectData2().values())
		{
			if (object.getClass().equals(GossipDTO.class)){

				//TODO estou atrazado ..... posso adicionar apenas DTO á lista é que valor superior será detectado
				//TODO possivel soluçao com flag..mais segura.

				lock.lock();
				gossipList.add((GossipDTO) object);
				System.out.println("Recebi um estou atrazado/ no sou o mais alto");
				lock.unlock();
				continue;
			}
			System.out.println("got:"+ object);
		}
	}

}
