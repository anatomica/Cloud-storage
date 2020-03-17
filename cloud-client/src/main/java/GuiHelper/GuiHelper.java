package GuiHelper;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class GuiHelper {

    public static void prepareTableView(TableView<FileAbout> tableView) {

        TableColumn<FileAbout, String> columnFileName = new TableColumn<>("Имя Файла");
        columnFileName.setCellValueFactory(new PropertyValueFactory<FileAbout, String>("name"));
        columnFileName.setPrefWidth(180);

        TableColumn<FileAbout, String> columnFileSize = new TableColumn<>("Размер");
        columnFileSize.setCellValueFactory(param -> {
            long size = param.getValue().getSize();
            return new ReadOnlyObjectWrapper<>(size + " bytes");
                });
        columnFileSize.setPrefWidth(113);

        tableView.getColumns().addAll(columnFileName, columnFileSize);
    }
}
