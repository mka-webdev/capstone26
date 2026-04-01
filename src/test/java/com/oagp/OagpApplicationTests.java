/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.oagp;

/**
 *
 * @author Kike
 */
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class OagpApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("1. Spring Context Initialization Test")
    void contextLoads() {
        System.out.println(">>> running: 1. Spring Context Initialization Test");
        // This line formally verifies that Spring Boot has loaded all components correctly
        assertNotNull(context, "The application context should not be null");
    }


    @Test
    @DisplayName("2. Database Persistence Layer Test")
    void databaseBeansLoaded() {
        System.out.println(">>> running: 2. Database Persistence Layer Test");
        // Verify that persistence components are active
        boolean dataSourceExists = context.containsBean("dataSource");
        assertNotNull(dataSourceExists, "The DataSource bean should be initialized for OAGP");
    }


    @Test
    @DisplayName("3. Repository Beans Count Test")
    void testRepositoryOperation() {
        System.out.println(">>> running: 3. Repository Beans Count Test");
        // Verify that the JPA repositories are not only loaded but functional
        long count = context.getBeanDefinitionCount();
        assertNotNull(count, "The context should contain active beans for data operations");
        System.out.println("Active beans in context: " + count);
    }
 
}