package com.xxl.job.admin.service;

import com.antherd.smcrypto.sm2.Keypair;
import com.antherd.smcrypto.sm2.Sm2;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.security.ConcurrentLruCache;
import com.xxl.job.admin.core.util.CookieUtil;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.core.util.JacksonUtil;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.admin.security.SecurityContext;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author xuxueli 2019-05-04 22:13:264
 */
@Configuration
public class LoginService {

    public static final String LOGIN_IDENTITY_KEY = "XXL_JOB_LOGIN_IDENTITY";

    public static final String KEY_PAIR_PATH=SecurityContext.STORE_PATH+"/token.pk";

    private final Keypair keypair;
    {
        Keypair pair = SecurityContext.loadStoreKeypair(KEY_PAIR_PATH, null);
        if(pair==null){
            pair=Sm2.generateKeyPairHex();
        }
        SecurityContext.saveStoreKeypair(KEY_PAIR_PATH,pair);
        keypair = pair;
    }

    private ConcurrentLruCache<String, String> cacheParseToken=new ConcurrentLruCache<>(1024, tokenHex->{
        String tokenJson = Sm2.doDecrypt(tokenHex, keypair.getPrivateKey());
        return tokenJson;
    });

    @Resource
    private XxlJobUserDao xxlJobUserDao;


    private String makeToken(XxlJobUser xxlJobUser){
        xxlJobUser.setPassword(null);
        String tokenJson = JacksonUtil.writeValueAsString(xxlJobUser);
        String tokenHex = Sm2.doEncrypt(tokenJson, keypair.getPublicKey());
        return tokenHex;
    }


    private XxlJobUser parseToken(String tokenHex){
        XxlJobUser xxlJobUser = null;
        if (tokenHex != null) {
            String tokenJson = cacheParseToken.get(tokenHex);
            xxlJobUser = JacksonUtil.readValue(tokenJson, XxlJobUser.class);
        }
        return xxlJobUser;
    }


    public ReturnT<String> login(HttpServletRequest request, HttpServletResponse response, String username, String password, boolean ifRemember){

        // param
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)){
            return new ReturnT<String>(500, I18nUtil.getString("login_param_empty"));
        }

        // valid passowrd
        XxlJobUser xxlJobUser = xxlJobUserDao.loadByUserName(username);
        if (xxlJobUser == null) {
            return new ReturnT<String>(500, I18nUtil.getString("login_param_unvalid"));
        }
        String passwordMd5 = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!passwordMd5.equals(xxlJobUser.getPassword())) {
            return new ReturnT<String>(500, I18nUtil.getString("login_param_unvalid"));
        }

        String loginToken = makeToken(xxlJobUser);

        // do login
        CookieUtil.set(response, LOGIN_IDENTITY_KEY, loginToken, ifRemember);
        return ReturnT.SUCCESS;
    }

    /**
     * logout
     *
     * @param request
     * @param response
     */
    public ReturnT<String> logout(HttpServletRequest request, HttpServletResponse response){
        CookieUtil.remove(request, response, LOGIN_IDENTITY_KEY);
        return ReturnT.SUCCESS;
    }

    /**
     * logout
     *
     * @param request
     * @return
     */
    public XxlJobUser ifLogin(HttpServletRequest request, HttpServletResponse response){
        String cookieToken = CookieUtil.getValue(request, LOGIN_IDENTITY_KEY);
        if (cookieToken != null) {
            XxlJobUser cookieUser = null;
            try {
                cookieUser = parseToken(cookieToken);
            } catch (Exception e) {
                logout(request, response);
            }
            if (cookieUser != null) {
                XxlJobUser dbUser = xxlJobUserDao.loadByUserName(cookieUser.getUsername());
                if (dbUser != null) {
                    dbUser.setPassword(null);
                    return dbUser;
                }
            }
        }
        return null;
    }


}
