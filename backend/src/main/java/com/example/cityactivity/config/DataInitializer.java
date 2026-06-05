package com.example.cityactivity.config;

import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User user1 = User.builder()
                    .username("user1")
                    .password("password1")
                    .name("城市探索者")
                    .avatar("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop")
                    .build();
            userRepository.save(user1);
            
            User user2 = User.builder()
                    .username("user2")
                    .password("password2")
                    .name("活动达人")
                    .avatar("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop")
                    .build();
            userRepository.save(user2);
            
            log.info("Initial users created");
        }
        
        if (activityRepository.count() == 0) {
            User creator = userRepository.findById(1L).orElse(null);
            if (creator != null) {
                Activity activity1 = Activity.builder()
                        .title("周末CBD美食探店小分队")
                        .type("探店")
                        .city("北京")
                        .location("朝阳区CBD商圈")
                        .time(LocalDateTime.of(2025, 1, 20, 18, 0))
                        .maxParticipants(8)
                        .currentParticipants(5)
                        .description("这周末一起去探索CBD新开的网红餐厅吧！主打融合菜，听说环境超棒，适合拍照打卡。")
                        .requirements("热爱美食，性格开朗，不挑食")
                        .image("https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop")
                        .views(328)
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .creator(creator)
                        .build();
                activityRepository.save(activity1);
                
                Activity activity2 = Activity.builder()
                        .title("香山徒步登山活动")
                        .type("徒步")
                        .city("北京")
                        .location("香山公园东门集合")
                        .time(LocalDateTime.of(2025, 1, 21, 9, 0))
                        .maxParticipants(15)
                        .currentParticipants(12)
                        .description("新年第一次登山活动！路线从东门到鬼见愁，全程约3小时，难度适中，适合新手。")
                        .requirements("穿着运动鞋，自带饮用水")
                        .image("https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=400&h=300&fit=crop")
                        .views(512)
                        .createdAt(LocalDateTime.now().minusDays(3))
                        .creator(creator)
                        .build();
                activityRepository.save(activity2);
                
                Activity activity3 = Activity.builder()
                        .title("周末篮球友谊赛")
                        .type("打球")
                        .city("上海")
                        .location("洛克公园篮球场")
                        .time(LocalDateTime.of(2025, 1, 22, 14, 0))
                        .maxParticipants(12)
                        .currentParticipants(8)
                        .description("下班后放松一下，来场3v3友谊赛！不分水平，重在参与，锻炼身体结交朋友。")
                        .requirements("带好运动装备，注意安全")
                        .image("https://images.unsplash.com/photo-1551632811-561732d1e306?w=400&h=300&fit=crop")
                        .views(245)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .creator(creator)
                        .build();
                activityRepository.save(activity3);
                
                Activity activity4 = Activity.builder()
                        .title("桌游之夜：狼人杀+剧本杀")
                        .type("桌游")
                        .city("广州")
                        .location("天河区某某桌游吧")
                        .time(LocalDateTime.of(2025, 1, 20, 19, 0))
                        .maxParticipants(10)
                        .currentParticipants(7)
                        .description("周末来场烧脑的桌游派对！狼人杀、剧本杀、uno都有，场地已预定好，就等你了！")
                        .requirements("喜欢逻辑推理，放得开玩")
                        .image("https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?w=400&h=300&fit=crop")
                        .views(403)
                        .createdAt(LocalDateTime.now().minusDays(4))
                        .creator(creator)
                        .build();
                activityRepository.save(activity4);
                
                Activity activity5 = Activity.builder()
                        .title("年夜饭预热聚餐")
                        .type("聚餐")
                        .city("深圳")
                        .location("福田区某湘菜馆")
                        .time(LocalDateTime.of(2025, 1, 23, 18, 30))
                        .maxParticipants(12)
                        .currentParticipants(9)
                        .description("年前最后一次聚餐，选了家超正宗的湘菜馆，无辣不欢的朋友赶紧报名！")
                        .requirements("能吃辣，AA制")
                        .image("https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400&h=300&fit=crop")
                        .views(287)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .creator(creator)
                        .build();
                activityRepository.save(activity5);
                
                log.info("Initial activities created");
            }
        }
    }
}
