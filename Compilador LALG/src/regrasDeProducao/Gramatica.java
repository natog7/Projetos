package regrasDeProducao;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Gramatica {
	protected Path caminho;
	protected HashMap<String, Elemento> alfabeto = new HashMap<String, Elemento>();
	protected List<RegraDeProducao> regrasDeProducao = new ArrayList<RegraDeProducao>();
	protected Elemento simboloInicial;

	public Gramatica(Path caminho) {
		this.caminho = caminho;
		lerArquivo();
	}

	public Gramatica(String simboloInicial) {
		this.regrasDeProducao = new ArrayList<RegraDeProducao>();
		this.simboloInicial = new Elemento(simboloInicial, false);
	}

	public Gramatica(String simboloInicial, List<RegraDeProducao> regrasDeProducao) {
		this.regrasDeProducao = regrasDeProducao;
		this.simboloInicial = new Elemento(simboloInicial, false);
	}

	public List<RegraDeProducao> getRegrasDeProducao() {
		return regrasDeProducao;
	}

	/**
	 * Pega o produtor do determinado s�mbolo.
	 * <br>Joga uma IllegalArgumentException caso o s�mbolo n�o perten�a ao alfabeto.
	 * <br>Joga uma IndexOutOfBoundsException caso o s�mbolo n�o possua um produtor.
	 * @param simbolo S�mbolo.
	 * @return Produtor.
	 */
	public Elemento getProdutor(String simbolo) {
        if(!alfabeto.containsKey(simbolo)) {
            throw new IllegalArgumentException("S�mbolo [" + simbolo + "] n�o pertence ao alfabeto.");
        }
        for(RegraDeProducao regra : regrasDeProducao) {
    		for(Elemento elem : regra.getProducao()) {
    			if(elem.getElemento().equals(simbolo)) {
    				return regra.getProdutor();
    			}
    		}
        }
        throw new IndexOutOfBoundsException("S�mbolo [" + simbolo + "] n�o possui um produtor.");
	}

	/**
	 * Adiciona novos elementos para uma determinada lista de elementos.
	 * @param simbolos Lista de elementos.
	 * @param novosSimbolos Novos elementos a serem adicionados.
	 */
	protected void adicionarSimbolos(List<Elemento> simbolos, Elemento... novosSimbolos) {
		for(Elemento novoSimbolo : novosSimbolos) {
			if(!simbolos.contains(novoSimbolo)) {
				simbolos.add(novoSimbolo);
			}
		}
	}

	/**
	 * Adiciona novos elementos para uma determinada lista de elementos.
	 * @param simbolos Lista de elementos.
	 * @param novosSimbolos Novos elementos a serem adicionados.
	 */
	protected void adicionarSimbolos(List<Elemento> simbolos, List<Elemento> novosSimbolos) {
		for(Elemento novoSimbolo : novosSimbolos) {
			if(!simbolos.contains(novoSimbolo)) {
				simbolos.add(novoSimbolo);
			}
		}
	}

	/**
	 * Calcula os primeiros de todos os s�mbolos pertencentes ao alfabeto.
	 */
	public void calcularPrimeiros() {
    	for(Elemento elem : alfabeto.values()) {
    		elem.setPrimeiros(primeiro(elem));
    	}
    }
	
	/**
	 * Calcula o conjunto primeiro do s�mbolo.
	 * @param simboloInicial S�mbolo.
	 * @return Conjunto primeiro.
	 */
	public List<Elemento> primeiro(Elemento simboloInicial) {
        if(!alfabeto.containsKey(simboloInicial.getElemento())) {
        	throw new IllegalArgumentException("S�mbolo [" + simboloInicial + "] n�o pertence ao alfabeto.");
        }
		return primeiro(new ArrayList<Elemento>(), simboloInicial);
	}
	
	/**
	 * Calcula o conjunto primeiro do s�mbolo.
	 * @param simbolos Conjunto primeiro atual.
	 * @param simboloAtual S�mbolo sendo percorrido.
	 * @return Conjunto primeiro.
	 */
	protected List<Elemento> primeiro(List<Elemento> simbolos, Elemento simboloAtual) {
		if(simboloAtual.isTerminal()) {
			simbolos.add(simboloAtual);
			return simbolos;
		}
        for(RegraDeProducao regra : regrasDeProducao) {
        	if(regra.getProdutor().equals(simboloAtual)) {
        		List<Elemento> primeiros = new ArrayList<Elemento>();
        		boolean simbolosAdicionados = false;
        		for(Elemento elemento : regra.getProducao()) {
	        		if(elemento.isTerminal()) {
	        			adicionarSimbolos(simbolos, elemento);
	        			adicionarSimbolos(simbolos, primeiros);
        				simbolosAdicionados = true;
	        			break;
	        		}
	        		else {
	        			List<Elemento> ultimosPrimeirosAnalisados = primeiro(new ArrayList<Elemento>(), elemento);
	        			adicionarSimbolos(primeiros, ultimosPrimeirosAnalisados);
	        			if(!ultimosPrimeirosAnalisados.contains(new Elemento("VAZIO", true))) {
	        				primeiros.remove(new Elemento("VAZIO", true));
		        			adicionarSimbolos(simbolos, primeiros);
	        				simbolosAdicionados = true;
	        				break;
	        			}
	        		}
        		}
        		if(!simbolosAdicionados) {
        			adicionarSimbolos(simbolos, primeiros);
        			simbolosAdicionados = true;
        		}
        	}
        }
		return simbolos;
	}
    
	/**
	 * Calcula os primeiros de todos os seguidores pertencentes ao alfabeto.
	 */
    public void calcularSeguidores() {
    	for(Elemento elem : alfabeto.values()) {
    		//if(!elem.isTerminal()) {
    			elem.setSeguidores(seguidor(elem));
    		//}
    	}
    }

	/**
	 * Calcula o conjunto seguidor do s�mbolo.
	 * @param simboloInicial S�mbolo.
	 * @return Conjunto seguidor.
	 */
	public List<Elemento> seguidor(Elemento simboloInicial) {
		/*if(simboloInicial.isTerminal()) {
			throw new IllegalArgumentException("O s�mbolo deve ser um n�o-terminal.");
		}*/
        if(!alfabeto.containsKey(simboloInicial.getElemento())) {
        	throw new IllegalArgumentException("S�mbolo [" + simboloInicial + "] n�o pertence ao alfabeto.");
        }
		return seguidor(new ArrayList<Elemento>(), simboloInicial, null);
	}
	
	/**
	 * Calcula o conjunto seguidor do s�mbolo.
	 * @param simboloInicial S�mbolo.
	 * @param ultimoSimboloCalculado �ltimo s�mbolo que teve seu conjunto seguidor calculado.
	 * @return Conjunto seguidor.
	 */
	public List<Elemento> seguidor(Elemento simboloInicial, Elemento ultimoSimboloCalculado) {
		/*if(simboloInicial.isTerminal()) {
			throw new IllegalArgumentException("O s�mbolo deve ser um n�o-terminal.");
		}*/
        if(!alfabeto.containsKey(simboloInicial.getElemento())) {
        	throw new IllegalArgumentException("S�mbolo [" + simboloInicial + "] n�o pertence ao alfabeto.");
        }
		return seguidor(new ArrayList<Elemento>(), simboloInicial, ultimoSimboloCalculado);
	}
	
	/**
	 * Calcula o conjunto seguidor do s�mbolo.
	 * @param simbolos Conjunto seguidor atual.
	 * @param simboloAtual S�mbolo sendo percorrido.
	 * @param ultimoSimboloCalculado �ltimo s�mbolo que teve seu conjunto seguidor calculado.
	 * @return Conjunto seguidor.
	 */
	protected List<Elemento> seguidor(List<Elemento> simbolos, Elemento simboloAtual, Elemento ultimoSimboloCalculado) {
    	if(simboloAtual.equals(simboloInicial)) {
    		Elemento aux = new Elemento("$", true);
    		if(!simbolos.contains(aux)) {
    			simbolos.add(aux);
    		}
    	}
        for(RegraDeProducao regra : regrasDeProducao) {
        	if(regra.getProducao().contains(simboloAtual)) {
        		for(int i = 0; i < regra.getProducao().size(); i++) {
        			if(regra.getProducao().get(i).equals(simboloAtual)) {
        				if(i == (regra.getProducao().size() - 1)) {
                    		Elemento elemento = regra.getProducao().get(i);
        					if(!elemento.isTerminal() && !elemento.equals(regra.getProdutor())) {
        						List<Elemento> seguidores = null;
        						try {
	        						if(!ultimoSimboloCalculado.equals(regra.getProdutor())) {
	        							seguidores = seguidor(regra.getProdutor(), simboloAtual);
	        						}
	        						else {
	        							seguidores = new ArrayList<Elemento>();
	        						}
        						}
        						catch(NullPointerException ex) {
        							seguidores = seguidor(regra.getProdutor(), simboloAtual);
        						}
        	        			adicionarSimbolos(simbolos, seguidores);
        					}
        					// Pegar seguidores do pai caso seja um terminal e n�o tenha nenhum seguidor direto (um simbolo a direita)
        					else if(elemento.isTerminal()) {
        						adicionarSimbolos(simbolos, seguidor(regra.getProdutor()));
        					}
        				}
        				else {
	                		Elemento elemento = regra.getProducao().get(i + 1);
	                		boolean possuiVazio = false;
	                		if(elemento.isTerminal()) {
	                    		if(!simbolos.contains(elemento)) {
		                			simbolos.add(elemento);
		                			possuiVazio = elemento.getElemento().equalsIgnoreCase("VAZIO");
	                    		}
	                		}
	                		else {
	                			List<Elemento> primeiros = primeiro(elemento);
	                			for(Elemento primeiro : primeiros) {
                					adicionarSimbolos(simbolos, primeiro);
	                				if(primeiro.getElemento().equalsIgnoreCase("VAZIO")) {
	                					possuiVazio = true;
	                				}
	                			}
	                		}
	                		if(possuiVazio) {
        						List<Elemento> seguidores = null;
        						try {
	        						if(!ultimoSimboloCalculado.equals(regra.getProdutor())) {
	    	                			seguidores = seguidor(regra.getProdutor(), simboloAtual);
	        						}
	        						else {
	        							seguidores = new ArrayList<Elemento>();
	        						}
        						}
        						catch(NullPointerException ex) {
        							seguidores = seguidor(regra.getProdutor(), simboloAtual);
        						}
        	        			adicionarSimbolos(simbolos, seguidores);
	                		}
    	        			while(simbolos.contains(new Elemento("VAZIO", true))) {
    	        				simbolos.remove(new Elemento("VAZIO", true));
    	        			}
        				}
        			}
        		}
        	}
        }
		return simbolos;
	}
	
	/*public void processarCadeia(String cadeia) {
		for(int i = 0; i < cadeia.length(); i++) {
			simboloAtual = cadeia.substring(i, i + 1);
			regraAtual = processarProxRegra();
		}
	}
	
	protected RegraDeProducao processarProxRegra() {
		RegraDeProducao proxRegra = null;
		if(regraAtual == null) {
			for(RegraDeProducao regra : regrasDeProducao) {
				if(regra.produtorIgual(simboloInicial.getElemento())) {
					proxRegra = regra;
					break;
				}
			}
		}
		else {
			
		}
		return proxRegra;
	}
	
	protected void processarRegra(RegraDeProducao regra, String simboloAtual) {
		if(this.simboloAtual.equals(simboloAtual)) {
			return;
		}
		for(int i = 0; i < regra.getProducao().size(); i++) {
			boolean possuiNaoTerminais = false;
			boolean simboloIgual = false;
			for(int j = 0; j < regra.getProducao().get(i).size(); j++) {
				Elemento aux = regra.getProducao().get(i).get(j);
				if(aux.isTerminal()) {
					if(aux.getElemento().equals(simboloAtual)) {
						simboloIgual = true;
					}
				}
				else {
					possuiNaoTerminais = true;
				}
			}
			if(!possuiNaoTerminais && simboloIgual) {
				return;
			}
			else if(possuiNaoTerminais) {
				
			}
		}
	}
	
	protected RegraDeProducao pegarRegra(Elemento naoTerminal) {
		if(naoTerminal.isTerminal()) {
			throw new IllegalArgumentException("O elemento deve ser um n�o terminal!");
		}
		for(int i = 0; i < regrasDeProducao.size(); i++) {
			for(int j = 0; j < regrasDeProducao.get(i).getProducao().size(); j++) {
				for(int k = 0; k < regrasDeProducao.get(i).getProducao().get(j).size(); k++) {
					if(regrasDeProducao.get(i).getProducao().get(j).contains(naoTerminal)) {
						return regrasDeProducao.get(i);
					}
				}
			}
		}
		throw new IndexOutOfBoundsException("N�o existe uma regra de produ��o com este elemento!");
	}*/
	
	/**
     * M�todo respons�vel por realizar a leitura da produ��o no modelo <i>.txt</i>, reconhecendo o elemento n�o-terminal inicial na primeira linha, 
     * os elementos n�o-terminais na segunda linha, elementos terminais na terceira linha, as regras de produ��o nas pr�ximas linhas e 
     * os elementos de sincroniza��o.
     * <br>
     * <br><b>Estrutura</b>:<br>
     * <br>elemento n�o-terminal inicial
     * <br>elementos n�o-terminais
     * <br>elementos terminais
     * <br>regras de produ��o
     * <br>elemento_n�o-terminal elemento1, elemento2, elemento3, ... 
     * <br>SYNC (essa parte de sincroniza��o � opcional)
     * <br>elemento_a_ser_sincronizado sincronizador1, sincronizador2, sincronizador3, ...
     * <br>SYNC
     * <br>
     * <br><b>Exemplo</b>:<br>
     * <br>S
     * <br>A B
     * <br>a b c vazio
     * <br>S A B
     * <br>A a
     * <br>A c
     * <br>B b
     * <br>B vazio
     * <br>SYNC
     * <br>A B
     * <br>SYNC
     * <br>
     * <br>Cadeias Aceitas: ab, cb, a, c.
     * <br>Cadeias Rejeitadas: aa, bb, ac, ba, vazio.
     */
    protected void lerArquivo() {
    	boolean lendoSimbolosDeSincronizacao = false;
		try (BufferedReader reader = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
		    String linha = null;
		    for (int i = 0; (linha = reader.readLine()) != null; i++) {
		    	String[] aux = linha.split(" ");
	            switch (i) { 
	                case 0:
	                	simboloInicial = new Elemento(aux[0], false);
						this.alfabeto.put(aux[0], simboloInicial);
	                    break;
	                case 1:
	                    for (String elem : aux) {
	                        alfabeto.put(elem.trim(), new Elemento(elem.trim(), false));
	                    }
	                    break;
	                case 2:
	                    for (String elem : aux) {
	                        alfabeto.put(elem.trim(), new Elemento(elem.trim(), true));
	                    }
	                    break;
	                default:
	                    //transicoes.add(new ArrayList<String>());
	                	if(aux[0].equals("SYNC")) {
	                		lendoSimbolosDeSincronizacao = !lendoSimbolosDeSincronizacao;
	                		continue;
	                	}
	                	if(lendoSimbolosDeSincronizacao) {
	                		Elemento elem = null;
		                    for (int j = 0; j < aux.length; j++) {
		                    	switch(j) {
			                    	case 0:
			                    		elem = alfabeto.get(aux[j].trim());
			                    		break;
		                    		default:
		                    			elem.getSincronizadores().add(alfabeto.get(aux[j].trim()));
		                    			break;
		                    	}
		                    }
	                	}
	                	else {
		                	Elemento produtor = null;
		                	List<Elemento> producao = new ArrayList<Elemento>();
		                    for (int j = 0; j < aux.length; j++) {
		                    	switch(j) {
			                    	case 0:
			                    		produtor = alfabeto.get(aux[j].trim());
			                    		break;
		                    		default:
		                    			producao.add(alfabeto.get(aux[j].trim()));
		                    			break;
		                    	}
		                    }
		                    if(produtor != null && producao.size() > 0) {
		                    	regrasDeProducao.add(new RegraDeProducao(produtor, producao));
		                    }
	                	}
	                    break;
	            }
		    }
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		    x.printStackTrace();
		}
    }
    
	public HashMap<String, Elemento> getAlfabeto() {
		return alfabeto;
	}
    
    @Override
    public String toString() {
    	return "[" + alfabeto.values().toString() + "] " + "[" + simboloInicial + "] " + "[" + regrasDeProducao.toString() + "]";
    }
}
