package com.kfwebstandard.selenium.test;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * This is a sample of a test class for Selenium. As we are using NetBeans the
 * class name needs to end in IT so that the Run Selenium command can recognize
 * a selenium test class
 *
 * @author Ken Fogel
 * @version 1.1
 */
public class SeleniumTestIT {

    private WebDriver driver;

    @BeforeClass
    public static void setupClass() {
        // Normally an executable that matches the browser you are using must
        // be in the classpath. The webdrivermanager library by Boni Garcia
        // downloads the required driver and makes it available
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void setupTest() {
        driver = new ChromeDriver();
    }

    /**
     * The most basic Selenium test method that tests to see if the page name
     * matches a specific name.
     *
     * @throws Exception
     */
    @Test
    public void testSimple() throws Exception {
        // And now use this to visit this project
        driver.get("http://localhost:8080/kf_web_standard_project/");

        // Wait for the page to load, timeout after 10 seconds
        WebDriverWait wait = new WebDriverWait(driver, 10);
        // Wait for the page to load, timeout after 10 seconds
        wait.until(ExpectedConditions.titleIs("JPA Displaying Database Tables JPA"));

    }

    @After
    public void shutdownTest() {
        driver.quit();
    }
}
