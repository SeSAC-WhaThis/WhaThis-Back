package com.example.whathis.aiSupport.inquiry.controller;

import com.example.whathis.aiSupport.inquiry.service.InquiryService;
import com.example.whathis.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ai/inquiry")
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;


    @PostMapping
    public String Inquiry(@RequestBody Map<String, String> requestBody,
                          @RequestHeader(value = "ConversationId", required = false) String conversationId,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userMessage = requestBody.get("message");
        // 문의한 사용자 ID 관리 (Session Management)
        String currentConversationId = (conversationId != null) ? conversationId : UUID.randomUUID().toString();
        return inquiryService.inquiry(userMessage, currentConversationId, userDetails);


    }
}
