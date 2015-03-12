package io.github.yabench.streams;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;

import io.github.yabench.RSPTest;
import io.github.yabench.commons.TimeUtils;

import java.time.Duration;
import java.util.List;

import org.apache.commons.cli.Option;

/**
 * Generates air temperature observations from weather stations.
 * <p>
 * An example observation:
 * <pre>
 * <code>
 *{@literal @}prefix sens-obs: <http://knoesis.wright.edu/ssw/> .
 *{@literal @}prefix weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#> .
 *{@literal @}prefix om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#> .
 *{@literal @}prefix owl-time: <http://www.w3.org/2006/time#> .
 *
 * sens-obs:Observation_AirTemperature_ANDW4_2004_8_13_15_45_00
 *      a                       weather:TemperatureObservation ;
 *      om-owl:observedProperty weather:_AirTemperature ;
 *      om-owl:procedure        sens-obs:System_ANDW4 ;
 *      om-owl:result           sens-obs:MeasureData_AirTemperature_ANDW4_2004_8_13_15_45_00 ;
 *      om-owl:samplingTime     sens-obs:Instant_2004_8_13_15_45_00 .
 * sens-obs:MeasureData_AirTemperature_ANDW4_2004_8_13_15_45_00
 *      a                       om-owl:MeasureData ;
 *      om-owl:floatValue       "76.0"^^xsd:float ;
 *      om-owl:uom              weather:fahrenheit .
 *  sens-obs:Instant_2004_8_13_15_45_00
 *      a                       owl-time:Instant ;
 *      owl-time:inXSDDateTime  "2004-08-13T15:45:00-00:00^^http://www.w3.org/2001/XMLSchema#dateTime"
 * </code>
 * </pre>
 */
@RSPTest(name = "TestQ1")
public class SG1 extends AbstractStreamGenerator {

    private final static String TEMPLATE_NAME = "TestQ1_Template";
    private final static String ARG_NUMBER_OF_STATIONS = "stations";
    private final static String ARG_INTERVAL = "interval";
    private final static String ARG_MIN_TEMPERATURE = "min_temp";
    private final static String ARG_MAX_TEMPERATURE = "max_temp";
    private final static String DEFAULT_NUMBER_OF_STATIONS = "10";
    private final static String DEFAULT_INTERVAL = "300000"; //5 minutes in milliseconds
    private final static String DEFAULT_MIN_TEMPERATURE = "0";
    private final static String DEFAULT_MAX_TEMPERATURE = "100";
    private final int numberOfStations;
    private final Duration interval; //in milliseconds
    private final int minTemp;
    private final int maxTemp;
    private final Random random = new Random();
    private final LinkedList<Station> stations = new LinkedList<>();

    public SG1(Path destination, CommandLine options) throws IOException {
        super(destination, options);
        this.numberOfStations = Integer.parseInt(
                getCLIOptions().getOptionValue(
                        ARG_NUMBER_OF_STATIONS, DEFAULT_NUMBER_OF_STATIONS));
        this.interval = TimeUtils.parseDuration(getCLIOptions().getOptionValue(
                        ARG_INTERVAL, DEFAULT_INTERVAL));
        this.minTemp = Integer.parseInt(
                getCLIOptions().getOptionValue(
                        ARG_MIN_TEMPERATURE, DEFAULT_MIN_TEMPERATURE));
        this.maxTemp = Integer.parseInt(
                getCLIOptions().getOptionValue(
                        ARG_MAX_TEMPERATURE, DEFAULT_MAX_TEMPERATURE));
    }

    @Override
    public void generate() throws IOException {
        for (int i = 0; i < numberOfStations; i++) {
            //TODO: This is a bad idea to cast long to int!
            int step = random.nextInt((int) interval.toMillis());
            stations.add(new Station(i, step));
        }
        Collections.sort(stations);

        final String template = readTemplate(TEMPLATE_NAME);

        long currentTime = 0;
        while (currentTime <= getDuration().toMillis()) {
            Station currentStation = stations.pop();

            final float nextValue = random.nextInt(maxTemp - minTemp) + minTemp;

            writeToDestination(String.format(Locale.ENGLISH,template,
                    currentStation.id, currentTime, nextValue));

            currentStation.nextObservation += interval.toMillis();
            currentTime = currentStation.nextObservation;
            stations.addLast(currentStation);
        }
    }

    public static List<Option> expectedOptions() {
        List<Option> options = getCommonExpectedOptions();

        options.add(OptionBuilder
                .withArgName("number")
                .withDescription("default: " + DEFAULT_NUMBER_OF_STATIONS)
                .hasArg()
                .create(ARG_NUMBER_OF_STATIONS));

        options.add(OptionBuilder
                .withArgName("milliseconds")
                .withDescription("default: " + DEFAULT_INTERVAL)
                .hasArg()
                .create(ARG_INTERVAL));

        options.add(OptionBuilder
                .withArgName("degree")
                .withDescription("default: " + DEFAULT_MIN_TEMPERATURE)
                .hasArg()
                .create(ARG_MIN_TEMPERATURE));

        options.add(OptionBuilder
                .withArgName("degree")
                .withDescription("default: " + DEFAULT_MAX_TEMPERATURE)
                .hasArg()
                .create(ARG_MAX_TEMPERATURE));
        return options;
    }

    private static class Station implements Comparable<Station> {

        public long nextObservation;
        public int id;

        public Station(int id, int step) {
            this.id = id;
            this.nextObservation = step;
        }

        @Override
        public int compareTo(Station t) {
            return Long.compare(this.nextObservation, t.nextObservation);
        }

    }

}
