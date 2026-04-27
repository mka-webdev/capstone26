package com.oagp.factory;

import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;
import com.oagp.model.strategy.AiAnswerStrategy;
import com.oagp.model.strategy.AiAnswerStrategyKey;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AiAnswerStrategyFactory {
    private final Map<AiAnswerStrategyKey, AiAnswerStrategy> strategyMap;

    public AiAnswerStrategyFactory(List<AiAnswerStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        s -> new AiAnswerStrategyKey(s.provider(), s.tier()),
                        Function.identity()
                ));
    }

    public AiAnswerStrategy getStrategy(AiProvider provider, AiTier tier) {
        AiAnswerStrategy strategy = strategyMap.get(new AiAnswerStrategyKey(provider, tier));

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No strategy found for provider=" + provider + ", tier=" + tier
            );
        }

        return strategy;
    }
}
