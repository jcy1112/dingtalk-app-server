package com.softeng.dingtalk.component;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.softeng.dingtalk.entity.User;
import com.softeng.dingtalk.entity.Vote;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhanyeye
 * @description DingTalk 服务端API 工具组件
 * @date 11/13/2019
 */
@Slf4j
@Component
public class DingTalkUtils {
    private static String CORPID;
    private static String APP_KEY;
    private static String APP_SECRET;
    private static String CHAT_ID;
    private static String AGENTID;
    private static String DOMAIN;

    @Value("${my.corpid}")
    public void setCORPID(String corpid) {
        CORPID = corpid;
    }

    @Value("${my.app_key}")
    public void setAppKey(String appKey) {
        APP_KEY = appKey;
    }

    @Value("${my.app_secret}")
    public void setAppSecret(String appSecret) {
        APP_SECRET = appSecret;
    }

    @Value("${my.chat_id}")
    public void setChatId(String chatId) {
        CHAT_ID = chatId;
    }

    @Value("${my.agent_id}")
    public void setAGENTID(String agentid) {
        AGENTID = agentid;
    }
    @Value("${my.domain}")
    public void setDOMAIN(String domain) {
        DOMAIN = domain;
    }



    private static final long cacheTime = 1000 * 60 * 55 * 2; //缓存时间 1小时 50分钟

    private static String accessToken;  //缓存的accessToken: 不可直接调用，以防过期
    private static long tokenTime;      //缓存时间
    private static String jsapiTicket;  //缓存的accessToken jsapi_ticket: 不可直接调用，以防过期
    private static long ticketTime;     //缓存时间


