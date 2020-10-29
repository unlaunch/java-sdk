package io.unlaunch.event;

import io.unlaunch.AccountDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

class SdkVersion {

    public static final String VERSION_FROM_MANIFEST = readSdkVersionFromManifest();

    private static final Logger logger = LoggerFactory.getLogger(SdkVersion.class);

    private static String readSdkVersionFromManifest() {
        Class<?> clazz = AccountDetails.class;
        String classPath = clazz.getResource(clazz.getSimpleName() + ".class").toString();
        if (!classPath.startsWith("jar")) {
            return "unknown-no-jar";
        }

        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        Manifest manifest = null;
        try {
            manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            String value = attr.getValue("Implementation-Version");
            return value;
        } catch (IOException e) {
            logger.warn("Unable to determine Unlaunch SDK version", e);
            return "unknown";
        }
    }
}
