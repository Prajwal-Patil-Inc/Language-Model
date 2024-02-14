import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MyHashTable {
    private MyLinkedObject[] hashTableLinkedObjects;  //Array of MyLinkedObject objects
    private MyHashFunction hashFunction;
    private boolean sortByFrequency;
        
    private Map<String, Integer> unigrams;
    private Map<String, Integer> bigrams;
    private Map<String, Integer> trigrams;

    //MyHashTable constructor
    public MyHashTable(int htSize, MyHashFunction hf){
        this.hashTableLinkedObjects = new MyLinkedObject[htSize];
        this.hashFunction = hf;
        this.sortByFrequency = false;
        unigrams = new HashMap<>();
        bigrams = new HashMap<>();
        trigrams = new HashMap<>();
    }

    //Method to add words
    public void addWord(String wordString){
        //Calculate the hash index for the given word
        int index = hashFunction.hash(wordString);

        if(hashTableLinkedObjects[index]==null){
            //if null, create a new linked object at the index with the given word
            hashTableLinkedObjects[index] = new MyLinkedObject(wordString);
        }
        else{
            //If not null, update the existing linked object's word using the setWord method
            hashTableLinkedObjects[index].setWord(wordString);
        }
    }

    //Fetches a list of all the words in the hash table
    public List<MyLinkedObject> getVocabularyList(){

        //Empty list for all the words
        List<MyLinkedObject> allWords = new ArrayList<>();

        for(MyLinkedObject linkedObject : hashTableLinkedObjects){
            if(linkedObject != null){
                //Collect words from the linked object and its chain
                collectWords(linkedObject, allWords);
            }
        }

        //Sorting by frequency of the words
        if(sortByFrequency){
            Collections.sort(allWords, (a, b) -> {
                int countComparison = Integer.compare(b.getCount(), a.getCount());
                return (countComparison == 0) ? a.getWord().compareTo(b.getWord()) : countComparison;
            });
        }
        //Sorting by alphabetical order
        else{
            Collections.sort(allWords, (a, b) -> a.getWord().compareTo(b.getWord()));
        }

        return allWords;
    }

    //Method to collect unigrams from the text

     // Method to collect unigrams from the text
     public void collectUnigrams(String document) {
        // Split the input text into words by regex 'white space'
        String[] words = document.split("\\s+");

        for (String word : words) {
            // Check if the word contains any unicode symbol that is not a full stop, an apostrophe, or lower case English letters
            if (!containsInvalidSymbol(word)) {
                unigrams.merge(word, 1, Integer::sum);
            }
        }
    }

    //Method to collect bigrams from the text 
    public void collectBigrams(String document) {
        //Split the input text into words by regex 'white space'
        String[] words = document.split("\\s+");
    
        for (int i = 0; i < words.length - 1; i++) {

            //Create a bigram from two consecutive words
            String bigram = words[i] + " " + words[i + 1];
            
            // Check if the bigram contains any unicode symbol that is not a full stop, an apostrophe, or lower case English letters
            if (!containsInvalidSymbol(bigram)) {
                bigrams.merge(bigram, 1, Integer::sum);
            } 
        }
    }    

    //Method to collect trigrams from the text 
    public void collectTrigrams(String document) {
        String[] words = document.split("\\s+");

        for (int i = 0; i < words.length - 2; i++) {

            // Create a trigram from three consecutive words
            String trigram = words[i] + " " + words[i + 1] + " " + words[i + 2];

            // Check if the trigram contains any unicode symbol that is not a full stop, an apostrophe, or lower case English letters
            if (!containsInvalidSymbol(trigram)) {
                trigrams.merge(trigram, 1, Integer::sum);
            }
        }
    }

    // Method to calculate unigram probabilities
    private Map<String, Integer> calculateUnigramProbabilities() {
        Map<String, Integer> unigramProbabilities = new HashMap<>();
        int totalUnigrams = getTotalUnigrams();

        for (Entry<String, Integer> entry : unigrams.entrySet()) {
            int probability = entry.getValue() / (int) totalUnigrams;
            unigramProbabilities.put(entry.getKey(), probability);
        }

        return unigramProbabilities;
    }

    // Method to calculate bigram probabilities
    private Map<String, Integer> calculateBigramProbabilities() {
        Map<String, Integer> bigramProbabilities = new HashMap<>();

        for (Entry<String, Integer> entry : bigrams.entrySet()) {
            String[] parts = entry.getKey().split(" ");
            if (parts.length == 2) {
                String unigram = parts[0];
                if (unigrams.containsKey(unigram)) {
                    int probability = entry.getValue() / (int) unigrams.get(unigram);
                    bigramProbabilities.put(entry.getKey(), probability);
                }
            }
        }

        return bigramProbabilities;
    }

    // Method to calculate trigram probabilities
    private Map<String, Integer> calculateTrigramProbabilities() {
        Map<String, Integer> trigramProbabilities = new HashMap<>();

        for (Entry<String, Integer> entry : trigrams.entrySet()) {
            String[] parts = entry.getKey().split(" ");
            if (parts.length == 3) {
                String bigram = parts[0] + " " + parts[1];
                if (bigrams.containsKey(bigram)) {
                    int probability = entry.getValue() / (int) bigrams.get(bigram);
                    trigramProbabilities.put(entry.getKey(), probability);
                }
            }
        }

        return trigramProbabilities;
    }

    //Calculate likely sequence using unigram, bigram and trigrams
    public List<String> calculateLikelySequence(String[] inputWords, int maxWords) {
        List<String> likelySequence = new ArrayList<>();
        Map<String, Integer> unigramProbabilities = calculateUnigramProbabilities();
        Map<String, Integer> bigramProbabilities = calculateBigramProbabilities();
        Map<String, Integer> trigramProbabilities = calculateTrigramProbabilities();
    
        // Add the input words to the sequence
        likelySequence.addAll(Arrays.asList(inputWords));
    
        // Fill the remaining sequence with unigram, bigram, and trigram possibilities
        for (int i = likelySequence.size(); i < maxWords; i++) {
            String unigram = i > 0 ? likelySequence.get(i - 1) : null;
            String bigram = i > 1 ? likelySequence.get(i - 2) + " " + likelySequence.get(i - 1) : null;
            String trigram = i > 2
                    ? bigram + " " + likelySequence.get(i - 3)
                    : null;
    
            String nextWord;
            if (i == 0) {
                nextWord = getNextWordBasedOnUnigram(unigram, unigramProbabilities);
            } else {
                nextWord = getNextWordBasedOnTrigram(trigram, trigramProbabilities);
                if (nextWord == null) {
                    nextWord = getNextWordBasedOnBigram(bigram, bigramProbabilities);
                }
            }
    
            if (nextWord != null) {
                String[] words = nextWord.split("\\s+");
                likelySequence.addAll(Arrays.asList(words));
            }
        }
    
        // Trim the sequence to the specified maximum word count
        return likelySequence.size() <= maxWords ? likelySequence : likelySequence.subList(0, maxWords);
    }
    
    //Calculate the likely sequence using unigram and bigrams only
    public List<String> calculateLikelySequenceUnigramBigram(String[] inputWords, int maxWords) {
        List<String> likelySequence = new ArrayList<>();
        Map<String, Integer> unigramProbabilities = calculateUnigramProbabilities();
        Map<String, Integer> bigramProbabilities = calculateBigramProbabilities();
    
        // Add the input words to the sequence
        likelySequence.addAll(Arrays.asList(inputWords));
    
        // Fill the remaining sequence with unigram and bigram possibilities
        for (int i = likelySequence.size(); i < maxWords; i++) {
            String unigram = i > 0 ? likelySequence.get(i - 1) : null;
            String bigram = i > 1 ? likelySequence.get(i - 2) + " " + likelySequence.get(i - 1) : null;
    
            String nextWord;
            if (i == 0) {
                nextWord = getNextWordBasedOnUnigram(unigram, unigramProbabilities);
            } else {
                nextWord = getNextWordBasedOnBigram(bigram, bigramProbabilities);
            }
    
            if (nextWord != null) {
                String[] words = nextWord.split("\\s+");
                likelySequence.addAll(Arrays.asList(words));
            }
        }
    
        // Trim the sequence to the specified maximum word count
        return likelySequence.size() <= maxWords ? likelySequence : likelySequence.subList(0, maxWords);
    }

    //Method to fetch the next word based on Unigram probabilities
    private String getNextWordBasedOnUnigram(String unigram, Map<String, Integer> unigramProbabilities) {
        String nextWord = null;
    
        if (unigramProbabilities.containsKey(unigram)) {
            nextWord = chooseRandomWord(unigramProbabilities);
        }
    
        return nextWord;
    }

    //Method to fetch the next word based on bigram probabilities
    private String getNextWordBasedOnBigram(String lastWord, Map<String, Integer> bigramProbabilities) {
        bigramProbabilities = calculateBigramProbabilities();
        String randomWord = chooseRandomWord(bigramProbabilities);
        return randomWord;
    }

    //Method to fetch the next word based on trigram probabilities
    private String getNextWordBasedOnTrigram(String lastWord, Map<String, Integer> trigramProbabilities) {
        trigramProbabilities= calculateBigramProbabilities();
        String randomWord = chooseRandomWord(trigramProbabilities);
        return randomWord;
    }

    //Method to get a random word using the probabilities
    private String chooseRandomWord(Map<String, Integer> probabilities) {
        if (probabilities.isEmpty()) {
            return null;
        }
    
        List<String> words = new ArrayList<>(probabilities.keySet());
        int randomIndex = new Random().nextInt(words.size());
        return words.get(randomIndex);
    }

    private boolean containsInvalidSymbol(String input) {
        //Pattern to match valid characters (full stop, apostrophe, and lower case English letters)
        String validChars = "[\\p{Ll}.'\\s]+";
        // Return true if the text does not match the validChars pattern
        return !input.matches(validChars);
    }
 
    //Method to get unigrams
    public Map<String, Integer> getUnigrams() {
        return unigrams;
    }
    
    //Fetches the bigrams
    public Map<String, Integer> getBigrams() {
        return bigrams;
    }

    //Fetches the trigrams
    public Map<String, Integer> getTrigrams() {
        return trigrams;
    }
    
    //Collect words from the linkedObject which is the starting of the linked list and the list to store all linkedObjects
    private void collectWords(MyLinkedObject linkedObject, List<MyLinkedObject> allWords){
        //Add the current linkedObject to the list
       allWords.add(linkedObject);

       if(linkedObject.getNext() != null){

        //Using recursion to collect words from the next linkedObject if it is not null 
        collectWords(linkedObject.getNext(), allWords);
       } 
    }

    //Get the total number of lists in the hash table
    public int getTotalLists() {
        return Arrays.stream(hashTableLinkedObjects)
            .filter(list -> list != null && list.getListLength() > 0)
            .mapToInt(list -> 1)
            .sum();
    }

    //Get the maximum length of the linked lists in the hash table
    public int getMaxLength() {
        return Arrays.stream(hashTableLinkedObjects)
                     .filter(Objects::nonNull)
                     .mapToInt(MyLinkedObject::getListLength)
                     .max()
                     .orElse(0);
    }

    //Calculate the average length of the linked lists in the hash table
    public double getAverageLength() {
        int totalNodes = Arrays.stream(hashTableLinkedObjects)
                .filter(list -> list != null)
                .mapToInt(MyLinkedObject::getListLength)
                .sum();
    
        int totalLists = getTotalLists();
        return (totalLists > 0) ? (double) totalNodes / totalLists : 0.0;
    }
    
    //Calculates the standard deviation of the linked list in the hash table
    public double getStandardDeviation() {
        //Collect the lengths of the non-null linked lists
        List<Integer> lengths = Arrays.stream(hashTableLinkedObjects)
                .filter(list -> list != null)
                .map(MyLinkedObject::getListLength)
                .collect(Collectors.toList());
    
        //Calculate the mean and the sum of the squared differences
        double mean = getAverageLength();
        double sumSquareDiff = lengths.stream().mapToDouble(len -> Math.pow(len - mean, 2)).sum();
    
        //Calculate and return the standard deviation
        return Math.sqrt(sumSquareDiff / lengths.size());
    }
    
    public void switchVocabListSorting(){
        sortByFrequency = !sortByFrequency;
    }

    public int getTotalUnigrams() {
        int total = 0;
        for (int count : unigrams.values()) {
            total += count;
        }
        return total;
    }
}