    /**
     * 获取 AccessToken
     * @return java.lang.String
     * @Date 9:10 PM 11/13/2019
     **/
    public String getAccessToken() {
        long curTime = System.currentTimeMillis();
        if (accessToken == null || curTime - tokenTime >= cacheTime ) {
            DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
            OapiGettokenRequest request = new OapiGettokenRequest();
            request.setAppkey(APP_KEY);
            request.setAppsecret(APP_SECRET);
            request.setHttpMethod("GET");
            try {
                OapiGettokenResponse response = client.execute(request);
                accessToken = response.getAccessToken();
                tokenTime = System.currentTimeMillis();
            } catch (ApiException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "DingtalkUtils 获取accesstoken失败");
            }
            log.debug("AccessToken 快要过期，重新获取");
        }
        return accessToken;
    }


    /**
     * 获取 Jsapi Ticket
     * @return java.lang.String
     * @Date 8:20 AM 2/23/2020
     **/
    public String getJsapiTicket()  {
        long curTime = System.currentTimeMillis();
        if (jsapiTicket == null || curTime - ticketTime >= cacheTime) {
            DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/get_jsapi_ticket");
            OapiGetJsapiTicketRequest req = new OapiGetJsapiTicketRequest();
            req.setTopHttpMethod("GET");
            try {
                OapiGetJsapiTicketResponse response = client.execute(req, getAccessToken());
                jsapiTicket = response.getTicket();
                ticketTime = System.currentTimeMillis();
            } catch (ApiException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "DingtalkUtils 获取JsapiTicket失败");
            }
            log.debug("JsapiTicket 快要过期，重新获取");
        }
        return jsapiTicket;
    }


    /**
     * 获得userid : 通过 access_token 和 requestAuthcode；在内部调用了getAccessToken()，不用传参
     * @param requestAuthCode
     * @return java.lang.String
     * @Date 5:07 PM 1/13/2020
     **/
    public String getUserId(String requestAuthCode) {
        String userId = null;
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/getuserinfo");
        OapiUserGetuserinfoRequest request = new OapiUserGetuserinfoRequest();
        request.setCode(requestAuthCode);
        request.setHttpMethod("GET");
        try {
            OapiUserGetuserinfoResponse response = client.execute(request, getAccessToken());
            userId = response.getUserid();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return userId;
    }


    /**
     * 通过 userid （钉钉的用户码），获取钉钉中用户信息
     * @param userid
     * @return com.softeng.dingtalk.entity.User
     * @Date 5:09 PM 1/13/2020
     **/
    public User getNewUser(String userid) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get");
        OapiUserGetRequest request = new OapiUserGetRequest();
        request.setUserid(userid);
        request.setHttpMethod("GET");
        OapiUserGetResponse response;
        try {
            response = client.execute(request, getAccessToken());
        } catch (ApiException e) {
            log.error("getUserDetail fail", e);
            throw new RuntimeException();
        }
        int authority = response.getIsAdmin() ? User.ADMIN_AUTHORITY : User.USER_AUTHORITY;
        User user = new User(response.getUserid(), response.getName(), response.getAvatar(), authority, response.getPosition());
        return  user;
    }


    // 获取周报信息
    public Map getReport(String userid, LocalDate date) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/report/list");
        OapiReportListRequest request = new OapiReportListRequest();
        request.setUserid(userid);
        Long startTime = LocalDateTime.of(date, LocalTime.of(12,0)).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        request.setStartTime(startTime); //开始时间
        request.setEndTime(startTime + TimeUnit.DAYS.toMillis(5));  //结束时间
        request.setCursor(0L);
        request.setSize(1L);
        OapiReportListResponse response;
        try {
            response = client.execute(request, getAccessToken());
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "获取周报失败");
        }
        if (response.getResult().getDataList().size() == 0) { // 无数据
            return Map.of();
        } else {
            List<OapiReportListResponse.JsonObject> contents = response.getResult().getDataList().get(0).getContents().stream()
                    .filter((item) -> !item.getValue().isEmpty())
                    .collect(Collectors.toList());
            return Map.of("contents", contents);
        }
    }

    // 获取部门id
    public List<String> listDepid() {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/department/list");
        OapiDepartmentListRequest request = new OapiDepartmentListRequest();
        request.setHttpMethod("GET");
        OapiDepartmentListResponse response;
        try {
            response = client.execute(request, getAccessToken());
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "获取部门Id 失败");
        }
        return response.getDepartment().stream().map(x -> String.valueOf(x.getId())).collect(Collectors.toList());
    }

    //获取整个部门的userid
    public List<String> listUserId(String depid) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/getDeptMember");
        OapiUserGetDeptMemberRequest req = new OapiUserGetDeptMemberRequest();
        req.setDeptId(depid);
        req.setHttpMethod("GET");
        OapiUserGetDeptMemberResponse response;
        try {
            response = client.execute(req, getAccessToken());
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "获取getUserIds失败");
        }

        return response.getUserIds();
    }


    // 发起投票时向群中发送消息
    public void sendVoteMsg(int pid, String title, String endtime, List<String> namelist) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/chat/send");
        OapiChatSendRequest request = new OapiChatSendRequest();
        request.setChatid(CHAT_ID);
        OapiChatSendRequest.ActionCard actionCard = new OapiChatSendRequest.ActionCard();

        StringBuffer content = new StringBuffer().append(" ## 投票 \n ##### 论文： ").append(title).append(" \n ##### 作者： ");
        for (String name : namelist) {
            content.append(name).append(", ");
        }
        content.append(" \n 截止时间: ").append(endtime);

        StringBuffer pcurl = new StringBuffer().append("dingtalk://dingtalkclient/action/openapp?corpid=").append(CORPID)
                .append("&container_type=work_platform&app_id=0_").append(AGENTID).append("&redirect_type=jump&redirect_url=")
                .append(DOMAIN).append("/paper/vote/").append(pid);

        log.debug(pcurl.toString());

        actionCard.setTitle("评审投票");
        actionCard.setMarkdown(content.toString());

