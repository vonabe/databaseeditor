package sample;

import javafx.application.Application;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

public class Main extends Application {

    private Connection connection = null;
    private Statement statement = null;

    @Override
    public void start(Stage primaryStage) {

//        ObservableList<TableColumn> tableColumns = FXCollections.observableArrayList(new TableColumn());
//        TableView tableView = new TableView();
//        tableView.itemsProperty().setValue(tableColumns);
//
//        primaryStage.setScene(new Scene(tableView, 900, 600));
//        primaryStage.show();

        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Rik62\\Desktop\\lapki_db.db");
        } catch (SQLException e) {
            e.printStackTrace();
            if (this.connection != null) {
                try {
                    this.connection.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }

        try {
            this.statement = this.connection.createStatement();
//            List<String> columns = getColumnNames("Dogs");
            Map<String, Data> stringDataMap = executeQuery("SELECT * FROM Dogs");

            Iterator<String> iterator = stringDataMap.keySet().iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                Data data = stringDataMap.get(next);
                System.out.println(data.toString());
            }

            offsetIndex(stringDataMap.get("name").data, 355, 16);

            iterator = stringDataMap.keySet().iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                Data data = stringDataMap.get(next);
                System.out.println(data.toString());
            }

            startCommit();

            String sql = "UPDATE Dogs SET id = '%1$s', name = '%2$s' WHERE Dogs.id = '%3$s';";
            List<String> sql_query = new LinkedList<>();

            iterator = stringDataMap.keySet().iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                Data data = stringDataMap.get(next);

                for (int i = 0; i < data.data.size(); i++) {
                    if (next.equals("id")) {
                        sql_query.add(String.format(sql, data.data.get(i), stringDataMap.get("name").data.get(i), i + 1));
                    }
                }
                continue;
            }

            for(String query : sql_query)
                addBatch(query);

            endCommit();


//            if (stringDataMap != null) {
////                startCommit();
//                iterator = stringDataMap.keySet().iterator();
//                while (iterator.hasNext()) {
//                    String next = iterator.next();
//                    Data data = stringDataMap.get(next);
//
////                    addBatch("");
//
////                    TableColumn tableColumn = new TableColumn(data.column_name);
////                    for (String d : data.data)
////                        tableColumn.getColumns().add(new TableColumn<>(d));
////                        tableView.getColumns().add(tableColumn);
////                        tableView.getColumns().add(new TableColumn(data.column_name));
////                    System.out.println(data.toString());
//                }
////                endCommit();
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


//        LinkedList<Boolean> list = new LinkedList<>();
//        list.add(true);
//        list.add(true);
//        list.add(false);
//        list.add(true);
//        list.add(false);
//        list.add(true);
//        Collections.sort(list);
//        System.out.println(Arrays.toString(list.toArray()));

//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
//        primaryStage.initStyle(StageStyle.TRANSPARENT);
//        primaryStage.initModality(Modality.WINDOW_MODAL);
//        StackPane stackPane = new StackPane();
//        primaryStage.setResizable(false);

//        primaryStage.setAlwaysOnTop(true);
//        Text txt = new Text("ИДЕТ КОМПИЛЯЦИЯ");
//        txt.setFont(Font.font("Verdana", 120));
//        txt.setFill(Color.WHITESMOKE);
//        stackPane.getChildren().add(txt);

//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(stackPane, 300, 275, Color.TRANSPARENT));
//        primaryStage.setFullScreen(true);
//        primaryStage.show();

//        primaryStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println(newValue.booleanValue());
//        });
//        try {
//            statement.close();
//            connection.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            System.exit(0);
//        }
    }


    public void offsetIndex(List<String> data, int fromID, int toID) {
        List<String> tmp_data = new LinkedList<>(data);
        String s = data.get(toID);
        data.set(toID, data.get(fromID));
        int offset = -1;
        for (int i = toID; i < data.size()-1; i++) {
            if (i == toID) {
                offset = i+1;
                data.set(i+1, s);
            } else {
                data.set(i+1, tmp_data.get(offset++));
            }
        }
        tmp_data.clear();
    }

    public void startCommit() {
        try {
            statement.getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void addBatch(String sql) {
        try {
            statement.addBatch(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                statement.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void endCommit() {
        try {
            System.out.println("Push");
            statement.executeBatch();
            statement.getConnection().commit();
            statement.getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
//            try {
//                statement.getConnection().rollback();
//            } catch (SQLException e1) {
//                e1.printStackTrace();
//            }
        }finally {
            System.out.println("End Push");
        }
    }

    public List<String> getColumnNames(String table_name) {
        List<String> list_names = new LinkedList<>();
        DatabaseMetaData metaData = null;
        try {
            metaData = this.connection.getMetaData();
            ResultSet dogs = metaData.getColumns(null, null, table_name, null);

            while (dogs.next()) {
                String column_name = dogs.getString("COLUMN_NAME");
                list_names.add(column_name);
            }
        } catch (SQLException e) {
        }
        return list_names;
    }

    /**
     * @param sql Запрос
     * @return Map с именами столбцов Data class в качестве значения хранящее в себе значения строк
     **/
    public Map<String, Data> executeQuery(String sql) {
        Map<String, Data> list = new LinkedHashMap<>();
        ResultSet resultSet = null;
        try {
            resultSet = this.statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                    String tableName = metaData.getTableName(i);
                    String columnName = metaData.getColumnName(i);
                    String string = resultSet.getString(i);
                    System.out.println(columnName + " " + string + " " + tableName + " " + i + "\n");
                    if (list.containsKey(columnName)) {
                        Data data = list.get(columnName);
                        data.addData(string);
                    } else {
                        list.put(columnName, new Data(tableName, columnName, string));
                    }
                }

            }
        } catch (SQLException e) {
            return null;
        }
        return list;
    }

    class Data {

        private String table_name = null;
        private String column_name = null;
        private List<String> data = new LinkedList<>();

        public Data(String table_name, String column_name, String data) {
            this.table_name = table_name;
            this.column_name = column_name;
            this.data.add(data);
        }

        public void addData(String data) {
            this.data.add(data);
        }

        public void setColumnName(String name) {
            column_name = name;
        }

        public String getColumnName() {
            return column_name;
        }

        public List<String> getData() {
            return data;
        }

        public String getTableName() {
            return table_name;
        }

        public void setTableName(String table_name) {
            this.table_name = table_name;
        }

        @Override
        public String toString() {
            return "[TABLE_NAME: " + table_name + ", COLUMN_NAME: " + column_name + ", DATA: " + Arrays.toString(data.toArray()) + "]";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
