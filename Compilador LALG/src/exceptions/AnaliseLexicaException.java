package exceptions;

/*
 * Representa um erro na análise léxica, guardando a palavra que está errada, a linha e a
 * coluna em que ela se encontra no script.
 */
@SuppressWarnings("serial")
public class AnaliseLexicaException extends AutomatoException {
	protected String palavra;
	protected int linha;
	protected int coluna;
	
	public AnaliseLexicaException() {
		super();
	}
	
	public AnaliseLexicaException(String message) {
		super(message);
	}
	
	public AnaliseLexicaException(String message, String palavra, int linha, int coluna) {
		super(message);
		this.palavra = palavra;
		this.linha = linha;
		this.coluna = coluna;
	}
	
	public AnaliseLexicaException(Throwable cause) {
		super(cause);
	}
	
	public AnaliseLexicaException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public String getPalavra() {
		return palavra;
	}
	
	public void setPalavra(String palavra) {
		this.palavra = palavra;
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
}
