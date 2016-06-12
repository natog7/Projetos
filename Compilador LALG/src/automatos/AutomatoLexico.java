package automatos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AutomatoLexico extends AutomatoNumero {
	public static final String CARACTERES = "abcdefghijklmnopqrstuvwxyz";
	public static final String SINALIZADOR_ESPACO_BRANCO = "#espaco_branco";
	public static final String SINALIZADOR_NOVA_LINHA = "#nova_linha";
	public static final String SINALIZADOR_TABULACAO = "#tabulacao";
	public static final String SINALIZADOR_NUMERO = "n";
	public static final String SINALIZADOR_CARACTERE = "c";
	public static final String SINALIZADOR_OUTRO = "#outro";
	public static final String SINALIZADOR_NAO_PERTENCE_ALFABETO = "#nao_alfabeto";
	public static final String SINALIZADOR_QUALQUER = "qualquer";
	public static final String SINALIZADOR_QUALQUER_COMENTARIO = "qualquer_comentario";
	public static final String SINALIZADOR_INICIO_COMENTARIO = "{";
	public static final String SINALIZADOR_FINAL_COMENTARIO = "}";
	protected AutomatoNumero automatoNumero;
	protected boolean lendoComentario = false;
	protected List<List<String>> transicoesValidas;
	protected List<List<String>> transicoesFeitas = new ArrayList<List<String>>();
	protected List<String> simbolosRejeitadosTemp = new ArrayList<String>();

	/**
	 * Instancia o aut�mato e seu aut�mato num�rico a partir dos caminhos dos respectivos arquivos em formato <i>txt</i> em que eles est�o contidos.
	 * @param caminhoAutomato Caminho do arquivo do aut�mato.
	 * @param caminhoAutomatoNumerico Caminho do arquivo do aut�mato num�rico.
	 */
	public AutomatoLexico(String caminhoAutomato, AutomatoNumero caminhoAutomatoNumerico) {
        super(caminhoAutomato);
        automatoNumero = caminhoAutomatoNumerico;
    }

	/**
	 * Instancia o aut�mato e seu aut�mato num�rico a partir dos caminhos dos respectivos arquivos em formato <i>txt</i> em que eles est�o contidos.
	 * @param caminhoAutomato Caminho do arquivo do aut�mato.
	 * @param caminhoAutomatoNumerico Caminho do arquivo do aut�mato num�rico.
	 */
	public AutomatoLexico(Path caminhoAutomato, AutomatoNumero caminhoAutomatoNumerico) {
		super(caminhoAutomato);
		automatoNumero = caminhoAutomatoNumerico;
	}

	/**
	 * Verifica se o s�mbolo � um caractere, estando ele em upper ou lower case.
	 * @param simbolo S�mbolo a ser verificado
	 * @return Retorna <i>true</i> se o s�mbolo � um caractere.
	 */
	protected boolean pertenceAosCaracteres(String c) {
		return CARACTERES.contains(c.toLowerCase()) || CARACTERES.toUpperCase().contains(c.toUpperCase());
	}

	/**
	 * Verifica se o s�mbolo � um caractere, estando ele em upper ou lower case.
	 * @param simbolo S�mbolo a ser verificado
	 * @return Retorna <i>true</i> se o s�mbolo � um caractere.
	 */
	protected boolean pertenceAosCaracteres(char simbolo) {
		return pertenceAosCaracteres(String.valueOf(simbolo));
	}

	/**
	 * Verifica se o s�mbolo � um digito ou um caractere.
	 * @param simbolo S�mbolo a ser verificado
	 * @return Retorna <i>true</i> se o s�mbolo � um digito ou um caractere.
	 */
	protected boolean pertenceAosCaracteresOuDigitos(String simbolo) {
		return pertenceAosCaracteres(simbolo) || DIGITOS.contains(simbolo);
	}

	/**
	 * Verifica se o s�mbolo n�o pertence ao alfabeto, nem � um digito e nem um caractere.
	 * @param simbolo S�mbolo a ser verificado
	 * @return Retorna <i>true</i> se o s�mbolo <b>N�O</b> pertence ao alfabeto, nem � um digito e nem um caractere.
	 */
	protected boolean naoPertenceAlfabeto(String simbolo) {
		return !DIGITOS.contains(simbolo) && !pertenceAosCaracteres(simbolo) && !alfabeto.contains(simbolo);
	}

	/**
	 * M�todo que realiza a transi��o do estado atual para o pr�ximo.
	 * @param indice Indice do estado atual
	 * @return Retorna o pr�ximo estado
	 */
	protected String transitarProximoEstado(int indice) {
		transicoesFeitas.add(transicoesValidas.get(indice));
		return transicoesValidas.get(indice).get(2);
	}

	/**
	 * M�todo de inicializa��o do processamento da cadeia pelo automato.
	 */
	@Override
	protected void iniciarProcessoCadeia() {
		super.iniciarProcessoCadeia();
        transicoesFeitas = new ArrayList<List<String>>();
        simbolosRejeitadosTemp = new ArrayList<String>();
	}

    /**
     * Transita com o s�mbolo para o pr�ximo estado caso esta transi��o exista.
     * @param estado Estado atual.
     * @param simbolo Simbolo lido que far� a transi��o.
     * @return Retorna o novo estado para o qual foi transitado.
     */
	@Override
	protected String transicao(String estado, String simbolo) {
        if (estado.equals("ERRO")) {
            throw new IllegalArgumentException("Erro.");
        }
        else if(simbolo.isEmpty()) {
        	throw new IllegalArgumentException("O s�mbolo n�o pode ser vazio!");
        }
        transicoesValidas = new ArrayList<List<String>>();
        for(List<String> lista : transicoes) {
        	if(lista.get(1).equals(estado)) {
        		transicoesValidas.add(lista);
        	}
        }
        String proxEstado = "";
        for (int i = 0; i < transicoesValidas.size();  i++) {
        	//System.out.println("\"" + transicoesValidas.get(i).get(0) + "\" == \"" + c + "\"");
        	if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_ESPACO_BRANCO)) {
        		if(simbolo.equals(" ")) {
        			proxEstado = transitarProximoEstado(i);
                    break;
        		}
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_NOVA_LINHA)) {
        		if(simbolo.equals(SINALIZADOR_NOVA_LINHA)) {
        			proxEstado = transitarProximoEstado(i);
                    break;
        		}
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_TABULACAO)) {
        		if(simbolo.equals(SINALIZADOR_TABULACAO)) {
        			proxEstado = transitarProximoEstado(i);
                    break;
        		}
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_INICIO_COMENTARIO) && simbolo.equals(SINALIZADOR_INICIO_COMENTARIO)) {
        		lendoComentario = true;
    			proxEstado = transitarProximoEstado(i);
                break;
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_FINAL_COMENTARIO) && simbolo.equals(SINALIZADOR_FINAL_COMENTARIO)) {
        		lendoComentario = false;
    			proxEstado = transitarProximoEstado(i);
                break;
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_DIGITO)) {
        		if(DIGITOS.contains(simbolo)) {
        			proxEstado = transitarProximoEstado(i);
                    break;
        		}
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_NUMERO)) {
        		try{
					ResultadoAutomato resultado = automatoNumero.processarCadeia(simbolo);
        			if(resultado.isCadeiaAceita()){
            			proxEstado = transitarProximoEstado(i);
	                    break;
        			}
        		}
        		catch(IllegalArgumentException ex){}
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_CARACTERE)) {
        		if(pertenceAosCaracteres(simbolo)) {
        			proxEstado = transitarProximoEstado(i);
                    break;
        		}
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_OUTRO)) {
        		boolean irProxEstado = true;
        		if(i != transicoesValidas.size() - 1) {
        			List<String> aux = transicoesValidas.get(transicoesValidas.size() - 1);
        			transicoesValidas.set(transicoesValidas.size() - 1, transicoesValidas.get(i));
        			transicoesValidas.set(i, aux);
					irProxEstado = false;
					i--;
        		}
        		else {
        			for(String simboloAux : simbolosRejeitadosTemp) {
        				if(transicoesValidas.get(i).get(0).equals(simboloAux)) {
        					irProxEstado = false;
        				}
        			}
        		}
        		if(irProxEstado) {
        			proxEstado = transitarProximoEstado(i);
                    break;
        		}
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_NAO_PERTENCE_ALFABETO)) {
        		if(naoPertenceAlfabeto(simbolo)) {
        			proxEstado = transitarProximoEstado(i);
                    break;
        		}
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_QUALQUER)) {
    			proxEstado = transitarProximoEstado(i);
                break;
        	}
        	else if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_QUALQUER_COMENTARIO) && lendoComentario) {
        		if(simbolo.equals(SINALIZADOR_FINAL_COMENTARIO)) {
        			continue;
        		}
    			proxEstado = transitarProximoEstado(i);
                break;
        	}
        	else if(transicoesValidas.get(i).get(0).equals(simbolo)) {
    			proxEstado = transitarProximoEstado(i);
                break;
            }
        	else {
        		simbolosRejeitadosTemp.add(transicoesValidas.get(i).get(0));
        	}
        }
        if (proxEstado.equals("ERRO")) {
            throw new IllegalArgumentException("Erro.");
        }
        else if(proxEstado.isEmpty()) {
        	throw new IllegalArgumentException("Transi��o inv�lida.");
        }
        return proxEstado;
	}
}
