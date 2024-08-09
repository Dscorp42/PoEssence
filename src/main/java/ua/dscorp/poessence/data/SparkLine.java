package ua.dscorp.poessence.data;

import java.util.List;

public class SparkLine {
    private List<String> data;
    private String totalChange;

    public String getTotalChange() {
        return totalChange;
    }

    public void setTotalChange(String totalChange) {
        this.totalChange = totalChange;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
