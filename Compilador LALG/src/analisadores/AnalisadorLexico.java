package analisadores;

import java.nio.file.Path;
import java.util.HashMap;

import application.Compilador;
import automatos.AutomatoLexico;
import automatos.AutomatoNumero;
import exceptions.AnaliseLexicaException;
import exceptions.ComentarioException;
import exceptions.FimDoScriptException;
import exceptions.MensagemInexistenteException;
import exceptions.TokenInexistenteException;

public class AnalisadorLexico extends AutomatoLexico {
	protected String caractereAtual = "";
	protected String proximoCaractere = "";
	protected boolean proximoCaractereNaProximaLinha = false;
	protected int ultimoJ = 0;
	protected int ultimoI = 0;
	protected int iPalavra = 0;
	protected int jPalavra = 0;
	protected String palavra;
	protected String proximoEstado = "";
	protected static final String CHAVE_COMENTARIO = "COMENT�RIO";
	protected boolean pulouLinha = false;
	protected boolean possuiAlgumErro = false;

	/**
	 * Instancia o aut�mato e seu aut�mato num�rico a partir dos caminhos dos respectivos arquivos em formato <i>txt</i> em que eles est�o contidos.
	 * @param caminhoAutomato Caminho do arquivo do aut�mato.
	 * @param caminhoAutomatoNumerico Caminho do arquivo do aut�mato num�rico.
	 */
	public AnalisadorLexico(String caminhoAutomato, AutomatoNumero caminhoAutomatoNumerico) {
        super(caminhoAutomato, caminhoAutomatoNumerico);
    }

	/**
	 * Instancia o aut�mato e seu aut�mato num�rico a partir dos caminhos dos respectivos arquivos em formato <i>txt</i> em que eles est�o contidos.
	 * @param caminhoAutomato Caminho do arquivo do aut�mato.
	 * @param caminhoAutomatoNumerico Caminho do arquivo do aut�mato num�rico.
	 */
	public AnalisadorLexico(Path caminhoAutomato, AutomatoNumero caminhoAutomatoNumerico) {
		super(caminhoAutomato, caminhoAutomatoNumerico);
	}

	/**
	 * M�todo que reinicia a analise l�xica.
	 */
	public void reiniciarAnaliseLexica() {
		caractereAtual = "";
		proximoCaractere = "";
		ultimoJ = 0;
		ultimoI = 0;
		possuiAlgumErro = false;
	}

	/**
	 * Pula a leitura de uma linha e tenta ler o pr�ximo caractere dessa pr�xima linha.
	 */
	protected void pularLeituraLinha() {
		ultimoJ++;
		ultimoI = 0;
		pulouLinha = true;
		atualizarProximoCaractere();
	}
	
	/**
	 * Utilizado dentro do m�todo <i>getProximoCaractere</i> para verificar se o caractere que est� sendo pego � o pr�ximo, e n�o o atual.
	 * @param i Linha atual da leitura
	 * @return Se � o pr�ximo caractere que est� sendo pego.
	 */
	protected boolean isProximoCaractere(int i) {
		return i > ultimoI;
	}
	
	/**
	 * Pega o pr�ximo caractere da cadeia de entrada, verificando a leitura atual � do caractere atual ou do pr�ximo caractere.
	 * @param i Coluna de leitura atual.
	 * @param j Linha de leitura atual.
	 * @param retornar Guarda se o m�todo que chamou precisar� retornar (finalizar o processamento) ap�s o t�rmino deste m�todo.
	 * @return Retorna o pr�ximo caractere.
	 */
	protected String getProximoCaractere(int i, int j, boolean retornar) {
		String caractere = "";
		try {
			caractere = Compilador.instance.getScript().get(j).substring(i, i + 1);
		}
		catch(IndexOutOfBoundsException ex) {
			try {
				// Aplicado apenas ao pr�ximo caractere, e n�o tamb�m ao caractere atual
				if(isProximoCaractere(i)) {
					caractere = Compilador.instance.getScript().get(j + 1).substring(0, 1);
					proximoCaractereNaProximaLinha = true;
				}
			}
			catch(IndexOutOfBoundsException ex2) {
				try {
					if(Compilador.instance.getScript().get(j + 1).length() == 0) {
						if(proximoCaractere.isEmpty()) {
							pularLeituraLinha();
							retornar = true;
						}
						else {
							// Pular linha sem descartar o �ltimo caractere lido
							ultimoJ++;
							ultimoI = 0;
							pulouLinha = true;
						}
					}
				}
				catch(IndexOutOfBoundsException ex3) { }
				if(j == Compilador.instance.getScript().size() - 1) {
					caractere = "FIM_DO_ARQUIVO";
				}
				else {
					caractere = "";
				}
			}
		}
		return caractere;
	}

