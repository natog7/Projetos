package regrasDeProducao;

import java.util.List;

public class RegraDeProducao {
	protected Elemento produtor;
	protected List<Elemento> producao;
	
	public RegraDeProducao() {
		
	}
	
	public RegraDeProducao(Elemento produtor) {
		this.produtor = produtor;
	}
	
	public RegraDeProducao(Elemento produtor, List<Elemento> producao) {
		this.produtor = produtor;
		this.producao = producao;
	}

	public Elemento getProdutor() {
		return produtor;
	}

	public void setProdutor(Elemento produtor) {
		this.produtor = produtor;
	}

	public List<Elemento> getProducao() {
		return producao;
	}

	/**
	 * Verifica se o produtor é igual a outro produtor.
	 * @param outroProdutor Outro produtor.
	 * @return Se são iguais.
	 */
	public boolean produtorIgual(String outroProdutor) {
		return produtor.equals(outroProdutor);
	}
	
	@Override
	public boolean equals(Object obj) {
		RegraDeProducao outro = (RegraDeProducao) obj;
		return produtor.equals(outro.getProdutor()) && producao.equals(outro.getProducao());
	}
	
	@Override
	public String toString() {
		String aux = "[" + produtor + " -> ";
		for(Elemento elem : producao) {
			aux += elem + ", ";
		}
		aux = aux.substring(0, aux.length() - 2);
		aux += "]";
		return aux;
	}
}
