package pl.edu.pja.SpeechProsody.programs;

import pl.edu.pja.SpeechProsody.utils.ProgramLauncher;

import java.io.ByteArrayOutputStream;

public class Which {

    public static String which(String program) {

        String[] cmd = new String[]{"which",program};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        launcher.setStdoutStream(bos);

        launcher.run();

        if(launcher.getReturnValue()!=0)
            return null;

        return bos.toString();
    }
}

