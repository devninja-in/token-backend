package gs.app.token.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gs.app.token.model.Client;
import gs.app.token.model.Token;
import gs.app.token.service.TokenClientService;

import java.util.Collection;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/clients")
public class TokenAppClientController {

    @Autowired private TokenClientService tokenClientService;

    @RequestMapping(value = "/{clientName}", method = RequestMethod.GET)
    public Client getClient(@PathVariable("clientName") String clientName) {
        return tokenClientService.getClient(clientName);
    }

    @RequestMapping(value = "/refresh/config", method = RequestMethod.POST)
    public void refreshClientConfig() {
        tokenClientService.loadClientConfiguration();
    }

    @RequestMapping(value = "/{clientName}/tokens", method = RequestMethod.GET)
    public Collection<Token> getTokens(@PathVariable("clientName") String clientName) {
        return tokenClientService.getTokens(clientName);
    }

    @RequestMapping(value = "/{clientName}/items/{item}/tokens", method = RequestMethod.GET)
    public Collection<Token> getItemTokens(@PathVariable("clientName") String clientName,
        @PathVariable("item") String item) {
        return tokenClientService.getItemTokens(clientName, item);
    }

    @RequestMapping(value = "/{clientName}/items/{item}/tokens", method = RequestMethod.POST)
    public Token createToken(@PathVariable("clientName") String clientName, @PathVariable("item") String item,
        @RequestBody Map<String, Object> tokenRequest) {

        return tokenClientService.generateToken(clientName, item, tokenRequest);
    }

}
