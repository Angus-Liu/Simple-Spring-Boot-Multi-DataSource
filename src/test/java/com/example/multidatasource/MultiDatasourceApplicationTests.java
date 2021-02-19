package com.example.multidatasource;

import com.example.multidatasource.service.User;
import com.example.multidatasource.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MultiDatasourceApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
    }

    @Test
    public void getUser() {
        User user;
        user = userService.getUser(1);
        System.out.println("user = " + user);

        user = userService.getUserFromDefault(1);
        System.out.println("user = " + user);

        user = userService.getUserFromSlave1(1);
        System.out.println("user = " + user);

        user = userService.getUserFromSlave2(1);
        System.out.println("user = " + user);

        user = userService.getUser(1);
        System.out.println("user = " + user);

        user = userService.getUserFromDefault(1);
        System.out.println("user = " + user);

        user = userService.getUserFromSlave1(1);
        System.out.println("user = " + user);

        user = userService.getUserFromSlave2(1);
        System.out.println("user = " + user);
    }
}
