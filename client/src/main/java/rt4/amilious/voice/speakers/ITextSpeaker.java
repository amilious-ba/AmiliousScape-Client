package rt4.amilious.voice.speakers;

import rt4.amilious.npc.Gender;

public interface ITextSpeaker {

    /** Speak with explicit gender (resolved by caller). */
    void speak(String speaker, String text, Gender gender, Runnable onComplete);

    void stop();
}