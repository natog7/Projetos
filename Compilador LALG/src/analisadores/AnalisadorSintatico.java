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
	 * M�todo que pega o pr�ximo token, caso n�o tenha, � acusado erro de fim do script.
	 * E caso ocorra um erro na an�lixe l�xica, esse � informado ao usu�rio.
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
	 * Pega todos os tokens que o analisador l�xico disponibiliza, preenchendo a lista de tokens.
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
	 * M�todo para pegar as informa��es de todos os tokens existentes.
	 * @return Informa��es dos tokens
	 */
	public String getTokensInfo() {
		String analise = "";
		for(Token token : tokens) {
			analise += token.toString() + "\n";
		}
		return analise;
	}

	/**
	 * Avan�a o token atual para o pr�ximo token da lista de tokens retornada pelo analisador l�xico.
	 * @param produtorAtual S�mbolo n�o-terminal que est� chamando esse m�todo.
	 */
	protected void obterSimbolo(Elemento produtorAtual) {
		tokenAtualIndice++;
		try {
			tokenAtual = tokens.get(tokenAtualIndice);
		}
		catch(IndexOutOfBoundsException ex) {
			// Fim do script alcan�ado
			if(produtorAtual.getElemento().equals("<programa>")) {
				throw new FimDoScriptException();
			}
		}
	}

	/**
	 * Avan�a o token atual para o pr�ximo token da lista de tokens retornada pelo analisador l�xico.
	 */
	protected void obterSimbolo() {
		tokenAtualIndice++;
		try {
			tokenAtual = tokens.get(tokenAtualIndice);
		}
		catch(IndexOutOfBoundsException ex) {
			// Fim do script alcan�ado
			throw new FimDoScriptException();
		}
	}

	/**
	 * Busca tokens de sincroniza��o a fim de retomar a an�lise sint�tica em um ponto seguro.
	 * @param sincronizadores S�mbolos sincronizadores adicionais.
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
			Controlador.getInstance().getLog().logErro("Falha ao retomar a an�lise sint�tica a partir do token " + tokenErrado + ".");
		}
	}

	/**
	 * Tenta retomar a an�lise a partir do token que ocorreu o erro sint�tico utilizando os tokens/s�mbolos de sincroniza��o.
	 * <br>Quando a an�lise for retomada o �ndice do token atual � atualizado para a localiza��o do token que obteve sucesso em retomar a an�lise.
	 * @param sincronizadores S�mbolos sincronizadores.
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
		// Caso a retomada da an�lise tenha alcan�ado o �ltimo token
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
	 * Alerta o erro ao usu�rio e tenta retomar a an�lise sint�tica.
	 * @param produtor S�mbolo n�o-terminal que est� chamando esse m�todo.
	 * @param tokensEsperados Tokens/s�mbolos que s�o esperado no lugar do token que ocasionou o erro.
	 */
	protected void erro(Elemento produtor, String... tokensEsperados) {
		erro(produtor, Arrays.asList(), tokensEsperados);
	}

	/**
	 * Alerta o erro ao usu�rio e tenta retomar a an�lise sint�tica.
	 * @param produtor S�mbolo n�o-terminal que est� chamando esse m�todo.
	 * @param sincronizadores Lista de s�mbolos sincronizadores adicionais.
	 * @param tokensEsperados Tokens/s�mbolos que s�o esperado no lugar do token que ocasionou o erro.
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
	 * Alerta o erro ao usu�rio.
	 */
	protected void alertarErroSintatico() {
		possuiAlgumErro = true;
		Controlador.getInstance().getLog().logErro(new AnaliseSintaticaException("Erro no s�mbolo [" + tokenAtual.getToken() + " = " + tokenAtual.getPalavra() + 
				"], � esperado um dos seguintes s�mbolos: " + tokensEsperadosString.toString() + ".", tokenAtual));
		/*Controlador.getInstance().getLog().logErro(new AnaliseSintaticaException("Erro no s�mbolo [" + tokenAtual.getToken() + " = " + tokenAtual.getPalavra() + 
				"], com o pai [" + tokenPai + "], � esperado um dos seguintes s�mbolos: " + tokensEsperadosString.toString() + ".", tokenAtual));*/
	}

	/**
	 * Reinicia as configura��es para uma pr�xima an�lise sint�tica.
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
			// An�lise finalizada com sucesso sem nenhum erro
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "programa".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "corpo".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "dc".
	 */
	protected void procedimentoDeclaracao() {
		Elemento produtor = getAlfabeto().get("<dc>");
		procedimentoDeclaracaoVariaveis();
		procedimentoDeclaracaoProcedimento();
	}

	/**
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "dc_v".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "tipo_var".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "variaveis".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "mais_var".
	 */
	protected void procedimentoMaisVar() {
		Elemento produtor = getAlfabeto().get("<mais_var>");
		if(tokenAtual.getToken().equals("VIRGULA")){
			obterSimbolo(produtor);
			procedimentoVariaveis();
		}
	}

	/**
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "dc_p".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "parametros".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "lista_par".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "mais_par".
	 */
	protected void procedimentoMaisParametros() {
		Elemento produtor = getAlfabeto().get("<mais_par>");
		if(tokenAtual.getToken().equals("PONTO_VIRGULA")){
			obterSimbolo(produtor);
			procedimentoListaParametros();
		}
	}

	/**
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "corpo_p".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "dc_loc".
	 */
	protected void procedimentoDeclaracaoLocal() {
		Elemento produtor = getAlfabeto().get("<dc_loc>");
		procedimentoDeclaracaoVariaveis();
	}

	/**
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "lista_arg".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "argumentos".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "mais_ident".
	 */
	protected void procedimentoMaisIdent() {
		Elemento produtor = getAlfabeto().get("<mais_ident>");
		if(tokenAtual.getToken().equals("PONTO_VIRGULA")){
			obterSimbolo(produtor);
			procedimentoArgumentos();
		}
	}

	/**
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "pfalsa".
	 */
	protected void procedimentoPFalsa() {
		Elemento produtor = getAlfabeto().get("<pfalsa>");
		if(tokenAtual.getToken().equals("ELSE")){
			obterSimbolo(produtor);
			procedimentoCmd();
		}
	}

	/**
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "comandos".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "cmd".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "cmdaux".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "condicao".
	 */
	protected void procedimentoCondicao() {
		Elemento produtor = getAlfabeto().get("<condicao>");
		procedimentoExpressao();
		procedimentoRelacao();
		procedimentoExpressao();
	}

	/**
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "relacao".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "expressao".
	 */
	protected void procedimentoExpressao() {
		Elemento produtor = getAlfabeto().get("<expressao>");
		procedimentoTermo();
		procedimentoOutrosTermos();
	}

	/**
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "op_un".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "outros_termos".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "op_ad".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "termo".
	 */
	protected void procedimentoTermo() {
		Elemento produtor = getAlfabeto().get("<termo>");
		procedimentoOpUn();
		procedimentoFator();
		procedimentoMaisFatores();
	}

	/**
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "mais_fatores".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "op_mult".
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
	 * M�todo equivalente a regra de produ��o do simbolo n�o-terminal "fator".
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