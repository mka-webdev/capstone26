package com.oagp.model.strategy;

import com.oagp.model.AIProvider;
import com.oagp.model.AITier;

public interface AIAnswerStrategy {
    AIProvider provider();
    AITier tier();
    String ask(String question) throws IllegalArgumentException;
}
