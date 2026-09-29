package com.adesidaleye.httpserver.core.io;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebRootHandlerTest {
    WebRootHandler webRootHandler;

    private Method checkIfEndsWithSlashMethod;

    private Method checkIfProvidedRelativePathExistsMethod;

    @BeforeAll
    public void beforeAll() throws NoSuchMethodException, WebRootNotFoundException {
        webRootHandler = new WebRootHandler("webRoot");

        Class<WebRootHandler> cls = WebRootHandler.class;
        checkIfEndsWithSlashMethod = cls.getDeclaredMethod("checkIfEndsWithSlash", String.class);
        checkIfEndsWithSlashMethod.setAccessible(true);

        checkIfProvidedRelativePathExistsMethod = cls.getDeclaredMethod("checkIfProvidedRelativePathExists", String.class);
        checkIfProvidedRelativePathExistsMethod.setAccessible(true);
    }

    @Test
    void constructorPathExistsAbsolute() {
        try {
            WebRootHandler webRootHandler = new WebRootHandler("C:\\Users\\HomePC\\Desktop\\Code\\java-projects\\http-server\\webRoot");
        } catch (WebRootNotFoundException e) {
            fail(e);
        }
    }

    @Test
    void constructorPathNotExistsAbsolute() {
        try {
            WebRootHandler webRootHandler = new WebRootHandler("C:\\Users\\HomePC\\Desktop\\Code\\java-projects\\http-server\\webRoot2");
            fail();
        } catch (WebRootNotFoundException e) {}
    }

    @Test
    void constructorPathExistsRelative() {
        try {
            WebRootHandler webRootHandler = new WebRootHandler("webRoot");
        } catch (WebRootNotFoundException e) {
            fail(e);
        }
    }

    @Test
    void constructorPathNotExistsRelative() {
        try {
            WebRootHandler webRootHandler = new WebRootHandler("webRoot2");
            fail();
        } catch (WebRootNotFoundException e) {}
    }

    @Test
    void checkIfEndsWithSlashMethodFalse1() {
        try {
            boolean result = (boolean) checkIfEndsWithSlashMethod.invoke(webRootHandler,"index.html");
            assertFalse(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail(e);
        }
    }

    @Test
    void checkIfEndsWithSlashMethodFalse2() {
        try {
            boolean result = (boolean) checkIfEndsWithSlashMethod.invoke(webRootHandler,"/index.html");
            assertFalse(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail(e);
        }
    }

    @Test
    void checkIfEndsWithSlashMethodFalse3() {
        try {
            boolean result = (boolean) checkIfEndsWithSlashMethod.invoke(webRootHandler,"/private/index.html");
            assertFalse(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail(e);
        }
    }

    @Test
    void checkIfEndsWithSlashMethodTrue1() {
        try {
            boolean result = (boolean) checkIfEndsWithSlashMethod.invoke(webRootHandler,"/");
            assertTrue(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail(e);
        }
    }

    @Test
    void checkIfEndsWithSlashMethodTrue2() {
        try {
            boolean result = (boolean) checkIfEndsWithSlashMethod.invoke(webRootHandler,"/private/");
            assertTrue(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail(e);
        }
    }

    @Test
    void testWebRootFilePathExistsGoodRelative() {
        try {
            boolean result = (boolean) checkIfProvidedRelativePathExistsMethod.invoke(webRootHandler,"/index.html");
            assertTrue(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail(e);
        }
    }

    @Test
    void testWebRootFilePathExists() {
        try {
            boolean result = (boolean) checkIfProvidedRelativePathExistsMethod.invoke(webRootHandler,"/./././index.html");
            assertTrue(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail(e);
        }
    }

    @Test
    void testWebRootFilePathExistsDoesNotExist() {
        try {
            boolean result = (boolean) checkIfProvidedRelativePathExistsMethod.invoke(webRootHandler,"/indexNotHere.html");
            assertFalse(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail(e);
        }
    }

    @Test
    void testWebRootFilePathExistsInvalid() {
        try {
            boolean result = (boolean) checkIfProvidedRelativePathExistsMethod.invoke(webRootHandler,"/../Request.txt");
            assertFalse(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail(e);
        }
    }

    @Test
    void testGetFileMimeTypeText() {
        try {
            String mimeType = webRootHandler.getFileMimeType("/");
            assertEquals("text/html", mimeType);
        } catch (FileNotFoundException e) {
            fail(e);
        }
    }

    @Test
    void testGetFileMimeTypePng() {
        try {
            String mimeType = webRootHandler.getFileMimeType("/logo.png");
            assertEquals("image/png", mimeType);
        } catch (FileNotFoundException e) {
            fail(e);
        }
    }

    @Test
    void testGetFileMimeTypeDefault() {
        try {
            String mimeType = webRootHandler.getFileMimeType("/favicon.ico");
            assertEquals("application/octet-stream", mimeType);
        } catch (FileNotFoundException e) {
            fail(e);
        }
    }

    @Test
    void testGetFileByteArrayData() {
        try {
            assertTrue(webRootHandler.getFileByteArrayData("/").length > 0);
        } catch (FileNotFoundException | ReadFileException e) {
            fail(e);
        }
    }

    @Test
    void testGetFileByteArrayDataFileNotPresent() {
        try {
            webRootHandler.getFileByteArrayData("/test.html");
            fail();
        } catch (FileNotFoundException e) {}
        catch (ReadFileException e) {
            fail(e);
        }
    }
}