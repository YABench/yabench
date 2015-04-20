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
						logger.info("Window #{} [{}:{}]. Missing triples in loop:\n{}", i, window.getStart(), window.getEnd(),
								prevfMeasure.getNotFoundReferences());
					}

					long startshift = 0;
					long endshift = 0;
					if (graceful) {
						if (prevfMeasure.getRecallScore() < 1) {
							for (long ts : inputWindow.getTimestampsExceptFirst()) {
								Window shiftWin = new Window(ts, (i - 1) * this.windowFactory.getSize().toMillis());
								TripleWindow startShiftWindow = inputStreamReader.readFromBuffer(shiftWin);
								BindingWindow expectedShift = queryExecutor.executeSelect(startShiftWindow);
								final FMeasure newfMeasure = new FMeasure().calculateScores(expectedShift.getBindings(),
										actual.getBindings());

								if (!newfMeasure.getNotFoundReferences().isEmpty()) {
									logger.info("Window #{} [{}:{}]. Missing triples in loop:\n{}", i, ts, this.windowFactory.getSize()
											.toMillis(), newfMeasure.getNotFoundReferences());
								}
								if (newfMeasure.getRecallScore() < prevfMeasure.getRecallScore()) {
									logger.info("break because recall got lower");
									break;
								} else if (newfMeasure.getRecallScore() == 1) {
									logger.info("break because recall == 1");
									startshift = ts;
									prevfMeasure = newfMeasure;
									break;
								} else if (newfMeasure.getRecallScore() >= prevfMeasure.getRecallScore()) {
									startshift = ts;
									logger.info("try again...");
									prevfMeasure = newfMeasure;
								}
							}
						}

						while (prevfMeasure.getPrecisionScore() < 1) {
							j++;
							long endts = inputStreamReader.readTimestampOfNextGraph();

							Window shiftWin = new Window(startshift, endts);
							TripleWindow startShiftWindow = inputStreamReader.readNextWindow(shiftWin);
							BindingWindow expectedShift = queryExecutor.executeSelect(startShiftWindow);

							final FMeasure newfMeasure = new FMeasure().calculateScores(expectedShift.getBindings(), actual.getBindings());

							if (!newfMeasure.getNotFoundReferences().isEmpty()) {
								logger.info("Window #{} [{}:{}]. Missing triples in loop:\n{}", i, startshift, endts,
										newfMeasure.getNotFoundReferences());
							}
							if (newfMeasure.getPrecisionScore() < prevfMeasure.getPrecisionScore()) {
								logger.info("break because precision got lower");
								break;
							} else if (newfMeasure.getPrecisionScore() == 1) {
								logger.info("break because precision == 1");
								endshift = endts;
								prevfMeasure = newfMeasure;
								break;
							} else if (newfMeasure.getPrecisionScore() >= prevfMeasure.getPrecisionScore()) {
								endshift = endts;
								logger.info("try again...");
								prevfMeasure = newfMeasure;
							}
						}

					}

					// todo: what if triple in the middle is missing? not
					// covered currently, because we assume that only triples at
					// window borders (start/end) can be missing

					if (!prevfMeasure.getNotFoundReferences().isEmpty()) {
						logger.info("Window #{} [{}:{}]. Missing triples:\n{}", i, inputWindow.getStart(), inputWindow.getEnd(),
								prevfMeasure.getNotFoundReferences());
					}

					startshift = startshift != 0 ? (startshift - ((i * windowFactory.getSlide().toMillis()) - windowFactory.getSize()
							.toMillis())) : startshift;
					endshift = endshift != 0 ? (endshift - (i * windowFactory.getSlide().toMillis())) : endshift;

					oracleResultsWriter.write(oracleResultBuilder.fMeasure(prevfMeasure).resultSize(expected, actual)
							.expectedInputSize(inputWindow.getTriples().size()).startshift(startshift).endshift(endshift).build());
				} else {
					throw new IllegalStateException("Actual results have more windows than expected!");
				}
			} else {
				break;
			}
		}
	}

	private long calculateDelay(final Window one, final Window two) {
		return graceful ? two.getEnd() - one.getEnd() : NO_DELAY;
	}

}
