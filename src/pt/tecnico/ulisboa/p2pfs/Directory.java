package pt.tecnico.ulisboa.p2pfs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Directory implements Serializable {

	private String DirName;
	private List<String> DirList;	
	private List<String> FilesList;
	
	public Directory(String dirName) {
		super();
		DirName = dirName;
		DirList = new ArrayList<String>();
		FilesList = new  ArrayList<String>();
	}

	
	public String getDirName() {
		return DirName;
	}
	public void setDirName(String dirName) {
		DirName = dirName;
	}
	
	public List<String> getDirList() {
		return DirList;
	}
	public void setDirList(List<String> dirList) {
		DirList = dirList;
	}
	public List<String> getFilesList() {
		return FilesList;
	}
	public void setFilesList(List<String> filesList) {
		FilesList = filesList;
	}
	public void addFile(String fileName) {
		FilesList.add(fileName);
		
		
	}

	
}
