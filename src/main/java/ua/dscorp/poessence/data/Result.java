package ua.dscorp.poessence.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    private String name;
    private float chaosValue;
    private float divineValue;
    private String detailsId;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getChaosValue() {
        return chaosValue;
    }

    public void setChaosValue(float chaosValue) {
        this.chaosValue = chaosValue;
    }

    public float getDivineValue() {
        return divineValue;
    }

    public void setDivineValue(float divineValue) {
        this.divineValue = divineValue;
    }

    public void setCurrencyTypeName(String currencyTypeName) {
        this.name = currencyTypeName;
    }


    public void setChaosEquivalent(float chaosEquivalent) {
        this.chaosValue = chaosEquivalent;
    }

    public String getDetailsId() {
        return detailsId;
    }

    public void setDetailsId(String detailsId) {
        this.detailsId = detailsId;
    }

    @Override
    public String toString() {
        return "Line{" +
                "name='" + name + '\'' +
                ", chaosValue=" + chaosValue +
                ", divineValue=" + divineValue +
                '}';
    }
}
