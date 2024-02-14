public class MyLinkedObject {
    private String word;
    private int count;
    private MyLinkedObject next;

    // MyLinkedObject constructor
    public MyLinkedObject(String w) {
        this.word = w;
        this.count = 1; 
        this.next = null;
    }

    // Setter method for the word field
    public void setWord(String w) {

        // If w is equal to the current word, increment the count
        if (w.equals(this.word)) {
            this.count++;
        } 
        else {
            // If next object does not exist, create a new object for w
                if(next == null || word.compareTo(word)<0) {
               
                // If w is alphabetically smaller than or equal to the word field of the next object,
                // insert a new object for w between this and the next objects
                    MyLinkedObject newObject = new MyLinkedObject(w);
                    newObject.next = this.next;
                    this.next = newObject;
                } else {
                    // Otherwise, pass on w to the next object recursively
                    this.next.setWord(w);
                }
            }
        }


    //Method to get the total count of lists
    public int getListLength() {
        int length = 1;
        MyLinkedObject currentList = this;
        
        while (currentList.getNext() != null) {
            currentList = currentList.getNext();
            length++;
        }
        
        return length;
    }

    // Gets the word
    public String getWord() {
        return word;
    }

    // Gets the word count
    public int getCount() {
        return count;
    }

    // Gets the next object in the list
    public MyLinkedObject getNext() {
        return next;
    }

    //Display the 'word: count'
    public void getObject(){
        System.out.println(getWord()+":"+getCount());
        if(next!=null){
            next.getObject();
        }
    }
}