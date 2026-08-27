package rt4.amilious.voice.speakers;

import rt4.amilious.npc.Gender;

/** No-op when nothing works. */
public final class DisabledSpeaker implements ITextSpeaker {

    @Override
    public void speak(String speaker, String text, Gender gender, Runnable onComplete) {

    }

    @Override
    public void stop() {

    }
}