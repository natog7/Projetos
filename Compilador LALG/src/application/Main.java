package application;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
	private static Main instance = new Main();
	
	public static Main getInstance() {
		return instance;
	}

	/*
	 * Método responsável por iniciar a janela.
	 */
	@Override
	public void start(Stage primaryStage) {
		Pane pane = null;
		try {
			pane = (Pane) FXMLLoader.load(getClass().getResource("view.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Scene scene = new Scene(pane, 800, 600);
		primaryStage = new Stage();
		primaryStage.setResizable(false);
		primaryStage.setScene(scene);
		primaryStage.show();

		Controlador.getInstance().setMainStage(primaryStage);
		Compilador.getInstance().iniciar();
	}

	/*
	 * Método responsável por iniciar o programa.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
