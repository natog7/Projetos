package analisadores;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.activity.InvalidActivityException;
import javax.management.OperationsException;

import application.Compilador;
import application.Controlador;
import exceptions.AnaliseLexicaException;
import exceptions.AnaliseSintaticaException;
import exceptions.ComentarioException;
import exceptions.FalhaAoRetomarAnaliseException;
import exceptions.FimDoScriptException;
import regrasDeProducao.Elemento;
import regrasDeProducao.Gramatica;

public class AnalisadorSintatico extends Gramatica {
	protected AnalisadorLexico analisadorLexico;
	//protected Gramatica gramatica;
	protected List<Token> tokens = new ArrayList<Token>();
	protected Token tokenAtual;
	protected int tokenAtualIndice = 0;
	protected List<Elemento> tokensEsperados = new ArrayList<Elemento>();
	protected List<String> tokensEsperadosString = new ArrayList<String>();
	protected Elemento tokenPai = null;
	protected boolean analiseFinalizada = false;
	protected boolean possuiAlgumErro = false;
	
	/**
	 * Construtor da classe.
	 * @param analisadorLexico
	 */
	public AnalisadorSintatico(AnalisadorLexico analisadorLexico, Path caminho) {
		super(caminho);
		this.analisadorLexico = analisadorLexico;
		lerArquivo();
		calcularPrimeiros();
		calcularSeguidores();
		/*for(Elemento e : getAlfabeto().values()) {
			System.out.println(e + " = Primeiros: " + e.getPrimeiros());
			System.out.println(e + " = Seguidores: " + e.getSeguidores());
		}*/
	}

	/**
	 * Método que pega o próximo token, caso não tenha, é acusado erro de fim do script.
	 * E caso ocorra um erro na análixe léxica, esse é informado ao usuário.
	 * @throws FimDoScriptException
	 */
	protected void getProximoToken() throws FimDoScriptException {
		Token token;
		try {
			token = analisadorLexico.getProximoToken();
			getTokens().add(token);
		}
		catch(AnaliseLexicaException ex) {
			possuiAlgumErro = true;
			Controlador.getInstance().getLog().logErro(ex);
		}
		catch(ComentarioException ex) {

		}
	}

	/**
	 * Pega todos os tokens que o analisador léxico disponibiliza, preenchendo a lista de tokens.
	 */
	public void getTodosTokens() {
		tokens.clear();
		analisadorLexico.reiniciarAnaliseLexica();
		try {
			while(true) {
				getProximoToken();
			}
		}
		catch(FimDoScriptException ex) { }
	}

	/**
	 * Método para pegar as informações de todos os tokens existentes.
	 * @return Informações dos tokens
	 */
	public String getTokensInfo() {
		String analise = "";
		for(Token token : tokens) {
			analise += token.toString() + "\n";
		}
		return analise;
	}

	/**
	 * Avança o token atual para o próximo token da lista de tokens retornada pelo analisador léxico.
	 * @param produtorAtual Símbolo não-terminal que está chamando esse método.
	 */
	protected void obterSimbolo(Elemento produtorAtual) {
		tokenAtualIndice++;
		try {
			tokenAtual = tokens.get(tokenAtualIndice);
		}
		catch(IndexOutOfBoundsException ex) {
			// Fim do script alcançado
			if(produtorAtual.getElemento().equals("<programa>")) {
				throw new FimDoScriptException();
			}
		}
	}

	/**
	 * Avança o token atual para o próximo token da lista de tokens retornada pelo analisador léxico.
	 */
	protected void obterSimbolo() {
		tokenAtualIndice++;
		try {
			tokenAtual = tokens.get(tokenAtualIndice);
		}
		catch(IndexOutOfBoundsException ex) {
			// Fim do script alcançado
			throw new FimDoScriptException();
		}
	}

