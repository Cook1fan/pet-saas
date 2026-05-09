package com.pet.saas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@MapperScan("com.pet.saas.mapper")
@EnableScheduling
public class PetSaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetSaasApplication.class, args);
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");

        String hostAddress;
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            hostAddress = "localhost";
        }

        System.out.println("\n" +
            "╔════════════════════════════════════════════════════════════════╗\n" +
            "║                        Pet SaaS 启动成功!                         ║\n" +
            "╠════════════════════════════════════════════════════════════════╣\n" +
            "║  本地访问:     http://localhost:" + port + contextPath + "/                  ║\n" +
            "║  外部访问:     http://" + hostAddress + ":" + port + contextPath + "/              ║\n" +
            "╠════════════════════════════════════════════════════════════════╣\n" +
            "║  Knife4j文档:  http://localhost:" + port + contextPath + "/doc.html          ║\n" +
            "║  Swagger文档:  http://localhost:" + port + contextPath + "/swagger-ui/index.html  ║\n" +
            "╚════════════════════════════════════════════════════════════════╝\n");
    }
}
