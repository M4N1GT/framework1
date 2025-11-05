package test.app.controller;

import framework.annotation.Controller;
import framework.annotation.Methode;

@Controller
public class Beta
 {
    @Methode(url = "/beta/ping")
    public void ping() {}
}
