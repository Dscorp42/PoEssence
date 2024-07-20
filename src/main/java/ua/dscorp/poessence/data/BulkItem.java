package ua.dscorp.poessence.data;

public class BulkItem {

    private String name;
    private boolean isAfk;
    private boolean isYou;
    private double exchangeAmount;
    private int itemAmount;
    private int stockAmount;

    public BulkItem() {
    }

    public BulkItem(String name, String status, String currentUserName, double exchangeAmount, int itemAmount, int stockAmount) {
        this.name = name;
        this.isAfk = status != null && status.equals("afk");
        this.isYou = name.equals(currentUserName);
        this.exchangeAmount = exchangeAmount;
        this.itemAmount = itemAmount;
        this.stockAmount = stockAmount;
    }

    public String getName() {
        return name;
    }

    public boolean isAfk() {
        return isAfk;
    }

    public boolean isYou() {
        return isYou;
    }

    public double getExchangeAmount() {
        return exchangeAmount;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public int getStockAmount() {
        return stockAmount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAfk(boolean afk) {
        isAfk = afk;
    }

    public void setYou(boolean you) {
        isYou = you;
    }

    public void setExchangeAmount(double exchangeAmount) {
        this.exchangeAmount = exchangeAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    public void setStockAmount(int stockAmount) {
        this.stockAmount = stockAmount;
    }

    @Override
    public String toString() {
        return "BulkItem{" +
                "name='" + name + '\'' +
                ", isAfk=" + isAfk +
                ", isYou=" + isYou +
                ", exchangeAmount=" + exchangeAmount +
                ", itemAmount=" + itemAmount +
                ", stockAmount=" + stockAmount +
                '}';
    }
}
