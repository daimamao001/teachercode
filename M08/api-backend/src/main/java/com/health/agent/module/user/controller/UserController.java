package com.health.agent.module.user.controller;

import com.health.agent.common.api.ApiResponse;
import com.health.agent.module.user.dto.UserLoginDTO;
import com.health.agent.module.user.dto.UserRegisterDTO;
import com.health.agent.module.user.service.IUserService;
import com.health.agent.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息查询等接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<Void> register(@Validated @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return ApiResponse.ok();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<UserVO> login(@Validated @RequestBody UserLoginDTO dto) {
        UserVO userVO = userService.login(dto);
        return ApiResponse.ok("登录成功", userVO);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public ApiResponse<UserVO> getUserInfo() {
        UserVO userVO = userService.getCurrentUserInfo();
        return ApiResponse.ok(userVO);
    }

    @Operation(summary = "根据ID获取用户信息")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return ApiResponse.ok(userVO);
    }
}

