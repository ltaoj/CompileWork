package org.ltj.lexical;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.ltj.parser.PredictWnd;
import org.ltj.parser.SyntacticAnalyzer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class CMainWnd implements Initializable{
	private @FXML Button bt_analyzer;
	private @FXML Button bt_clear;

	private @FXML ScrollPane sp_input;
	private @FXML TextArea ta_input;

	private @FXML ScrollPane sp_result;
	private @FXML TableView<TResult> table_result;
	private ObservableList<TResult> ob_result;
	private String[] COLUMN_RESULT_NAME = {"单词","类别"};
	private String[] COLUMN_RESULT_CELL_VALUE = {"word", "type"};
	private int[] COLUMN_RESULT_SIZE = {100, 100};

	private @FXML ScrollPane sp_error;
	private @FXML TableView table_error;
	private ObservableList ob_error;
	private String[] COLUMN_ERROR_NAME = {"错误代码","说明","行号"};
	private String[] COLUMN_ERROR_CELL_VALUE = {"errorCode","describe","rowNum"};
	private int[] COLUMN_ERROR_SIZE = {200,550,200};

	private SyntacticAnalyzer synAnalyzer;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ob_result = FXCollections.observableArrayList();
		ob_error = FXCollections.observableArrayList();
		initTableColumn(table_result, COLUMN_RESULT_NAME, COLUMN_RESULT_CELL_VALUE, COLUMN_RESULT_SIZE);
		initTableColumn(table_error, COLUMN_ERROR_NAME, COLUMN_ERROR_CELL_VALUE, COLUMN_ERROR_SIZE);
		table_result.setItems(ob_result);
		table_error.setItems(ob_error);

		ta_input.setText("int main(){\n\tint a = 1;\n\tint b = a;\n\tif (a >= b){"
				+ "\n\t\treturn true;\n\t}else{\n\t\treturn false;\n\t}\n\n}");
		new Thread(new Runnable() {

			@Override
			public void run() {
				synAnalyzer = new SyntacticAnalyzer();
			}
		}).start();
	}

	private void initTableColumn(TableView table, String[] column_name, String[] cell_value, int[] column_size){
		table.getColumns().clear();
		for(int i = 0;i < column_name.length;i++){
			TableColumn<TResult, String> column= new TableColumn<TResult, String>(column_name[i]);
			column.setCellValueFactory(new PropertyValueFactory<>(cell_value[i]));
			column.setPrefWidth(column_size[i]);
			table.getColumns().add(column);
		}
	}
	// 点击词法分析按钮事件
	public void OnAnalyzerClicked(){
		clearData();// 清空上次状态
		if(ta_input.getText()==null || ta_input.getText().trim().equals("")){
			// 源代码框为空，对用户进行输入提示
		}else{
			LexicalAnalyzer lexAnalyzer = new LexicalAnalyzer(ta_input.getText(), ob_result, ob_error);
			lexAnalyzer.scanAll();

			ArrayList<String> inputStack = lexAnalyzer.getInputStack();
			SyntacticAnalyzer synAnalyzer = new SyntacticAnalyzer();
			synAnalyzer.analyze(inputStack);
		}

	}

	// 单击清空按钮响应事件
	public void OnClearClicked(){
		clearText();
		clearData();
	}

	public void OnPredictClicked(){
		try {
			new PredictWnd(this).start(new Stage());
		} catch (Exception e) {
			e.printStackTrace();
		};
	}
	// 清空TableView内容
	private void clearData(){
		ob_result.clear();
		ob_error.clear();
	}

	private void clearText(){
		ta_input.setText("");
	}
}
