package com.example.whathis.aiSupport.inquiry.service;

import com.example.whathis.config.CustomUserDetails;
import com.example.whathis.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final ChatClient chatClient;

    public String inquiry(String userMessage, String conversationId, CustomUserDetails userDetails) {

        //현재 날짜 계산
        String currentDate = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm (E)", Locale.KOREAN));

        User currentUser = userDetails != null ? userDetails.getUser() : null;
        // 로그인 사용자의 conversation_id값을 userId 기반으로 고정해서, 과거 대화 기록을 유지할 수 있게 함.
        final String effectiveConversationId = (currentUser != null)
            ? UUID.nameUUIDFromBytes(("user:" + currentUser.getId()).getBytes(StandardCharsets.UTF_8))
            .toString()
            : conversationId;

        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put("isAuthenticated", currentUser != null);
        if (currentUser != null) {
            putIfNotNull(toolContext, "userId", currentUser.getId());
            putIfNotNull(toolContext, "userEmail", currentUser.getEmail());
            putIfNotNull(toolContext, "userName", currentUser.getName());
            putIfNotNull(toolContext, "userNickname", currentUser.getNickname());
        }

        return chatClient.prompt()
            // AiConfig의 system prompt 템플릿에 날짜를 매 요청 시점에 주입합니다.
            .system(s -> s.param("current_date", currentDate))
            // default system prompt를 덮어쓰지 않도록, 사용자 컨텍스트는 별도 system message로 추가합니다.
            .messages(new SystemMessage(buildUserContextSystemMessage(currentUser)))
            .user(userMessage)
            .toolContext(toolContext)

            // Advisor에게 사용자 ID를 알리며, ChatMemoryAdvisor는 이 ID를 키(Key)로 사용하여 DB에서 이전 대화 기록을 불러옵니다.
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, effectiveConversationId))
            .call()
            .content();
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private String buildUserContextSystemMessage(User user) {
        if (user == null) {
            return "현재 대화 사용자는 비로그인 상태입니다. 로그인 기능이 필요한 요청(펀딩/최근 본 상품/개인화 추천)은 로그인 필요 안내를 하세요.";
        }

        return "현재 대화 사용자는 로그인 상태입니다. 사용자 이름/닉네임 정보를 참고해 응대하되, 이메일/내부 ID 등 민감한 식별자를 답변에 그대로 노출하지 마세요. " +
            "이름: " + user.getName() + ", 닉네임: " + user.getNickname() + ".";
    }
}
