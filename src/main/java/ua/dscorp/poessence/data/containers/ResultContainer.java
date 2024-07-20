package ua.dscorp.poessence.data.containers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ua.dscorp.poessence.data.Result;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultContainer {
    private List<Result> result;

    // Getters and Setters
    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }
}
