package pl.edu.pja.SpeechProsody.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pja.SpeechProsody.utils.ProgramLauncher;
import pl.edu.pja.SpeechProsody.utils.ProgramPaths;

import java.io.*;
import java.util.Vector;

public class Intsint {

    private final static Logger logger = LoggerFactory.getLogger(Intsint.class);

    public static class Tone {
        double time;
        char code;
        double f0;
        double estimate;
    }

    public static class Result {
        public double key;
        public double range;
        public Vector<Tone> tones = new Vector<Tone>();
    }

    public static Result intsint(Vector<Momel.Point> points, File tmpdir) throws IOException {

        File momel_file = new File(tmpdir, "tmp.momel");
        File intsint_file = new File(tmpdir, "tmp.intsint");

        if (momel_file.exists() || intsint_file.exists()) {
            logger.error("Temp directory isn't empty of momel/intsint files! Deleting them!");
            momel_file.delete();
            intsint_file.delete();
        }

        try (
                PrintWriter writer = new PrintWriter(momel_file)
        ) {
            for (Momel.Point point : points) {
                writer.println(point.time + " " + point.frequency);
            }
        } catch (IOException e) {
            throw (e);
        }

        String[] cmd = new String[]{ProgramPaths.perl_bin, ProgramPaths.intsint_script.getAbsolutePath(), momel_file.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        logger.trace("Calculating INTSINT codes...");
        launcher.run();
        logger.trace("Done.");

        Result ret = new Result();
        try (
                BufferedReader reader = new BufferedReader(new FileReader(intsint_file))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(";")) continue;

                if (line.startsWith("<parameter range=")) {
                    ret.range = Double.parseDouble(line.substring(17, line.length() - 1));
                    continue;
                }
                if (line.startsWith("<parameter key=")) {
                    ret.key = Double.parseDouble(line.substring(15, line.length() - 1));
                    continue;
                }

                String[] tok = line.split("\\s+");
                if (tok.length != 4) {
                    logger.error("Cannot parse INTSINT line: " + line);
                    continue;
                }

                Tone tone = new Tone();
                tone.time = Double.parseDouble(tok[0]);
                tone.code = tok[1].trim().charAt(0);
                tone.f0 = Double.parseDouble(tok[2]);
                tone.estimate = Double.parseDouble(tok[3]);

                ret.tones.add(tone);
            }
        }

        momel_file.delete();
        intsint_file.delete();

        return ret;
    }

    public static void test() {
        try (
                BufferedReader reader = new BufferedReader(new FileReader(new File("/home/guest/.praat-dir/plugin_momel-intsint/analysis/temp/extract_1.momel")));
        ) {
            String line;
            Vector<Momel.Point> points = new Vector<>();
            while ((line = reader.readLine()) != null) {
                String[] tok = line.split("\\s+");
                Momel.Point point = new Momel.Point();
                point.time = Double.parseDouble(tok[0]);
                point.frequency = Double.parseDouble(tok[1]);
                points.add(point);
            }

            Result ret = intsint(points, new File("."));
            System.out.println("Key: " + ret.key + " Hz");
            System.out.println("Range: " + ret.range + " octaves");
            for (Tone tone : ret.tones) {
                System.out.println(tone.time + "s " + tone.code + " @ " + tone.f0 + " Hz or ~" + tone.estimate + " Hz");
            }

        } catch (Exception e) {
            logger.error("main error", e);
        }
    }

}
