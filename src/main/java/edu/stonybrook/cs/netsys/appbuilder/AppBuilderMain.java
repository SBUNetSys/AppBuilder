package edu.stonybrook.cs.netsys.appbuilder;

import edu.stonybrook.cs.netsys.appbuilder.appInfo.App;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qqcao on 11/24/16 Thursday.
 * <p>
 * Wear app building process:
 * <p>
 * 1. receive zip file from UIWear phone proxy, unzip file and validate preferences and mapping rules (currently skip)
 * 2. process mapping rules for generating xml layout (if using UIWear provided view types,like Card etc)
 * 3. process generated xml layout files from step 2 or user provided customize xml layout files:
 * fill default values in the layout, e.g., initial text for TextView, initial image for ImageView etc.
 * 4. Using UIWear rendering protocol to generate arrays.xml by parsing preference file name, extracting the view id
 *
 * Rendering protocol:
 * generally there are three sections for the arrays.xml :
 * 1. The first section contains preference id array and wear app layout array, these arrays in place in strict order,
 * since how UIWear find the right layout for inflating and rendering is based on the order(or index of the array).
 * 2. Second section is for general view types like card and customize view, except for list-like view types. In this
 * section, UIWear indexes all view ids(phone and wear) for the view of each preference.
 *
 * 3. Third section is specifically designed for list-like view like WearableListView, RecyclerView, ListView etc. The
 * different part to section 2 is that for list-like view types, besides these view themselves, they need view for their
 * items. Each item shares the same (or varies a little) style. In addition to the section 2 view id indexation, this
 * section also indexes item view ids and relate them to their parent, i.e., list-like views.
 *
 */
public class AppBuilderMain {
    public static final String TEMPLATE_FOLDER_NAME = "uiwear/template";
    public static final String MANIFEST_NAME = "AndroidManifest.xml.ftl";
    public static final String BUILD_GRADLE_NAME = "build.gradle.ftl";
    public static final String ACTIVITY_NAME = "MainActivity.java.ftl";
    public static final String STRINGS_NAME = "strings.xml.ftl";

    private static String templateLocation;

    private static Configuration configuration;

    static {
        new AppBuilderMain();
        try {
            configuration = FreeMarker.initialize(new File(templateLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AppBuilderMain() {
        URL url = getClass().getClassLoader().getResource(TEMPLATE_FOLDER_NAME);
        assert url != null;
        templateLocation = url.getFile();
        System.out.println(templateLocation);
    }

    public static void main(String[] args) throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();

        App wearApp = new App();
        wearApp.setAppName("Spotify");
        wearApp.setAppPkgName("com.spotify.music");
        root.put("app", wearApp);

        Template temp = configuration.getTemplate(STRINGS_NAME);
        Writer out = new OutputStreamWriter(System.out);
        temp.process(root, out);
    }
}
