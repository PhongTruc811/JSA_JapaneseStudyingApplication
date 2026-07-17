package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.MenuNavigator;
import com.mescode.japanese.app.navigation.MenuOptions;
import com.mescode.japanese.model.Kanji;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ShowKanjiFrame extends JFrame {
    private final MenuNavigator menuNavigator;
    private final AppContext appContext;
    private final List<Kanji> kanjis;
    private final KanjiTableModel tableModel;
    private final TableRowSorter<KanjiTableModel> sorter;
    private final JTextField searchField = new JTextField();
    private final JLabel visibleCountLabel = new JLabel();
    private final List<JToggleButton> unitButtons = new ArrayList<>();
    private final JTable table;
    private final JPanel rootPanel = new JPanel(new BorderLayout(0, 18));
    private final JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
    private final JScrollPane scrollPane;

    private boolean darkMode;
    private Color background;
    private Color panelBackground;
    private Color cardBackground;
    private Color border;
    private Color titleForeground;
    private Color textForeground;
    private Color accent;

    public ShowKanjiFrame(MenuNavigator menuNavigator, List<Kanji> kanjis) {
        this.menuNavigator = menuNavigator;
        this.appContext = menuNavigator.getAppContext();
        this.kanjis = kanjis == null ? Collections.emptyList() : new ArrayList<>(kanjis);
        this.tableModel = new KanjiTableModel(this.kanjis);
        this.sorter = new TableRowSorter<>(tableModel);
        this.table = new JTable(tableModel);
        this.scrollPane = new JScrollPane(table);

        setTitle("Danh sách Hán tự");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 620));
        setSize(1180, 760);
        setLocationRelativeTo(null);

        configureTheme(appContext.isDarkMode());
        buildUI();
        applyTheme();
        installListeners();
        applyFilters();
        appContext.addThemeListener(isDark -> SwingUtilities.invokeLater(() -> {
            configureTheme(isDark);
            applyTheme();
        }));
    }

    private void configureTheme(boolean dark) {
        darkMode = dark;
        background = UITheme.getBackground(dark);
        panelBackground = UITheme.getPanelBackground(dark);
        cardBackground = UITheme.getCardBackground(dark);
        border = UITheme.getBorder(dark);
        titleForeground = UITheme.getTitleForeground(dark);
        textForeground = UITheme.getTextForeground(dark);
        accent = UITheme.getAccent(dark);
    }

    private void buildUI() {
        rootPanel.setBorder(new EmptyBorder(24, 32, 22, 32));
        setContentPane(rootPanel);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(buildHeader());
        top.add(Box.createVerticalStrut(18));
        top.add(buildControls());
        rootPanel.add(top, BorderLayout.NORTH);

        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(16, 18, 18, 18)));
        tablePanel.add(buildTableHeader(), BorderLayout.NORTH);
        configureTable();
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        rootPanel.add(tablePanel, BorderLayout.CENTER);
        rootPanel.add(buildFooter(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = namedLabel("Danh sách Hán tự", "pageTitle");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 30));
        JLabel subtitle = namedLabel(
                "Hán Việt, Kanji, Hiragana và ý nghĩa từ Unit 1 đến Unit 3.",
                "pageSubtitle");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setBorder(new EmptyBorder(5, 0, 0, 0));
        text.add(title);
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);

        JLabel stat = namedLabel(kanjis.size() + " Hán tự", "statChip");
        stat.setOpaque(true);
        stat.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        stat.setBorder(new EmptyBorder(10, 16, 10, 16));
        header.add(stat, BorderLayout.EAST);
        return header;
    }

    private JLabel namedLabel(String text, String name) {
        JLabel label = new JLabel(text);
        label.setName(name);
        return label;
    }

    private JComponent buildControls() {
        JPanel controls = new JPanel(new BorderLayout(20, 0));
        controls.setOpaque(false);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        search.setOpaque(false);
        JLabel searchLabel = namedLabel("Tìm kiếm", "controlLabel");
        searchField.setPreferredSize(new Dimension(300, 38));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton resetButton = new JButton("Đặt lại");
        resetButton.setName("commandButton");
        resetButton.setFocusPainted(false);
        resetButton.setPreferredSize(new Dimension(84, 38));
        resetButton.addActionListener(event -> resetFilters());
        search.add(searchLabel);
        search.add(searchField);
        search.add(resetButton);
        controls.add(search, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setOpaque(false);
        filters.add(namedLabel("Lọc nhanh", "controlLabel"));
        for (int unit = 1; unit <= 3; unit++) {
            JToggleButton button = new JToggleButton("Unit " + unit);
            button.setActionCommand(String.valueOf(unit));
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(78, 38));
            button.addActionListener(event -> applyFilters());
            unitButtons.add(button);
            filters.add(button);
        }
        controls.add(filters, BorderLayout.EAST);
        return controls;
    }

    private JComponent buildTableHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = namedLabel("Tất cả Hán tự", "sectionTitle");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        visibleCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        header.add(title, BorderLayout.WEST);
        header.add(visibleCountLabel, BorderLayout.EAST);
        return header;
    }

    private void configureTable() {
        table.setRowSorter(sorter);
        table.setRowHeight(50);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setGridColor(border);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(1, 44));
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(0).setMaxWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(210);
        table.getColumnModel().getColumn(2).setPreferredWidth(190);
        table.getColumnModel().getColumn(3).setPreferredWidth(220);
        table.getColumnModel().getColumn(4).setPreferredWidth(300);
        table.setDefaultRenderer(Object.class, new KanjiCellRenderer());
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        JButton backButton = new JButton("←  Quay lại");
        backButton.setName("commandButton");
        backButton.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(120, 40));
        backButton.addActionListener(event -> menuNavigator.navigateTo(MenuOptions.Vocab));
        footer.add(backButton);
        return footer;
    }

    private void installListeners() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) { applyFilters(); }

            @Override
            public void removeUpdate(DocumentEvent event) { applyFilters(); }

            @Override
            public void changedUpdate(DocumentEvent event) { applyFilters(); }
        });
    }

    private void applyFilters() {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<Integer> selectedUnits = unitButtons.stream()
                .filter(AbstractButton::isSelected)
                .map(button -> Integer.parseInt(button.getActionCommand()))
                .toList();

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends KanjiTableModel, ? extends Integer> entry) {
                Kanji kanji = tableModel.getKanjiAt(entry.getIdentifier());
                boolean unitMatches = selectedUnits.isEmpty() || selectedUnits.contains(kanji.getUnit());
                boolean textMatches = query.isEmpty()
                        || contains(kanji.getHanViet(), query)
                        || contains(kanji.getKanji(), query)
                        || contains(kanji.getHiragana(), query)
                        || contains(kanji.getMeaning(), query);
                return unitMatches && textMatches;
            }
        });
        visibleCountLabel.setText(sorter.getViewRowCount() + "/" + kanjis.size() + " mục đang hiển thị");
        styleFilterButtons();
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private void resetFilters() {
        searchField.setText("");
        unitButtons.forEach(button -> button.setSelected(false));
        applyFilters();
    }

    private void applyTheme() {
        rootPanel.setBackground(background);
        tablePanel.setBackground(panelBackground);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(16, 18, 18, 18)));
        scrollPane.getViewport().setBackground(panelBackground);
        scrollPane.setBorder(BorderFactory.createLineBorder(border));
        searchField.setBackground(darkMode ? new Color(0x111827) : Color.WHITE);
        searchField.setForeground(titleForeground);
        searchField.setCaretColor(titleForeground);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border), new EmptyBorder(7, 10, 7, 10)));

        colorNamedComponents(rootPanel);
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(darkMode ? new Color(0x172033) : new Color(0xE0F2FE));
        tableHeader.setForeground(titleForeground);
        table.setForeground(titleForeground);
        table.setBackground(panelBackground);
        table.setSelectionBackground(darkMode ? new Color(0x1D4ED8) : new Color(0xBAE6FD));
        table.setSelectionForeground(darkMode ? Color.WHITE : new Color(0x0F172A));
        styleFilterButtons();
        repaint();
    }

    private void colorNamedComponents(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel label) {
                if ("pageTitle".equals(label.getName()) || "sectionTitle".equals(label.getName())) {
                    label.setForeground(titleForeground);
                } else if ("statChip".equals(label.getName())) {
                    label.setBackground(accent);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setForeground(textForeground);
                }
            }
            if (component instanceof JButton button) {
                button.setBackground(cardBackground);
                button.setForeground(textForeground);
                button.setBorder(BorderFactory.createLineBorder(border));
            }
            if (component instanceof Container child) {
                colorNamedComponents(child);
            }
        }
    }

    private void styleFilterButtons() {
        for (JToggleButton button : unitButtons) {
            button.setBackground(button.isSelected() ? accent : cardBackground);
            button.setForeground(button.isSelected() ? Color.WHITE : textForeground);
            button.setBorder(BorderFactory.createLineBorder(button.isSelected() ? accent : border));
        }
    }

    private class KanjiCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable source, Object value, boolean selected, boolean focused, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    source, value, selected, focused, row, column);
            label.setBorder(new EmptyBorder(0, 14, 0, 14));
            if (!selected) {
                label.setBackground(row % 2 == 0
                        ? panelBackground
                        : (darkMode ? new Color(0x243248) : new Color(0xF0F9FF)));
                label.setForeground(titleForeground);
            }
            label.setHorizontalAlignment(
                    column == 0 || column == 2 || column == 3
                            ? SwingConstants.CENTER : SwingConstants.LEFT);
            if (column == 2) {
                label.setFont(new Font("Yu Gothic UI", Font.BOLD, 22));
            } else if (column == 3) {
                label.setFont(new Font("Yu Gothic UI", Font.PLAIN, 17));
            } else {
                label.setFont(new Font("Segoe UI", column == 0 ? Font.BOLD : Font.PLAIN, 15));
            }
            return label;
        }
    }

    private static class KanjiTableModel extends AbstractTableModel {
        private final String[] columns = {"Unit", "Hán Việt", "Kanji", "Hiragana", "Ý nghĩa"};
        private final List<Kanji> rows;

        KanjiTableModel(List<Kanji> rows) { this.rows = rows; }
        Kanji getKanjiAt(int row) { return rows.get(row); }

        @Override
        public int getRowCount() { return rows.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Kanji kanji = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> "Unit " + kanji.getUnit();
                case 1 -> kanji.getHanViet();
                case 2 -> kanji.getKanji();
                case 3 -> kanji.getHiragana();
                case 4 -> kanji.getMeaning();
                default -> "";
            };
        }
    }
}