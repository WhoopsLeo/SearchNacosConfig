package com.colipu.utils;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.colipu.dto.ConfigurationDto;
import com.colipu.dto.Result;
import com.colipu.exception.BusinessException;
import jcifs.UniAddress;
import jcifs.smb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
        ArrayList<ConfigurationDto> result = new ArrayList<>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                30,
                50,
                500,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(1000),
                new ThreadPoolExecutor.AbortPolicy());


        List<Future<List<ConfigurationDto>>> futureList = new ArrayList<>();
        try {
            SmbFile smbFile = new SmbFile(remoteUrl);
            if (smbFile.exists()) {
                SmbFile[] files = smbFile.listFiles();
                for (SmbFile f : files) {

                    String filePath = f.getPath();
                    String[] parts = filePath.split("/");
                    // 拿到win和iis目录
                    if (parts[parts.length - 1].equals("win") || parts[parts.length - 1].equals("iis")) {
                        // 递归遍历win和iis下面的文件, 将结果传入resultList
                        scanFiles(f, futureList, targetSubString,executor);
                    }
                }

            }

            for (Future<List<ConfigurationDto>> listFuture : futureList) {
                if (listFuture.get() != null){
                    for (ConfigurationDto configurationDto : listFuture.get()) {
                        result.add(configurationDto);
                    }
                }
            }
//            for (Future<ConfigurationDto> future : futureList) {
//                if (future.get()!= null){
//                    ConfigurationDto configurationDto = future.get();
//                    result.add(configurationDto);
//                }
//            }


        } catch (MalformedURLException e) {
            logger.error("URL格式错误，文件获取失败，错误原因：{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("URL格式错误，文件获取失败！");
        } catch (SmbException e) {
            logger.error(" SMB协议异常，文件获取失败，错误原因：{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("SMB协议异常，文件获取失败！");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        executor.shutdown();

        return Result.ok(result, (long) result.size());
    }


    /**
     * 递归读取文件夹下的文件，并查找是否有目标字符串
     *
     * @param smbFile
     * @param list
     * @param targetSubString
     */
    public static List<ConfigurationDto> scanFiles(SmbFile smbFile, List<Future<List<ConfigurationDto>>> list, String targetSubString, ThreadPoolExecutor executor) {
        try {
            if (smbFile.isDirectory()) {
                SmbFile[] smbFiles = smbFile.listFiles();
                if (smbFiles != null) {
                    for (SmbFile file : smbFiles) {
                        // 如果是子目录，递归遍历子目录中的文件
                        if (file.isDirectory()) {
                            Future<List<ConfigurationDto>> future = executor.submit(() -> {
//                                ConfigurationDto configurationDto = scanFiles(file, list, targetSubString, executor);
                                List<ConfigurationDto> configurationDtoList = scanFiles(file, list, targetSubString, executor);

                                if (configurationDtoList.size() != 0) {
                                    return configurationDtoList;
                                } else {
                                    return null;
                                }
                            });
                            // 过滤掉那些没有匹配到目标字符串的configurationDto
                            if (future.get() != null){
                                list.add(future);
                            }

                        } else {
                            // 查找文件中是否包含目标字符串
                            SmbFileInputStream smbFileInputStream = new SmbFileInputStream(file);
                            BufferedReader br = new BufferedReader(new InputStreamReader(smbFileInputStream));

                            // 开始查找字符串
//                            StringBuffer sb = new StringBuffer();
                            List<String> matchedLines = new ArrayList<>();
                            String line;
                            while ((line = br.readLine()) != null) {

                                if (line.contains(targetSubString)) {
                                    matchedLines.add(line);
                                }
                            }
                            br.close();
                            String filePath = file.getPath();
                            String[] parts = filePath.split("@");
                            filePath = filePath.substring(parts[0].length() + 1);
                            if (matchedLines.size() == 0){
                                return null;
                            }

                            List<ConfigurationDto> resultList = new ArrayList<>();
                            for (String matchedLine : matchedLines) {
                                ConfigurationDto configurationDto = new ConfigurationDto(null, filePath, matchedLine);
                                resultList.add(configurationDto);
                            }

                            return resultList;
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
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}



