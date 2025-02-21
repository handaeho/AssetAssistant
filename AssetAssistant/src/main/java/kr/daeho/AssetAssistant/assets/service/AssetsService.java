package kr.daeho.AssetAssistant.assets.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import kr.daeho.AssetAssistant.assets.dto.AssetsDto;
import kr.daeho.AssetAssistant.assets.entity.AssetsEntity;
import kr.daeho.AssetAssistant.assets.interfaces.AssetsInterfaces;
import kr.daeho.AssetAssistant.assets.repository.AssetsRepository;
import kr.daeho.AssetAssistant.assets.exception.AssetsException;
import kr.daeho.AssetAssistant.assets.vo.IncomeVo;
import kr.daeho.AssetAssistant.assets.vo.ExpenseVo;
import kr.daeho.AssetAssistant.assets.vo.AssetsDetailVo;

/**
 * 자산 관리 서비스 클래스
 * 
 * 자산 정보를 조회, 등록, 수정, 삭제하는 기능을 제공
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
public class AssetsService implements AssetsInterfaces {
    // 자산 리포지토리 선언
    // final로 선언해 불변성 보장, @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final AssetsRepository assetsRepository;

    /**
     * 자산 정보 조회
     * 
     * @Transactional(readOnly = true): 읽기 전용 트랜잭션으로 성능 최적화
     *                         - isolation = READ_COMMITTED로 설정하여 더티 리드 방지
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public AssetsDto getAssetsInfo(String userId) {
        validateUserId(userId);
        try {
            log.info("Fetching assets info for user: {}", userId);
            // DB에서 사용자 아이디로 자산 정보 검색 후, Entity 객체로 불러오기
            AssetsEntity assetsEntity = assetsRepository.findByUserId(userId)
                    .orElseThrow(() -> new AssetsException.AssetNotFoundException("자산 정보를 찾을 수 없습니다: " + userId));
            // Entity 객체를 Dto 객체로 변환하여 반환
            return AssetsDto.fromAssetsEntity(assetsEntity);
        } catch (Exception e) {
            log.error("Failed to fetch assets info for user: {}", userId, e);
            throw new AssetsException("ASSETS_NOT_FOUND", "자산 정보 조회 실패", e);
        }
    }

    /**
     * 자산 정보 등록
     * 
     * @Transactional: isolation = READ_COMMITTED로 설정하여 더티 리드 방지
     *                 - 기본 propagation = REQUIRED 사용
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AssetsDto createAssets(String userId, AssetsDto assetsDto) {
        validateUserId(userId);
        validateAssetData(userId, assetsDto);
        try {
            // 입력받은 Dto 객체를 Entity 객체로 변환
            AssetsEntity assetsEntity = AssetsEntity.builder()
                    .userId(assetsDto.getUserId())
                    .totalAssets(assetsDto.getTotalAssets())
                    .income(assetsDto.getIncome())
                    .expense(assetsDto.getExpense())
                    .assetDetails(assetsDto.getAssetDetails())
                    .assetsTypeRatios(assetsDto.getAssetsTypeRatios())
                    .incomeExpenseRatio(assetsDto.getIncomeExpenseRatio())
                    .build();
            // 변환된 Entity 객체를 DB에 저장
            AssetsEntity savedAssetsEntity = assetsRepository.save(assetsEntity);
            // 저장된 Entity 객체를 Dto 객체로 변환하여 반환 -> Entity의 세부 내용 노출 x. 필요한 정보만 반환
            return AssetsDto.fromAssetsEntity(savedAssetsEntity);
        } catch (Exception e) {
            throw new AssetsException("ASSETS_CREATE_FAILED", "자산 정보 등록 실패", e);
        }
    }

    /**
     * 자산 정보 업데이트
     * 
     * @Transactional: isolation = READ_COMMITTED로 설정하여 더티 리드 방지
     *                 - 기본 propagation = REQUIRED 사용
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AssetsDto updateAssets(String userId, AssetsDto assetsDto) {
        validateAssetData(userId, assetsDto);
        try {
            // DB에서 사용자 아이디로 자산 정보 검색 후, Entity 객체로 불러오기
            AssetsEntity assetsEntity = assetsRepository.findByUserId(userId)
                    .orElseThrow(() -> new AssetsException.AssetNotFoundException("자산 정보를 찾을 수 없습니다: " + userId));

            // 사용자 입력 받은 DTO 객체를 통해 Entity 객체 업데이트
            Map<String, Double> assetsTypeRatios = updateEntityFromDto(assetsEntity, assetsDto);
            assetsEntity.updateAssetsTypeRatios(assetsTypeRatios);

            // DB에 수정된 Entity 객체 저장
            AssetsEntity updatedEntity = assetsRepository.save(assetsEntity);
            log.info("Successfully updated assets for user: {}", userId);
            return AssetsDto.fromAssetsEntity(updatedEntity);
        } catch (Exception e) {
            log.error("Failed to update assets for user: {}", userId, e);
            throw new AssetsException("ASSETS_UPDATE_FAILED", "자산 정보 수정 실패", e);
        }
    }

    /**
     * 자산 정보 삭제
     * 
     * @Transactional: isolation = READ_COMMITTED로 설정하여 더티 리드 방지
     *                 - 기본 propagation = REQUIRED 사용
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteAssets(String userId) {
        validateUserId(userId);
        try {
            // DB에서 사용자 아이디로 자산 정보 검색 후, 해당 내용 삭제
            assetsRepository.deleteByUserId(userId);
        } catch (Exception e) {
            throw new AssetsException("ASSETS_DELETE_FAILED", "자산 정보 삭제 실패", e);
        }
    }

    /**
     * 사용자 입력 받은 정보(DTO) 기반, 수입/지출 비율 계산 메소드
     * 
     * @param assetsDto 자산 정보가 담긴 DTO
     * @return 수입 대비 지출 비율
     */
    private double computeIncomeExpenseRatio(AssetsDto assetsDto) {
        // 수입 정보 또는 지출 정보가 없으면
        if (assetsDto.getIncome() == null || assetsDto.getExpense() == null) {
            return 0.0;
        }

        // 수입 및 지출 정보 가져오기
        IncomeVo income = assetsDto.getIncome();
        ExpenseVo expense = assetsDto.getExpense();

        // 수입 금액 및 지출 금액 가져오기
        int incomeAmount = income.getIncomeAmount();
        int totalExpenseAmount = expense.getFixedExpenseAmount() + expense.getVariableExpenseAmount(); // 고정 지출 + 변동 지출

        // 수입 금액이 0보다 크면, 수입 대비 지출 비율 계산
        return incomeAmount > 0 ? ((double) totalExpenseAmount / incomeAmount * 100) : 0.0;
    }

    /**
     * 사용자 입력 받은 정보(DTO) 기반, 자산 타입별 비율을 계산하는 메소드
     * 
     * @param assetsDto 자산 정보가 담긴 DTO
     * @return 자산 타입별 비율이 담긴 Map
     */
    private Map<String, Double> computeAssetsTypeRatio(AssetsDto assetsDto) {
        Map<String, Double> assetsTypeRatio = new HashMap<>(); // 자산 타입별 비율
        Map<String, Integer> assetsTypeAmount = new HashMap<>(); // 자산 타입별 금액

        // 전체 자산 금액
        int totalAssets = assetsDto.getTotalAssets();
        if (totalAssets <= 0) {
            return assetsTypeRatio;
        }

        // 자산 타입별 금액 합산
        List<AssetsDetailVo> assetDetails = assetsDto.getAssetDetails();
        if (assetDetails != null) {
            for (AssetsDetailVo detail : assetDetails) {
                String assetType = detail.getAssetsType(); // 자산 타입
                int assetPrice = detail.getAssetsPrice(); // 자산 금액
                assetsTypeAmount.merge(assetType, assetPrice, Integer::sum); // 자산 타입별 금액 합산 (같은 타입의 자산은 합산)
            }
        }

        // 각 자산 타입별 비율 계산
        for (Map.Entry<String, Integer> entry : assetsTypeAmount.entrySet()) {
            String assetType = entry.getKey();
            int amount = entry.getValue();
            double ratio = ((double) amount / totalAssets) * 100;
            assetsTypeRatio.put(assetType, ratio);
        }

        return assetsTypeRatio;
    }

    /**
     * 사용자 아이디 유효성 검사
     * 
     * @param userId 사용자 아이디
     */
    private void validateUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("해당 사용자 아이디가 없습니다.");
        }
    }

    /**
     * 자산 정보 유효성 검사
     * 
     * @param userId    사용자 아이디
     * @param assetsDto 자산 정보가 담긴 DTO
     */
    private void validateAssetData(String userId, AssetsDto assetsDto) {
        validateUserId(userId);
        if (assetsDto == null) {
            throw new IllegalArgumentException("자산 정보를 입력해 주세요.");
        }
    }

    /**
     * 자산 정보 Entity 객체 업데이트
     * 
     * @param entity 자산 정보가 담긴 Entity 객체
     * @param dto    자산 정보가 담긴 DTO
     */
    private Map<String, Double> updateEntityFromDto(AssetsEntity entity, AssetsDto dto) {
        entity.updateTotalAssets(dto.getTotalAssets());
        entity.updateIncome(dto.getIncome());
        entity.updateExpense(dto.getExpense());
        entity.updateAssetDetails(dto.getAssetDetails());

        double incomeExpenseRatio = computeIncomeExpenseRatio(dto);
        entity.updateIncomeExpenseRatio(incomeExpenseRatio);

        Map<String, Double> assetsTypeRatios = computeAssetsTypeRatio(dto);

        return assetsTypeRatios;
    }

    /*
     * 입력 예시:
     * {
     * "userId": "user123",
     * "totalAssets": 1000000,
     * "assetDetails": [
     * {
     * "assetsName": "삼성전자",
     * "assetsType": "주식",
     * "assetsPrice": 500000
     * },
     * {
     * "assetsName": "국민은행적금",
     * "assetsType": "적금",
     * "assetsPrice": 500000
     * }
     * ]
     * }
     * 
     * 자동 계산 된 비율 조회 예시:
     * {
     * "assetsTypeRatios": {
     * "주식": 50.0,
     * "적금": 50.0
     * }
     * }
     */
}
