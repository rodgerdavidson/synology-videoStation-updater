package org.rodgerdavidson;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;

import java.util.Properties;

public class LoginUtility {
    private final EdgeDriver edge;

    private final String user;
    private final String pw;


    public LoginUtility(EdgeDriver edge, Properties props) {
        this.edge = edge;

        pw = props.getProperty("pw");
        user = props.getProperty("user");
    }


    public void login() {
        enterUserName(user);
        enterPw(pw);
    }

    private void enterPw(String pw) {
        SeleniumRunner.waitForAvailable(By.name("current-password"), edge);
        WebElement pwBox = edge.findElementByName("current-password");
        pwBox.sendKeys(pw);
        edge.findElementByClassName("login-btn").click();
    }


    private void enterUserName(String user) {
        SeleniumRunner.waitForAvailable(By.name("username"), edge);
        WebElement username = edge.findElementByName("username");
        username.clear();
        username.sendKeys(user);
        edge.findElementByClassName("login-btn").click();
    }
}
