package com.oagp.model.strategy;

import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputText;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OpenAiPaidStrategy implements AiAnswerStrategy {

    private static final Logger log =
            LoggerFactory.getLogger(OpenAiPaidStrategy.class);

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
        log.debug("API key: {}", apiKey);
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("No api key provided");
            throw new IllegalArgumentException("OpenAI API key is missing. Please set the OPENAI_API_KEY environment variable.");
        }

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();

        try {
            log.debug("Sending prompt to OpenAI");
            ResponseCreateParams params = ResponseCreateParams.builder()
                    .input(question)
                    .model(ChatModel.GPT_5_2)
                    .build();
            Response resp = client.responses().create(params);

            return resp.output().stream()
                    .flatMap(item -> item.message().stream())
                    .flatMap(message -> message.content().stream())
                    .flatMap(content -> content.outputText().stream())
                    .map(ResponseOutputText::text)
                    .collect(java.util.stream.Collectors.joining())
                    .trim();
        } catch (Exception e) {
            log.error("Error while creating OpenAI response", e);
            return "OpenAI service returned an unexpected error. Please try again later.";
        }
    }
}
