/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author BuYa
 */
public class Clyde extends Ghost{
    
    public Clyde(float x, float y, float vitesse) {
        super(x, y, vitesse);
        
        try {
            img = ImageIO.read(new File("res/clyde.png"));
        } catch (IOException ex) {
            Logger.getLogger(Pacman.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void setCible(int xPacman, int yPacman){
        if(Math.sqrt(Math.pow(x - xPacman, 2) + Math.pow(y - yPacman, 2)) > 8){
            cible = new Tile(xPacman, yPacman, 0);
        } else {
            cible = new Tile(0, 36, 0);
        }
    }
}
