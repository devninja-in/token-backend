package gs.app.token.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import gs.app.token.exception.TokenAppException;
import gs.app.token.exception.TokenAppExceptionCode;
import gs.app.token.model.Client;
import gs.app.token.model.Item;
import gs.app.token.model.ItemConfiguration;
import gs.app.token.model.Token;
import gs.app.token.model.UIField;
import gs.app.token.service.GoogleSpreadsheetService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TokenGoogleSpreadsheetRepository implements TokenRepository {

    private static final String LOG_PREFIX = "Token Google Sheet Repository:";
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenGoogleSpreadsheetRepository.class);

    private static final long TOKEN_EXPIRY_PERIOD_AFTER_SELL_END_N_HOURS = 2;
    private static final String UI_FIELDS_SHEET_RANGE = "ui_fields!A2:D";
    private static final String CLIENT_SHEET_RANGE = "clients!A2:L";
    private static final String TOKEN_SHEET_RANGE = "tokens!A2:M";
    private static final Map<String, Client> CLIENT_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, List<Token>>> GENERATED_TOKENS = new ConcurrentHashMap<>();

    private static enum ClientConfigSheetColumn {
        CLIENT_NAME(0), ITEM_NAME(1), DAY_OF_WEEK(2), SELL_START_TIME(3), SELL_END_TIME(
            4), TOKEN_START_BEFOR_N_HOURS(5), SLOT_DURATION(6), PERSON_PER_SLOT(7), UI_FIELDS(
            8), UNIQUE_FIELDS(9), REQUIRED_FIELDS(10), ALLOW_DUPLICATE(11);

        private int index;

        ClientConfigSheetColumn(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private static enum TokenSheetColumn {
        CLIENT_NAME, ITEM_NAME, TOKEN_NUMBER, TOKEN_SELL_START, TOKEN_SELL_END, TOKEN_SLOT_START, TOKEN_SLOT_END, SLOT_TOKEN_NUMBER, NAME_FIELD_VALUE, MOBILE_FIELD_VALUE, EMAIL_FIELD_VALUE, BUILDING_FIELD_VALUE, FLAT_FIELD_VALUE;

    }

    private @Autowired GoogleSpreadsheetService googleSpreadsheetService;
    private @Value("${client_detail_sheet_id}") String clientDetailSheetId;
    private @Value("${token_sheet_id}") Integer tokenSheetId;

    @Override
    public List<Token> getTokens(String clientName) {
        Map<String, List<Token>> itemTokens = GENERATED_TOKENS.get(clientName);
        if (itemTokens == null) {
            throw new TokenAppException(TokenAppExceptionCode.NOT_FOUND,
                String.format("No token found for" + " %s", clientName));
        }
        List<Token> tokens = new ArrayList<>();
        itemTokens.values().stream().forEach(tokensByItem -> tokens.addAll(tokensByItem));
        return tokens;
    }

    @Override
    public List<Token> getItemTokens(String clientName, String itemName) {
        Map<String, List<Token>> itemTokens = GENERATED_TOKENS.get(clientName);
        if (itemTokens == null) {
            throw new TokenAppException(TokenAppExceptionCode.NOT_FOUND,
                String.format("No token found for" + " %s", clientName));
        }
        List<Token> tokens = itemTokens.get(itemName);
        if (tokens == null) {
            throw new TokenAppException(TokenAppExceptionCode.NOT_FOUND,
                String.format("No token found for" + " %s", clientName));
        }

        if (tokens == null) {
            tokens = new ArrayList<>();
            GENERATED_TOKENS.get(clientName).put(itemName, tokens);
        }

        return tokens;
    }

    @Override
    public Collection<Client> getAllClient() {
        return Collections.unmodifiableCollection(CLIENT_MAP.values());
    }

    @Override
    public Client getClient(String clientName) {
        Client client = CLIENT_MAP.get(clientName);
        if (client == null) {
            throw new TokenAppException(TokenAppExceptionCode.NOT_FOUND,
                String.format("No client exist with name %s", clientName));
        }
        //TODO: return copy
        return client;
    }

    @Override
    public void insertToken(Token token) {
        try {
            googleSpreadsheetService.addRow(clientDetailSheetId, TOKEN_SHEET_RANGE, token.toList());
        }
        catch (Exception e) {
            LOGGER.error("Failed to store generated token {}", token, e);
            throw new TokenAppException(TokenAppExceptionCode.TOKEN_INSERT_FAILED,
                String.format("Failed to store generated token %s", token));
        }
    }

    @Override
    public void removeExpiredToken() {
        try {
            List<Token> validTokes = new ArrayList<>();
            List<Token> inValidTokes = new ArrayList<>();
            GENERATED_TOKENS.entrySet().stream().forEach(tokensByClientAndItems -> {
                tokensByClientAndItems.getValue().entrySet().stream().forEach(tokensByItem -> {
                    List<Token> tokensToRemove = tokensByItem.getValue().stream().filter(
                        token -> token.getSellEnd().plusHours(TOKEN_EXPIRY_PERIOD_AFTER_SELL_END_N_HOURS)
                            .isBefore(LocalDateTime.now())).collect(Collectors.toList());
                    LOGGER.info("Removing {} expiry tokens", tokensToRemove);

                    if (!tokensToRemove.isEmpty()) {
                        inValidTokes.addAll(tokensToRemove);
                    }
                    tokensByItem.getValue().removeAll(tokensToRemove);
                    validTokes.addAll(tokensByItem.getValue());
                });
            });

            if (inValidTokes.size() > 0) {
                googleSpreadsheetService.deleteRows(clientDetailSheetId, tokenSheetId, 1,
                    validTokes.size() + inValidTokes.size() + 1);
                googleSpreadsheetService.addRows(clientDetailSheetId, TOKEN_SHEET_RANGE,
                    validTokes.stream().map(Token::toList).collect(Collectors.toList()));
            }
            else {
                LOGGER.info("No token entry to delete from sheet");
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to complete token clean up jobs", e);
        }

    }

    @Override
    public void loadClientConfiguration() throws TokenAppException {
        Map<String, UIField> uiFields = loadUIFields();
        try {
            CLIENT_MAP.clear();
            List<List<Object>> clientsData = googleSpreadsheetService
                .getSpreadSheetData(clientDetailSheetId, CLIENT_SHEET_RANGE);
            clientsData.stream().forEach(row -> {
                String clientName = row.get(ClientConfigSheetColumn.CLIENT_NAME.index).toString();
                Client client = CLIENT_MAP.get(clientName);
                if (client == null) {
                    client = new Client().setName(clientName);
                    CLIENT_MAP.put(clientName, client);
                }
                Item item = null;
                String itemName = row.get(ClientConfigSheetColumn.ITEM_NAME.index).toString();
                Optional<Item> itemOptional = client.getItems().stream()
                    .filter(i -> i.getName().equalsIgnoreCase(itemName)).findFirst();
                if (itemOptional.isPresent()) {
                    item = itemOptional.get();
                }
                else {
                    item = new Item().setName(itemName);
                    client.getItems().add(item);
                }

                List<String> requiredFields = Arrays
                    .stream(row.get(ClientConfigSheetColumn.REQUIRED_FIELDS.index).toString().split(","))
                    .filter(requiredField -> !StringUtils.isEmpty(requiredField))
                    .collect(Collectors.toList());

                item.setUiFields(
                    Arrays.stream(row.get(ClientConfigSheetColumn.UI_FIELDS.index).toString().split(","))
                        .map(uiField -> uiFields.get(uiField).setRequired(requiredFields.contains(uiField)))
                        .collect(Collectors.toList()));
                item.setUniqueFields(
                    Arrays.stream(row.get(ClientConfigSheetColumn.UNIQUE_FIELDS.index).toString().split(","))
                        .filter(uiField -> !StringUtils.isEmpty(uiField)).collect(Collectors.toList()));

                item.setAllowDuplicate(
                    Boolean.valueOf(row.get(ClientConfigSheetColumn.ALLOW_DUPLICATE.index).toString()));

                ItemConfiguration itemConfiguration = null;
                String dayOfWeek = row.get(ClientConfigSheetColumn.DAY_OF_WEEK.index).toString();
                Optional<ItemConfiguration> itemConfigurationOptional = item.getItemConfigurations().stream()
                    .filter(ic -> ic.getDay().equalsIgnoreCase(dayOfWeek)).findFirst();

                if (itemConfigurationOptional.isPresent()) {
                    itemConfiguration = itemConfigurationOptional.get();
                }
                else {
                    itemConfiguration = new ItemConfiguration();
                    item.getItemConfigurations().add(itemConfiguration);

                }
                itemConfiguration.setDay(dayOfWeek)
                    .setSellStartTime(row.get(ClientConfigSheetColumn.SELL_START_TIME.index).toString())
                    .setSellEndTime(row.get(ClientConfigSheetColumn.SELL_END_TIME.index).toString())
                    .setTokenGenerationStart(
                        row.get(ClientConfigSheetColumn.TOKEN_START_BEFOR_N_HOURS.index).toString())
                    .setSlotDuration(row.get(ClientConfigSheetColumn.SLOT_DURATION.index).toString())
                    .setPersonPerSlot(
                        Integer.parseInt(row.get(ClientConfigSheetColumn.PERSON_PER_SLOT.index).toString()));

            });

            loadGeneratedTokens();

        }
        catch (Exception e) {
            LOGGER.error("Failed to load client configuration", e);
            throw new TokenAppException(TokenAppExceptionCode.CLIENT_CONFIGURATION_LOAD_FAILED,
                e.getMessage());
        }
    }

    /**
     * load and cache generated tokens
     */
    private void loadGeneratedTokens() {
        try {
            GENERATED_TOKENS.clear();

            Collection<Client> clients = CLIENT_MAP.values();

            clients.forEach(client -> {
                Map<String, List<Token>> itemTokens = new ConcurrentHashMap<>();
                client.getItems().stream().forEach(item -> itemTokens.put(item.getName(), new ArrayList<>()));
                GENERATED_TOKENS.put(client.getName(), itemTokens);
            });

            List<List<Object>> tokensData = googleSpreadsheetService
                .getSpreadSheetData(clientDetailSheetId, TOKEN_SHEET_RANGE);
            tokensData.stream().forEach(row -> {

                String clientName = row.get(TokenSheetColumn.CLIENT_NAME.ordinal()).toString();

                Map<String, List<Token>> itemTokens = GENERATED_TOKENS.get(clientName);

                String itemName = row.get(TokenSheetColumn.ITEM_NAME.ordinal()).toString();
                List<Token> tokens = itemTokens.get(itemName);

                tokens.add(new Token().setClientName(clientName).setItemName(itemName)
                    .setNumber(Integer.parseInt(row.get(TokenSheetColumn.TOKEN_NUMBER.ordinal()).toString()))
                    .setSellStart(LocalDateTime
                        .parse(row.get(TokenSheetColumn.TOKEN_SELL_START.ordinal()).toString(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME)).setSellEnd(LocalDateTime
                        .parse(row.get(TokenSheetColumn.TOKEN_SELL_END.ordinal()).toString(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME)).setSlotStart(LocalDateTime
                        .parse(row.get(TokenSheetColumn.TOKEN_SLOT_START.ordinal()).toString(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME)).setSlotEnd(LocalDateTime
                        .parse(row.get(TokenSheetColumn.TOKEN_SLOT_END.ordinal()).toString(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME)).setSlotTokenNumber(
                        Integer.parseInt(row.get(TokenSheetColumn.SLOT_TOKEN_NUMBER.ordinal()).toString()))
                    .addField("name", row.get(TokenSheetColumn.NAME_FIELD_VALUE.ordinal()).toString())
                    .addField("mobile", row.get(TokenSheetColumn.MOBILE_FIELD_VALUE.ordinal()).toString())
                    .addField("email", row.get(TokenSheetColumn.EMAIL_FIELD_VALUE.ordinal()).toString())
                    .addField("building", row.get(TokenSheetColumn.BUILDING_FIELD_VALUE.ordinal()).toString())
                    .addField("flat", row.get(TokenSheetColumn.FLAT_FIELD_VALUE.ordinal()).toString()));
            });
            removeExpiredToken();
        }
        catch (Exception e) {
            LOGGER.error("Failed to load generated tokens", e);
            throw new TokenAppException(TokenAppExceptionCode.TOKEN_LOAD_FAILED, e.getMessage());
        }
    }

    /**
     * @return UI field configuration
     */
    private Map<String, UIField> loadUIFields() {
        try {
            List<List<Object>> uiFieldsData = googleSpreadsheetService
                .getSpreadSheetData(clientDetailSheetId, UI_FIELDS_SHEET_RANGE);
            return uiFieldsData.stream().map(
                row -> new UIField(row.get(0).toString(), row.get(1).toString(), row.get(2).toString(),
                    Integer.parseInt(row.get(3).toString())))
                .collect(Collectors.toMap(UIField::getName, uiField -> uiField));
        }
        catch (Exception e) {
            LOGGER.error("Failed to load fields configuration", e);
            throw new TokenAppException(TokenAppExceptionCode.FIELD_CONFIGURATION_LOAD_FAILED,
                e.getMessage());
        }
    }

}
