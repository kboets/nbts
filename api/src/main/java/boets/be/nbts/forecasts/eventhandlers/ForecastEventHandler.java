package boets.be.nbts.forecasts.eventhandlers;

import boets.be.nbts.datacollector.models.ForecastCollectedEvent;
import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.forecasts.domain.ForecastService;
import boets.be.nbts.forecasts.domain.calculator.CalculationContext;
import boets.be.nbts.forecasts.domain.calculator.PointsCalculationEngine;
import boets.be.nbts.forecasts.domain.models.Forecast;
import boets.be.nbts.forecasts.domain.models.ForecastDetail;
import boets.be.nbts.forecasts.domain.models.ForecastRound;
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

        //1. Check if the leagues should be forecasted
        for (var rawData : rawDataList) {
            List<Result> results = rawData.results();
            //1. check if the current round is already forecasted, if so, skip the forecasting for this league
            if (forecastService.isForecastAlreadyCalculated(rawData)) {
                log.info("[FORECASTS] Forecast already calculated for league {} and round {}, skipping forecasting", rawData.league().name(), rawData.currentRound());
                continue;
            }
            //2. check if there are enough results to forecast, if not, skip the forecasting for this league
            if (!forecastService.canForecastResults(results)) {
                log.info("[FORECASTS] Not enough results to forecast for league {} and round {}, skipping forecasting", rawData.league().name(), rawData.currentRound());
                continue;
            }
            updatedForecastList.add(rawData);
        }

        for (var rawData : updatedForecastList) {
            //1. Prepare the data for forecasting
           List<CalculationContext> calculationContexts = forecastService.prepareDataForForecasting(rawData);
           List<Forecast> forecasts = new ArrayList<>();
           List<ForecastDetail> forecastDetails = new ArrayList<>();
           Forecast forecast = new Forecast(rawData.league().name(), rawData.league().countryCode(), forecastDetails);
           forecasts.add(forecast);
           for (var context : calculationContexts) {
               // 2. call the points engine rule
               int totalPoints = pointsCalculationEngine.calculateTotalPoints(context);
               log.info("[FORECASTS] Total points for context {}: {}", context, totalPoints);
               ForecastDetail forecastDetail = new ForecastDetail(context.nextMatch().matchDate(), context.team(), context.opponent(), totalPoints);
               forecastDetails.add(forecastDetail);
           }
           forecast.withForecastDetails(forecastDetails);
           //3. save the forecast results
            ForecastRound forecastRound = new ForecastRound(calculationContexts.getFirst().nextMatch().round(), rawData.league().leagueId(), rawData.league().season());
           forecastService.saveForecastCache(forecastRound, forecasts);
        }
    }
}
