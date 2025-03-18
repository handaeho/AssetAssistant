package kr.daeho.AssetAssistant.assets.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.util.StringUtils;

import kr.daeho.AssetAssistant.assets.dto.AssetsDto;
import kr.daeho.AssetAssistant.assets.entity.AssetsEntity;
import kr.daeho.AssetAssistant.assets.interfaces.AssetsInterfaces;
import kr.daeho.AssetAssistant.assets.repository.AssetsRepository;
import kr.daeho.AssetAssistant.assets.vo.IncomeVo;
import kr.daeho.AssetAssistant.assets.vo.ExpenseVo;
import kr.daeho.AssetAssistant.assets.vo.AssetsDetailVo;
import kr.daeho.AssetAssistant.assets.vo.AssetType;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;
import kr.daeho.AssetAssistant.common.utils.ModelMapper;

/**
 * 자산 관리 서비스 클래스
 * 
 * 자산 정보를 조회, 등록, 수정, 삭제하는 기능을 제공
 * 
 * 컨트롤러에서 요청을 받아 비즈니스 로직을 처리하고, 결과를 반환
 * 
 * 핵심 비즈니스 로직 처리, 트랜잭션 관리, 예외 처리 등 웹 요청 및 응답을 위한 실제 로직 처리
 * 
 * [자동 계산 기능]
 * - 총 자산(totalAssets): 자산 상세 목록(assetDetails)의 합계로 자동 계산
 * - 총 수입(totalIncome): 수입 목록(incomes)의 합계로 자동 계산
 * - 총 지출(totalExpense): 지출 목록(expenses)의 합계로 자동 계산
 * - 자산 타입별 비율(assetsTypeRatios): 각 자산 유형별 비율을 총 자산 대비 백분율로 계산
 * - 수입 대비 지출 비율(incomeExpenseRatio): 총 지출액 / 총 수입액 * 100으로 계산
 * 
 * 인터페이스를 상속받아 서비스를 구현(implements)함으로써,
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
    // final로 선언해 불변성 보장, @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final AssetsRepository assetsRepository; // 자산 리포지토리 선언
    private final ModelMapper modelMapper; // DTO와 Entity 간 변환 처리

    /**
     * 자산 정보 조회
     * 
     * @Transactional(readOnly = true): 읽기 전용 트랜잭션으로 성능 최적화
     *                         - isolation = READ_COMMITTED로 설정하여 더티 리드 방지
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public AssetsDto getAssetsInfo(String userId) {
        log.info("자산 정보 조회 요청 처리: {}", userId);

        // 사용자 ID 유효성 검사
        validateUserId(userId);

        try {
            // DB에서 사용자 아이디로 자산 정보 검색 후, Entity 객체로 불러오기
            AssetsEntity assetsEntity = findAssetsEntityByUserId(userId);

            log.info("자산 정보 조회 완료: {}", userId);

            // Entity 객체를 Dto 객체로 변환하여 반환
            return modelMapper.toAssetsDto(assetsEntity);
        } catch (ApplicationException e) {
            // ApplicationException은 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("자산 정보 조회 중 예상치 못한 오류 발생: {}", userId, e);
            throw new ApplicationException.AssetsNotFoundException(userId, e);
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
        log.info("자산 정보 등록 요청 처리: {}", userId);

        // 사용자 ID 유효성 검사
        validateUserId(userId);

        // 자산 정보 유효성 검사
        validateAssetsDto(assetsDto);

        try {
            // 입력받은 Dto 객체를 Entity 객체로 변환하고 필요 정보 추가
            AssetsEntity assetsEntity = buildAssetsEntityFromDto(userId, assetsDto);

            // 금액 및 비율 계산
            calculateAndUpdateAmounts(assetsEntity);

            // 변환된 Entity 객체를 DB에 저장
            AssetsEntity savedAssetsEntity = assetsRepository.save(assetsEntity);

            log.info("자산 정보 등록 완료: {}", userId);

            // 저장된 Entity 객체를 Dto 객체로 변환하여 반환 -> Entity의 세부 내용 노출 x. 필요한 정보만 반환
            return modelMapper.toAssetsDto(savedAssetsEntity);
        } catch (Exception e) {
            log.error("자산 정보 등록 실패: {}", userId, e);
            throw new ApplicationException.AssetsCreateFailedException(e);
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
        log.info("자산 정보 수정 요청 처리: {}", userId);

        // 사용자 ID 유효성 검사
        validateUserId(userId);

        // 자산 정보 유효성 검사
        validateAssetsDto(assetsDto);

        try {
            // DB에서 사용자 아이디로 자산 정보 검색
            AssetsEntity assetsEntity = findAssetsEntityByUserId(userId);

            // 사용자 입력 받은 DTO 객체를 통해 Entity 객체 업데이트
            updateAssetsEntityFromDto(assetsEntity, assetsDto);

            // 금액 및 비율 계산
            calculateAndUpdateAmounts(assetsEntity);

            // DB에 수정된 Entity 객체 저장
            AssetsEntity updatedEntity = assetsRepository.save(assetsEntity);

            log.info("자산 정보 수정 완료: {}", userId);

            return modelMapper.toAssetsDto(updatedEntity);
        } catch (ApplicationException e) {
            // ApplicationException은 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("자산 정보 수정 실패: {}", userId, e);
            throw new ApplicationException.AssetsUpdateFailedException(e);
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
        log.info("자산 정보 삭제 요청 처리: {}", userId);

        // 사용자 ID 유효성 검사
        validateUserId(userId);

        try {
            // DB에서 사용자 아이디로 자산 정보 검색 후, 해당 내용 삭제
            assetsRepository.deleteByUserId(userId);
            log.info("자산 정보 삭제 완료: {}", userId);
        } catch (Exception e) {
            log.error("자산 정보 삭제 실패: {}", userId, e);
            throw new ApplicationException.AssetsDeleteFailedException(e);
        }
    }

    /**
     * 사용자 ID 유효성 검사
     * 
     * StringUtils.hasText: null이 아니고, 공백 문자만으로 이루어지지 않은 실제 텍스트를 포함하고 있는지
     * 
     * @param userId 검증할 사용자 ID
     * @throws IllegalArgumentException 사용자 ID가 유효하지 않을 경우
     */
    private void validateUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("올바른 사용자 아이디를 입력해 주세요.");
        }
    }

    /**
     * 자산 DTO 유효성 검사
     * 
     * @param assetsDto 검증할 자산 DTO
     * @throws IllegalArgumentException 자산 DTO가 유효하지 않을 경우
     */
    private void validateAssetsDto(AssetsDto assetsDto) {
        if (assetsDto == null) {
            throw new IllegalArgumentException("올바른 자산 정보를 입력해 주세요.");
        }
    }

    /**
     * 사용자 ID로 자산 엔티티 조회
     * 
     * @param userId 사용자 ID
     * @return 자산 엔티티
     * @throws ApplicationException.AssetsNotFoundException 자산 정보가 없을 경우
     */
    private AssetsEntity findAssetsEntityByUserId(String userId) {
        return assetsRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException.AssetsNotFoundException(userId));
    }

    /**
     * DTO 정보를 바탕으로 새 자산 엔티티 생성
     * 
     * @param userId    사용자 ID
     * @param assetsDto 자산 DTO
     * @return 생성된 자산 엔티티
     */
    private AssetsEntity buildAssetsEntityFromDto(String userId, AssetsDto assetsDto) {
        return AssetsEntity.builder()
                .userId(userId)
                .totalAssets(0) // 자동 계산 예정
                .totalIncome(0) // 자동 계산 예정
                .totalExpense(0) // 자동 계산 예정
                .incomeExpenseRatio(0.0) // 자동 계산 예정
                .incomes(assetsDto.getIncomes() != null ? assetsDto.getIncomes() : new ArrayList<>())
                .expenses(assetsDto.getExpenses() != null ? assetsDto.getExpenses() : new ArrayList<>())
                .assetDetails(assetsDto.getAssetDetails() != null ? assetsDto.getAssetDetails() : new ArrayList<>())
                .assetsTypeRatios(new HashMap<>()) // 자동 계산 예정
                .build();
    }

    /**
     * 자산 엔티티 업데이트
     * 
     * @param entity 업데이트할 엔티티
     * @param dto    적용할 DTO 정보
     */
    private void updateAssetsEntityFromDto(AssetsEntity entity, AssetsDto dto) {
        // 수입, 지출, 자산 상세 정보 업데이트
        entity.updateIncomes(dto.getIncomes() != null ? dto.getIncomes() : new ArrayList<>());
        entity.updateExpenses(dto.getExpenses() != null ? dto.getExpenses() : new ArrayList<>());
        entity.updateAssetDetails(dto.getAssetDetails() != null ? dto.getAssetDetails() : new ArrayList<>());
    }

    /**
     * 모든 금액과 비율을 계산하고 엔티티를 업데이트
     * 
     * @param entity 계산 및 업데이트할 엔티티
     */
    private void calculateAndUpdateAmounts(AssetsEntity entity) {
        // 총 수입 계산 및 업데이트
        int totalIncome = calculateTotalIncome(entity.getIncomes());
        entity.updateTotalIncome(totalIncome);

        // 총 지출 계산 및 업데이트
        int totalExpense = calculateTotalExpense(entity.getExpenses());
        entity.updateTotalExpense(totalExpense);

        // 총 자산 계산 및 업데이트
        int totalAssets = calculateTotalAssets(entity.getAssetDetails());
        entity.updateTotalAssets(totalAssets);

        // 수입 대비 지출 비율 계산 및 업데이트
        double incomeExpenseRatio = calculateIncomeExpenseRatio(totalIncome, totalExpense);
        entity.updateIncomeExpenseRatio(incomeExpenseRatio);

        // 자산 타입별 비율 계산 및 업데이트
        Map<String, Double> assetsTypeRatios = calculateAssetsTypeRatios(entity.getAssetDetails(), totalAssets);
        entity.updateAssetsTypeRatios(assetsTypeRatios);
    }

    /**
     * 총 수입 계산
     * 사용자가 입력한 모든 수입 항목(월급, 배당금 등)의 합계를 계산
     * 
     * @param incomes 수입 목록
     * @return 계산된 총 수입 금액
     */
    private int calculateTotalIncome(List<IncomeVo> incomes) {
        if (incomes == null || incomes.isEmpty()) {
            return 0;
        }

        return incomes.stream()
                .mapToInt(IncomeVo::getIncomeAmount)
                .sum();
    }

    /**
     * 총 지출 계산
     * 사용자가 입력한 모든 지출 항목(월세, 식비 등)의 합계를 계산
     * 
     * @param expenses 지출 목록
     * @return 계산된 총 지출 금액
     */
    private int calculateTotalExpense(List<ExpenseVo> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            return 0;
        }

        return expenses.stream()
                .mapToInt(ExpenseVo::getExpenseAmount)
                .sum();
    }

    /**
     * 자산 상세 목록에서 총 자산 금액 계산
     * 사용자가 입력한 모든 자산 항목(주식, 적금 등)의 금액 합계를 계산
     * 
     * @param assetDetails 자산 상세 목록
     * @return 계산된 총 자산 금액
     */
    private int calculateTotalAssets(List<AssetsDetailVo> assetDetails) {
        if (assetDetails == null || assetDetails.isEmpty()) {
            return 0;
        }

        // 모든 자산의 금액을 합산
        return assetDetails.stream()
                .mapToInt(AssetsDetailVo::getAssetsPrice)
                .sum();
    }

    /**
     * 수입 대비 지출 비율 계산
     * 총 지출액을 총 수입액으로 나누고 100을 곱하여 백분율 계산
     * 
     * @param totalIncome  총 수입
     * @param totalExpense 총 지출
     * @return 계산된 수입 대비 지출 비율
     */
    private double calculateIncomeExpenseRatio(int totalIncome, int totalExpense) {
        // 수입 금액이 0보다 크면, 수입 대비 지출 비율 계산
        return totalIncome > 0 ? ((double) totalExpense / totalIncome * 100) : 0.0;
    }

    /**
     * 자산 타입별 비율 계산
     * 자산 타입별로 금액을 합산 및 각 타입별 금액이 총 자산에서 차지하는 비율을 계산
     * 
     * @param assetDetails 자산 상세 목록
     * @param totalAssets  총 자산 금액
     * @return 자산 타입별 비율 Map
     */
    private Map<String, Double> calculateAssetsTypeRatios(List<AssetsDetailVo> assetDetails, int totalAssets) {
        Map<String, Double> assetsTypeRatio = new HashMap<>(); // 자산 타입별 비율
        Map<AssetType, Integer> assetsTypeAmount = new HashMap<>(); // 자산 타입별 금액

        if (assetDetails == null || assetDetails.isEmpty() || totalAssets <= 0) {
            return assetsTypeRatio;
        }

        // 자산 타입별 금액 합산
        for (AssetsDetailVo detail : assetDetails) {
            AssetType assetType = detail.getAssetsType();
            int assetPrice = detail.getAssetsPrice();

            // 기존 금액 + 새 금액 계산
            // getOrDefault: 특정 키에 매핑된 값이 존재하면 그 값 반환, 없으면 기본 값 반환
            // assetsTypeAmount 맵에서 assetType 키에 해당하는 값이 있으면 그 값을 가져오고, 없으면 0을 기본 값으로 사용
            // 그리고 그 값과 assetPrice를 더한 값을 다시 assetsTypeAmount 맵에 저장
            assetsTypeAmount.put(assetType,
                    assetsTypeAmount.getOrDefault(assetType, 0) + assetPrice);
        }

        // 각 자산 타입별 비율 계산
        for (Map.Entry<AssetType, Integer> entry : assetsTypeAmount.entrySet()) {
            AssetType assetType = entry.getKey();
            int amount = entry.getValue();
            double ratio = ((double) amount / totalAssets) * 100;
            assetsTypeRatio.put(assetType.getCode(), ratio);
        }

        return assetsTypeRatio;
    }

    /*
     * 입력 예시 (자동 계산):
     * {
     * "incomes": [
     * {
     * "incomeName": "월급",
     * "incomeAmount": 2000000,
     * "incomeType": "고정"
     * },
     * {
     * "incomeName": "주식배당금",
     * "incomeAmount": 1000000,
     * "incomeType": "변동"
     * }
     * ],
     * "expenses": [
     * {
     * "expenseName": "월세",
     * "expenseAmount": 1000000,
     * "expenseType": "고정"
     * },
     * {
     * "expenseName": "식비",
     * "expenseAmount": 500000,
     * "expenseType": "변동"
     * }
     * ],
     * "assetDetails": [
     * {
     * "assetsName": "삼성전자",
     * "assetsPrice": 500000,
     * "assetsType": "주식",
     * },
     * {
     * "assetsName": "국민은행적금",
     * "assetsPrice": 500000,
     * "assetsType": "적금",
     * }
     * ]
     * }
     * 
     * 자동 계산 결과:
     * - totalIncome: 3000000 (2000000 + 1000000)
     * - totalExpense: 1500000 (1000000 + 500000)
     * - totalAssets: 1000000 (500000 + 500000)
     * - assetsTypeRatios: { "주식": 50.0, "적금": 50.0 }
     * - incomeExpenseRatio: 50.0 (1500000 / 3000000 * 100)
     */
}
