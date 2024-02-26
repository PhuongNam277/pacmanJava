package main;

import java.awt.CardLayout;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Sound.SoundEffect;
import State.*;
import entity.Monster.Monster;
import entity.Player.*;

public class MyFrame extends JFrame implements ActionListener, Runnable {
    PlayerMove playermove = new PlayerMove();
    Player player = new Player(this, playermove);
    SoundEffect soundMain = new SoundEffect();
    public SoundEffect soundInternal = new SoundEffect();
    SoundEffect soundNext = new SoundEffect();
    Monster monster = new Monster(this);
    
    Thread gameThread;

    String Ending;
    String nameCardLayout;

    public int jframeWidth = 615, jframeHeight = 615;
    public int jframeHeightParent=690;
    public int countFoot = 0;
    int FPS = 60;
    JPanel cardPanel; // Use JPanel instead of JLayeredPane

    // Tạo các object để sử dụng các biến của chúng
    Intro intro = new Intro(this);
    Trailer trailer = new Trailer(this);
    FirstMap firstMap = new FirstMap(this);
    SecondMap secondMap = new SecondMap(this);
    ThirdMap thirdMap = new ThirdMap(this);

    CardLayout cardLayout = new CardLayout();

    MyFrame() {

        cardPanel = new JPanel(); // Use JPanel instead of JLayeredPane
        cardPanel.setBounds(0, 0, jframeWidth, jframeHeightParent);

        // Sử dụng CardLayout cho cardPanel
        cardPanel.setLayout(cardLayout);

        // Thêm các panel vào cardPanel với tên đặc biệt
        cardPanel.add(thirdMap.thirdMapPanel, "ThirdMap");
        cardPanel.add(secondMap.secondMapPanel, "SecondMap");
        cardPanel.add(firstMap.firstMapPanel, "FirstMap");
        cardPanel.add(trailer.trailerPanel, "Trailer");
        cardPanel.add(intro.introPanel, "Intro");

        // Thể hiện thẻ đầu tiên (ở đây là Intro)
        nameCardLayout = "Intro";
        cardLayout.show(cardPanel, nameCardLayout);
        // Thêm sound cho phần intro
        if (nameCardLayout == "Intro") {
            soundMain.setFile(0);
            soundMain.start();
            soundMain.loop();
        }
        // Setup Jframe
        this.init();

        // Thêm ActionListener cho nút "Start" trong Intro
        intro.Start.addActionListener(this);

        // Thêm ActionListener cho nút "Exit" trong Intro
        intro.Exit.addActionListener(this);

        // Thêm ActionListener cho nút "nextButton" trong Intro
        trailer.nextButton.addActionListener(this);
        // Thêm ActionListener cho nút "skipButton" trong Intro
        // trailer.skipButton.addActionListener(this);

    }

    // Hàm setup các dữ liệu ban đầu của jframe
    public void init() {
        ImageIcon logo = new ImageIcon("./picture/logo.jpg");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(jframeWidth, jframeHeightParent);
        this.setTitle("Journey to Adulthood");
        this.setIconImage(logo.getImage());
        this.setResizable(false);
        this.setLayout(null);
        this.setLocationRelativeTo(null);
        this.add(cardPanel);
        this.addKeyListener(playermove);
        this.setFocusable(true);
        this.StartGame();
        this.setVisible(true);
    }

    // Hàm viết logic các nút bấm
    @Override
    public void actionPerformed(ActionEvent e) {
        // Xử lý sự kiện khi nút "Start" được nhấn
        if (e.getSource() == intro.Start) {
            // Dừng âm thanh hiện tại
            soundMain.stop();
            // Chuyển sang cửa sổ Trailer
            nameCardLayout = "Trailer";
            cardLayout.show(cardPanel, nameCardLayout);
            trailer.timer.start();

            // Thay đổi âm thanh phần intro thành trailer
            soundMain.setFile(2);
            ;
            soundMain.start();
            // Tạo và bắt đầu sử dụng âm thanh gõ phím
            soundInternal.setFile(1);
            soundInternal.start();
            soundInternal.loop();
        // } else if (e.getSource() == trailer.skipButton) {
            //     // Dừng âm thanh gõ phím
            //     soundInternal.stop();
        } else if (e.getSource() == trailer.nextButton) {
            // Dừng âm thanh phần trailer
            soundInternal.stop();
            soundMain.stop();
            nameCardLayout = "FirstMap";
            cardLayout.show(cardPanel, nameCardLayout);
            // Thay đổi âm thanh Trailer sang âm thanh của map
            soundMain.setFile(3);
            soundMain.start();
            soundMain.loop();
            // Tạo âm thanh ăn vật phẩm 
            soundInternal.setFile(4);
        }

        // Xử lý sự kiện khi nút "Exit" được nhấn
        else if (e.getSource() == intro.Exit) {
            // Thoát ứng dụng
            // Tạo 1 bảng thông báo để xác nhận có muốn thoát k ,nếu có thì thoát
            int exitis = JOptionPane.showConfirmDialog(null, "Bạn có chắc muốn thoát", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (exitis == 0) {
                System.exit(exitis);
            }
        }
    }

    // Hàm để sử dụng gameloop
    public void StartGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Tạo hệ thống game loop để làm animation cho nhân vật với 60 khung hình trên
    // giây
    @Override
    public void run() {
        double drawInteval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInteval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                getTransform();
                if (nameCardLayout == "FirstMap" || nameCardLayout == "SecondMap" || nameCardLayout == "ThirdMap") {
                    PlayerVsMonster();
                }
                repaint();
                delta--;
            }
          
        }

    }

    // Hàm để update chuyển động ,tọa độ khi nhân vật ,monster di chuyển
    public void update() {
        player.update();
        if (nameCardLayout == "FirstMap" || nameCardLayout == "SecondMap" || nameCardLayout == "ThirdMap") {
            countFoot++;
        }
        monster.running();
    }

