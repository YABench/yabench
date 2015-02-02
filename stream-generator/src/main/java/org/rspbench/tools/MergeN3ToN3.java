package org.rspbench.tools;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class MergeN3ToN3 {

    private static final String OUTPUT_PREFIX = "output_";
    private static final String OUTPUT_POSTFIX = ".n3";
    private static int numberOfFilePerGroup = 0;
    private static int currentFileNumber = 0;
    private static int fileFlag = 0;
    private static OutputStream os;
    private static Model model;

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {

            if (args.length > 1) {
                numberOfFilePerGroup = Integer.parseInt(args[1]);
            }

            os = Files.newOutputStream(
                    new File(OUTPUT_PREFIX + fileFlag + OUTPUT_POSTFIX).toPath(),
                    StandardOpenOption.CREATE);
            
            model = ModelFactory.createDefaultModel();

            Files.walkFileTree(new File(args[0]).toPath(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
                    System.out.println("Pre visit Dir: " + t.toAbsolutePath());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path t, BasicFileAttributes bfa) throws IOException {
                    RDFDataMgr.read(model, t.toAbsolutePath().toString(), Lang.N3);
                    
                    System.out.println(currentFileNumber);
                    
                    if (numberOfFilePerGroup > 0 && numberOfFilePerGroup <= ++currentFileNumber) {
                        currentFileNumber = 0;
                        
                        model.write(os, "TURTLE");
                        model.close();
                        
                        model = ModelFactory.createDefaultModel();
                        
                        os.flush();
                        os.close();
                        
                        ++fileFlag;
                        
                        os = Files.newOutputStream(
                                new File(OUTPUT_PREFIX + fileFlag + OUTPUT_POSTFIX).toPath(),
                                StandardOpenOption.CREATE);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
                    System.out.println("File failed: " + t.toAbsolutePath());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
                    System.out.println("Post visit Dir: " + t.toAbsolutePath());
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            System.out.println("Please, provide a path to folder with N3 files!");
        }
    }
}
