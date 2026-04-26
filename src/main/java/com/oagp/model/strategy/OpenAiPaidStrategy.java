package com.oagp.model.strategy;

import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Component;

@Component
public class OpenAiPaidStrategy implements AiAnswerStrategy {

    private final String apiKey;

    public OpenAiPaidStrategy() {
        this.apiKey = System.getenv("OPENAI_API_KEY");
    }

    @Override
    public AiProvider provider() {
        return AiProvider.OPEN_AI;
    }

    @Override
    public AiTier tier() {
        return AiTier.PAID;
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
