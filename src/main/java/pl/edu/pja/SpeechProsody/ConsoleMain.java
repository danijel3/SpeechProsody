package pl.edu.pja.SpeechProsody;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pja.SpeechProsody.programs.Intsint;
import pl.edu.pja.SpeechProsody.programs.Momel;
import pl.edu.pja.SpeechProsody.programs.Praat;
import pl.edu.pja.SpeechProsody.utils.ProgramPaths;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class ConsoleMain {

    @Parameter(names = "-i", description = "Input WAV file.", required = true)
    private String input_path;

    @Parameter(names = "-o", description = "Output JSON file.", required = true)
    private String output_path;

    @Parameter(names = "-t", description = "Path to tmp directory.")
    private String tmp_dir_path = "./";

    @Parameter(names = "-h", description = "This help.", help = true)
    private boolean help = false;

    @Parameter(names = "-d", description = "Print debug of intermediary steps.")
    private boolean debug = false;

    final static Logger logger = LoggerFactory.getLogger(ConsoleMain.class);

    public static void main(String[] args) {
        try {
            ConsoleMain main = new ConsoleMain();
            JCommander parse = new JCommander(main, args);
            if (main.help) {
                parse.usage();
                return;
            }
            main.run();
        } catch (ParameterException e) {
            System.out.println("Argument parsing error: " + e.getMessage());
            System.exit(1);
            return;
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void run() {

        File paths_file = new File("paths.conf");
        if (paths_file.exists()) {
            try {
                ProgramPaths.loadFromFile(new File("paths.conf"));
            } catch (IOException e) {
                return;
            }
        } else {
            try {
                ProgramPaths.saveToFile(new File("paths.conf"));
            } catch (IOException e) {
            }
        }

        if (!ProgramPaths.check()) {
            logger.error("Some programs couldn't be found! Exiting!");
            return;
        }

        logger.info("Starting SpeechProsody...");

        File tmp_dir = new File(tmp_dir_path);
        File wav_file = new File(input_path);
        File out_file = new File(output_path);

        try {

            Vector<Praat.PitchMark> pitchmarks = Praat.momel_pitch(wav_file, tmp_dir);

            if (debug) {
                for (Praat.PitchMark pitchmark : pitchmarks) {
                    System.out.println("PM> " + pitchmark.time + "s " + pitchmark.frequency + "Hz " + pitchmark.intensity + "dB " + pitchmark.strength + "Pa");
                }
            }

            Vector<Double> pitches = Praat.pitchmarks_to_pitchstream(pitchmarks);

            if (debug) {
                for (Double pitch : pitches) {
                    System.out.print(pitch + ", ");
                }
                System.out.println();
            }

            Vector<Momel.Point> momel_points = Momel.momel(pitches);

            if (debug) {
                for (Momel.Point point : momel_points) {
                    System.out.println("MP> " + point.time + "ms " + point.frequency + "Hz ");
                }
            }

            Intsint.Result result = Intsint.intsint(momel_points, tmp_dir);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();


            try (
                    PrintWriter writer = new PrintWriter(out_file)
            ) {

                writer.println(gson.toJson(result));

            } catch (IOException e) {
                throw e;
            }

        } catch (IOException e) {
            logger.error("Main error.", e);
        }
    }
}
