package automatos;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import exceptions.AutomatoException;
import exceptions.MensagemInexistenteException;

public class Automato extends AutomatoBase {
	protected String estadoInicial = "0";
	protected List<String> estadosFinais = new ArrayList<String>();
	protected List<List<String>> transicoes = new ArrayList<List<String>>();
	protected List<List<String>> mensagens = new ArrayList<List<String>>();
	protected List<List<String>> erros = new ArrayList<List<String>>();
	protected List<String> alfabeto = new ArrayList<String>();
	protected String estadoAtual = "";

	/**
	 * Instancia o autômato a partir do caminho do arquivo em formato <i>txt</i> em que ele está contido.
	 * @param caminhoAutomato Caminho do arquivo do autômato.
	 */
    public Automato(String caminhoAutomato) {
        caminho = Paths.get(caminhoAutomato);
        lerAutomato();
    }

	/**
	 * Instancia o autômato a partir do caminho do arquivo em formato <i>txt</i> em que ele está contido.
	 * @param caminhoAutomato Caminho do arquivo do autômato.
	 */
    public Automato(Path caminhoAutomato) {
        caminho = caminhoAutomato;
        lerAutomato();
    }

    /**
     * Retorna o próximo caractere que será lido.
     * @param cadeia Cadeia que contém o caractere.
     * @param i Indice do próximo caractere.
     * @return Retorna o próximo caractere.
     */
    protected char proxCaractere(String cadeia, int i) {
        return cadeia.charAt(i);
    }

    /**
     * Transita com o símbolo para o próximo estado caso esta transição exista.
     * @param estado Estado atual.
     * @param simbolo Simbolo lido que fará a transição.
     * @return Retorna o novo estado para o qual foi transitado.
     */
    protected String transicao(String estado, String simbolo) {
        if (estado.equals("ERRO")) {
            throw new IllegalArgumentException("Erro.");
        }
        else if(!alfabeto.contains(simbolo)) {
        	throw new IllegalArgumentException("Caractere não pertence ao alfabeto.");
        }
        List<List<String>> transicoesValidas = new ArrayList<List<String>>();
        for(List<String> lista : transicoes) {
        	if(lista.get(1).equals(estado)) {
        		transicoesValidas.add(lista);
        	}
        }
        String proxEstado = "";
        for (int i = 0; i < transicoesValidas.size();  i++) {
            if(transicoesValidas.get(i).get(0).equals(simbolo)) {
                proxEstado = transicoesValidas.get(i).get(2);
                break;
            }
        }
        if (proxEstado.equals("ERRO")) {
            throw new IllegalArgumentException("Erro.");
        }
        else if(proxEstado.isEmpty()) {
        	throw new IllegalArgumentException("Transição inválida.");
        }
    	//System.out.println("Transição [" + c + "]: " + estado + " -> " + proxEstado);
        return proxEstado;
    }
    
    protected List<List<String>> getPossiveisTransicoes(String estado) {
        List<List<String>> transicoesValidas = new ArrayList<List<String>>();
        for(List<String> lista : transicoes) {
        	if(lista.get(1).equals(estado)) {
        		transicoesValidas.add(lista);
        	}
        }
        return transicoesValidas;
    }
    
    protected List<String> getPossiveisEstadosTransicoes(String estado) {
        List<String> transicoesValidas = new ArrayList<String>();
        for(List<String> lista : transicoes) {
        	if(lista.get(1).equals(estado)) {
        		transicoesValidas.add(lista.get(2));
        	}
        }
        return transicoesValidas;
    }

    /**
     * Verifica se o estado é um estado final.
     * @param estado Estado a ser verificado.
     * @return Se é ou não um estado final.
     */
    protected boolean estadoFinal(String estado) {
        return estadosFinais.contains(estado);
    }

    /**
     * Método que inicializa o processo de processamento da cadeia.
     */
    protected void iniciarProcessoCadeia() {
        estadoAtual = estadoInicial;
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
        char c = proxCaractere(cadeia, i);
        boolean rejeitar = false;
        while (i < cadeia.length()) {
            c = proxCaractere(cadeia, i++);
            try {
            	estadoAtual = transicao(estadoAtual, String.valueOf(c));
            }
            catch (IllegalArgumentException ex) {
            	//System.out.println(ex.getMessage());
                rejeitar = true;
                break;
            }
        }
        boolean resultado;
        String mensagem = "";
        String erro = "";
        try {
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
        }
        return new ResultadoAutomato(resultado, mensagem, erro);
    }
    
