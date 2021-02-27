package gs.app.token.model;

import java.util.ArrayList;
import java.util.Collection;

public class Item {

    private String name;
    private Collection<UIField> uiFields = new ArrayList<>();
    private Collection<String> uniqueFields = new ArrayList<>();
    private Boolean allowDuplicate = Boolean.FALSE;
    private Collection<ItemConfiguration> itemConfigurations = new ArrayList<>();

    public String getName() {
        return name;
    }

    public Item setName(String name) {
        this.name = name;
        return this;
    }

    public Collection<UIField> getUiFields() {
        return uiFields;
    }

    public Item setUiFields(Collection<UIField> uiFields) {
        this.uiFields = uiFields;
        return this;
    }

    public Collection<String> getUniqueFields() {
        return uniqueFields;
    }

    public Item setUniqueFields(Collection<String> uniqueFields) {
        this.uniqueFields = uniqueFields;
        return this;
    }

    public Boolean getAllowDuplicate() {
        return allowDuplicate;
    }

    public Item setAllowDuplicate(Boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
        return this;
    }

    public Collection<ItemConfiguration> getItemConfigurations() {
        return itemConfigurations;
    }

    public Item setItemConfigurations(Collection<ItemConfiguration> itemConfigurations) {
        this.itemConfigurations = itemConfigurations;
        return this;
    }

}
