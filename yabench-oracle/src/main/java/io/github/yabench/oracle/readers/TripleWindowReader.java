package io.github.yabench.oracle.readers;

import io.github.yabench.commons.TemporalGraph;
import io.github.yabench.commons.TemporalRDFReader;
import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.Window;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripleWindowReader implements Closeable, AutoCloseable {

	private static final int NOT_FOUND = -1;
	private final TemporalRDFReader reader;
	private static final Logger logger = LoggerFactory.getLogger(TripleWindowReader.class);
	private List<TemporalTriple> content = new ArrayList<>();

	public TripleWindowReader(Reader reader) {
		this.reader = new TemporalRDFReader(reader);
	}

	public TripleWindowReader(TemporalRDFReader reader) {
		this.reader = reader;
	}

	protected TemporalRDFReader getReader() {
		return reader;
	}

	public TripleWindow readNextWindow(final Window window) throws IOException {
		content = new ArrayList<>(Arrays.asList(content.stream().filter((triple) -> triple.getTime() >= window.getStart())
				.toArray(TemporalTriple[]::new)));

		TemporalTriple triple;
		while ((triple = reader.readNextTriple()) != null) {
			if (triple.getTime() <= window.getEnd()) {
				content.add(triple);
			} else {
				break;
			}
		}

		final TripleWindow w = new TripleWindow(new ArrayList<>(content), window.getStart(), window.getEnd());
		if (triple != null) {
			content.add(triple);
		} else if (triple == null && content.isEmpty()) {
			return null;
		}
		return w;
	}

	public long readTimestampOfNextTriple() throws IOException {
		TemporalTriple triple = reader.readNextTriple();
		if (triple != null) {
			content.add(triple);
			return triple.getTime();
		} else {
			return NOT_FOUND;
		}
	}

	public long readTimestampOfNextGraph() throws IOException {
		TemporalGraph graph = reader.readNextGraph();
		long retTime = NOT_FOUND;
		if (graph != null) {
			for (TemporalTriple triple : graph.getTriples()) {
				content.add(triple);
				if (retTime == NOT_FOUND) {
					retTime = triple.getTime();
				}
			}
		} else {
			return retTime;
		}
		return retTime;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

}
