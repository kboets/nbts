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
        log.info("[FORECASTS] Received forecast event");

        var rawDataList = event.forecastRawData();
        var updatedForecastList = new ArrayList<ForecastRawData>();

        //1. Verify if the forecast is still valid, if not, skip the forecasting for this league
        for (var rawData : rawDataList) {
            List<Result> results = rawData.results();
            //A. check if the next round is already forecasted, if so, skip the forecasting for this league
            if (forecastService.isForecastAlreadyCalculated(rawData)) {
                log.info("[FORECASTS] Forecast already calculated for league {} and round {}, skipping forecasting", rawData.league().name(), rawData.currentRound());
                continue;
            }
            //B. check if there are enough results to forecast, if not, skip the forecasting for this league
            if (!forecastService.canForecastResults(results)) {
                log.info("[FORECASTS] Not enough results to forecast for league {} and round {}, skipping forecasting", rawData.league().name(), rawData.currentRound());
                continue;
            }
            updatedForecastList.add(rawData);
        }

        // 2. For each valid raw data, prepare the data for forecasting, call the points engine rule and save the forecast results
        for (var rawData : updatedForecastList) {
            //A. Prepare the data for forecasting
           List<CalculationContext> calculationContexts = forecastService.prepareDataForForecasting(rawData);
           List<Forecast> forecasts = new ArrayList<>();
           List<ForecastDetail> forecastDetails = new ArrayList<>();
           Forecast forecast = new Forecast(rawData.league().name(),
                   rawData.league().countryCode(),
                   calculationContexts.getFirst().nextMatch().round(),
                   forecastDetails);
           for (var context : calculationContexts) {
               //B. call the points engine rule
               CalculationContext resultContext = pointsCalculationEngine.calculateTotalPoints(context);
               int totalPoints = resultContext.teamScore() - resultContext.opponentScore();
               log.info("[FORECASTS] Total points for context {}: {}", resultContext, totalPoints);
               ForecastDetail forecastDetail = new ForecastDetail(resultContext.nextMatch().matchDate(), resultContext.team(), resultContext.opponent(), totalPoints);
               forecastDetails.add(forecastDetail);
           }
           forecast = forecast.withForecastDetails(forecastDetails);
           forecasts.add(forecast);
           //C. save the forecast results
           ForecastRound forecastRound = new ForecastRound(calculationContexts.getFirst().nextMatch().round(), rawData.league().leagueId(), rawData.league().season());
           forecastService.saveForecastCache(forecastRound, forecasts);
        }
    }
}
