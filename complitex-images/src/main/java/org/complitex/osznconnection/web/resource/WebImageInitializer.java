package org.complitex.osznconnection.web.resource;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.apache.wicket.SharedResources;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.util.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Artem
 */
public final class WebImageInitializer implements IInitializer {

    private static final Logger log = LoggerFactory.getLogger(WebImageInitializer.class);
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "gif", "bmp", "png");
    private static final String IMAGES_DIRECTORY_NAME = "images";

    @Override
    public void init(Application application) {
        try {
            SharedResources sharedResources = application.getSharedResources();
            URI imagesURI = getClass().getResource(IMAGES_DIRECTORY_NAME).toURI();
            File images = new File(imagesURI);
            if (!images.exists()) {
                throw new RuntimeException("Directory " + images.getAbsolutePath() + " doesn't exist.");
            }
            if (!images.isDirectory()) {
                throw new RuntimeException("File " + images.getAbsolutePath() + " is not directory.");
            }

            FilenameFilter imageFilter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return IMAGE_EXTENSIONS.contains(Files.extension(name));
                }
            };

            for (File image : images.listFiles(imageFilter)) {
                String relatedPath = IMAGES_DIRECTORY_NAME + "/" + image.getName();
                //Now resource name is equal to physical related path but it is not required and may will be changed in future.
                String resourceName = relatedPath;
                sharedResources.add(resourceName, PackageResource.get(getClass(), relatedPath));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
