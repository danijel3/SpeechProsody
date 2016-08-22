package pl.edu.pja.SpeechProsody.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pja.SpeechProsody.programs.Which;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Properties;

public class ProgramPaths {

    /*******************************************************************************************************************/
    public static String perl_bin = "perl";
    public static String praat_bin = "/home/guest/apps/praat";
    public static String sox_bin = "sox";
    public static String ffmpeg_bin = "ffmpeg";
    public static String momel_bin = "/home/guest/.praat-dir/plugin_momel-intsint/analysis/momel_linux";
    public static File intsint_script = new File("/home/guest/.praat-dir/plugin_momel-intsint/analysis/intsint.pl");
    /*******************************************************************************************************************/

    final static Logger logger = LoggerFactory.getLogger(ProgramPaths.class);

    /**
     * Uses 'which' program to check for paths of programs.
     *
     * @return true if no programs are missng, false if any is missing.
     */
    public static boolean check() {

        if (!System.getProperty("os.name").toLowerCase().equals("linux")) {
            logger.warn("Cannot check program presence under this operating system!");
            return true;
        }

        boolean ret = true;

        Field[] fields = ProgramPaths.class.getFields();

        for (Field field : fields) {
            int mods = field.getModifiers();
            if (!Modifier.isStatic(mods) || !Modifier.isPublic(mods))
                continue;

            if (field.getType() == File.class) {
                try {
                    String name = field.getName();
                    File file = (File) field.get(null);

                    if (!file.exists() || !file.canRead()) {
                        logger.warn("File " + name + " couldn't be found under: " + file.getAbsolutePath());
                        ret = false;
                    }
                } catch (IllegalAccessException e) {
                    logger.error("internal error", e);
                }
            }

            if (field.getType() != String.class)
                continue;

            try {
                String name = field.getName();
                String program = (String) field.get(null);

                String which = Which.which(program);

                if (which == null || which.trim().isEmpty()) {
                    logger.warn("Program " + name + " couldn't be found under: " + program);
                    ret = false;
                } else {
                    which = which.trim();
                    logger.trace("Using '" + which + "' for program " + name);
                }

            } catch (IllegalAccessException e) {
                logger.error("internal error", e);
            }
        }

        return ret;
    }

    public static void saveToFile(File file) throws IOException {

        Properties props = new Properties();

        Field[] fields = ProgramPaths.class.getFields();

        for (Field field : fields) {
            int mods = field.getModifiers();
            if (!Modifier.isStatic(mods) || !Modifier.isPublic(mods))
                continue;

            try {
                String value;
                if (field.getType() == String.class)
                    value = (String) field.get(null);
                else if (field.getType() == File.class)
                    value = ((File) field.get(null)).getPath();
                else
                    continue;

                props.setProperty(field.getName(), value);
            } catch (IllegalAccessException e) {
                logger.error("internal error", e);
            }
        }

        props.store(new FileOutputStream(file), "SpeechProsody generated paths");
    }

    public static void loadFromFile(File file) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream((file)));

        for (Map.Entry<Object, Object> entry : props.entrySet()) {

            String name = (String) entry.getKey();
            String val = (String) entry.getValue();

            try {
                Field field = ProgramPaths.class.getField(name);
                if (field.getType() == String.class)
                    field.set(null, val);
                else if (field.getType() == File.class)
                    field.set(null, new File(val));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                logger.error("Cannot set value for " + name, e);
            }
        }
    }
}