	/**
	 * M�todo respons�vel por fazer a atualiza��o do caractere atual e do pr�ximo que est�o sendo lidos da cadeia de entrada.
	 * Guarda a onde o analisador parou a sua leitura.
	 */
    protected void atualizarProximoCaractere() {
		boolean retornar = false;
		proximoCaractereNaProximaLinha = false;
		int i = ultimoI;
		int j = ultimoJ;
		pulouLinha = false;
		if((j < Compilador.instance.getScript().size() && i < Compilador.instance.getScript().get(j).length()) || 
				Compilador.instance.getScript().get(j).length() == 0) {
			try {
				caractereAtual = getProximoCaractere(i, j, retornar);
				proximoCaractere = getProximoCaractere(i + 1, j, retornar);
				if(retornar) {
					return;
				}
			}
			// A linha � vazia
			catch(StringIndexOutOfBoundsException ex) {
				pularLeituraLinha();
				return;
			}
			caractereAtual = tratarCaracteresEspeciais(caractereAtual);
			proximoCaractere = tratarCaracteresEspeciais(proximoCaractere);
			if(!pulouLinha) {
				ultimoJ = ((proximoCaractereNaProximaLinha) ? j + 1 : j);
				ultimoI = ((proximoCaractereNaProximaLinha) ? 0 : i + 1);
			}
			palavra += caractereAtual;
			jPalavra = j;
			iPalavra = i + 1;
		}
    }

    /**
     * Gera o token da atual palavra, verificando se ela encontra na tabela de palavras reservadas ou na de s�mbolos ou se o pr�ximo estado possui uma mensagem,
     * definindo assim o token da palavra.
     * @return Retorna a palavra formatada e o seu token.
     */
    protected HashMap<String, String> gerarToken() {
		HashMap<String, String> token = new HashMap<String, String>();
		String key = "";
		palavra = trimEspecial(palavra); // Retirar espa�os e tabula��es
		try {
			key = Compilador.getInstance().getChaveNasPalavrasReservadasOuTabelaSimbolos(palavra);
		}
		catch(IndexOutOfBoundsException ex) {
			try {
				key = getMensagem(proximoEstado);
				if(key.equals(CHAVE_COMENTARIO)) {
					throw new ComentarioException();
				}
			}
			catch(MensagemInexistenteException ex2) {
				// Chave desconhecida
				throw new TokenInexistenteException();
			}
		}
		token.put(key, palavra);
		return token;
    }
    
    /**
     * Trata os caracteres especiais, substituindo-os para o respectivo s�mbolo de leitura do aut�mato.
     * @param caractere Caractere a ser tratado.
     * @return Retorna o caractere com os novos s�mbolos.
     */
    public String tratarCaracteresEspeciais(String caractere) {
        if(caractere.isEmpty()) {
        	caractere = SINALIZADOR_NOVA_LINHA;
        }
        else if(caractere.equals("\t")) {
        	caractere = SINALIZADOR_TABULACAO;
        }
        else if(caractere.equals(" ")) {
        	caractere = SINALIZADOR_ESPACO_BRANCO;
        }
        return caractere;
    }
    
    /**
     * Remove s�mbolos de espa�os em branco, tabula��es e novas linhas da string passada.
     * @param string String para remo��o desses s�mbolos.
     * @return Retorna a string com esses s�mbolos removidos.
     */
    public String trimEspecial(String string) {
        string = string.replace(SINALIZADOR_NOVA_LINHA, "");
        string = string.replace(SINALIZADOR_TABULACAO, "");
        string = string.replace(SINALIZADOR_ESPACO_BRANCO, "");
        return string;
    }
    
