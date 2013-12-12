package pt.ulisboa.tecnico.p2pfs;

public class StatAggregator{
	
	private double nFiles;
	private double nMBFiles;
	private double nMBMeta;

	public void setMBMeta(double mb) {
		this.nMBMeta = mb;
	}
//	public void remMBMeta(double mb){
//		nMBMeta -= mb;
//	}
	public StatAggregator(){
		nFiles = 0;
		nMBFiles = 0;
		nMBMeta = 0;
	}
	public void setNumFile(double part){
		nFiles = part;
	}
//	public void remFile(double part){
//		nFiles -= part;
//	}
	public void setMBFiles(double mb){
		nMBFiles = mb;
	}
//	public void remMBFiles(double mb){	
//		nMBFiles -= mb;
//	}
	@SuppressWarnings("unused")
	public double getNumStoredFiles(){
		return nFiles;
	}
	@SuppressWarnings("unused")
	public double getNumMBFiles(){	
		return nMBFiles;
	}
	@SuppressWarnings("unused")
	public double getNumMBMeta() {
		return nMBMeta;
	}
	public String toString(){
		return "nFiles: " + nFiles + "\n nMBFIles: " + nMBFiles + "\n nMBMeta: " + nMBMeta + "\n"; 
	}
}
