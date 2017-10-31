package org.ditto.easyhan.service;

import com.google.gson.Gson;
import org.ditto.easyhan.model.qq.QQOpenId;
import org.ditto.easyhan.model.qq.QQUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

@Service
public class QQService {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(QQService.class.getSimpleName());

    @Autowired
    private RestTemplate restTemplate;

    private static final String GET_OPENID_URL = "https://graph.qq.com/oauth2.0/me?access_token=%s";
    private static final String GET_USER_INFO = "https://graph.qq.com/user/get_user_info?access_token=%s&oauth_consumer_key=%s&openid=%s";


    public QQUserInfo getQQUserInfo(String accessToken) {
        QQUserInfo qqUserInfo = null;
        QQOpenId qqOpenId = getOpenId(accessToken);
        if (qqOpenId != null) {
            ResponseEntity<String> response
                    = restTemplate.getForEntity(String.format(GET_USER_INFO, accessToken, qqOpenId.client_id, qqOpenId.openid), String.class);
            if (response != null && HttpStatus.OK.equals(response.getStatusCode())) {
                logger.info(String.format("response.body=%s ", response.getBody()));
                qqUserInfo = gson.fromJson(response.getBody(), QQUserInfo.class);
                logger.info(String.format("getQQUserInfo qqOpenId.client_id=%s qqOpenId.openid=%s", qqOpenId.client_id, qqOpenId.openid));
            }
        }
        return qqUserInfo;
    }

    public QQOpenId getOpenId(String accessToken) {
        QQOpenId qqOpenId = null;
        ResponseEntity<String> response
                = restTemplate.getForEntity(String.format(GET_OPENID_URL, accessToken), String.class);
        if (response != null && HttpStatus.OK.equals(response.getStatusCode())) {
            // Extract the text between the two title elements
            String pattern = "callback\\( (.+?) \\);";
            String updated = response.getBody().replaceAll(pattern, "$1");
            logger.info(String.format("getOpenId response.body=%s jsonString=%s", response.getBody(), updated));
            qqOpenId = gson.fromJson(updated, QQOpenId.class);
            logger.info(String.format("getOpenId qqOpenId.client_id=%s qqOpenId.openid=%s", qqOpenId.client_id, qqOpenId.openid));
        }
        return qqOpenId;
    }


}
