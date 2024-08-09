package ua.dscorp.poessence.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Line {
    private String name;
    private float chaosValue;
    private float divineValue;
    private String detailsId;
    private String icon;
    private String mapTier;
    private List<BulkItem> bulkItems;
    private List<BulkItem> bulkChaosItems;
    private int ninjaPriceMultiplier = 100;
    private int stackSize;
    private String offers;
    private SparkLine sparkline;

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

    public float getChaosModdedValue() {
        return chaosValue * ninjaPriceMultiplier / 100;
    }

    public float getDivineModdedValue() {
        return divineValue * ninjaPriceMultiplier / 100;
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

    public List<BulkItem> getBulkItems() {
        return bulkItems;
    }

    public void setBulkItems(List<BulkItem> bulkItems) {
        this.bulkItems = bulkItems;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getNinjaPriceMultiplier() {
        return ninjaPriceMultiplier;
    }

    public void setNinjaPriceMultiplier(int ninjaPriceMultiplier) {
        this.ninjaPriceMultiplier = ninjaPriceMultiplier;
    }

    public String getMapTier() {
        return mapTier;
    }

    public void setMapTier(String mapTier) {
        this.mapTier = mapTier;
    }

    public String getOffers() {
        return offers;
    }

    public void setOffers(String offers) {
        this.offers = offers;
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    @Override
    public String toString() {
        return "Line{" +
                "name='" + name + '\'' +
                ", chaosValue=" + chaosValue +
                ", divineValue=" + divineValue +
                '}';
    }

    public List<BulkItem> getBulkChaosItems() {
        return bulkChaosItems;
    }

    public void setBulkChaosItems(List<BulkItem> bulkChaosItems) {
        this.bulkChaosItems = bulkChaosItems;
    }

    public boolean isEmptyNinjaSparkLine() {
        return sparkline.getData().isEmpty();
    }

    public SparkLine getSparkline() {
        return sparkline;
    }

    public void setSparkline(SparkLine sparkline) {
        if (this.sparkline == null)
            this.sparkline = sparkline;
    }

    public SparkLine getReceiveSparkLine() {
        return sparkline;
    }

    public void setReceiveSparkLine(SparkLine sparkline) {
        if (this.sparkline == null)
            this.sparkline = sparkline;
    }
}
