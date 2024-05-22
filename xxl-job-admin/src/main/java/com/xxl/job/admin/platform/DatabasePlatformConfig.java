package com.xxl.job.admin.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ice2Faith
 * @date 2024/5/22 9:27
 * @desc
 */
@Configuration
@ConfigurationProperties(prefix = "xxl.job.database.platform")
public class DatabasePlatformConfig implements InitializingBean {
    private Logger log= LoggerFactory.getLogger(DatabasePlatformConfig.class);

    private String type;


    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("database platform active: "+type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DatabasePlatformType type(){
        return DatabasePlatformType.of(type);
    }
}