//        actionCard.setBtnOrientation("1");
//        OapiChatSendRequest.BtnJson btn1 = new OapiChatSendRequest.BtnJson();
//        btn1.setTitle("移动端尚不支持");
//        btn1.setActionUrl("https://www.dogedoge.com/");
//        OapiChatSendRequest.BtnJson btn2 = new OapiChatSendRequest.BtnJson();
//        btn2.setTitle("PC端");
//        btn2.setActionUrl(pcurl.toString());
//        List<OapiChatSendRequest.BtnJson> btnJsonList = new ArrayList<>();
//        btnJsonList.add(btn1);
//        btnJsonList.add(btn2);
//        actionCard.setBtnJsonList(btnJsonList);

        actionCard.setSingleTitle("目前支持PC端");
        actionCard.setSingleUrl(pcurl.toString());

        request.setActionCard(actionCard);
        request.setMsgtype("action_card");

        try {
            OapiChatSendResponse response = client.execute(request, getAccessToken());


            log.debug(response.getBody());
        } catch (ApiException e) {
            e.printStackTrace();
        }

    }



    // 发送投票结果
    public void sendVoteResult(int pid, String title, boolean result, int accept, int total) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/chat/send");
        OapiChatSendRequest request = new OapiChatSendRequest();
        request.setChatid(CHAT_ID);
        OapiChatSendRequest.ActionCard actionCard = new OapiChatSendRequest.ActionCard();

        StringBuffer content = new StringBuffer().append(" #### 投票结果 \n ##### 论文： ").append(title)
                .append(" \n 最终结果： ").append(result ? "Accept" : "reject")
                .append("  \n  Accept: ").append(accept).append(" 票  \n ")
                .append("Reject: ").append(total-accept).append(" 票  \n ")
                .append("已参与人数： ").append(total).append("人  \n ");

        StringBuffer pcurl = new StringBuffer().append("dingtalk://dingtalkclient/action/openapp?corpid=").append(CORPID)
                .append("&container_type=work_platform&app_id=0_").append(AGENTID).append("&redirect_type=jump&redirect_url=")
                .append(DOMAIN).append("/paper/vote/").append(pid);


        actionCard.setTitle("投票结果");
        actionCard.setMarkdown(content.toString());
        actionCard.setSingleTitle("查看详情");
        actionCard.setSingleUrl(pcurl.toString());


        request.setActionCard(actionCard);
        request.setMsgtype("action_card");

        try {
            OapiChatSendResponse response = client.execute(request, getAccessToken());

            log.debug(response.getBody());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }




    // 字节数组转化成十六进制字符串
    private String bytesToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private String sign(String ticket, String nonceStr, long timeStamp, String url)  {
        String plain = "jsapi_ticket=" + ticket + "&noncestr=" + nonceStr + "&timestamp=" + String.valueOf(timeStamp)
                + "&url=" + url;
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.reset();
            sha1.update(plain.getBytes("UTF-8"));
            return bytesToHex(sha1.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map authentication(String url) {
        long timeStamp = System.currentTimeMillis();
        String nonceStr = "todowhatliesclearathand";
        String signature = sign(getJsapiTicket(),nonceStr, timeStamp, url);
        return Map.of("agentId", AGENTID,"url", url, "nonceStr", nonceStr, "timeStamp", timeStamp, "corpId", CORPID, "signature", signature);
    }



//    public void sentGroupMessage() {
//        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/chat/send");
//        OapiChatSendRequest request = new OapiChatSendRequest();
//        request.setChatid(CHAT_ID);
//        OapiChatSendRequest.ActionCard actionCard = new OapiChatSendRequest.ActionCard();
//
//        actionCard.setTitle("下午好，打扰了，这是一个测试标题");
//        actionCard.setMarkdown("markdown 内容，今天雨夹雪，雨夹雪，雨夹雪，\n  ###### 6号标题\n + 1 + 2 + 3，");
//        actionCard.setBtnOrientation("1");
//
//        OapiChatSendRequest.BtnJson btn1 = new OapiChatSendRequest.BtnJson();
//        btn1.setTitle("test");
//        btn1.setActionUrl("http://www.baidu.com");
//
//        OapiChatSendRequest.BtnJson btn2 = new OapiChatSendRequest.BtnJson();
//        btn2.setTitle("PC端");
//        btn2.setActionUrl("dingtalk://dingtalkclient/action/openapp?corpid=dingeff939842ad9207f35c2f4657eb6378f&container_type=work_platform&app_id=0_313704868&redirect_type=jump&redirect_url=http://www.dingdev.xyz:8080/paper/vote/2");
//
//        List<OapiChatSendRequest.BtnJson> btnJsonList = new ArrayList<>();
//
//        btnJsonList.add(btn1);
//        btnJsonList.add(btn2);
//
//        actionCard.setBtnJsonList(btnJsonList);
//
//        request.setActionCard(actionCard);
//        request.setMsgtype("action_card");
//
//        try {
//            OapiChatSendResponse response = client.execute(request, getAccessToken());
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
//    }



}
