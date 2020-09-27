package top.itning.yunshu.yunshunas.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author itning
 * @date 2020/9/5 12:29
 */
public interface FileService {
    /**
     * 获取音乐
     *
     * @param id       音乐ID
     * @param range    请求头Range
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    void getOneMusic(String id, String range, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
