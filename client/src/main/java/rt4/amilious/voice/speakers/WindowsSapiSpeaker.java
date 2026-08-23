package rt4.amilious.voice.speakers;

import rt4.amilious.Gender;

public final class WindowsSapiSpeaker extends AbstractProcessSpeaker {

    public WindowsSapiSpeaker() {
        super("windows-sapi");
    }

    @Override
    public boolean isAvailable() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    @Override
    protected String[] buildCommand(String speaker, String text, Gender gender) {
        String safe = shellSingleQuote(text);
        boolean female = gender == Gender.FEMALE;
        String prefer = female
                ? "($_.Gender -eq 'Female') -or ($_.Name -match 'Zira|Female|Hazel|Susan|Helen')"
                : "($_.Gender -eq 'Male') -or ($_.Name -match 'David|Male|Mark|George|James')";

        String ps =
                "Add-Type -AssemblyName System.Speech; "
                        + "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                        + "$v = $s.GetInstalledVoices() | ForEach-Object { $_.VoiceInfo } "
                        + "| Where-Object { " + prefer + " } | Select-Object -First 1; "
                        + "if ($v) { $s.SelectVoice($v.Name) }; "
                        + "$s.Speak('" + safe + "');";

        return new String[] {
                "powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", ps
        };
    }

    @Override
    protected String hint() {
        return "Windows SAPI / PowerShell required.";
    }
}