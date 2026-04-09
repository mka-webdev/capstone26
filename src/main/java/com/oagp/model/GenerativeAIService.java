package com.oagp.model;

public interface GenerativeAIService {
    String ask(String question);
    String ask(String question, AITier tier);
}
