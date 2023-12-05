import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import java.awt.Font;

public class Jogo extends JPanel implements Runnable {
  // Dimensões
  private int largura, altura; // Largura e altura da bola
  private int larguraRaquete = 14;
  private int comprimentoRaquete = 60;
  private int bolaX = 10, bolaY = 110; // Posição inicial da bola
  private int jogador1X = 10, jogador1Y = 0; // Posição inicial do raquete do jogador 1
  private int jogador2X = this.getWidth() - 35, jogador2Y = 0; // Posição inicial do raquete do jogador 2

  // Pontuações
  private int pontuacaoJogador1 = 0, pontuacaoJogador2 = 0;

  // Sinalizadores ou booleanos
  private boolean jogador1ParaCima, jogador1ParaBaixo, jogador2ParaCima, jogador2ParaBaixo;
  private boolean movendoDireita;
  private boolean jogando, jogoTerminado;
  private int vencedor = 0; // Sinalizador inteiro para o vencedor
  private boolean isServer; // Verifica se o jogo é executado por um servidor
  private boolean movendoParaCima = false;
  // Eventos especiais do jogo
  private int invisibilidade = 0; // Sinalizador inteiro para invisibilidade (1 se verdadeiro, 0 se falso)
  private boolean jogador2Invisivel, jogador1Invisivel; // Sinalizadores para os botões de ataque especial de
                                                        // invisibilidade
  private boolean jogador1Veloz, jogador2Velocidade; // Sinalizadores para os botões de ataque especial de aumento de
                                                     // velocidade da bola

  // Variáveis
  Thread jogo;
  private Server server;
  private String[] valores;
  private String mensagem;
  private int tamanhoPasso = 10; // Quantos pixels mudar para cada movimento
  private int passoBola = 5;
  private int tamanhoBola = 8;
  private int pontuaçãoVencedora = 11;

  /* Construtor */
  public Jogo(Server o, boolean isServer_) {
    server = o;
    isServer = isServer_;
    jogando = true; // Jogo não terminado
    jogo = new Thread(this); // Coloca a classe em uma Thread
    jogo.start(); // Inicia a Thread
  }

  // Desenhar bola e raquetes
  public void paintComponent(Graphics g) {
    setOpaque(true);
    super.paintComponent(g);

    // Desenhar fundo
    setBackground(Color.BLACK);

    // Desenhar bola
    g.setColor(Color.RED);
    if (invisibilidade == 1) {
      g.setColor(Color.BLACK);
    }
    g.fillOval(bolaX, bolaY, tamanhoBola, tamanhoBola);

    g.setColor(Color.WHITE);
    // Desenhar raquetes
    g.fillRect(jogador1X, jogador1Y, larguraRaquete, comprimentoRaquete);
    g.fillRect(jogador2X, jogador2Y, larguraRaquete, comprimentoRaquete);

    Font placarFont = new Font("ArcadeClassic", Font.BOLD, 40); // Escolha a fonte, estilo e tamanho
    g.setFont(placarFont);

    // Desenhar pontuações centralizadas
    String placar = pontuacaoJogador1 + "            " + pontuacaoJogador2;
    int larguraPlacar = g.getFontMetrics().stringWidth(placar);
    int xPlacar = (this.getWidth() - larguraPlacar) / 2;
    int yPlacar = 40;

    g.setColor(Color.RED);
    g.drawString(placar, xPlacar, yPlacar);

    g.setColor(Color.GRAY); // Escolha a cor da rede
    int meio = this.getWidth() / 2; // Posição central da tela em termos de largura
    int tamanhoPonto = 14; // Altura de cada ponto da rede
    int espacoPonto = 12; // Espaço entre os pontos

    for (int y = 0; y < this.getHeight(); y += tamanhoPonto + espacoPonto) {
      g.fillRect(meio - 1, y, 8, tamanhoPonto); // Desenha um retângulo (ponto da rede)
    }

    if (jogoTerminado) {
      Font fimDeJogoFont = new Font("ArcadeClassic", Font.BOLD, 40);
      g.setFont(fimDeJogoFont);
      g.setColor(Color.RED); // Escolha a cor da mensagem

      String fimDeJogo = "Fim   de  Jogo";
      int larguraFimDeJogo = g.getFontMetrics().stringWidth(fimDeJogo);
      int xFimDeJogo = (this.getWidth() - larguraFimDeJogo) / 2; // Centraliza a mensagem na tela
      int yFimDeJogo = (this.getHeight() / 2); // Posição y para a mensagem

      g.drawString(fimDeJogo, xFimDeJogo, yFimDeJogo);
    }
  }

