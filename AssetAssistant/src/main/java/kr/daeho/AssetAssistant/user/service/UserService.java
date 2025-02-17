package kr.daeho.AssetAssistant.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.daeho.AssetAssistant.user.dto.UserDto;
import kr.daeho.AssetAssistant.user.entity.UserEntity;
import kr.daeho.AssetAssistant.user.interfaces.UserInterfaces;
import kr.daeho.AssetAssistant.user.repository.UserReposiory;
import kr.daeho.AssetAssistant.user.exception.UserException;

/**
 * 사용자 서비스
 * 
 * 사용자 정보를 조회, 등록, 수정, 삭제하는 기능을 제공
 * 
 * 컨트롤러에서 요청을 받아 비즈니스 로직을 처리하고, 결과를 반환
 * 
 * 핵심 비즈니스 로직 처리, 트랜잭션 관리, 예외 처리 등 웹 요청 및 응답을 위한 실제 로직 처리
 * 
 * 인터페이스를 상속받아 서비스를 구현함으로써, 
 * 
 * 컨트롤러는 서비스(실제 구현체)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보보
 * 
 * @Service: 서비스 클래스임을 명시
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (final 및 notNull 필드에 대한 생성자 자동 생성)
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserInterfaces {
    // 사용자 리포지토리 선언 (final로 선언해 불변성 보장)
    private final UserReposiory userReposiory;

    // 사용자 정보 조회
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserInfo(String userId) {
        if(userId == null || userId.isEmpty()){
            throw new IllegalArgumentException("해당 사용자 아이디가 없습니다.");
        }

        try{
            // DB에서 사용자 검색 후, Entity 객체로 불러오기
            UserEntity userEntity = userReposiory.findByUserId(userId);
            // Entity 객체를 Dto 객체로 변환하여 반환
            return UserDto.convertEntityToDto(userEntity);
        } catch (Exception e) {
            throw new UserException("사용자 정보 조회 실패");
        }
    }

    // 사용자 정보 등록
    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if(userDto == null) {
            throw new IllegalArgumentException("사용자 정보를 입력해 주세요.");
        }

        try {
            // 입력받은 Dto 객체를 Entity 객체로 변환
            UserEntity userEntity = UserEntity.builder()
                .userId(userDto.getUserId())
                .userName(userDto.getUserName())
                .userPassword(userDto.getUserPassword())
                .userAge(userDto.getUserAge())
                .userJob(userDto.getUserJob())
                .build();
            // 변환된 Entity 객체를 DB에 저장
            UserEntity savedUserEntity = userReposiory.save(userEntity);
            // 저장된 Entity 객체를 Dto 객체로 변환하여 반환
            // Entity의 세부 내용 노출 x. 필요한 정보만 반환
            return UserDto.convertEntityToDto(savedUserEntity);
        } catch (Exception e) {
            throw new UserException("사용자 정보 등록 실패");
        }
    }

    // 사용자 정보 수정
    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto) {
        if(userDto == null) {
            throw new IllegalArgumentException("사용자 정보를 입력해 주세요.");
        } else if(userDto.getUserId() == null || userDto.getUserId().isEmpty()) {
            throw new IllegalArgumentException("올바른 사용자 아이디를 입력해 주세요.");
        }

        try {
            // DB에서 사용자 검색 후, Entity 객체로 불러오기
            UserEntity userEntity = userReposiory.findByUserId(userDto.getUserId());
            // 해당 사용자 아이디가 없으면 예외 발생
            if(userEntity == null) {
                throw new IllegalArgumentException("사용자 아이디를 찾을 수 없습니다.");
            }
            // 입력받은 Dto 객체를 통해 사용자 정보 수정
            userEntity.updateUserName(userDto.getUserName());
            userEntity.updateUserPassword(userDto.getUserPassword());
            userEntity.updateUserAge(userDto.getUserAge());
            userEntity.updateUserJob(userDto.getUserJob());
            // 수정된 Entity 객체를 DB에 저장
            UserEntity updatedUserEntity = userReposiory.save(userEntity);
            // 수정된 Entity 객체를 Dto 객체로 변환하여 반환
            return UserDto.convertEntityToDto(updatedUserEntity);
        } catch (Exception e) {
            throw new UserException("사용자 정보 수정 실패");
        }
    }

    // 사용자 정보 삭제
    @Override
    @Transactional
    public void deleteUser(String userId) {
        if(userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("사용자 아이디를 찾을 수 없습니다.");
        }
        try {
            // DB에서 사용자 검색 후, 해당 내용 삭제
            userReposiory.deleteByUserId(userId);
        } catch (Exception e) {
            throw new UserException("사용자 정보 삭제 실패");
        }
    }

}
