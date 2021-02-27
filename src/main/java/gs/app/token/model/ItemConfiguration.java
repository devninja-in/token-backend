package gs.app.token.model;

public class ItemConfiguration {

    private String day;
    private String sellStartTime;
    private String sellEndTime;
    private String tokenGenerationStart;
    private String slotDuration;
    private Integer personPerSlot;

    public String getDay() {
        return day;
    }

    public ItemConfiguration setDay(String day) {
        this.day = day;
        return this;
    }

    public String getSellStartTime() {
        return sellStartTime;
    }

    public ItemConfiguration setSellStartTime(String sellStartTime) {
        this.sellStartTime = sellStartTime;
        return this;
    }

    public String getSellEndTime() {
        return sellEndTime;
    }

    public ItemConfiguration setSellEndTime(String sellEndTime) {
        this.sellEndTime = sellEndTime;
        return this;
    }

    public String getTokenGenerationStart() {
        return tokenGenerationStart;
    }

    public ItemConfiguration setTokenGenerationStart(String tokenGenerationStart) {
        this.tokenGenerationStart = tokenGenerationStart;
        return this;
    }

    public String getSlotDuration() {
        return slotDuration;
    }

    public ItemConfiguration setSlotDuration(String slotDuration) {
        this.slotDuration = slotDuration;
        return this;
    }

    public Integer getPersonPerSlot() {
        return personPerSlot;
    }

    public ItemConfiguration setPersonPerSlot(Integer personPerSlot) {
        this.personPerSlot = personPerSlot;
        return this;
    }
}
