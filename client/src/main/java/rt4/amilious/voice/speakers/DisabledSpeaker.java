package rt4.amilious.voice.speakers;

import rt4.amilious.Gender;
import rt4.amilious.voice.speakers.ITextSpeaker;

/** No-op when nothing works. */
public final class DisabledSpeaker implements ITextSpeaker {

    @Override
    public void speak(String speaker, String text, Gender gender, Runnable onComplete) {

    }

    @Override
    public void stop() {

    }
}