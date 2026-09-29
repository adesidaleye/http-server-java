package com.adesidaleye.httpserver.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;

public class WebRootHandler {
    private File webRoot;

    public WebRootHandler(String webRootPath) throws WebRootNotFoundException {
        webRoot = new File(webRootPath);

        if (!webRoot.exists() || !webRoot.isDirectory()) {
            throw new WebRootNotFoundException("Webroot provided does not exist or is not a folder");
        }
    }

    public String getFileMimeType(String relativePath) throws FileNotFoundException {
        if (checkIfEndsWithSlash(relativePath)) {
            relativePath += "index.html"; // By default, serve the index.html, if it exists.
        }

        File file = new File(webRoot, relativePath);

        if (!checkIfProvidedRelativePathExists(relativePath)) {
            throw new FileNotFoundException("File not found: " + relativePath);
        }

        // returns MIME type from the file extension (.html -> text/html, etc)
        String mimeType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());

        // unknown extension, fall back to a generic binary type
        if (mimeType == null) {
            return "application/octet-stream";
        }

        return mimeType;
    }

    // reads the actual file contents to send as the response body
    public byte[] getFileByteArrayData(String relativePath) throws FileNotFoundException, ReadFileException {
        File file = resolveFile(relativePath);

        try (FileInputStream in = new FileInputStream(file)) {
            return in.readAllBytes(); // reads the whole file into memory in one call
        } catch (IOException e) {
            throw new ReadFileException(e);
        }
    }

    // HELPERS

    /**
     * Resolves a request path to a File inside the web root.
     * - If the path ends with "/", appends "index.html".
     * - Throws FileNotFoundException if the resolved file doesn't exist.
     * - Rejects paths that escape the web root (path traversal protection).
     */
    private File resolveFile(String relativePath) throws FileNotFoundException {
        if (relativePath == null || relativePath.isEmpty()) {
            throw new FileNotFoundException("Empty path");
        }

        if (checkIfEndsWithSlash(relativePath)) {
            relativePath += "index.html";
        }

        File file = new File(webRoot, relativePath);

        if (!checkIfProvidedRelativePathExists(relativePath)) {
            throw new FileNotFoundException("File not found: " + relativePath);
        }

        return file;
    }

    private boolean checkIfEndsWithSlash(String relativePath) {
        return relativePath.endsWith("/");
    }

    /**
     * Returns true if the file exists AND its canonical path stays inside the web root.
     * This prevents "../" attacks like /../../etc/passwd.
     */
    private boolean checkIfProvidedRelativePathExists(String relativePath) {
        File file = new File(webRoot, relativePath);

        try {
            // getCanonicalPath() resolves ".." and "." segments into the real, absolute path
            String canonicalPath = file.getCanonicalPath();
            String canonicalRoot = webRoot.getCanonicalPath();

            if (!canonicalPath.startsWith(canonicalRoot + File.separator)) {
                return false; // attempted traversal outside web root
            }

            return file.exists() && file.isFile();
        } catch (IOException e) {
            return false;
        }
    }
}
