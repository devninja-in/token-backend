package gs.app.token.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/token")
public class TokenAppStatusController {

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public String health(){
        return "OK";
    }

}
