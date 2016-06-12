package automatos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AutomatoNumero extends Automato {
	public static final String DIGITOS = "0123456789";
	public static final String SINALIZADOR_DIGITO = "d";

	/**
	 * Instancia o autômato a partir do caminho do arquivo em formato <i>txt</i> em que ele está contido.
	 * @param caminhoAutomato Caminho do arquivo do autômato.
	 */
	public AutomatoNumero(String caminhoAutomato) {
        super(caminhoAutomato);
    }

	/**
	 * Instancia o autômato a partir do caminho do arquivo em formato <i>txt</i> em que ele está contido.
	 * @param caminhoAutomato Caminho do arquivo do autômato.
	 */
	public AutomatoNumero(Path caminhoAutomato) {
		super(caminhoAutomato);
	}

    /**
     * Transita com o símbolo para o próximo estado caso esta transição exista.
     * @param estado Estado atual.
     * @param simbolo Simbolo lido que fará a transição.
     * @return Retorna o novo estado para o qual foi transitado.
     */
	@Override
	protected String transicao(String estado, String simbolo) {
        if (estado.equals("ERRO")) {
            throw new IllegalArgumentException("Erro.");
        }
        else if(!alfabeto.contains(simbolo) && !DIGITOS.contains(simbolo)) {
        	throw new IllegalArgumentException("Caractere não pertence ao alfabeto.");
        }
        List<List<String>> transicoesValidas = new ArrayList<List<String>>();
        for(List<String> lista : transicoes) {
        	if(lista.get(1).equals(estado)) {
        		transicoesValidas.add(lista);
        	}
        }
        String proxEstado = "";
        for (int i = 0; i < transicoesValidas.size();  i++) {
        	if(transicoesValidas.get(i).get(0).equals(SINALIZADOR_DIGITO)) {
        		if(DIGITOS.contains(simbolo)) {
                    proxEstado = transicoesValidas.get(i).get(2);
                    break;
        		}
        	}
        	else if(transicoesValidas.get(i).get(0).equals(simbolo)) {
                proxEstado = transicoesValidas.get(i).get(2);
                break;
            }
        }
        if (proxEstado.equals("ERRO")) {
            throw new IllegalArgumentException("Erro.");
        }
        else if(proxEstado.isEmpty()) {
        	throw new IllegalArgumentException("Transição inválida.");
        }
        return proxEstado;
	}
	
}
