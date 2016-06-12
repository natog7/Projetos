package exceptions;

import analisadores.Token;

@SuppressWarnings("serial")
public class AnaliseSintaticaException extends AnaliseLexicaException {
	
	public AnaliseSintaticaException() {
		super();
	}
	
	public AnaliseSintaticaException(String message) {
		super(message);
	}
	
	public AnaliseSintaticaException(String message, String palavra, int linha, int coluna) {
		super(message);
		this.palavra = palavra;
		this.linha = linha;
		this.coluna = coluna;
	}
	
	public AnaliseSintaticaException(String message, Token token) {
		super(message);
		this.palavra = token.getPalavra();
		this.linha = token.getLinha();
		this.coluna = token.getColuna();
	}
	
	public AnaliseSintaticaException(Throwable cause) {
		super(cause);
	}
	
	public AnaliseSintaticaException(String message, Throwable cause) {
		super(message, cause);
	}

}
