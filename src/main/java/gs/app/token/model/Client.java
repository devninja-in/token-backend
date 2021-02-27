package gs.app.token.model;

import java.util.ArrayList;
import java.util.Collection;

public class Client {

    private String name;
    private Collection<Item> items = new ArrayList<>();

    public String getName() {
        return name;
    }

    public Client setName(String name) {
        this.name = name;
        return this;
    }

    public Collection<Item> getItems() {
        return items;
    }

    public Client setItems(Collection<Item> items) {
        this.items = items;
        return this;
    }
}
