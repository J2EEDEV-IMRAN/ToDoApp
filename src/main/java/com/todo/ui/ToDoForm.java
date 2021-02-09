package com.todo.ui;

import com.todo.app.backend.ToDoRepository;
import com.todo.app.backend.ToDoTask;
import com.vaadin.data.converter.LocalDateToDateConverter;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;

import org.vaadin.spring.events.EventBus;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

@UIScope
@SpringComponent
public class ToDoForm extends AbstractForm<ToDoTask> {

    private static final long serialVersionUID = 1L;

    EventBus.UIEventBus eventBus;
    ToDoRepository repo;

    TextField itemName = new MTextField("ItemName");
    TextField description = new MTextField("Description");
    DateField date = new DateField("Date");

    ToDoForm(ToDoRepository r, EventBus.UIEventBus b) {
        super(ToDoTask.class);
        this.repo = r;
        this.eventBus = b;

        // On save & cancel, publish events that other parts of the UI can listen
        setSavedHandler(toDoTask -> {
            // persist changes
            repo.save(toDoTask);
            // send the event for other parts of the application
            eventBus.publish(this, new ToDoTaskModifiedEvent(toDoTask));
        });
        setResetHandler(p -> eventBus.publish(this, new ToDoTaskModifiedEvent(p)));

        setSizeUndefined();
    }

    @Override
    protected void bind() {
     
        getBinder()
                .forMemberField(date)
                .withConverter(new LocalDateToDateConverter());
        super.bind();
    }

    @Override
    protected Component createContent() {
        return new MVerticalLayout(
                new MFormLayout(
                		itemName,
                        description,
                        date
                ).withWidth(""),
                getToolbar()
        ).withWidth("");
    }

}
