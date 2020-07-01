public class Player
{
    int player;
    boolean active;
    String colour;

    Player(int p, boolean a, String c)
    {
        player = p;
        active = a;
        colour = c;
    }

    public int getPlayer() {
        return player;
    }
    
    public String getColour() {
        return colour;
    }

    public boolean getActive()
    {
        return active;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}