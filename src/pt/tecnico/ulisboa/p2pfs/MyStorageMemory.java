package pt.tecnico.ulisboa.p2pfs;

import java.io.IOException;

import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageMemory;


public class MyStorageMemory extends StorageMemory{
	
	private final StatAggregator stat = new StatAggregator();
	
	@Override
	public boolean put(Number160 locationKey, Number160 domainKey, Number160 contentKey, Data value){
		boolean ret = super.put(locationKey, domainKey, contentKey, value);
	
		try {
			if(value.getObject() instanceof MetaStub){
				
				stat.addMB(value.getData().length * 1000 * 1000); //Byte to MB... and not Byte to Megabibyte
				
			}else{
			if(value.getObject() instanceof FileStub){
			
				stat.addMB(value.getData().length * 1000 * 1000);
				stat.addFile(1); //TODO
				
			}else{
				System.out.println("@MyStorageMemory: ERRO FICHEIRO RECEBIDO NAO E META NEM FILE");
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
		
		
		return ret;
	}

	public int getNumberOfRootMetaFilesImResponsibleFor(){
		
		return 0;
	}
	
	private class FileStub{}
	private class MetaStub{}
	
	
	private class StatAggregator{
		
		private double nFiles;
		private double nMB;
		
		public StatAggregator(){
			nFiles = 0;
			nMB = 0;
		}
		
		public void addFile(double part){
			
			nFiles += part;
		}
		
		public void remFile(double part){
			
			nFiles -= part;
		}
		
		public void addMB(double mb){
			
			nMB += mb;
		}
		
		public void remMB(double mb){
			
			nMB -= mb;
		}
		
		public double getNumStoredFiles(){
			
			return nFiles;
		}
		
		public double getNumMegaBytes(){
			
			return nMB;
		}
	}
}
