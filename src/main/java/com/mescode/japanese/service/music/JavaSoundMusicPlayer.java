package com.mescode.japanese.service.music;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.SwingUtilities;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Application-wide offline player. MP3 support is provided through the
 * MP3SPI service provider on the runtime classpath; WAV uses Java Sound
 * directly. All formats are converted to signed 16-bit little-endian PCM.
 */
public final class JavaSoundMusicPlayer implements MusicPlayer {
    private static final int OUTPUT_BUFFER_BYTES = 32 * 1024;
    private static final long STATE_UPDATE_INTERVAL_NANOS = 250_000_000L;
    private static final long PREVIOUS_RESTART_THRESHOLD_MS = 3_000L;

    private final Object lock = new Object();
    private final List<MusicTrack> tracks;
    private final List<Consumer<MusicState>> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService worker;
    private final Random random = new Random();
    private final Set<Integer> failedTracks = new HashSet<>();
    private final Set<Integer> shuffleCyclePlayed = new HashSet<>();

    private int currentIndex;
    private PlaybackStatus status;
    private long positionMillis;
    private long durationMillis;
    private float volume = 0.35f;
    private boolean muted;
    private boolean shuffle;
    private RepeatMode repeatMode = RepeatMode.ALL;
    private String errorMessage;
    private boolean desiredPlaying;
    private boolean closed;
    private long generation;

    private volatile SourceDataLine activeLine;
    private volatile AudioInputStream activeStream;

