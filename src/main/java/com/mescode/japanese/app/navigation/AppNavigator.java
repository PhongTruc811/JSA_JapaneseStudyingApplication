package com.mescode.japanese.app.navigation;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.controller.KanaController;
import com.mescode.japanese.controller.LoginController;
import com.mescode.japanese.controller.PeTrialController;
import com.mescode.japanese.controller.VocabController;
import com.mescode.japanese.model.kana.KanaQuizOptions;
import com.mescode.japanese.model.petrial.PeTrialAnswerResult;
import com.mescode.japanese.model.petrial.PeTrialConfig;
import com.mescode.japanese.model.vocab.VocabQuizAnswerResult;
import com.mescode.japanese.model.vocab.VocabQuizConfig;
import com.mescode.japanese.model.vocab.Vocabulary;
import com.mescode.japanese.repo.KanjiRepository;
import com.mescode.japanese.repo.PeTrialRepository;
import com.mescode.japanese.service.KanaService;
import com.mescode.japanese.service.PeTrialService;
import com.mescode.japanese.service.UserService;
import com.mescode.japanese.service.VocabService;
import com.mescode.japanese.service.music.JavaSoundMusicPlayer;
import com.mescode.japanese.service.music.MusicPlayer;
import com.mescode.japanese.service.music.PlaylistLoader;
import com.mescode.japanese.view.activate.AccessKeyFrame;
import com.mescode.japanese.view.activate.ActivateFrame;
import com.mescode.japanese.view.chatbot.MizukiChatAssistant;
import com.mescode.japanese.view.grammar.GrammarLessonFrame;
import com.mescode.japanese.view.grammar.GrammarMenuFrame;
import com.mescode.japanese.view.grammar.GrammarQuizFrame;
import com.mescode.japanese.view.kana.KanaDifficultyDialog;
import com.mescode.japanese.view.kana.KanaMenuFrame;
import com.mescode.japanese.view.kana.KanaQuizFrame;
import com.mescode.japanese.view.login.LoginFrame;
import com.mescode.japanese.view.menu.AppMenuFrame;
import com.mescode.japanese.view.petrial.PeTrialDifficultyDialog;
import com.mescode.japanese.view.petrial.PeTrialQuizFrame;
import com.mescode.japanese.view.petrial.PeTrialResultFrame;
import com.mescode.japanese.view.settings.SettingFrame;
import com.mescode.japanese.view.splash.SplashFrame;
import com.mescode.japanese.view.vocabulary.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

// Navigator + Bootstrap
// AppContext CHỈ tồn tại trong Navigator
public class AppNavigator implements AutoCloseable {
    JFrame currentFrame;

    private final AppContext appContext;
    private final MusicPlayer musicPlayer;
    private MizukiChatAssistant mizuki;
    private boolean closed;

    public AppNavigator(AppContext appContext) {
        this(appContext, new JavaSoundMusicPlayer(PlaylistLoader.loadBundled()));
    }

