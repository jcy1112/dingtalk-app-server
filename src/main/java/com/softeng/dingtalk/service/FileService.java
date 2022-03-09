package com.softeng.dingtalk.service;

import com.aliyun.dingtalkdrive_1_0.models.GetDownloadInfoResponseBody;
import com.softeng.dingtalk.api.BaseApi;
import com.softeng.dingtalk.entity.User;
import com.softeng.dingtalk.vo.PaperFileDownloadInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author RickyWu
 * @description
 * @create 7/3/2022
 */
@Service
@Transactional
@Slf4j
public class FileService {
    @Autowired
    BaseApi baseApi;

    @Autowired
    UserService userService;

    public String addFile(MultipartFile multipartFile, int uid){
        File file;
        String fileId=null;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            file=File.createTempFile(originalFilename, null);
            multipartFile.transferTo(file);
            String unionId=userService.getUserUnionId(uid);
            fileId=baseApi.addFile(file,unionId,originalFilename);
            log.info("获得fileId "+fileId);
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("存储服务出现问题！");
        }
        return fileId;
    }

    public PaperFileDownloadInfoVO getPaperFileDownloadInfoVO(String fileId, int uid){
        String unionId=userService.getUserUnionId(uid);
        GetDownloadInfoResponseBody.GetDownloadInfoResponseBodyDownloadInfo fileDownloadInfo=baseApi.getFileDownloadInfo(unionId,fileId);
        PaperFileDownloadInfoVO paperFileDownloadInfoVO=new PaperFileDownloadInfoVO();
        paperFileDownloadInfoVO.setUrl(fileDownloadInfo.getResourceUrl());
        paperFileDownloadInfoVO.setHeaderKey1(fileDownloadInfo.getHeaders().get("Authoration").toString());
        paperFileDownloadInfoVO.setHeaderKey1(fileDownloadInfo.getHeaders().get("Date").toString());
        return paperFileDownloadInfoVO;
    }
}
