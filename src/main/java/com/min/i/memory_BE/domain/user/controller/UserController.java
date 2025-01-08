package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

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
            description = "사용자를 홈 페이지로 리디렉션합니다. 로그인된 사용자만 홈 페이지로 이동할 수 있습니다."
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
            description = "사용자를 마이 페이지로 리디렉션합니다. 로그인된 사용자만 마이 페이지로 이동할 수 있습니다."
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

    // 사용자 정보 수정
    @Operation(
            summary = "사용자 정보 수정",
            description = "사용자가 비밀번호, 이름, 프로필 사진 링크, 이메일을 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            )
    })
    @PutMapping("/update")
    public ResponseEntity<String> updateUser(
            @Parameter(description = "사용자가 수정할 이메일") @RequestParam String email,
            @Parameter(description = "사용자가 수정할 비밀번호") @RequestParam(required = false) String password,
            @Parameter(description = "사용자가 수정할 이름") @RequestParam(required = false) String name,
            @Parameter(description = "사용자가 수정할 프로필 사진 URL") @RequestParam(required = false) String profileImgUrl,
            Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User updatedUser = userService.updateUser(email, password, name, profileImgUrl);
            return ResponseEntity.ok("사용자 정보가 성공적으로 수정되었습니다.");
        } else {
            return ResponseEntity.status(401).body("로그인 필요");
        }
    }

    // 사용자 탈퇴 (계정 영구 삭제)
    @Operation(
            summary = "사용자 탈퇴",
            description = "사용자가 탈퇴하면 계정이 삭제됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "탈퇴 처리 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이메일 형식 오류)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 필요"
            )
    })
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@Parameter(description = "사용자 이메일") @RequestParam String email, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            userService.deleteUser(email);
            return ResponseEntity.ok("사용자 탈퇴가 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("잘못된 요청입니다.");
        }
    }

    // 사용자 계정 비활성화
    @Operation(
            summary = "사용자 계정 비활성화",
            description = "사용자가 계정을 비활성화하면 상태가 '비활성'으로 변경됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "계정 비활성화 처리 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이메일 형식 오류)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 필요"
            )
    })
    @PostMapping("/deactivate")
    public ResponseEntity<String> deactivateUser(@Parameter(description = "사용자 이메일") @RequestParam String email, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            userService.deactivateUser(email);
            return ResponseEntity.ok("사용자 계정이 비활성화되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("잘못된 요청입니다.");
        }
    }

    // 사용자 계정 활성화
    @Operation(
            summary = "사용자 계정 활성화",
            description = "사용자가 비활성화된 계정을 활성화합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "계정 활성화 처리 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이메일 형식 오류)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 필요"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "비활성화되지 않은 계정은 활성화할 수 없습니다."
            )
    })
    @PostMapping("/activate")
    public ResponseEntity<String> activateUser(@Parameter(description = "사용자 이메일") @RequestParam String email, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            userService.activateUser(email);
            return ResponseEntity.ok("사용자 계정이 활성화되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            //이미 활성화된 계정은 활성화 작업이 필요없어서 요청을 보낼 때 충돌이 발생! (=상태의 불일치)
            return ResponseEntity.status(409).body("비활성화된 계정만 활성화할 수 있습니다.");
        }
    }
}