    AppNavigator(AppContext appContext, MusicPlayer musicPlayer) {
        this.appContext = appContext;
        this.musicPlayer = musicPlayer == null ? MusicPlayer.unavailable() : musicPlayer;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public void firstStart() {
        new SplashFrame(this);
    }

    public void navigateTo(AppRoute appRoute) {
        if (appRoute == AppRoute.AddVocab) {
            JOptionPane.showMessageDialog(
                    currentFrame,
                    "This feature is out of scope for now and will be available in a future update.",
                    "Coming Soon",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        VocabQuizConfig vocabularyQuizConfig = null;
        KanaQuizOptions kanaQuizOptions = null;
        PeTrialConfig peTrialConfig = null;
        if (appRoute == AppRoute.VocabQuiz) {
            vocabularyQuizConfig = chooseVocabularyQuiz();
            if (vocabularyQuizConfig == null) {
                return;
            }
        }
        if (appRoute == AppRoute.HiraQuiz || appRoute == AppRoute.KataQuiz) {
            kanaQuizOptions = KanaDifficultyDialog.showOptionsDialog(currentFrame, appContext.isDarkMode());
            if (kanaQuizOptions == null) {
                return;
            }
        }
        if (appRoute == AppRoute.PETrialSP26) {
            peTrialConfig = PeTrialDifficultyDialog.showDialog(currentFrame, appContext.isDarkMode());
            if (peTrialConfig == null) {
                return;
            }
        }

        if (currentFrame != null) {
            currentFrame.dispose();
        }
        switch (appRoute) {
            case AccessKey -> currentFrame = new AccessKeyFrame(this);
            case Activate -> currentFrame = new ActivateFrame(this);
            case Login -> {
                LoginFrame view = new LoginFrame(this);
                currentFrame = view;
                new LoginController(view, new UserService());
            }
            case AppMenu -> currentFrame = new AppMenuFrame(this);
            // Kana Module
            case Kana -> currentFrame = new KanaMenuFrame(this);
            case HiraQuiz, KataQuiz -> createKanaQuiz(appRoute, kanaQuizOptions);
            // Vocabulary Module
            case Vocab -> currentFrame = new VocabMenuFrame(this);
                case ShowVocab -> currentFrame = new ShowVocabFrame(this, filterChapterVocabs(appContext.getVocabs()));
                case ShowKanji -> currentFrame = new ShowKanjiFrame(this, new KanjiRepository().getKanjis());
                case VocabQuiz -> createVocabQuiz(vocabularyQuizConfig);
            // Grammar Module
            case Grammar -> currentFrame = new GrammarMenuFrame(this);
                case GrammarLesson -> currentFrame = new GrammarLessonFrame(this, "chapter-1");
                case GrammarQuiz -> currentFrame = new GrammarQuizFrame(this, "chapter-1");
            // PE Module
            case PETrialSP26 -> createPeTrialQuiz(peTrialConfig);
            case Settings -> currentFrame = new SettingFrame(this, appContext);
            default -> throw new IllegalArgumentException("Unknown screen");
        }
        openMaximized(currentFrame);
    }

    public void restartKanaQuiz(AppRoute quizOption, KanaQuizOptions options) {
        if (quizOption != AppRoute.HiraQuiz && quizOption != AppRoute.KataQuiz) {
            throw new IllegalArgumentException("Kana quiz option required");
        }
        if (options == null) {
            throw new IllegalArgumentException("Kana quiz options required");
        }
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        createKanaQuiz(quizOption, options);
        openMaximized(currentFrame);
    }

    public void restartVocabQuiz(VocabQuizConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Vocabulary quiz config required");
        }
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        createVocabQuiz(config);
        openMaximized(currentFrame);
    }

    public void restartPeTrialQuiz(PeTrialConfig difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("PE Trial difficulty required");
        }
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        createPeTrialQuiz(difficulty);
        openMaximized(currentFrame);
    }

    public void openGrammarMenu() {
        switchFrame(new GrammarMenuFrame(this));
    }

    public void openGrammarLesson(String chapterId) {
        if (chapterId == null || chapterId.isBlank()) {
            throw new IllegalArgumentException("Grammar chapter id required");
        }
        switchFrame(new GrammarLessonFrame(this, chapterId));
    }

    public void openGrammarQuiz(String chapterId) {
        if (chapterId == null || chapterId.isBlank()) {
            throw new IllegalArgumentException("Grammar chapter id required");
        }
        switchFrame(new GrammarQuizFrame(this, chapterId));
    }

    private void switchFrame(JFrame nextFrame) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        currentFrame = nextFrame;
        openMaximized(currentFrame);
    }

    private void createKanaQuiz(AppRoute quizOption, KanaQuizOptions options) {
        KanaQuizFrame view = new KanaQuizFrame(this, quizOption, options);
        currentFrame = view;
        KanaService service = quizOption == AppRoute.KataQuiz
                ? new KanaService(appContext.getKatakanaList(), options.group())
                : new KanaService(appContext.getHiraganaList(), options.group());
        new KanaController(view, service);
    }

