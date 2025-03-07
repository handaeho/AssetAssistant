package kr.daeho.AssetAssistant.common.utils;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import kr.daeho.AssetAssistant.users.dto.SignupRequestDto;
import kr.daeho.AssetAssistant.users.dto.UserDto;
import kr.daeho.AssetAssistant.users.entity.UserEntity;
import kr.daeho.AssetAssistant.assets.dto.AssetsDto;
import kr.daeho.AssetAssistant.assets.entity.AssetsEntity;

/**
 * Entity-DTO 변환을 처리하는 공통 유틸리티. 모든 모델 변환 로직을 중앙화 (Entity -> DTO, DTO -> Entity)
 * 
 * Entity -> DTO:
 * DB에서 클라이언트에게 전달할 데이터만 빌드
 * Entity를 DTO로 변환할 때는 특정 DTO 인스턴스의 상태가 필요하지 않으므로 static 메서드로 구현
 * 
 * DTO -> Entity:
 * 신규 생성이라면, DB에서 필요한 모든 필드 값을 빌드해야 하고 (기본 값, 자동 생성 값 등 포함)
 * 업데이트라면, 업데이트 할 필드만 빌드하면 나머지는 DB에 있는 기존 값을 유지하게 가능
 * DTO를 Entity로 변환할 때는 특정 DTO 인스턴스의 현재 상태(this)가 필요하므로 인스턴스 메서드로 구현
 * 
 * @Component: 스프링 컨테이너에 빈으로 등록
 *             - Bean: 스프링 컨테이너에 의해 관리되는 객체
 *             - 범용적인 빈 등록을 위한 어노테이션. 특별한 프록시 처리는 하지 않음
 *             - 설정 관련 빈 등록 및 프록시 설정을 위한 @Configuration 컴포넌트보다 범용적으로 사용하는 용도
 */
@Component
public class ModelMapper {
    /**
     * UserEntity를 UserDto로 변환
     */
    public UserDto toUserDto(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        return UserDto.builder()
                .userId(userEntity.getUserId())
                .userName(userEntity.getUsername())
                .userAge(userEntity.getUserAge())
                .userJob(userEntity.getUserJob())
                .userUpdatedAt(userEntity.getUserUpdatedAt())
                .build();
    }

    /**
     * UserDto를 UserEntity로 변환
     */
    public UserEntity toUserEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        return UserEntity.builder()
                .userId(userDto.getUserId())
                .userName(userDto.getUserName())
                .userAge(userDto.getUserAge())
                .userJob(userDto.getUserJob())
                .userUpdatedAt(userDto.getUserUpdatedAt())
                .build();
    }

    /**
     * AssetsEntity를 AssetsDto로 변환
     */
    public AssetsDto toAssetsDto(AssetsEntity entity) {
        if (entity == null)
            return null;

        return AssetsDto.builder()
                .userId(entity.getUserId())
                .totalAssets(entity.getTotalAssets())
                .income(entity.getIncome())
                .expense(entity.getExpense())
                .assetDetails(entity.getAssetDetails())
                .assetsTypeRatios(entity.getAssetsTypeRatios())
                .incomeExpenseRatio(entity.getIncomeExpenseRatio())
                .build();
    }

    /**
     * AssetsDto를 AssetsEntity로 변환
     */
    public AssetsEntity toAssetsEntity(AssetsDto dto) {
        if (dto == null)
            return null;

        return AssetsEntity.builder()
                .userId(dto.getUserId())
                .totalAssets(dto.getTotalAssets())
                .income(dto.getIncome())
                .expense(dto.getExpense())
                .assetDetails(dto.getAssetDetails())
                .assetsTypeRatios(dto.getAssetsTypeRatios())
                .incomeExpenseRatio(dto.getIncomeExpenseRatio())
                .build();
    }

    /**
     * SignUpRequestDto를 UserEntity로 변환
     */
    public UserEntity signUpRequestToUserEntity(SignupRequestDto signupRequestDto) {
        if (signupRequestDto == null) {
            return null;
        }

        return UserEntity.builder()
                .userId(signupRequestDto.getUserId())
                .userName(signupRequestDto.getUserName())
                .build();
    }

    /**
     * UserEntity 목록을 UserDto 목록으로 변환
     */
    public List<UserDto> toUserDtoList(List<UserEntity> userEntities) {
        if (userEntities == null) {
            return null;
        }

        return userEntities.stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    /**
     * 기존 UserEntity에 UserDto 데이터를 적용해 업데이트
     */
    public void updateUserEntityFromDto(UserEntity entity, UserDto dto) {
        if (entity == null || dto == null) {
            return;
        }

        // null이 아닌 필드만 업데이트
        if (dto.getUserName() != null) {
            entity.updateUserName(dto.getUserName());
        }

        if (dto.getUserAge() != 0) {
            entity.updateUserAge(dto.getUserAge());
        }

        if (dto.getUserJob() != null) {
            entity.updateUserJob(dto.getUserJob());
        }
    }

    // NOTE: 다른 변환 메서드들 추가 해가면서 작성
}
