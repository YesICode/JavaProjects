
package generator.wolfram;


public enum StartConfig {
    MIDDLE, RANDOM;
    
    @Override
    public String toString(){
        switch(this) {
            case MIDDLE: return "One in the Middle";
            case RANDOM: return "Random";
            default: throw new IllegalArgumentException();
        }
    }
}
