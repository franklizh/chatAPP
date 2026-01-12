package com.easychat.controller;

import com.easychat.entity.dto.SocialPostPublishDTO;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.query.FeedQuery;
import com.easychat.entity.vo.FeedResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.SocialPostService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/social/post")
public class SocialPostController extends ABaseController {
    @Resource
    private SocialPostService socialPostService;

//    @RequestMapping("/publish")
//    public ResponseVO publish(HttpServletRequest request, SocialPostPublishDTO dto) {
//        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
//        String userId = tokenUserInfoDto.getUserId();
//        socialPostService.publishPost(userId, dto);
//        return getSuccessResponseVO(null);
//    }

    @PostMapping("/publish")
    public ResponseVO<Void> publish(
            HttpServletRequest request,
            @RequestBody @Valid SocialPostPublishDTO dto) {

        // 1️⃣ 参数兜底校验（防 NPE）
        if (dto == null) {
            return getServerErrorResponseVO("请求参数不能为空");
        }

        // 2️⃣ 从 token 中取用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        if (tokenUserInfoDto == null) {
            return getServerErrorResponseVO("用户未登录");
        }

        String userId = tokenUserInfoDto.getUserId();

        // 3️⃣ 调用 Service
        socialPostService.publishPost(userId, dto);

        return getSuccessResponseVO(null);
    }


//    @RequestMapping("/feed")
//    public ResponseVO feed(
//            HttpServletRequest request,
//            @NotNull int page,
//            @NotNull int pageSize) {
//
//        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
//        String userId = tokenUserInfoDto.getUserId();
//        return getSuccessResponseVO(
//                socialPostService.getFeed(userId, page, pageSize)
//        );
//    }


    @GetMapping("/feed")
    public ResponseVO<FeedResultVO> feed(
            HttpServletRequest request,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime lastTime,
            @RequestParam(required = false) Long lastPostId){
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        if(tokenUserInfoDto==null){
            return getServerErrorResponseVO("用户未登录");
        }

        FeedQuery query =new FeedQuery();
        query.setLimit(limit);
        query.setLastTime(lastTime);
        query.setLastPostId(lastPostId);

        FeedResultVO result=socialPostService.getFeed(tokenUserInfoDto.getUserId(),query);

        return getSuccessResponseVO(result);

    }
}
