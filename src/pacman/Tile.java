/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman;

/**
 *
 * @author BuYa
 */
public class Tile {
    
    private int x;
    private int y;
    private int type;
    private boolean solide;
    
    public Tile(int x, int y, int type){
        this.x = x;
        this.y = y;
        this.type = type;
        if(type == 44 || type == 45 || type == 46 || type == 47){
            this.solide = false;
        } else {
            this.solide = true;
        }
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the solide
     */
    public boolean isSolide() {
        return solide;
    }
    
    
}
