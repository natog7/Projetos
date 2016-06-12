package automatos;

public class ResultadoAutomato {
	protected boolean cadeiaAceita = false;
	protected String mensagem = "";
	protected String erro = "";

	/**
	 * Construtor que tem por parametro se a cadeia � aceita ou n�o.
	 * @param cadeiaAceita Cadeia aceita ou n�o.
	 */
	public ResultadoAutomato(boolean cadeiaAceita) {
		this.cadeiaAceita = cadeiaAceita;
	}

	/**
	 * Construtor que tem por parametro se a cadeia � aceita ou n�o, e qual mensagem ser� mostrada junto ao resultado.
	 * @param cadeiaAceita Cadeia aceita ou n�o.
	 * @param mensagem Mensagem do estado.
	 */
	public ResultadoAutomato(boolean cadeiaAceita, String mensagem) {
		this.cadeiaAceita = cadeiaAceita;
		this.mensagem = mensagem;
	}

	/**
	 * Construtor que tem por parametro se a cadeia � aceita ou n�o, qual mensagem ser� mostrada junto ao resultado e
	 * qual o erro, caso tenha, presente na cadeia.
	 * @param cadeiaAceita Cadeia aceita ou n�o.
	 * @param mensagem Mensagem do estado.
	 * @param erro Mensagem de erro do estado.
	 */
	public ResultadoAutomato(boolean cadeiaAceita, String mensagem, String erro) {
		this.cadeiaAceita = cadeiaAceita;
		this.mensagem = mensagem;
		this.erro = erro;
	}

	/**
	 * Verififca se o resultado possui uma mensagem.
	 * @return Se cont�m uma mensagem.
	 */
	public boolean contemMensagem() {
		return !mensagem.isEmpty();
	}

	/**
	 * Verififca se o resultado possui uma mensagem de erro.
	 * @return Se cont�m uma mensagem de erro.
	 */
	public boolean contemErro() {
		return !erro.isEmpty();
	}

	/**
	 * Verifica se a cadeia foi aceita.
	 * @return Retorna <i>true</i> se a cadeia foi aceita.
	 */
	public boolean isCadeiaAceita() {
		return cadeiaAceita;
	}

	public String getMensagem() {
		return mensagem;
	}

	public String getErro() {
		return erro;
	}

	@Override
	public String toString() {
		return "[ Aceita: " + cadeiaAceita + ", MSG: \"" + mensagem + "\", ERRO: \"" + erro + "\" ]";
	}
}
