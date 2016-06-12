package exceptions;

/*
 * Representa um erro que � lan�ado quando n�o existe um tokem que define a palavra.
 */
@SuppressWarnings("serial")
public class TokenInexistenteException extends RuntimeException {

	public TokenInexistenteException() {
	}

	public TokenInexistenteException(String message) {
		super(message);
	}

	public TokenInexistenteException(Throwable cause) {
		super(cause);
	}

	public TokenInexistenteException(String message, Throwable cause) {
		super(message, cause);
	}

}
