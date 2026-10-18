package boets.be.nbts.forecasts.domain.calculator;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointsCalculationEngine {

    private final List<PointsCalculationRule> rules;

    public PointsCalculationEngine(List<PointsCalculationRule> rules) {
        this.rules = rules;
    }

    public CalculationContext calculateTotalPoints(CalculationContext context) {
        CalculationContext currentContext = context;
        for (PointsCalculationRule rule : rules) {
            currentContext = rule.calculatePoints(currentContext);
        }
        return currentContext;
    }
}
