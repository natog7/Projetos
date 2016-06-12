package analisadores;

import java.util.HashMap;
import java.util.Map.Entry;

public class Token {
	protected HashMap<String, String> token = new HashMap<String, String>();
	protected int linha;
	protected int coluna;
	
	public Token() { }

	/**
	 * Construtor contendo o token com sua palavra e chave.
	 * @param token Token
	 */
	public Token(HashMap<String, String> token) {
		this.token = token;
	}

	/**
	 * COnstrutor contendo o token, a linha e a coluna em que ele se encontra.
	 * @param token Token
	 * @param linha Linha onde o token se encontra
	 * @param coluna Coluna onde o token se encontra
	 */
	public Token(HashMap<String, String> token, int linha, int coluna) {
		this.token = token;
		this.linha = linha;
		this.coluna = coluna;
	}

	public HashMap<String, String> getTokenMap() {
		return token;
	}
	
	public void setToken(HashMap<String, String> token) {
		this.token = token;
	}
	
	public int getLinha() {
		return linha;
	}
	
	public void setLinha(int linha) {
		this.linha = linha;
	}
	
	public int getColuna() {
		return coluna;
	}
	
	public void setColuna(int coluna) {
		this.coluna = coluna;
	}
	
	public String getPalavra() {
		for(Entry<String, String> tokenAtual : token.entrySet()) {
			return tokenAtual.getValue();
		}
		return "";
	}
	
	public String getToken() {
		for(Entry<String, String> tokenAtual : token.entrySet()) {
			return tokenAtual.getKey();
		}
		return "";
	}
	
	@Override
	public String toString() {
		String aux = "Linha: " + linha + " Coluna: " + coluna + " ";
		for(Entry<String, String> tokenAtual : token.entrySet()) {
			aux += "[ " + tokenAtual.getKey() + " = " + tokenAtual.getValue() + " ]";
		}
		return aux;
	}
}
