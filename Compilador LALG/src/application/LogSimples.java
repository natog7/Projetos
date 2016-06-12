package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import exceptions.AnaliseLexicaException;
import exceptions.AnaliseSintaticaException;
import javafx.scene.control.TextArea;

public class LogSimples {
	protected TextArea textLog;
	protected String mensagens;
	protected int qtdMensagens;
	protected String erros;
	protected int qtdErros;
	protected DateTimeFormatter dateFormat;
	protected boolean informarTempo = false;
	protected boolean clearOnCompile = false;

	/**
	 * Construtor para campo de texto.
	 * @param textLog TextArea que mostrar� as mensagens e errors do log.
	 */
	public LogSimples(TextArea textLog) {
		this.textLog = textLog;
		reiniciarValores();
		dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
	}

	/**
	 * Construtor para campo de texto e informar o tempo.
	 * @param textLog TextArea que mostrar� as mensagens e errors do log.
	 * @param informarTempo O tempo em que as mensagens ocorreram ser� mostrado?
	 */
	public LogSimples(TextArea textLog, boolean informarTempo) {
		this.textLog = textLog;
		reiniciarValores();
		dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
		this.informarTempo = informarTempo;
	}

	/**
	 * Construtor para campo de texto, informar o tempo e limpar o log.
	 * @param textLog TextArea que mostrar� as mensagens e errors do log.
	 * @param informarTempo O tempo em que as mensagens ocorreram ser� mostrado?
	 * @param clearOnCompile O log ser� limpo ao compilar?
	 */
	public LogSimples(TextArea textLog, boolean informarTempo, boolean clearOnCompile) {
		this.textLog = textLog;
		reiniciarValores();
		dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
		this.informarTempo = informarTempo;
		this.clearOnCompile = clearOnCompile;
	}

	/**
	 * Formata a mensagem para posteriormente mostrar ao usu�rio no log.
	 * @param mensagem Mensagem a ser mostrada.
	 */
	public void log(String mensagem) {
		print(((informarTempo) ? "(" + dateFormat.format(LocalDateTime.now()) + ") " : "") + mensagem);
	}

	/**
	 * Formata a mensagem de erro para posteriormente reportar ao usu�rio no log.
	 * @param mensagem Mensagem de erro a ser mostrada.
	 */
	public void logErro(String mensagem) {
		printErro(((informarTempo) ? "(" + dateFormat.format(LocalDateTime.now()) + ") " : "") + "[ERRO] " + mensagem);
	}

	/**
	 * Formata a mensagem de erro para posteriormente reportar ao usu�rio no log, junto com a palavra que causou o erro e sua respectiva linha e coluna.
	 * @param mensagem Mensagem de erro a ser mostrada.
	 * @param palavra Palavra que possui o erro.
	 * @param linha Linha de ocorr�ncia do erro.
	 * @param coluna Coluna de ocorr�ncia do erro.
	 */
	public void logErro(String mensagem, String palavra, int linha, int coluna) {
		printErro(((informarTempo) ? "(" + dateFormat.format(LocalDateTime.now()) + ") " : "") + "[ERRO - Linha: " + linha + " Coluna: " + coluna + "] " + mensagem + " = " + palavra);
	}

	/**
	 * Formata a mensagem de erro a partir do pr�prio erro, para posteriormente reportar ao usu�rio no log, junto com a palavra que causou o erro e sua respectiva linha e coluna.
	 * @param erroAnalise Erro lan�ado que ser� mostrado.
	 */
	public void logErro(AnaliseLexicaException erroAnalise) {
		printErro(((informarTempo) ? "(" + dateFormat.format(LocalDateTime.now()) + ") " : "") + "[ERRO L�xico - Linha: " + erroAnalise.getLinha() + " Coluna: " + erroAnalise.getColuna() + "] " + 
				erroAnalise.getMessage() + " = " + erroAnalise.getPalavra());
	}

	/**
	 * Formata a mensagem de erro a partir do pr�prio erro, para posteriormente reportar ao usu�rio no log, junto com a palavra que causou o erro e sua respectiva linha e coluna.
	 * @param erroAnalise Erro lan�ado que ser� mostrado.
	 */
	public void logErro(AnaliseSintaticaException erroAnalise) {
		printErro(((informarTempo) ? "(" + dateFormat.format(LocalDateTime.now()) + ") " : "") + "[ERRO Sint�tico - Linha: " + erroAnalise.getLinha() + " Coluna: " + erroAnalise.getColuna() + "] " + 
				erroAnalise.getMessage());
	}

	/**
	 * M�todo respons�vel por imprimir a mensagem na TextArea e adicion�-la na lista de mensagens.
	 * @param mensagem Mensagem a ser imprimida.
	 */
	protected void print(String mensagem) {
		mensagens = mensagem + "\n" + mensagens;
		qtdMensagens++;
		textLog.setText(mensagens);
	}

	/**
	 * M�todo respons�vel por imprimir a mensagem de erro na TextArea (Log) e adicion�-la na lista de erros.
	 * @param mensagem Mensagem a ser imprimida.
	 */
	protected void printErro(String mensagem) {
		print(mensagem);
		qtdErros++;
		erros = mensagem + "\n" + erros;
	}
	
	/**
	 * Mostra todas as mensagens, inclusive erros, no log.
	 */
	public void mostrarTodasMensagens() {
		textLog.setText(mensagens);
	}
	
	/**
	 * Mostra apenas mensagens de erros no log.
	 */
	public void mostrarApenasErros() {
		textLog.setText(erros);
	}

	/**
	 * M�todo para limpar o log caso a op��o limpar ao compilar esteja ativa.
	 */
	public void limparAoCompilar() {
		if(clearOnCompile) {
			limpar();
		}
	}

	/**
	 * Limpa todas as mensagens e erros do log.
	 */
	public void limpar() {
		textLog.setText("");
		reiniciarValores();
	}

	/**
	 * Reinicia o armazenamento das mensagens e dos erros.
	 */
	protected void reiniciarValores() {
		mensagens = "";
		erros = "";
		qtdMensagens = 0;
		qtdErros = 0;
	}
	
	public void setClearOnCompile(boolean clearOnCompile) {
		this.clearOnCompile = clearOnCompile;
	}
	
	public int getQtdMensagens() {
		return qtdMensagens;
	}

	public int getQtdErros() {
		return qtdErros;
	}

	public TextArea getTextLog() {
		return textLog;
	}
}