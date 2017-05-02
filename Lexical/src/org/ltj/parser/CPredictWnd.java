package org.ltj.parser;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class CPredictWnd implements Initializable{

	private @FXML TreeView<String> treeview;
	private @FXML TableView table_content;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		TreeItem<String> root = new TreeItem<String>("预测分析法");
		root.setExpanded(true);
		root.getChildren().addAll(
				new TreeItem<String>("终结符"),
				new TreeItem<String>("非终结符"),
				new TreeItem<String>("first集"),
				new TreeItem<String>("follow集"),
				new TreeItem<String>("预测分析表"));
		treeview.setRoot(root);
		treeview.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		treeview.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue,
					TreeItem<String> newValue) {
				System.out.println(newValue);
			}
		});
	}

}