	/**
	 * Busca tokens de sincronização a fim de retomar a análise sintática em um ponto seguro.
	 * @param sincronizadores Símbolos sincronizadores adicionais.
	 */
	protected void retomarAnalise(List<Elemento> sincronizadores) {
		//Elemento elemento = producao.getAlfabeto().get(tokenAtual.getToken());
		List<Elemento> sincronizadoresUtilizados = new ArrayList<Elemento>();
		Token tokenErrado = tokenAtual;
		for(Elemento token : tokensEsperados) {
			for(Elemento elem : token.getSeguidores()) {
				if(!sincronizadoresUtilizados.contains(elem)) {
					sincronizadoresUtilizados.add(elem);
				}
			}
		}
		try {
			//for(Elemento elem : producao.getProdutor(token.getElemento()).getSeguidores()) {
			for(Elemento elem : tokenPai.getSeguidores()) {
				if(!sincronizadoresUtilizados.contains(elem)) {
					sincronizadoresUtilizados.add(elem);
				}
			}
		}
		catch(IndexOutOfBoundsException ex) { }
		for(Elemento elem : sincronizadores) {
			if(!sincronizadoresUtilizados.contains(elem)) {
				sincronizadoresUtilizados.add(elem);
			}
		}
		/*for(Elemento elem : tokenEsperado.getSincronizadores()) {
			if(!sincronizadores.contains(elem)) {
				sincronizadores.add(elem);
			}
		}*/
		try {
			retomarAnalisePorSincronizadores(sincronizadoresUtilizados);
			tokenAtual = tokens.get(tokenAtualIndice);
		}
		catch(FalhaAoRetomarAnaliseException ex) {
			Controlador.getInstance().getLog().logErro("Falha ao retomar a análise sintática a partir do token " + tokenErrado + ".");
		}
	}

	/**
	 * Tenta retomar a análise a partir do token que ocorreu o erro sintático utilizando os tokens/símbolos de sincronização.
	 * <br>Quando a análise for retomada o índice do token atual é atualizado para a localização do token que obteve sucesso em retomar a análise.
	 * @param sincronizadores Símbolos sincronizadores.
	 */
	protected void retomarAnalisePorSincronizadores(List<Elemento> sincronizadores) {
		int i = 0;
		for(i = tokenAtualIndice; i < tokens.size(); i++) {
			for(Elemento elem : sincronizadores) {
				if(tokens.get(i).getToken().equals(elem.getElemento())) {
					tokenAtualIndice = i;
					return;
				}
			}
		}
		// Caso a retomada da análise tenha alcançado o último token
		if(i == tokens.size()) {
			throw new FimDoScriptException();
		}
		throw new FalhaAoRetomarAnaliseException();
	}

	/*protected boolean retomarAnalise(String tokenRecuperacao) {
		boolean sucesso = false;
		for(int i = tokenAtualIndice; i < tokens.size(); i++) {
			if(tokens.get(i).getToken().equals(tokenRecuperacao)) {
				sucesso = true;
				tokenAtualIndice = i;
				break;
			}
		}
		return sucesso;
	}*/

	/**
	 * Alerta o erro ao usuário e tenta retomar a análise sintática.
	 * @param produtor Símbolo não-terminal que está chamando esse método.
	 * @param tokensEsperados Tokens/símbolos que são esperado no lugar do token que ocasionou o erro.
	 */
	protected void erro(Elemento produtor, String... tokensEsperados) {
		erro(produtor, Arrays.asList(), tokensEsperados);
	}

	/**
	 * Alerta o erro ao usuário e tenta retomar a análise sintática.
	 * @param produtor Símbolo não-terminal que está chamando esse método.
	 * @param sincronizadores Lista de símbolos sincronizadores adicionais.
	 * @param tokensEsperados Tokens/símbolos que são esperado no lugar do token que ocasionou o erro.
	 */
	protected void erro(Elemento produtor, List<Elemento> sincronizadores, String... tokensEsperados) {
		tokenPai = produtor;
		tokensEsperadosString.addAll(Arrays.asList(tokensEsperados));
		for(String token : tokensEsperados) {
			this.tokensEsperados.add(getAlfabeto().get(token));
		}
		alertarErroSintatico();
		retomarAnalise(sincronizadores);
		tokensEsperadosString.clear();
		this.tokensEsperados.clear();
	}

