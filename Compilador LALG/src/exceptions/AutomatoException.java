package exceptions;

/*
 * Representa um erro no aut�mato.
 */
@SuppressWarnings("serial")
public class AutomatoException extends RuntimeException {
	
	public AutomatoException() {
		super();
	}
	
	public AutomatoException(String message) {
		super(message);
	}
	
	public AutomatoException(Throwable cause) {
		super(cause);
	}
	
	public AutomatoException(String message, Throwable cause) {
		super(message, cause);
	}
}
