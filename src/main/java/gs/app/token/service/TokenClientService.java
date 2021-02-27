package gs.app.token.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import gs.app.token.TokenApplication;
import gs.app.token.exception.TokenAppException;
import gs.app.token.exception.TokenAppExceptionCode;
import gs.app.token.helper.TokenGeneratorHelper;
import gs.app.token.model.Client;
import gs.app.token.model.Item;
import gs.app.token.model.Token;
import gs.app.token.repository.TokenRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TokenClientService {

    private static Logger LOGGER = LoggerFactory.getLogger(TokenApplication.class);

    @Autowired private TokenRepository tokenRepository;

    /**
     * @return all clients
     */
    public Collection<Client> getAllClient() {
        return tokenRepository.getAllClient();
    }

    /**
     * @param clientName
     * @return client
     */
    public Client getClient(String clientName) {
        return tokenRepository.getClient(clientName);
    }

    /**
     *
     * @param clientName
     * @return tokens by client name
     */
    public List<Token> getTokens(String clientName) {
        return tokenRepository.getTokens(clientName);
    }

    /**
     * @param clientName
     * @param itemName
     * @return tokens by client name and item
     */
    public List<Token> getItemTokens(String clientName, String itemName) {
        return tokenRepository.getItemTokens(clientName, itemName);
    }

    /**
     * @param clientName
     * @param itemName
     * @param inputFields
     * @return generated token
     */
    public Token generateToken(String clientName, String itemName, Map<String, Object> inputFields) {

        Client client = tokenRepository.getClient(clientName);
        Optional<Item> itemOptional = client.getItems().stream()
            .filter(item -> item.getName().equalsIgnoreCase(itemName)).findFirst();

        if (!itemOptional.isPresent()) {
            throw new TokenAppException(TokenAppExceptionCode.NOT_FOUND,
                String.format("No item exist with name %s", itemName));
        }

        Item item = itemOptional.get();
        List<Token> tokens = tokenRepository.getItemTokens(clientName, itemName);

        if (!item.getAllowDuplicate()) {
            Collection<String> uniqueFields = item.getUniqueFields();
            String inputKey = uniqueFields.stream()
                .map(uniqueField -> inputFields.get(uniqueField).toString()).collect(Collectors.joining(","));
            Optional<Token> tokenOptional = tokens.stream().filter(token -> {
                Map<String, Object> tokenFields = token.getFields();
                return uniqueFields.stream().map(uniqueField -> tokenFields.get(uniqueField).toString())
                    .collect(Collectors.joining(",")).equalsIgnoreCase(inputKey);
            }).findFirst();
            if (tokenOptional.isPresent()) {
                return tokenOptional.get();
            }
        }

        Token lastGeneratedToken = tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);

        Token token = TokenGeneratorHelper.generateToken(clientName, item, lastGeneratedToken);
        token.setFields(inputFields);
        tokens.add(token);
        tokenRepository.insertToken(token);
        return token;
    }

    @Scheduled(fixedRate = 2 * 60 * 60 * 1000)
    public void removeExpiredToken(){
        tokenRepository.removeExpiredToken();
    }

    /**
     *
     */
    public void loadClientConfiguration() {
        tokenRepository.loadClientConfiguration();
    }
}