    /**
     * Pega o pr�ximo token.
     * @return Retorna o token.
     * @throws FimDoScriptException Caso n�o haja mais palavras a serem lidas, pois o fim do arquivo foi atingido.
     * @throws AnaliseLexicaException Caso ocorra algum erro na leitura l�xica.
     */
    public Token getProximoToken() throws FimDoScriptException, AnaliseLexicaException {
    	if(proximoCaractere.equals("FIM_DO_ARQUIVO")) {
    		throw new FimDoScriptException();
    	}
    	
		HashMap<String, String> token = new HashMap<String, String>();
		boolean encontrouToken = false;
		
        iniciarProcessoCadeia();
        palavra = "";
        proximoEstado = "";
        while (!encontrouToken && !proximoCaractere.equals("FIM_DO_ARQUIVO")) {
            atualizarProximoCaractere();
            try {
            	if(proximoEstado.isEmpty()) {
            		estadoAtual = transicao(estadoAtual, caractereAtual);
            	}
            	else {
            		estadoAtual = proximoEstado;
            	}
            	proximoEstado = transicao(estadoAtual, proximoCaractere);
            	// Cadeia Aceita
            	if(!trimEspecial(palavra).isEmpty() && (estadoFinal(proximoEstado) || (proximoCaractereNaProximaLinha && !lendoComentario))) {
            		// Para o processamento pois uma palavra foi detectada
            		try {
	    				token = gerarToken();
	    				encontrouToken = true;
            		}
            		catch(TokenInexistenteException ex) { }
            	}
            }
            // Cadeia Rejeitada
            catch (IllegalArgumentException ex) {
                try {
            		palavra = trimEspecial(palavra); // Retirar espa�os e tabula��es
                	throw new AnaliseLexicaException(getErro(estadoAtual), palavra, jPalavra + 1, iPalavra - palavra.length() + 1);
                }
                // Cadeia Rejeitada n�o cont�m um erro
                catch(MensagemInexistenteException ex2) {
                	if(!proximoCaractere.equals("FIM_DO_ARQUIVO")) {
	                	iniciarProcessoCadeia();
						palavra = "";
				        proximoEstado = "";
                	}
				}
            }
        }
    	if(proximoCaractere.equals("FIM_DO_ARQUIVO") && token.size() == 0) {
    		throw new FimDoScriptException();
    	}
        return new Token(token, jPalavra + 1, iPalavra - palavra.length() + 1);
    }
	
	public boolean isPossuiAlgumErro() {
		return possuiAlgumErro;
	}
	
