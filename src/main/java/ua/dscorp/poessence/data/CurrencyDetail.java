package ua.dscorp.poessence.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyDetail {
    private String name;
    private String icon;
    private String tradeId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    @Override
    public String toString() {
        return "CurrencyDetail{" +
                "name='" + name + '\'' +
                ", icon=" + icon +
                '}';
    }
}
