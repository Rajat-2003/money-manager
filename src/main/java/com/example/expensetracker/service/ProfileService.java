package com.example.expensetracker.service;

import java.util.Map;
import java.util.UUID;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;

import com.example.expensetracker.dto.AuthDTO;
import com.example.expensetracker.dto.ProfileDTO;
import com.example.expensetracker.entity.ProfileEntity;
import com.example.expensetracker.repository.ProfileRepository;
import com.example.expensetracker.util.JwtUtil;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ProfileService {

        private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

        private final ProfileRepository profileRepository;
        private final EmailService emailService;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtUtil jwtUtil;

        @Value("${app.activation.url}")
        private String activationURL;
        public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        
            ProfileEntity newProfile = toEntity(profileDTO);

            newProfile.setActivationToken(UUID.randomUUID().toString());
            newProfile = profileRepository.save(newProfile);
            String activationLink = activationURL+"/api/v1.0/activate?token=" + newProfile.getActivationToken();
            String subject = "Activate your Expense Tracker account";
            String body = "Welcome to Expense Tracker! Please activate your account using the following link: " + activationLink;
            emailService.sendEmail(newProfile.getEmail(), subject, body);
            return toDTO(newProfile);
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken)
    {
        return profileRepository.findByActivationToken(activationToken)
        .map(profile->{
                profile.setIsActive(true);
                profileRepository.save(profile);
                return true;
        })
        .orElse(false);
    }

    public boolean isAccountActive(String email)
     {
         return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
     }

     public ProfileEntity getCurrentProfile()
    {
        Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Profile not found for email: " + authentication.getName()));

    }

    public ProfileDTO getPublicProfile(String email) {
            ProfileEntity currentUser = null;
            if (email == null) {
                    currentUser = getCurrentProfile();
            } else {
                    currentUser = profileRepository.findByEmail(email)
                                    .orElseThrow(() -> new RuntimeException("Profile not found for email: " + email));
            }

            return ProfileDTO.builder()
                            .id(currentUser.getId())
                            .fullName(currentUser.getFullName())
                            .email(currentUser.getEmail())
                            .profileImageUrl(currentUser.getProfileImageUrl())
                            .createdAt(currentUser.getCreatedAt())
                            .updatedAt(currentUser.getUpdatedAt())
                            .build();

    }

//     public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
//         try {
//                 authenticationManager.authenticate(new UsernamePasswordAuthenticationToken( authDTO.getEmail(), authDTO.getPassword()));
//                 String token= jwtUtil.generateToken(authDTO.getEmail());
//                 return Map.of(
//                         "token",token,
//                         "user", getPublicProfile(authDTO.getEmail())
//                 );
                
//         } catch (Exception e) {
//                 throw new RuntimeException("Invalid credentials");
//         }
//     }


    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        logger.info("Authenticating user: {}", authDTO.getEmail());
        
        try {
            // Debug: Check if user exists and is active
            ProfileEntity user = profileRepository.findByEmail(authDTO.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            logger.info("User found: ID={}, Email={}, Active={}", 
                    user.getId(), user.getEmail(), user.getIsActive());
            
            // Debug: Test password encoding
            boolean passwordMatches = passwordEncoder.matches(authDTO.getPassword(), user.getPassword());
            logger.info("Password matches: {}", passwordMatches);
            
            if (!passwordMatches) {
                logger.warn("Password does not match for user: {}", authDTO.getEmail());
                throw new IllegalArgumentException("Invalid password");
            }
            
            if (!user.getIsActive()) {
                logger.warn("Account is not active for user: {}", authDTO.getEmail());
                throw new IllegalArgumentException("Account is not active");
            }
            
            // Perform Spring Security authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword())
            );
            
            logger.info("Spring Security authentication successful for: {}", authDTO.getEmail());
            
            String token = jwtUtil.generateToken(authDTO.getEmail());
            logger.info("JWT token generated successfully for: {}", authDTO.getEmail());
            
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDTO.getEmail())
            );
            
        } catch (BadCredentialsException e) {
            logger.error("Bad credentials for user: {} - {}", authDTO.getEmail(), e.getMessage());
            throw new IllegalArgumentException("Invalid email or password");
        } catch (DisabledException e) {
            logger.error("Account disabled for user: {} - {}", authDTO.getEmail(), e.getMessage());
            throw new IllegalArgumentException("Account is disabled");
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user: {} - {} - {}", 
                    authDTO.getEmail(), e.getClass().getSimpleName(), e.getMessage());
            throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Re-throw our custom validation errors
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during authentication for user: {}", authDTO.getEmail(), e);
            throw new RuntimeException("Authentication failed due to unexpected error");
        }
    }
    
}
