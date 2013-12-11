package pt.ulisboa.tecnico.p2pfs;

import java.io.IOException;
import java.util.NavigableMap;

import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaDto;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaFileDto;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number480;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageMemory;


public class MyStorageMemory extends StorageMemory{
	
	private final StatAggregator stat = new StatAggregator();
	private Number160 myOwnerPeerId;
	
	public MyStorageMemory(Number160 myOwnerPeerId){
		super();
		this.myOwnerPeerId = myOwnerPeerId;
	}
	
	@Override
	public boolean put(Number160 locationKey, Number160 domainKey, Number160 contentKey, Data value){
		boolean ret = super.put(locationKey, domainKey, contentKey, value);
	
		try {
			if(value.getObject() instanceof FuseKademliaDto){
				
				stat.addMBMeta(value.getData().length * 1000 * 1000); //Byte to MB... and not Byte to Mebibyte
				
			}else{
			if(value.getObject() instanceof FuseKademliaFileDto){
			
				stat.addMBFiles(value.getData().length * 1000 * 1000);
				stat.addFile((double) 1 / ((FuseKademliaFileDto) value.getObject()).getTotalNumberParts());
				
			}else{
				System.out.println("@MyStorageMemory put: ERRO FICHEIRO RECEBIDO NAO E META NEM FILE");
			}}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	@Override
	public Data remove(Number160 locationKey, Number160 domainKey, Number160 contentKey){
		Data ret = super.remove(locationKey, domainKey, contentKey);
		
		try {
			if(ret.getObject() instanceof FuseKademliaDto){
				
				stat.remMBMeta(ret.getData().length * 1000 * 1000); //Byte to MB... and not Byte to Mebibyte
				
			}else{
			if(ret.getObject() instanceof FuseKademliaFileDto){
			
				stat.remMBFiles(ret.getData().length * 1000 * 1000);
				stat.remFile((double) 1 / ((FuseKademliaFileDto) ret.getObject()).getTotalNumberParts());
				
			}else{
				System.out.println("@MyStorageMemory remove: ERRO FICHEIRO RECEBIDO NAO E META NEM FILE");
			}}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}

	public int getNumberOfRootMetaFilesImResponsibleFor(){
		
		NavigableMap<Number480, Data> map = super.map();
		int rootMetasIOwn = 0;
		
		for(Number480 n : map.descendingKeySet()){
			Data d = map.get(n);
			try {
				if(d.getObject() instanceof FuseKademliaDto){
					
					if(((FuseKademliaDto) d.getObject()).isRoot()){
						
						if(this.myOwnerPeerId.compareTo(super.findPeerIDForResponsibleContent(n.getLocationKey())) == 0){
							rootMetasIOwn++;
						}
					}
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rootMetasIOwn;
	}
	
	public double getNumStoredFiles(){
		return stat.getNumStoredFiles();
	}
	public double getNumMBFiles(){
		return stat.getNumMBFiles();
	}
	public double getNumMBMeta(){
		return stat.getNumMBMeta();
	}
	public void printStats(){
		System.out.println(stat.toString());
	}
	private class StatAggregator{
		
		private double nFiles;
		private double nMBFiles;
		private double nMBMeta;

		public void addMBMeta(double mb) {
			this.nMBMeta += mb;
		}
		public void remMBMeta(double mb){
			nMBMeta -= mb;
		}
		public StatAggregator(){
			nFiles = 0;
			nMBFiles = 0;
			nMBMeta = 0;
		}
		public void addFile(double part){
			nFiles += part;
		}
		public void remFile(double part){
			nFiles -= part;
		}
		public void addMBFiles(double mb){
			nMBFiles += mb;
		}
		public void remMBFiles(double mb){	
			nMBFiles -= mb;
		}
		public double getNumStoredFiles(){
			return nFiles;
		}
		public double getNumMBFiles(){	
			return nMBFiles;
		}
		public double getNumMBMeta() {
			return nMBMeta;
		}
		public String toString(){
			return "nFiles: " + nFiles + "\n nMBFIles: " + nMBFiles + "\n nMBMeta: " + nMBMeta + "\n"; 
		}
	}
}
