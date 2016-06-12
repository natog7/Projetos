package exceptions;

@SuppressWarnings("serial")
public class FalhaAoRetomarAnaliseException extends RuntimeException {
	
		public FalhaAoRetomarAnaliseException() {
			super();
		}
		
		public FalhaAoRetomarAnaliseException(String message) {
			super(message);
		}
		
		public FalhaAoRetomarAnaliseException(Throwable cause) {
			super(cause);
		}
		
		public FalhaAoRetomarAnaliseException(String message, Throwable cause) {
			super(message, cause);
		}
}
