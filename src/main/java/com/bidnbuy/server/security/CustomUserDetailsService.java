package com.bidnbuy.server.security;

import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@DependsOn("entityManagerFactory")
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdStr) throws UsernameNotFoundException{
        try{
            Long userId = Long.parseLong(userIdStr);
            log.info("인증을 위해 사용자 아이디로 userEntity 로드 시도 : {}", userId);

            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자 ID: " + userId + " 를 찾을 수 없습니다."));
            return new User(
                    String.valueOf(userEntity.getUserId()),
                    "",
//                    Collections.emptyList()
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }catch (NumberFormatException e){
            log.error("JWT에서 추출된 ID가 유효한 숫자가 아닙니다: {}", userIdStr, e);
            throw new UsernameNotFoundException("유효하지 않은 사용자 ID 형식입니다: " + userIdStr);
        }
    }


    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        String roleName = "ROLE_" + role.toUpperCase();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }



}
