package automatos;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import exceptions.AutomatoException;
import regrasDeProducao.Elemento;

public class GrafoSintatico {
	protected Path caminho;
	protected Elemento elementoInicial;
	protected HashMap<String, Elemento> alfabeto = new HashMap<String, Elemento>();
	protected List<List<String>> transicoes = new ArrayList<List<String>>();
	protected List<List<String>> mensagens = new ArrayList<List<String>>();
	protected List<List<String>> erros = new ArrayList<List<String>>();
	protected Elemento elementoAtual;

	public GrafoSintatico(String caminhoAutomato) {
        caminho = Paths.get(caminhoAutomato);
        lerAutomato();
	}

	public GrafoSintatico(Path caminhoAutomato) {
        caminho = caminhoAutomato;
        lerAutomato();
	}
	
	/**
     * Transita com o símbolo para o próximo estado caso esta transição exista.
     * @param estado Estado atual.
     * @param simbolo Simbolo lido que fará a transição.
     * @return Retorna o novo estado para o qual foi transitado.
     */
    protected Elemento transicao(Elemento elementoAtual, String simboloAtual) {
        Elemento proxElemento = alfabeto.get(simboloAtual);
        boolean possuiTransicao = false;
        if(proxElemento == null) {
        	throw new IllegalArgumentException("Caractere não pertence ao alfabeto.");
        }
        for(List<String> lista : transicoes) {
        	if(lista.get(0).equals(elementoAtual.getElemento()) && lista.get(1).equals(simboloAtual)) {
        		possuiTransicao = true;
        		break;
        	}
        }
        if(!possuiTransicao) {
        	throw new IllegalArgumentException("Transição inválida.");
        }
        return proxElemento;
    }
	
    /**
     * Método que inicializa o processo de processamento da cadeia.
     */
    protected void iniciarProcessoCadeia() {
        elementoAtual = elementoInicial;
    }

    /**
     * Processa a cadeia, retornando o resultado do processamento.
     * @param cadeia Cadeia a ser processada.
     * @return Resultado do processamento.
     * @throws AutomatoException Caso acontessa algum erro no processamento.
     */
    public ResultadoAutomato processarCadeia(String cadeia) throws AutomatoException {
    	iniciarProcessoCadeia();
        int i = 0;
        char c = cadeia.charAt(i);
        boolean rejeitar = false;
        while (i < cadeia.length()) {
            c = cadeia.charAt(i++);
            try {
            	elementoAtual = transicao(elementoAtual, String.valueOf(c));
            }
            catch (IllegalArgumentException ex) {
            	//System.out.println(ex.getMessage());
                rejeitar = true;
                break;
            }
        }
        boolean resultado = false;
        String mensagem = "";
        String erro = "";
        /*try {
        	mensagem = getMensagem(estadoAtual);
        }
        catch(MensagemInexistenteException ex) { }
        if (estadoFinal(estadoAtual) && !rejeitar) {
        	resultado = true;
        }
        else {
			resultado = false;
	        try {
	        	erro = getErro(estadoAtual);
	        }
	        catch(MensagemInexistenteException ex) { }
        }*/
        return new ResultadoAutomato(resultado, mensagem, erro);
    }
    
    public void calcularPrimeiros() {
    	for(Elemento elem : alfabeto.values()) {
    		if(!elem.isTerminal()) {
    			elem.setPrimeiros(primeiro(elem));
    		}
    	}
    }

	public List<Elemento> primeiro(Elemento simboloInicial) {
		if(simboloInicial.isTerminal()) {
			throw new IllegalArgumentException("O símbolo deve ser um não-terminal.");
		}
        if(!alfabeto.containsKey(simboloInicial.getElemento())) {
        	throw new IllegalArgumentException("Caractere não pertence ao alfabeto.");
        }
		return primeiro(new ArrayList<Elemento>(), simboloInicial);
	}
	
	public List<Elemento> primeiro(List<Elemento> simbolos, Elemento simboloAtual) {
        for(List<String> lista : transicoes) {
        	if(lista.get(0).equals(simboloAtual.getElemento())) {
        		Elemento elemento = alfabeto.get(lista.get(1));
        		if(elemento.isTerminal()) {
        			simbolos.add(elemento);
        		}
        		else {
        			primeiro(simbolos, elemento);
        		}
        	}
        }
		return simbolos;
	}
    
