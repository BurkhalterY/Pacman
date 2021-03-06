/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author BuYa
 */
public class Frame extends JFrame{

    private static long ms = 0;
    private static int ticksTotal = 0;
    
    private static long startTime = System.currentTimeMillis();
    private static long initialTime = System.nanoTime();
    private static int UPS = 60;
    private static final double timeU = 1000000000 / UPS;
    private static int FPS = 60;
    private static final double timeF = 1000000000 / FPS;
    private static double deltaU = 0, deltaF = 0;
    private static int frames = 0, ticks = 0;
    private static long timer = System.currentTimeMillis();

    private static boolean pause = true;
    private static Thread t;
    
    private static long pauseStart = System.currentTimeMillis();
    private static long pauseLong = 0;
    
    private static Panel pan;
    private static Editor editor;
    private static Menu menu = new Menu();
    private static CardLayout cl = new CardLayout();
    private static JPanel content = new JPanel();
    private static Clavier clav = new Clavier(); 

    public Frame(){
        Sound.initSound("original");
        menu.selectionMenu();
        this.setTitle("PAC-MAN");
        this.setSize(500, 600);
        //this.setSize(Panel.getMap().getMapWidth()*16+16, Panel.getMap().getMapHeight()*16+38);
        //this.setSize(this.getToolkit().getScreenSize());
        //this.setUndecorated(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        menu.addKeyListener(clav);
        
        //Sound.loopSiren();
        
        content.setLayout(cl);
        content.add(menu, "menu");
        
        this.setContentPane(content);
        this.setVisible(true);
        
        cl.show(content, "menu");
    }

    /**
     * @return the ms
     */
    public static long getMs() {
        return ms;
    }
    
    /**
     * @return the ticksTotal
     */
    public static int getTicksTotal() {
        return ticksTotal;
    }
    
    public static void go(){
        cl.show(content, "pan");
        pan.requestFocusInWindow();
        
        while (!pause) {
            long currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            deltaF += (currentTime - initialTime) / timeF;
            initialTime = currentTime;

            if (deltaU >= 1) {
                
                pan.go();
                ticks++;
                ticksTotal++;
                deltaU = 0;
            }

            if (deltaF >= 1) {
                pan.repaint();
                frames++;
                deltaF = 0;
            }

            if (System.currentTimeMillis() - timer >= 1000) {
                frames = 0;
                ticks = 0;
                timer = System.currentTimeMillis();
            }

            ms = System.currentTimeMillis() - startTime - pauseLong;
        }
        
        cl.show(content, "menu");
        menu.requestFocusInWindow();
    }

    public static void setPause() {
        pause = !pause;
        if(!pause){
            pauseLong += System.currentTimeMillis() - pauseStart;
            t = new Thread(new Menu());
            t.start();
            
        } else {
            pauseStart = System.currentTimeMillis();
        }
    }
    
    public static void start() {
        pause = false;
        Texture.initTexture(menu.getTexturePack());
        pan = new Panel();
        pan.init(menu.getMap(), menu.getTileset(), 1, menu.getListFantomes());
        pan.addKeyListener(clav);
        content.add(pan, "pan");
        pauseLong += System.currentTimeMillis() - pauseStart;
        t = new Thread(new Menu());
        t.start();
    }
    
    public static void stop() {
        pause = true;
        menu.removeAll();
        menu.selectionMenu();
        menu.setIdMenu(0);
        pauseLong = 0;
        startTime = System.currentTimeMillis();
        pauseStart = System.currentTimeMillis();
        ticksTotal = 0;
        Ghost.setScatter(true);
        menu.repaint();
    }
    
    public static void startLevelEditor() {
        menu.removeAll();
        editor = new Editor();
        content.add(editor, "editor");
        cl.show(content, "editor");
    }
    
    public static void setPanMouseListener(){
        pan.addMouseListener(pan);
    }
}