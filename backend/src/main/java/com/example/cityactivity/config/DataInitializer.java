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
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    
    @Override
    public void run(String... args) {
        List<User> users = new ArrayList<>();
        
        if (userRepository.count() == 0) {
            User user1 = User.builder()
                    .username("user1")
                    .password("password1")
                    .name("城市探索者")
                    .avatar("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop")
                    .build();
            users.add(userRepository.save(user1));
            
            User user2 = User.builder()
                    .username("food_explorer")
                    .password("password2")
                    .name("美食探险家小王")
                    .avatar("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop")
                    .build();
            users.add(userRepository.save(user2));
            
            User user3 = User.builder()
                    .username("outdoor_guide")
                    .password("password3")
                    .name("户外领队-大山")
                    .avatar("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&h=200&fit=crop")
                    .build();
            users.add(userRepository.save(user3));
            
            User user4 = User.builder()
                    .username("sports_lover")
                    .password("password4")
                    .name("运动达人阿杰")
                    .avatar("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop")
                    .build();
            users.add(userRepository.save(user4));
            
            User user5 = User.builder()
                    .username("boardgame_queen")
                    .password("password5")
                    .name("桌游女王Luna")
                    .avatar("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop")
                    .build();
            users.add(userRepository.save(user5));
            
            User user6 = User.builder()
                    .username("picnic_lover")
                    .password("password6")
                    .name("野餐达人小楠")
                    .avatar("https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop")
                    .build();
            users.add(userRepository.save(user6));
            
            User user7 = User.builder()
                    .username("badminton_fan")
                    .password("password7")
                    .name("羽球小王子")
                    .avatar("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200&h=200&fit=crop")
                    .build();
            users.add(userRepository.save(user7));
            
            User user8 = User.builder()
                    .username("bbq_master")
                    .password("password8")
                    .name("烧烤达人老陈")
                    .avatar("https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&h=200&fit=crop")
                    .build();
            users.add(userRepository.save(user8));
            
            log.info("Initial users created: {}", users.size());
        } else {
            users = userRepository.findAll();
        }
        
        if (activityRepository.count() == 0 && users.size() >= 2) {
            createSampleActivities(users);
            log.info("Initial activities created");
        }
    }
    
    private void createSampleActivities(List<User> users) {
        User user1 = users.get(0);
        User user2 = users.size() > 1 ? users.get(1) : user1;
        User user3 = users.size() > 2 ? users.get(2) : user1;
        User user5 = users.size() > 4 ? users.get(4) : user1;
        
        Activity activity1 = Activity.builder()
                .title("周末CBD美食探店小分队")
                .type("探店")
                .city("北京")
                .location("朝阳区CBD商圈")
                .time(LocalDateTime.now().plusDays(3).withHour(18).withMinute(0))
                .maxParticipants(8)
                .currentParticipants(5)
                .description("这周末一起去探索CBD新开的网红餐厅吧！主打融合菜，听说环境超棒，适合拍照打卡。")
                .requirements("热爱美食，性格开朗，不挑食")
                .image("https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop")
                .views(328)
                .createdAt(LocalDateTime.now().minusDays(2))
                .creator(user2)
                .build();
        activityRepository.save(activity1);
        
        Activity activity2 = Activity.builder()
                .title("香山徒步登山活动")
                .type("徒步")
                .city("北京")
                .location("香山公园东门集合")
                .time(LocalDateTime.now().plusDays(5).withHour(9).withMinute(0))
                .maxParticipants(15)
                .currentParticipants(12)
                .description("周末登山活动！路线从东门到鬼见愁，全程约3小时，难度适中，适合新手。")
                .requirements("穿着运动鞋，自带饮用水")
                .image("https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=400&h=300&fit=crop")
                .views(512)
                .createdAt(LocalDateTime.now().minusDays(3))
                .creator(user3)
                .build();
        activityRepository.save(activity2);
        
        Activity activity3 = Activity.builder()
                .title("周末篮球友谊赛")
                .type("打球")
                .city("北京")
                .location("洛克公园篮球场")
                .time(LocalDateTime.now().plusDays(6).withHour(14).withMinute(0))
                .maxParticipants(12)
                .currentParticipants(8)
                .description("周末放松一下，来场3v3友谊赛！不分水平，重在参与，锻炼身体结交朋友。")
                .requirements("带好运动装备，注意安全")
                .image("https://images.unsplash.com/photo-1551632811-561732d1e306?w=400&h=300&fit=crop")
                .views(245)
                .createdAt(LocalDateTime.now().minusDays(1))
                .creator(user1)
                .build();
        activityRepository.save(activity3);
        
        Activity activity4 = Activity.builder()
                .title("桌游之夜：狼人杀+剧本杀")
                .type("桌游")
                .city("北京")
                .location("三里屯某某桌游吧")
                .time(LocalDateTime.now().plusDays(4).withHour(19).withMinute(0))
                .maxParticipants(10)
                .currentParticipants(7)
                .description("周末来场烧脑的桌游派对！狼人杀、剧本杀、uno都有，场地已预定好，就等你了！")
                .requirements("喜欢逻辑推理，放得开玩")
                .image("https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?w=400&h=300&fit=crop")
                .views(403)
                .createdAt(LocalDateTime.now().minusDays(4))
                .creator(user5)
                .build();
        activityRepository.save(activity4);
        
        Activity activity5 = Activity.builder()
                .title("周末公园野餐聚会")
                .type("聚餐")
                .city("北京")
                .location("朝阳公园")
                .time(LocalDateTime.now().plusDays(7).withHour(12).withMinute(0))
                .maxParticipants(12)
                .currentParticipants(9)
                .description("周日阳光正好，来公园野餐吧！每人带一道菜，分享美食和故事。")
                .requirements("自带一道菜品，不挑食")
                .image("https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400&h=300&fit=crop")
                .views(287)
                .createdAt(LocalDateTime.now().minusDays(1))
                .creator(user2)
                .build();
        activityRepository.save(activity5);
        
        Activity activity6 = Activity.builder()
                .title("周三下班后羽毛球局")
                .type("打球")
                .city("北京")
                .location("李宁羽毛球馆")
                .time(LocalDateTime.now().plusDays(3).withHour(18).withMinute(30))
                .maxParticipants(8)
                .currentParticipants(6)
                .description("周三下班放松一下，来打羽毛球！场地已订好，球拍可借。")
                .requirements("穿着运动服，注意安全")
                .image("https://images.unsplash.com/photo-1514525253440-b393452e8d26?w=400&h=300&fit=crop")
                .views(176)
                .createdAt(LocalDateTime.now().minusDays(2))
                .creator(user3)
                .build();
        activityRepository.save(activity6);
        
        Activity activity7 = Activity.builder()
                .title("深夜狼人杀专场")
                .type("桌游")
                .city("北京")
                .location("五道口桌游吧")
                .time(LocalDateTime.now().plusDays(2).withHour(20).withMinute(30))
                .maxParticipants(12)
                .currentParticipants(9)
                .description("今晚来场深夜狼人杀！高手云集，就等你了~")
                .requirements("熟悉狼人杀规则，能玩到深夜")
                .image("https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?w=400&h=300&fit=crop")
                .views(234)
                .createdAt(LocalDateTime.now().minusHours(6))
                .creator(user5)
                .build();
        activityRepository.save(activity7);
        
        Activity activity8 = Activity.builder()
                .title("周日咖啡品鉴会")
                .type("探店")
                .city("北京")
                .location("三里屯某精品咖啡店")
                .time(LocalDateTime.now().plusDays(8).withHour(15).withMinute(0))
                .maxParticipants(8)
                .currentParticipants(5)
                .description("一起品尝来自世界各地的精品咖啡，了解咖啡文化~")
                .requirements("喜欢咖啡，不迟到")
                .image("https://images.unsplash.com/photo-1514525253440-b393452e8d26?w=400&h=300&fit=crop")
                .views(189)
                .createdAt(LocalDateTime.now().minusDays(1))
                .creator(user2)
                .build();
        activityRepository.save(activity8);
    }
}
