package edu.sjsu.cmpe172.picklereserve.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Configuration
@Profile("ai")
public class AiConfiguration {
    @Bean
    ChatClient aiChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    VectorStore knowledgeVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    KnowledgeSourceLoader knowledgeSourceLoader(VectorStore vectorStore) {
        return new KnowledgeSourceLoader(vectorStore);
    }

    public static class KnowledgeSourceLoader {
        private final VectorStore vectorStore;

        KnowledgeSourceLoader(VectorStore vectorStore) {
            this.vectorStore = vectorStore;
        }

        @jakarta.annotation.PostConstruct
        void load() throws IOException {
            ClassPathResource resource = new ClassPathResource("knowledge/picklereserve-faq.txt");
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            vectorStore.add(List.of(Document.builder()
                    .text(content)
                    .metadata(Map.of("source", "picklereserve-faq.txt"))
                    .build()));
        }
    }
}
