package exceptions;

/*
 * Representa um erro que é lançado quando a leitura do script chegou a seu fim.
 */
@SuppressWarnings("serial")
public class FimDoScriptException extends RuntimeException {
	
	public FimDoScriptException() {
		super();
	}
	
	public FimDoScriptException(String message) {
		super(message);
	}
	
	public FimDoScriptException(Throwable cause) {
		super(cause);
	}
	
	public FimDoScriptException(String message, Throwable cause) {
		super(message, cause);
	}
}
