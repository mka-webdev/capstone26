package com.oagp.factory;

import com.oagp.model.AIProvider;
import com.oagp.model.AITier;
import com.oagp.model.strategy.AIAnswerStrategy;
import com.oagp.model.strategy.AIAnswerStrategyKey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AIAnswerStrategyFactory {
    private final Map<AIAnswerStrategyKey, AIAnswerStrategy> strategyMap;

    public AIAnswerStrategyFactory(List<AIAnswerStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        s -> new AIAnswerStrategyKey(s.provider(), s.tier()),
                        Function.identity()
                ));
    }

    public AIAnswerStrategy getStrategy(AIProvider provider, AITier tier) {
        AIAnswerStrategy strategy = strategyMap.get(new AIAnswerStrategyKey(provider, tier));

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No strategy found for provider=" + provider + ", tier=" + tier
            );
        }

        return strategy;
    }
}
