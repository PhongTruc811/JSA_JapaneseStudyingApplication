package com.mescode.japanese.app.navigation;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.controller.KanaController;
import com.mescode.japanese.controller.LoginController;
import com.mescode.japanese.controller.VocabController;
import com.mescode.japanese.model.Vocabulary;
import com.mescode.japanese.repo.KanjiRepository;
import com.mescode.japanese.service.KanaService;
import com.mescode.japanese.service.UserService;
import com.mescode.japanese.service.VocabService;
import com.mescode.japanese.view.kana.KanaMenuFrame;
import com.mescode.japanese.view.vocabulary.VocabMenuFrame;
import com.mescode.japanese.view.kana.KanaQuizFrame;
import com.mescode.japanese.view.vocabulary.VocabQuizFrame;
import com.mescode.japanese.view.login.LoginFrame;
import com.mescode.japanese.view.menu.MenuFrame;
import com.mescode.japanese.view.vocabulary.AddVocabFrame;
import com.mescode.japanese.view.vocabulary.ShowVocabularyFrame;
import com.mescode.japanese.view.vocabulary.ShowKanjiFrame;
import com.mescode.japanese.view.settings.SettingsFrame;
import com.mescode.japanese.view.splash.SplashFrame;
import com.mescode.japanese.view.activate.AccessKeyFrame;
import com.mescode.japanese.view.activate.ActivateFrame;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

// Navigator + Bootstrap
// AppContext CHỈ tồn tại trong Navigator
public class MenuNavigator {
    private static final String KANJI_QUIZ_OPTION = "Kanji";
    private static final String VOCAB_QUIZ_OPTION = "Vocabs (Chapter 1-3)";

    JFrame currentFrame;

    private final AppContext appContext;

    public MenuNavigator(AppContext appContext) {
        this.appContext = appContext;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public void firstStart() {
        // Show splash screen first, then auto-navigate to access key gate
        new SplashFrame(this);
    }

    public void navigateTo(MenuOptions menuOptions) {
        VocabularyQuizChoice vocabularyQuizChoice = null;
        if (menuOptions == MenuOptions.VocabQuiz) {
            vocabularyQuizChoice = chooseVocabularyQuiz();
            if (vocabularyQuizChoice == null) {
                return;
            }
        }

        if(currentFrame != null) {
            currentFrame.dispose();
        }
        switch (menuOptions) {
            case AccessKey -> {
                currentFrame = new AccessKeyFrame(this);
            }
            case Activate -> {
                currentFrame = new ActivateFrame(this);
            }
            case Login -> {
                LoginFrame view = new LoginFrame(this);
                currentFrame = view;
                UserService service = new UserService();
                new LoginController(view, service);
            }
            case MenuHome -> {
                currentFrame = new MenuFrame(this);
            }
            case Kana -> {
                currentFrame = new KanaMenuFrame(this);
            }
            case HiraQuiz -> {
                // service -> controller -> view
                KanaQuizFrame view = new KanaQuizFrame(this);
                currentFrame = view;
                KanaService service = new KanaService(appContext.getHiraganaList());
                new KanaController(view, service);
            }
            case KataQuiz -> {
                KanaQuizFrame view = new KanaQuizFrame(this);
                currentFrame = view;
                KanaService service = new KanaService(appContext.getKatakanaList());
                new KanaController(view, service);
            }
            case Vocab -> {
                currentFrame = new VocabMenuFrame(this);
            }
            case AddVocab -> {
                currentFrame = new AddVocabFrame(this);
            }
            case Grammar -> {
                currentFrame = new com.mescode.japanese.view.grammar.GrammarMenuFrame(this);
            }
            case GrammarQuiz -> {
                currentFrame = new com.mescode.japanese.view.grammar.GrammarQuizFrame(this);
            }
            case VocabQuiz -> {
                com.mescode.japanese.view.vocabulary.VocabQuizFrame view =
                        new com.mescode.japanese.view.vocabulary.VocabQuizFrame(this, vocabularyQuizChoice.showKanji);
                currentFrame = view;
                VocabService service = new VocabService(vocabularyQuizChoice.vocabularies);
                new VocabController(view, service);
            }
            case ShowVocab -> {
                currentFrame = new com.mescode.japanese.view.vocabulary.ShowVocabularyFrame(this, appContext.getVocabs());
            }
            case ShowKanji -> {
                currentFrame = new ShowKanjiFrame(this, new KanjiRepository().getKanjis());
            }
            case Settings -> {
                currentFrame = new SettingsFrame(this, appContext);
            }
            default -> throw new IllegalArgumentException("Unknown screen");
        }
        openMaximized(currentFrame);
    }

    private VocabularyQuizChoice chooseVocabularyQuiz() {
        String[] options = {KANJI_QUIZ_OPTION, VOCAB_QUIZ_OPTION};
        Object selected = JOptionPane.showInputDialog(
                currentFrame,
                "Choose quiz type:",
                "Vocabulary Quiz",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (selected == null) {
            return null;
        }

        List<Vocabulary> vocabularies = appContext.getVocabs();
        if (KANJI_QUIZ_OPTION.equals(selected)) {
            return new VocabularyQuizChoice(filterKanjiVocabs(vocabularies), true);
        }
        return new VocabularyQuizChoice(filterChapterVocabs(vocabularies), false);
    }

    private List<Vocabulary> filterKanjiVocabs(List<Vocabulary> vocabularies) {
        if (vocabularies == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(vocabularies.stream()
                .filter(vocab -> vocab != null && hasText(vocab.getKanji()))
                .toList());
    }

    private List<Vocabulary> filterChapterVocabs(List<Vocabulary> vocabularies) {
        if (vocabularies == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(vocabularies.stream()
                .filter(vocab -> vocab != null && vocab.getLesson() != null)
                .filter(vocab -> vocab.getLesson() >= 1 && vocab.getLesson() <= 3)
                .toList());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void openMaximized(JFrame frame) {
        if (frame == null) {
            return;
        }
        frame.setResizable(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private record VocabularyQuizChoice(List<Vocabulary> vocabularies, boolean showKanji) {
    }
}
