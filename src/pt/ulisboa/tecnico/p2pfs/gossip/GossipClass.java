package pt.ulisboa.tecnico.p2pfs.gossip;

import java.io.Serializable;

public class GossipClass  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public float valor;
	public float peso;
		
	public GossipClass(float avg, float peso) {
		super();
		this.valor = avg;
		this.peso = peso;
	}

	public float getValor() { 
		return valor;
	}

	public void setValor(float avgFiles) {
		this.valor = avgFiles;
	}

	public float getPeso() {
		return peso;
	}

	public void setPeso(float peso) {
		this.peso = peso;
	}
	
	
}
