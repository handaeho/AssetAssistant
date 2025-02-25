package kr.daeho.AssetAssistant.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.daeho.AssetAssistant.users.dto.UserDto;
import kr.daeho.AssetAssistant.users.entity.UserEntity;
import kr.daeho.AssetAssistant.users.interfaces.UserInterfaces;
import kr.daeho.AssetAssistant.users.repository.UserReposiory;
import kr.daeho.AssetAssistant.exceptions.ApplicationExceptions;

/**
 * 사용자 서비스
 * 
 * 사용자 정보를 조회, 등록, 수정, 삭제하는 기능을 제공
 * 
 * 컨트롤러에서 요청을 받아 비즈니스 로직을 처리하고, 결과를 반환
 * 
 * 핵심 비즈니스 로직 처리, 트랜잭션 관리, 예외 처리 등 웹 요청 및 응답을 위한 실제 로직 처리
 * 
 * 인터페이스를 상속받아 서비스를 구현(implements)함으로써,
 * 
 * 컨트롤러는 서비스(실제 구현체)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보
 * 
 * @Service: 서비스 클래스임을 명시
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserInterfaces {
    // 사용자 리포지토리 선언
    // final로 선언해 불변성 보장, @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final UserReposiory userReposiory;

    // SecurityConfig에서 @Bean으로 설정한 PasswordEncoder 빈 주입
    // 애플리케이션 전반에서 동일한 인스턴스를 사용 보장 (일관성 유지)
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 정보 조회
     *
     * @param userId 조회할 사용자의 아이디
     * @return UserDto 변환된 사용자 정보 객체
     * @throws IllegalArgumentException 만약 userId가 null 또는 빈 문자열이면 발생
     * @throws ApplicationExceptions    사용자를 찾지 못하거나 조회 중 예외 발생 시 발생
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserInfo(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("해당 사용자 아이디가 없습니다.");
        }
        try {
            // DB에서 사용자 검색 후, Entity 객체로 불러오기
            UserEntity userEntity = userReposiory.findByUserId(userId)
                    .orElseThrow(() -> new ApplicationExceptions("USER_NOT_FOUND", "해당 사용자 정보를 찾을 수 없습니다: " + userId));
            // Entity 객체를 Dto 객체로 변환하여 반환
            return UserDto.fromUserEntity(userEntity);
        } catch (Exception e) {
            throw new ApplicationExceptions("USER_NOT_FOUND", "사용자 정보 조회 실패", e);
        }
    }

    // TODO: 삭제
    /**
     * 사용자 등록
     *
     * @param userDto 등록할 사용자 정보가 담긴 DTO 객체
     * @return UserDto 저장된 사용자 정보 반환 (필요한 정보만 포함)
     * @throws IllegalArgumentException 만약 userDto가 null/빈값이면 발생
     * @throws ApplicationExceptions    등록 중 예외 발생 시 발생
     */
    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        // 사용자 아이디 중복 체크
        if (userReposiory.existsByUserId(userDto.getUserId())) {
            throw new ApplicationExceptions("USER_ALREADY_EXISTS", "이미 존재하는 아이디입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userDto.getUserPassword());

        try {
            // 입력받은 Dto 객체와 암호화 된 비밀번호를 Entity 객체로 변환
            UserEntity userEntity = UserEntity.builder()
                    .userId(userDto.getUserId())
                    .userName(userDto.getUserName())
                    .userPassword(encodedPassword)
                    .userAge(userDto.getUserAge())
                    .userJob(userDto.getUserJob())
                    .build();

            // 변환된 Entity 객체를 DB에 저장
            UserEntity savedUserEntity = userReposiory.save(userEntity);

            // 저장된 Entity 객체를 Dto 객체로 변환하여 반환 (Entity의 세부 정보를 노출하지 않도록 필요한 정보만 반환)
            return UserDto.fromUserEntity(savedUserEntity);
        } catch (Exception e) {
            throw new ApplicationExceptions("USER_CREATE_FAILED", "사용자 정보 등록 실패", e);
        }
    }

    /**
     * 사용자 정보 수정
     *
     * @param userId  수정할 사용자의 아이디 (경로 변수 혹은 파라미터로 전달됩니다.)
     * @param userDto 수정할 사용자 정보가 담긴 DTO 객체
     * @return UserDto 수정된 사용자 정보 반환
     * @throws IllegalArgumentException 만약 userId 또는 userDto가 null/빈값이면 발생
     * @throws ApplicationExceptions    수정 중 예외 발생 시 발생
     */
    @Override
    @Transactional
    public UserDto updateUser(String userId, UserDto userDto) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("해당 사용자 아이디가 없습니다.");
        }
        if (userDto == null) {
            throw new IllegalArgumentException("사용자 정보를 입력해 주세요.");
        }
        try {
            // DB에서 사용자 검색 후, Entity 객체를 파라미터로 전달된 userId를 기준으로 조회
            UserEntity existingUser = userReposiory.findByUserId(userId)
                    .orElseThrow(() -> new ApplicationExceptions("USER_NOT_FOUND", "해당 사용자 정보를 찾을 수 없습니다."));
            // 입력받은 Dto 객체의 정보를 바탕으로 Entity 객체의 사용자 정보 수정
            existingUser.updateUserName(userDto.getUserName());
            existingUser.updateUserPassword(userDto.getUserPassword());
            existingUser.updateUserAge(userDto.getUserAge());
            existingUser.updateUserJob(userDto.getUserJob());
            // 수정된 Entity 객체를 DB에 저장
            UserEntity updatedUserEntity = userReposiory.save(existingUser);
            // 수정된 Entity 객체를 Dto 객체로 변환하여 반환
            return UserDto.fromUserEntity(updatedUserEntity);
        } catch (Exception e) {
            // 수정 중 발생한 예외를 통합 예외로 래핑하여 throw
            throw new ApplicationExceptions("USER_UPDATE_FAILED", "사용자 정보 수정 실패", e);
        }
    }

    /**
     * 사용자 정보 삭제
     *
     * @param userId 삭제할 사용자의 아이디
     * @throws IllegalArgumentException 만약 userId가 null 또는 빈 문자열이면 발생
     * @throws ApplicationExceptions    삭제 중 예외 발생 시 발생
     */
    @Override
    @Transactional
    public void deleteUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("해당 사용자 아이디가 없습니다.");
        }
        try {
            // DB에서 사용자 아이디를 기준으로 삭제 수행
            userReposiory.deleteByUserId(userId);
        } catch (Exception e) {
            throw new ApplicationExceptions("USER_DELETE_FAILED", "사용자 정보 삭제 실패", e);
        }
    }
}
