package gs.app.token.repository;

import gs.app.token.model.Client;
import gs.app.token.model.Token;

import java.util.Collection;
import java.util.List;

public interface TokenRepository {

    /**
     *
     * @param clientName
     * @return all generated tokens by client names
     */
    List<Token> getTokens(String clientName);
    /**
     *
     * @param clientName
     * @param itemName
     * @return list of generated token by client name and item
     */
    List<Token> getItemTokens(String clientName, String itemName);

    /**
     *
     * @return all clients
     */
    Collection<Client> getAllClient();

    /**
     *
     * @param clientName
     * @return client by name
     */
    Client getClient(String clientName);

    /**
     *
     * @param token
     */
    void insertToken(Token token);

    /**
     * remove expired tokens
     */
    void removeExpiredToken();

    /**
     * load client configuration
     */
    void loadClientConfiguration();
}
