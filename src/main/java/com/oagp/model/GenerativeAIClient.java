package com.oagp.model;

public interface GenerativeAIClient {
    String ask(String question);
    String ask(String question, AITier tier);
}
