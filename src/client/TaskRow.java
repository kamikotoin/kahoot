package client;

import javafx.beans.property.*;

public class TaskRow {
    private final int id;
    private final StringProperty text = new SimpleStringProperty();
    private final StringProperty priority = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final BooleanProperty done = new SimpleBooleanProperty();

    public TaskRow(int id, String text, String priority, String category, boolean done) {
        this.id = id;
        this.text.set(text);
        this.priority.set(priority);
        this.category.set(category);
        this.done.set(done);
    }

    public int getId(){ return id; }
    public StringProperty textProperty(){ return text; }
    public StringProperty priorityProperty(){ return priority; }
    public StringProperty categoryProperty(){ return category; }
    public BooleanProperty doneProperty(){ return done; }
}
