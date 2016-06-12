package regrasDeProducao;

import java.util.ArrayList;
import java.util.List;

public class Elemento implements Comparable<Elemento> {
	protected String elemento;
	protected boolean terminal;
	protected List<Elemento> primeiros = new ArrayList<Elemento>();
	protected List<Elemento> seguidores = new ArrayList<Elemento>();
	protected List<Elemento> sincronizadores = new ArrayList<Elemento>();

	public Elemento(String elemento, boolean terminal) {
		this.elemento = elemento;
		this.terminal = terminal;
	}

	public String getElemento() {
		return elemento;
	}

	public void setElemento(String elemento) {
		this.elemento = elemento;
	}

	public boolean isTerminal() {
		return terminal;
	}

	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}

	public List<Elemento> getPrimeiros() {
		return primeiros;
	}

	public void setPrimeiros(List<Elemento> primeiros) {
		this.primeiros = primeiros;
	}

	public List<Elemento> getSeguidores() {
		return seguidores;
	}

	public void setSeguidores(List<Elemento> seguidores) {
		this.seguidores = seguidores;
	}

	public List<Elemento> getSincronizadores() {
		return sincronizadores;
	}

	public void setSincronizadores(List<Elemento> sincronizadores) {
		this.sincronizadores = sincronizadores;
	}

	@Override
	public int compareTo(Elemento outro) {
		return elemento.compareTo(outro.getElemento());
	}
	
	@Override
	public boolean equals(Object obj) {
		Elemento outro = (Elemento) obj;
		return elemento.equals(outro.getElemento()) && terminal == outro.isTerminal();
	}
	
	@Override
	public String toString() {
		return terminal ? elemento : "<<" + elemento + ">>";
	}

}
