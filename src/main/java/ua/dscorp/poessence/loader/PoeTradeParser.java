package ua.dscorp.poessence.loader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.dscorp.poessence.data.BulkItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PoeTradeParser {

    public static final int MAX_RESULTS = 7;

    public static List<BulkItem> parseResult(String json, String currentAccountName) {

        List<BulkItem> items = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Result result = objectMapper.readValue(json, Result.class);

            List<ResultEntry> entries = result.getResults();
            for (ResultEntry entry : entries) {
                Account account = entry.getListing().getAccount();
                String name = account.getName();
                String status = account.getOnline() != null ? account.getOnline().getStatus() : "offline";
                for (Offer offer : entry.getListing().getOffers()) {
                    double exchangeAmount = offer.getExchange().getAmount();
                    int itemAmount = offer.getItem().getAmount();
                    int stock = offer.getItem().getStock();

                    if (items.size() < MAX_RESULTS) {
                        items.add(new BulkItem(name, status, currentAccountName, exchangeAmount, itemAmount, stock));
                    }
                    else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Poe trade parse failed: " + e.getMessage());
        }
        return items;
    }

    // Class for the Exchange info
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Exchange {
        private String currency;
        private double amount;
        private String whisper;

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getWhisper() {
            return whisper;
        }

        public void setWhisper(String whisper) {
            this.whisper = whisper;
        }
    }

    // Class for the Item info
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String currency;
        private int amount;
        private int stock;
        private String id;
        private String whisper;

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getWhisper() {
            return whisper;
        }

        public void setWhisper(String whisper) {
            this.whisper = whisper;
        }
    }

    // Class for the Offer
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Offer {
        private Exchange exchange;
        private Item item;

        public Exchange getExchange() {
            return exchange;
        }

        public void setExchange(Exchange exchange) {
            this.exchange = exchange;
        }

        public Item getItem() {
            return item;
        }

        public void setItem(Item item) {
            this.item = item;
        }
    }

    // Class for the Account info
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {
        private String name;
        private OnlineStatus online;
        private String lastCharacterName;
        private String language;
        private String realm;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public OnlineStatus getOnline() {
            return online;
        }

        public void setOnline(OnlineStatus online) {
            this.online = online;
        }

        public String getLastCharacterName() {
            return lastCharacterName;
        }

        public void setLastCharacterName(String lastCharacterName) {
            this.lastCharacterName = lastCharacterName;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }
    }

    // Class for the Online Status
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OnlineStatus {
        private String league;
        private String status;

        public String getLeague() {
            return league;
        }

        public void setLeague(String league) {
            this.league = league;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    // Class for the Listing info
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Listing {
        private String indexed;
        private Account account;
        private List<Offer> offers;
        private String whisper;

        public String getIndexed() {
            return indexed;
        }

        public void setIndexed(String indexed) {
            this.indexed = indexed;
        }

        public Account getAccount() {
            return account;
        }

        public void setAccount(Account account) {
            this.account = account;
        }

        public List<Offer> getOffers() {
            return offers;
        }

        public void setOffers(List<Offer> offers) {
            this.offers = offers;
        }

        public String getWhisper() {
            return whisper;
        }

        public void setWhisper(String whisper) {
            this.whisper = whisper;
        }
    }

    // Class for the Result Entry
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultEntry {
        private String id;
        private Listing listing;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Listing getListing() {
            return listing;
        }

        public void setListing(Listing listing) {
            this.listing = listing;
        }
    }

    // Class for the Main Result
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private List<ResultEntry> results;

        @JsonProperty("result")
        public void unpackNestedResults(Map<String, ResultEntry> resultsMap) {
            this.results = new ArrayList<>(resultsMap.values());
        }

        public List<ResultEntry> getResults() {
            return results;
        }

        public void setResults(List<ResultEntry> results) {
            this.results = results;
        }
    }
}
