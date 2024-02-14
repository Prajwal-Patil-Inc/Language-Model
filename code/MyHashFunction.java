//Abstract super class MyHashFunction 
public abstract class MyHashFunction {

    int htSize;

    //Public constructor to initialize the htSize
    public MyHashFunction(int htSize)
    {
        this.htSize = htSize;
    }

    //Abstract function hash
    public abstract int hash(String wordString);

}

//FirstLetterHF class extending the super class MyHashFunction
class FirstLetterHF extends MyHashFunction{
    public FirstLetterHF(int htSize){
        super(htSize);
    }

    //Overriding the method hash in the super class
    public int hash(String wordString){
        if(wordString.length()>0){
            char firstLetter = wordString.charAt(0);
            return firstLetter % htSize;
        }
        else{
            return 0;
        }
    }
}

//RollingHash class extending the super class MyHashFunction 
class RollingHash extends MyHashFunction{
    public RollingHash(int htSize){
        super(htSize);
    }

    //Overriding the method hash in the super class
    public int hash(String wordString){
        int hash = 0 ;
        int p = 31;
        for(int i=0; i<wordString.length(); i++){
            //Using the polynomial rolling hash formula
            hash = (hash * p + wordString.charAt(i)) % htSize;
        }
        return hash;
    }
} 

