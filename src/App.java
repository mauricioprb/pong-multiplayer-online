import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class App extends JFrame {
    private JPanel jContentPane;
    private Jogo jogo;
    private String resultado;
    Server s;
    private boolean isServer;

    public App() {
        super();
        resultado = JOptionPane.showInputDialog("Servidor ou Cliente?");

        if (resultado.equalsIgnoreCase("servidor")) {
            s = new Server();
            isServer = true;
        } else if (resultado.equalsIgnoreCase("cliente")) {
            String ip;
            ip = JOptionPane.showInputDialog("Endereço IP:");
            s = new Server(ip);
            isServer = false;
        } else {
            throw new IllegalArgumentException("Cliente ou servidor esperado, obtido " + resultado);
        }

        s.start(); // Começa a conexão
        initializeScreen(); // renderiza a tela

        this.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                myKeyReleased(evt);
            }

            public void keyPressed(KeyEvent evt) {
                myKeyPressed(evt);
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                jogo.mouseMoved(e);
            }
        });
    }

    private Jogo getJogo() {
        if (jogo == null) {
            jogo = new Jogo(s, isServer); // Criar novo jogo
        }
        return jogo;
    }

    // Envia a tecla pressionada para o jogo
    private void myKeyPressed(KeyEvent e) {
        jogo.keyPressed(e);
    }

    // Envia a tecla solta para o jogo
    private void myKeyReleased(KeyEvent e) {
        jogo.keyReleased(e);
    }

    // Renderiza a tela
    private void initializeScreen() {
        this.setResizable(false);
        this.setBounds(new Rectangle(100, 184, 1000, 500)); // Posição na desktop
        this.setContentPane(getJContentPane());
        this.setTitle("Pong " + resultado);
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJogo(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                App thisClass = new App();
                thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                thisClass.setVisible(true);
            }
        });
    }
}
