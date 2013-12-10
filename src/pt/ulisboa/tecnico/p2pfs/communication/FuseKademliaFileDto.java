package pt.ulisboa.tecnico.p2pfs.communication;

import java.io.Serializable;

public class FuseKademliaFileDto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int partNumber;
	
	private int totalNumberParts;
	
	private String content;
	
	public FuseKademliaFileDto() {
		this.partNumber = 0;
		this.totalNumberParts = 0;
		this.content = "";
	}

	public FuseKademliaFileDto(int partNumber, int totalNumberParts,
			String content) {
		this.partNumber = partNumber;
		this.totalNumberParts = totalNumberParts;
		this.content = content;
	}

	public int getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}

	public int getTotalNumberParts() {
		return totalNumberParts;
	}

	public void setTotalNumberParts(int totalNumberParts) {
		this.totalNumberParts = totalNumberParts;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
