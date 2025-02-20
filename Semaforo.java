import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class Semaforo extends JPanel {
    private Map<String, SemaforoIntersecao> semaforos;
    private boolean pedidoPedestre;
    private Random random;
    private JFrame frame;
    private Timer timer;
    private int tempoGlobal;

    // Classe para representar cada interseção
    class SemaforoIntersecao {
        boolean sinalVerdeCarros;
        boolean sinalVerdePedestres;
        int tempoRestante;
        int tempoCarros;
        int tempoPedestres;
        int carrosPassados;
        int pedestresPassados;
        ArrayList<Carro> carros;
        ArrayList<Pedestre> pedestres;
        double satisfacaoLocal;

        SemaforoIntersecao(int tempoCarros, int tempoPedestres) {
            this.sinalVerdeCarros = true;
            this.sinalVerdePedestres = false;
            this.tempoCarros = tempoCarros;
            this.tempoPedestres = tempoPedestres;
            this.tempoRestante = tempoCarros;
            this.carrosPassados = 0;
            this.pedestresPassados = 0;
            this.satisfacaoLocal = 100.0;
            this.carros = new ArrayList<>();
            this.pedestres = new ArrayList<>();
        }

        void moverObjetos() {
            for (int i = carros.size() - 1; i >= 0; i--) {
                Carro carro = carros.get(i);
                carro.mover();
                if (carro.x > 800) {
                    carros.remove(i);
                    carrosPassados++; // Conta carros que saíram da tela
                }
            }
            for (int i = pedestres.size() - 1; i >= 0; i--) {
                Pedestre pedestre = pedestres.get(i);
                pedestre.mover();
                if (pedestre.y > 600) {
                    pedestres.remove(i);
                    pedestresPassados++; // Conta pedestres que saíram da tela
                }
            }
        }

        void ajustarTemposComIA() {
            double razaoFluxo = (carrosPassados > 0 && pedestresPassados > 0) ?
                               (double) carrosPassados / pedestresPassados : 1.0;
            if (razaoFluxo > 1.5 && tempoCarros < 30) {
                tempoCarros += 3;
                tempoPedestres = Math.max(5, tempoPedestres - 2);
            } else if (razaoFluxo < 0.5 && tempoPedestres < 25) {
                tempoPedestres += 3;
                tempoCarros = Math.max(10, tempoCarros - 2);
            }
            double esperaCarros = sinalVerdeCarros ? 0 : tempoRestante;
            double esperaPedestres = sinalVerdePedestres ? 0 : tempoRestante;
            satisfacaoLocal = 100 - (esperaCarros + esperaPedestres) * 0.5;
            // Não reseta os contadores aqui para manter o histórico no dashboard
        }

        void mudarSinal() {
            sinalVerdeCarros = !sinalVerdeCarros;
            sinalVerdePedestres = !sinalVerdePedestres;
            tempoRestante = sinalVerdeCarros ? tempoCarros : tempoPedestres;
            for (Carro carro : carros) {
                if (carro.x < 375) carro.velocidade = sinalVerdeCarros ? (random.nextDouble() * 1.5 + 1.0) : 0;
            }
            for (Pedestre pedestre : pedestres) {
                if (pedestre.y < 300) pedestre.velocidade = sinalVerdePedestres ? (random.nextDouble() * 1.5 + 1.0) : 0;
            }
        }
    }

    // Classe Carro
    class Carro {
        double x, y, velocidade;
        Carro(double x, double y) {
            this.x = x;
            this.y = y + random.nextInt(20) - 10;
            this.velocidade = random.nextDouble() * 1.5 + 1.0;
        }
        void mover() {
            if (x < 800) {
                boolean seguro = true;
                for (Pedestre pedestre : semaforos.get("main").pedestres) {
                    if (pedestre.y >= 250 && pedestre.y <= 400 && pedestre.velocidade > 0) {
                        seguro = false;
                        break;
                    }
                }
                if (seguro && x < 375) {
                    x += velocidade;
                } else if (x >= 375) {
                    x += velocidade;
                }
            } else {
                velocidade = 0;
            }
        }
        void desenhar(Graphics g, int offsetX) {
            g.setColor(Color.BLUE);
            g.fillRect((int)x + offsetX, (int)y, 30, 15);
            g.setColor(Color.BLACK);
            g.fillOval((int)x + 5 + offsetX, (int)y + 10, 5, 5);
            g.fillOval((int)x + 20 + offsetX, (int)y + 10, 5, 5);
            g.setColor(Color.YELLOW);
            g.fillRect((int)x + 25 + offsetX, (int)y + 2, 5, 3);
        }
    }

    // Classe Pedestre
    class Pedestre {
        double x, y, velocidade;
        int passo;
        Pedestre(double x, double y) {
            this.x = x + random.nextInt(10) - 5;
            this.y = y;
            this.velocidade = random.nextDouble() * 1.5 + 1.0;
            this.passo = 0;
        }
        void mover() {
            if (y < 600) {
                boolean seguro = true;
                for (Carro carro : semaforos.get("main").carros) {
                    if (carro.x >= 350 && carro.x <= 425 && carro.velocidade > 0) {
                        seguro = false;
                        break;
                    }
                }
                if (seguro) {
                    y += velocidade;
                    passo = (passo + 1) % 20;
                }
            } else {
                velocidade = 0;
            }
        }
        void desenhar(Graphics g, int offsetY) {
            g.setColor(Color.RED);
            g.fillOval((int)x, (int)y + offsetY, 10, 10);
            g.setColor(Color.BLACK);
            g.drawLine((int)x + 5, (int)y + 10 + offsetY, (int)x + 5, (int)y + 20 + offsetY);
            if (passo < 10) {
                g.drawLine((int)x + 5, (int)y + 20 + offsetY, (int)x + 2, (int)y + 30 + offsetY);
                g.drawLine((int)x + 5, (int)y + 20 + offsetY, (int)x + 8, (int)y + 30 + offsetY);
            } else {
                g.drawLine((int)x + 5, (int)y + 20 + offsetY, (int)x + 8, (int)y + 30 + offsetY);
                g.drawLine((int)x + 5, (int)y + 20 + offsetY, (int)x + 2, (int)y + 30 + offsetY);
            }
        }
    }

    public Semaforo() {
        this.semaforos = new HashMap<>();
        this.semaforos.put("main", new SemaforoIntersecao(20, 10));
        this.semaforos.put("secondary", new SemaforoIntersecao(15, 8));
        this.pedidoPedestre = false;
        this.random = new Random();
        this.tempoGlobal = 0;
        iniciarInterfaceGrafica();
        timer = new Timer(50, e -> atualizarSimulacao());
        timer.start();
    }

    private void iniciarInterfaceGrafica() {
        frame = new JFrame("Simulação Avançada de Tráfego com IA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.add(this);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Visualização", this);
        tabbedPane.addTab("Dashboard", new DashboardPanel());
        frame.add(tabbedPane, BorderLayout.CENTER);
        JButton botaoPedestre = new JButton("Solicitar Travessia (Principal)");
        botaoPedestre.addActionListener(e -> solicitarTravessia("main"));
        frame.add(botaoPedestre, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    // Painel de Dashboard aprimorado
    class DashboardPanel extends JPanel {
        private JLabel totalSatisfacao, totalCarros, totalPedestres, tempoMedioCarros, tempoMedioPedestres, emissaoEstimada;

        public DashboardPanel() {
            setLayout(new GridLayout(6, 2));
            setBackground(Color.WHITE);
            add(new JLabel("Métricas de Tráfego:", SwingConstants.CENTER));
            totalSatisfacao = new JLabel("Satisfação Total: 100.00%");
            totalCarros = new JLabel("Carros Totais: 0");
            totalPedestres = new JLabel("Pedestres Totais: 0");
            tempoMedioCarros = new JLabel("Tempo Médio Carros: 0.0s");
            tempoMedioPedestres = new JLabel("Tempo Médio Pedestres: 0.0s");
            emissaoEstimada = new JLabel("Emissões Estimadas: 0.0 kg CO2");
            
            add(new JLabel("Satisfação Total:"));
            add(totalSatisfacao);
            add(new JLabel("Carros Passados:"));
            add(totalCarros);
            add(new JLabel("Pedestres Passados:"));
            add(totalPedestres);
            add(new JLabel("Tempo Médio Carros:"));
            add(tempoMedioCarros);
            add(new JLabel("Tempo Médio Pedestres:"));
            add(tempoMedioPedestres);
            add(new JLabel("Emissões Estimadas:"));
            add(emissaoEstimada);

            // Timer para atualizar métricas a cada segundo
            new Timer(1000, e -> atualizarMetricas()).start();
        }

        private void atualizarMetricas() {
            double somaSatisfacao = 0;
            int totalCarrosCount = 0, totalPedestresCount = 0;
            double tempoTotalCarros = 0, tempoTotalPedestres = 0;
            int countCarros = 0, countPedestres = 0;

            for (Map.Entry<String, SemaforoIntersecao> entry : semaforos.entrySet()) {
                SemaforoIntersecao semaforo = entry.getValue();
                somaSatisfacao += semaforo.satisfacaoLocal;
                totalCarrosCount += semaforo.carrosPassados;
                totalPedestresCount += semaforo.pedestresPassados;

                // Calcula tempo médio para carros e pedestres (simulação simplificada)
                for (Carro carro : semaforo.carros) {
                    if (carro.x >= 0 && carro.x <= 800) {
                        tempoTotalCarros += (800 - carro.x) / carro.velocidade; // Tempo estimado para atravessar
                        countCarros++;
                    }
                }
                for (Pedestre pedestre : semaforo.pedestres) {
                    if (pedestre.y >= 250 && pedestre.y <= 600) {
                        tempoTotalPedestres += (600 - pedestre.y) / pedestre.velocidade; // Tempo estimado para atravessar
                        countPedestres++;
                    }
                }
            }

            double satisfacaoMedia = somaSatisfacao / semaforos.size();
            totalSatisfacao.setText("Satisfação Total: " + String.format("%.2f", satisfacaoMedia) + "%");
            totalCarros.setText("Carros Totais: " + totalCarrosCount);
            totalPedestres.setText("Pedestres Totais: " + totalPedestresCount);

            // Cálculo de tempos médios (simplificado)
            double tempoMedioC = countCarros > 0 ? tempoTotalCarros / countCarros : 0.0;
            double tempoMedioP = countPedestres > 0 ? tempoTotalPedestres / countPedestres : 0.0;
            tempoMedioCarros.setText("Tempo Médio Carros: " + String.format("%.1f", tempoMedioC) + "s");
            tempoMedioPedestres.setText("Tempo Médio Pedestres: " + String.format("%.1f", tempoMedioP) + "s");

            // Estimativa de emissões (simplificada, baseada em tempo de espera e tráfego)
            double emissao = (totalCarrosCount * 0.1 + totalPedestresCount * 0.01) * (100 - satisfacaoMedia) / 100; // kg CO2 fictício
            emissaoEstimada.setText("Emissões Estimadas: " + String.format("%.1f", emissao) + " kg CO2");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(173, 216, 230));

        int offsetY = 0;
        for (Map.Entry<String, SemaforoIntersecao> entry : semaforos.entrySet()) {
            SemaforoIntersecao semaforo = entry.getValue();
            desenharIntersecao(g, semaforo, offsetY);
            offsetY += 400;
        }
    }

    private void desenharIntersecao(Graphics g, SemaforoIntersecao semaforo, int offsetY) {
        g.setColor(new Color(50, 50, 50));
        g.fillRect(0, 300 + offsetY, 800, 100);
        g.setColor(Color.WHITE);
        for (int i = 0; i < 800; i += 40) {
            g.fillRect(i, 345 + offsetY, 20, 5);
        }
        g.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) {
            g.fillRect(375, 300 + i * 20 + offsetY, 50, 15);
        }
        g.setColor(new Color(100, 100, 100));
        g.fillRect(350, 400 + offsetY, 100, 200);
        g.setColor(Color.GRAY);
        g.fillRect(340, 200 + offsetY, 10, 100);
        g.fillRect(450, 200 + offsetY, 10, 100);
        g.setColor(semaforo.sinalVerdeCarros ? Color.GREEN : Color.RED);
        g.fillOval(335, 190 + offsetY, 20, 20);
        g.setColor(semaforo.sinalVerdePedestres ? Color.GREEN : Color.RED);
        g.fillOval(445, 190 + offsetY, 20, 20);
        for (Carro carro : semaforo.carros) carro.desenhar(g, 0);
        for (Pedestre pedestre : semaforo.pedestres) pedestre.desenhar(g, offsetY);
    }

    private void atualizarSimulacao() {
        tempoGlobal++;
        for (Map.Entry<String, SemaforoIntersecao> entry : semaforos.entrySet()) {
            SemaforoIntersecao semaforo = entry.getValue();
            if (random.nextInt(100) < 20 && semaforo.sinalVerdeCarros && semaforo.carros.size() < 10) {
                semaforo.carros.add(new Carro(-30, 330));
            }
            if (random.nextInt(100) < 20 && semaforo.sinalVerdePedestres && semaforo.pedestres.size() < 5) {
                semaforo.pedestres.add(new Pedestre(390, 250));
            }
            semaforo.moverObjetos();

            semaforo.tempoRestante--;
            if (semaforo.tempoRestante <= 0 || (pedidoPedestre && semaforo.sinalVerdeCarros && entry.getKey().equals("main"))) {
                semaforo.mudarSinal();
            }
            semaforo.ajustarTemposComIA();
        }
        repaint();
    }

    public synchronized void solicitarTravessia(String key) {
        if (key.equals("main") && !semaforos.get("main").sinalVerdePedestres) {
            pedidoPedestre = true;
            System.out.println("Pedestre solicitou travessia no semáforo principal!");
        }
    }

    public static void main(String[] args) {
        new Semaforo();
    }
}