package edu.stonybrook.cs.netsys.appbuilder;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.IOException;

/**
 * Created by qqcao on 11/24/16Thursday.
 * <p>
 * Dealing with freemarker configurations and other global states
 */
public class FreeMarker {
    private static Configuration sInstance;

    public static synchronized Configuration initialize(File templateLocation) throws IOException {
        if (sInstance == null) {
            sInstance = new Configuration(Configuration.VERSION_2_3_25);
            sInstance.setDirectoryForTemplateLoading(templateLocation);
            sInstance.setDefaultEncoding("UTF-8");
            sInstance.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            sInstance.setLogTemplateExceptions(false);
        }
        return sInstance;
    }
}
