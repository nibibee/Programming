public abstract class FairyCharacter {
    protected String name;
    protected String mood;
    protected boolean hunger;
    protected String place;
    protected String job;
    protected boolean breath;
    protected boolean isFree;
    protected int money;

    //For Villain
   /* public abstract void eat(Food food );
    public abstract void listen(Sound sound);
    public abstract void imagine(Food food);
    public abstract void broke();
    public abstract void think();
*/
   // For All
    public abstract void setName(String name);
    public abstract String getName();

    public abstract void setPlace(String place);
    public abstract String getPlace();

    public abstract void setMood(String mood);
    public abstract String getMood();

    public abstract void setHunger(boolean hunger);
    public abstract boolean getHunger();

    public abstract void setJob(String job);
    public abstract String getJob();





}