    /*
	public Token oldGetProximoToken() throws FimDoScriptException, AnaliseLexicaException {
		HashMap<String, String> token = new HashMap<String, String>();
		palavra = "";
		boolean tokenEncontrado = false;
		boolean proximoCaractereNaProximaLinha = false;
		int i = 0;
		int j = 0;
		for(j = ultimoJ; j < Compilador.instance.getScript().size() && !tokenEncontrado; j++) {
			for(i = ultimoI; i < Compilador.instance.getScript().get(j).length() && !tokenEncontrado; i++) {
				caractereAtual = Compilador.instance.getScript().get(j).substring(i, i + 1);
				try {
					proximoCaractere = Compilador.instance.getScript().get(j).substring(i + 1, i + 2);
				}
				catch(IndexOutOfBoundsException ex) {
					try {
						proximoCaractere = Compilador.instance.getScript().get(j + 1).substring(0, 1);
						proximoCaractereNaProximaLinha = true;
					}
					catch(IndexOutOfBoundsException ex2) {
						proximoCaractere = "FIM_DO_ARQUIVO";
					}
				}
				//if(!caractereAtual.equals(" ")) {
					palavra += caractereAtual;
				//}
				if(proximoCaractereNaProximaLinha || proximoCaractere.equals(" ") || proximoCaractere.equals("\n") || proximoCaractere.equals("\t") || 
						proximoCaractere.equals("FIM_DO_ARQUIVO") || proximoCaractere.equals(";") || proximoCaractere.equals(":") || 
						proximoCaractere.equals(",") || proximoCaractere.equals("(") || proximoCaractere.equals(")") || 
						(caractereAtual.equals("(") && pertenceAosCaracteresOuDigitos(proximoCaractere)) || 
						(palavra.equals("end") && proximoCaractere.equals("."))) {
					ultimoJ = ((proximoCaractereNaProximaLinha) ? j + 1 : j);
					ultimoI = ((proximoCaractereNaProximaLinha) ? 0 : i + 1);
					palavra = palavra.trim();
					try {
						ResultadoAutomato resultado = processarCadeia(palavra);
						// Cadeia Aceita
						if(resultado.isCadeiaAceita()) {
							String key = "";
							try {
								key = Compilador.getInstance().getChaveNasPalavrasReservadasOuTabelaSimbolos(palavra);
							}
							catch(IndexOutOfBoundsException ex) {
								if(resultado.contemMensagem()) {
									key = resultado.getMensagem();
								}
								else {
									key = "Chave desconhecida";
								}
							}
							token.put(key, palavra);
							tokenEncontrado = true;
							break;
						}
						// Cadeia Rejeitada e cont�m um erro
						else if(resultado.contemErro()) {
							throw new AnaliseLexicaException(resultado.getErro(), palavra, j + 1, i - palavra.length() + 2);
						}
						// Cadeia Rejeitada e n�o cont�m um erro (exemplo: coment�rio)
						else {
							palavra = "";
							proximoCaractereNaProximaLinha = false;
						}
					}
					// Palavra vazia
					catch(StringIndexOutOfBoundsException ex) {
						proximoCaractereNaProximaLinha = false;
					}
					catch(IllegalArgumentException ex) { }
				}
			}
		}
		if(tokenEncontrado) {
			return new Token(token, j, i - palavra.length() + 2);
		}
		else {
			throw new FimDoScriptException();
		}
	}
	
	public String analisar() {
		String resultado = "";
		for(int i = 0; i < Compilador.getInstance().getScript().size(); i++) {
			ResultadoAutomato res = processarCadeia(Compilador.getInstance().getScript().get(i));
			resultado += Compilador.getInstance().getScript().get(i) + " [" + 
					(res.isCadeiaAceita() ? "Aceito" : "Rejeitado") + "]\n";
		}
		return resultado;
	}
	
	
    protected void oldAtualizarProximoCaractere() {
		boolean retornar = false;
		proximoCaractereNaProximaLinha = false;
		int i = ultimoI;
		int j = ultimoJ;
		pulouLinha = false;
		if((j < Compilador.instance.getScript().size() && i < Compilador.instance.getScript().get(j).length()) || 
				Compilador.instance.getScript().get(j).length() == 0) {
			try {
				caractereAtual = Compilador.instance.getScript().get(j).substring(i, i + 1);
				try {
					proximoCaractere = Compilador.instance.getScript().get(j).substring(i + 1, i + 2);
				}
				catch(IndexOutOfBoundsException ex) {
					try {
						proximoCaractere = Compilador.instance.getScript().get(j + 1).substring(0, 1);
						proximoCaractereNaProximaLinha = true;
					}
					catch(IndexOutOfBoundsException ex2) {
						try {
							if(Compilador.instance.getScript().get(j + 1).length() == 0) {
								if(proximoCaractere.isEmpty()) {
									pularLeituraLinha(pulouLinha);
									//proximoCaractereNaProximaLinha = true;
									return;
								}
								else {
									// Pular linha sem descartar o �ltimo caractere lido
									ultimoJ++;
									ultimoI = 0;
									pulouLinha = true;
								}
							}
						}
						catch(IndexOutOfBoundsException ex3) { }
						if(j == Compilador.instance.getScript().size() - 1) {
							proximoCaractere = "FIM_DO_ARQUIVO";
						}
						else {
							proximoCaractere = "";
						}
					}
				}
			}
			// A linha � vazia
			catch(StringIndexOutOfBoundsException ex) {
				pularLeituraLinha(pulouLinha);
				return;
			}
			caractereAtual = tratarCaracteresEspeciais(caractereAtual);
			proximoCaractere = tratarCaracteresEspeciais(proximoCaractere);
			if(!pulouLinha) {
				ultimoJ = ((proximoCaractereNaProximaLinha) ? j + 1 : j);
				ultimoI = ((proximoCaractereNaProximaLinha) ? 0 : i + 1);
			}
			palavra += caractereAtual;
			jPalavra = j;
			iPalavra = i + 1;
		}
    }
	*/
}
