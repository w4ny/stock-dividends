package com.example.stock.serviceimpl;

import com.example.stock.exception.impl.AlreadyExistUserException;
import com.example.stock.model.Auth;
import com.example.stock.model.MemberEntity;
import com.example.stock.persist.MemberRepository;
import com.example.stock.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MemberServiceImpl implements UserDetailsService, MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "couldn't find user -> " + username));
    }

    @Override
    public MemberEntity register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        return this.memberRepository.save(member.toEntity());
    }

    @Override
    public MemberEntity authenticate(Auth.SignIn member) {
        MemberEntity memberEntity = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 ID입니다."));

        if (!this.passwordEncoder.matches(member.getPassword(), memberEntity.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return memberEntity;
    }

}