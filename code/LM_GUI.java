import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.table.DefaultTableModel;

public class LM_GUI extends JFrame{

    private JTextArea inputTextArea;
    private JTextArea likelySequenceInputTextArea;

    private JTable vocabTable;
    private JTable bigramTable;
    private JTable trigramTable;

    private MyHashTable vocabularyHashTable;

    private DefaultTableModel vocabTableModel;
    private DefaultTableModel bigramTableModel;
    private DefaultTableModel trigramTableModel;

    private JButton selectAFile;
    private JButton process;
    private JButton switchButton;
    private JButton collectBigramsButton;
    private JButton collectTrigramsButton;
    private JButton showStatisticsButton;
    private JButton showStatisticsforNGramButton;
    private JButton calculateLikelySequenceButtonUB;
    private JButton calculateLikelySequenceButton;

    Map<String, Integer> unigramCounts;
    Map<String, Integer> bigramCounts;
    Map<String, Integer> trigramCounts;

    //LM_GUI constructor to initialize UI components
    public LM_GUI(){
        super("Language Model");
        
        vocabularyHashTable = new MyHashTable(1000, new FirstLetterHF(1000));

        inputTextArea = new JTextArea();
        likelySequenceInputTextArea = new JTextArea(1,10);

        vocabTableModel = new DefaultTableModel(new Object[]{"Word", "Frequency"},0);
        bigramTableModel = new DefaultTableModel(new Object[]{"Bigram", "Frequency"}, 0);
        trigramTableModel = new DefaultTableModel(new Object[]{"Trigram", "Frequency"}, 0);

        vocabTable = new JTable(vocabTableModel);
        bigramTable = new JTable(bigramTableModel);
        trigramTable = new JTable(trigramTableModel);

        selectAFile = new JButton("Select a file");
        process = new JButton("Process text");
        switchButton = new JButton("Switch vocab sort");
        collectBigramsButton = new JButton("Collect Bigrams");
        collectTrigramsButton = new JButton("Collect Trigrams");
        showStatisticsButton = new JButton("Show Stats");
        showStatisticsforNGramButton = new JButton("Show NGram stats");
        calculateLikelySequenceButton = new JButton("Calculate likely");
        calculateLikelySequenceButtonUB = new JButton("Calculate likely U&B");

        //Button Action listeners

        selectAFile.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(LM_GUI.this);

                if(result == JFileChooser.APPROVE_OPTION){
                    File selectedFile = fileChooser.getSelectedFile();
                    processText(readFile(selectedFile));
                    inputTextArea.setText(readFile(selectedFile));
                    updateVocabTable();
                    vocabularyHashTable.collectUnigrams(inputTextArea.getText().toString());
                }
            }
        });

        process.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                processText(inputTextArea.getText());
                updateVocabTable();
                vocabularyHashTable.collectUnigrams(inputTextArea.getText().toString());
            }
        }
        );

        switchButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                switchVocabListSorting();
                updateVocabTable();
            }
        });

        collectBigramsButton.addActionListener(e -> {
            vocabularyHashTable.collectBigrams(inputTextArea.getText());
            updateBigramTrigramTable();
            JOptionPane.showMessageDialog(this, "Bigrams collected successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        collectTrigramsButton.addActionListener(e -> {
            vocabularyHashTable.collectTrigrams(inputTextArea.getText());
            updateBigramTrigramTable();
            JOptionPane.showMessageDialog(this, "Trigrams collected successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        calculateLikelySequenceButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                displayLikelySequence();
            }
        });

        calculateLikelySequenceButtonUB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                displayLikelySequenceUB();
            }
        });

        showStatisticsButton.addActionListener(e -> showStats());
        showStatisticsforNGramButton.addActionListener(e -> showStatsForNGram());
        
        JPanel fileSelection = new JPanel();
        fileSelection.add(selectAFile);

        setLayout(new BorderLayout());
        
        add(fileSelection, BorderLayout.NORTH);

        JPanel textAreaPanel = new JPanel(new GridLayout(1,2));

        JScrollPane inputTextAreaScrollPane = new JScrollPane(inputTextArea);
        JScrollPane tableScrollPane = new JScrollPane(vocabTable);
        JScrollPane bigramTableScrollPane = new JScrollPane(bigramTable);
        JScrollPane trigramTableScrollPane = new JScrollPane(trigramTable);
  
        textAreaPanel.add(inputTextAreaScrollPane);
        textAreaPanel.add(tableScrollPane);
        textAreaPanel.add(bigramTableScrollPane);
        textAreaPanel.add(trigramTableScrollPane);

        add(textAreaPanel, BorderLayout.CENTER);

        JScrollPane likelySequenceScrollPane = new JScrollPane(likelySequenceInputTextArea); 

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(process);
        buttonPanel.add(switchButton);
        buttonPanel.add(collectBigramsButton);
        buttonPanel.add(collectTrigramsButton);
        buttonPanel.add(showStatisticsButton);
        buttonPanel.add(showStatisticsforNGramButton);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(likelySequenceScrollPane);
        buttonPanel.add(calculateLikelySequenceButton);
        buttonPanel.add(calculateLikelySequenceButtonUB);
        
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    //Method to read the file contents selected by the user
    private String readFile(File file)
    {
        StringBuilder content = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            while((line = reader.readLine())!= null){
                content.append(line).append("\n");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return content.toString();
    }

    //Process the user inputted text and add the extracted words to the vocabulary hash table. Additionally, identifies and handles mis-processed items.
    private void processText(String text){
        vocabularyHashTable = new MyHashTable(1000, new FirstLetterHF(1000));
        Set<String> misProcessedItems = new HashSet<>();
    
            String[] words = text.split("\\s+");
            for(String word: words){
                if(!isValidWord(word)){
                    misProcessedItems.add(word);
                }
                word = word.replaceAll("[^a-zA-Z']", "");
            
                if(!word.isEmpty()){
                    if (isValidWord(word)) {
                        vocabularyHashTable.addWord(word.toLowerCase());
                    }
                }
            }
            // If there are mis-processed items, display a warning message.
            if (!misProcessedItems.isEmpty()) {
                StringBuilder warningMessage = new StringBuilder("Warning: Some items were mis-processed. Please check the mis-processed items list.\nMis-processed items:\n");
                misProcessedItems.forEach(item -> warningMessage.append(item).append("\n"));
        
                if (warningMessage.length() > 0) { // Display warning for long mis-processed items list
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, warningMessage.toString(), "Mis-processed Items", JOptionPane.WARNING_MESSAGE)
                    );
                    System.out.println(warningMessage.toString());
                }
            }
    }
    
    //Display the words in the vocabTable
    private void updateVocabTable(){
        vocabTableModel.setRowCount(0);
        List<MyLinkedObject> vocabList = vocabularyHashTable.getVocabularyList();
        for (MyLinkedObject vocab : vocabList) {
            String word = vocab.getWord();
            int frequency = vocab.getCount();
            vocabTableModel.addRow(new Object[]{word, frequency});
        }
    }

    private void switchVocabListSorting(){
        vocabularyHashTable.switchVocabListSorting();
    }

    //Display bigrams/trigram in the bigramTable/trigramTable
    private void updateBigramTrigramTable() {
        // Display bigram counts
        Map<String, Integer> bigramCounts = vocabularyHashTable.getBigrams();
        DefaultTableModel bigramTableModel = createTableModel(bigramCounts, "Bigram");
        bigramTable.setModel(bigramTableModel);

        // Display trigram counts
        Map<String, Integer> trigramCounts = vocabularyHashTable.getTrigrams();
        DefaultTableModel trigramTableModel = createTableModel(trigramCounts, "Trigram");
        trigramTable.setModel(trigramTableModel);
    }

    //Default table model for the bigram and trigram tables 
    private DefaultTableModel createTableModel(Map<String, Integer> counts, String columnName) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{columnName, "Frequency"}, 0);
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        return model;
    }

    //Get the total number of words in the text
    private int getTotalWordCount(String text) {
        String[] words = text.split("\\s+");
        int validWordCount = 0;
    
        for (String word : words) {
            //isValidWord method to check if the word is valid
            if (isValidWord(word)) {
                validWordCount++;
            }
        }
    
        return validWordCount;
    }

    private boolean isValidWord(String word) {
        // Add the regex to check for valid words
        String validChars = "[\\p{Ll}.'\\s]*";
        return word.matches(validChars);
    }

    //Method to display statistic data of the input text/document
    private void showStats(){
        int totalWords = getTotalWordCount(inputTextArea.getText());
        int totalUniqueWords = vocabularyHashTable.getVocabularyList().size();
        int total = vocabularyHashTable.getTotalLists();
        int max = vocabularyHashTable.getMaxLength();
        double avgLength = vocabularyHashTable.getAverageLength();
        double standardDeviation = vocabularyHashTable.getStandardDeviation();

        //Display stats in the popup
        String statisticsMessage = String.format(
            "Total words: %d\n" +
            "Total Unique words: %d\n" + 
            "Total lists: %d\n" +
            "Max Length: %d\n" +
            "Average Length: %.2f\n" +
            "Standard Deviation: %.2f",
                totalWords, totalUniqueWords, total, max, avgLength, standardDeviation);
        JOptionPane.showMessageDialog(this, statisticsMessage, "Hash Table Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStatsForNGram() {
    
        // Display bigram and trigram statistics
        bigramCounts = vocabularyHashTable.getBigrams();
        trigramCounts = vocabularyHashTable.getTrigrams();
    
        int totalBigrams = bigramCounts.size();
        int totalTrigrams = trigramCounts.size();
    
        // Display statistics for bigrams
        int maxBigramCount = bigramCounts.values().stream().max(Integer::compareTo).orElse(0);
        int minBigramCount = bigramCounts.values().stream().min(Integer::compareTo).orElse(0);
    
        // Display statistics for trigrams
        int maxTrigramCount = trigramCounts.values().stream().max(Integer::compareTo).orElse(0);
        int minTrigramCount = trigramCounts.values().stream().min(Integer::compareTo).orElse(0);
      
        // Display all statistics in a dialog or any suitable UI component
        String statsMessage = String.format(
            "Statistics:\n" +
            "\nBigram Statistics:\n" +
            "Total Bigrams: %d\n" +
            "Max Bigram Count: %d\n" +
            "Min Bigram Count: %d\n" +
            "\nTrigram Statistics:\n" +
            "Total Trigrams: %d\n" +
            "Max Trigram Count: %d\n" +
            "Min Trigram Count: %d\n",
            totalBigrams, maxBigramCount, minBigramCount, totalTrigrams, maxTrigramCount, minTrigramCount
    );
    
        JOptionPane.showMessageDialog(this, statsMessage.toString(), "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    //Method to display likely sequence using unigram, bigram and trigrams
    private void displayLikelySequence(){
        String likelyInputText = likelySequenceInputTextArea.getText();
        if(!likelyInputText.isEmpty()){
            if(bigramTable.getRowCount()!=0 && trigramTable.getRowCount()!=0){
                String []likelyInputWords = likelyInputText.split("\\s+");
                
                int wordLimit = 20;
                List<String> likelySequence = vocabularyHashTable.calculateLikelySequence(likelyInputWords, wordLimit);
                System.out.println(likelySequence);

                StringBuilder sequence = new StringBuilder("Likely Sequence: \n");
                for(String word: likelySequence){
                    sequence.append(word).append(" ");
                }

                JOptionPane.showMessageDialog(this, sequence.toString(), "Likely Sequence", JOptionPane.INFORMATION_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(this, "Calculate bigrams and trigrams first to calculate likely sequence", "Likely Sequence",JOptionPane.INFORMATION_MESSAGE);
            }
        }
        else{
            JOptionPane.showMessageDialog(this, "Enter a word to calculate likely sequence", "Likely Sequence",JOptionPane.INFORMATION_MESSAGE);
        }
    }


    //Method to display likely sequence using only unigram and bigram
    private void displayLikelySequenceUB(){
        String likelyInputText = likelySequenceInputTextArea.getText();
        if(!likelyInputText.isEmpty()){
            if(bigramTable.getRowCount()!=0 ){
                String []likelyInputWords = likelyInputText.split("\\s+");
                
                int wordLimit = 20;
                List<String> likelySequence = vocabularyHashTable.calculateLikelySequenceUnigramBigram(likelyInputWords, wordLimit);
                System.out.println(likelySequence);

                StringBuilder sequence = new StringBuilder("Likely Sequence using unigrams & bigrams: \n");
                for(String word: likelySequence){
                    sequence.append(word).append(" ");
                }
                JOptionPane.showMessageDialog(this, sequence.toString(), "Likely Sequence using unigrams & bigrams", JOptionPane.INFORMATION_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(this, "Calculate bigrams first to calculate likely sequence", "Likely Sequence using unigrams & bigrams",JOptionPane.INFORMATION_MESSAGE);
            }
        }
        else{
            JOptionPane.showMessageDialog(this, "Enter a word to calculate likely sequence using unigrams & bigrams", "Likely Sequence using unigrams & bigrams",JOptionPane.INFORMATION_MESSAGE);
        }

    }

}