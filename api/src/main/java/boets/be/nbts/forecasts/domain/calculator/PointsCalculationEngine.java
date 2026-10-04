package boets.be.nbts.forecasts.domain.calculator;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointsCalculationEngine {

    private final List<PointsCalculationRule> rules;

    public PointsCalculationEngine(List<PointsCalculationRule> rules) {
        this.rules = rules;
    }

    public int calculateTotalPoints(CalculationContext context) {
        return rules.stream()
                .mapToInt(rule -> rule.calculatePoints(context))
                .sum();
    }
}
