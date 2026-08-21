package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;
import com.mescode.japanese.model.vocab.Vocabulary;
import com.mescode.japanese.util.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ShowVocabFrame extends JFrame {

    private final AppNavigator navigator;
    private final AppContext appContext;
    private final List<Vocabulary> vocabularies;

    private boolean darkMode;
    private boolean uiBuilt;
    private boolean searchExpanded;
    private boolean filterExpanded;

    private Color background;
    private Color panelBackground;
    private Color cardBackground;
    private Color border;
    private Color hover;
    private Color pressed;
    private Color titleForeground;
    private Color textForeground;
    private Color accent;
    private Color glow;
    private Color mutedText;
    private Color tableStripe;
    private Color tableSelection;
    private Color favoriteOn;
    private Color favoriteOff;
    private Color learnedOn;
    private Color learnedOff;
    private Color controlCollapsed;

    private JTable table;
    private VocabularyTableModel tableModel;
    private TableRowSorter<VocabularyTableModel> sorter;
    private JTextField searchField;
    private JLabel resultCountLabel;
    private JToggleButton favoritesOnlyToggle;
    private JToggleButton learnedOnlyToggle;
    private JToggleButton unlearnedOnlyToggle;
    private JToggleButton chapter1Toggle;
    private JToggleButton chapter2Toggle;
    private JToggleButton chapter3Toggle;

    private final Font titleFont = new Font("Segoe UI Semibold", Font.BOLD, 30);
    private final Font subtitleFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font statValueFont = new Font("Segoe UI Semibold", Font.BOLD, 18);
    private final Font statLabelFont = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font toolbarFont = new Font("Segoe UI Semibold", Font.BOLD, 14);
    private final Font tableHeaderFont = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private final Font tableFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font kanaFont = new Font("Yu Gothic UI", Font.BOLD, 24);
    private final Font romajiFont = new Font("Segoe UI Semibold", Font.BOLD, 15);
    private final Font chapterFont = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private final Font starFont = new Font("Segoe UI Symbol", Font.PLAIN, 22);
    private final Font learnedFont = new Font("Segoe UI Symbol", Font.PLAIN, 18);
    private final Font controlTitleFont = new Font("Segoe UI Semibold", Font.BOLD, 13);

    public ShowVocabFrame(AppNavigator navigator, List<Vocabulary> vocabularies) {
        this.navigator = navigator;
        this.appContext = navigator.getAppContext();
        this.vocabularies = vocabularies == null ? List.of() : vocabularies;
        this.darkMode = appContext.isDarkMode();

        appContext.addThemeListener(isDark -> SwingUtilities.invokeLater(() -> refreshTheme(isDark)));

        setupFrame();
        configureTheme(darkMode);
        setupUI("", false, false, false, false, false, false, false, false);
    }

    private void setupFrame() {
        setTitle("Danh sách từ vựng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setMinimumSize(new Dimension(900, 640));
        setResizable(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void configureTheme(boolean dark) {
        darkMode = dark;
        background = UITheme.getBackground(dark);
        panelBackground = UITheme.getPanelBackground(dark);
        cardBackground = UITheme.getCardBackground(dark);
        border = UITheme.getBorder(dark);
        hover = UITheme.getHover(dark);
        pressed = UITheme.getPressed(dark);
        titleForeground = UITheme.getTitleForeground(dark);
        textForeground = UITheme.getTextForeground(dark);
        accent = UITheme.getAccent(dark);
        glow = UITheme.getGlow(dark);
        mutedText = dark ? new Color(0x94A3B8) : new Color(0x64748B);
        tableStripe = dark ? new Color(0x233047) : new Color(0xEEF4FF);
        tableSelection = dark ? new Color(0x1E40AF) : new Color(0xBFDBFE);
        favoriteOn = dark ? new Color(0xFDBA74) : new Color(0xEA580C);
        favoriteOff = dark ? new Color(0x64748B) : new Color(0x94A3B8);
        learnedOn = dark ? new Color(0x86EFAC) : new Color(0x15803D);
        learnedOff = dark ? new Color(0x64748B) : new Color(0x94A3B8);
        controlCollapsed = dark ? new Color(0x172033) : new Color(0xE6EEF9);
    }

    private void refreshTheme(boolean dark) {
        String currentSearch = searchField == null ? "" : searchField.getText();
        boolean favoritesOnly = favoritesOnlyToggle != null && favoritesOnlyToggle.isSelected();
        boolean learnedOnly = learnedOnlyToggle != null && learnedOnlyToggle.isSelected();
        boolean unlearnedOnly = unlearnedOnlyToggle != null && unlearnedOnlyToggle.isSelected();
        boolean chapter1Only = chapter1Toggle != null && chapter1Toggle.isSelected();
        boolean chapter2Only = chapter2Toggle != null && chapter2Toggle.isSelected();
        boolean chapter3Only = chapter3Toggle != null && chapter3Toggle.isSelected();
        configureTheme(dark);
        if (uiBuilt) {
            setupUI(currentSearch, favoritesOnly, learnedOnly, unlearnedOnly,
                    chapter1Only, chapter2Only, chapter3Only, searchExpanded, filterExpanded);
            revalidate();
        }
        repaint();
    }

    private void setupUI(String searchText, boolean favoritesOnly, boolean learnedOnly, boolean unlearnedOnly,
                         boolean chapter1Only, boolean chapter2Only, boolean chapter3Only,
                         boolean searchExpandedState, boolean filterExpandedState) {
        this.searchExpanded = searchExpandedState;
        this.filterExpanded = filterExpandedState;

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(28, 32, 28, 32));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTablePanel(searchText, favoritesOnly, learnedOnly, unlearnedOnly,
                chapter1Only, chapter2Only, chapter3Only), BorderLayout.CENTER);

        uiBuilt = true;
        applyFilter();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Danh sách từ vựng");
        title.setFont(titleFont);
        title.setForeground(titleForeground);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Tổng hợp từ chapter 1-3 trong sách Dekiru Nihongo");
        subtitle.setFont(subtitleFont);
        subtitle.setForeground(mutedText);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(14));
        header.add(buildStatsRow());
        return header;
    }

    private JComponent buildControlsRow(String searchText, boolean favoritesOnly, boolean learnedOnly, boolean unlearnedOnly,
                                        boolean chapter1Only, boolean chapter2Only, boolean chapter3Only) {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.add(buildSearchPanel(searchText));
        row.add(buildQuickFilterPanel(favoritesOnly, learnedOnly, unlearnedOnly,
                chapter1Only, chapter2Only, chapter3Only));
        return row;
    }

    private JComponent buildSearchPanel(String searchText) {
        JPanel panel = new RoundedPanel(searchExpanded ? panelBackground : controlCollapsed, border, 18);
        panel.setLayout(new BorderLayout(0, searchExpanded ? 10 : 0));
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JButton headerButton = createSectionHeaderButton("Tìm kiếm", searchExpanded, () -> {
            searchExpanded = !searchExpanded;
            rebuildCurrentUI();
        });
        panel.add(headerButton, BorderLayout.NORTH);

        if (searchExpanded) {
            JPanel content = new JPanel(new BorderLayout(10, 0));
            content.setOpaque(false);

            searchField = new JTextField(searchText);
            searchField.setFont(tableFont);
            searchField.setForeground(titleForeground);
            searchField.setCaretColor(accent);
            searchField.setBackground(darkMode ? new Color(0x111827) : Color.WHITE);
            searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(border),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { applyFilter(); }
                @Override
                public void removeUpdate(DocumentEvent e) { applyFilter(); }
                @Override
                public void changedUpdate(DocumentEvent e) { applyFilter(); }
            });
            content.add(searchField, BorderLayout.CENTER);

            JButton clearButton = createTextButton("Đặt lại", false);
            clearButton.addActionListener(e -> searchField.setText(""));
            content.add(clearButton, BorderLayout.EAST);
            panel.add(content, BorderLayout.CENTER);
        } else {
            searchField = new JTextField(searchText);
        }

        return panel;
    }

    private JComponent buildQuickFilterPanel(boolean favoritesOnly, boolean learnedOnly, boolean unlearnedOnly,
                                             boolean chapter1Only, boolean chapter2Only, boolean chapter3Only) {
        JPanel panel = new RoundedPanel(filterExpanded ? panelBackground : controlCollapsed, border, 18);
        panel.setLayout(new BorderLayout(0, filterExpanded ? 10 : 0));
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JButton headerButton = createSectionHeaderButton("Lọc nhanh", filterExpanded, () -> {
            filterExpanded = !filterExpanded;
            rebuildCurrentUI();
        });
        panel.add(headerButton, BorderLayout.NORTH);

        if (filterExpanded) {
            JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            content.setOpaque(false);

            favoritesOnlyToggle = createToggleButton("Từ đã gắn sao", favoritesOnly);
            favoritesOnlyToggle.addActionListener(e -> applyFilter());
            content.add(favoritesOnlyToggle);

            learnedOnlyToggle = createToggleButton("Đã học", learnedOnly);
            learnedOnlyToggle.addActionListener(e -> {
                if (learnedOnlyToggle.isSelected()) {
                    unlearnedOnlyToggle.setSelected(false);
                }
                applyFilter();
            });
            content.add(learnedOnlyToggle);

            unlearnedOnlyToggle = createToggleButton("Chưa học", unlearnedOnly);
            unlearnedOnlyToggle.addActionListener(e -> {
                if (unlearnedOnlyToggle.isSelected()) {
                    learnedOnlyToggle.setSelected(false);
                }
                applyFilter();
            });
            content.add(unlearnedOnlyToggle);

            chapter1Toggle = createToggleButton("Chương 1", chapter1Only);
            chapter1Toggle.addActionListener(e -> applyFilter());
            content.add(chapter1Toggle);

            chapter2Toggle = createToggleButton("Chương 2", chapter2Only);
            chapter2Toggle.addActionListener(e -> applyFilter());
            content.add(chapter2Toggle);

            chapter3Toggle = createToggleButton("Chương 3", chapter3Only);
            chapter3Toggle.addActionListener(e -> applyFilter());
            content.add(chapter3Toggle);

            panel.add(content, BorderLayout.CENTER);
        } else {
            favoritesOnlyToggle = createToggleButton("Từ đã gắn sao", favoritesOnly);
            learnedOnlyToggle = createToggleButton("Đã học", learnedOnly);
            unlearnedOnlyToggle = createToggleButton("Chưa học", unlearnedOnly);
            chapter1Toggle = createToggleButton("Chương 1", chapter1Only);
            chapter2Toggle = createToggleButton("Chương 2", chapter2Only);
            chapter3Toggle = createToggleButton("Chương 3", chapter3Only);
        }

        return panel;
    }
    private JComponent buildStatsRow() {
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.add(new StatChip(String.valueOf(vocabularies.size()), "từ vựng", true));
        stats.add(new StatChip(String.valueOf(countFavoriteVocabs()), "đã gắn sao", false));
        stats.add(new StatChip(String.valueOf(countLearnedVocabs()), "đã học", false));
        return stats;
    }

    private long countFavoriteVocabs() {
        return vocabularies.stream()
                .filter(appContext::isFavoriteVocab)
                .count();
    }

    private long countLearnedVocabs() {
        return vocabularies.stream()
                .filter(appContext::isLearnedVocab)
                .count();
    }

    private JComponent buildTablePanel(String searchText, boolean favoritesOnly, boolean learnedOnly, boolean unlearnedOnly,
                                       boolean chapter1Only, boolean chapter2Only, boolean chapter3Only) {
        JPanel panel = new RoundedPanel(panelBackground, border, 22);
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JLabel sectionTitle = new JLabel("Tất cả từ vựng");
        sectionTitle.setFont(toolbarFont);
        sectionTitle.setForeground(titleForeground);
        toolbar.add(sectionTitle, BorderLayout.WEST);

        resultCountLabel = new JLabel();
        resultCountLabel.setFont(statLabelFont);
        resultCountLabel.setForeground(mutedText);
        toolbar.add(resultCountLabel, BorderLayout.EAST);

        top.add(toolbar);
        top.add(Box.createVerticalStrut(12));
        top.add(buildControlsRow(searchText, favoritesOnly, learnedOnly, unlearnedOnly, chapter1Only, chapter2Only, chapter3Only));

        panel.add(top, BorderLayout.NORTH);
        panel.add(createTableScrollPane(), BorderLayout.CENTER);
        panel.add(buildFooter(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JButton backButton = createTextButton("Quay lại", true);
        backButton.addActionListener(e -> navigator.navigateTo(AppRoute.Vocab));
        footer.add(backButton, BorderLayout.WEST);
        return footer;
    }

    private JScrollPane createTableScrollPane() {
        tableModel = new VocabularyTableModel(vocabularies);
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(false);
        table.setFillsViewportHeight(true);
        table.setRowHeight(58);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(tableFont);
        table.setForeground(textForeground);
        table.setBackground(panelBackground);
        table.setSelectionBackground(tableSelection);
        table.setSelectionForeground(titleForeground);
        table.setDefaultRenderer(Object.class, new VocabularyCellRenderer());
        table.getColumnModel().getColumn(0).setCellRenderer(new FavoriteCellRenderer());
        table.getColumnModel().getColumn(0).setCellEditor(new FavoriteCellEditor());
        table.getColumnModel().getColumn(1).setCellRenderer(new LearnedCellRenderer());
        table.getColumnModel().getColumn(1).setCellEditor(new LearnedCellEditor());

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setFont(tableHeaderFont);
        header.setForeground(darkMode ? new Color(0xE2E8F0) : new Color(0x0F172A));
        header.setBackground(darkMode ? new Color(0x172033) : new Color(0xDCE8F8));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, border));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));

        table.getColumnModel().getColumn(0).setPreferredWidth(66);
        table.getColumnModel().getColumn(0).setMaxWidth(76);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setMaxWidth(82);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(170);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setMaxWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(280);

        sorter = new TableRowSorter<>(tableModel);
        sorter.setSortable(0, false);
        sorter.setSortable(1, false);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(border));
        scrollPane.getViewport().setBackground(panelBackground);
        scrollPane.setBackground(panelBackground);
        return scrollPane;
    }

    private JButton createSectionHeaderButton(String text, boolean expanded, Runnable action) {
        JButton button = new JButton(text + (expanded ? "  -" : "  +"));
        button.setFont(controlTitleFont);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(titleForeground);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> action.run());
        return button;
    }

    private JButton createTextButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(primary ? Color.WHITE : titleForeground);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color fill = primary ? accent : (darkMode ? new Color(0x0F172A) : new Color(0xF0F9FF));
        Color hoverFill = primary ? glow : tableSelection;
        button.setUI(new RoundedButtonUI(fill, hoverFill, primary ? pressed : border));
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        return button;
    }

    private JToggleButton createToggleButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text, selected);
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setForeground(selected ? Color.WHITE : titleForeground);
        button.setBackground(selected ? accent : (darkMode ? new Color(0x0F172A) : new Color(0xF0F9FF)));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? accent : border),
                new EmptyBorder(8, 12, 8, 12)
        ));
        button.addChangeListener(e -> {
            boolean active = button.isSelected();
            button.setForeground(active ? Color.WHITE : titleForeground);
            button.setBackground(active ? accent : (darkMode ? new Color(0x0F172A) : new Color(0xF0F9FF)));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(active ? accent : border),
                    new EmptyBorder(8, 12, 8, 12)
            ));
        });
        return button;
    }

    private void rebuildCurrentUI() {
        String currentSearch = searchField == null ? "" : searchField.getText();
        boolean favoritesOnly = favoritesOnlyToggle != null && favoritesOnlyToggle.isSelected();
        boolean learnedOnly = learnedOnlyToggle != null && learnedOnlyToggle.isSelected();
        boolean unlearnedOnly = unlearnedOnlyToggle != null && unlearnedOnlyToggle.isSelected();
        boolean chapter1Only = chapter1Toggle != null && chapter1Toggle.isSelected();
        boolean chapter2Only = chapter2Toggle != null && chapter2Toggle.isSelected();
        boolean chapter3Only = chapter3Toggle != null && chapter3Toggle.isSelected();
        setupUI(currentSearch, favoritesOnly, learnedOnly, unlearnedOnly,
                chapter1Only, chapter2Only, chapter3Only, searchExpanded, filterExpanded);
        revalidate();
        repaint();
    }

    private void applyFilter() {
        if (sorter == null || table == null) {
            return;
        }

        String query = searchField == null ? "" : searchField.getText().trim();
        boolean favoritesOnly = favoritesOnlyToggle != null && favoritesOnlyToggle.isSelected();
        boolean learnedOnly = learnedOnlyToggle != null && learnedOnlyToggle.isSelected();
        boolean unlearnedOnly = unlearnedOnlyToggle != null && unlearnedOnlyToggle.isSelected();
        boolean chapter1Only = chapter1Toggle != null && chapter1Toggle.isSelected();
        boolean chapter2Only = chapter2Toggle != null && chapter2Toggle.isSelected();
        boolean chapter3Only = chapter3Toggle != null && chapter3Toggle.isSelected();

        RowFilter<VocabularyTableModel, Integer> searchFilter = query.isEmpty()
                ? null
                : RowFilter.regexFilter("(?iu)" + Pattern.quote(query), 2, 3, 4, 5);

        RowFilter<VocabularyTableModel, Integer> favoritesFilter = favoritesOnly
                ? new RowFilter<>() {
                    @Override
                    public boolean include(Entry<? extends VocabularyTableModel, ? extends Integer> entry) {
                        return Boolean.TRUE.equals(entry.getValue(0));
                    }
                }
                : null;

        RowFilter<VocabularyTableModel, Integer> learnedFilter = null;
        if (learnedOnly) {
            learnedFilter = new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends VocabularyTableModel, ? extends Integer> entry) {
                    return Boolean.TRUE.equals(entry.getValue(1));
                }
            };
        } else if (unlearnedOnly) {
            learnedFilter = new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends VocabularyTableModel, ? extends Integer> entry) {
                    return !Boolean.TRUE.equals(entry.getValue(1));
                }
            };
        }

        RowFilter<VocabularyTableModel, Integer> chapterFilter = (chapter1Only || chapter2Only || chapter3Only)
                ? new RowFilter<>() {
                    @Override
                    public boolean include(Entry<? extends VocabularyTableModel, ? extends Integer> entry) {
                        String chapterValue = String.valueOf(entry.getValue(4));
                        return (chapter1Only && "Chương 1".equals(chapterValue))
                                || (chapter2Only && "Chương 2".equals(chapterValue))
                                || (chapter3Only && "Chương 3".equals(chapterValue));
                    }
                }
                : null;

        java.util.ArrayList<RowFilter<VocabularyTableModel, Integer>> filters = new java.util.ArrayList<>();
        if (searchFilter != null) filters.add(searchFilter);
        if (favoritesFilter != null) filters.add(favoritesFilter);
        if (learnedFilter != null) filters.add(learnedFilter);
        if (chapterFilter != null) filters.add(chapterFilter);

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        updateResultCount();
    }
    private void updateResultCount() {
        if (resultCountLabel == null || table == null || tableModel == null) {
            return;
        }
        int visibleRows = table.getRowCount();
        int totalRows = tableModel.getRowCount();
        resultCountLabel.setText(String.format(Locale.ROOT, "%d/%d từ đang hiển thị", visibleRows, totalRows));
    }

    private String safeText(String value) {
        return isBlank(value) ? "-" : value.trim();
    }

    private String chapterText(Integer lesson) {
        if (lesson == null || lesson <= 0) {
            return "-";
        }
        return "Chương " + lesson;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private class VocabularyTableModel extends AbstractTableModel {
        private final String[] columns = {"Lưu ý", "Đã học", "Kana (hiragana/katana)", "Romaji", "Chapter", "Tiếng Việt"};
        private final List<Vocabulary> rows;

        VocabularyTableModel(List<Vocabulary> rows) {
            this.rows = rows == null ? List.of() : rows;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 0 || columnIndex == 1) ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0 || columnIndex == 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Vocabulary vocab = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> appContext.isFavoriteVocab(vocab);
                case 1 -> appContext.isLearnedVocab(vocab);
                case 2 -> safeText(vocab == null ? null : vocab.getKana());
                case 3 -> safeText(vocab == null ? null : vocab.getRomaji());
                case 4 -> chapterText(vocab == null ? null : vocab.getLesson());
                case 5 -> safeText(vocab == null ? null : vocab.getMeaning());
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= rows.size()) {
                return;
            }
            Vocabulary vocab = rows.get(rowIndex);
            if (columnIndex == 0) {
                appContext.setFavoriteVocab(vocab, Boolean.TRUE.equals(aValue));
            } else if (columnIndex == 1) {
                appContext.setLearnedVocab(vocab, Boolean.TRUE.equals(aValue));
            } else {
                return;
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    private class VocabularyCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int modelColumn = table.convertColumnIndexToModel(column);
            String displayValue = value == null ? "-" : value.toString();

            setText(displayValue);
            setToolTipText(displayValue);
            setOpaque(true);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setBackground(isSelected ? tableSelection : (row % 2 == 0 ? panelBackground : tableStripe));
            setForeground(modelColumn == 2 ? titleForeground : textForeground);

            if (modelColumn == 2) {
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(kanaFont);
            } else if (modelColumn == 3) {
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(romajiFont);
            } else if (modelColumn == 4) {
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(chapterFont);
                setForeground(accent);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(tableFont);
            }
            return this;
        }
    }

    private class FavoriteCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            boolean favorite = Boolean.TRUE.equals(value);
            setText(favorite ? "★" : "☆");
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(starFont);
            setToolTipText(favorite ? "Bỏ lưu ý" : "Lưu ý từ này");
            setOpaque(true);
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setBackground(isSelected ? tableSelection : (row % 2 == 0 ? panelBackground : tableStripe));
            setForeground(favorite ? favoriteOn : favoriteOff);
            return this;
        }
    }

    private class LearnedCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            boolean learned = Boolean.TRUE.equals(value);
            setText(learned ? "☑" : "☐");
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(learnedFont);
            setToolTipText(learned ? "Đánh dấu chưa học" : "Đánh dấu đã học");
            setOpaque(true);
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setBackground(isSelected ? tableSelection : (row % 2 == 0 ? panelBackground : tableStripe));
            setForeground(learned ? learnedOn : learnedOff);
            return this;
        }
    }

    private class FavoriteCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JButton button = new JButton();
        private boolean favorite;
        private int editingRow = -1;

        FavoriteCellEditor() {
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setFont(starFont);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> {
                if (editingRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(editingRow);
                    tableModel.setValueAt(!favorite, modelRow, 0);
                    fireEditingStopped();
                    SwingUtilities.invokeLater(() -> rebuildCurrentUI());
                }
            });
        }

        @Override
        public Object getCellEditorValue() {
            return !favorite;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            favorite = Boolean.TRUE.equals(value);
            editingRow = row;
            button.setText(favorite ? "★" : "☆");
            button.setForeground(favorite ? favoriteOn : favoriteOff);
            button.setBackground(isSelected ? tableSelection : (row % 2 == 0 ? panelBackground : tableStripe));
            return button;
        }
    }

    private class LearnedCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JButton button = new JButton();
        private boolean learned;
        private int editingRow = -1;

        LearnedCellEditor() {
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setFont(learnedFont);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> {
                if (editingRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(editingRow);
                    tableModel.setValueAt(!learned, modelRow, 1);
                    fireEditingStopped();
                    SwingUtilities.invokeLater(() -> rebuildCurrentUI());
                }
            });
        }

        @Override
        public Object getCellEditorValue() {
            return !learned;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            learned = Boolean.TRUE.equals(value);
            editingRow = row;
            button.setText(learned ? "☑" : "☐");
            button.setForeground(learned ? learnedOn : learnedOff);
            button.setBackground(isSelected ? tableSelection : (row % 2 == 0 ? panelBackground : tableStripe));
            return button;
        }
    }

    private class StatChip extends JPanel {
        private final String value;
        private final String label;
        private final boolean primary;

        StatChip(String value, String label, boolean primary) {
            this.value = value;
            this.label = label;
            this.primary = primary;
            setOpaque(false);
            setBorder(new EmptyBorder(10, 16, 10, 16));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(primary ? 138 : 128, 52);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color fill = primary ? accent : cardBackground;
            Color stroke = primary ? glow : border;
            g2.setColor(new Color(15, 23, 42, darkMode ? 28 : 10));
            g2.fillRoundRect(4, 6, w - 8, h - 8, 18, 18);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 8, h - 8, 18, 18);
            g2.setColor(stroke);
            g2.drawRoundRect(0, 0, w - 9, h - 9, 18, 18);

            g2.setFont(statValueFont);
            g2.setColor(primary ? Color.WHITE : titleForeground);
            FontMetrics valueMetrics = g2.getFontMetrics();
            int valueX = 16;
            int valueY = 22 + valueMetrics.getAscent() / 2;
            g2.drawString(value, valueX, valueY);

            g2.setFont(statLabelFont);
            g2.setColor(primary ? new Color(255, 255, 255, 220) : mutedText);
            g2.drawString(label, valueX + valueMetrics.stringWidth(value) + 8, valueY - 1);
            g2.dispose();
        }
    }

    private class GradientPanel extends JPanel {
        GradientPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            Color top = darkMode ? new Color(0x0F172A) : new Color(0xEAF2FF);
            Color bottom = darkMode ? new Color(0x111827) : new Color(0xF8F4EC);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            g2.fillRect(0, 0, w, h);

            if (!darkMode) {
                g2.setColor(new Color(255, 255, 255, 120));
                g2.fillOval(w - 240, -70, 280, 220);
                g2.setColor(new Color(191, 219, 254, 90));
                g2.fillOval(-120, h - 220, 320, 240);
            } else {
                g2.setColor(new Color(59, 130, 246, 28));
                g2.fillOval(w - 260, -80, 300, 240);
                g2.setColor(new Color(14, 165, 233, 18));
                g2.fillOval(-140, h - 240, 320, 260);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color fill;
        private final Color stroke;
        private final int arc;

        RoundedPanel(Color fill, Color stroke, int arc) {
            this.fill = fill;
            this.stroke = stroke;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(15, 23, 42, 16));
            g2.fillRoundRect(4, 6, w - 8, h - 8, arc, arc);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 8, h - 8, arc, arc);
            g2.setColor(stroke);
            g2.drawRoundRect(0, 0, w - 9, h - 9, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final Color fill;
        private final Color hoverFill;
        private final Color border;

        RoundedButtonUI(Color fill, Color hoverFill, Color border) {
            this.fill = fill;
            this.hoverFill = hoverFill;
            this.border = border;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton button = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = c.getWidth();
            int h = c.getHeight();
            Color current = button.getModel().isRollover() ? hoverFill : fill;
            g2.setColor(current);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g2.dispose();
            super.paint(g, c);
        }
    }
}




