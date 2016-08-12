package pl.edu.pja.SpeechProsody.programs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pl.edu.pja.SpeechProsody.utils.ProgramLauncher;
import pl.edu.pja.SpeechProsody.utils.ProgramPaths;

public class Praat {

    public static class PitchMark {
        public double time;
        public double intensity;
        public double frequency;
        public double strength;
    }

    final static Logger logger = LoggerFactory.getLogger(Praat.class);

    public static Vector<PitchMark> pitch(File wav_file, File pitch_file,
                                          File pitch_wav) throws IOException {

        File script = new File("pitch.script");
        PrintWriter writer = new PrintWriter(script);

        writer.println("Read from file: \"" + wav_file.getAbsolutePath() + "\"");
        writer.println("To Pitch: 0, 75, 600");
        writer.println("Save as short text file: \""
                + pitch_file.getAbsolutePath() + "\"");
        writer.println("To Sound (hum)");
        writer.println("Save as WAV file: \"" + pitch_wav.getAbsolutePath()
                + "\"");

        writer.close();

        String[] cmd = new String[]{ProgramPaths.praat_bin,
                script.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        logger.trace("Running Praat to compute pitch...");
        launcher.run();
        logger.trace("Done.");

        return parseSimplePitchFile(pitch_file);
    }

    public static void savePitchMarksToXML(String audio_id,
                                           Vector<PitchMark> pitch, File xml)
            throws TransformerFactoryConfigurationError,
            ParserConfigurationException, TransformerException {

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();

        Element elRoot = doc.createElement("audio-segment");
        elRoot.setAttribute("id", audio_id);
        doc.appendChild(elRoot);

        for (PitchMark p : pitch) {
            Element elPitch = doc.createElement("pitch");
            elPitch.setAttribute("t", String.format("%2.3f", p.time));
            elPitch.setAttribute("i", String.format("%2.3f", p.intensity));
            elPitch.setAttribute("c", String.format("%2.3f", p.strength));
            elPitch.setTextContent(String.format("%2.3f", p.frequency));
            elRoot.appendChild(elPitch);
        }

        Transformer trans = TransformerFactory.newInstance().newTransformer();

        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                "4");
        trans.setOutputProperty(OutputKeys.METHOD, "xml");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        Source source = new DOMSource(doc);
        Result result = new StreamResult(xml);
        trans.transform(source, result);
    }

    public static Vector<PitchMark> parseSimplePitchFile(File pitch_file) throws IOException {

        Vector<PitchMark> ret = new Vector<PitchMark>();

        try (
                BufferedReader reader = new BufferedReader(new FileReader(pitch_file));
        ) {
            int num, numc;
            double dx, x1;

            String line;

            reader.readLine();// header line #1
            reader.readLine();// header line #2
            reader.readLine();// empty line
            reader.readLine();// min time
            reader.readLine();// max time

            line = reader.readLine();// marks num
            num = Integer.parseInt(line);

            line = reader.readLine();// dx
            dx = Double.parseDouble(line);

            line = reader.readLine();// x1
            x1 = Double.parseDouble(line);

            reader.readLine();// max freq
            reader.readLine();// max n candidates

            for (int i = 0; i < num; i++) {
                PitchMark mark = new PitchMark();

                mark.time = i * dx + x1;

                line = reader.readLine();// intensity

                mark.intensity = Double.parseDouble(line);

                line = reader.readLine();// num candidates
                numc = Integer.parseInt(line);

                if (numc == 0)
                    continue;

                line = reader.readLine();// frequency
                mark.frequency = Double.parseDouble(line);

                line = reader.readLine();// strength
                mark.strength = Double.parseDouble(line);

                for (int j = 1; j < numc; j++) {
                    reader.readLine();// frequency
                    reader.readLine();// strength
                }

                ret.add(mark);
            }

        } catch (IOException e) {
            throw (e);
        }
        return ret;
    }

    public static Vector<PitchMark> momel_pitch(File wav_file, File tmp_dir) throws IOException {

        File script = new File(tmp_dir, "tmp.script");
        File pitch_file = new File(tmp_dir, "tmp.pitch");

        if (script.exists() || pitch_file.exists()) {
            logger.warn("Temporary files exists! Deleting!");
            script.delete();
            pitch_file.delete();
        }

        try (
                PrintWriter writer = new PrintWriter(script)
        ) {
            writer.println("Read from file: \"" + wav_file.getAbsolutePath() + "\"");
            writer.println("To Pitch: 0, 75, 600");
            writer.println("Smooth: 10");
            writer.println("Save as short text file: \"" + pitch_file.getAbsolutePath() + "\"");

        } catch (IOException e) {
            throw e;
        }

        String[] cmd = new String[]{ProgramPaths.praat_bin, "--run", script.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        logger.trace("Running Praat to compute pitch...");
        launcher.run();
        logger.trace("Done.");

        Vector<PitchMark> ret = parseSimplePitchFile(pitch_file);

        script.delete();
        pitch_file.delete();

        return ret;
    }

    public static Vector<Double> pitchmarks_to_pitchstream(Vector<PitchMark> pitchmarks) {

        Vector<Double> ret = new Vector<>();

        for (PitchMark mark : pitchmarks) {
            ret.add(mark.frequency);
        }

        return ret;
    }

}