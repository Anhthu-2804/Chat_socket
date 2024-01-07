package server;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class server {
    private JFrame jframe;
    private boolean flag;
    private JTextArea content;
    private JTextField enterchat;
    private JButton send;
    private String temp = "";
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private JMenuBar jmenuBar;
    private BufferedImage myPicture;
    private JPanel Pimage;

    public server() {
        jframe = new JFrame();
        jframe.setTitle("Server.ChatGPT Plus");
        jframe.setSize(750, 540);
        jframe.setLayout(null);
        jframe.getContentPane().setBackground(Color.CYAN);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setResizable(false);

        jmenuBar = new JMenuBar();

        jframe.setJMenuBar(jmenuBar); // Thay đổi ở đây


        JMenu chatGpt5 = new JMenu("Chat Gpt5");
        JMenu About = new JMenu("Giới Thiệu");
        jmenuBar.add(About);
        jmenuBar.add(chatGpt5);
        JMenuItem gioiThieu = new JMenuItem("Gioi Thieu");
        About.add(gioiThieu);
        JMenuItem about = new JMenuItem("About");
        chatGpt5.add(about);
        JMenuItem exit = new JMenuItem("Exit");
        chatGpt5.add(exit);
        JMenuItem newChat = new JMenuItem("New Chat");
        chatGpt5.add(newChat);
        JMenuItem chatGPT = new JMenuItem("Chat GPT");
        chatGpt5.add(chatGPT);




        gioiThieu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Object[] options = {"OK"};
                JOptionPane.showConfirmDialog(jframe,
                        "Chào mừng bạn đến với ứng dụng chat bot", "Information",
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

    private void sendMessage() {
        try {
            temp += "Tôi: " + enterchat.getText() + "\n";
            SwingUtilities.invokeLater(() -> content.setText(temp)); //để đảm bảo cập nhật trên thread chính
            os.writeObject("chat," + enterchat.getText());
            enterchat.setText("");
            enterchat.requestFocus();
        } catch (Exception r) {
            r.printStackTrace();
        }
    }

    private void connectToClient() {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Đang đợi client...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client đã kết nối!");
            os = new ObjectOutputStream(clientSocket.getOutputStream());
            is = new ObjectInputStream(clientSocket.getInputStream());

            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        String stream = is.readObject().toString();
                        String[] data = stream.split(",");
                        if (data[0].equals("chat")) {
                            temp += "Client:" + data[1] + '\n';
                            SwingUtilities.invokeLater(() -> content.setText(temp));
                        } else if (data[0].equals("newchat")) {
                            // Gửi yêu cầu xác nhận tới người dùng
                            int response = JOptionPane.showConfirmDialog(jframe,
                                    "Client muốn tạo chat mới. Bạn có đồng ý không?",
                                    "Yêu cầu mới",
                                    JOptionPane.YES_NO_OPTION);
                            // Gửi phản hồi lại cho client
                            os.writeObject("newchatresponse," + (response == JOptionPane.YES_OPTION ? "yes" : "no"));
                            if (response == JOptionPane.YES_OPTION) {
                                clearChat();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void clearChat() {
        temp = ""; // Xóa nội dung lưu trữ của các tin nhắn
        content.setText(""); // Xóa nội dung hiển thị trên JTextArea
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(server::new);
    }
}
