/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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
public class Ghost extends Entity{
    
    public enum Etat{
        Attente,
        Normal,
        Scatter,
        Peur,
        Retour,
        AttenteBleu;
    }
    protected Tile cible = new Tile(-1, -1);
    protected Etat etat;
    protected int xScatter, yScatter;
    protected boolean basAttente, enTrainDeSortir, dejaManger;
    protected BufferedImage cibles, cibleImg;
    private static boolean scatter = true, peur;
    private static int phase;
    private static int phases[] = {7, 20, 7, 20, 5, 20, 5};
    private static long start = 0, pauseStart = 0, pauseDuree = 0, pausePrevu = 0;
    private static Tile cage = new Tile(-8, -5);

    public Ghost(float x, float y, float vitesse, int xScatter, int yScatter, String pictureFile, int numero) {
        super(x, y, vitesse, pictureFile, Texture.getGhosts_rows(), Texture.getGhosts_columns());
        this.xScatter = xScatter;
        this.yScatter = yScatter;
        etat = Etat.Attente;
        
        BufferedImage[] prov = sprites;
        if(Texture.isGhosts_scarred_multiframe()){
            sprites = new BufferedImage[Texture.getGhosts_rows() * Texture.getGhosts_columns() * 4];
        } else {
            sprites = new BufferedImage[Texture.getGhosts_rows() * Texture.getGhosts_columns() * 2];
        }
        
        
        for(int i = 0; i < prov.length; i++){
            sprites[i] = prov[i];
        }
        
        BufferedImage spriteSheet = null;
        try {
            spriteSheet = ImageIO.read(new File("res/textures_pack/"+Texture.getTextureFolder()+"/peur.png"));
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
            try {
                spriteSheet = ImageIO.read(new File("res/textures_pack/original/peur.png"));
            } catch (IOException ex1) {
                Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        
        int rows = Texture.getGhosts_rows();
        if(Texture.isGhosts_scarred_multiframe()){
            rows *= 3;
        }
        for(int i = 0; i < Texture.getGhosts_columns(); i++) {
            for(int j = 0; j < rows; j++) {
                sprites[(j * Texture.getGhosts_columns()) + i + prov.length] = spriteSheet.getSubimage(i * spriteWidth, j * spriteHeight, spriteWidth, spriteHeight);
            }
        }
        
        try {
            cibles = ImageIO.read(new File("res/textures_pack/"+Texture.getTextureFolder()+"/cibles.png"));
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
            try {
                spriteSheet = ImageIO.read(new File("res/textures_pack/original/cibles.png"));
            } catch (IOException ex1) {
                Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        cibleImg = cibles.getSubimage(numero*8, 0, 8, 8);
    }
    
    public static void calculEtatGlobal(){
        if(peur){
            if(Frame.getMs() - pauseStart >= pausePrevu){
                peur = false;
                pauseDuree = Frame.getMs()-pauseStart;
                pausePrevu = 0;
            }
        } else {
            if(phase >= phases.length){
                scatter = false;
            } else {
                if(Frame.getMs() - start - pauseDuree >= phases[phase]*1000){
                    scatter = !scatter;
                    //System.out.println(scatter);
                    for(int i = 0; i < Panel.getGhostsTab().length; i++){
                        Panel.getGhostsTab()[i].inverserDirection();
                    }
                    phase++;
                    start = Frame.getMs();
                    pauseDuree = 0;
                }
            }
        }
    }
    
    public void avancer(){
        if(!peur){
            if(scatter){
                if(etat == Etat.Normal || etat == Etat.Peur){
                    etat = Etat.Scatter;
                } else if(etat == Etat.AttenteBleu){
                    etat = Etat.Attente;
                }
            } else {
                if(etat == Etat.Scatter || etat == Etat.Peur){
                    etat = Etat.Normal;
                } else if(etat == Etat.AttenteBleu){
                    etat = Etat.Attente;
                }
            }
        }
        
        if(etat == Etat.Attente || etat == Etat.AttenteBleu){
            if((peutSortir() && (y > cage.getY()+3-vitesse && y < cage.getY()+3+vitesse) || y == cage.getY()) || enTrainDeSortir){
                if(dejaManger){
                    setVitesse(vitesseDefaut);
                } else {
                    setVitesse(vitesseDefaut/2);
                }
                if(x > cage.getX()+0.5f+vitesse){
                    x-=vitesse;
                    directionCourente = Direction.Gauche;
                } else if(x < cage.getX()+0.5f-vitesse){
                    x+=vitesse;
                    directionCourente = Direction.Droite;
                } else {
                    basAttente = false;
                    enTrainDeSortir = true;
                    sortir();
                }
            } else {
                if(etat == Etat.AttenteBleu){
                    setVitesse(vitesseDefaut/2);
                } else {
                    setVitesse(vitesseDefaut);
                }
                if(basAttente){
                    y+=vitesse;
                    directionCourente = Direction.Bas;
                    if(y >= cage.getY()+3.5f){
                        basAttente = false;
                    }
                } else {
                    y-=vitesse;
                    directionCourente = Direction.Haut;
                    if(y <= cage.getY()+2.5f){
                        basAttente = true;
                    }
                }
            }
        } else {
            float vitesseMode = vitesseDefaut;
            
            if(etat == Etat.Scatter){
                setCibleScatter();
            } else if(etat == Etat.Peur){
                setCiblePeur();
                vitesseMode = 0.05f*Math.round((((vitesseDefaut+facteurVitesse)*(2f/3f))+0.025f)/0.05f)-0.15f;
            } else if(etat == Etat.Normal){
                setCible();
            } else if(etat == Etat.Retour){
                cible = cage;
                vitesseMode = vitesseDefaut*2;
                if(Math.round(x-0.5f) == cage.getX() && Math.round(y) == cage.getY()){
                    basAttente = true;
                }
            }
            
            if(Panel.getMap().effet(Math.round(x), Math.round(y)) == 2){
                vitesseMode = 0.05f*Math.round((((vitesseDefaut+facteurVitesse)*(2f/3f))+0.025f)/0.05f)-0.25f;
            }
            
            setVitesse(vitesseMode);

            if(basAttente){
                if(y < cible.getY()+3 && !enTrainDeSortir){
                    y += vitesse;
                } else {
                    etat = Etat.Attente;
                    enTrainDeSortir = true;
                    sortir();
                }
            } else {
                setDirection();
                super.avancer();
            }
                        
            if(touchePacman()){
                if(etat == Etat.Peur || etat == Etat.AttenteBleu){
                    etat = Etat.Retour;
                    dejaManger = true;
                } else if(etat == Etat.Normal || etat == Etat.Scatter){
                    Frame.stop();
                }
            }
        }
    }
    
    public void avancerEntity(){
        super.avancer();
    }
    
    public Direction[] setDirectionsPreferees(){
        Direction directionsPreferees[] = new Direction[4];
        
        if (Math.abs(x - cible.getX()) < Math.abs(y - cible.getY())) {
            if(y < cible.getY()) {
                directionsPreferees[0]= Direction.Bas;
                directionsPreferees[3]= Direction.Haut;	
            } else {
                directionsPreferees[0]= Direction.Haut;
                directionsPreferees[3]= Direction.Bas;	
            }

            if(x < cible.getX()) {
                directionsPreferees[1] = Direction.Droite;
                directionsPreferees[2] = Direction.Gauche;
            } else {
                directionsPreferees[1] = Direction.Gauche;
                directionsPreferees[2] = Direction.Droite;
            }
	} else {
            if(x < cible.getX()) {
                directionsPreferees[0]= Direction.Droite;
                directionsPreferees[3]= Direction.Gauche;	
            } else {
                directionsPreferees[0]= Direction.Gauche;
                directionsPreferees[3]= Direction.Droite;	
            }

            if(y < cible.getY()) {
                directionsPreferees[1] = Direction.Bas;
                directionsPreferees[2] = Direction.Haut;
            } else {
                directionsPreferees[1] = Direction.Haut;
                directionsPreferees[2] = Direction.Bas;
            }
        }
        
        if(etat == Etat.Peur){
            for(int i = 0; i < directionsPreferees.length / 2; i++){
                Direction temp = directionsPreferees[i];
                directionsPreferees[i] = directionsPreferees[directionsPreferees.length - i - 1];
                directionsPreferees[directionsPreferees.length - i - 1] = temp;
            }
        }
        
        return directionsPreferees;
    }
    
    public void setDirection(){
        Direction directionsPossibles[] = setDirectionsPossibles();
        Direction directionsPreferees[] = setDirectionsPreferees();
        
        Direction newDirection = null;
        
        int i=0;
        while(newDirection == null && i < directionsPreferees.length){
            for(int j=0; j < directionsPossibles.length; j++){
                if(directionsPreferees[i] == directionsPossibles[j]){
                    newDirection = directionsPreferees[i];
                }
            }
            i++;
        }
        
        if(newDirection == null){
            Direction directionRestante = setDirectionRestante();
            int j=0;
            while(newDirection == null && j < directionsPreferees.length){
                if(directionsPreferees[j] == directionRestante){
                    newDirection = directionRestante;
                }
                j++;
            }
        }
        
        directionSuivante = newDirection;
    }
    
    public Direction[] setDirectionsPossibles(){
        Direction directionsPossibles[] = new Direction[4];
        
        int i = 0;
        if(collision(Direction.Haut) && directionCourente != Direction.Bas && Panel.getMap().effet(Math.round(x), Math.round(y)) != 3){
            directionsPossibles[i] = Direction.Haut;
            i++;
        }
        if(collision(Direction.Gauche) && directionCourente != Direction.Droite){
            directionsPossibles[i] = Direction.Gauche;
            i++;
        }
        if(collision(Direction.Bas) && directionCourente != Direction.Haut){
            directionsPossibles[i] = Direction.Bas;
            i++;
        }
        if(collision(Direction.Droite) && directionCourente != Direction.Gauche){
            directionsPossibles[i] = Direction.Droite;
        }
        
        return directionsPossibles;
    }
    
    public Direction setDirectionRestante(){
        Direction directionRestante = null;
        
        switch (directionCourente) {
            case Gauche:
                directionRestante = Direction.Droite;
                break;
            case Droite:
                directionRestante = Direction.Gauche;
                break;
            case Haut:
                directionRestante = Direction.Bas;
                break;
            case Bas:
                directionRestante = Direction.Haut;
                break;
            default:
                break;
        }
        
        return directionRestante;
    }
    
    public void setCible(){
        cible = new Tile(0, 0);
    }
    
    public void setCibleScatter(){
        cible = new Tile(xScatter, yScatter);
    }
    
    public void setCiblePeur(){
        cible = new Tile(Panel.getPlayersTab()[0].getX(), Panel.getPlayersTab()[0].getY());
    }
    
    public boolean touchePacman(){
        boolean touche = false;
        
        if(getX() == Panel.getPlayersTab()[0].getX() && getY() == Panel.getPlayersTab()[0].getY()){
            touche = true;
        }
        
        return touche;
    }
    
    public void setIdSprite(){
        
        if(!Texture.isGhosts_scarred_multiframe() && (etat == Etat.Peur || etat == Etat.AttenteBleu)){
            idSprite = Texture.getGhosts_moving_frames()*4;
            if(Frame.getMs() - pauseStart >= pausePrevu-2000 && (Frame.getMs() - pauseStart) % 500 < 250){
                idSprite += Texture.getGhosts_moving_frames();
            }
        } else {
            switch (directionCourente) {
                case Droite:
                    idSprite = Texture.getGhosts_moving_frames()*0;
                    break;
                case Gauche:
                    idSprite = Texture.getGhosts_moving_frames()*1;
                    break;
                case Haut:
                    idSprite = Texture.getGhosts_moving_frames()*2;
                    break;
                case Bas:
                    idSprite = Texture.getGhosts_moving_frames()*3;
                    break;
                default:
                    break;
            }
            
            if(Texture.isGhosts_scarred_multiframe()){
                if(etat == Etat.Peur || etat == Etat.AttenteBleu){
                    idSprite += Texture.getGhosts_moving_frames()*4;
                    if(Frame.getMs() - pauseStart >= pausePrevu-2000 && (Frame.getMs() - pauseStart) % 500 < 250){
                        idSprite += Texture.getGhosts_moving_frames()*4;
                    }
                } else if(etat == Etat.Retour){
                    idSprite += Texture.getGhosts_moving_frames()*12;
                }
            }
        }

        for(int i = 0; i < Texture.getGhosts_moving_frames(); i++){
            if(Frame.getTicksTotal() % (Texture.getGhosts_moving_frames()/Texture.getGhosts_speed()/vitesse) >= i*(Texture.getGhosts_moving_frames()/Texture.getGhosts_speed()/vitesse)/(Texture.getGhosts_moving_frames())
            && Frame.getTicksTotal() % (Texture.getGhosts_moving_frames()/Texture.getGhosts_speed()/vitesse) < (i+1)*(Texture.getGhosts_moving_frames()/Texture.getGhosts_speed()/vitesse)/(Texture.getGhosts_moving_frames())){
                idSprite += (i%Texture.getGhosts_moving_frames());
            }
        }
    
        if(etat == Etat.Retour && !Texture.isGhosts_scarred_multiframe()){
            idSprite /= Texture.getGhosts_moving_frames();
            idSprite += Texture.getGhosts_moving_frames()*6;
        }
    }

    /**
     * @return the etat
     */
    public Etat getEtat() {
        return etat;
    }

    /**
     * @param etat the etat to set
     */
    public void setEtat(Etat etat) {
        this.etat = etat;
    }
    
    /**
     * @param aPeur the peur to set
     */
    public static void setPeurTrue() {
        peur = true;
        for(int i = 0; i < Panel.getGhostsTab().length; i++){
            Panel.getGhostsTab()[i].inverserDirection();
            if(Panel.getGhostsTab()[i].etat == Etat.Normal || Panel.getGhostsTab()[i].etat == Etat.Scatter){
                Panel.getGhostsTab()[i].etat = Etat.Peur;
            } else if(Panel.getGhostsTab()[i].etat == Etat.Attente){
                Panel.getGhostsTab()[i].etat = Etat.AttenteBleu;
            }
        }
        pausePrevu += 6000;
        pauseStart = Frame.getMs();
    }
    
    public boolean peutSortir(){
        return true;
    }
    
    public void sortir(){
        if(y > cage.getY()){
            y-=vitesse;
            directionCourente = Direction.Haut;
        } else {
            y = Math.round(y);
            enTrainDeSortir = false;
            basAttente = false;
            if(etat == Etat.AttenteBleu){
                etat = Etat.Peur;
            } else {
                if(scatter){
                    etat = Etat.Scatter;
                } else {
                    etat = Etat.Normal;
                }
            }
        }
    }

    public void inverserDirection(){
        switch (directionCourente) {
            case Gauche:
                directionCourente = Direction.Droite;
                break;
            case Haut:
                directionCourente = Direction.Bas;
                break;
            case Droite:
                directionCourente = Direction.Gauche;
                break;
            case Bas:
                directionCourente = Direction.Haut;
                break;
            default:
                break;
        }
    }
    
    public void afficherTuile(Graphics g, int width, int height){
        float size;
        int mapWidth = Panel.getMap().getMapWidth();
        int mapHeight = Panel.getMap().getMapHeight();
        
        if(width/mapWidth > height/mapHeight){
            size = (float)height/mapHeight;
        } else {
            size = (float)width/mapWidth;
        }
        
        Graphics2D g2d = (Graphics2D)g;
        AffineTransform transformation = new AffineTransform();
        
        transformation.translate(cible.getX()*size, cible.getY()*size);
        transformation.scale(size/8, size/8);
        g2d.drawImage(cibleImg, transformation, null);
    }

    /**
     * @param aCage the cage to set
     */
    public static void setCage(Tile aCage) {
        cage = aCage;
    }
    
    /**
     * @return the cage
     */
    public static Tile getCage() {
        return cage;
    }
    
    /**
     * @return the peur
     */
    public static boolean isPeur() {
        return peur;
    }

     /**
     * @param aScatter the scatter to set
     */
    public static void setScatter(boolean aScatter) {
        scatter = aScatter;
    }
    
}
