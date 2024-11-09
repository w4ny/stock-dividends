package com.example.stock.service;

import com.example.stock.model.Auth;
import com.example.stock.model.MemberEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface MemberService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    MemberEntity register(Auth.SignUp member);

    MemberEntity authenticate(Auth.SignIn member);

}
