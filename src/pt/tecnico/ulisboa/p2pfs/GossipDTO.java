package pt.tecnico.ulisboa.p2pfs;

import java.io.Serializable;

public class GossipDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public float valor;
	public float peso;
	public int nodeID;
	
	public GossipDTO(int nodeId,float avg, float peso) {
		super();
		this.nodeID = nodeId;
		this.valor = avg;
		this.peso = peso;
	}
	
	
	public float getValor() {
		return valor;
	}
	public void setValor(int valor) {
		this.valor = valor;
	}
	public float getPeso() {
		return peso;
	}
	public void setPeso(int peso) {
		this.peso = peso;
	}
	public int getNodeID() {
		return nodeID;
	}


	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}


}
