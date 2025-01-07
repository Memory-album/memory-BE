package com.min.i.memory_BE.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Operation(
            summary = "로그인 페이지로 이동",
            description = "사용자를 로그인 페이지로 리디렉션합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그인 페이지로 이동"
    )
    @GetMapping("/loginPage")
    public ResponseEntity<String> loginPage() {
        return ResponseEntity.ok("로그인 페이지로 이동");
    }
//

    @Operation(
            summary = "홈 페이지로 이동",
            description = "사용자를 홈 페이지로 리디렉션합니다. 인증된 사용자만 홈 페이지로 이동할 수 있습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "홈 페이지로 이동"
    )
    @GetMapping("/home")
    public ResponseEntity<String> homePage() {
        return ResponseEntity.ok("홈으로 이동");
    }
//

    @Operation(
            summary = "마이 페이지로 이동",
            description = "인증된 사용자만 마이 페이지로 이동할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "마이 페이지로 이동",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 필요"
            )
    })
    @GetMapping("/my-page")
    public ResponseEntity<String> myPage(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return ResponseEntity.ok("마이 페이지: " + auth.getName());
        } else {
            return ResponseEntity.status(401).body("로그인 필요");
        }
    }

}
