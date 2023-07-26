package com.colipu.utils;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.colipu.dto.ConfigurationDto;
import com.colipu.dto.Result;
import com.colipu.exception.BusinessException;
import jcifs.UniAddress;
import jcifs.smb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SmbUtil {

    private static final Logger logger = LoggerFactory.getLogger(SmbUtil.class);

    public static void ConnectionState(String ip, String domain, String user, String pass) {
        UniAddress dc = null;
        try {
            dc = UniAddress.getByName(ip);
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain + ";" + user + ":" + pass);
            SmbSession.logon(dc, auth);
        } catch (UnknownHostException e) {
            logger.error("主机解析异常，错误原因：{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("主机解析异常，连接失败！");
        } catch (SmbException e) {
            logger.error(" SMB协议异常，错误原因：{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("SMB协议异常，连接失败！");
        }

    }

    /**
     * 读取共享文件夹下的所有文件(文件夹)
     *
     * @param remoteUrl
     * @param targetSubString
     */
    public static Result getSharedFileList(String remoteUrl, String targetSubString) {
        List<ConfigurationDto> resultList = new ArrayList<>();
        SmbFile smbFile;
        try {
            smbFile = new SmbFile(remoteUrl);
            if (smbFile.exists()) {
                SmbFile[] files = smbFile.listFiles();
                for (SmbFile f : files) {
                    // filePath:   smb://colipu;xuwenjie:Asdf19971017@10.10.18.109/moveinconfig/win/
                    String filePath = f.getPath();
                    String[] parts = filePath.split("/");
                    // 拿到win和iis目录
                    if (parts[parts.length - 1].equals("win") || parts[parts.length - 1].equals("iis")) {
                        // 递归遍历win和iis下面的文件, 将结果传入resultList
                        scanFiles(f, resultList, targetSubString);
                    }
                }
            }
        } catch (MalformedURLException e) {
            logger.error("URL格式错误，文件获取失败，错误原因：{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("URL格式错误，文件获取失败！");
        } catch (SmbException e) {
            logger.error(" SMB协议异常，文件获取失败，错误原因：{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("SMB协议异常，文件获取失败！");
        }
        return Result.ok(resultList, (long) resultList.size());
    }


    /**
     * 递归读取文件夹下的文件，并查找是否有目标字符串
     *
     * @param smbFile
     * @param list
     * @param targetSubString
     */
    public static void scanFiles(SmbFile smbFile, List<ConfigurationDto> list, String targetSubString) {
        try {
            if (smbFile.isDirectory()) {
                SmbFile[] smbFiles = smbFile.listFiles();
                if (smbFiles != null) {
                    for (SmbFile file : smbFiles) {
                        if (file.isDirectory()) {
                            // 如果是子目录，递归遍历子目录中的文件
                            scanFiles(file, list, targetSubString);
                        } else {
                            // 查找文件中是否包含目标字符串
                            SmbFileInputStream smbFileInputStream = new SmbFileInputStream(file);
                            BufferedReader br = new BufferedReader(new InputStreamReader(smbFileInputStream));

                            // 开始查找字符串
                            String line;
                            while ((line = br.readLine()) != null) {
                                if (line.contains(targetSubString)) {
                                    // smb://colipu;xuwenjie:Asdf19971017@10.10.18.109/moveinconfig/iis/API_B2BLucene/Web.config
                                    String filePath = file.getPath();
                                    String[] parts = filePath.split("@");
                                    filePath = filePath.substring(parts[0].length() + 1);
                                    ConfigurationDto configurationDto = new ConfigurationDto(null, filePath, line);
                                    list.add(configurationDto);
                                }
                            }
                            br.close();
                        }
                    }
                }
            }
        } catch (SmbException e) {
            logger.error("SMB协议异常，文件获取失败，错误原因：{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("通过SMB读取文件异常！", e);
        } catch (MalformedURLException e) {
            logger.error("获取SmbFileInputStream异常，错误原因: {}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("获取SmbFileInputStream异常！", e);
        } catch (UnknownHostException e) {
            logger.error("获取SmbFileInputStream异常，错误原因: {}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("获取SmbFileInputStream异常！", e);
        } catch (IOException e) {
            logger.error("获取SmbFileInputStream异常，错误原因: {}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("获取SmbFileInputStream异常！", e);
        }
    }
}