    public JavaSoundMusicPlayer(PlaylistLoadResult playlist) {
        PlaylistLoadResult safePlaylist = playlist == null
                ? new PlaylistLoadResult(List.of(), List.of("Chưa có nhạc"))
                : playlist;
        this.tracks = safePlaylist.tracks();
        this.currentIndex = tracks.isEmpty() ? -1 : 0;
        this.status = tracks.isEmpty() ? PlaybackStatus.UNAVAILABLE : PlaybackStatus.STOPPED;
        this.errorMessage = tracks.isEmpty()
                ? safePlaylist.warnings().stream().findFirst().orElse("Chưa có nhạc")
                : "";
        this.worker = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "mizuki-music-player");
            thread.setDaemon(true);
            return thread;
        });
        worker.execute(this::playbackWorker);
    }

    public JavaSoundMusicPlayer(List<MusicTrack> tracks) {
        this(new PlaylistLoadResult(tracks, List.of()));
    }

    @Override
    public void play() {
        synchronized (lock) {
            if (!canControlLocked()) {
                return;
            }
            desiredPlaying = true;
            errorMessage = "";
            if (status != PlaybackStatus.PLAYING) {
                status = activeStream == null ? PlaybackStatus.LOADING : PlaybackStatus.PLAYING;
            }
            lock.notifyAll();
        }
        publishState();
    }

    @Override
    public void pause() {
        synchronized (lock) {
            if (!canControlLocked()) {
                return;
            }
            desiredPlaying = false;
            if (status == PlaybackStatus.PLAYING || status == PlaybackStatus.LOADING) {
                status = PlaybackStatus.PAUSED;
            }
            lock.notifyAll();
        }
        publishState();
    }

    @Override
    public void next() {
        boolean interrupt;
        synchronized (lock) {
            if (!canControlLocked()) {
                return;
            }
            currentIndex = chooseNextIndexLocked(false);
            positionMillis = 0L;
            durationMillis = 0L;
            errorMessage = "";
            generation++;
            status = desiredPlaying ? PlaybackStatus.LOADING : PlaybackStatus.PAUSED;
            interrupt = true;
            lock.notifyAll();
        }
        if (interrupt) {
            interruptActivePlayback();
        }
        publishState();
    }

    @Override
    public void previous() {
        synchronized (lock) {
            if (!canControlLocked()) {
                return;
            }
            if (positionMillis > PREVIOUS_RESTART_THRESHOLD_MS) {
                positionMillis = 0L;
            } else {
                currentIndex = currentIndex <= 0 ? tracks.size() - 1 : currentIndex - 1;
                positionMillis = 0L;
                durationMillis = 0L;
            }
            errorMessage = "";
            generation++;
            status = desiredPlaying ? PlaybackStatus.LOADING : PlaybackStatus.PAUSED;
            lock.notifyAll();
        }
        interruptActivePlayback();
        publishState();
    }

    @Override
    public void seek(Duration position) {
        long requested = position == null ? 0L : Math.max(0L, position.toMillis());
        synchronized (lock) {
            if (!canControlLocked()) {
                return;
            }
            positionMillis = durationMillis > 0L
                    ? Math.min(requested, durationMillis)
                    : requested;
            generation++;
            status = desiredPlaying ? PlaybackStatus.LOADING : PlaybackStatus.PAUSED;
            lock.notifyAll();
        }
        interruptActivePlayback();
        publishState();
    }

    @Override
    public void setVolume(float volume) {
        synchronized (lock) {
            if (closed) {
                return;
            }
            this.volume = Math.max(0f, Math.min(1f, volume));
        }
        publishState();
    }

    @Override
    public void setMuted(boolean muted) {
        synchronized (lock) {
            if (closed) {
                return;
            }
            this.muted = muted;
        }
        publishState();
    }

    @Override
    public void setShuffle(boolean shuffle) {
        synchronized (lock) {
            if (closed) {
                return;
            }
            this.shuffle = shuffle;
            shuffleCyclePlayed.clear();
            if (shuffle && currentIndex >= 0) {
                shuffleCyclePlayed.add(currentIndex);
            }
        }
        publishState();
    }

    @Override
    public void setRepeatMode(RepeatMode repeatMode) {
        synchronized (lock) {
            if (closed) {
                return;
            }
            this.repeatMode = repeatMode == null ? RepeatMode.ALL : repeatMode;
        }
        publishState();
    }

    @Override
    public MusicState snapshot() {
        synchronized (lock) {
            MusicTrack track = currentIndex >= 0 && currentIndex < tracks.size()
                    ? tracks.get(currentIndex)
                    : null;
            return new MusicState(
                    track,
                    currentIndex,
                    tracks.size(),
                    status,
                    positionMillis,
                    durationMillis,
                    volume,
                    muted,
                    shuffle,
                    repeatMode,
                    errorMessage
            );
        }
    }

    @Override
    public void addListener(Consumer<MusicState> listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
        dispatch(listener, snapshot());
    }

    @Override
    public void removeListener(Consumer<MusicState> listener) {
        listeners.remove(listener);
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (closed) {
                return;
            }
            closed = true;
            desiredPlaying = false;
            generation++;
            status = PlaybackStatus.CLOSED;
            lock.notifyAll();
        }
        interruptActivePlayback();
        worker.shutdownNow();
        try {
            worker.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        publishState();
    }

    private void playbackWorker() {
        while (true) {
            int trackIndex;
            long startPosition;
            long token;
            synchronized (lock) {
                while (!closed && (!desiredPlaying || currentIndex < 0)) {
                    try {
                        lock.wait();
                    } catch (InterruptedException exception) {
                        if (closed) {
                            return;
                        }
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (closed) {
                    return;
                }
                trackIndex = currentIndex;
                startPosition = positionMillis;
                token = generation;
                status = PlaybackStatus.LOADING;
            }
            publishState();

            try {
                playTrack(trackIndex, startPosition, token);
            } catch (Exception exception) {
                handlePlaybackFailure(trackIndex, token, exception);
            } finally {
                clearActivePlayback();
            }
        }
    }

    private void playTrack(int trackIndex, long requestedPosition, long token) throws Exception {
        MusicTrack track = tracks.get(trackIndex);
        DecodedAudio decodedAudio = openPcmStream(track);
        try (AudioInputStream pcmStream = decodedAudio.stream()) {
            activeStream = pcmStream;
            AudioFormat format = pcmStream.getFormat();
            long discoveredDuration = decodedAudio.durationMillis() > 0L
                    ? decodedAudio.durationMillis()
                    : durationMillis(pcmStream);
            long targetPosition = discoveredDuration > 0L
                    ? Math.min(requestedPosition, discoveredDuration)
                    : requestedPosition;
            long skippedBytes = skipToPosition(pcmStream, format, targetPosition);
            long actualStart = millisForBytes(skippedBytes, format);

            synchronized (lock) {
                if (!isCurrentLocked(trackIndex, token)) {
                    return;
                }
                durationMillis = discoveredDuration;
                positionMillis = actualStart;
                status = desiredPlaying ? PlaybackStatus.PLAYING : PlaybackStatus.PAUSED;
                errorMessage = "";
            }
            publishState();

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, OUTPUT_BUFFER_BYTES);
            activeLine = line;
            line.start();

            byte[] buffer = new byte[OUTPUT_BUFFER_BYTES];
            long playedBytes = 0L;
            long lastUpdate = 0L;
            boolean reachedEnd = false;
            while (true) {
                synchronized (lock) {
                    while (!closed && isCurrentLocked(trackIndex, token) && !desiredPlaying) {
                        line.stop();
                        status = PlaybackStatus.PAUSED;
                        lock.wait();
                    }
                    if (closed || !isCurrentLocked(trackIndex, token)) {
                        break;
                    }
                    line.start();
                    status = PlaybackStatus.PLAYING;
                }

                int read = pcmStream.read(buffer, 0, buffer.length);
                if (read < 0) {
                    reachedEnd = true;
                    break;
                }
                applySoftwareGain(buffer, read);
                int written = line.write(buffer, 0, read);
                playedBytes += Math.max(0, written);

                long now = System.nanoTime();
                if (lastUpdate == 0L || now - lastUpdate >= STATE_UPDATE_INTERVAL_NANOS) {
                    synchronized (lock) {
                        if (isCurrentLocked(trackIndex, token)) {
                            positionMillis = Math.min(
                                    durationMillis > 0L ? durationMillis : Long.MAX_VALUE,
                                    actualStart + millisForBytes(playedBytes, format)
                            );
                            failedTracks.remove(trackIndex);
                        }
                    }
                    publishState();
                    lastUpdate = now;
                }
            }
            if (reachedEnd) {
                line.drain();
                handleNaturalCompletion(trackIndex, token);
            }
        }
    }

    private DecodedAudio openPcmStream(MusicTrack track) throws Exception {
        URL resourceUrl = JavaSoundMusicPlayer.class.getResource(track.resource());
        if (resourceUrl == null) {
            throw new IOException("Không tìm thấy " + track.resource());
        }
        long metadataDuration = readMetadataDuration(resourceUrl);
        InputStream resource = resourceUrl.openStream();
        BufferedInputStream buffered = new BufferedInputStream(resource, 1024 * 1024);
        AudioInputStream encoded;
        try {
            encoded = AudioSystem.getAudioInputStream(buffered);
        } catch (Exception exception) {
            buffered.close();
            throw exception;
        }
        AudioFormat source = encoded.getFormat();
        float sampleRate = source.getSampleRate() > 0f ? source.getSampleRate() : 44_100f;
        int channels = Math.max(1, source.getChannels());
        AudioFormat target = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                16,
                channels,
                channels * 2,
                sampleRate,
                false
        );
        if (source.matches(target)) {
            return new DecodedAudio(encoded, metadataDuration);
        }
        try {
            return new DecodedAudio(
                    AudioSystem.getAudioInputStream(target, encoded),
                    metadataDuration
            );
        } catch (Exception exception) {
            encoded.close();
            throw exception;
        }
    }

    private long readMetadataDuration(URL resourceUrl) {
        try {
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(resourceUrl);
            Object durationMicros = fileFormat.properties().get("duration");
            if (durationMicros instanceof Number number && number.longValue() > 0L) {
                return number.longValue() / 1000L;
            }
            AudioFormat format = fileFormat.getFormat();
            if (fileFormat.getFrameLength() > 0 && format.getFrameRate() > 0f) {
                return Math.round(fileFormat.getFrameLength() * 1000d / format.getFrameRate());
            }
        } catch (Exception ignored) {
            // Playback can still proceed when optional metadata probing fails.
        }
        return 0L;
    }

    private long durationMillis(AudioInputStream stream) {
        AudioFormat format = stream.getFormat();
        long frames = stream.getFrameLength();
        if (frames <= 0L || frames == AudioSystem.NOT_SPECIFIED || format.getFrameRate() <= 0f) {
            return 0L;
        }
        return Math.max(0L, Math.round(frames * 1000d / format.getFrameRate()));
    }

    private long skipToPosition(AudioInputStream stream, AudioFormat format, long millis)
            throws IOException {
        long requestedBytes = bytesForMillis(millis, format);
        long skipped = 0L;
        byte[] discard = new byte[OUTPUT_BUFFER_BYTES];
        while (skipped < requestedBytes) {
            long direct = stream.skip(requestedBytes - skipped);
            if (direct > 0L) {
                skipped += direct;
                continue;
            }
            int read = stream.read(discard, 0, (int) Math.min(discard.length, requestedBytes - skipped));
            if (read < 0) {
                break;
            }
            skipped += read;
        }
        int frameSize = Math.max(1, format.getFrameSize());
        return skipped - skipped % frameSize;
    }

    private long bytesForMillis(long millis, AudioFormat format) {
        if (millis <= 0L || format.getFrameRate() <= 0f || format.getFrameSize() <= 0) {
            return 0L;
        }
        return Math.max(0L, Math.round(millis / 1000d * format.getFrameRate()) * format.getFrameSize());
    }

    private long millisForBytes(long bytes, AudioFormat format) {
        if (bytes <= 0L || format.getFrameRate() <= 0f || format.getFrameSize() <= 0) {
            return 0L;
        }
        return Math.max(0L, Math.round(
                bytes * 1000d / (format.getFrameRate() * format.getFrameSize())
        ));
    }

    private void applySoftwareGain(byte[] buffer, int length) {
        float gain;
        synchronized (lock) {
            gain = muted ? 0f : volume;
        }
        if (gain >= 0.999f) {
            return;
        }
        for (int index = 0; index + 1 < length; index += 2) {
            int low = buffer[index] & 0xFF;
            int high = buffer[index + 1];
            short sample = (short) (low | high << 8);
            int scaled = Math.round(sample * gain);
            scaled = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, scaled));
            buffer[index] = (byte) (scaled & 0xFF);
            buffer[index + 1] = (byte) ((scaled >>> 8) & 0xFF);
        }
    }

    private void handleNaturalCompletion(int trackIndex, long token) {
        synchronized (lock) {
            if (!isCurrentLocked(trackIndex, token)) {
                return;
            }
            positionMillis = durationMillis;
            failedTracks.remove(trackIndex);
            if (repeatMode == RepeatMode.ONE) {
                positionMillis = 0L;
                generation++;
                status = PlaybackStatus.LOADING;
            } else {
                boolean atLastTrack = currentIndex == tracks.size() - 1;
                if (shuffle) {
                    shuffleCyclePlayed.add(currentIndex);
                }
                boolean finishedShuffleCycle = shuffle
                        && shuffleCyclePlayed.size() >= tracks.size();
                if (repeatMode == RepeatMode.OFF
                        && ((!shuffle && atLastTrack) || finishedShuffleCycle)) {
                    desiredPlaying = false;
                    status = PlaybackStatus.STOPPED;
                } else {
                    if (finishedShuffleCycle) {
                        shuffleCyclePlayed.clear();
                    }
                    currentIndex = chooseNextIndexLocked(true);
                    positionMillis = 0L;
                    durationMillis = 0L;
                    generation++;
                    status = PlaybackStatus.LOADING;
                }
            }
            lock.notifyAll();
        }
        publishState();
    }

    private void handlePlaybackFailure(int trackIndex, long token, Exception exception) {
        synchronized (lock) {
            if (closed || !isCurrentLocked(trackIndex, token)) {
                return;
            }
            failedTracks.add(trackIndex);
            errorMessage = readableError(exception);
            if (desiredPlaying && failedTracks.size() < tracks.size()) {
                currentIndex = chooseNextIndexLocked(false);
                positionMillis = 0L;
                durationMillis = 0L;
                generation++;
                status = PlaybackStatus.LOADING;
                lock.notifyAll();
            } else {
                desiredPlaying = false;
                status = PlaybackStatus.ERROR;
            }
        }
        publishState();
    }

    private int chooseNextIndexLocked(boolean naturalCompletion) {
        if (tracks.size() <= 1) {
            return Math.max(0, currentIndex);
        }
        if (shuffle) {
            int candidate;
            int attempts = 0;
            do {
                candidate = random.nextInt(tracks.size());
                attempts++;
            } while ((candidate == currentIndex
                    || failedTracks.contains(candidate)
                    || shuffleCyclePlayed.contains(candidate))
                    && attempts < tracks.size() * 3);
            if (failedTracks.contains(candidate) || shuffleCyclePlayed.contains(candidate)) {
                for (int index = 0; index < tracks.size(); index++) {
                    if (index != currentIndex
                            && !failedTracks.contains(index)
                            && !shuffleCyclePlayed.contains(index)) {
                        return index;
                    }
                }
            }
            return candidate;
        }
        int candidate = (currentIndex + 1) % tracks.size();
        if (!naturalCompletion) {
            int attempts = 0;
            while (failedTracks.contains(candidate) && attempts < tracks.size()) {
                candidate = (candidate + 1) % tracks.size();
                attempts++;
            }
        }
        return candidate;
    }

    private boolean canControlLocked() {
        return !closed && currentIndex >= 0 && !tracks.isEmpty();
    }

    private boolean isCurrentLocked(int trackIndex, long token) {
        return currentIndex == trackIndex && generation == token;
    }

    private String readableError(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }
        return "Không thể phát bài này: " + message;
    }

    private void interruptActivePlayback() {
        SourceDataLine line = activeLine;
        if (line != null) {
            try {
                line.stop();
                line.flush();
                line.close();
            } catch (RuntimeException ignored) {
                // The worker may already have closed the line.
            }
        }
        AudioInputStream stream = activeStream;
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
                // The worker owns final cleanup.
            }
        }
    }

    private void clearActivePlayback() {
        SourceDataLine line = activeLine;
        activeLine = null;
        if (line != null && line.isOpen()) {
            try {
                line.stop();
                line.flush();
                line.close();
            } catch (RuntimeException ignored) {
                // Best-effort device cleanup.
            }
        }
        AudioInputStream stream = activeStream;
        activeStream = null;
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
                // Best-effort stream cleanup.
            }
        }
    }

    private void publishState() {
        MusicState state = snapshot();
        for (Consumer<MusicState> listener : listeners) {
            dispatch(listener, state);
        }
    }

    private void dispatch(Consumer<MusicState> listener, MusicState state) {
        if (SwingUtilities.isEventDispatchThread()) {
            listener.accept(state);
        } else {
            SwingUtilities.invokeLater(() -> listener.accept(state));
        }
    }

    private record DecodedAudio(AudioInputStream stream, long durationMillis) {
    }
}
