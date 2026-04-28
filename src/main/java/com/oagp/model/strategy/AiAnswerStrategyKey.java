package com.oagp.model.strategy;

import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;

public record AiAnswerStrategyKey(AiProvider provider, AiTier tier) {}
