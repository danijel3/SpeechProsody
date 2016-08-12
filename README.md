# SpeechProsody

This project contains a simple Java wrapper which can be used to calculate INTSINT/MOMEL features and dump them in an easy Json format.

## Requirements

  * praat - from [http://www.praat.org]
  * intsint and momel praat plugin - Google "momel_intsint.zip"
  * perl
  
Optional (not used yet):
  * sox
  * ffmpeg
  
## Usage

Download and place praat in some convenient place. Unpack momel_intsint.zip somewhere as well.

Modify the "paths.conf" file to point to the right programs/files.

Run the program.

```
Usage: <main class> [options]
  Options:
    -d
       Print debug of intermediary steps.
       Default: false
    -h
       This help.
       Default: false
  * -i
       Input WAV file.
  * -o
       Output JSON file.
    -t
       Path to tmp directory.
       Default: ./
```