    private void createVocabQuiz(VocabQuizConfig config) {
        VocabService service = new VocabService(appContext.getVocabs(), config);
        if (service.getQuizQuestions().isEmpty()) {
            JOptionPane.showMessageDialog(currentFrame,
                    VocabService.validateQuizData(appContext.getVocabs(), config),
                    "Cannot start quiz", JOptionPane.WARNING_MESSAGE);
            currentFrame = new VocabMenuFrame(this);
            return;
        }
        DoVocabQuizFrame view = new DoVocabQuizFrame(this, config);
        currentFrame = view;
        new VocabController(view, service, config, results -> showVocabQuizResult(config, results));
    }

    private void createPeTrialQuiz(PeTrialConfig difficulty) {
        try {
            PeTrialService service = new PeTrialService(new PeTrialRepository());
            PeTrialQuizFrame view = new PeTrialQuizFrame(this, difficulty);
            currentFrame = view;
            new PeTrialController(
                    view,
                    service,
                    difficulty,
                    (results, timeExpired) -> showPeTrialResult(difficulty, results, timeExpired)
            );
        } catch (IllegalStateException exception) {
            if (currentFrame != null) {
                currentFrame.dispose();
            }
            JOptionPane.showMessageDialog(
                    currentFrame,
                    exception.getMessage(),
                    "Cannot start PE Trial",
                    JOptionPane.WARNING_MESSAGE
            );
            currentFrame = new AppMenuFrame(this);
        }
    }

    private void showVocabQuizResult(VocabQuizConfig config, List<VocabQuizAnswerResult> results) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        currentFrame = new VocabQuizResultFrame(this, config, results);
        openMaximized(currentFrame);
    }

    private void showPeTrialResult(PeTrialConfig difficulty, List<PeTrialAnswerResult> results,
                                   boolean timeExpired) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        currentFrame = new PeTrialResultFrame(this, difficulty, results, timeExpired);
        openMaximized(currentFrame);
    }

    private VocabQuizConfig chooseVocabularyQuiz() {
        return VocabQuizTypeDialog.showDialog(currentFrame, appContext.isDarkMode(), appContext.getVocabs());
    }

    static List<Vocabulary> filterChapterVocabs(List<Vocabulary> vocabularies) {
        if (vocabularies == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(vocabularies.stream()
                .filter(vocab -> vocab != null && vocab.getLesson() != null)
                .filter(vocab -> vocab.getLesson() >= 1 && vocab.getLesson() <= 3)
                .toList());
    }

    // Mở frame với chế độ full screen, và thêm window listener để đóng app khi frame đóng
    private void openMaximized(JFrame frame) {
        if (frame == null) {
            return;
        }
        frame.setResizable(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                AppNavigator.this.close();
            }
        });
        frame.setVisible(true);
        updateChatAssistantHost(frame);
    }

    private boolean isPeTrialFrame(JFrame frame) {
        return frame instanceof PeTrialQuizFrame
                || frame instanceof PeTrialResultFrame;
    }

    private MizukiChatAssistant getMizuki() {
        if (mizuki == null) { // kiểm tra xem mizuki đã được khởi tạo chưa, nếu chưa thì new để khời tạo
            mizuki = new MizukiChatAssistant(appContext, musicPlayer);
        }
        return mizuki;
    }

    // Quản lý trạng thái AppChatAssistant theo loại frame được hiển thị
    private void updateChatAssistantHost(JFrame frame) {
        if (isPeTrialFrame(frame)) {
            if (mizuki != null) {
                mizuki.detach(); // tạm thời đóng chat assistant khi đang trong module PE Trial
            }
            return;
        }
        if (supportsChatAssistant(frame)) {
            getMizuki().attachTo(frame);
        } else {
            closeChatAssistant();
        }
    }

    // Kiểm tra xem frame hiện tại có hỗ trợ chat assistant không
    private boolean supportsChatAssistant(JFrame currentFrame) {
        // return false nếu frame hiện tại là AccessKeyFrame, ActivateFrame hoặc LoginFrame
        return !(currentFrame instanceof AccessKeyFrame)
                && !(currentFrame instanceof ActivateFrame)
                && !(currentFrame instanceof LoginFrame);
    }

    private void closeChatAssistant() {
        if (mizuki == null) {
            return;
        }
        mizuki.close();
        mizuki = null;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        closeChatAssistant();
        musicPlayer.close();
    }
}
