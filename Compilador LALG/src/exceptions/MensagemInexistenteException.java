package exceptions;

/*
 * Representa um erro que � lan�ado quando uma mensagem ou uma mensagem de erro n�o existe naquele estado do aut�mato.
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