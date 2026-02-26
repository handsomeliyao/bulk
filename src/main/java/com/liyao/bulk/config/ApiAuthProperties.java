package com.liyao.bulk.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bulk.auth")
public class ApiAuthProperties {

    private String loginPage = "/login";
    private String expiredMessage = "用户信息已过期，请重新登录";

    private List<String> whitelist = new ArrayList<>();
}
