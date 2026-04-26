package com.oagp.model.strategy;

import com.oagp.model.AIProvider;
import com.oagp.model.AITier;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Component;

@Component
public class OpenAIPaidStrategy implements AIAnswerStrategy {

    private final String apiKey;

    public OpenAIPaidStrategy() {
        this.apiKey = System.getenv("OPENAI_API_KEY");
    }

    @Override
    public AIProvider provider() {
        return AIProvider.OPEN_AI;
    }

    @Override
    public AITier tier() {
        return AITier.PAID;
    }
    @Override
    public String ask(String question) throws IllegalArgumentException {
        if (apiKey == null || apiKey.isEmpty())
            throw new IllegalArgumentException("OpenAI API key is missing. Please set the OPENAI_API_KEY environment variable.");

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();

        ResponseCreateParams params = ResponseCreateParams.builder()
                .input(question)
                .model(ChatModel.GPT_5_2)
                .build();
        Response resp = client.responses().create(params);

        return resp.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text())
                .collect(java.util.stream.Collectors.joining())
                .trim();
    }
}
