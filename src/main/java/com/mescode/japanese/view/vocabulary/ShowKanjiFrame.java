package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;
import com.mescode.japanese.model.kanji.Kanji;
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

public class ShowKanjiFrame extends JFrame {
    private final AppNavigator navigator;
    private final AppContext appContext;
    private final List<Kanji> kanjis;

    private boolean darkMode;
    private boolean uiBuilt;
    private boolean searchExpanded;
    private boolean filterExpanded;
    private Color background, panelBackground, cardBackground, border, hover, pressed;
    private Color titleForeground, textForeground, accent, glow, mutedText, tableStripe, tableSelection, controlCollapsed;

    private JTable table;
    private KanjiTableModel tableModel;
    private TableRowSorter<KanjiTableModel> sorter;
    private JTextField searchField;
    private JLabel resultCountLabel;
    private JToggleButton unit1Toggle, unit2Toggle, unit3Toggle;

    private final Font titleFont = new Font("Segoe UI Semibold", Font.BOLD, 30);
    private final Font subtitleFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font statValueFont = new Font("Segoe UI Semibold", Font.BOLD, 18);
    private final Font statLabelFont = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font toolbarFont = new Font("Segoe UI Semibold", Font.BOLD, 14);
    private final Font tableHeaderFont = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private final Font tableFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font kanjiFont = new Font("Yu Gothic UI", Font.BOLD, 24);
    private final Font hanVietFont = new Font("Segoe UI Semibold", Font.BOLD, 15);
    private final Font hiraganaFont = new Font("Yu Gothic UI", Font.PLAIN, 17);
    private final Font unitFont = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private final Font controlTitleFont = new Font("Segoe UI Semibold", Font.BOLD, 13);

    public ShowKanjiFrame(AppNavigator navigator, List<Kanji> kanjis) {
        this.navigator = navigator;
        this.appContext = navigator.getAppContext();
        this.kanjis = kanjis == null ? List.of() : List.copyOf(kanjis);
        darkMode = appContext.isDarkMode();
        appContext.addThemeListener(isDark -> SwingUtilities.invokeLater(() -> refreshTheme(isDark)));
        setupFrame();
        configureTheme(darkMode);
        setupUI("", false, false, false, false, false);
    }

    private void setupFrame() {
        setTitle("Danh sách Hán tự");
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
        controlCollapsed = dark ? new Color(0x172033) : new Color(0xE6EEF9);
    }

    private void refreshTheme(boolean dark) {
        String query = searchField == null ? "" : searchField.getText();
        configureTheme(dark);
        setupUI(query, isSelected(unit1Toggle), isSelected(unit2Toggle), isSelected(unit3Toggle),
                searchExpanded, filterExpanded, true);
        revalidate();
        repaint();
    }

    private boolean isSelected(AbstractButton button) { return button != null && button.isSelected(); }

    private void setupUI(String searchText, boolean unit1, boolean unit2, boolean unit3,
                         boolean searchExpandedState, boolean filterExpandedState) {
        setupUI(searchText, unit1, unit2, unit3, searchExpandedState, filterExpandedState, false);
    }

