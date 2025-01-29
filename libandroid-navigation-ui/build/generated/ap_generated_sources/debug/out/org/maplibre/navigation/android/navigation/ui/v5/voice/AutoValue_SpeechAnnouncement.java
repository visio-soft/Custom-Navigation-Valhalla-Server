package org.maplibre.navigation.android.navigation.ui.v5.voice;

import androidx.annotation.Nullable;
import javax.annotation.processing.Generated;
import org.maplibre.navigation.android.navigation.v5.milestone.VoiceInstructionMilestone;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_SpeechAnnouncement extends SpeechAnnouncement {

  @Nullable
  private final String ssmlAnnouncement;

  private final String announcement;

  @Nullable
  private final VoiceInstructionMilestone voiceInstructionMilestone;

  private AutoValue_SpeechAnnouncement(
      @Nullable String ssmlAnnouncement,
      String announcement,
      @Nullable VoiceInstructionMilestone voiceInstructionMilestone) {
    this.ssmlAnnouncement = ssmlAnnouncement;
    this.announcement = announcement;
    this.voiceInstructionMilestone = voiceInstructionMilestone;
  }

  @Nullable
  @Override
  public String ssmlAnnouncement() {
    return ssmlAnnouncement;
  }

  @Override
  public String announcement() {
    return announcement;
  }

  @Nullable
  @Override
  VoiceInstructionMilestone voiceInstructionMilestone() {
    return voiceInstructionMilestone;
  }

  @Override
  public String toString() {
    return "SpeechAnnouncement{"
        + "ssmlAnnouncement=" + ssmlAnnouncement + ", "
        + "announcement=" + announcement + ", "
        + "voiceInstructionMilestone=" + voiceInstructionMilestone
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SpeechAnnouncement) {
      SpeechAnnouncement that = (SpeechAnnouncement) o;
      return (this.ssmlAnnouncement == null ? that.ssmlAnnouncement() == null : this.ssmlAnnouncement.equals(that.ssmlAnnouncement()))
          && this.announcement.equals(that.announcement())
          && (this.voiceInstructionMilestone == null ? that.voiceInstructionMilestone() == null : this.voiceInstructionMilestone.equals(that.voiceInstructionMilestone()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= (ssmlAnnouncement == null) ? 0 : ssmlAnnouncement.hashCode();
    h$ *= 1000003;
    h$ ^= announcement.hashCode();
    h$ *= 1000003;
    h$ ^= (voiceInstructionMilestone == null) ? 0 : voiceInstructionMilestone.hashCode();
    return h$;
  }

  @Override
  public SpeechAnnouncement.Builder toBuilder() {
    return new AutoValue_SpeechAnnouncement.Builder(this);
  }

  static final class Builder extends SpeechAnnouncement.Builder {
    private String ssmlAnnouncement;
    private String announcement;
    private VoiceInstructionMilestone voiceInstructionMilestone;
    Builder() {
    }
    Builder(SpeechAnnouncement source) {
      this.ssmlAnnouncement = source.ssmlAnnouncement();
      this.announcement = source.announcement();
      this.voiceInstructionMilestone = source.voiceInstructionMilestone();
    }
    @Override
    public SpeechAnnouncement.Builder ssmlAnnouncement(@Nullable String ssmlAnnouncement) {
      this.ssmlAnnouncement = ssmlAnnouncement;
      return this;
    }
    @Override
    public SpeechAnnouncement.Builder announcement(String announcement) {
      if (announcement == null) {
        throw new NullPointerException("Null announcement");
      }
      this.announcement = announcement;
      return this;
    }
    @Override
    public SpeechAnnouncement.Builder voiceInstructionMilestone(@Nullable VoiceInstructionMilestone voiceInstructionMilestone) {
      this.voiceInstructionMilestone = voiceInstructionMilestone;
      return this;
    }
    @Override
    @Nullable VoiceInstructionMilestone voiceInstructionMilestone() {
      return voiceInstructionMilestone;
    }
    @Override
    SpeechAnnouncement autoBuild() {
      if (this.announcement == null) {
        String missing = " announcement";
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AutoValue_SpeechAnnouncement(
          this.ssmlAnnouncement,
          this.announcement,
          this.voiceInstructionMilestone);
    }
  }

}
