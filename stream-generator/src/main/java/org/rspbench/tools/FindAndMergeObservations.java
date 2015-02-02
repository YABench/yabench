package org.rspbench.tools;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class FindAndMergeObservations {

    public static void main(String[] args) throws IOException {
        if (args.length > 2) {
            int numberOfStations = Integer.valueOf(args[2]);
            Path observationsFolder = new File(args[1]).toPath();
            List<Path> obsrvsToMerge = new CopyOnWriteArrayList<>();

            try (Stream<Path> stations = Files.list(new File(args[0]).toPath())) {
                stations.sorted().limit(numberOfStations).forEachOrdered((Path t) -> {
                    final String stationName = t.getFileName().toString().split("_", 2)[0];

                    System.out.println(stationName);

                    Stream<Path> observations;
                    try {
                        observations = Files.list(observationsFolder);

                        List<Path> stationObsrvs = observations.filter((Path obsrv) -> {
                            final String obsrvPrefix = obsrv.getFileName()
                                    .toString().split("_", 2)[0];

                            return obsrvPrefix.equals(stationName);
                        }).collect(Collectors.toList());

                        obsrvsToMerge.addAll(stationObsrvs);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
            }
            
            Model model = ModelFactory.createDefaultModel();
            OutputStream os = Files.newOutputStream(
                                new File("observations.n3").toPath(),
                                StandardOpenOption.CREATE);
            
            obsrvsToMerge.stream().forEach((p) -> {
                RDFDataMgr.read(model, p.toAbsolutePath().toString(), Lang.N3);
            });
            
            model.write(os, Lang.TURTLE.getName());
            model.close();
        } else {
            System.out.println("Not enough arguments!");
        }
    }

}
