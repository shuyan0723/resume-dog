package com.resumedog.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * HS256 签名密钥。生产环境务必通过环境变量注入，泄露即等同于任意用户身份伪造。
     */
    private String secret;

    /** 访问令牌有效期（分钟） */
    private long expireMinutes = 60 * 24 * 7;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(long expireMinutes) {
        this.expireMinutes = expireMinutes;
    }
}
