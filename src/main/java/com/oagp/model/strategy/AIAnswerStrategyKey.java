package com.oagp.model.strategy;

import com.oagp.model.AIProvider;
import com.oagp.model.AITier;

public record AIAnswerStrategyKey(AIProvider provider, AITier tier) {}