    private void setupUI(String searchText, boolean unit1, boolean unit2, boolean unit3,
                         boolean searchExpandedState, boolean filterExpandedState, boolean themeRefresh) {
        searchExpanded = searchExpandedState;
        filterExpanded = filterExpandedState;
        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(28, 32, 28, 32));
        setContentPane(root);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTablePanel(searchText, unit1, unit2, unit3), BorderLayout.CENTER);
        uiBuilt = true;
        applyFilter();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Danh sách Hán tự");
        title.setFont(titleFont); title.setForeground(titleForeground); title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Hán Việt, Kanji, Hiragana và ý nghĩa từ Unit 1 đến Unit 3.");
        subtitle.setFont(subtitleFont); subtitle.setForeground(mutedText); subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title); header.add(Box.createVerticalStrut(4)); header.add(subtitle);
        header.add(Box.createVerticalStrut(14)); header.add(buildStatsRow());
        return header;
    }

    private JComponent buildStatsRow() {
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        stats.setOpaque(false); stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.add(new StatChip(String.valueOf(kanjis.size()), "Hán tự", true));
        return stats;
    }

    private JComponent buildTablePanel(String searchText, boolean unit1, boolean unit2, boolean unit3) {
        JPanel panel = new RoundedPanel(panelBackground, border, 22);
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel top = new JPanel(); top.setOpaque(false); top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JPanel toolbar = new JPanel(new BorderLayout(12, 0)); toolbar.setOpaque(false);
        JLabel sectionTitle = new JLabel("Tất cả Hán tự"); sectionTitle.setFont(toolbarFont); sectionTitle.setForeground(titleForeground);
        resultCountLabel = new JLabel(); resultCountLabel.setFont(statLabelFont); resultCountLabel.setForeground(mutedText);
        toolbar.add(sectionTitle, BorderLayout.WEST); toolbar.add(resultCountLabel, BorderLayout.EAST);
        top.add(toolbar); top.add(Box.createVerticalStrut(12));
        top.add(buildControlsRow(searchText, unit1, unit2, unit3));
        panel.add(top, BorderLayout.NORTH); panel.add(createTableScrollPane(), BorderLayout.CENTER); panel.add(buildFooter(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildControlsRow(String searchText, boolean unit1, boolean unit2, boolean unit3) {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0)); row.setOpaque(false);
        row.add(buildSearchPanel(searchText)); row.add(buildQuickFilterPanel(unit1, unit2, unit3));
        return row;
    }

    private JComponent buildSearchPanel(String searchText) {
        JPanel panel = new RoundedPanel(searchExpanded ? panelBackground : controlCollapsed, border, 18);
        panel.setLayout(new BorderLayout(0, searchExpanded ? 10 : 0)); panel.setBorder(new EmptyBorder(12, 14, 12, 14));
        panel.add(createSectionHeaderButton("Tìm kiếm", searchExpanded, () -> { searchExpanded = !searchExpanded; rebuildCurrentUI(); }), BorderLayout.NORTH);
        if (searchExpanded) {
            JPanel content = new JPanel(new BorderLayout(10, 0)); content.setOpaque(false);
            searchField = new JTextField(searchText); searchField.setFont(tableFont); searchField.setForeground(titleForeground); searchField.setCaretColor(accent);
            searchField.setBackground(darkMode ? new Color(0x111827) : Color.WHITE);
            searchField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border), new EmptyBorder(8, 10, 8, 10)));
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { applyFilter(); }
                public void removeUpdate(DocumentEvent e) { applyFilter(); }
                public void changedUpdate(DocumentEvent e) { applyFilter(); }
            });
            content.add(searchField, BorderLayout.CENTER);
            JButton clear = createTextButton("Đặt lại", false); clear.addActionListener(e -> searchField.setText("") ); content.add(clear, BorderLayout.EAST);
            panel.add(content, BorderLayout.CENTER);
        } else { searchField = new JTextField(searchText); }
        return panel;
    }

    private JComponent buildQuickFilterPanel(boolean unit1, boolean unit2, boolean unit3) {
        JPanel panel = new RoundedPanel(filterExpanded ? panelBackground : controlCollapsed, border, 18);
        panel.setLayout(new BorderLayout(0, filterExpanded ? 10 : 0)); panel.setBorder(new EmptyBorder(12, 14, 12, 14));
        panel.add(createSectionHeaderButton("Lọc nhanh", filterExpanded, () -> { filterExpanded = !filterExpanded; rebuildCurrentUI(); }), BorderLayout.NORTH);
        unit1Toggle = createToggleButton("Unit 1", unit1); unit2Toggle = createToggleButton("Unit 2", unit2); unit3Toggle = createToggleButton("Unit 3", unit3);
        unit1Toggle.addActionListener(e -> applyFilter()); unit2Toggle.addActionListener(e -> applyFilter()); unit3Toggle.addActionListener(e -> applyFilter());
        if (filterExpanded) {
            JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); content.setOpaque(false);
            content.add(unit1Toggle); content.add(unit2Toggle); content.add(unit3Toggle); panel.add(content, BorderLayout.CENTER);
        }
        return panel;
    }

    private JScrollPane createTableScrollPane() {
        tableModel = new KanjiTableModel(kanjis); table = new JTable(tableModel); table.setAutoCreateRowSorter(false);
        table.setFillsViewportHeight(true); table.setRowHeight(58); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setFont(tableFont); table.setForeground(textForeground); table.setBackground(panelBackground); table.setSelectionBackground(tableSelection); table.setSelectionForeground(titleForeground);
        table.setDefaultRenderer(Object.class, new KanjiCellRenderer());
        JTableHeader header = table.getTableHeader(); header.setReorderingAllowed(false); header.setResizingAllowed(true); header.setFont(tableHeaderFont); header.setForeground(darkMode ? new Color(0xE2E8F0) : new Color(0x0F172A)); header.setBackground(darkMode ? new Color(0x172033) : new Color(0xDCE8F8)); header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, border)); header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        int[] widths = {90, 190, 180, 190, 300}; for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        sorter = new TableRowSorter<>(tableModel); table.setRowSorter(sorter);
        JScrollPane scroll = new JScrollPane(table); scroll.setBorder(BorderFactory.createLineBorder(border)); scroll.getViewport().setBackground(panelBackground); scroll.setBackground(panelBackground); return scroll;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false);
        JButton back = createTextButton("Quay lại", true); back.addActionListener(e -> navigator.navigateTo(AppRoute.Vocab)); footer.add(back, BorderLayout.WEST); return footer;
    }

    private JButton createSectionHeaderButton(String text, boolean expanded, Runnable action) {
        JButton button = new JButton(text + (expanded ? "  -" : "  +")); button.setFont(controlTitleFont); button.setHorizontalAlignment(SwingConstants.LEFT); button.setFocusPainted(false); button.setBorderPainted(false); button.setContentAreaFilled(false); button.setOpaque(false); button.setForeground(titleForeground); button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); button.addActionListener(e -> action.run()); return button;
    }

    private JButton createTextButton(String text, boolean primary) {
        JButton button = new JButton(text); button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12)); button.setFocusPainted(false); button.setBorderPainted(false); button.setContentAreaFilled(false); button.setOpaque(false); button.setForeground(primary ? Color.WHITE : titleForeground); button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); button.setUI(new RoundedButtonUI(primary ? accent : (darkMode ? new Color(0x0F172A) : new Color(0xF0F9FF)), primary ? glow : tableSelection, primary ? pressed : border)); button.setBorder(new EmptyBorder(8, 12, 8, 12)); return button;
    }

    private JToggleButton createToggleButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text, selected); button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12)); button.setFocusPainted(false); button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(selected ? accent : border), new EmptyBorder(8, 12, 8, 12))); updateToggleStyle(button);
        button.addChangeListener(e -> updateToggleStyle(button)); return button;
    }

    private void updateToggleStyle(AbstractButton button) { boolean active = button.isSelected(); button.setForeground(active ? Color.WHITE : titleForeground); button.setBackground(active ? accent : (darkMode ? new Color(0x0F172A) : new Color(0xF0F9FF))); button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(active ? accent : border), new EmptyBorder(8, 12, 8, 12))); }

    private void rebuildCurrentUI() {
        setupUI(searchField == null ? "" : searchField.getText(), isSelected(unit1Toggle), isSelected(unit2Toggle), isSelected(unit3Toggle), searchExpanded, filterExpanded);
        revalidate(); repaint();
    }

    private void applyFilter() {
        if (sorter == null || table == null) return;
        String query = searchField == null ? "" : searchField.getText().trim();
        boolean u1 = isSelected(unit1Toggle), u2 = isSelected(unit2Toggle), u3 = isSelected(unit3Toggle);
        RowFilter<KanjiTableModel, Integer> search = query.isEmpty() ? null : RowFilter.regexFilter("(?iu)" + Pattern.quote(query), 0, 1, 2, 3, 4);
        RowFilter<KanjiTableModel, Integer> units = (u1 || u2 || u3) ? new RowFilter<>() { public boolean include(Entry<? extends KanjiTableModel, ? extends Integer> e) { int unit = kanjis.get(e.getIdentifier()).getUnit(); return (u1 && unit == 1) || (u2 && unit == 2) || (u3 && unit == 3); } } : null;
        java.util.ArrayList<RowFilter<KanjiTableModel, Integer>> filters = new java.util.ArrayList<>(); if (search != null) filters.add(search); if (units != null) filters.add(units);
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        if (resultCountLabel != null) resultCountLabel.setText(String.format(Locale.ROOT, "%d/%d Hán tự đang hiển thị", table.getRowCount(), tableModel.getRowCount()));
    }

    private String safeText(String value) { return value == null || value.trim().isEmpty() ? "-" : value.trim(); }

    private class KanjiTableModel extends AbstractTableModel {
        private final String[] columns = {"Unit", "Hán Việt", "Kanji", "Hiragana", "Ý nghĩa"}; private final List<Kanji> rows;
        KanjiTableModel(List<Kanji> rows) { this.rows = rows; }
        public int getRowCount() { return rows.size(); } public int getColumnCount() { return columns.length; } public String getColumnName(int c) { return columns[c]; }
        public Object getValueAt(int r, int c) { Kanji k = rows.get(r); return switch (c) { case 0 -> "Unit " + k.getUnit(); case 1 -> safeText(k.getHanViet()); case 2 -> safeText(k.getKanji()); case 3 -> safeText(k.getHiragana()); case 4 -> safeText(k.getMeaning()); default -> ""; }; }
    }

    private class KanjiCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable source, Object value, boolean selected, boolean focused, int row, int column) {
            super.getTableCellRendererComponent(source, value, selected, focused, row, column); int modelColumn = source.convertColumnIndexToModel(column); setText(value == null ? "-" : value.toString()); setToolTipText(getText()); setOpaque(true); setBorder(new EmptyBorder(0, 14, 0, 14)); setBackground(selected ? tableSelection : (row % 2 == 0 ? panelBackground : tableStripe)); setForeground(modelColumn == 2 ? titleForeground : textForeground);
            if (modelColumn == 2) { setHorizontalAlignment(SwingConstants.CENTER); setFont(kanjiFont); } else if (modelColumn == 1) { setHorizontalAlignment(SwingConstants.LEFT); setFont(hanVietFont); } else if (modelColumn == 3) { setHorizontalAlignment(SwingConstants.CENTER); setFont(hiraganaFont); } else if (modelColumn == 0) { setHorizontalAlignment(SwingConstants.CENTER); setFont(unitFont); setForeground(accent); } else { setHorizontalAlignment(SwingConstants.LEFT); setFont(tableFont); } return this;
        }
    }

    private class StatChip extends JPanel {
        private final String value, label; private final boolean primary;
        StatChip(String value, String label, boolean primary) { this.value = value; this.label = label; this.primary = primary; setOpaque(false); setBorder(new EmptyBorder(10, 16, 10, 16)); }
        public Dimension getPreferredSize() { return new Dimension(138, 52); }
        protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w = getWidth(), h = getHeight(); g2.setColor(new Color(15, 23, 42, darkMode ? 28 : 10)); g2.fillRoundRect(4, 6, w - 8, h - 8, 18, 18); g2.setColor(primary ? accent : cardBackground); g2.fillRoundRect(0, 0, w - 8, h - 8, 18, 18); g2.setColor(primary ? glow : border); g2.drawRoundRect(0, 0, w - 9, h - 9, 18, 18); g2.setFont(statValueFont); g2.setColor(primary ? Color.WHITE : titleForeground); FontMetrics fm = g2.getFontMetrics(); int x = 16, y = 22 + fm.getAscent() / 2; g2.drawString(value, x, y); g2.setFont(statLabelFont); g2.setColor(primary ? new Color(255, 255, 255, 220) : mutedText); g2.drawString(label, x + fm.stringWidth(value) + 8, y - 1); g2.dispose(); }
    }

    private class GradientPanel extends JPanel { GradientPanel() { setOpaque(false); } protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w = getWidth(), h = getHeight(); g2.setPaint(new GradientPaint(0, 0, darkMode ? new Color(0x0F172A) : new Color(0xEAF2FF), 0, h, darkMode ? new Color(0x111827) : new Color(0xF8F4EC))); g2.fillRect(0, 0, w, h); g2.setColor(darkMode ? new Color(59, 130, 246, 28) : new Color(255, 255, 255, 120)); g2.fillOval(w - 260, -80, 300, 240); g2.setColor(darkMode ? new Color(14, 165, 233, 18) : new Color(191, 219, 254, 90)); g2.fillOval(-140, h - 240, 320, 260); g2.dispose(); super.paintComponent(g); } }

    private static class RoundedPanel extends JPanel { private final Color fill, stroke; private final int arc; RoundedPanel(Color fill, Color stroke, int arc) { this.fill = fill; this.stroke = stroke; this.arc = arc; setOpaque(false); } protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w = getWidth(), h = getHeight(); g2.setColor(new Color(15, 23, 42, 16)); g2.fillRoundRect(4, 6, w - 8, h - 8, arc, arc); g2.setColor(fill); g2.fillRoundRect(0, 0, w - 8, h - 8, arc, arc); g2.setColor(stroke); g2.drawRoundRect(0, 0, w - 9, h - 9, arc, arc); g2.dispose(); super.paintComponent(g); } }

    private static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI { private final Color fill, hoverFill, border; RoundedButtonUI(Color fill, Color hoverFill, Color border) { this.fill = fill; this.hoverFill = hoverFill; this.border = border; } public void paint(Graphics g, JComponent c) { AbstractButton b = (AbstractButton) c; Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w = c.getWidth(), h = c.getHeight(); g2.setColor(b.getModel().isRollover() ? hoverFill : fill); g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16); g2.setColor(border); g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16); g2.dispose(); super.paint(g, c); } }
}
