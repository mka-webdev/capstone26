package com.oagp.model.strategy;

import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;

public interface AiAnswerStrategy {
    AiProvider provider();
    AiTier tier();
    String ask(String question) throws IllegalArgumentException;
}