  // Posições em X e Y para a bola
  public void posicionarBola(int nx, int ny) {
    bolaX = nx;
    bolaY = ny;
    this.largura = this.getWidth();
    this.altura = this.getHeight();
    repaint();
  }

  // Listeners de eventos para mover o raquete
  public void keyPressed(KeyEvent evento) {
    if (isServer) {
      switch (evento.getKeyCode()) {
        // Mover raquete do Jogador 1
        case KeyEvent.VK_W:
          jogador1ParaCima = true;
          break;
        case KeyEvent.VK_S:
          jogador1ParaBaixo = true;
          break;
        case KeyEvent.VK_C:
          jogador1Veloz = true;
          break;
        case KeyEvent.VK_X:
          jogador1Invisivel = true;
          break;
      }
    } else {
      switch (evento.getKeyCode()) {
        // Mover raquete do Jogador 2
        case KeyEvent.VK_UP:
          server.send("P2UP");
          break;
        case KeyEvent.VK_DOWN:
          server.send("P2DOWN");
          break;
        case KeyEvent.VK_C:
          server.send("P2SPD");
          break;
        case KeyEvent.VK_X:
          server.send("P2INV");
          break;
      }
    }
  }

  // Listeners de eventos para parar o movimento do raquete
  public void keyReleased(KeyEvent evento) {
    if (isServer) {
      switch (evento.getKeyCode()) {
        // Mover raquete do Jogador 1
        case KeyEvent.VK_W:
          jogador1ParaCima = false;
          break;
        case KeyEvent.VK_S:
          jogador1ParaBaixo = false;
          break;
        case KeyEvent.VK_C:
          jogador1Veloz = false;
          break;
        case KeyEvent.VK_X:
          jogador1Invisivel = false;
          break;
      }
    } else {
      switch (evento.getKeyCode()) {
        // Mover raquete do Jogador 2
        case KeyEvent.VK_UP:
          server.send("!P2UP");
          break;
        case KeyEvent.VK_DOWN:
          server.send("!P2DOWN");
          break;
        case KeyEvent.VK_C:
          server.send("!P2SPD");
          break;
        case KeyEvent.VK_X:
          server.send("!P2INV");
          break;
      }
    }
  }

  // Scripts para mover os raquetes
  public void moverRaquetes() {
    if (jogador1Y + comprimentoRaquete <= this.getHeight() && jogador1Y - 1 >= 0) {
      if (jogador1ParaCima) {
        jogador1Y -= tamanhoPasso;
        repaint();
      }
      if (jogador1ParaBaixo) {
        jogador1Y += tamanhoPasso;
        repaint();
      }
    } else {
      if (jogador1Y + comprimentoRaquete > this.getHeight()) {
        jogador1Y = this.getHeight() - comprimentoRaquete;
        repaint();
      } else if (jogador1Y - 1 <= 0) {
        jogador1Y = 1;
        repaint();
      }
    }

    if (jogador2Y + comprimentoRaquete <= this.getHeight() && jogador2Y - 1 >= 0) {
      if (jogador2ParaCima) {
        jogador2Y -= tamanhoPasso;
        repaint();
      }
      if (jogador2ParaBaixo) {
        jogador2Y += tamanhoPasso;
        repaint();
      }
    } else {
      if (jogador2Y + comprimentoRaquete > this.getHeight()) {
        jogador2Y = this.getHeight() - comprimentoRaquete;
        repaint();
      } else if (jogador2Y - 1 <= 0) {
        jogador2Y = 1;
        repaint();
      }
    }
    repaint();
  }

