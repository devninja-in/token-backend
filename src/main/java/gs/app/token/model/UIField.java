package gs.app.token.model;

public class UIField {

    private String name;
    private String label;
    private String type;
    private Integer maxLength;
    private Boolean required;

    public UIField(String name, String label, String type, Integer maxLength) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.maxLength = maxLength;
    }

    public String getName() {
        return name;
    }

    public UIField setName(String name) {
        this.name = name;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public UIField setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getType() {
        return type;
    }

    public UIField setType(String type) {
        this.type = type;
        return this;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public UIField setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public Boolean getRequired() {
        return required;
    }

    public UIField setRequired(Boolean required) {
        this.required = required;
        return this;
    }
}
