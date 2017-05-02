package org.ltj.parser;

import org.ltj.lexical.CMainWnd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PredictWnd extends Application{

	public static CMainWnd mCMainWnd;

	public PredictWnd(CMainWnd cMainWnd) {
		this.mCMainWnd = cMainWnd;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("FPredictWnd.fxml"));
			Scene scene = new Scene(root,1000, 600);
			primaryStage.setTitle("语法分析过程");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static CMainWnd getCMainWnd(){
		return mCMainWnd;
	}

}
