package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import analisadores.AnalisadorLexico;
import analisadores.AnalisadorSintatico;
import analisadores.Token;
import automatos.AutomatoNumero;
import regrasDeProducao.Gramatica;

public class Compilador {
	public static Compilador instance = new Compilador();
	private List<String> script = new ArrayList<String>();
	protected AnalisadorSintatico analisadorSintatico;
	protected HashMap<String, String> palavrasReservadas = new HashMap<String, String>();
	protected HashMap<String, String> tabelaSimbolos = new HashMap<String, String>();
	public static final String CAMINHO_AUTOMATO_PRINCIPAL = System.getProperty("user.dir") + File.separator + "automatos" + File.separator + "Aut�mato Final.txt";
	public static final String CAMINHO_REGRAS_DE_PRODUCAO = System.getProperty("user.dir") + File.separator + "automatos" + File.separator + "Regras de Produ��o.txt";
	public static final String CAMINHO_AUTOMATO_NUMERO = System.getProperty("user.dir") + File.separator + "automatos" + File.separator + "Aut�mato Numero.txt";
	public static final String CAMINHO_PALAVRAS_RESERVADAS = System.getProperty("user.dir") + File.separator + "automatos" + File.separator + "Tabela Palavras Reservadas.txt";
	public static final String CAMINHO_TABELA_SIMBOLOS = System.getProperty("user.dir") + File.separator + "automatos" + File.separator + "Tabela Simbolos.txt";
	public AutomatoNumero automatoNumero;
	protected boolean possuiAlgumErro = false;

	private Compilador() {
	}

	public static Compilador getInstance() {
		return instance;
	}

	/**
	 * M�todo respons�vel por iniciar o aut�mato, o analisador sint�tico, a tabela de palavras reservadas e a de s�mbolos.
	 */
	public void iniciar(){
		automatoNumero = new AutomatoNumero(CAMINHO_AUTOMATO_NUMERO);
		analisadorSintatico = new AnalisadorSintatico(new AnalisadorLexico(CAMINHO_AUTOMATO_PRINCIPAL, automatoNumero), Paths.get(CAMINHO_REGRAS_DE_PRODUCAO));
		palavrasReservadas = lerTabela(CAMINHO_PALAVRAS_RESERVADAS);
		tabelaSimbolos = lerTabela(CAMINHO_TABELA_SIMBOLOS);
	}

	/**
	 * Analisa lexicamente e sintaticamente o script, retornando as informa��es do programa compilado.
	 * @return Lista de tokens retornada pelo analisador l�xico.
	 */
	public List<Token> compilar() {
		return analisarSintaticamente();
	}

	/**
	 * Analisa lexicamente o script, retornando as informa��es dos tokens criados na an�lise l�xica.
	 * @return Lista de tokens retornada pelo analisador l�xico.
	 */
	public List<Token> analisarLexicamente() {
		analisadorSintatico.getTodosTokens();
		//return analisadorSintatico.getTokensInfo();
		possuiAlgumErro = analisadorSintatico.getAnalisadorLexico().isPossuiAlgumErro();
		return analisadorSintatico.getTokens();
	}

	/**
	 * Analisa lexicamente e sintaticamente o script, retornando as informa��es dos tokens criados na an�lise l�xica.
	 * @return Lista de tokens retornada pelo analisador l�xico.
	 */
	public List<Token> analisarSintaticamente() {
		analisadorSintatico.getTodosTokens();
		analisadorSintatico.analisar();
		possuiAlgumErro = analisadorSintatico.isPossuiAlgumErro() || analisadorSintatico.getAnalisadorLexico().isPossuiAlgumErro();
		return analisadorSintatico.getTokens();
	}
	
	/**
	 * Retorna a chave da palavra caso ela perten�a a tabela de palavras reservadas ou � tabela de s�mbolos
	 * @param palavra Palavra a ser verificada
	 * @return Retorna a chave da palavra
	 * @throws IndexOutOfBoundsException Se a palavra n�o pertence � tabela de palavras reservadas nem � tabela de s�mbolos
	 */
	public String getChaveNasPalavrasReservadasOuTabelaSimbolos(String palavra) throws IndexOutOfBoundsException {
		String key = "";
		key = palavrasReservadas.get(palavra);
		if(key == null) {
			key = tabelaSimbolos.get(palavra);
		}
		if(key == null) {
			throw new IndexOutOfBoundsException();
		}
		return key;
	}

	/**
	 * M�todo que verifica se a palavra pertence a tabela de s�mbolos.
	 * @param palavra Palavra a ser verificada.
	 * @return Se a palavra pertence ou n�o.
	 */
	public boolean pertenceTabelaSimbolos(String palavra) {
		String key = "";
		key = tabelaSimbolos.get(palavra);
		if(key == null) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * M�todo para limpar o script.
	 */
	public void limparScript() {
		script = new ArrayList<String>();
	}

	/**
	 * M�todo para carregar o arquivo em formato <i>txt</i> do script.
	 * @param caminho Caminho do arquivo que cont�m o script.
	 */
	public void carregarScript(String caminho) {
		carregarScript(Paths.get(caminho));
	}

	/**
	 * M�todo para carregar o arquivo em formato <i>txt</i> do script.
	 * @param caminho Caminho do arquivo que cont�m o script.
	 */
	public void carregarScript(Path caminho) {
		try {
			script = Files.readAllLines(caminho);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		    e.printStackTrace();
		}
		/*
		try (BufferedReader reader = Files.newBufferedReader(caminho)) {
		    String linha = null;
		    while ((linha = reader.readLine()) != null) {
		    	script.add(linha);
		    }
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}*/
	}

	/**
	 * L� a tabela armazenada em um arquivo em formato <i>txt</i>, criando um HashMap com os valores adquiridos.
	 * @param caminho Caminho do arquivo que cont�m a tabela.
	 * @return Retorna um HashMap com os valores adquiridos da tabela.
	 */
    protected HashMap<String, String> lerTabela(String caminho) {
    	HashMap<String, String> tabela = new HashMap<String, String>();
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(caminho))) {
		    String linha = null;
		    while ((linha = reader.readLine()) != null) {
		    	String[] aux = linha.split(" ");
		    	tabela.put(aux[0], aux[1]);
		    }
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		    x.printStackTrace();
		}
		return tabela;
    }
	
	public List<String> getScript() {
		return script;
	}
	
	public AnalisadorLexico getAnalisadorLexico() {
		return analisadorSintatico.getAnalisadorLexico();
	}
	
	public AnalisadorSintatico getAnalisadorSintatico() {
		return analisadorSintatico;
	}
	
	public boolean isPossuiAlgumErro() {
		return possuiAlgumErro;
	}

	public void main() {
	}
}
