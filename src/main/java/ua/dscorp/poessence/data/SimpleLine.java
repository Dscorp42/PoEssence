package ua.dscorp.poessence.data;

import java.util.Objects;

public class SimpleLine {
    private String detailsId;
    private String name;
    private String icon;
    private boolean isIncluded;

    public SimpleLine() {
    }

    public SimpleLine(String detailsId, String name, String icon, boolean isIncluded) {
        this.detailsId = detailsId;
        this.name = name;
        this.icon = icon;
        this.isIncluded = isIncluded;
    }

    public String getDetailsId() {
        return detailsId;
    }

    public void setDetailsId(String detailsId) {
        this.detailsId = detailsId;
    }

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

    public boolean getIsIncluded() {
        return isIncluded;
    }

    public void setIsIncluded(boolean isIncluded) {
        this.isIncluded = isIncluded;
    }

    @Override
    public String toString() {
        return "SimpleLine{" +
                "detailsId='" + detailsId + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", isIncluded=" + isIncluded +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleLine that = (SimpleLine) o;
        return Objects.equals(detailsId, that.detailsId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(detailsId);
    }
}
