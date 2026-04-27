package com.crazycheko.ticket.config;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

@SpringBootTest
public class DbTest {

    private DataSource dataSource;
    @Test
    public void testDb() throws Exception{

        String url = "jdbc:postgresql://127.0.0.1:5432/ticket_db";
        String user = "postgres";
        String password = "password";
        Class.forName("org.postgresql.Driver");
        try{
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("成功");
        }finally {
            System.out.println("結束");
        }

    }
}
