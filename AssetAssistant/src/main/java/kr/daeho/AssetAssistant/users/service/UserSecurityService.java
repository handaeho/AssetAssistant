package kr.daeho.AssetAssistant.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.daeho.AssetAssistant.users.entity.UserEntity;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * 사용자 보안 관련 서비스
 * 
 * 비밀번호 암호화, 인증, 권한 관련 로직을 담당하는 서비스
 * 
 * 책임 분리 원칙(SRP)에 따라 보안 관련 로직만 처리
 * 
 * @Service: 서비스 클래스임을 명시
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSecurityService {
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 암호화
     * 
     * @param rawPassword 평문 비밀번호
     * @return 암호화된 비밀번호
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 비밀번호 검증
     * 
     * @param rawPassword     평문 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @return 일치 여부
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 비밀번호 변경 시 현재 비밀번호 검증
     * 
     * @param userEntity      사용자 엔티티
     * @param currentPassword 현재 비밀번호
     * @throws ApplicationException.UserPasswordNotMatchException 비밀번호 불일치 시
     */
    public void validateCurrentPassword(UserEntity userEntity, String currentPassword) {
        if (!verifyPassword(currentPassword, userEntity.getUserPassword())) {
            throw new ApplicationException.UserPasswordNotMatchException(userEntity.getUserId());
        }
    }
}