  public void run() {
    while (true) // loop infinito
    {
      jogador2X = this.getWidth() - 20;
      jogador1X = 10;
      if (jogando) {
        if (isServer) {
          server.send(
              bolaX + "," + bolaY + "," + jogador1Y + "," + jogador2Y + "," + pontuacaoJogador1 + ","
                  + pontuacaoJogador2 + "," + vencedor + "," + passoBola + "," + invisibilidade);
          analisarMensagem();
          moverBola();
          posicionarBola(bolaX, bolaY);
          // Atraso de 50 milissegundos
          try {
            Thread.sleep(10);
          } catch (InterruptedException ex) {
          }
          moverRaquetes();

          // Aumentar a pontuação do jogador 2
          if (bolaX <= 0) {
            bolaAleatória();
            reiniciarBola();
            pontuacaoJogador2++;

          }
          if (bolaX + 8 >= this.getWidth()) {
            bolaAleatória();
            reiniciarBola();
            pontuacaoJogador1++;
          }
          // Fim de Jogo.
          // Quando a pontuação atingir o valor, o jogo terminará
          if (pontuacaoJogador1 == pontuaçãoVencedora || pontuacaoJogador2 == pontuaçãoVencedora) {
            jogando = false;
            jogoTerminado = true;
            vencedor = 1;
          }
          // A bola colide com o jogador 1
          if (bolaX <= jogador1X + larguraRaquete && bolaY >= jogador1Y
              && bolaY <= (jogador1Y + comprimentoRaquete)) {
            movendoDireita = true;
            if (jogador1Veloz) {
              passoBola = passoBola + 5;
            } else if (jogador1Invisivel) {
              invisibilidade = 1;
            }
          }
          // A bola colide com o jogador 2

          if (bolaX >= (jogador2X - tamanhoBola) && bolaY >= jogador2Y
              && bolaY <= (jogador2Y + comprimentoRaquete)) {
            movendoDireita = false;
            if (jogador2Velocidade) {
              passoBola = passoBola + 5;
            } else if (jogador2Invisivel) {
              invisibilidade = 1;
            }
          }
        } else {
          mensagem = server.recv();
          if (mensagem.equals("")) {

          } else {
            valores = mensagem.split(",");
            bolaX = Integer.parseInt(valores[0]);
            bolaY = Integer.parseInt(valores[1]);
            jogador1Y = Integer.parseInt(valores[2]);
            jogador2Y = Integer.parseInt(valores[3]);
            pontuacaoJogador1 = Integer.parseInt(valores[4]);
            pontuacaoJogador2 = Integer.parseInt(valores[5]);
            passoBola = Integer.parseInt(valores[7]);
            invisibilidade = Integer.parseInt(valores[8]);
            if (Integer.parseInt(valores[6]) == 0) {

            } else if (Integer.parseInt(valores[6]) == 1) {
              jogando = false;
              jogoTerminado = true;
            }
            posicionarBola(bolaX, bolaY);
            moverRaquetes();
            try {
              Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
          }
        }
      }
    }
  }

  public void moverBola() {
    if (movendoDireita) {
      bolaX += passoBola;
      if (bolaX >= (largura - tamanhoBola))
        movendoDireita = false;
    } else {
      bolaX += -passoBola;
      if (bolaX <= 0)
        movendoDireita = true;
    }
    if (movendoParaCima) {
      bolaY += passoBola;
      if (bolaY >= (altura - tamanhoBola)) {
        movendoParaCima = false;
      }
    } else {
      bolaY += -passoBola;
      if (bolaY <= 0) {
        movendoParaCima = true;
      }
    }
  }

  public void mouseMoved(MouseEvent e) {
    if (isServer) {
      jogador1Y = e.getY() - (comprimentoRaquete / 2);
    } else {
      jogador2Y = e.getY() - (comprimentoRaquete / 2);
      // Envia a nova posição Y do raquete do jogador 2 para o servidor
      server.send("Y:" + jogador2Y);
      System.out.println("Y");
    }
  }

  public void reiniciarBola() {
    invisibilidade = 0;
    passoBola = 5;
  }

  public void bolaAleatória() {
    bolaX = (jogador2X - jogador1X) / 2;
  }

  public void analisarMensagem() {
    mensagem = server.recv();
    System.out.println(mensagem);
    if (mensagem.equals("P2UP")) {
      jogador2ParaCima = true;
    } else if (mensagem.equals("P2DOWN")) {
      jogador2ParaBaixo = true;
    } else if (mensagem.equals("!P2DOWN")) {
      jogador2ParaBaixo = false;
    } else if (mensagem.equals("!P2UP")) {
      jogador2ParaCima = false;
    } else if (mensagem.contains("Y:")) {
      System.out.println("Y");
      String valor[] = mensagem.split(":");
      jogador2Y = Integer.parseInt(valor[1]);
    }
    // Verifica se o golpe de velocidade extra para o jogador 2 é verdadeiro
    else if (mensagem.equals("P2SPD")) {
      jogador2Velocidade = true;
    }
    // Verifica se o golpe de velocidade extra para o jogador 2 não é verdadeiro
    else if (mensagem.equals("!P2SPD")) {
      jogador2Velocidade = false;
    } else if (mensagem.equals("P2INV")) {
      jogador2Invisivel = true;
    } else if (mensagem.equals("!P2INV")) {
      jogador2Invisivel = false;
    }
  }
}
