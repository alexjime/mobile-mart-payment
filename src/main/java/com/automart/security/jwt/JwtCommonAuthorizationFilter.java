package com.automart.security.jwt;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.automart.user.domain.Admin;
import com.automart.security.UserPrincipal;
import com.automart.user.domain.User;
import com.automart.user.repository.AdminRepository;
import com.automart.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * HTTP 기본 인증 헤더를 처리하여 결과를 SecurityContextHolder에 저장한다.
 */
public class JwtCommonAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;
    private AdminRepository adminRepository;
    private JwtTokenProvider jwtTokenProvider;
    private RedisTemplate<String, Object> redisTemplate;


    public JwtCommonAuthorizationFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserRepository userRepository, AdminRepository adminRepository, RedisTemplate<String, Object> redisTemplate) {
        super(authenticationManager);
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * request에서 Header(jwtToken)을 획득 후, 해당 유저를 DB에서 찾아 인증을 진행한다. (Authentication 생성)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // JWT token이 어디있는지 알기 위해, Authorization header를 찾는다.
        String header = request.getHeader("Authorization");

        // 만약 header에 Bearer가 포함되어 있지 않거나 header가 null이라면 작업을 끝낸다.
        if (header == null || !header.startsWith("Bearer")) {
            chain.doFilter(request, response);
            return;
        }

        // 토큰 인증을 위한 전처리
        HttpServletRequestWrapper myRequest = new HttpServletRequestWrapper((HttpServletRequest) request) {
            @Override
            public String getHeader(String name) {
                if (name.equals("Authorization")) {
                    String basic = request.getHeader("Authorization").replace("Bearer", "");
                    return basic;
                }
                return super.getHeader(name);
            }
        };
        // 만약 header가 존재한다면, DB로 부터 user의 권한을 확인하고, authorization을 수행한다.
        Authentication authentication = getUsernamePasswordAuthentication(myRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // filter 수행을 계속한다.
        chain.doFilter(myRequest, response);
    }

    /**
     * 헤더의 jwtToken 내부에 있는 정보를 통해 DB와 일치하는 유저를 찾아 인증이 완료한다. (UserPrincipal 객체 생성)
     */
    private Authentication getUsernamePasswordAuthentication(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String principal = null;
        UserPrincipal userPrincipal = null;

        if (redisTemplate.opsForValue().get(token) != null) { // access token이 블랙리스트(=로그아웃된 유저)라면 요청실패
            request.setAttribute("exception", "BLOCKED_TOKEN");
            return null;
        }

        try {

            // 만약 token이 존재한다면, token을 통해 user의 이메일을 얻는다.
            // 이때, token이 만료됐으면 ExpiredTokenResolver로 이동한다.
            principal = jwtTokenProvider.getPrincipal(token, JwtTokenProvider.TokenType.ACCESS_TOKEN); // user의 email 또는 admin의 id 값

            if(principal.contains("@")) {
                // Admin 일 경우
                Optional<User> oUser = userRepository.findByEmail(principal);
                User user = oUser.get();
                userPrincipal = UserPrincipal.create(user);

            } else {
                // User 일 경우
                Optional<Admin> oAdmin = adminRepository.findById(principal);
                Admin admin = oAdmin.get();
                userPrincipal = UserPrincipal.create(admin);

            }

            // OAuth 인지 일반 로그인인지 구분할 필요가 없음. 왜냐하면 password를 Authentication이 가질 필요가 없으니!!
            // JWT가 로그인 프로세스를 가로채서 인증다 해버림. (OAuth2.0이든 그냥 일반 로그인 이든)

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication;

        } catch (ExpiredJwtException e) {

            principal = e.getClaims().getSubject();

            if (redisTemplate.opsForValue().get(principal) != null) { // refresh토큰만 살아있는 경우(access토큰이 기간만 만료된것일 때)
                String accessToken = jwtTokenProvider.createToken(principal, JwtTokenProvider.TokenType.ACCESS_TOKEN);
                request.setAttribute("exception", "EXPIRED_TOKEN");
                request.setAttribute("token", accessToken);
            }

        }
        return null;
    }
}

