package com.xxl.job.admin.security;

import com.antherd.smcrypto.sm2.Keypair;
import com.antherd.smcrypto.sm2.Sm2;
import com.antherd.smcrypto.sm3.Sm3;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Ice2Faith
 * @date 2024/5/24 23:07
 * @desc
 */
@Component
public class SecurityContext implements InitializingBean  {
    private static SecurityContext instance;

    private volatile Keypair lastKeyPair;
    private volatile String lastPublicKeySm3;

    private volatile Keypair currKeyPair= Sm2.generateKeyPairHex();
    private volatile String currPublicKeySm3= Sm3.sm3(currKeyPair.getPublicKey());

    private ScheduledExecutorService pool= Executors.newSingleThreadScheduledExecutor();
    {
        pool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                refreshKeypair();
            }
        },0,30, TimeUnit.MINUTES);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SecurityContext.instance=this;
    }

    public static SecurityContext getInstance(){
        return instance;
    }

    public synchronized Keypair refreshKeypair(){
        lastKeyPair = currKeyPair;
        lastPublicKeySm3=currPublicKeySm3;
        currKeyPair = Sm2.generateKeyPairHex();
        currPublicKeySm3=Sm3.sm3(currKeyPair.getPublicKey());
        return currKeyPair;
    }

    public synchronized Keypair currentKeypair(){
        return currKeyPair;
    }

    public synchronized Keypair findKeypair(String sign){
        if(Objects.equals(sign,currPublicKeySm3)){
            return currKeyPair;
        }
        if(Objects.equals(sign,lastPublicKeySm3)){
            return lastKeyPair;
        }
        return null;
    }

}
