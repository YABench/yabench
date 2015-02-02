package org.rspbench.tools;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public abstract class DefaultFileVisitor<T> implements FileVisitor<T>{

    @Override
    public FileVisitResult preVisitDirectory(T t, BasicFileAttributes bfa) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(T t, IOException ioe) throws IOException {
        ioe.printStackTrace();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(T t, IOException ioe) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
}
