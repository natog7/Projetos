package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import analisadores.Token;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Controlador implements Initializable {
	private static Controlador instance;
	private static final String VERSAO_PROGRAMA = "1.1c";
	private Stage mainStage;
	private FileChooser fileChooser = new FileChooser();
	private File file;
	private Log log;
	@FXML
	private TextArea editor;
	@FXML
	private Tab editorTab;
	@FXML
	private TableView<Token> saidaAnalise;
	private ObservableList<Token> saidaAnaliseDados = FXCollections.observableArrayList();
	@FXML
	private TableColumn<Token, Integer> saidaAnaliseColunaLinha;
	@FXML
	private TableColumn<Token, Integer> saidaAnaliseColunaColuna;
	@FXML
	private TableColumn<Token, String> saidaAnaliseColunaPalavra;
	@FXML
	private TableColumn<Token, String> saidaAnaliseColunaToken;
	@FXML
	private Tab saidaAnaliseLexicaTab;
	@FXML
	private Tab saidaInformacoes;
	@FXML
	private TextField qtdLinhasText;
	@FXML
	private TextField qtdCaracteresText;
	@FXML
	private TabPane logTabPane;
	@FXML
	private Tab logTab;
	@FXML
	private Tab errosTab;
	@FXML
	private TextArea logTextArea;
	@FXML
	private TextArea errosTextArea;
	@FXML
	private CheckMenuItem clearOnCompileButton;
	@FXML
	private Label linhaText;
	@FXML
	private Label colunaText;
	//private boolean adicionandoNumeroLinhas = false;
	//private List<Integer> numeroLinhasAdicionadas = new ArrayList<Integer>();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		instance = this;
		log = new Log(logTextArea, errosTextArea, true, clearOnCompileButton.isSelected());
		
		clearOnCompileButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			
			@Override
	        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
	        	log.setClearOnCompile(newValue);
            }
        });
		
		editor.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				digitandoTexto(oldValue, newValue);
			}
		});
		
		logTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

			@Override
			public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
				trocarLogTab(oldTab, newTab);
			}
		});
		
		saidaAnalise.setItems(saidaAnaliseDados);
		
		saidaAnaliseColunaLinha.setCellValueFactory(new PropertyValueFactory<Token, Integer>("Linha"));
		saidaAnaliseColunaColuna.setCellValueFactory(new PropertyValueFactory<Token, Integer>("Coluna"));
		saidaAnaliseColunaPalavra.setCellValueFactory(new PropertyValueFactory<Token, String>("Palavra"));
		saidaAnaliseColunaToken.setCellValueFactory(new PropertyValueFactory<Token, String>("Token"));
	}

	/**
	 * Método que tem por função compilar o que estiver na textArea do editor.
	 */
	public void compilar() {
		//saidaAnalise.setText("");
		saidaAnaliseDados.clear();
		log.limparAoCompilar();
		log.log("Compilando...");
		salvar();
		try {
			saidaAnaliseDados.addAll(Compilador.getInstance().compilar());
			saidaAnalise.setItems(saidaAnaliseDados);
			log.log(!Compilador.getInstance().isPossuiAlgumErro() ? "Compilado!" : "Falha ao compilar!");
		}
		catch(IndexOutOfBoundsException ex) {
			log.logErro("Não há nada a ser compilado! O arquivo está vazio.");
		}
		atualizarNumeroMensagensLog();
		saidaAnaliseLexicaTab.setText("Análise Léxica (" + Compilador.getInstance().getAnalisadorSintatico().getTokens().size() + ")");
	}

	/**
	 * Método que realisa a analise léxica do que estiver na textArea do editor.
	 */
	public void analisarLexicamente(){
		saidaAnaliseDados.clear();
		log.limparAoCompilar();
		log.log("Analisando...");
		salvar();
		try {
			saidaAnaliseDados.addAll(Compilador.getInstance().analisarLexicamente());
			saidaAnalise.setItems(saidaAnaliseDados);
			log.log("Analisado!");
		}
		catch(IndexOutOfBoundsException ex) {
			log.logErro("Não há nada a ser analisado! O arquivo está vazio.");
		}
		atualizarNumeroMensagensLog();
		saidaAnaliseLexicaTab.setText("Análise Léxica (" + Compilador.getInstance().getAnalisadorSintatico().getTokens().size() + ")");
	}
	
	public void analisarSintaticamente() {
		saidaAnaliseDados.clear();
		log.limparAoCompilar();
		log.log("Analisando...");
		salvar();
		try {
			saidaAnaliseDados.addAll(Compilador.getInstance().compilar());
			saidaAnalise.setItems(saidaAnaliseDados);
			log.log("Analisado!");
		}
		catch(IndexOutOfBoundsException ex) {
			log.logErro("Não há nada a ser analisado! O arquivo está vazio.");
		}
		atualizarNumeroMensagensLog();
		saidaAnaliseLexicaTab.setText("Análise Léxica (" + Compilador.getInstance().getAnalisadorSintatico().getTokens().size() + ")");
	}

	/**
	 * Método para escolher um arquivo a ser aberto.
	 */
	public void escolherArquivo() {
		fileChooser.setTitle("Selecionar Arquivo");
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documento de Texto (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
        );
		if(new File(System.getProperty("user.dir") + "\\arquivos").exists()) {
			fileChooser.setInitialDirectory(new File(System.getProperty("user.dir") + "\\arquivos"));
		}
		else if(new File(System.getProperty("user.dir")).exists()) {
			fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		}
		else if(new File(System.getProperty("user.home")).exists()) {
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		}
		file = fileChooser.showOpenDialog(getMainStage());
		try {
			Compilador.getInstance().carregarScript(Paths.get(file.getAbsolutePath()));
			try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()))) {
			    editor.setText(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
				log.log("Script \"" + file.getName() + "\" foi aberto.");
			} catch (IOException x) {
			    System.err.format("IOException: %s%n", x);
			}
			try {
				setTituloJanela(file.getName());
				setTextEditorTab(file.getName());
			}
			catch(NullPointerException x2) {
				setTituloJanela();
			}
		}
		catch(NullPointerException ex) { }
		//numeroLinhasAdicionadas.clear();
		//verificarNumerosLinhasAdicionados();
	}

	/**
	 * Método para criar um novo arquivo, porém sem salvá-lo.
	 */
	public void novo(){
		if(!editor.getText().isEmpty()) {
			salvarComo();
		}
		file = null;
		editor.setText("");
		setTituloJanela();
		setTextEditorTab("Novo Script");
		Compilador.getInstance().limparScript();
		log.log("Novo script criado.");
		atualizarNumeroMensagensLog();
		//numeroLinhasAdicionadas.clear();
		//verificarNumerosLinhasAdicionados();
	}

	/**
	 * Método para salvar o arquivo.
	 */
	private void salvarArquivo() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(Files.newBufferedWriter(
					Paths.get(file.getPath())));
			writer.write(editor.getText());
			log.log("Script salvo como \"" + file.getName() + "\".");
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Compilador.getInstance().carregarScript(Paths.get(file.getAbsolutePath()));
		}
		try {
			setTituloJanela(file.getName());
			setTextEditorTab(file.getName());
		}
		catch(NullPointerException x2) {
			setTituloJanela();
		}
		atualizarNumeroMensagensLog();
	}

	/**
	 * Método para salvar um arquivo previamente salvo, ou escolher um lugar para salvá-lo caso ele nunca fora salvo.
	 */
	public void salvar() {
		if(file == null) {
			salvarComo();
			return;
		}
		salvarArquivo();
	}

	/**
	 * Método para salvar o arquivo em determinado lugar.
	 */
	public void salvarComo() {
		fileChooser.setTitle("Salvar Arquivo...");
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documento de Texto (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
        );
		fileChooser.setInitialDirectory(new File(System.getProperty("user.dir") + "\\arquivos"));
		file = fileChooser.showSaveDialog(getMainStage());
		if(file != null) {
			salvarArquivo();
		}
	}

	/**
	 * Método para salvar e, na sequencia, fechar o programa.
	 */
	public void salvarEFechar() {
		if(editor.getText().length() != 0) {
			salvarComo();
		}
		System.exit(0);
	}

	/**
	 * Método para fechar o programa.
	 */
	public void fechar() {
		System.exit(0);
	}

	/**
	 * Método para limpar o log.
	 */
	public void limparLog() {
		log.limpar();
		atualizarNumeroMensagensLog();
	}

	/**
	 * Troca a aba do log, filtrando as mensagens de acordo.
	 * @param oldTab Aba antiga.
	 * @param newTab Aba nova.
	 */
	public void trocarLogTab(Tab oldTab, Tab newTab) {
		switch(newTab.getText()) {
			case "Log":
				log.mostrarTodasMensagens();
				break;
			case "Errors":
				log.mostrarApenasErros();
				break;
		}
	}
	
	/**
	 * Atualiza a quantidade de mensagens existentes mostradas no log.
	 */
	public void atualizarNumeroMensagensLog() {
		logTab.setText("Log " + ((log.getQtdMensagens() > 0) ? "(" + log.getQtdMensagens() + ")" : ""));
		errosTab.setText("Erros " + ((log.getQtdErros() > 0) ? "(" + log.getQtdErros() + ")" : ""));
	}
	
	/*
	public void verificarNumerosLinhasAdicionados() {
		int j = 0;
		try {
			if(editor.getText().substring(0, 2).equals("1.")) {
				numeroLinhasAdicionadas.add(0);
			}
		}
		catch(IndexOutOfBoundsException ex) { }
		for(int k = 0; k < editor.getText().length(); k++) {
			if(String.valueOf(editor.getText().charAt(k)).equals("\n")) {
				j++;
				try {
					if(editor.getText().substring(k, k + 2).equals((j + 1) + ".")) {
						numeroLinhasAdicionadas.add(j);
					}
				}
				catch(IndexOutOfBoundsException ex) { }
			}
		}
	}
	*/

	/**
	 * Método responsável por verificar se o texto foi alterado ou não, dentro da textArea do editor. 
	 * Caso tenha sido alterado, é mostrado a linha e coluna onde ele foi alterado.
	 * @param oldValue Texto antigo.
	 * @param newValue Texto novo.
	 */
	public void digitandoTexto(String oldValue, String newValue) {
		/*if(adicionandoNumeroLinhas) {
			return;
		}*/
		boolean achouAlteracao = false;
		int j = 0;
		int i = 0;
		/*String editorText = editor.getText();
		if(!numeroLinhasAdicionadas.contains(0)) {
			editorText = "1. " + editorText;
			numeroLinhasAdicionadas.add(0);
		}*/
		for(int k = 0; k < oldValue.length() && k < newValue.length(); k++, i++) {
			if(String.valueOf(oldValue.charAt(k)).equals("\n")) {
				j++;
				i = -1;
				/*if(!numeroLinhasAdicionadas.contains(j)) {
					editorText = editorText.substring(0, k + 1) + (j + 1) + ". " + editorText.substring(k + 1, editor.getText().length());
					numeroLinhasAdicionadas.add(j);
				}*/
			}
			if(!oldValue.substring(k, k + 1).equals(newValue.substring(k, k + 1))) {
				linhaText.setText(String.valueOf(j + 1));
				colunaText.setText(String.valueOf(i + 1));
				achouAlteracao = true;
				break;
			}
		}
		if(!achouAlteracao) {
			linhaText.setText(String.valueOf(j + 1));
			colunaText.setText(String.valueOf(i + 1));
		}
		qtdLinhasText.setText("" + (j + 1));
		qtdCaracteresText.setText("" + newValue.length());
		/*adicionandoNumeroLinhas = true;
		editor.setText(editorText);
		adicionandoNumeroLinhas = false;*/
	}

	public Stage getMainStage() {
		return mainStage;
	}
	
	/**
	 * Adiciona algo a mais ao título da janela
	 * @param escritaAdicional String adicional
	 */
	public void setTituloJanela(String escritaAdicional) {
		getMainStage().setTitle("Editor LALG - v" + VERSAO_PROGRAMA + " - " + escritaAdicional);
	}
	
	/**
	 * Restaure o título da janela para o título original
	 */
	public void setTituloJanela() {
		getMainStage().setTitle("Editor LALG - v" + VERSAO_PROGRAMA);
	}
	
	public void setTextEditorTab(String texto) {
		editorTab.setText(texto);
	}

	/**
	 * Guarda e configura a janela passada.
	 * @param mainStage Janela criada na Aplicação principal.
	 */
	public void setMainStage(Stage mainStage) {
		this.mainStage = mainStage;
		
		setTituloJanela();
		this.mainStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
			@Override
			public void handle(WindowEvent e){
				fechar();
			}
		});
	}

	public Log getLog() {
		return log;
	}

	public static Controlador getInstance() {
		return instance;
	}
}
