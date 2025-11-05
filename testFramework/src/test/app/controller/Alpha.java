package test.app.controller;

import framework.annotation.Controller;
import framework.annotation.Methode;

@Controller
public class Alpha
 {
     @Methode(url = "/alpha/hello")
    public void hello() {}

    @Methode(url = "/alpha/greet")
    public void greet() {}
}
