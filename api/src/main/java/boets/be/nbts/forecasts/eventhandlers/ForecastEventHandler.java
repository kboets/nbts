package boets.be.nbts.forecasts.eventhandlers;

import boets.be.nbts.datacollector.models.ForecastCollectedEvent;
import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.forecasts.domain.ForecastService;
import boets.be.nbts.forecasts.domain.calculator.CalculationContext;
import boets.be.nbts.forecasts.domain.calculator.PointsCalculationEngine;
import boets.be.nbts.forecasts.domain.models.Forecast;
import boets.be.nbts.results.domain.models.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ForecastEventHandler {

    private final ForecastService forecastService;
    private final PointsCalculationEngine pointsCalculationEngine;


    @ApplicationModuleListener
    public void onForecastDataCollected(ForecastCollectedEvent event) {
        // Handle the forecast event here
        log.info("[FORECASTS] Received forecast event: {}", event);


        var rawDataList = event.forecastRawData();
        var updatedForecastList = new ArrayList<ForecastRawData>();

        //1. Check if the results should be forecasted based on the current round and the number of available results.
        for (var rawData : rawDataList) {
            List<Result> results = rawData.results();
            if (forecastService.shouldResultBeForecasted(results)) {
                updatedForecastList.add(rawData);
            }
        }

        for (var rawData : updatedForecastList) {
            //1. Prepare the data for forecasting
           List<CalculationContext> calculationContexts = forecastService.prepareDataForForecasting(rawData);
           List<Forecast> forecasts = new ArrayList<>();
           for (var context : calculationContexts) {
               // 2. call the points engine rule
               int totalPoints = pointsCalculationEngine.calculateTotalPoints(context);
               log.info("[FORECASTS] Total points for context {}: {}", context, totalPoints);
               Forecast forecast = new Forecast(context.nextMatch(), context.team(), context.opponent(), totalPoints);
               forecasts.add(forecast);
           }
           //3. save the forecast results



        }



    }
}
