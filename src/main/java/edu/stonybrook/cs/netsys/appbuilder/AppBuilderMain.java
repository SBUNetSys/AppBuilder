package edu.stonybrook.cs.netsys.appbuilder;

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

import edu.stonybrook.cs.netsys.appbuilder.data.App;

import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ACTIVITY_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.BUILD_GRADLE_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.MANIFEST_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.STRINGS_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.TEMPLATE_FOLDER_NAME;

/**
 * Created by qqcao on 11/24/16 Thursday. <p> Wear app building process: <p>
 * 1. receive zip file from UIWear phone proxy, unzip file and validate preferences
 * and mapping rules (currently skip)
 * 2. process project files, pre-process mapping rules for generating xml layout (if using UIWear
 * provided view types, like Card etc)
 * 3. process generated xml layout files from step 2 or user provided customize xml layout
 * files: fill default values in the layout, e.g., initial text for TextView, initial image for
 * ImageView etc.
 * 4. use UIWear rendering protocol to generate arrays.xml by parsing preference file name,
 * extracting the view id.
 * 5. put all together and generate app project files, copy to WearAppEnv project, call gradle
 * build scripts to build apk, send back to phone proxy.
 *
 * Rendering protocol: generally there are three sections for the arrays.xml :
 * 1. The first section contains preference id array and wear app layout array,
 * these arrays in place in strict order, since how UIWear find the right layout for inflating
 * and rendering is based on the order(or index of the array).
 * 2. Second section is for general view types like card and customize view, except
 * for list-like view types. In this section, UIWear indexes all view ids(phone and wear) for the
 * view of each preference.
 * 3. Third section is specifically designed for list-like view like
 * WearableListView, RecyclerView, ListView etc. The different part to section 2 is that for
 * list-like view types, besides these view themselves, they need view for their items. Each item
 * shares the same (or varies a little) style. In addition to the section 2 view id indexation, this
 * section also indexes item view ids and relate them to their parent, i.e., list-like views.
 */
public class AppBuilderMain {
    private static final String[] TEMPLATES = new String[]{MANIFEST_NAME, BUILD_GRADLE_NAME,
            ACTIVITY_NAME, STRINGS_NAME};

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
        // step 0, open socket listening for new wear app building request
        // step 1, new thread handle received file, pkgName.zip, unzip and put to temp folder
        // step 2, start parsing below code starts here

        // generate project files based on templates
        Map<String, Object> root = new HashMap<>();
        App wearApp = new App();
        wearApp.setAppName("Spotify");
        wearApp.setAppPkgName("com.spotify.music");
        root.put("app", wearApp);

        for (String templateName : TEMPLATES) {
            Template temp = configuration.getTemplate(templateName);
            Writer out = new OutputStreamWriter(System.out);
            temp.process(root, out);
        }

        // pre-process mapping rules for generating xml layout
        // TODO: 11/25/16 parse card layout, skip here

        //
    }
}
