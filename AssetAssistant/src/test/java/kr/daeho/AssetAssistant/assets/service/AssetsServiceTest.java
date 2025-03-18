package kr.daeho.AssetAssistant.assets.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
// Static Import: 클래스의 static 멤버(필드나 메서드)를 클래스 이름 없이 바로 사용
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import kr.daeho.AssetAssistant.assets.dto.AssetsDto;
import kr.daeho.AssetAssistant.assets.entity.AssetsEntity;
import kr.daeho.AssetAssistant.assets.repository.AssetsRepository;
import kr.daeho.AssetAssistant.assets.vo.AssetsDetailVo;
import kr.daeho.AssetAssistant.assets.vo.ExpenseVo;
import kr.daeho.AssetAssistant.assets.vo.IncomeVo;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;
import kr.daeho.AssetAssistant.common.utils.ModelMapper;

/**
 * AssetsService 단위 테스트 클래스 -> AssetsService 클래스 내 메소드 테스트
 * 
 * Mockito를 사용하여 의존성을 모의(Mock)하고, JUnit을 통해 테스트 케이스를 실행
 * 
 * [Mockito]
 * 1. when(...): 특정 메서드 호출에 대해 예상되는 동작(반환값 또는 예외)을 설정
 * -> public static <T> OngoingStubbing<T> when(T methodCall) { ... }
 * 
 * 2. thenReturn(...): 실제 로직 대신 모의 객체가 반환할 값을 지정
 * -> public static <T> OngoingStubbing<T> thenReturn(T value) { ... }
 * 
 * 3. any(...): 파라미터로 전달되는 객체가 지정된 클래스의 인스턴스라면 어떤 값이든 상관없이 매칭
 * -> public static <T> T any(Class<T> type) { ... }
 * 
 * 4. assertNotNull(...): 특정 객체가 null이 아님을 검증
 * -> public static void assertNotNull(Object object) { ... }
 * 
 * 5. assertThrows(...): 특정 코드 실행 시 지정된 예외가 발생하는지를 검증
 * -> public static <T extends Throwable> T assertThrows(Class<T>
 * exceptionClass, Executable executable) { ... }
 * 
 * 6. assertEquals(...): 예상(expected) 값과 실제(actual) 값이 동일한지를 비교
 * -> public static void assertEquals(Object expected, Object actual) { ... }
 * 
 * 7. verify(...): 모의(mock) 객체의 특정 메서드 호출이 예상대로 이루어졌는지 검증
 * -> public static void verify(Mock<T> mock, VerificationMode mode, Matcher<T>
 * matcher) { ... }
 * 
 * 8. never(...): 모의(mock) 객체의 특정 메서드가 절대 호출되지 않아야 하는지 검증
 * -> public static <T> T never() { ... }
 * 
 * 9. any(): 특정 메서드 호출 시 인자의 실제 값은 중요하지 않을 때, 그 자리에서 "어떤 값이든 상관없다"라고 지정
 * -> 단, 타입은 정확하게 전달 되어야 함
 * -> public static <T> T any(Class<T> type) { ... }
 * 
 * 10. thenAnswer(...): 호출된 메서드의 인자나 상황에 따라 동적으로 값을 반환하도록 설정
 * -> public static <T> Answer<T> thenAnswer(Answer<T> answer) { ... }
 * -> thenReturn과 달리, thenAnswer는 메서드가 호출될 때마다 동적으로 결과를 계산하여 반환
 * -> when(someService.someMethod(any())).thenAnswer(invocation -> {
 * Object arg = invocation.getArgument(0);
 * return "Dynamic response based on: " + arg;
 * ... }); 처럼 람다식 또는 Answer 인터페이스를 구현
 * 
 * InvocationOnMock 객체: 호출 시 전달된 인자, 메서드 이름, 호출 순서 등 호출 정보를 담은 객체
 * invocation.getArgument(0): 호출된 메서드의 첫 번째 인자를 반환
 * invocation.getArguments(): 호출된 메서드의 모든 인자를 배열로 반환
 * invocation.getMethod(): 호출된 메서드 객체 반환
 * invocation.getTarget(): 호출된 메서드를 포함하는 객체 반환
 * invocation.getLocation(): 호출 위치 정보 반환
 * 
 * ex)
 * when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);
 * -> userRepository.save() 메서드가 호출될 때, UserEntity 타입의 어떤 객체가 전달되더라도
 * testUserEntity를 반환하도록 설정
 * -> 인자의 타입만 지정하면, 실제 전달되는 값은 무시.
 * -> 값의 구체적인 내용보다 메서드 호출 자체가 이루어졌는지, 그리고 호출 횟수 등을 검증할 때 유용
 * 
 * [Given - when - Then: Behavior-Driven Development(BDD)의 테스트 시나리오 기술 패턴]
 * 1. Given: 테스트가 시작되기 전에 필요한 초기 상태나 조건을 설정하는 단계
 * -> DB에 특정 사용자 지정, 테스트 할 시스템의 환경 설정 등
 * 
 * 2. When: 실제 테스트 대상 행동을 수행하는 단계. 시스템 상호작용, 특정 메소드 호출 등
 * -> 사용자가 로그인 폼에 정보를 입력 후 로그인 버튼 클릭, 특정 API 엔드포인트에 요청 전송 등
 * 
 * 3. Then: 행동(When)이 실행된 후 기대되는 결과나 상태를 검증하는 단계
 * -> 로그인 성공 시 JWT 토큰 반환, API 요청에 대한 올바른 응답 반환 등
 * 
 * @ExtendWith: JUnit 5 테스트 실행 시, 테스트 클래스나 메서드에 특정 확장의 기능을 추가
 *              - MockitoExtension.class: Mockito 확장을 사용하도록 설정
 */
