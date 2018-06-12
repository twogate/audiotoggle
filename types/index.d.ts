interface Window {
  AudioToggle: AudioToggle;
}

interface AudioToggle {
  setAutoRoute: (command: string) => void;
}

declare var AudioToggle: AudioToggle;
