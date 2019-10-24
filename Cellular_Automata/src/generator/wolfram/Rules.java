
package generator.wolfram;


public enum Rules {
    RULE30, RULE90, RULE110, RULE190, RULE222;
    
    @Override
    public String toString(){
        switch(this) {
            case RULE30: return "30";
            case RULE90: return "90";
            case RULE110: return "110";
            case RULE190: return "190";
            case RULE222: return "222";
            default: throw new IllegalArgumentException();
        }
    }
}
