package edu.sjsu.cmpe172.picklereserve.ai;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("ai")
@Service
public class RagService {
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(VectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    public String answer(String question) {
        List<Document> matches = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question).topK(3).build());
        String context = matches == null ? "" : matches.stream()
                .map(Document::getText).collect(Collectors.joining("\n\n---\n\n"));
        return chatClient.prompt()
                .system("Answer only from the supplied PickleReserve policy context. "
                        + "If the answer is not in the context, say that the policy does not specify it.")
                .user("Knowledge context:\n" + context + "\n\nQuestion:\n" + question)
                .call().content();
    }
}
