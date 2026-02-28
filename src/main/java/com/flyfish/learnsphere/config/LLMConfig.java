package com.flyfish.learnsphere.config;


import com.flyfish.learnsphere.memory.RedisChatMemoryStore;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 大模型配置
 * @Author: FlyFish
 * @CreateTime: 2025/11/26
 */
@Configuration
public class LLMConfig {

    @Value("${deepseek.apiKey}")
    private String apiKey;

    @Value("${embedding.apiKey:${deepseek.apiKey}}")
    private String embeddingApiKey;

    @Value("${embedding.baseUrl:https://api.openai.com/v1}")
    private String embeddingBaseUrl;

    @Value("${embedding.modelName:text-embedding-3-small}")
    private String embeddingModelName;

    @Bean
    public ChatLanguageModel deepseekChatModel(){
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-reasoner")
                .temperature(0.3)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-reasoner")
                .build();
    }

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisChatMemoryStore(stringRedisTemplate);
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(embeddingApiKey)
                .baseUrl(embeddingBaseUrl)
                .modelName(embeddingModelName)
                .build();
    }
}