	/**
	 * Alerta o erro ao usuário.
	 */
	protected void alertarErroSintatico() {
		possuiAlgumErro = true;
		Controlador.getInstance().getLog().logErro(new AnaliseSintaticaException("Erro no símbolo [" + tokenAtual.getToken() + " = " + tokenAtual.getPalavra() + 
				"], é esperado um dos seguintes símbolos: " + tokensEsperadosString.toString() + ".", tokenAtual));
		/*Controlador.getInstance().getLog().logErro(new AnaliseSintaticaException("Erro no símbolo [" + tokenAtual.getToken() + " = " + tokenAtual.getPalavra() + 
				"], com o pai [" + tokenPai + "], é esperado um dos seguintes símbolos: " + tokensEsperadosString.toString() + ".", tokenAtual));*/
	}

	/**
	 * Reinicia as configurações para uma próxima análise sintática.
	 */
	public void reiniciarAnalise() {
		tokensEsperadosString.clear();
		this.tokensEsperados.clear();
		tokenPai = null;
		tokenAtual = null;
		tokenAtualIndice = -1;
		analiseFinalizada = false;
		possuiAlgumErro = false;
	}

	/**
	 * Analisa sintaticamente o script escrito.
	 */
	public void analisar() {
		reiniciarAnalise();
		try {
			obterSimbolo();
			procedimentoPrograma();
			// Análise finalizada com sucesso sem nenhum erro
			analiseFinalizada = true;
		}
		catch(AnaliseSintaticaException ex) {
			possuiAlgumErro = true;
			Controlador.getInstance().getLog().logErro(ex);
		}
		catch(FimDoScriptException ex) {
			analiseFinalizada = true;
		}
		catch(FalhaAoRetomarAnaliseException ex) {
			
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "programa".
	 */
	protected void procedimentoPrograma() {
		Elemento produtor = getAlfabeto().get("<programa>");
		if(tokenAtual.getToken().equals("PROGRAMA")) {
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "PROGRAMA");
		}
		if(tokenAtual.getToken().equals("IDENTIFICADOR")) {
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "IDENTIFICADOR");
		}
		if(tokenAtual.getToken().equals("PONTO_VIRGULA")) {
			obterSimbolo(produtor);
		}
		else{
			//erro(producao.primeiro(new Elemento("<corpo>", false)), "PONTO_VIRGULA");
			erro(produtor, "PONTO_VIRGULA");
		}
		procedimentoCorpo();
		if(tokenAtual.getToken().equals("FINAL_PROGRAMA")) {
			obterSimbolo(produtor);
		}
		else {
			erro(produtor, "FINAL_PROGRAMA");
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "corpo".
	 */
	protected void procedimentoCorpo() {
		Elemento produtor = getAlfabeto().get("<corpo>");
		procedimentoDeclaracao();
		if(tokenAtual.getToken().equals("INICIO")) {
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "INICIO");
		}
		procedimentoComandos();
		if(tokenAtual.getToken().equals("FIM")) {
			obterSimbolo(produtor);
		}
		else {
			erro(produtor, "FIM");
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "dc".
	 */
	protected void procedimentoDeclaracao() {
		Elemento produtor = getAlfabeto().get("<dc>");
		procedimentoDeclaracaoVariaveis();
		procedimentoDeclaracaoProcedimento();
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "dc_v".
	 */
	protected void procedimentoDeclaracaoVariaveis() {
		Elemento produtor = getAlfabeto().get("<dc_v>");
		if(tokenAtual.getToken().equals("VARIAVEL")) {
			obterSimbolo(produtor);
			procedimentoVariaveis();
			if(tokenAtual.getToken().equals("ATRIBUICAO_TIPO")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "ATRIBUICAO_TIPO");
			}
			procedimentoTipoVar();
			if(tokenAtual.getToken().equals("PONTO_VIRGULA")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "PONTO_VIRGULA");
			}
			procedimentoDeclaracaoVariaveis();
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "tipo_var".
	 */
	protected void procedimentoTipoVar() {
		Elemento produtor = getAlfabeto().get("<tipo_var>");
		if(tokenAtual.getToken().equals("REAL")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("INTEIRO")){
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "REAL", "INTEIRO");
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "variaveis".
	 */
	protected void procedimentoVariaveis() {
		Elemento produtor = getAlfabeto().get("<variaveis>");
		if(tokenAtual.getToken().equals("IDENTIFICADOR")){
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "IDENTIFICADOR");
		}
		procedimentoMaisVar();
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "mais_var".
	 */
	protected void procedimentoMaisVar() {
		Elemento produtor = getAlfabeto().get("<mais_var>");
		if(tokenAtual.getToken().equals("VIRGULA")){
			obterSimbolo(produtor);
			procedimentoVariaveis();
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "dc_p".
	 */
	protected void procedimentoDeclaracaoProcedimento() {
		Elemento produtor = getAlfabeto().get("<dc_p>");
		if(tokenAtual.getToken().equals("METODO")){
			obterSimbolo(produtor);
			if(tokenAtual.getToken().equals("IDENTIFICADOR")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "IDENTIFICADOR");
			}
			procedimentoParametros();
			if(tokenAtual.getToken().equals("PONTO_VIRGULA")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "PONTO_VIRGULA");
			}
			procedimentoCorpoProcedimento();
			procedimentoDeclaracaoProcedimento();
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "parametros".
	 */
	protected void procedimentoParametros() {
		Elemento produtor = getAlfabeto().get("<parametros>");
		if(tokenAtual.getToken().equals("ABRE_PARENTESES")){
			obterSimbolo(produtor);
			procedimentoListaParametros();
			if(tokenAtual.getToken().equals("FECHA_PARENTESES")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "FECHA_PARENTESES");
			}
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "lista_par".
	 */
	protected void procedimentoListaParametros() {
		Elemento produtor = getAlfabeto().get("<lista_par>");
		procedimentoVariaveis();
		if(tokenAtual.getToken().equals("ATRIBUICAO_TIPO")){
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "ATRIBUICAO_TIPO");
		}
		procedimentoTipoVar();
		procedimentoMaisParametros();
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "mais_par".
	 */
	protected void procedimentoMaisParametros() {
		Elemento produtor = getAlfabeto().get("<mais_par>");
		if(tokenAtual.getToken().equals("PONTO_VIRGULA")){
			obterSimbolo(produtor);
			procedimentoListaParametros();
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "corpo_p".
	 */
	protected void procedimentoCorpoProcedimento() {
		Elemento produtor = getAlfabeto().get("<corpo_p>");
		procedimentoDeclaracaoLocal();
		if(tokenAtual.getToken().equals("INICIO")){
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "INICIO");
		}
		procedimentoComandos();
		if(tokenAtual.getToken().equals("FIM")){
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "FIM");
		}
		if(tokenAtual.getToken().equals("PONTO_VIRGULA")){
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "PONTO_VIRGULA");
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "dc_loc".
	 */
	protected void procedimentoDeclaracaoLocal() {
		Elemento produtor = getAlfabeto().get("<dc_loc>");
		procedimentoDeclaracaoVariaveis();
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "lista_arg".
	 */
	protected void procedimentoListaArgumentos() {
		Elemento produtor = getAlfabeto().get("<lista_arg>");
		if(tokenAtual.getToken().equals("ABRE_PARENTESES")){
			obterSimbolo(produtor);
			procedimentoArgumentos();
			if(tokenAtual.getToken().equals("FECHA_PARENTESES")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "FECHA_PARENTESES");
			}
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "argumentos".
	 */
	protected void procedimentoArgumentos() {
		Elemento produtor = getAlfabeto().get("<argumentos>");
		if(tokenAtual.getToken().equals("IDENTIFICADOR")){
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "IDENTIFICADOR");
		}
		procedimentoMaisIdent();
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "mais_ident".
	 */
	protected void procedimentoMaisIdent() {
		Elemento produtor = getAlfabeto().get("<mais_ident>");
		if(tokenAtual.getToken().equals("PONTO_VIRGULA")){
			obterSimbolo(produtor);
			procedimentoArgumentos();
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "pfalsa".
	 */
	protected void procedimentoPFalsa() {
		Elemento produtor = getAlfabeto().get("<pfalsa>");
		if(tokenAtual.getToken().equals("ELSE")){
			obterSimbolo(produtor);
			procedimentoCmd();
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "comandos".
	 */
	protected void procedimentoComandos() {
		Elemento produtor = getAlfabeto().get("<comandos>");
		if(tokenAtual.getToken().equals("LER") || 
				tokenAtual.getToken().equals("ESCREVER") || 
				tokenAtual.getToken().equals("WHILE") || 
				tokenAtual.getToken().equals("IF") || 
				tokenAtual.getToken().equals("IDENTIFICADOR") || 
				tokenAtual.getToken().equals("INICIO")){
			procedimentoCmd();
			if(tokenAtual.getToken().equals("PONTO_VIRGULA")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "PONTO_VIRGULA");
			}
			procedimentoComandos();
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "cmd".
	 */
	protected void procedimentoCmd() {
		Elemento produtor = getAlfabeto().get("<cmd>");
		if(tokenAtual.getToken().equals("LER")){
			obterSimbolo(produtor);
			if(tokenAtual.getToken().equals("ABRE_PARENTESES")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "ABRE_PARENTESES");
			}
			procedimentoVariaveis();
			if(tokenAtual.getToken().equals("FECHA_PARENTESES")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "FECHA_PARENTESES");
			}
		}
		else if(tokenAtual.getToken().equals("ESCREVER")){
			obterSimbolo(produtor);
			if(tokenAtual.getToken().equals("ABRE_PARENTESES")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "ABRE_PARENTESES");
			}
			procedimentoVariaveis();
			if(tokenAtual.getToken().equals("FECHA_PARENTESES")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "FECHA_PARENTESES");
			}
		}
		else if(tokenAtual.getToken().equals("WHILE")){
			obterSimbolo(produtor);
			procedimentoCondicao();
			if(tokenAtual.getToken().equals("DO")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "DO");
			}
			procedimentoCmd();
		}
		else if(tokenAtual.getToken().equals("IF")){
			obterSimbolo(produtor);
			procedimentoCondicao();
			if(tokenAtual.getToken().equals("THEN")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "THEN");
			}
			procedimentoCmd();
			procedimentoPFalsa();
		}
		else if(tokenAtual.getToken().equals("IDENTIFICADOR")){
			obterSimbolo(produtor);
			procedimentoCmdAux();
		}
		else if(tokenAtual.getToken().equals("INICIO")){
			obterSimbolo(produtor);
			procedimentoComandos();
			if(tokenAtual.getToken().equals("FIM")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "FIM");
			}
		}
		else{
			erro(produtor, "LER", "ESCREVER", "WHILE", "IF", "IDENTIFICADOR", "INICIO");
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "cmdaux".
	 */
	protected void procedimentoCmdAux() {
		Elemento produtor = getAlfabeto().get("<cmdaux>");
		if(tokenAtual.getToken().equals("ATRIBUICAO")){
			obterSimbolo(produtor);
			procedimentoExpressao();
		}
		else if(tokenAtual.getToken().equals("ABRE_PARENTESES")){
			procedimentoListaArgumentos();
		}
		else {
			erro(produtor, Arrays.asList(getAlfabeto().get("ATRIBUICAO")), "ATRIBUICAO");
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "condicao".
	 */
	protected void procedimentoCondicao() {
		Elemento produtor = getAlfabeto().get("<condicao>");
		procedimentoExpressao();
		procedimentoRelacao();
		procedimentoExpressao();
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "relacao".
	 */
	protected void procedimentoRelacao() {
		Elemento produtor = getAlfabeto().get("<relacao>");
		if(tokenAtual.getToken().equals("IGUAL")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("DIFERENTE")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("MAIOR_IGUAL")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("MENOR_IGUAL")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("MAIOR")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("MENOR")){
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "IGUAL", "DIFERENTE", "MAIOR_IGUAL", "MENOR_IGUAL", "MAIOR", "MENOR");
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "expressao".
	 */
	protected void procedimentoExpressao() {
		Elemento produtor = getAlfabeto().get("<expressao>");
		procedimentoTermo();
		procedimentoOutrosTermos();
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "op_un".
	 */
	protected void procedimentoOpUn() {
		Elemento produtor = getAlfabeto().get("<op_un>");
		if(tokenAtual.getToken().equals("OPERADOR_SOMA")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("OPERADOR_SUBTRACAO")){
			obterSimbolo(produtor);
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "outros_termos".
	 */
	protected void procedimentoOutrosTermos() {
		Elemento produtor = getAlfabeto().get("<outros_termos>");
		if(tokenAtual.getToken().equals("OPERADOR_SOMA") || 
				tokenAtual.getToken().equals("OPERADOR_SUBTRACAO")){
			procedimentoOpAd();
			procedimentoTermo();
			procedimentoOutrosTermos();
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "op_ad".
	 */
	protected void procedimentoOpAd() {
		Elemento produtor = getAlfabeto().get("<op_ad>");
		if(tokenAtual.getToken().equals("OPERADOR_SOMA")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("OPERADOR_SUBTRACAO")){
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "OPERADOR_SOMA", "OPERADOR_SUBTRACAO");
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "termo".
	 */
	protected void procedimentoTermo() {
		Elemento produtor = getAlfabeto().get("<termo>");
		procedimentoOpUn();
		procedimentoFator();
		procedimentoMaisFatores();
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "mais_fatores".
	 */
	protected void procedimentoMaisFatores() {
		Elemento produtor = getAlfabeto().get("<mais_fatores>");
		if(tokenAtual.getToken().equals("OPERADOR_MULTIPLICACAO") || 
				tokenAtual.getToken().equals("OPERADOR_DIVISAO")){
			procedimentoOpMult();
			procedimentoFator();
			procedimentoMaisFatores();
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "op_mult".
	 */
	protected void procedimentoOpMult() {
		Elemento produtor = getAlfabeto().get("<op_mult>");
		if(tokenAtual.getToken().equals("OPERADOR_MULTIPLICACAO")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("OPERADOR_DIVISAO")){
			obterSimbolo(produtor);
		}
		else{
			erro(produtor, "OPERADOR_MULTIPLICACAO", "OPERADOR_DIVISAO");
		}
	}

	/**
	 * Método equivalente a regra de produção do simbolo não-terminal "fator".
	 */
	protected void procedimentoFator() {
		Elemento produtor = getAlfabeto().get("<fator>");
		if(tokenAtual.getToken().equals("IDENTIFICADOR")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("NUMERO_INTEIRO")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("NUMERO_REAL")){
			obterSimbolo(produtor);
		}
		else if(tokenAtual.getToken().equals("ABRE_PARENTESES")){
			obterSimbolo(produtor);
			procedimentoExpressao();
			if(tokenAtual.getToken().equals("FECHA_PARENTESES")){
				obterSimbolo(produtor);
			}
			else{
				erro(produtor, "FECHA_PARENTESES");
			}
		}
		else{
			erro(produtor, "IDENTIFICADOR", "NUMERO_INTEIRO", "NUMERO_REAL", "ABRE_PARENTESES");
		}
	}

	public AnalisadorLexico getAnalisadorLexico() {
		return analisadorLexico;
	}

	public List<Token> getTokens() {
		return tokens;
	}
	
	public boolean isPossuiAlgumErro() {
		return possuiAlgumErro;
	}
}