@ExtendWith(MockitoExtension.class)
public class AssetsServiceTest {
    /**
     * 테스트 대상 객체 - 자산 서비스 (자산 정보 조회, 등록, 수정, 삭제)
     * 
     * @InjectMocks: 모의 객체들을 주입받는 실제 객체
     */
    @InjectMocks
    private AssetsService assetsService;

    /**
     * 모의 객체 - 자산 리포지토리
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private AssetsRepository assetsRepository;

    /**
     * 모의 객체 - 모델 매퍼
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private ModelMapper modelMapper;

    /**
     * 테스트 데이터
     */
    private final String TEST_USER_ID = "testuser";
    private AssetsEntity testAssetsEntity;
    private AssetsDto testAssetsDto;
    private List<AssetsDetailVo> testAssetDetails;
    private Map<String, Double> testAssetsTypeRatios;
    private List<IncomeVo> testIncomes;
    private List<ExpenseVo> testExpenses;

    /**
     * 각 테스트 전에 실행되는 설정 메소드
     * 
     * @BeforeEach: 각 테스트 메소드 실행 전에 호출됨
     */
    @BeforeEach
    void setUp() {
        // 테스트용 자산 상세 정보 생성
        testAssetDetails = new ArrayList<>();
        testAssetDetails.add(AssetsDetailVo.of("삼성전자", "주식", 500000));
        testAssetDetails.add(AssetsDetailVo.of("국민은행적금", "적금", 500000));

        // 테스트용 자산 타입별 비율 생성
        testAssetsTypeRatios = new HashMap<>();
        testAssetsTypeRatios.put("주식", 50.0);
        testAssetsTypeRatios.put("적금", 50.0);

        // 테스트용 수입 정보 생성
        testIncomes = new ArrayList<>();
        testIncomes.add(IncomeVo.of("월급", 2000000, "고정"));
        testIncomes.add(IncomeVo.of("주식배당금", 1000000, "변동"));

        // 테스트용 지출 정보 생성
        testExpenses = new ArrayList<>();
        testExpenses.add(ExpenseVo.of("월세", 1000000, "고정"));
        testExpenses.add(ExpenseVo.of("식비", 500000, "변동"));

        // 테스트용 현재 시간
        LocalDateTime now = LocalDateTime.now();

        // 테스트용 자산 DTO 생성 (자동 계산되는 값들 포함)
        testAssetsDto = AssetsDto.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .totalAssets(1000000) // 실제로는 자동 계산
                .totalIncome(3000000) // 실제로는 자동 계산
                .totalExpense(1500000) // 실제로는 자동 계산
                .incomes(testIncomes)
                .expenses(testExpenses)
                .assetDetails(testAssetDetails)
                .assetsTypeRatios(testAssetsTypeRatios)
                .incomeExpenseRatio(50.0) // 실제로는 자동 계산
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 테스트용 자산 엔티티 생성
        testAssetsEntity = AssetsEntity.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .totalAssets(1000000)
                .totalIncome(3000000)
                .totalExpense(1500000)
                .incomes(testIncomes)
                .expenses(testExpenses)
                .assetDetails(testAssetDetails)
                .assetsTypeRatios(testAssetsTypeRatios)
                .incomeExpenseRatio(50.0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 자산 정보 조회 테스트
     * 
     * 시나리오: 존재하는 사용자 아이디로 조회하면 자산 정보를 반환해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("자산 정보 조회 성공")
    void getAssetsInfo_Success() {
        // Given - 자산 정보 조회 테스트 준비
        // 사용자 아이디로 레포지토리에서 자산 정보 검색 후, 자산 엔티티 반환
        when(assetsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testAssetsEntity));
        // modelMapper를 사용하여 자산 엔티티를 자산 DTO로 변환 후 반환
        when(modelMapper.toAssetsDto(testAssetsEntity)).thenReturn(testAssetsDto);

        // When - 자산 정보 조회 실행
        // 자산 정보 조회 서비스를 호출하고, 사용자 아이디에 해당하는 자산 정보 DTO 반환
        AssetsDto result = assetsService.getAssetsInfo(TEST_USER_ID);

        // Then - 자산 정보 조회 결과 검증
        assertNotNull(result, "자산 정보 조회 결과는 null이 아니어야 함.");
        assertEquals(TEST_USER_ID, result.getUserId(), "사용자 ID가 일치해야 함.");
        assertEquals(1000000, result.getTotalAssets(), "총 자산 금액이 일치해야 함.");
        assertEquals(3000000, result.getTotalIncome(), "총 수입 금액이 일치해야 함.");
        assertEquals(1500000, result.getTotalExpense(), "총 지출 금액이 일치해야 함.");
        assertEquals(50.0, result.getIncomeExpenseRatio(), "수입 대비 지출 비율이 일치해야 함.");
        assertEquals(2, result.getIncomes().size(), "수입 항목의 개수가 일치해야 함.");
        assertEquals(2, result.getExpenses().size(), "지출 항목의 개수가 일치해야 함.");

        // 사용자 ID에 해당하는 자산 정보 조회 리포지토리 호출 검증
        verify(assetsRepository).findByUserId(TEST_USER_ID);
        // 자산 정보 조회 후, 자산 정보 Entity를 DTO로 변환 매퍼 호출 검증
        verify(modelMapper).toAssetsDto(testAssetsEntity);
    }

    /**
     * 자산 정보 조회 실패 테스트 - 사용자 ID가 null인 경우
     * 
     * 시나리오: 사용자 아이디가 null인 경우, 자산 정보 조회 실패
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("자산 정보 조회 실패 - 사용자 ID가 null")
    void getAssetsInfo_NullUserId() {
        // When & Then - 사용자 아이디가 null인 경우, 자산 정보 조회 실패 검증
        assertThrows(IllegalArgumentException.class, () -> {
            assetsService.getAssetsInfo(null);
        }, "사용자 ID가 null인 경우 IllegalArgumentException이 발생해야 함.");

        // 사용자 ID가 null인 경우, 자산 정보 조회 리포지토리 호출 안됨 검증
        verify(assetsRepository, never()).findByUserId(any());
    }

    /**
     * 자산 정보 조회 테스트 - 자산 정보가 없는 경우
     * 
     * 시나리오: 자산 정보가 없는 경우, 예외가 발생해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("자산 정보 조회 실패 - 자산 정보 없음")
    void getAssetsInfo_NotFound() {
        // Given - 자산 정보가 없는 경우 조회 테스트 준비
        when(assetsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        // When & Then - 자산 정보가 없는 경우, 예외가 발생해야 함
        assertThrows(ApplicationException.AssetsNotFoundException.class, () -> {
            assetsService.getAssetsInfo(TEST_USER_ID);
        }, "자산 정보가 없는 경우 AssetsNotFoundException이 발생해야 함.");

        // 자산 정보가 없는 경우, 자산 정보 조회 리포지토리 호출 검증
        verify(assetsRepository).findByUserId(TEST_USER_ID);
        // 자산 정보가 없는 경우, 자산 정보 조회 매퍼 호출 안됨 검증
        verify(modelMapper, never()).toAssetsDto(any());
    }

    /**
     * 자산 정보 등록 테스트 - 자동 계산 기능 검증
     * 
     * 시나리오: 자산 정보 등록을 위해 사용자 자산 정보 입력 DTO 객체를 받아,
     * 총 자산, 총 수입, 총 지출 금액 및 자산 유형 별 비율, 수입 대비 지출 비율 등을 자동 계산 후, 자산 정보를 저장
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("자산 정보 등록 성공 - 자동 계산 검증")
    void createAssets_AutoCalculation() {
        // Given - 자동 계산 검증 및 자산 정보 등록 테스트 준비
        // 사용자 입력 DTO 준비 (totalAssets, totalIncome, totalExpense는 자동 계산되므로 설정하지 않음)
        AssetsDto inputDto = AssetsDto.builder()
                .userId(TEST_USER_ID)
                .incomes(testIncomes)
                .expenses(testExpenses)
                .assetDetails(testAssetDetails)
                .build();

        // 자동 계산된 assetsEntity 모킹
        // thenAnswer(...): 호출된 메서드의 인자나 상황에 따라 동적으로 값을 반환하도록 설정
        // invocation: 실제 호출 시 전달된 인자나 호출 정보를 참조
        // invocation.getArgument(0):
        // assetsRepository.save() 메서드 호출 시, 전달된 첫 번째 인자 (저장할 AssetsEntity 객체)
        when(assetsRepository.save(any(AssetsEntity.class))).thenAnswer(invocation -> {
            AssetsEntity savedEntity = invocation.getArgument(0);
            // ID와 날짜 설정
            savedEntity = AssetsEntity.builder()
                    .id(1L)
                    .userId(savedEntity.getUserId())
                    .totalAssets(savedEntity.getTotalAssets())
                    .totalIncome(savedEntity.getTotalIncome())
                    .totalExpense(savedEntity.getTotalExpense())
                    .incomes(savedEntity.getIncomes())
                    .expenses(savedEntity.getExpenses())
                    .assetDetails(savedEntity.getAssetDetails())
                    .assetsTypeRatios(savedEntity.getAssetsTypeRatios())
                    .incomeExpenseRatio(savedEntity.getIncomeExpenseRatio())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            return savedEntity;
        });

        // 저장된 엔티티를 DTO로 변환하는 모킹
        // thenAnswer(...): 호출된 메서드의 인자나 상황에 따라 동적으로 값을 반환하도록 설정
        // invocation: 실제 호출 시 전달된 인자나 호출 정보를 참조
        // invocation.getArgument(0):
        // modelMapper.toAssetsDto() 메서드 호출 시, 전달된 첫 번째 인자 (저장할 AssetsEntity 객체)
        when(modelMapper.toAssetsDto(any(AssetsEntity.class))).thenAnswer(invocation -> {
            AssetsEntity entity = invocation.getArgument(0);
            return AssetsDto.builder()
                    .id(entity.getId())
                    .userId(entity.getUserId())
                    .totalAssets(entity.getTotalAssets())
                    .totalIncome(entity.getTotalIncome())
                    .totalExpense(entity.getTotalExpense())
                    .incomes(entity.getIncomes())
                    .expenses(entity.getExpenses())
                    .assetDetails(entity.getAssetDetails())
                    .assetsTypeRatios(entity.getAssetsTypeRatios())
                    .incomeExpenseRatio(entity.getIncomeExpenseRatio())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        });

        // When - assetsService의 createAssets 메소드로 자동 계산 및 자산 정보 등록 테스트 실행
        // 자산 정보 등록 후, 저장된 자산 정보 Entity를 DTO로 변환해 반환
        AssetsDto assetsDto = assetsService.createAssets(TEST_USER_ID, inputDto);

        // Then - 자동 계산 및 자산 정보 등록 검증
        assertNotNull(assetsDto, "결과는 null이 아니어야 함.");

        // 금액 계산 확인
        assertNotNull(assetsDto.getTotalAssets(), "총 자산이 계산되어야 함.");
        assertNotNull(assetsDto.getTotalIncome(), "총 수입이 계산되어야 함.");
        assertNotNull(assetsDto.getTotalExpense(), "총 지출이 계산되어야 함.");

        // 자동 계산된 값들 검증 - 테스트 데이터 기준
        assertEquals(1000000, assetsDto.getTotalAssets(), "총 자산은 1,000,000원이어야 함.");
        assertEquals(3000000, assetsDto.getTotalIncome(), "총 수입은 3,000,000원이어야 함.");
        assertEquals(1500000, assetsDto.getTotalExpense(), "총 지출은 1,500,000원이어야 함.");

        // 비율 계산 확인
        assertNotNull(assetsDto.getAssetsTypeRatios(), "자산 유형별 비율이 계산되어야 함.");
        assertNotNull(assetsDto.getIncomeExpenseRatio(), "수입 대비 지출 비율이 계산되어야 함.");

        // 자산 정보 등록 후, 저장된 자산 정보 Entity를 저장하는 리포지토리 호출 검증
        verify(assetsRepository).save(any(AssetsEntity.class));
        // 자산 정보 등록 후, 저장된 자산 정보 Entity를 DTO로 변환하는 매퍼 호출 검증
        verify(modelMapper).toAssetsDto(any(AssetsEntity.class));
    }

    /**
     * 자산 정보 업데이트 테스트 - 성공 케이스
     * 
     * 시나리오: 자산 정보 업데이트를 위해 사용자 자산 정보 입력 DTO 객체를 받아,
     * 총 자산, 총 수입, 총 지출 금액 및 자산 유형 별 비율, 수입 대비 지출 비율 등을 자동 계산 후, 자산 정보를 저장
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("자산 정보 업데이트 성공")
    void updateAssets_Success() {
        // Given - 자산 정보 업데이트 테스트 준비
        // 사용자 아이디로 자산 정보 조회 후, 자산 정보 Entity 리턴
        when(assetsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testAssetsEntity));

        // 자산 정보 Entity 저장 후, 저장된 자산 정보 Entity 리턴
        when(assetsRepository.save(any(AssetsEntity.class))).thenReturn(testAssetsEntity);

        // 저장된 자산 정보 Entity를 DTO로 변환 후, 변환된 자산 정보 DTO 리턴
        when(modelMapper.toAssetsDto(testAssetsEntity)).thenReturn(testAssetsDto);

        // 업데이트할 자산 정보 DTO 생성 (수입/지출 내역 변경)
        List<IncomeVo> updatedIncomes = new ArrayList<>();
        updatedIncomes.add(IncomeVo.of("새 월급", 2500000, "고정"));

        List<ExpenseVo> updatedExpenses = new ArrayList<>();
        updatedExpenses.add(ExpenseVo.of("새 월세", 1200000, "고정"));

        AssetsDto updateDto = AssetsDto.builder()
                .userId(TEST_USER_ID)
                .incomes(updatedIncomes)
                .expenses(updatedExpenses)
                .assetDetails(testAssetDetails)
                .build();

        // When - assetsService의 updateAssets 메소드로 자동 계산 및 자산 정보 업데이트 테스트 실행
        // 자산 정보 업데이트 후, 업데이트된 자산 정보 Entity를 DTO로 변환해 반환
        AssetsDto assetsDto = assetsService.updateAssets(TEST_USER_ID, updateDto);

        // Then - 자동 계산 및 자산 정보 업데이트 검증
        assertNotNull(assetsDto, "자산 정보 업데이트 결과는 null이 아니어야 함.");
        assertEquals(TEST_USER_ID, assetsDto.getUserId(), "사용자 ID가 일치해야 함.");

        // 비율 계산 확인
        assertNotNull(assetsDto.getAssetsTypeRatios(), "자산 유형별 비율이 계산되어야 함.");
        assertNotNull(assetsDto.getIncomeExpenseRatio(), "수입 대비 지출 비율이 계산되어야 함.");

        // 사용자 아이디에 해당하는 자산 정보 조회 리포지토리 호출 검증
        verify(assetsRepository).findByUserId(TEST_USER_ID);
        // 자산 정보 업데이트 후, 업데이트된 자산 정보 Entity를 저장하는 리포지토리 호출 검증
        verify(assetsRepository).save(any(AssetsEntity.class));
        // 자산 정보 업데이트 후, 업데이트된 자산 정보 Entity를 DTO로 변환하는 매퍼 호출 검증
        verify(modelMapper).toAssetsDto(any(AssetsEntity.class));
    }

    /**
     * 자산 정보 업데이트 테스트 - 자산 정보가 없는 경우
     * 
     * 시나리오: 입력받은 사용자 아이디에 해당하는 자산 정보가 없는 경우, 예외가 발생해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("자산 정보 업데이트 실패 - 자산 정보 없음")
    void updateAssets_NotFound() {

    }
}
