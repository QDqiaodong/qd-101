package com.example.cityactivity.service;

import com.example.cityactivity.dto.request.BuddyConvertActivityRequest;
import com.example.cityactivity.dto.response.BuddyRequestResponse;
import com.example.cityactivity.entity.BuddyRequest;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.enums.BuddyRequestStatus;
import com.example.cityactivity.exception.BusinessException;
import com.example.cityactivity.repository.BuddyApplicationRepository;
import com.example.cityactivity.repository.BuddyRequestRepository;
import com.example.cityactivity.repository.UserRepository;
import com.example.cityactivity.service.impl.BuddyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuddyServiceTest {

    @Mock
    private BuddyRequestRepository buddyRequestRepository;

    @Mock
    private BuddyApplicationRepository buddyApplicationRepository;

    @Mock
    private UserService userService;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private BuddyServiceImpl buddyService;

    private User testUser;
    private BuddyRequest openBuddyRequest;
    private BuddyRequest matchedBuddyRequest;
    private BuddyRequest notEnoughBuddyRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .name("测试用户")
                .avatar("avatar.jpg")
                .build();

        openBuddyRequest = BuddyRequest.builder()
                .id(1L)
                .title("找个饭搭子")
                .type("饭搭子")
                .city("北京")
                .description("一起吃火锅")
                .targetCount(1)
                .currentCount(1)
                .status(BuddyRequestStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .creator(testUser)
                .build();

        matchedBuddyRequest = BuddyRequest.builder()
                .id(2L)
                .title("周末羽毛球搭子")
                .type("球搭子")
                .city("北京")
                .description("周末打球")
                .targetCount(2)
                .currentCount(2)
                .status(BuddyRequestStatus.MATCHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .creator(testUser)
                .build();

        notEnoughBuddyRequest = BuddyRequest.builder()
                .id(3L)
                .title("多人爬山搭子")
                .type("户外运动")
                .city("北京")
                .description("一起爬山")
                .targetCount(3)
                .currentCount(1)
                .status(BuddyRequestStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .creator(testUser)
                .build();
    }

    @Test
    @DisplayName("转活动前置条件 - 状态为MATCHED时可以转换")
    void convertToActivity_WhenStatusMatched_ShouldSuccess() {
        BuddyConvertActivityRequest request = BuddyConvertActivityRequest.builder()
                .requestId(2L)
                .creatorId(1L)
                .location("测试地点")
                .time(LocalDateTime.now().plusDays(3))
                .build();

        when(buddyRequestRepository.findById(2L)).thenReturn(Optional.of(matchedBuddyRequest));
        when(activityService.createActivity(any())).thenReturn(null);

        assertDoesNotThrow(() -> buddyService.convertToActivity(request));
        verify(activityService, times(1)).createActivity(any());
    }

    @Test
    @DisplayName("转活动前置条件 - currentCount >= targetCount时可以转换")
    void convertToActivity_WhenCurrentCountReachesTarget_ShouldSuccess() {
        BuddyRequest requestWithEnoughPeople = BuddyRequest.builder()
                .id(4L)
                .title("吃饭搭子")
                .type("饭搭子")
                .city("北京")
                .description("test")
                .targetCount(1)
                .currentCount(1)
                .status(BuddyRequestStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .creator(testUser)
                .build();

        BuddyConvertActivityRequest request = BuddyConvertActivityRequest.builder()
                .requestId(4L)
                .creatorId(1L)
                .location("测试地点")
                .time(LocalDateTime.now().plusDays(3))
                .build();

        when(buddyRequestRepository.findById(4L)).thenReturn(Optional.of(requestWithEnoughPeople));
        when(activityService.createActivity(any())).thenReturn(null);

        assertDoesNotThrow(() -> buddyService.convertToActivity(request));
        verify(activityService, times(1)).createActivity(any());
    }

    @Test
    @DisplayName("转活动前置条件 - 人数不足且状态非MATCHED时不能转换")
    void convertToActivity_WhenNotEnoughAndNotMatched_ShouldThrowException() {
        BuddyConvertActivityRequest request = BuddyConvertActivityRequest.builder()
                .requestId(3L)
                .creatorId(1L)
                .location("测试地点")
                .time(LocalDateTime.now().plusDays(3))
                .build();

        when(buddyRequestRepository.findById(3L)).thenReturn(Optional.of(notEnoughBuddyRequest));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> buddyService.convertToActivity(request));

        assertTrue(exception.getMessage().contains("需达到目标人数或状态为已配对"));
        verify(activityService, never()).createActivity(any());
    }

    @Test
    @DisplayName("转活动前置条件 - 已转换为活动的不能再次转换")
    void convertToActivity_WhenAlreadyConverted_ShouldThrowException() {
        BuddyRequest convertedRequest = BuddyRequest.builder()
                .id(5L)
                .title("已转换的征集")
                .type("饭搭子")
                .city("北京")
                .description("test")
                .targetCount(1)
                .currentCount(1)
                .status(BuddyRequestStatus.CONVERTED)
                .convertedActivityId(100L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .creator(testUser)
                .build();

        BuddyConvertActivityRequest request = BuddyConvertActivityRequest.builder()
                .requestId(5L)
                .creatorId(1L)
                .location("测试地点")
                .time(LocalDateTime.now().plusDays(3))
                .build();

        when(buddyRequestRepository.findById(5L)).thenReturn(Optional.of(convertedRequest));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> buddyService.convertToActivity(request));

        assertTrue(exception.getMessage().contains("已转换为活动"));
        verify(activityService, never()).createActivity(any());
    }

    @Test
    @DisplayName("转活动前置条件 - 已关闭的不能转换")
    void convertToActivity_WhenClosed_ShouldThrowException() {
        BuddyRequest closedRequest = BuddyRequest.builder()
                .id(6L)
                .title("已关闭的征集")
                .type("饭搭子")
                .city("北京")
                .description("test")
                .targetCount(1)
                .currentCount(1)
                .status(BuddyRequestStatus.CLOSED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .creator(testUser)
                .build();

        BuddyConvertActivityRequest request = BuddyConvertActivityRequest.builder()
                .requestId(6L)
                .creatorId(1L)
                .location("测试地点")
                .time(LocalDateTime.now().plusDays(3))
                .build();

        when(buddyRequestRepository.findById(6L)).thenReturn(Optional.of(closedRequest));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> buddyService.convertToActivity(request));

        assertTrue(exception.getMessage().contains("已关闭"));
        verify(activityService, never()).createActivity(any());
    }

    @Test
    @DisplayName("转活动前置条件 - 非发起人不能转换")
    void convertToActivity_WhenNotCreator_ShouldThrowException() {
        User anotherUser = User.builder()
                .id(2L)
                .username("another")
                .password("password")
                .name("另一个用户")
                .build();

        BuddyRequest requestByAnother = BuddyRequest.builder()
                .id(7L)
                .title("别人的征集")
                .type("饭搭子")
                .city("北京")
                .description("test")
                .targetCount(1)
                .currentCount(2)
                .status(BuddyRequestStatus.MATCHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .creator(anotherUser)
                .build();

        BuddyConvertActivityRequest request = BuddyConvertActivityRequest.builder()
                .requestId(7L)
                .creatorId(1L)
                .location("测试地点")
                .time(LocalDateTime.now().plusDays(3))
                .build();

        when(buddyRequestRepository.findById(7L)).thenReturn(Optional.of(requestByAnother));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> buddyService.convertToActivity(request));

        assertTrue(exception.getMessage().contains("无权限"));
        verify(activityService, never()).createActivity(any());
    }

    @Test
    @DisplayName("搭子征集创建 - 应该设置初始状态为OPEN")
    void createRequest_ShouldSetStatusOpen() {
        com.example.cityactivity.dto.request.BuddyRequestCreateRequest createRequest =
                com.example.cityactivity.dto.request.BuddyRequestCreateRequest.builder()
                        .title("测试征集")
                        .type("饭搭子")
                        .city("北京")
                        .description("测试描述")
                        .targetCount(2)
                        .creatorId(1L)
                        .build();

        when(userService.findById(1L)).thenReturn(testUser);
        when(buddyRequestRepository.save(any(BuddyRequest.class))).thenAnswer(invocation -> {
            BuddyRequest saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });
        when(buddyApplicationRepository.countByBuddyRequestIdAndStatus(anyLong(), any())).thenReturn(0L);

        BuddyRequestResponse response = buddyService.createRequest(createRequest);

        assertNotNull(response);
        assertEquals(BuddyRequestStatus.OPEN, response.getStatus());
        assertEquals(1, response.getCurrentCount());
        assertEquals(2, response.getTargetCount());
        assertEquals("测试征集", response.getTitle());
    }

    @Test
    @DisplayName("搭子征集 - 关闭征集应该改变状态")
    void closeRequest_ShouldChangeStatusToClosed() {
        when(buddyRequestRepository.findById(1L)).thenReturn(Optional.of(openBuddyRequest));
        when(buddyRequestRepository.save(any(BuddyRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(buddyApplicationRepository.countByBuddyRequestIdAndStatus(anyLong(), any())).thenReturn(0L);

        BuddyRequestResponse response = buddyService.closeRequest(1L, 1L);

        assertNotNull(response);
        assertEquals(BuddyRequestStatus.CLOSED, response.getStatus());
    }

    @Test
    @DisplayName("搭子征集 - 已关闭的不能再次关闭")
    void closeRequest_WhenAlreadyClosed_ShouldThrowException() {
        BuddyRequest closedRequest = BuddyRequest.builder()
                .id(8L)
                .title("已关闭")
                .type("饭搭子")
                .city("北京")
                .targetCount(1)
                .currentCount(1)
                .status(BuddyRequestStatus.CLOSED)
                .creator(testUser)
                .build();

        when(buddyRequestRepository.findById(8L)).thenReturn(Optional.of(closedRequest));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> buddyService.closeRequest(8L, 1L));

        assertTrue(exception.getMessage().contains("已关闭或已转换"));
    }
}
