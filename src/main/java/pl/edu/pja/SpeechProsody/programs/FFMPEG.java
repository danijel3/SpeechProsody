package pl.edu.pja.SpeechProsody.programs;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pja.SpeechProsody.utils.ProgramLauncher;
import pl.edu.pja.SpeechProsody.utils.ProgramPaths;

public class FFMPEG {

    private final static Logger logger = LoggerFactory.getLogger(FFMPEG.class);

    public static void convertTo16k(File input, File output) throws RuntimeException {

        String[] cmd = new String[]{ProgramPaths.ffmpeg_bin,
                "-i", input.getAbsolutePath(), "-acodec", "pcm_s16le", "-ac",
                "1", "-ar", "16k", output.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        logger.trace("FFMPEG: " + input.getAbsolutePath() + " -> "
                + output.getAbsolutePath());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("FFMPEG Retval: " + launcher.getReturnValue());
    }

}
