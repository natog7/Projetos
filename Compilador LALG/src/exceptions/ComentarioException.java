package exceptions;

/*
 * Representa uma exceção no comentário.
 */
@SuppressWarnings("serial")
public class ComentarioException extends AutomatoException {

	public ComentarioException() {
		
	}

	public ComentarioException(String message) {
		super(message);
	}

	public ComentarioException(Throwable cause) {
		super(cause);
	}

	public ComentarioException(String message, Throwable cause) {
		super(message, cause);
	}

}