    /*public void calcularSeguidores() {
    	for(Elemento elem : alfabeto.values()) {
    		if(!elem.isTerminal()) {
    			elem.setSeguidores(primeiro(elem));
    		}
    	}
    }
	
	public List<String> seguidor(Elemento simboloInicial) {
		if(simboloInicial.isTerminal()) {
			throw new IllegalArgumentException("O símbolo deve ser um não-terminal.");
		}
        if(!alfabeto.containsKey(simboloInicial.getElemento())) {
        	throw new IllegalArgumentException("Caractere não pertence ao alfabeto.");
        }
		return seguidor(new ArrayList<Elemento>(), simboloInicial);
	}*/
	
	/*public List<String> seguidor(List<Elemento> simbolos, Elemento simboloAtual) {
		for(int i = 0; i < simbolos.size(); i++) {
			List<List<String>> transicoes = getPossiveisTransicoes(simbolos.get(i).getElemento());
			for(int j = 0; j < transicoes.size(); j++) {
				boolean estadoFinal = estadoFinal(transicoes.get(j).get(2));
				Elemento elementoAux = new Elemento(transicoes.get(j).get(2), estadoFinal);
				seguidor(simbolos, elementoAux);
				if(estadoFinal) {
					if(!simbolos.contains(elementoAux)) {
						simbolos.add(elementoAux);
					}
				}
			}
			
		}
		return null;
	}*/

    /**
     * Método responsável por realizar a leitura do grafo sintático no modelo <i>.txt</i>, reconhecendo o elemento não-terminal inicial na primeira linha, 
     * os elementos não-terminais na segunda linha, elementos terminais na terceira linha, parte de mensagens de erros, tipos de 
     * mensagens e as possíveis transições entre os elementos para cada possível símbolo, caractere ou número lido.
     * <br>
     * <br><b>Estrutura</b>:<br>
     * <br>alfabeto (com exceção do elemento inicial)
     * <br>elemento inicial
     * <br>elementos terminais
     * <br>ERRO
     * <br>elemento_erro msg_erro
     * <br>(elemento_erro: estado em que será enviado um erro caso o autômato rejeite a cadeia estando nele)
     * <br>ERRO
     * <br>MSG
     * <br>elemento_msg msg
     * <br>(elemento_msg: estado em que será enviado uma mensagem caso o autômato aceite ou rejeite a cadeia estando nele)
     * <br>MSG
     * <br>transições
     * <br>simbolo elementoAtual proximoElemento
     * <br>
     * <br><b>Exemplo</b>:<br>
     * <br>S
     * <br>A B
     * <br>a b
     * <br>ERRO
     * <br>A a_ou_b_necessario
     * <br>ERRO
     * <br>MSG
     * <br>a Elemento_a_alcancado
     * <br>MSG
     * <br>S A
     * <br>A B
     * <br>A a
     * <br>B b
     * <br>
     * <br>Cadeias Aceitas: a, b.
     * <br>Cadeias Rejeitadas: aa, bb, ab, ba.
     */
    protected void lerAutomato() {
    	boolean lendoErros = false;
    	boolean lendoMensagens = false;
    	int qtdErros = 0;
    	int qtdMensagens = 0;
    	int qtdLinhasAdicionais = 0;
		try (BufferedReader reader = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
		    String linha = null;
		    for (int i = 0; (linha = reader.readLine()) != null; i++) {
		    	String[] aux = linha.split(" ");
	            switch (i) { 
	                case 0:
	                	elementoInicial = new Elemento(aux[0], false);
						this.alfabeto.put(aux[0], elementoInicial);
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
	                	if(aux[0].equals("ERRO")) {
	                		lendoErros = !lendoErros;
	                		qtdLinhasAdicionais++;
	                		continue;
	                	}
	                	if(aux[0].equals("MSG")) {
	                		lendoMensagens = !lendoMensagens;
	                		qtdLinhasAdicionais++;
	                		continue;
	                	}
	                	if(lendoErros) {
	                		erros.add(new ArrayList<String>());
		                    for (int j = 0; j < aux.length; j++) {
		                    	erros.get(qtdErros).add(aux[j]);
		                    }
		                    qtdErros++;
	                	}
	                	else if(lendoMensagens) {
	                		mensagens.add(new ArrayList<String>());
		                    for (int j = 0; j < aux.length; j++) {
		                    	mensagens.get(qtdMensagens).add(aux[j]);
		                    }
		                    qtdMensagens++;
	                	}
	                	else {
		                    transicoes.add(new ArrayList<String>());
		                    for (int j = 0; j < aux.length; j++) {
		                    	transicoes.get(i - 3 - qtdErros - qtdMensagens - qtdLinhasAdicionais).add(aux[j].trim());
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
}
