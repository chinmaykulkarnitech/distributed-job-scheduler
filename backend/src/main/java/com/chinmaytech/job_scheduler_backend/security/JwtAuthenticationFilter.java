package com.chinmaytech.job_scheduler_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;



@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

     //   System.out.println(">>> Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println(">>> NO JWT");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            boolean valid = jwtService.isValid(token);

       //     System.out.println(">>> JWT VALID: " + valid);

            if (!valid) {
         //       System.out.println(">>> JWT REJECTED");
                filterChain.doFilter(request, response);
                return;
            }

            String userId = jwtService.extractUserId(token);

        //    System.out.println(">>> USER ID: " + userId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            java.util.Collections.emptyList()
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

       //     System.out.println(">>> AUTHENTICATION SET");

        } catch (Exception e) {

      //      System.out.println(">>> JWT ERROR: " + e.getMessage());

        }

        filterChain.doFilter(request, response);
    }
}
