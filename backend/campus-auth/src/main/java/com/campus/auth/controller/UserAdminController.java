package com.campus.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.auth.common.Result;
import com.campus.auth.entity.User;
import com.campus.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "管理员用户管理接口")
public class UserAdminController {

    private final UserService userService;

    @GetMapping("/users")
    @Operation(summary = "分页查询用户")
    public Result<PageResult<UserVO>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {

        Page<User> p = new Page<>(page, size);
        IPage<User> result = userService.pageUsers(p, keyword, role);
        List<UserVO> list = result.getRecords().stream().map(this::convert).collect(Collectors.toList());
        return Result.success(new PageResult<>(list, result.getTotal(), page, size));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "更新用户状态")
    public Result<Boolean> updateUserStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        boolean success;
        if (request.getStatus() != null && request.getStatus() == 1) {
            success = userService.enableUser(id);
        } else {
            success = userService.disableUser(id);
        }
        return Result.success(success);
    }

    private UserVO convert(User user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }

    @lombok.Data
    public static class UserVO {
        private Long userId;
        private String username;
        private String realName;
        private String email;
        private String phone;
        private String role;
        private Integer status;
    }

    @lombok.Data
    public static class StatusRequest {
        private Integer status;
    }

    @lombok.Data
    public static class PageResult<T> {
        private List<T> list;
        private Long total;
        private Integer page;
        private Integer size;

        public PageResult(List<T> list, Long total, Integer page, Integer size) {
            this.list = list;
            this.total = total;
            this.page = page;
            this.size = size;
        }
    }
}
