package com.tesco.pma.util;

import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.io.IOError;

@UtilityClass
public class PathUtils {

    /**
     * Converts a path string, or a sequence of strings that when joined form a path string, to a Path
     * that is this path with redundant name elements eliminated and represented the absolute path.
     * If this path is already absolute then this method simply returns this path. Otherwise, this method resolves the path
     * in an implementation dependent manner,
     * typically by resolving the path against a file system default directory.
     * Depending on the implementation, this method may throw an I/O error if the file system is not accessible.
     *
     * @param first - the path string or initial part of the path string
     * @param more - additional strings to be joined to form the path string
     * @return the resulting Path
     * @throws InvalidPathException - if the path string cannot be converted to a Path
     * @throws IOError - if an I/O error occurs
     * @throws SecurityException - in the case of the default provider, a security manager is installed, and this path is not absolute,
     * then the security manager's checkPropertyAccess method is invoked to check access to the system property user.dir
     */
    public Path get(String first, String... more) {
        return Paths.get(first, more).normalize().toAbsolutePath();
    }

    /**
     * Resolve the path by specified name
     *
     * @param fileName    represents the path string with file name to resolve against path specified in location
     * @param destination represents the destination location path for resolving fileName against it
     * @return resolved path representation
     */
    public Path resolve(String fileName, Path destination) {
        return destination.resolve(Paths.get(fileName));
    }
}