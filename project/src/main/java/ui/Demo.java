package ui;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import search.index.AbstractDocumentBuilder;
import search.index.AbstractIndex;
import search.index.AbstractIndexBuilder;
import search.index.impl.DocumentBuilder;
import search.index.impl.Index;
import search.index.impl.IndexBuilder;
import search.query.AbstractHit;
import search.query.AbstractIndexSearcher;
import search.query.Sort;
import search.query.impl.*;
import search.util.Config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Demo extends JFrame {
    JLabel titleLabel;
    JLabel instructionLabel;
    private JRadioButton rb1;
    private JRadioButton rb2;
    private JRadioButton rb3;
    JButton jumpBtn;
    ButtonGroup group;
    JPanel indexGeneratingPanel;
    JPanel searchingPanel;
    JPanel consistingPanelForIGPanel;
    JPanel jumpBtnPanel;
    JPanel cards;
    CardLayout cl;

//    private JPanel resultsPanel;

    private JPanel fileChooserPanel = new JPanel();
    private JLabel fileChooserLabel = new JLabel("所选文件路径：");
    private JTextField fileChooserJtf = new JTextField(25);
    private JButton fileChooserButton=new JButton("浏览");

    private JTextField searchField;
    private JButton searchBtn;
    private JButton SettingButton;
    private JButton exitBtnOnP1;
    private JButton exitBtnOnP2;
    private JButton pageFirstBtn;
    private JButton pageLastBtn;

    private JTable resultTable;
    private Object[][] tableDate;
    private DefaultTableModel tableModel;
    private String[] keywords;

    private JButton keywordUpBtn;
    private JButton keywordDownBtn;
    private JPanel searchAreaPanel;
    private JPanel scrollAreaPanel;
    private JLabel pageScrollLabel;
    private JLabel keywordScrollLabel;
    private JButton backBtn;
    private JButton pageBackBtn;
    private JButton pageForwardBtn;

    private int currentHitIndex;
    private int currentDocIndex;
    private List<Integer> currentKeywordPositions;
    private int keywordIndex;
    private boolean previewOver;
    private int docNum;

    String indexFileString = Config.INDEX_DIR + "index.dat";
    String rootDir;
    AbstractDocumentBuilder documentBuilder;
    AbstractIndexBuilder indexBuilder;
    AbstractIndex index;
    Sort usedSorter;
    Sort simpleSorter;
    Sort filenameSorter;
    Sort lengthSorter;
    Sort timeSorter;
    AbstractIndexSearcher searcher;
    AbstractHit[] hits;
    AbstractHit currentHit;

    private void updateFile() {
        keywordIndex = 0;
        currentHit = hits[currentHitIndex];
        currentDocIndex = currentHit.getDocId();
        currentKeywordPositions = currentHit.getAllPositions();
        String filePath = index.getDocName(currentDocIndex);
        Path path = Paths.get(filePath);
        File file = new File(filePath);
        try {
            // 文件名
            tableModel.setValueAt(file.getName(), 0, 1);
            // 最后修改时间
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime lastModifiedTime = attrs.lastModifiedTime();
            ZonedDateTime lastModifiedDateTime = lastModifiedTime.toInstant().atZone(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = lastModifiedDateTime.format(formatter);
            tableModel.setValueAt(formattedDateTime, 4, 1);
            // 文件大小
            tableModel.setValueAt(attrs.size() + "B", 3, 1);
            // 关键字出现位置
            String keywordPosString = currentHit.getAllPositionsAsString();
            tableModel.setValueAt(keywordPosString, 2, 1);
            // 内容预览
            String contentPreview = sliceTextSimplified(currentHit.getContent(), keywordIndex);
            tableModel.setValueAt(contentPreview, 1, 1);
            keywords = currentHit.getAllTerms();
            resultTable.getColumnModel().getColumn(1).setCellRenderer(new FinalHighlightTableCellRenderer(keywords));
            // 结果数量展示
            pageScrollLabel.setText("共有" + docNum + "条记录，第" + (currentHitIndex + 1) +  "/" + docNum + "页");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        String contentPreview = sliceTextSimplified(currentHit.getContent(), keywordIndex);
        tableModel.setValueAt(contentPreview, 1, 1);
        resultTable.getColumnModel().getColumn(1).setCellRenderer(new FinalHighlightTableCellRenderer(keywords));
    }

    // Simplified method that correctly handles surrogate pairs and respects startPos:
    private String sliceTextSimplified(String text, int startPos) {
        if (text == null || startPos < 0) {
            return "";
        }

        int effectiveStartPos = text.offsetByCodePoints(0, startPos); // Convert startPos to actual char index considering surrogate pairs
        if (effectiveStartPos >= text.length()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int i = effectiveStartPos;
        int charCount = 0; // Count of perceived characters (each code point as 1 unit)

        while (i < text.length() && charCount < 256) {
            int codePoint = text.codePointAt(i);
            // 没有判断换行符
            result.append(text, i, i + Character.charCount(codePoint));
            charCount++;
            i += Character.charCount(codePoint);
        }
        previewOver = i >= text.length();
        return result.toString();
    }

    public Demo() {
        setTitle("搜索引擎");
        indexGeneratingPanel = new JPanel();
        searchingPanel = new JPanel();
        consistingPanelForIGPanel = new JPanel();
        jumpBtnPanel = new JPanel();
        cards = new JPanel(new CardLayout());
        titleLabel = new JLabel("欢迎使用搜索引擎");
        instructionLabel = new JLabel("使用前请务必阅读说明书.md，首先选择搜索区域以生成索引。");



        if (new File(indexFileString).length() == 0) {
            rb1 = new JRadioButton("索引文件为空，无法继承使用");
            rb1.setEnabled(false);
        } else {
            rb1 = new JRadioButton("读取历史记录索引");
        }

        rb2 = new JRadioButton("使用text目录测试用例更新索引");
        rb3 = new JRadioButton("使用本地目录更新索引，请在下方选择或输入路径");
        jumpBtn = new JButton("生成索引");
        jumpBtn.addActionListener(e -> {
            if (rb1.isSelected()) {
                System.out.println("使用上次载入的索引");
                index = new Index();
                index.load(new File(indexFileString));
            } else if(rb2.isSelected()) {
                rootDir = Config.DOC_DIR;
            } else if(rb3.isSelected()) {
                rootDir = fileChooserJtf.getText();
                if(rootDir.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "请进行输入！");
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(null, "请选择搜索区域！");
                return;
            }
            
            if (rb2.isSelected() || rb3.isSelected()) {
                if (!new File(rootDir).exists()) {
                    JOptionPane.showMessageDialog(null, "请输入正确的路径！");
                    return;
                } else {
                    documentBuilder = new DocumentBuilder();
                    indexBuilder = new IndexBuilder(documentBuilder);
                    index = indexBuilder.buildIndex(rootDir);
                    index.optimize();
                    index.save(new File(indexFileString));
                }
            }
            simpleSorter = new SimpleSorter();
            filenameSorter = new FilenameSorter();
            lengthSorter = new LengthSorter();
            timeSorter = new TimeSorter();
            usedSorter = simpleSorter;
            searcher = new IndexSearcher();
            searcher.open(indexFileString);
            cl.show(cards, "searchingPanel");
            System.out.println(Charset.defaultCharset().name());
            System.out.println(indexFileString);
            System.out.println(index);  //控制台打印index的内容
        });


        indexGeneratingPanel.setLayout(new BorderLayout(20, 0));
        indexGeneratingPanel.setMaximumSize(new Dimension(800, 300));
        indexGeneratingPanel.setMinimumSize(new Dimension(800, 300));
        indexGeneratingPanel.setPreferredSize(new Dimension(800, 300));

        titleLabel.setHorizontalAlignment(0);
        indexGeneratingPanel.add(titleLabel, BorderLayout.NORTH);

        jumpBtn.setDefaultCapable(true);
        jumpBtn.setEnabled(true);
        jumpBtn.setHideActionText(false);
        jumpBtn.setHorizontalTextPosition(11);
        jumpBtn.setFont(new Font("华文中宋", Font.BOLD, 24));
        jumpBtnPanel.add(jumpBtn);
        indexGeneratingPanel.add(jumpBtnPanel, BorderLayout.SOUTH);


        consistingPanelForIGPanel.setLayout(new GridBagLayout());
        consistingPanelForIGPanel.setAutoscrolls(false);
        indexGeneratingPanel.add(consistingPanelForIGPanel, BorderLayout.CENTER);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        consistingPanelForIGPanel.add(rb1, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0.005;
        gbc.anchor = GridBagConstraints.WEST;
        consistingPanelForIGPanel.add(rb2, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        consistingPanelForIGPanel.add(rb3, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 0.005;
        gbc.fill = GridBagConstraints.BOTH;
        consistingPanelForIGPanel.add(fileChooserPanel, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.005;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        consistingPanelForIGPanel.add(instructionLabel, gbc);
        
        titleLabel.setFont(new Font("华文中宋", Font.BOLD, 36));
        instructionLabel.setFont(new Font("华文中宋", Font.BOLD, 20));
        rb1.setFont(new Font("华文中宋", Font.BOLD, 24));
        rb2.setFont(new Font("华文中宋", Font.BOLD, 24));
        rb3.setFont(new Font("华文中宋", Font.BOLD, 24));
        fileChooserLabel.setFont(new Font("华文中宋", Font.BOLD, 22));
        fileChooserJtf.setFont(new Font("华文中宋", Font.BOLD, 14));
        fileChooserButton.setFont(new Font("华文中宋", Font.BOLD, 14));
        group = new ButtonGroup();
        group.add(rb1);
        group.add(rb2);
        group.add(rb3);

        fileChooserPanel.add(fileChooserLabel);
        fileChooserPanel.add(fileChooserJtf);
        fileChooserJtf.setText("未选择文件");
        fileChooserJtf.setColumns(35);
        fileChooserPanel.add(fileChooserButton);
        fileChooserButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser("D:\\");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int val=fc.showOpenDialog(null);    //文件打开对话框
            if(val== JFileChooser.APPROVE_OPTION)
            {
                //正常选择文件
                fileChooserJtf.setText(fc.getSelectedFile().toString());
            }
            else
            {
                //未正常选择文件，如选择取消按钮
                fileChooserJtf.setText("未选择文件");
            }
        });

        indexGeneratingPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        cards.add(indexGeneratingPanel, "indexGeneratingPanel");

        searchingPanel = new JPanel();
        searchingPanel.setLayout(new BorderLayout(0, 0));
        searchingPanel.setMaximumSize(new Dimension(800, 300));
        searchingPanel.setMinimumSize(new Dimension(800, 300));
        searchingPanel.setPreferredSize(new Dimension(800, 300));
        searchAreaPanel = new JPanel();
        searchAreaPanel.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        searchingPanel.add(searchAreaPanel, BorderLayout.NORTH);
        searchField = new JTextField();
        searchAreaPanel.add(searchField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        searchBtn = new JButton();
        searchBtn.setText("搜索");
        searchAreaPanel.add(searchBtn, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SettingButton = new JButton();
        SettingButton.setText("设置");
        searchAreaPanel.add(SettingButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        backBtn = new JButton();
        backBtn.setText("返回");
        searchAreaPanel.add(backBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exitBtnOnP2 = new JButton();
        exitBtnOnP2.setText("退出");
        searchAreaPanel.add(exitBtnOnP2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,null,null,null,0,false));

        scrollAreaPanel = new JPanel();
        scrollAreaPanel.setLayout(new GridLayoutManager(1, 8, new Insets(0, 0, 0, 0), -1, -1));
        searchingPanel.add(scrollAreaPanel, BorderLayout.SOUTH);
        pageScrollLabel = new JLabel();
        pageScrollLabel.setText("共有" + docNum + "条记录，第" + (currentHitIndex + 1) +  "/" + docNum + "页");
        scrollAreaPanel.add(pageScrollLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pageLastBtn = new JButton();
        pageLastBtn.setText("尾页");
        scrollAreaPanel.add(pageLastBtn, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keywordScrollLabel = new JLabel();
        keywordScrollLabel.setText("预览区域：");
        scrollAreaPanel.add(keywordScrollLabel, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keywordUpBtn = new JButton();
        keywordUpBtn.setText("上移");
        scrollAreaPanel.add(keywordUpBtn, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keywordDownBtn = new JButton();
        keywordDownBtn.setText("下移");
        scrollAreaPanel.add(keywordDownBtn, new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pageBackBtn = new JButton();
        pageBackBtn.setText("上页");
        scrollAreaPanel.add(pageBackBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pageForwardBtn = new JButton();
        pageForwardBtn.setText("下页");
        scrollAreaPanel.add(pageForwardBtn, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pageFirstBtn = new JButton();
        pageFirstBtn.setText("首页");
        scrollAreaPanel.add(pageFirstBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        tableDate = new Object[5][2];
        tableDate[0][0] = "文件名";
        tableDate[1][0] = "内容预览";
        tableDate[2][0] = "关键词位置";
        tableDate[3][0] = "文件大小";
        tableDate[4][0] = "修改时间";
        for (int i = 0; i < 5; i++) {
            for (int j = 1; j < 2; j++) {
                tableDate[i][j] = "无";
            }
        }
//        tableDate[1][1] = "高亮测试测试测试测试测试测试测试测试测试高亮测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试";
        String[] name = {"key", "value"};
        resultTable = new JTable(new DefaultTableModel(tableDate, name));
        searchingPanel.add(resultTable, BorderLayout.CENTER);


        resultTable.setEnabled(false);
        resultTable.getColumn("key").setMaxWidth(70);
        //设置表头内容居中
        ((DefaultTableCellRenderer) resultTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        //设置单元格内容居中
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.LEFT);

//        String[] keywords = {"高亮"};
//        resultTable.getColumnModel().getColumn(1).setCellRenderer(new HighlightTableCellRenderer(keywords));

        resultTable.getColumn("key").setCellRenderer(render);
        resultTable.setFont(new Font("华文中宋", Font.PLAIN, 13));
        resultTable.setRowHeight(30);
        resultTable.setRowHeight(1, 100);
        resultTable.setRowHeight(2, 80);
        tableModel = (DefaultTableModel) resultTable.getModel();
        searchingPanel.add(resultTable, BorderLayout.CENTER);

        backBtn.addActionListener(e -> {
            cl.show(cards, "indexGeneratingPanel");
        });

        exitBtnOnP2.addActionListener(e -> {
            System.exit(0);
        });

        searchBtn.addActionListener(e ->{
            docNum = 0;
            if (searchField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "请输入搜索内容！", "提示", JOptionPane.WARNING_MESSAGE);
            } else {
                System.out.println("当前搜索内容：" + searchField.getText());
                String string = searchField.getText().toLowerCase();
                String[] strIn = string.split(" ");
                hits = searcher.search(strIn, usedSorter);
                if (hits == null) {
                    JOptionPane.showMessageDialog(null, "输入格式有误，请重新输入", "提示", JOptionPane.WARNING_MESSAGE);
                } else if (hits.length == 0) {
                    JOptionPane.showMessageDialog(null, "未搜索到结果，请重新输入", "提示", JOptionPane.WARNING_MESSAGE);
                } else {
                    docNum = hits.length;
                    currentHitIndex = 0;

                    updateFile();
                }
            }
        });

        pageFirstBtn.addActionListener(e -> {
            if(docNum == 0) {
                JOptionPane.showMessageDialog(null, "未搜索到结果！", "提示", JOptionPane.WARNING_MESSAGE);
            } else if (currentHitIndex == 0) {
                JOptionPane.showMessageDialog(null, "已到达第一页！", "提示", JOptionPane.WARNING_MESSAGE);
            } else {
                currentHitIndex = 0;
                updateFile();
            }
        });

        pageBackBtn.addActionListener(e -> {
            if(docNum == 0) {
                JOptionPane.showMessageDialog(null, "未搜索到结果！", "提示", JOptionPane.WARNING_MESSAGE);
            } else if (currentHitIndex == 0) {
                JOptionPane.showMessageDialog(null, "已到达第一页！", "提示", JOptionPane.WARNING_MESSAGE);
            } else {
                currentHitIndex -= 1;
                updateFile();
            }
        });

        pageForwardBtn.addActionListener(e -> {
            if(docNum == 0) {
                JOptionPane.showMessageDialog(null, "未搜索到结果！", "提示", JOptionPane.WARNING_MESSAGE);
            } else if (currentHitIndex == docNum - 1) {
                JOptionPane.showMessageDialog(null, "已到达最后一页！", "提示", JOptionPane.WARNING_MESSAGE);
            } else {
                currentHitIndex += 1;
                updateFile();
            }
        });

        pageLastBtn.addActionListener(e -> {
            if(docNum == 0) {
                JOptionPane.showMessageDialog(null, "未搜索到结果！", "提示", JOptionPane.WARNING_MESSAGE);
            } else if (currentHitIndex == docNum - 1) {
                JOptionPane.showMessageDialog(null, "已到达最后一页！", "提示", JOptionPane.WARNING_MESSAGE);
            } else {
                currentHitIndex = docNum - 1;
                updateFile();
            }
        });

        keywordUpBtn.addActionListener(e -> {
            if(docNum == 0) {
                JOptionPane.showMessageDialog(null, "未搜索到结果！", "提示", JOptionPane.WARNING_MESSAGE);
            } else if(keywordIndex == 0) {
                JOptionPane.showMessageDialog(null, "已到达文档开头！", "提示", JOptionPane.WARNING_MESSAGE);
            } else {
                keywordIndex -= 256;
                updatePreview();
            }
        });

        keywordDownBtn.addActionListener(e -> {
            if(docNum == 0) {
                JOptionPane.showMessageDialog(null, "未搜索到结果！", "提示", JOptionPane.WARNING_MESSAGE);
            } else if(previewOver) {
                JOptionPane.showMessageDialog(null, "已到达文档末尾！", "提示", JOptionPane.WARNING_MESSAGE);
            } else {
                keywordIndex += 256;
                updatePreview();
            }
        });

        // 添加按钮的点击事件监听器
        SettingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 创建一个包含单选按钮的字符串数组
                String[] options = {"相关度", "文件名", "文件大小", "修改时间"};

                // 使用JOptionPane显示包含单选按钮的对话框
                int selectedOption = JOptionPane.showOptionDialog(searchingPanel,
                        "请选择排序依据:",
                        "排序依据选择",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]); // 默认选项

                // 根据用户的选择执行下一步操作
                if (selectedOption >= 0) {
                    String selectedValue = options[selectedOption];
                    JOptionPane.showMessageDialog(searchingPanel, "搜索结果将按 " + selectedValue + " 进行排序！");

                    // 在这里添加根据选择执行的其他操作
                    // 例如: if (selectedValue.equals("选项1")) { ... }
                    switch (selectedValue) {
                        case "相关度":
                            usedSorter = simpleSorter;
                            break;
                        case "文件名":
                            usedSorter = filenameSorter;
                            break;
                        case "文件大小":
                            usedSorter = lengthSorter;
                            break;
                        case "修改时间":
                            usedSorter = timeSorter;
                            break;
                    }
                }
            }
        });

        cards.add(searchingPanel, "searchingPanel");
        cl = (CardLayout) (cards.getLayout());
        cl.show(cards, "indexGeneratingPanel");
        add(cards);
        setBounds(300, 200, 800, 360);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) {
        Demo demo = new Demo();
        demo.setVisible(true);
    }
}

class FinalHighlightTableCellRenderer extends DefaultTableCellRenderer {

    private Pattern pattern;

    public FinalHighlightTableCellRenderer(String[] keywords) {
        StringBuilder regexBuilder = new StringBuilder();
        for (String keyword : keywords) {
            if (!regexBuilder.isEmpty()) {
                regexBuilder.append("|");
            }
            regexBuilder.append(Pattern.quote(keyword));
        }
        this.pattern = Pattern.compile(regexBuilder.toString(), Pattern.CASE_INSENSITIVE);
        setHorizontalAlignment(JLabel.LEFT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable resultTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setOpaque(true);
        textPane.setBackground(isSelected ? resultTable.getSelectionBackground() : resultTable.getBackground());
        textPane.setForeground(isSelected ? resultTable.getSelectionForeground() : resultTable.getForeground());
        textPane.setFont(resultTable.getFont());

        if (row == 1 && column == 1 && value != null) {
            String text = value.toString();
            Matcher matcher = pattern.matcher(text);
            StringBuilder htmlText = new StringBuilder("<html><body>");
            while (matcher.find()) {
                matcher.appendReplacement(htmlText, "<span style='color: red;'>" + matcher.group() + "</span>");
            }
            matcher.appendTail(htmlText);
            htmlText.append("</body></html>");
            textPane.setText(htmlText.toString());
        } else {
            textPane.setText(value == null ? "" : value.toString());
        }

        return textPane;
    }
}