package com.oagp.model.strategy;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class GeminiFreeStrategy implements AiAnswerStrategy {

    private static final Logger log =
            LoggerFactory.getLogger(GeminiFreeStrategy.class);

    private final String apiKey;

    @Value("${gemini.model}")
    private String model;

    public GeminiFreeStrategy() {
        this.apiKey = System.getenv("GEMINI_API_KEY_FREE");
    }

    @Override
    public AiProvider provider() {
        return AiProvider.GEMINI;
    }

    @Override
    public AiTier tier() {
        return AiTier.FREE;
    }

    @Override
    public String ask(String question) throws IllegalArgumentException {
        log.debug("API key: {}", apiKey);
        if (apiKey == null || apiKey.isEmpty()){
            log.error("No api key provided");
            throw new IllegalArgumentException("Gemini API key is missing. Please set the GEMINI_API_KEY_FREE environment variable.");
        }

        log.debug("Sending prompt to Gemini");
        GenerateContentResponse response;
        try(Client client = Client.builder().apiKey(apiKey).build()){
            response = client.models.generateContent(
                    model,
                    question,
                    null
            );
        }catch (IOException e) {
            log.error("Error while creating Gemini Free Response", e);
            return "Unable to connect to the Gemini API. Please check your internet connection and try again.";
        } catch (HttpException e) {
            log.error("Error while creating Gemini Free Response", e);
            return "The Gemini API is currently unavailable or rejected the request. Please try again later.";
        }

        return response.text();
    }
}
