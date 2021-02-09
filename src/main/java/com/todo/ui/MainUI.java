package com.todo.ui;

import com.todo.app.backend.ToDoRepository;
import com.todo.app.backend.ToDoTask;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.components.DisclosurePanel;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.grid.MGrid;
import org.vaadin.viritin.label.RichText;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * @author amran hossain
 *
 */

@Title("ToDo Task CRUD example")
@Theme("valo")
@SpringUI
public class MainUI extends UI {

    private static final long serialVersionUID = 1L;

    ToDoRepository repo;
    ToDoForm toDoForm;
    EventBus.UIEventBus eventBus;

    private MGrid<ToDoTask> list = new MGrid<>(ToDoTask.class)
            .withProperties("id", "itemName", "description", "date")
            .withColumnHeaders("id", "Item Name", "Description", "Date")
            // not yet supported by V8
            //.setSortableProperties("itemname")
            .withFullWidth();

    private MTextField filterByItemName = new MTextField()
            .withPlaceholder("Filter by Item Name");
    private Button addNew = new MButton(VaadinIcons.PLUS, this::add);
    private Button edit = new MButton(VaadinIcons.PENCIL, this::edit);
    private Button delete = new ConfirmButton(VaadinIcons.TRASH,
            "Are you sure you want to delete the entry?", this::remove);

    public MainUI(ToDoRepository r, ToDoForm f, EventBus.UIEventBus b) {
        this.repo = r;
        this.toDoForm = f;
        this.eventBus = b;
    }

    @Override
    protected void init(VaadinRequest request) {
        DisclosurePanel aboutBox = new DisclosurePanel("ToDo with Vaadin UI", new RichText().withMarkDownResource("/welcome.md"));
        setContent(
                new MVerticalLayout(
                        aboutBox,
                        new MHorizontalLayout(filterByItemName, addNew, edit, delete),
                        list
                ).expand(list)
        );
        listEntities();

        list.asSingleSelect().addValueChangeListener(e -> adjustActionButtonState());
        filterByItemName.addValueChangeListener(e -> {
            listEntities(e.getValue());
        });

        // Listen to change events emitted by ToDoForm see onEvent method
        eventBus.subscribe(this);
    }

    protected void adjustActionButtonState() {
        boolean hasSelection = !list.getSelectedItems().isEmpty();
        edit.setEnabled(hasSelection);
        delete.setEnabled(hasSelection);
    }

    private void listEntities() {
        listEntities(filterByItemName.getValue());
    }

    final int PAGESIZE = 45;

    private void listEntities(String nameFilter) {
        // A dead simple in memory listing would be:
        // list.setRows(repo.findAll());

        // But we want to support filtering, first add the % marks for SQL name query
        String likeFilter = "%" + nameFilter + "%";
        
        list.setDataProvider(
             
                (sortOrder, offset, limit) -> {
                    final int pageSize = limit;
                    final int startPage = (int) Math.floor((double) offset / pageSize);
                    final int endPage = (int) Math.floor((double) (offset + pageSize - 1) / pageSize);
                    final Sort.Direction sortDirection = sortOrder.isEmpty() || sortOrder.get(0).getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC;
                    // fall back to id as "natural order"
                    final String sortProperty = sortOrder.isEmpty() ? "id" : sortOrder.get(0).getSorted();
                    if (startPage != endPage) {
                        List<ToDoTask> page0 = repo.findByItemNameLikeIgnoreCase(likeFilter, PageRequest.of(startPage, pageSize, sortDirection, sortProperty));
                        page0 = page0.subList(offset % pageSize, page0.size());
                        List<ToDoTask> page1 = repo.findByItemNameLikeIgnoreCase(likeFilter, PageRequest.of(endPage, pageSize, sortDirection, sortProperty));
                        page1 = page1.subList(0, limit - page0.size());
                        List<ToDoTask> result = new ArrayList<>(page0);
                        result.addAll(page1);
                        return result.stream();
                    } else {
                        return repo.findByItemNameLikeIgnoreCase(likeFilter, PageRequest.of(startPage, pageSize, sortDirection, sortProperty)).stream();
                    }
                },
                // count fetching strategy
                () -> (int) repo.countByItemNameLikeIgnoreCase(likeFilter)
        );
        adjustActionButtonState();

    }

    public void add(ClickEvent clickEvent) {
        edit(new ToDoTask());
    }

    public void edit(ClickEvent e) {
        edit(list.asSingleSelect().getValue());
    }

    public void remove() {
        repo.delete(list.asSingleSelect().getValue());
        list.deselectAll();
        listEntities();
    }

    protected void edit(final ToDoTask toDoTaskEntry) {
    	toDoForm.setEntity(toDoTaskEntry);
    	toDoForm.openInModalPopup();
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    public void onToDoTaskModified(ToDoTaskModifiedEvent event) {
        listEntities();
        toDoForm.closePopup();
    }

}
