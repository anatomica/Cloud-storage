package Lesson2.Netty.common;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

public class CreateTable extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Transposed tableView
        TableColumn<MyField, String> myFieldNameColumn = new TableColumn<>("Name");
        myFieldNameColumn.setCellValueFactory(param -> param.getValue().name);

        TableColumn<MyField, String> myFieldValueColumn = new TableColumn<>("Value");
        myFieldValueColumn.setCellValueFactory(param -> param.getValue().value);

        TableView<MyField> myFieldTableView = new TableView<>();
        myFieldTableView.getColumns().addAll(
                myFieldNameColumn,
                myFieldValueColumn
        );

        transporter = new Transporter<>(myFieldTableView.getItems());
        mapper = new PersonMapper();

        // Source tableView
        TableColumn<Person, Long> personIdColumn = new TableColumn<>("Id");
        personIdColumn.setCellValueFactory(param -> param.getValue().id);

        TableColumn<Person, String> personFullNameColumn = new TableColumn<>("Full name");
        personFullNameColumn.setCellValueFactory(param -> param.getValue().fullName);

        TableColumn<Person, LocalDate> personBirthdayColumn = new TableColumn<>("Birthday");
        personBirthdayColumn.setCellValueFactory(param -> param.getValue().birthday);

        TableColumn<Person, Boolean> personArchiveColumn = new TableColumn<>("Archive");
        personArchiveColumn.setCellValueFactory(param -> param.getValue().archive);


        TableView<Person> personTableView = new TableView<>();
        personTableView.getSelectionModel().selectedItemProperty().addListener(this::transposition);
        personTableView.getColumns().addAll(
                personIdColumn,
                personFullNameColumn,
                personBirthdayColumn,
                personArchiveColumn
        );

        // Make scene
        SplitPane splitPane = new SplitPane(myFieldTableView, personTableView);
        splitPane.setOrientation(Orientation.VERTICAL);

        primaryStage.setScene(new Scene(splitPane, 300, 275));
        primaryStage.show();

        initData(personTableView.getItems());
    }

    // Заполение данных для теста
    private void initData(ObservableList<Person> items) {
        Person person = new Person();
        person.id.set(1L);
        person.fullName.set("Ivan");
        person.birthday.set(LocalDate.now());
        person.archive.set(false);

        Person person1 = new Person();
        person1.id.set(2L);
        person1.fullName.set("Petr");
        person1.birthday.set(LocalDate.now());
        person1.archive.set(true);

        Person person2 = new Person();
        person2.id.set(3L);
        person2.fullName.set("Serg");
        person2.birthday.set(LocalDate.now());
        person2.archive.set(false);

        Person person3 = new Person();
        person3.id.set(4L);
        person3.fullName.set("Juli");
        person3.birthday.set(LocalDate.now());
        person3.archive.set(true);

        items.addAll(person, person1, person2, person3);
    }

    private Transporter<Person> transporter;
    private BiConsumer<Person, List<MyField>> mapper;

    // Реакция на выделение записи в нижней таблице
    private void transposition(ObservableValue<? extends Person> observable, Person oldValue, Person newValue) {
        transporter.transport(newValue, mapper);
    }

    /**
     * В качесте аргумента использует список (не TableView, что бы не привязываться к GUI)
     * @param <T> Тип переносимой сущности
     */
    class Transporter<T> {

        private final List<MyField> targetList;

        public Transporter(List<MyField> targetList) {
            this.targetList = targetList;
        }

        public void transport(T value, BiConsumer<T, List<MyField>> consumer) {
            targetList.clear();
            consumer.accept(value, targetList);
        }

    }

    /**
     * Транспонирование есть представление объекта в список его полей. Для этого нужен какой нибудь объект.
     * Можно усложнить объект - сохраняя информацию о типе поля
     */
    class MyField {

        StringProperty name;
        StringProperty value;

        public MyField(Property property) {
            this.name = new SimpleStringProperty(property.getName());

            Object propValue = property.getValue();
            this.value = new SimpleStringProperty(propValue != null ? propValue.toString() : null);
        }

    }


    /**
     * Произвольный объект отображаемый в таблице
     * В данном примере важно указание наименования поля (можно без него, если изменить логику формирования объекта
     * MyField - например, через reflection api)
     */
    class Person {
        ObjectProperty<Long> id = new SimpleObjectProperty<>(this, "id");
        StringProperty fullName = new SimpleStringProperty(this, "fullName");
        ObjectProperty<LocalDate> birthday = new SimpleObjectProperty<>(this, "birthday");
        BooleanProperty archive = new SimpleBooleanProperty(this, "archive");
    }

    /**
     * Конверторы/потребители для соответствующиего типа.
     * Так же можно описать единый через reflection api, но это может не соответствовать какой либо логике.
     *
     * Возможен вариант не через BiConsumer, а например через Callback/Function. В этом случае будут пораждаться
     * временные списки, особой необходимости в рамках данной задачи нет, поэтому не стоит напрягать gc лишний раз)
     */
    class PersonMapper implements BiConsumer<Person, List<MyField>> {
        @Override
        public void accept(Person person, List<MyField> items) {
            items.add(new MyField(person.id));
            items.add(new MyField(person.fullName));
            items.add(new MyField(person.birthday));
            items.add(new MyField(person.archive));
        }
    }

}