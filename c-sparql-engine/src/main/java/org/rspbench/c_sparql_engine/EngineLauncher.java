package org.rspbench.c_sparql_engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.rspbench.tester.RDFStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Statement;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.ConsoleFormatter;
import eu.larkc.csparql.core.engine.CsparqlEngine;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class EngineLauncher {

	// C:\Users\peter\Documents\yabench\c-sparql-engine>java -jar
	// .\target\c-sparql-engine-0.0.1-SNAPSHOT-jar-with-dependencies.jar -query
	// asdf -dest asdfdest -source ..\streams\TestQ1

	private static final String PROGRAM_NAME = "c-sparql-engine streamer";
	private static final String ARG_QUERY = "query";
	private static final String ARG_SOURCE = "source";
	private static final String ARG_DEST = "dest";
	private static final String ARG_HELP = "help";
	private static final String ARG_WSIZE = "wsize";
	private static final String ARG_WSLIDE = "wslide";
	private static final String PARAM_WSIZE = "$SIZE$";
	private static final String PARAM_WSLIDE = "$SLIDE$";

	private static CsparqlEngine engine;

	private final static transient Logger log = LoggerFactory.getLogger(EngineLauncher.class);

	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();
		Options options = createCLIOptions();

		CsparqlQueryResultProxy csparqlProxy;
		String query = null;
		RdfStream stream = null;

		try {
			CommandLine cli = parser.parse(options, args);

			if (cli.hasOption(ARG_QUERY) &&
					cli.hasOption(ARG_SOURCE) &&
					cli.hasOption(ARG_DEST) &&
					cli.hasOption(ARG_WSIZE) &&
					cli.hasOption(ARG_WSLIDE)) {

				// 1. read query file and replace paremeters, i.e., window-size and window-slide
				query = readQueryFile(cli.getOptionValue(ARG_QUERY));
				query = query.replace(PARAM_WSIZE, cli.getOptionValue(ARG_WSIZE));
				query = query.replace(PARAM_WSLIDE, cli.getOptionValue(ARG_WSLIDE));

				// 2. read quads from source file
				Path source = new File(cli.getOptionValue(ARG_SOURCE)).toPath();
				RDFStreamReader reader = new RDFStreamReader(source);

				// quad counter for output
				int i = 1;
				// needed for interval calcualation inbetween streaming quads
				long oldTime, time = 0;

				// 4. Initialize C-SPARQL Engine
				log.info("initialize engine");
				engine = new CsparqlEngineImpl();
				engine.initialize();

				// 5. Register RDF Stream
				log.info("register stream");
				stream = new RdfStream("http://ex.org/streams/test");
				engine.registerStream(stream);

				// Register a C-SPARQL query
				log.info("register query");
				csparqlProxy = engine.registerQuery(query);
				long l1 = System.currentTimeMillis();
				if (csparqlProxy != null) {
					csparqlProxy.addObserver(new ConsoleFormatter());
					// c1.addObserver(new JsonFormatter(stepValue,
					// testIdentifier, jsonOutputDir));
				}

				// stream quads
				while (reader.hasNext()) {
					oldTime = time;
					time = reader.nextTime();
					final Statement stmt = reader.nextStatement();
					Thread.sleep(time - oldTime);

					stream.put(new RdfQuadruple(stmt.getSubject().toString(), stmt.getPredicate().toString(), stmt.getObject().toString(),
							System.currentTimeMillis()));
					long l2 = System.currentTimeMillis() - l1;
					System.out.println(String.valueOf(i) +
							": streaming statement...\nsubject: " +
							stmt.getSubject().toString() +
							"\nPredicate: " +
							stmt.getPredicate().toString() +
							"\nObject: " +
							stmt.getObject().toString() +
							"\n\n");
					
					System.out.println("time between: "+String.valueOf(l2));


					i++;
				}

				engine.unregisterQuery(csparqlProxy.getId());
				engine.unregisterStream(stream.getIRI());

			} else {
				printHelp(options);

			}
		} catch (ParseException exp) {
			System.out.println(exp.getMessage() + "\n");
			printHelp(options);
		} catch (Exception iex) {
			iex.printStackTrace();
		}
	}

	/**
	 * @param queryfile
	 * @return
	 * @throws IOException
	 */
	private static String readQueryFile(String queryfile) throws IOException {
		FileInputStream inputStream = new FileInputStream(queryfile);
		try {
			String query = IOUtils.toString(inputStream);
			return query;
		} finally {
			inputStream.close();
		}

	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(PROGRAM_NAME, options);
	}

	private static Options createCLIOptions() {
		Options opts = new Options();

		Option query = OptionBuilder.withArgName("query")
				.withDescription("destination file containing the query to be registered at the engine").hasArg().isRequired()
				.create(ARG_QUERY);

		Option dest = OptionBuilder.withArgName("dest").withDescription("destination folder of query results").hasArg().isRequired()
				.create(ARG_DEST);

		Option source = OptionBuilder.withArgName("source").withDescription("source file containing the triples to be streamed").hasArg()
				.isRequired().create(ARG_SOURCE);

		Option wsize = OptionBuilder.withArgName("wsize").withDescription("window size of the query in seconds").hasArg().isRequired()
				.create(ARG_WSIZE);

		Option wslide = OptionBuilder.withArgName("wslide").withDescription("window slide of the query in seconds").hasArg().isRequired()
				.create(ARG_WSLIDE);

		Option help = OptionBuilder.withDescription("print this message").create(ARG_HELP);

		opts.addOption(query);
		opts.addOption(source);
		opts.addOption(dest);
		opts.addOption(help);
		opts.addOption(wslide);
		opts.addOption(wsize);

		return opts;
	}
}
