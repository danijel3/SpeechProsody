package pl.edu.pja.SpeechProsody.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pja.SpeechProsody.utils.ProgramLauncher;
import pl.edu.pja.SpeechProsody.utils.ProgramPaths;

import java.io.*;
import java.util.Vector;

public class Momel {

    public static class Point {
        public double time;
        public double frequency;
    }

    final static Logger logger = LoggerFactory.getLogger(Momel.class);

    /**
     * Computes MOMEL parameters given a sequence of pitches.
     *
     * @param pitches a sequence of pitch values (100 per second)
     * @param win1    cible window length
     * @param lo      F0 threshold
     * @param hi      F0 ceiling
     * @param maxerr  maximum error
     * @param win2    reduc window length
     * @param mind    minimal distance
     * @param minr    minimal frequency ratio
     * @return sequence of MOMEL points
     */
    public static Vector<Point> momel(Vector<Double> pitches, int win1, double lo, double hi, double maxerr, int win2, double mind, double minr) {

        String[] cmd = new String[]{ProgramPaths.momel_bin, "" + win1, "" + lo, "" + hi, "" + maxerr, "" + win2, "" + mind, "" + minr};

        String input_values = "";
        for (Double pitch : pitches) {
            input_values += pitch + "\n";
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(input_values.getBytes());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        launcher.setStdinStream(bis);
        launcher.setStdoutStream(bos);

        logger.trace("Computing MOMEL targets...");
        launcher.run();
        logger.trace("Done.");

        Vector<Point> ret = new Vector<Point>();
        String output_values = bos.toString();
        for (String val : output_values.split("\n")) {
            String[] tok = val.trim().split("\\s+");
            if (tok.length != 2) {
                logger.error("Error parsing MOMEL output: " + val);
                continue;
            }
            Point pt = new Point();
            pt.time = Double.parseDouble(tok[0]);
            pt.frequency = Double.parseDouble(tok[1]);
            ret.add(pt);
        }
        return ret;
    }

    /**
     * Computes MOMEL parameters given a sequence of pitches. Uses default recommended parameters.
     *
     * @param pitches a sequence of pitch values (100 per second)
     * @return sequence of MOMEL points
     */
    public static Vector<Point> momel(Vector<Double> pitches) {
        return momel(pitches, 30, 60, 750, 1.04, 20, 5, 0.05);
    }

    /**
     * Unit test...
     */
    public static void test() {
        try (
                BufferedReader reader = new BufferedReader(new FileReader(new File("/home/guest/.praat-dir/plugin_momel-intsint/analysis/temp/extract_1.hz")));
        ) {
            String line;
            Vector<Double> f0 = new Vector<Double>();
            while ((line = reader.readLine()) != null) {
                f0.add(Double.parseDouble(line));
            }

            Vector<Momel.Point> momels = Momel.momel(f0);
            for (Momel.Point point : momels) {
                System.out.println(point.frequency + " Hz @ " + point.time + " ms");
            }

        } catch (Exception e) {
            logger.error("main error", e);
        }
    }
}
