package com.example.cityactivity.service.impl;

import com.example.cityactivity.dto.response.ActivityResponse;
import com.example.cityactivity.dto.response.CreatorActivityTypeDTO;
import com.example.cityactivity.dto.response.CreatorCommonAreaDTO;
import com.example.cityactivity.dto.response.CreatorResponse;
import com.example.cityactivity.dto.response.CreatorReviewTagDTO;
import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.exception.ResourceNotFoundException;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.repository.UserRepository;
import com.example.cityactivity.service.ActivityService;
import com.example.cityactivity.service.CreatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreatorServiceImpl implements CreatorService {
    
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ActivityService activityService;
    
    private static final Map<Long, CreatorProfileConfig> CREATOR_PROFILES = new HashMap<>();
    
    static {
        CREATOR_PROFILES.put(2L, new CreatorProfileConfig(
            "美食探险家小王",
            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop",
            "吃遍北京大街小巷的美食博主，带你发现隐藏的美味。",
            Arrays.asList("美食达人", "探店专业户", "气氛担当"),
            Arrays.asList(
                new ReviewTagConfig("组织靠谱", 42),
                new ReviewTagConfig("选店有品味", 38),
                new ReviewTagConfig("气氛活跃", 35),
                new ReviewTagConfig("人超nice", 30)
            )
        ));
        
        CREATOR_PROFILES.put(3L, new CreatorProfileConfig(
            "户外领队-大山",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop",
            "户外爱好者，周末不是在爬山就是在去爬山的路上。",
            Arrays.asList("户外达人", "徒步领队", "阳光男孩"),
            Arrays.asList(
                new ReviewTagConfig("路线专业", 32),
                new ReviewTagConfig("安全靠谱", 28),
                new ReviewTagConfig("体力好", 25),
                new ReviewTagConfig("热心肠", 20)
            )
        ));
        
        CREATOR_PROFILES.put(5L, new CreatorProfileConfig(
            "桌游女王Luna",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop",
            "桌游吧老板娘，狼人杀资深玩家，剧本杀情感本天花板。",
            Arrays.asList("桌游大神", "逻辑女王", "氛围组组长"),
            Arrays.asList(
                new ReviewTagConfig("逻辑清晰", 48),
                new ReviewTagConfig("气氛超棒", 45),
                new ReviewTagConfig("颜值担当", 40),
                new ReviewTagConfig("DM专业", 35)
            )
        ));
        
        CREATOR_PROFILES.put(8L, new CreatorProfileConfig(
            "烧烤达人老陈",
            "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&h=200&fit=crop",
            "东北人，撸串专业户，号称北京烧烤活地图。",
            Arrays.asList("烧烤大王", "美食雷达", "东北老铁"),
            Arrays.asList(
                new ReviewTagConfig("选择困难症福音", 24),
                new ReviewTagConfig("太好吃了", 22),
                new ReviewTagConfig("豪爽大气", 20),
                new ReviewTagConfig("性价比高", 18)
            )
        ));
        
        CREATOR_PROFILES.put(13L, new CreatorProfileConfig(
            "夜猫子小夜",
            "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop",
            "深夜活动组织者，越夜越精神，带你玩转北京夜生活。",
            Arrays.asList("深夜玩家", "夜生活达人", "通宵王者"),
            Arrays.asList(
                new ReviewTagConfig("玩得尽兴", 38),
                new ReviewTagConfig("熬夜冠军", 32),
                new ReviewTagConfig("安排周到", 28),
                new ReviewTagConfig("有趣的灵魂", 25)
            )
        ));
        
        CREATOR_PROFILES.put(18L, new CreatorProfileConfig(
            "跑团团长-阿杰",
            "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&h=200&fit=crop",
            "跑步爱好者，全马选手，带你从入门到半马。",
            Arrays.asList("运动达人", "跑步教练", "自律狂魔"),
            Arrays.asList(
                new ReviewTagConfig("专业指导", 28),
                new ReviewTagConfig("耐心细致", 25),
                new ReviewTagConfig("减肥成功", 20),
                new ReviewTagConfig("自律达人", 18)
            )
        ));
        
        CREATOR_PROFILES.put(20L, new CreatorProfileConfig(
            "调酒师James",
            "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop",
            "资深调酒师，开过酒吧，爱喝也会调，喜欢微醺的感觉。",
            Arrays.asList("调酒达人", "品酒师", "优雅男士"),
            Arrays.asList(
                new ReviewTagConfig("酒品超棒", 22),
                new ReviewTagConfig("知识渊博", 18),
                new ReviewTagConfig("品味独到", 15),
                new ReviewTagConfig("绅士风度", 12)
            )
        ));
        
        CREATOR_PROFILES.put(21L, new CreatorProfileConfig(
            "剧本杀编导-小雨",
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop",
            "戏剧学院毕业，剧本杀狂热爱好者，情感本专业户。",
            Arrays.asList("剧本杀达人", "情感本天花板", "眼泪收割机"),
            Arrays.asList(
                new ReviewTagConfig("沉浸感强", 27),
                new ReviewTagConfig("哭到崩溃", 24),
                new ReviewTagConfig("情感细腻", 22),
                new ReviewTagConfig("选本有眼光", 20)
            )
        ));
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "creators", key = "'all:' + #type + ':' + #sortBy")
    public List<CreatorResponse> getAllCreators(String type, String sortBy) {
        List<CreatorResponse> creators = CREATOR_PROFILES.keySet().stream()
                .map(userId -> buildCreatorResponse(userId))
                .filter(creator -> {
                    if (type == null || type.isEmpty()) return true;
                    return creator.getCommonTypes().stream()
                            .anyMatch(t -> t.getType().equals(type));
                })
                .sorted((a, b) -> switch (sortBy) {
                    case "successRate" -> b.getSuccessRate() - a.getSuccessRate();
                    case "fillSpeed" -> Double.compare(a.getAvgFillSpeedHours(), b.getAvgFillSpeedHours());
                    default -> b.getTotalActivities() - a.getTotalActivities();
                })
                .collect(Collectors.toList());
        
        log.debug("Found {} creators", creators.size());
        return creators;
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "creator_detail", key = "#id")
    public CreatorResponse getCreatorById(Long id) {
        if (!CREATOR_PROFILES.containsKey(id)) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Creator", id));
            return buildCreatorResponseFromUser(user);
        }
        return buildCreatorResponse(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "creator_activities", key = "#creatorId")
    public List<ActivityResponse> getCreatorActivities(Long creatorId) {
        return activityService.getActivitiesByCreator(creatorId);
    }
    
    private CreatorResponse buildCreatorResponse(Long userId) {
        CreatorProfileConfig config = CREATOR_PROFILES.get(userId);
        List<Activity> activities = activityRepository.findByCreatorId(userId);
        
        int totalActivities = !activities.isEmpty() ? activities.size() : getDefaultTotalActivities(userId);
        int successRate = calculateSuccessRate(activities, userId);
        double avgFillSpeed = calculateAvgFillSpeed(activities, userId);
        List<CreatorActivityTypeDTO> commonTypes = calculateCommonTypes(activities, userId);
        List<CreatorCommonAreaDTO> commonAreas = calculateCommonAreas(activities, userId);
        
        List<CreatorReviewTagDTO> reviewTags = config.reviewTags().stream()
                .map(tag -> CreatorReviewTagDTO.builder()
                        .tag(tag.tag())
                        .count(tag.count())
                        .build())
                .collect(Collectors.toList());
        
        return CreatorResponse.builder()
                .id(userId)
                .name(config.name())
                .avatar(config.avatar())
                .bio(config.bio())
                .totalActivities(totalActivities)
                .successRate(successRate)
                .avgFillSpeedHours(avgFillSpeed)
                .commonTypes(commonTypes)
                .commonAreas(commonAreas)
                .reviewTags(reviewTags)
                .styleTags(config.styleTags())
                .build();
    }
    
    private CreatorResponse buildCreatorResponseFromUser(User user) {
        List<Activity> activities = activityRepository.findByCreatorId(user.getId());
        
        return CreatorResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .avatar(user.getAvatar())
                .bio("热爱生活的活动发起人")
                .totalActivities(activities.size())
                .successRate(85)
                .avgFillSpeedHours(24.0)
                .commonTypes(calculateCommonTypes(activities, user.getId()))
                .commonAreas(calculateCommonAreas(activities, user.getId()))
                .reviewTags(Arrays.asList(
                    CreatorReviewTagDTO.builder().tag("组织靠谱").count(10).build(),
                    CreatorReviewTagDTO.builder().tag("人很好").count(8).build(),
                    CreatorReviewTagDTO.builder().tag("气氛好").count(6).build()
                ))
                .styleTags(Arrays.asList("活动达人", "热心肠"))
                .build();
    }
    
    private int getDefaultTotalActivities(Long userId) {
        return switch (userId.intValue()) {
            case 2 -> 48;
            case 3 -> 36;
            case 5 -> 56;
            case 8 -> 28;
            case 13 -> 42;
            case 18 -> 32;
            case 20 -> 24;
            case 21 -> 30;
            default -> 10;
        };
    }
    
    private int calculateSuccessRate(List<Activity> activities, Long userId) {
        if (!activities.isEmpty()) {
            long successful = activities.stream()
                    .filter(a -> a.getCurrentParticipants() >= a.getMaxParticipants() * 0.5)
                    .count();
            return (int) ((successful * 100) / activities.size());
        }
        return switch (userId.intValue()) {
            case 2 -> 95;
            case 3 -> 92;
            case 5 -> 98;
            case 8 -> 93;
            case 13 -> 90;
            case 18 -> 88;
            case 20 -> 85;
            case 21 -> 96;
            default -> 85;
        };
    }
    
    private double calculateAvgFillSpeed(List<Activity> activities, Long userId) {
        if (!activities.isEmpty()) {
            double avg = activities.stream()
                    .mapToDouble(a -> Math.max(1.0, 48.0 - (a.getCurrentParticipants() * 4.0)))
                    .average()
                    .orElse(24.0);
            return Math.round(avg * 10) / 10.0;
        }
        return switch (userId.intValue()) {
            case 2 -> 12.0;
            case 3 -> 24.0;
            case 5 -> 8.0;
            case 8 -> 18.0;
            case 13 -> 6.0;
            case 18 -> 36.0;
            case 20 -> 48.0;
            case 21 -> 10.0;
            default -> 24.0;
        };
    }
    
    private List<CreatorActivityTypeDTO> calculateCommonTypes(List<Activity> activities, Long userId) {
        if (!activities.isEmpty()) {
            Map<String, Long> typeCounts = activities.stream()
                    .collect(Collectors.groupingBy(Activity::getType, Collectors.counting()));
            return typeCounts.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .limit(3)
                    .map(entry -> CreatorActivityTypeDTO.builder()
                            .type(entry.getKey())
                            .count(entry.getValue().intValue())
                            .build())
                    .collect(Collectors.toList());
        }
        
        return switch (userId.intValue()) {
            case 2 -> Arrays.asList(
                CreatorActivityTypeDTO.builder().type("探店").count(28).build(),
                CreatorActivityTypeDTO.builder().type("聚餐").count(15).build(),
                CreatorActivityTypeDTO.builder().type("其他").count(5).build()
            );
            case 3 -> Arrays.asList(
                CreatorActivityTypeDTO.builder().type("徒步").count(30).build(),
                CreatorActivityTypeDTO.builder().type("打球").count(4).build(),
                CreatorActivityTypeDTO.builder().type("聚餐").count(2).build()
            );
            case 5 -> Arrays.asList(
                CreatorActivityTypeDTO.builder().type("桌游").count(52).build(),
                CreatorActivityTypeDTO.builder().type("聚餐").count(4).build()
            );
            case 8 -> Arrays.asList(
                CreatorActivityTypeDTO.builder().type("聚餐").count(25).build(),
                CreatorActivityTypeDTO.builder().type("探店").count(3).build()
            );
            case 13 -> Arrays.asList(
                CreatorActivityTypeDTO.builder().type("桌游").count(18).build(),
                CreatorActivityTypeDTO.builder().type("聚餐").count(15).build(),
                CreatorActivityTypeDTO.builder().type("探店").count(9).build()
            );
            case 18 -> Arrays.asList(
                CreatorActivityTypeDTO.builder().type("徒步").count(28).build(),
                CreatorActivityTypeDTO.builder().type("打球").count(3).build(),
                CreatorActivityTypeDTO.builder().type("聚餐").count(1).build()
            );
            case 20 -> Arrays.asList(
                CreatorActivityTypeDTO.builder().type("探店").count(18).build(),
                CreatorActivityTypeDTO.builder().type("聚餐").count(6).build()
            );
            case 21 -> Arrays.asList(
                CreatorActivityTypeDTO.builder().type("桌游").count(28).build(),
                CreatorActivityTypeDTO.builder().type("聚餐").count(2).build()
            );
            default -> Arrays.asList(
                CreatorActivityTypeDTO.builder().type("聚餐").count(5).build()
            );
        };
    }
    
    private List<CreatorCommonAreaDTO> calculateCommonAreas(List<Activity> activities, Long userId) {
        if (!activities.isEmpty()) {
            Map<String, Long> areaCounts = activities.stream()
                    .collect(Collectors.groupingBy(Activity::getLocation, Collectors.counting()));
            return areaCounts.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .limit(3)
                    .map(entry -> CreatorCommonAreaDTO.builder()
                            .name(entry.getKey())
                            .count(entry.getValue().intValue())
                            .build())
                    .collect(Collectors.toList());
        }
        
        return switch (userId.intValue()) {
            case 2 -> Arrays.asList(
                CreatorCommonAreaDTO.builder().name("CBD商圈").count(15).build(),
                CreatorCommonAreaDTO.builder().name("三里屯").count(12).build(),
                CreatorCommonAreaDTO.builder().name("五道口").count(8).build()
            );
            case 3 -> Arrays.asList(
                CreatorCommonAreaDTO.builder().name("香山").count(12).build(),
                CreatorCommonAreaDTO.builder().name("奥森公园").count(10).build(),
                CreatorCommonAreaDTO.builder().name("朝阳公园").count(6).build()
            );
            case 5 -> Arrays.asList(
                CreatorCommonAreaDTO.builder().name("三里屯").count(20).build(),
                CreatorCommonAreaDTO.builder().name("五道口").count(18).build(),
                CreatorCommonAreaDTO.builder().name("朝阳区").count(10).build()
            );
            case 8 -> Arrays.asList(
                CreatorCommonAreaDTO.builder().name("簋街").count(12).build(),
                CreatorCommonAreaDTO.builder().name("双井").count(8).build(),
                CreatorCommonAreaDTO.builder().name("望京").count(5).build()
            );
            case 13 -> Arrays.asList(
                CreatorCommonAreaDTO.builder().name("三里屯").count(16).build(),
                CreatorCommonAreaDTO.builder().name("簋街").count(12).build(),
                CreatorCommonAreaDTO.builder().name("五道口").count(8).build()
            );
            case 18 -> Arrays.asList(
                CreatorCommonAreaDTO.builder().name("朝阳公园").count(12).build(),
                CreatorCommonAreaDTO.builder().name("奥森公园").count(15).build(),
                CreatorCommonAreaDTO.builder().name("后海").count(5).build()
            );
            case 20 -> Arrays.asList(
                CreatorCommonAreaDTO.builder().name("三里屯").count(10).build(),
                CreatorCommonAreaDTO.builder().name("CBD商圈").count(8).build(),
                CreatorCommonAreaDTO.builder().name("望京").count(6).build()
            );
            case 21 -> Arrays.asList(
                CreatorCommonAreaDTO.builder().name("朝阳区").count(15).build(),
                CreatorCommonAreaDTO.builder().name("三里屯").count(8).build(),
                CreatorCommonAreaDTO.builder().name("五道口").count(5).build()
            );
            default -> Arrays.asList(
                CreatorCommonAreaDTO.builder().name("北京市区").count(5).build()
            );
        };
    }
    
    private record CreatorProfileConfig(
        String name,
        String avatar,
        String bio,
        List<String> styleTags,
        List<ReviewTagConfig> reviewTags
    ) {}
    
    private record ReviewTagConfig(String tag, int count) {}
}
