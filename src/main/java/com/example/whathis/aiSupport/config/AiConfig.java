package com.example.whathis.aiSupport.config;

import com.example.whathis.aiSupport.inquiry.tools.InquiryTools;
import com.example.whathis.aiSupport.search.tools.SearchTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class AiConfig {

    @Value("classpath:/prompts/inquiry-system-prompt.st")
    private Resource inquiryPromptResource;

    @Value("classpath:/prompts/search-system-prompt.st")
    private Resource searchPromptResource;

    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder chatclientBuilder, JdbcChatMemoryRepository jdbcChatMemoryRepository, InquiryTools inquiryTools) {

        String systemPromptTemplate = loadPromptTemplate(inquiryPromptResource);
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(jdbcChatMemoryRepository)
            .maxMessages(15)
            .build();

        return chatclientBuilder
            .defaultSystem(systemPromptTemplate)
            .defaultTools(inquiryTools)
            .defaultAdvisors(
                new SimpleLoggerAdvisor(),
                MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .build();
    }

    @Bean
    public ChatClient searchChatClient(ChatClient.Builder chatclientBuilder, SearchTools searchTools) {
        String systemPromptTemplate = loadPromptTemplate(searchPromptResource);
        return chatclientBuilder
            .defaultSystem(systemPromptTemplate)
            .defaultTools(searchTools)
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .build();
    }

    // 프롬프트 파일 불러오기 (템플릿 원문 그대로)
    private String loadPromptTemplate(Resource resource) {
        try {
            return StreamUtils.copyToString(
                resource.getInputStream(),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("프롬프트 파일을 읽을 수 없습니다.", e);
        }
    }

}