    /**
     * Pega a mensagem do estado caso exista uma. Se não houver uma mensagem contida nesse estado, é jogada uma exceção do tipo MensagemInexistenteException.
     * @param estado Estado que contém a mensagem
     * @return A mensagem do estado
     */
    public String getMensagem(String estado) {
        String mensagem = "";
        try {
			for(int j = 0; j < mensagens.size(); j++) {
				if(mensagens.get(j).get(0).equals(estado)) {
					mensagem = mensagens.get(j).get(1);
					break;
				}
			}
        }
        catch(IndexOutOfBoundsException ex) { }
        if(mensagem.isEmpty()) {
        	throw new MensagemInexistenteException();
        }
        return mensagem;
    }

    /**
     * Pega o erro do estado caso exista um. Se não houver um erro contido nesse estado, é jogada uma exceção do tipo MensagemInexistenteException.
     * @param estado Estado que contém o erro
     * @return O erro do estado
     */
    public String getErro(String estado) {
        String erro = "";
        try {
    		for(int j = 0; j < erros.size(); j++) {
    			if(erros.get(j).get(0).equals(estado)) {
    				erro = erros.get(j).get(1);
    				//throw new AutomatoErroException(erros.get(i).get(1));
    				break;
    			}
    		}
        }
        catch(IndexOutOfBoundsException ex) { }
        if(erro.isEmpty()) {
        	throw new MensagemInexistenteException();
        }
        return erro;
    }

    /**
     * Método responsável por realizar a leitura do automato no modelo <i>.txt</i>, reconhecendo o alfabeto na primeira linha, o 
     * estado inicial na segunda linha, estado(s) final(is) na terceira linha, parte de mensagens de erros, tipos de 
     * mensagens e as possíveis transições entre os estados para cada possível símbolo, caractere ou número lido.
     * <br>
     * <br><b>Estrutura</b>:<br>
     * <br>alfabeto
     * <br>estado inicial
     * <br>estados finais
     * <br>ERRO
     * <br>estado_erro msg_erro
     * <br>(estado_erro: estado em que será enviado um erro caso o autômato rejeite a cadeia estando nele)
     * <br>ERRO
     * <br>MSG
     * <br>estado_msg msg
     * <br>(estado_msg: estado em que será enviado uma mensagem caso o autômato aceite ou rejeite a cadeia estando nele)
     * <br>MSG
     * <br>transições
     * <br>simbolo estadoAtual proximoEstado
     * <br>
     * <br><b>Exemplo</b>:<br>
     * <br>0 1
     * <br>A
     * <br>B
     * <br>ERRO
     * <br>A Transicao_nao_reconhecida
     * <br>ERRO
     * <br>MSG
     * <br>B Estado_final_alcancado
     * <br>MSG
     * <br>0 A B
     * <br>1 A B
     * <br>
     * <br>Cadeias Aceitas: 0, 1.
     * <br>Cadeias Rejeitadas: 01, 10, 11, 00, 001, 002, 2, 021, 7.
     */
    protected void lerAutomato() {
    	boolean lendoErros = false;
    	boolean lendoMensagens = false;
    	int qtdErros = 0;
    	int qtdMensagens = 0;
    	int qtdLinhasAdicionais = 0;
    	estadosFinais = new ArrayList<String>();
    	transicoes = new ArrayList<List<String>>();
    	mensagens = new ArrayList<List<String>>();
    	erros = new ArrayList<List<String>>();
    	alfabeto = new ArrayList<String>();
		try (BufferedReader reader = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
		    String linha = null;
		    for (int i = 0; (linha = reader.readLine()) != null; i++) {
		    	String[] aux = linha.split(" ");
	            switch (i) { 
	                case 0:
	                    for (String elem : aux) {
	                        alfabeto.add(elem.trim());
	                    }
	                    break;
	                case 1:
	                	estadoInicial = aux[0];
	                    break;
	                case 2:
	                    for (String estFinal : aux) {
	                        if (!estFinal.equals("")) {
	                        	estadosFinais.add(estFinal.trim());
	                        }
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
    
    @Override
    public String toString() {
    	return "[" + alfabeto.toString() + "] " + "[" + estadoInicial + "] " + "[" + estadosFinais.toString() + "] " + "[" + transicoes.toString() + "]";
    }
}
