package client;

import server.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class client {
    public static JFrame jframe;
    JButton[][] nut;
    boolean flat = true;

    JTextArea content;
    JTextField enterchat;
    JButton send;
    Timer thoigian;
    Integer second, minute;
    JLabel demthoigian;
    JLabel turn;
    TextField textField;
    JPanel p;
    JPanel Pimage;
    String temp = "";
    BufferedImage myPicture;

    // Server Socket
    Socket socket;
    ObjectOutputStream os;
    ObjectInputStream is;

    // MenuBar
    MenuBar menuBar;


    // Phương thức khởi tạo
    public client() {
        jframe = new JFrame();
        jframe.setTitle("Client.ChatGPT Plus");
        jframe.setSize(750, 540);
        jframe.setLayout(null);
        jframe.getContentPane().setBackground(Color.CYAN);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setResizable(false);

        menuBar = new MenuBar();
//        JLabel Title = new JLabel("ChatGPT plus");
//        Title.setForeground(Color.RED);
//        Title.setBounds(100, 10, 20, 20);
//        Title.setFont(new Font("Times New Roman", Font.BOLD, 16));

        jframe.setMenuBar(menuBar); // Thay đổi ở đây

//        jframe.add(Title);

        Menu chatGpt5 = new Menu("Chat Gpt5");
        Menu About = new Menu("Giới Thiệu");
        menuBar.add(About);
        menuBar.add(chatGpt5);
        MenuItem gioiThieu = new MenuItem("Gioi Thieu");
        About.add(gioiThieu);
        MenuItem about = new MenuItem("About");
        chatGpt5.add(about);
        MenuItem exit = new MenuItem("Exit");
        chatGpt5.add(exit);
        MenuItem newChat = new MenuItem("New Chat");
        chatGpt5.add(newChat);

        gioiThieu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Object[] options = {"OK"};
                JOptionPane.showConfirmDialog(jframe,
                        "Chào mừng bạn đến với ứng dụng chatGPT 5", "Information",
                        JOptionPane.CLOSED_OPTION);
            }
        });
        
        newChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearChat();
                try {
                    os.writeObject("newchat,123");
                } catch (IOException ie) {
                    //
                }
            }
        });

        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showConfirmDialog(jframe,
                        "Thư đẹp gái", "Information",
                        JOptionPane.CLOSED_OPTION);
            }
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Khung chat content
        Font fo = new Font("Arial", Font.BOLD, 15);
        content = new JTextArea();
        content.setFont(fo);
        content.setBackground(Color.white);
        content.setEditable(false);

        JScrollPane sp = new JScrollPane(content);
        sp.setBounds(40, 40, 670, 340);

        send = new JButton("Send"); // nút gửi
        send.setBounds(640, 420, 70, 40);
        enterchat = new JTextField(""); //khung nhập tin nhắn
        enterchat.setFont(fo);
        enterchat.setBounds(40, 420, 580, 40);
        enterchat.setBackground(Color.white);

        jframe.add(enterchat);
        jframe.add(send);
        jframe.add(sp);
        jframe.setVisible(true);

        enterchat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        connectToClient();
    }

    // Phương thức gửi tin nhắn
    private void sendMessage() {
        try {
            temp += "Tôi: " + enterchat.getText() + "\n";
            SwingUtilities.invokeLater(() -> content.setText(temp));
            os.writeObject("chat," + enterchat.getText());
            enterchat.setText("");
            enterchat.requestFocus();
        } catch (Exception r) {
            r.printStackTrace();
        }
    }


    private void connectToClient() {
        try {
            socket = new Socket("127.0.0.1", 1234);
            System.out.println("Đã kết nối tới server!");
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());

            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        String stream = is.readObject().toString();
                        String[] data = stream.split(",");
                        if (data[0].equals("chat")) {
                            temp += "Server:" + data[1] + '\n';
                            SwingUtilities.invokeLater(() -> content.setText(temp));
                        } else if (data[0].equals("newchatresponse")) {
                            // Xử lý phản hồi từ server
                            if (data[1].equals("yes")) {
                                clearChat();
                            } else {
                                JOptionPane.showMessageDialog(jframe, "Server từ chối tạo chat mới.");
                            }
                        } else if (data[0].equals("newchat")) {
                            // Gửi yêu cầu xác nhận tới người dùng
                            int response = JOptionPane.showConfirmDialog(jframe,
                                    "Server muốn tạo chat mới. Bạn có đồng ý không?",
                                    "Yêu cầu mới",
                                    JOptionPane.YES_NO_OPTION);
                            // Gửi phản hồi lại cho server
                            os.writeObject("newchatresponse," + (response == JOptionPane.YES_OPTION ? "yes" : "no"));
                            if (response == JOptionPane.YES_OPTION) {
                                clearChat();
                            }
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            });
            receiveThread.start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }



//    }

    private void clearChat() {
        temp = ""; // Xóa nội dung lưu trữ của các tin nhắn
        content.setText(""); // Xóa nội dung hiển thị trên JTextArea
    }


    public static void main(String[] args) {
        // Tạo đối tượng Server và gọi phương thức khởi tạo
        new client();
    }

}

