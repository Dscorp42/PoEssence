package ua.dscorp.poessence.data.containers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ua.dscorp.poessence.data.Line;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinesContainer {
    private List<Line> lines;

    // Getters and Setters
    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }
}
