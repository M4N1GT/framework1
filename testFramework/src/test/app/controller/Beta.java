package test.app.controller;

import framework.annotation.Controller;
import framework.annotation.Methode;
import java.util.Map;

@Controller
public class Beta
 {
    @Methode(url = "/beta/ping")
    public void ping() {}

    @Methode(url = "/beta/echo")
    public String echo(Map<String, Object> params) {
        return String.valueOf(params);
    }
}
