package exceptions;

/*
 * Representa um erro que é lançado quando uma mensagem ou uma mensagem de erro não existe naquele estado do autômato.
 */
@SuppressWarnings("serial")
public class MensagemInexistenteException extends AutomatoException {
	
	public MensagemInexistenteException() {
		super();
	}
	
	public MensagemInexistenteException(String message) {
		super(message);
	}
	
	public MensagemInexistenteException(Throwable cause) {
		super(cause);
	}
	
	public MensagemInexistenteException(String message, Throwable cause) {
		super(message, cause);
	}
}