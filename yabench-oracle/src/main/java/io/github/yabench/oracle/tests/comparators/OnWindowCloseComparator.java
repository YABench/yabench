package io.github.yabench.oracle.tests.comparators;

import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.readers.EngineResultsReader;
import io.github.yabench.oracle.FMeasure;
import io.github.yabench.oracle.readers.BufferedTWReader;
import io.github.yabench.oracle.OracleResultBuilder;
import io.github.yabench.oracle.OracleResultsWriter;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.Window;
import io.github.yabench.oracle.WindowFactory;
import io.github.yabench.oracle.readers.TripleWindowReader;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class OnWindowCloseComparator implements OracleComparator {

	private static final Logger logger = LoggerFactory.getLogger(OnWindowCloseComparator.class);
	private final BufferedTWReader inputStreamReader;
	private final EngineResultsReader queryResultsReader;
	private final WindowFactory windowFactory;
	private final QueryExecutor queryExecutor;
	private final OracleResultsWriter oracleResultsWriter;
	private final boolean graceful;

	OnWindowCloseComparator(BufferedTWReader inputStreamReader, EngineResultsReader queryResultsReader, WindowFactory windowFactory,
			QueryExecutor queryExecutor, OracleResultsWriter oracleResultsWriter, boolean graceful) {
		this.inputStreamReader = inputStreamReader;
		this.queryResultsReader = queryResultsReader;
		this.windowFactory = windowFactory;
		this.queryExecutor = queryExecutor;
		this.oracleResultsWriter = oracleResultsWriter;
		this.graceful = graceful;
	}

	@Override
	public void compare() throws IOException {
		final OracleResultBuilder oracleResultBuilder = new OracleResultBuilder();
		for (int i = 1;; i++) {
			final Window window = windowFactory.nextWindow();
			final BindingWindow actual = queryResultsReader.next();
			if (actual != null) {
				inputStreamReader.purge(window.getStart());
				final TripleWindow inputWindow = inputStreamReader.readNextWindow(window);

				if (inputWindow != null) {
					BindingWindow expected = queryExecutor.executeSelect(inputWindow);
					final FMeasure fMeasure = new FMeasure().calculateScores(expected.getBindings(), actual.getBindings());
					FMeasure prevfMeasure = fMeasure;

					if (!prevfMeasure.getNotFoundReferences().isEmpty()) {
						logger.info("Window #{} [{}:{}]. Missing triples:", i, window.getStart(), window.getEnd());
						logger.info("missing triples");
					}

					long startshift = (i * windowFactory.getSlide().toMillis()) - windowFactory.getSize().toMillis();
					long endshift = (i * windowFactory.getSlide().toMillis());

					logger.info("expected bindings size: " + String.valueOf(expected.getBindings().size()));
					logger.info("actual bindings size: " + String.valueOf(actual.getBindings().size()));
					logger.info("precision: " + prevfMeasure.getPrecisionScore());
					logger.info("recall: " + prevfMeasure.getRecallScore());
					logger.info("wsize: " + inputWindow.getTriples().size());
					long delay = actual.getEnd() - expected.getEnd();

					if (!(expected.getBindings().size() == 0 && actual.getBindings().size() == 0)) {
						if (graceful) {
							delay = -1;
							if (prevfMeasure.getRecallScore() < 1) {
								TreeSet<Long> tslist = inputWindow.getTimestampsExceptFirst();
								for (long ts : tslist) {
									Window shiftWin = new Window(ts, (i - 1) * this.windowFactory.getSize().toMillis());
									TripleWindow startShiftWindow = inputStreamReader.readFromBuffer(shiftWin);
									BindingWindow expectedShift = queryExecutor.executeSelect(startShiftWindow);
									final FMeasure newfMeasure = new FMeasure().calculateScores(expectedShift.getBindings(),
											actual.getBindings());

									if (!newfMeasure.getNotFoundReferences().isEmpty()) {
										// logger.info("Window #{} [{}:{}]. Missing triples in loop:\n{}",
										// i, ts, this.windowFactory.getSize()
										// .toMillis(),
										// newfMeasure.getNotFoundReferences());
										logger.info("missing triples!");
									}

									if (newfMeasure.getRecallScore() < prevfMeasure.getRecallScore()) {
										break;
									} else if (newfMeasure.getRecallScore() == 1) {
										startshift = ts;

										prevfMeasure = newfMeasure;
										break;
									} else if (newfMeasure.getRecallScore() >= prevfMeasure.getRecallScore()) {
										startshift = ts;
										prevfMeasure = newfMeasure;
									}

								}
							}
							while ((prevfMeasure.getPrecisionScore() < 1)) {

								long endts = inputStreamReader.readTimestampOfNextGraph();
								if (endts != -1) {
									Window shiftWin = new Window(startshift, endts);
									TripleWindow startShiftWindow = inputStreamReader.readNextWindow(shiftWin);
										BindingWindow expectedShift = queryExecutor.executeSelect(startShiftWindow);

									final FMeasure newfMeasure = new FMeasure().calculateScores(expectedShift.getBindings(),
											actual.getBindings());

									if (!newfMeasure.getNotFoundReferences().isEmpty()) {
										logger.info("Window #{} [{}:{}]. Missing triples in loop:", i, startshift, endts);
										// logger.info("missing triples!");
									}
									if ((newfMeasure.getPrecisionScore() < prevfMeasure.getPrecisionScore()) ||
											(newfMeasure.getRecallScore() < prevfMeasure.getRecallScore())) {
										break;
									} else if (newfMeasure.getPrecisionScore() == 1) {
										endshift = endts;
										prevfMeasure = newfMeasure;
										break;
									} else if (newfMeasure.getPrecisionScore() >= prevfMeasure.getPrecisionScore()) {
										endshift = endts;
										prevfMeasure = newfMeasure;
									}
								} else {
									logger.info("Window cannot be shifted back anymore, because there are no more triples in the stream to get the timestamp from.");
									break;
								}
					
							}

						}

					} else {
						logger.info("0 bindings!");
					}

					// todo: what if triple in the middle is missing? not
					// covered currently, because we assume that only triples at
					// window borders (start/end) can be missing

					if (!prevfMeasure.getNotFoundReferences().isEmpty()) {
						// logger.info("Window #{} [{}:{}]. Missing triples in loop:\n{}",
						// i, ts, this.windowFactory.getSize()
						// .toMillis(),
						// newfMeasure.getNotFoundReferences());
						logger.info("missing triples!");
					}

					startshift = startshift < 0 ? 0 : startshift;

					oracleResultsWriter.write(oracleResultBuilder.fMeasure(prevfMeasure).resultSize(expected, actual)
							.expectedInputSize(inputWindow.getTriples().size()).startshift(startshift).endshift(endshift).delay(delay).build());
				} else {
					throw new IllegalStateException("Actual results have more windows than expected!");
				}
			} else {
				break;
			}
		}

	}

	private static void logOutputBinding(List<Binding> bs) {
		for (Binding b : bs) {
			logger.info(b.toString());
		}

	}

	private static void logOutputTriples(List<TemporalTriple> ts) {
		int i = 1;
		for (TemporalTriple t : ts) {
			logger.info(i + " - " + t.getStatement().toString());
			i++;
		}

	}

	private long calculateDelay(final Window one, final Window two) {
		return graceful ? two.getEnd() - one.getEnd() : NO_DELAY;
	}

}
