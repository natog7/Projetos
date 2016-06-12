package automatos;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AutomatoBase {
	protected Path caminho;
    
    /**
     * M�todo respons�vel por realizar a leitura do automato no modelo <i>.txt</i>.
     */
    protected abstract void lerAutomato();

}
