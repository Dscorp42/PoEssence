package ua.dscorp.poessence.data.containers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ua.dscorp.poessence.data.CurrencyDetail;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyDetailsContainer {

    private List<CurrencyDetail> currencyDetails;

    // Getters and Setters
    public List<CurrencyDetail> getCurrencyDetails() {
        return currencyDetails;
    }

    public void setCurrencyDetails(List<CurrencyDetail> currencyDetails) {
        this.currencyDetails = currencyDetails;
    }

}
