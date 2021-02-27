package gs.app.token.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class Token {

    private Integer number;
    private String clientName;
    private String itemName;
    private LocalDateTime sellStart;
    private LocalDateTime sellEnd;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private Integer slotTokenNumber;
    private Map<String, Object> fields = new LinkedHashMap<>();

    public Integer getNumber() {
        return number;
    }

    public Token setNumber(Integer number) {
        this.number = number;
        return this;
    }

    public String getClientName() {
        return clientName;
    }

    public Token setClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public String getItemName() {
        return itemName;
    }

    public Token setItemName(String itemName) {
        this.itemName = itemName;
        return this;
    }

    public LocalDateTime getSlotStart() {
        return slotStart;
    }

    public Token setSlotStart(LocalDateTime slotStart) {
        this.slotStart = slotStart;
        return this;
    }

    public LocalDateTime getSlotEnd() {
        return slotEnd;
    }

    public Token setSlotEnd(LocalDateTime slotEnd) {
        this.slotEnd = slotEnd;
        return this;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public Token setFields(Map<String, Object> fields) {
        this.fields = fields;
        return this;
    }

    public Token addField(String name, Object value) {
        this.fields.put(name, value);
        return this;
    }

    public Integer getSlotTokenNumber() {
        return slotTokenNumber;
    }

    public Token setSlotTokenNumber(Integer slotTokenNumber) {
        this.slotTokenNumber = slotTokenNumber;
        return this;
    }

    public LocalDateTime getSellStart() {
        return sellStart;
    }

    public Token setSellStart(LocalDateTime sellStart) {
        this.sellStart = sellStart;
        return this;
    }

    public LocalDateTime getSellEnd() {
        return sellEnd;
    }

    public Token setSellEnd(LocalDateTime sellEnd) {
        this.sellEnd = sellEnd;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Token.class.getSimpleName() + "[", "]").add("number=" + number)
            .add("clientName='" + clientName + "'").add("itemName='" + itemName + "'")
            .add("sellStart=" + sellStart).add("sellEnd=" + sellEnd).add("slotStart=" + slotStart)
            .add("slotEnd=" + slotEnd).add("slotTokenNumber=" + slotTokenNumber).add("fields=" + fields)
            .toString();
    }

    /**
     * @return token values as list
     */
    public List<Object> toList() {
        return Arrays.asList(clientName, itemName, number, sellStart.toString(), sellEnd.toString(),
            slotStart.toString(), slotEnd.toString(), slotTokenNumber,
            StringUtils.isEmpty(fields.get("name")) ? "NA" : fields.get("name"),
            StringUtils.isEmpty(fields.get("mobile")) ? "NA" : fields.get("mobile"),
            StringUtils.isEmpty(fields.get("email")) ? "NA" : fields.get("email"),
            StringUtils.isEmpty(fields.get("building")) ? "NA" : fields.get("building"),
            StringUtils.isEmpty(fields.get("flat")) ? "NA" : fields.get("flat"));
    }
}
