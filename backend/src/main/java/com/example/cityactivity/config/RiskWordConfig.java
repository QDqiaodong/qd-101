package com.example.cityactivity.config;

import com.example.cityactivity.enums.RiskLevel;
import com.example.cityactivity.enums.RiskType;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RiskWordConfig {

    @Getter
    private final List<RiskWordEntry> riskWords = new ArrayList<>();

    public RiskWordConfig() {
        initVulgarInvitationWords();
        initGrayMarketingWords();
        initFakeFeeWords();
        initLocationInductionWords();
    }

    private void initVulgarInvitationWords() {
        riskWords.add(new RiskWordEntry("约炮", RiskType.VULGAR_INVITATION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("一夜情", RiskType.VULGAR_INVITATION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("上门服务", RiskType.VULGAR_INVITATION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("特殊服务", RiskType.VULGAR_INVITATION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("全套服务", RiskType.VULGAR_INVITATION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("美女陪玩", RiskType.VULGAR_INVITATION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("帅哥陪玩", RiskType.VULGAR_INVITATION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("寂寞", RiskType.VULGAR_INVITATION, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("交友", RiskType.VULGAR_INVITATION, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("单身", RiskType.VULGAR_INVITATION, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("私聊", RiskType.VULGAR_INVITATION, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("加微信", RiskType.VULGAR_INVITATION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("加v", RiskType.VULGAR_INVITATION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("vx", RiskType.VULGAR_INVITATION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("暧昧", RiskType.VULGAR_INVITATION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("性感", RiskType.VULGAR_INVITATION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("刺激", RiskType.VULGAR_INVITATION, RiskLevel.LOW));
    }

    private void initGrayMarketingWords() {
        riskWords.add(new RiskWordEntry("赌博", RiskType.GRAY_MARKETING, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("博彩", RiskType.GRAY_MARKETING, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("彩票", RiskType.GRAY_MARKETING, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("时时彩", RiskType.GRAY_MARKETING, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("刷单", RiskType.GRAY_MARKETING, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("兼职刷单", RiskType.GRAY_MARKETING, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("日赚千元", RiskType.GRAY_MARKETING, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("月入过万", RiskType.GRAY_MARKETING, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("轻松赚钱", RiskType.GRAY_MARKETING, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("躺赚", RiskType.GRAY_MARKETING, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("传销", RiskType.GRAY_MARKETING, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("直销", RiskType.GRAY_MARKETING, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("微商代理", RiskType.GRAY_MARKETING, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("加盟", RiskType.GRAY_MARKETING, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("贷款", RiskType.GRAY_MARKETING, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("网贷", RiskType.GRAY_MARKETING, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("黑户贷款", RiskType.GRAY_MARKETING, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("无抵押", RiskType.GRAY_MARKETING, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("医美", RiskType.GRAY_MARKETING, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("整容", RiskType.GRAY_MARKETING, RiskLevel.LOW));
    }

    private void initFakeFeeWords() {
        riskWords.add(new RiskWordEntry("报名费", RiskType.FAKE_FEE, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("押金", RiskType.FAKE_FEE, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("保证金", RiskType.FAKE_FEE, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("入会费", RiskType.FAKE_FEE, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("会员费", RiskType.FAKE_FEE, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("服务费", RiskType.FAKE_FEE, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("手续费", RiskType.FAKE_FEE, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("定金", RiskType.FAKE_FEE, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("预付款", RiskType.FAKE_FEE, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("激活费", RiskType.FAKE_FEE, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("解冻费", RiskType.FAKE_FEE, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("验资", RiskType.FAKE_FEE, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("转账", RiskType.FAKE_FEE, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("支付宝转账", RiskType.FAKE_FEE, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("微信转账", RiskType.FAKE_FEE, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("打款", RiskType.FAKE_FEE, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("扫码支付", RiskType.FAKE_FEE, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("付款码", RiskType.FAKE_FEE, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("红包", RiskType.FAKE_FEE, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("先付钱", RiskType.FAKE_FEE, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("先缴费", RiskType.FAKE_FEE, RiskLevel.HIGH));
    }

    private void initLocationInductionWords() {
        riskWords.add(new RiskWordEntry("酒店房间", RiskType.LOCATION_INDUCTION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("宾馆", RiskType.LOCATION_INDUCTION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("民宿", RiskType.LOCATION_INDUCTION, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("家里", RiskType.LOCATION_INDUCTION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("家中", RiskType.LOCATION_INDUCTION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("出租屋", RiskType.LOCATION_INDUCTION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("私人会所", RiskType.LOCATION_INDUCTION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("偏僻", RiskType.LOCATION_INDUCTION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("郊区", RiskType.LOCATION_INDUCTION, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("地下", RiskType.LOCATION_INDUCTION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("秘密地点", RiskType.LOCATION_INDUCTION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("保密", RiskType.LOCATION_INDUCTION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("单独见面", RiskType.LOCATION_INDUCTION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("一个人来", RiskType.LOCATION_INDUCTION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("不要告诉别人", RiskType.LOCATION_INDUCTION, RiskLevel.HIGH));
        riskWords.add(new RiskWordEntry("偷偷", RiskType.LOCATION_INDUCTION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("夜黑风高", RiskType.LOCATION_INDUCTION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("深夜", RiskType.LOCATION_INDUCTION, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("凌晨", RiskType.LOCATION_INDUCTION, RiskLevel.LOW));
        riskWords.add(new RiskWordEntry("小路", RiskType.LOCATION_INDUCTION, RiskLevel.MEDIUM));
        riskWords.add(new RiskWordEntry("巷子", RiskType.LOCATION_INDUCTION, RiskLevel.MEDIUM));
    }

    @Getter
    public static class RiskWordEntry {
        private final String word;
        private final RiskType riskType;
        private final RiskLevel riskLevel;

        public RiskWordEntry(String word, RiskType riskType, RiskLevel riskLevel) {
            this.word = word;
            this.riskType = riskType;
            this.riskLevel = riskLevel;
        }
    }
}
