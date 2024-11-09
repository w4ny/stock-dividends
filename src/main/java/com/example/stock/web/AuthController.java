package com.example.stock.web;

import com.example.stock.model.Auth;
import com.example.stock.model.MemberEntity;
import com.example.stock.security.TokenProvider;
import com.example.stock.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody Auth.SignUp request) {
        MemberEntity result = this.memberService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody Auth.SignIn request) {
        MemberEntity entity = this.memberService.authenticate(request);
        String token = this.tokenProvider.generateToken(entity.getUsername(), entity.getRoles());
        log.info("user login -> " + request.getUsername());
        return ResponseEntity.ok(token);
    }

}