//Ham viet logic biến hình cho nhân vật
    public void transform(Map DefaultMap,boolean addHeart,boolean removeHeart,int heartXLocation,int heartYLocation,JPanel childJPanel,JLabel heart){
       double dem=Math.floor((Math.random()*2)+1);
    
       //Set các điều kiện để nhân vật có thể biến hình
     int X=(heartXLocation+secondMap.newImageIconHeart.getIconWidth())-(player.PlayerWidth+player.PlayerPositionX);
     int Y=(heartYLocation+secondMap.newImageIconHeart.getIconHeight())-(player.PLayerHeight+player.PlayerPositionY);
       if (X>=-30 && X<=3 && Y>=-30 && Y<=3 && addHeart==true  && removeHeart==false  ) {
        //Nhân vật không thể biến hình và bị giảm 500 điểm
            if (dem==1) {
            player.imgName="";
         }
         //Cho nhân vật biến hình và sau 10s về như cũ
         if (dem==2) {
            player.imgName="Attack";
            soundNext.setFile(5);
            soundNext.start();
            
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
              @Override
              public void run() {
                  player.imgName = "";
                  timer.cancel();
              }
          }, 10000);
         }
        // Xóa hình trái tym trên map
         childJPanel.remove(heart);
         DefaultMap.addHeart=false;
         DefaultMap.removeHeart=true;
          
      }
          player.getPlayerImage(player.imgName);
}

   
//Hàm Thực hiện biến hình qua mỗi map
    public void getTransform(){
        switch (nameCardLayout) {
            case "FirstMap":
                transform(firstMap, firstMap.addHeart, firstMap.removeHeart, firstMap.heartXLocation, firstMap.heartYLocation, firstMap.childFirstMapPanel, firstMap.heart);
                break;
            case "SecondMap":
                transform(secondMap, secondMap.addHeart, secondMap.removeHeart, secondMap.heartXLocation, secondMap.heartYLocation, secondMap.childSecondMapPanel, secondMap.heart);
            break;
        }
    }
    public boolean removeImage( int xMonsterImg,int yMonsterImg){
        int X=(xMonsterImg+monster.MonsterWidth)-(player.PlayerWidth+player.PlayerPositionX);
        int Y=(yMonsterImg+monster.MonsterHeight)-(player.PLayerHeight+player.PlayerPositionY);
        if (X>=-30 && X<=3 && Y>=-30 && Y<=3){
            return true;
        }
        return false;
    } 
    public void removePlayer(){
    String TitleEnding[]={
        "Bạn đã trượt đại học do vướng vào con đường cờ bạc.Bạn có muốn làm lại cuộc đời?",
        "Bạn đã trượt đại học do quá đắm chìm vào sự thú vị ,hay ho của game.Bạn có muốn làm lại cuộc đời?",
        "Bạn đã bị nghiện do vô tình dẵm vào kim tiêm của người nghiện.Bạn có muốn làm lại cuộc đời?",
    };
   boolean pVsDice=removeImage(monster.xDice, monster.yDice);
   boolean pVsSyrinnge=removeImage(monster.xSyrinnge, monster.ySyrinnge);
   boolean pVsJoystick=removeImage(monster.xJoystick, monster.yJoystick);
    if (pVsDice==true || pVsSyrinnge==true || pVsJoystick==true) {
        if (pVsDice==true) {
            Ending=TitleEnding[0];
        }
        else if(pVsJoystick==true){
            Ending=TitleEnding[1];
        }
        else if(pVsSyrinnge==true){
            Ending=TitleEnding[2];
        }
        int exitis = JOptionPane.showConfirmDialog(null, Ending, "Xác nhận",
        JOptionPane.YES_NO_OPTION);
        if (exitis==0) {
            soundMain.stop();
            player.setDefaultPlayer();
            player.getPlayerImage(player.imgName);
            nameCardLayout="Intro";
            cardLayout.show(cardPanel, nameCardLayout);
            soundMain.setFile(0);
            soundMain.start();
            
        }
        else{
            System.exit(0);
        }
    }
     
    
    }
     public void removeMonster(){
        if (removeImage(monster.xDice, monster.yDice)==true) {
             monster.dice=null;   
        }
        else if (removeImage(monster.xSyrinnge, monster.ySyrinnge)==true) {
            monster.syrinnge=null;   
       }
       else if (removeImage(monster.xJoystick, monster.yJoystick)==true) {
        monster.joystick=null;   
   }
     }
     public void PlayerVsMonster(){
        if (player.imgName=="") {
            removePlayer();
        }
        else if (player.imgName=="Attack") {
            removeMonster();
        }
       }
    // hàm vẽ nhân vật
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        if (nameCardLayout == "FirstMap" || nameCardLayout == "SecondMap" || nameCardLayout == "ThirdMap") {
            player.draw(g2);
            monster.draw(g2);
            repaint();
        }
        g2.dispose();

    }

}
