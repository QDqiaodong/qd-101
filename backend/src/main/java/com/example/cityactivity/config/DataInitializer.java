package com.example.cityactivity.config;

import com.example.cityactivity.entity.Activity;
import com.example.cityactivity.entity.BuddyRequest;
import com.example.cityactivity.entity.Comment;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.enums.BuddyRequestStatus;
import com.example.cityactivity.repository.ActivityRepository;
import com.example.cityactivity.repository.BuddyRequestRepository;
import com.example.cityactivity.repository.CommentRepository;
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
    private final BuddyRequestRepository buddyRequestRepository;
    private final CommentRepository commentRepository;
    
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

        if (buddyRequestRepository.count() == 0 && users.size() >= 2) {
            createSampleBuddyRequests(users);
            log.info("Initial buddy requests created");
        }

        if (commentRepository.count() == 0 && activityRepository.count() > 0 && users.size() >= 3) {
            createSampleComments(users);
            log.info("Initial comments created");
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

    private void createSampleBuddyRequests(List<User> users) {
        User user1 = users.get(0);
        User user2 = users.size() > 1 ? users.get(1) : user1;
        User user3 = users.size() > 2 ? users.get(2) : user1;
        User user4 = users.size() > 3 ? users.get(3) : user1;
        User user6 = users.size() > 5 ? users.get(5) : user1;
        User user7 = users.size() > 6 ? users.get(6) : user1;

        BuddyRequest buddy1 = BuddyRequest.builder()
                .title("找个饭搭子，一起吃火锅去！")
                .type("饭搭子")
                .city("北京")
                .description("最近想吃火锅，一个人吃太无聊了，找个同样爱吃火锅的小伙伴一起~ 男女不限，AA制。")
                .targetCount(1)
                .currentCount(1)
                .status(BuddyRequestStatus.OPEN)
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now().minusHours(2))
                .creator(user2)
                .build();
        buddyRequestRepository.save(buddy1);

        BuddyRequest buddy2 = BuddyRequest.builder()
                .title("周末羽毛球搭子，有人一起吗？")
                .type("球搭子")
                .city("北京")
                .description("周末想打羽毛球，水平一般，纯属娱乐健身。找个水平差不多的球友一起打，场地可以商量。")
                .targetCount(2)
                .currentCount(1)
                .status(BuddyRequestStatus.OPEN)
                .createdAt(LocalDateTime.now().minusHours(5))
                .updatedAt(LocalDateTime.now().minusHours(5))
                .creator(user7)
                .build();
        buddyRequestRepository.save(buddy2);

        BuddyRequest buddy3 = BuddyRequest.builder()
                .title("一起探店！寻找城市里的宝藏咖啡馆")
                .type("探店搭子")
                .city("北京")
                .description("喜欢探店拍照，特别是有特色的咖啡馆和小店。周末可以一起去探索，互相拍照~")
                .targetCount(1)
                .currentCount(1)
                .status(BuddyRequestStatus.MATCHING)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusHours(8))
                .creator(user6)
                .build();
        buddyRequestRepository.save(buddy3);

        BuddyRequest buddy4 = BuddyRequest.builder()
                .title("健身搭子，互相监督一起瘦！")
                .type("健身搭子")
                .city("北京")
                .description("想找个健身搭子，互相监督打卡。我一般晚上下班后去健身房，有一起的吗？")
                .targetCount(1)
                .currentCount(1)
                .status(BuddyRequestStatus.OPEN)
                .createdAt(LocalDateTime.now().minusHours(12))
                .updatedAt(LocalDateTime.now().minusHours(12))
                .creator(user4)
                .build();
        buddyRequestRepository.save(buddy4);

        BuddyRequest buddy5 = BuddyRequest.builder()
                .title("周末爬山搭子，香山走起~")
                .type("户外运动")
                .city("北京")
                .description("这周末想去香山徒步，有一起的小伙伴吗？路线轻松，主要是锻炼身体呼吸新鲜空气。")
                .targetCount(3)
                .currentCount(1)
                .status(BuddyRequestStatus.OPEN)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .creator(user3)
                .build();
        buddyRequestRepository.save(buddy5);

        BuddyRequest buddy6 = BuddyRequest.builder()
                .title("找个一起吃晚饭的饭搭子")
                .type("饭搭子")
                .city("上海")
                .description("刚来上海工作，一个人吃饭太寂寞了，找个附近的饭搭子，工作日晚餐可以一起吃~")
                .targetCount(1)
                .currentCount(1)
                .status(BuddyRequestStatus.OPEN)
                .createdAt(LocalDateTime.now().minusHours(8))
                .updatedAt(LocalDateTime.now().minusHours(8))
                .creator(user1)
                .build();
        buddyRequestRepository.save(buddy6);
    }

    private void createSampleComments(List<User> users) {
        List<Activity> activities = activityRepository.findAll();
        if (activities.isEmpty()) return;

        Activity hikingActivity = activities.stream()
                .filter(a -> "徒步".equals(a.getType()))
                .findFirst()
                .orElse(activities.get(0));

        User user1 = users.get(0);
        User user2 = users.size() > 1 ? users.get(1) : user1;
        User user3 = users.size() > 2 ? users.get(2) : user1;
        User user4 = users.size() > 3 ? users.get(3) : user1;
        User user5 = users.size() > 4 ? users.get(4) : user1;
        User user6 = users.size() > 5 ? users.get(5) : user1;
        User user7 = users.size() > 6 ? users.get(6) : user1;

        Comment q1 = Comment.builder()
                .activity(hikingActivity)
                .user(user3)
                .content("请问集合点具体在香山公园东门的哪个位置？有明显的标志物吗？大概需要提前多久到？")
                .category("MEETING_POINT")
                .likes(5)
                .isPinned(false)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        commentRepository.save(q1);

        Comment a1 = Comment.builder()
                .activity(hikingActivity)
                .user(hikingActivity.getCreator())
                .content("东门进去有个大石碑，就在那里集合～建议提前10分钟到，我们会准时出发的！")
                .parent(q1)
                .replyToUser(user3)
                .likes(3)
                .isPinned(false)
                .createdAt(LocalDateTime.now().minusDays(2).plusHours(1))
                .build();
        commentRepository.save(a1);

        Comment q2 = Comment.builder()
                .activity(hikingActivity)
                .user(user5)
                .content("请问这个活动对新手友好吗？我平时很少运动，会不会跟不上大部队？")
                .category("BEGINNER_FRIENDLY")
                .likes(8)
                .isPinned(true)
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();
        commentRepository.save(q2);

        Comment a2 = Comment.builder()
                .activity(hikingActivity)
                .user(hikingActivity.getCreator())
                .content("完全没问题！这条路线是入门级的，全程都是修好的步道，我们会控制节奏，大家相互照应～")
                .parent(q2)
                .replyToUser(user5)
                .likes(6)
                .isPinned(false)
                .createdAt(LocalDateTime.now().minusDays(3).plusHours(2))
                .build();
        commentRepository.save(a2);

        Comment a2_2 = Comment.builder()
                .activity(hikingActivity)
                .user(user7)
                .content("我也是新手，上周参加过一次，完全跟得上，领队人超好的！")
                .parent(q2)
                .replyToUser(user5)
                .likes(2)
                .isPinned(false)
                .createdAt(LocalDateTime.now().minusDays(3).plusHours(3))
                .build();
        commentRepository.save(a2_2);

        Comment q3 = Comment.builder()
                .activity(hikingActivity)
                .user(user6)
                .content("请问费用大概是多少呀？门票是AA还是组织者统一买？")
                .category("FEE")
                .likes(4)
                .isPinned(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        commentRepository.save(q3);

        Comment a3 = Comment.builder()
                .activity(hikingActivity)
                .user(hikingActivity.getCreator())
                .content("门票10块钱自己买哈，下山后聚餐AA，人均大概50左右，丰俭由人～")
                .parent(q3)
                .replyToUser(user6)
                .likes(3)
                .isPinned(false)
                .createdAt(LocalDateTime.now().minusDays(1).plusMinutes(30))
                .build();
        commentRepository.save(a3);

        Comment q4 = Comment.builder()
                .activity(hikingActivity)
                .user(user4)
                .content("需要带什么装备吗？有没有强制要求的？")
                .category("EQUIPMENT")
                .likes(3)
                .isPinned(false)
                .createdAt(LocalDateTime.now().minusHours(5))
                .build();
        commentRepository.save(q4);

        Comment a4 = Comment.builder()
                .activity(hikingActivity)
                .user(hikingActivity.getCreator())
                .content("建议穿舒适的运动鞋，带瓶水就行～有登山杖可以带上，没有也完全没问题。")
                .parent(q4)
                .replyToUser(user4)
                .likes(2)
                .isPinned(false)
                .createdAt(LocalDateTime.now().minusHours(4))
                .build();
        commentRepository.save(a4);

        Comment generalComment = Comment.builder()
                .activity(hikingActivity)
                .user(user2)
                .content("期待！上次一起徒步超开心的，这次还能认识新朋友～")
                .likes(1)
                .isPinned(false)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();
        commentRepository.save(generalComment);
    }
}
