package culecalc;

import java.text.NumberFormat;
import java.util.ArrayList;

public class CFormula {
    private long coefficient;
    private String formula;
    private ArrayList<CFormulaPart> elements = new ArrayList<>();
    private double mass;
    
    public CFormula(){}
    public CFormula(String formula){
        this.formula = formatFormula(formula);
        this.coefficient = getCoefficient();
        this.elements = getElements();
        this.mass = getMass();
        //Sets the % composition for each FormulaPart
        
        for(CFormulaPart f : elements)
            f.setComposition(this.mass / this.coefficient);
        
    }
    
    public final String formatFormula(String in){
        String out = "";
        String[] split = in.replaceAll("\\s", "").split("(?=[(]\\w+[)]\\d+)", -1);
        for(int i = 0; i < split.length; i++){
            int coef = 1;
            try{
                coef = Integer.parseInt(split[i].split("[)]")[1]);
            }catch(Exception ex){
            }
            if(coef == 1){ out += split[i]; continue; }
            String temp = split[i].substring(1).replaceAll("([)]\\d+)", "");
            String[] resplit = temp.split("(?=\\p{Upper})");
            for(String e : resplit){
                String[] reresplit = e.split("(?<=[\\w&&\\D])(?=\\d)");
                int subscript = reresplit.length > 1 ? Integer.parseInt(reresplit[1]) : 1;
                subscript *= coef;
               out += reresplit[0] + subscript;
            }
            
        }
        out = out.replaceAll("([(]|[)])", "");
        return out;
    }
    
    public final long getCoefficient(){
        //Uses a Regex lookahead to match first uppercase character and then splits between it and the previous character.
        String[] split = formula.split("(?=\\p{Upper})");
        try{
            return formula.length() >= 1 ? (Long.parseLong(!(split[0].charAt(0) > '0' && split[0].charAt(0) <= '9') ? "1" : split[0])) : 1;
        }catch(Exception ex){
            //TODO: Print error message in panel.
            System.out.println("Check your formula for errors!");
        }
        return 1;
    }
    
    //Returns an ArrayList of all the FormulaParts
    private final ArrayList<CFormulaPart> getElements(){
        if(this.formula.length() == 0){ return new ArrayList<>(); }
        
        //TODO: Take ()'s into account
        String[] split = this.formula.split("(?=\\p{Upper})");
        for(int i = 0; i < split.length; i++){
            if(!(split[i].charAt(0) > '0' && split[i].charAt(0) <= '9')){
                //Uses a Regex lookahead to split between the last character and first digit found.
                String[] e = split[i].split("(?<=[\\w&&\\D])(?=\\d)");
                CElement element = new CElement();
                for(CElement t : CuleCalc.elements){
                    if(t.getSymbol().equals(e[0])){
                        element = t;
                        break;
                    }
                }
                int subscript = e.length > 1 ? Integer.parseInt(e[1]) : 1;
                if(!exists(element)){
                    elements.add(new CFormulaPart(element, subscript));
                }else{
                    for(CFormulaPart cf : elements){
                        if(cf.getElement().equals(element)){
                            cf.addCount(subscript);
                            break;
                        }
                    }
                }
            }
        }
        
        this.formula = "";
        for(CFormulaPart cf : elements)
            this.formula += cf.getElement().getSymbol() + cf.getSubscript();
        
        return elements;
    }
    
    public boolean exists(CElement e){
        for(CFormulaPart cf : elements){
            if(cf.getElement().equals(e)){
               return true;
           }
        }
        return false;
    }
    
    public int getSize(){ return this.elements.size(); }
    
    public final double getMass(){
        double mass = 0.0;
        for(CFormulaPart f : this.elements){
            mass += f.getElement().getMass() * f.getCount();
        }
        return mass * this.coefficient;
    }
    
    public String getMass(boolean units){ return this.getMass() + (units ? " amu" : ""); }
    
    public static int findGCF(int[] numbers) {
        int min = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < min) {
                min = numbers[i];
            }
        }
        while (min > 1) {
            int count = 0;
            int mod = 0;
            while (count < numbers.length) {
                mod += numbers[count] % min;
                count++;
            }
            
            if (mod == 0) {
                return min;
            }
            min--;
        }
        return 1;
    }
    
    public String getEmpirical(){
        int[] subs = new int[elements.size()];
        int count = 0;
        
        for(CFormulaPart cf : elements)
            subs[count++] = cf.getSubscript();
        
        int gcf = findGCF(subs);
        String s = "";
        for(CFormulaPart cf : elements){
            s += cf.getElement().getSymbol() + ((cf.getSubscript() / gcf) > 1 ? (cf.getSubscript() / gcf) : "");
        }
        return s;
    }
    
    /**
     * CFormulaPart getPart(int i)
     * 
     * @param i
     * @return FormulaPart in the ArrayList elements
     * Usage:
     *  {Formula object}.getPart(i).getElement() -> returns CElement of ArrayList<CElement> @ i
     *  {Formula object}.getPart(i).getCount() -> returns subscript/count of the CElement of ArrayList<CElement> @ i
     *  {Formula object}.getPart(i).getMass() -> returns the mass of the CFormulaPart as a double factoring in the subscript/count
     *  {Formula object}.getPart(i).getMass(boolean) -> returns the mass of the CFormulaPart as a string with or without units
     *  {Formula object}.getPart(i).getComposition() -> returns the % composition as a double
     *  {Formula object}.getPart(i).getComposition(boolean) -> returns the % composition as a formatted string or unformatted
     */
    
    //Returns a part of a formula or Unobtainium if out of bounds or nonexistant 
    public CFormulaPart getPart(int i){ if(i >= elements.size()){ return new CFormulaPart(); }else{ return elements.get(i); } }
    
    @Override
    public String toString(){ return this.formula; }
}

//Data structure for parts of a formula containing the CElement and its subscript/count and its % composition
class CFormulaPart {
    private CElement element;
    private int count;
    private double composition;
    
    public CFormulaPart(){ this(CuleCalc.elements.get(0),1);}
    public CFormulaPart(CElement element, int count){
        this.element = element;
        this.count = count;
    }
    public CElement getElement(){ return this.element; }
    public int getCount(){ return this.count; }
    public int getSubscript(){ return this.count; }
    public void addCount(int subscript){ this.count += subscript; }
    public double getMass(){ return this.element.getMass() * count; }
    public String getMass(boolean units){ return this.getMass() + (units ? " amu" : ""); }
    public void setComposition(double formulaMass){ this.composition = this.getMass() / formulaMass; }
    public double getComposition(){ return this.composition; }
    public String getComposition(boolean format){
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
	percentFormat.setMinimumFractionDigits(2);
        return format ? percentFormat.format(this.getComposition()) + " " + this.element.getSymbol() : String.valueOf(this.composition);
    }

    @Override
    public String toString(){ return this.element.getSymbol() + this.count; }
}

