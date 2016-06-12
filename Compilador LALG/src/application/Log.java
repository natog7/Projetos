package application;

import java.time.LocalDateTime;

import exceptions.AnaliseLexicaException;
import exceptions.AnaliseSintaticaException;
import javafx.scene.control.TextArea;

public class Log extends LogSimples {
	protected TextArea errorsTextLog;

	/**
	 * Construtor para campo de texto, informar o tempo e limpar o log.
	 * @param textLog TextArea que mostrar� as mensagens e errors do log.
	 * @param errorsTextLog TextArea que mostrar� apenas as mensagens de erros do log.
	 */
	public Log(TextArea textLog, TextArea errorsTextLog) {
		super(textLog);
		this.errorsTextLog = errorsTextLog;
	}

	/**
	 * Construtor para campo de texto, informar o tempo e limpar o log.
	 * @param textLog TextArea que mostrar� as mensagens e errors do log.
	 * @param errorsTextLog TextArea que mostrar� apenas as mensagens de erros do log.
	 * @param informarTempo O tempo em que as mensagens ocorreram ser� mostrado?
	 */
	public Log(TextArea textLog, TextArea errorsTextLog, boolean informarTempo) {
		super(textLog, informarTempo);
		this.errorsTextLog = errorsTextLog;
	}

	/**
	 * Construtor para campo de texto, informar o tempo e limpar o log.
	 * @param textLog TextArea que mostrar� as mensagens e errors do log.
	 * @param errorsTextLog TextArea que mostrar� apenas as mensagens de erros do log.
	 * @param informarTempo O tempo em que as mensagens ocorreram ser� mostrado?
	 * @param clearOnCompile O log ser� limpo ao compilar?
	 */
	public Log(TextArea textLog, TextArea errorsTextLog, boolean informarTempo, boolean clearOnCompile) {
		super(textLog, informarTempo, clearOnCompile);
		this.errorsTextLog = errorsTextLog;
	}

	/**
	 * M�todo respons�vel por imprimir a mensagem de erro na TextArea (Log) e adicion�-la na lista de erros.
	 * @param mensagem Mensagem a ser imprimida.
	 */
	@Override
	protected void printErro(String mensagem) {
		super.printErro(mensagem);
		errorsTextLog.setText(erros);
	}

	/**
	 * Mostra apenas mensagens de erros no log.
	 */
	@Override
	public void mostrarApenasErros() {
		errorsTextLog.setText(erros);
	}

	/**
	 * Limpa todas as mensagens e erros do log.
	 */
	@Override
	public void limpar() {
		super.limpar();
		errorsTextLog.setText("");
	}

	public TextArea getErrorsTextLog() {
		return errorsTextLog;
	}
}
