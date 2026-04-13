package com.oagp.client;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.oagp.model.AITier;
import com.oagp.model.GenerativeAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Component;

@Component
public class OpenAiClient implements GenerativeAIClient {

    private final OpenAIClient  client;

    public OpenAiClient() {
        client = OpenAIOkHttpClient.fromEnv();
    }

    @Override
    public String ask(String question) {
        return ask(question, null);
    }

    @Override
    public String ask(String question, AITier tier) {
        ResponseCreateParams params = ResponseCreateParams.builder()
                .input(question)
                .model(ChatModel.GPT_5_2)
                .build();
        Response resp = client.responses().create(params);

        return client.responses().create(params).output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text())
                .collect(java.util.stream.Collectors.joining())
                .trim();
    }
}
