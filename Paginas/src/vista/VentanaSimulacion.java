package vista;

import controladores.Controller;
import controladores.FIFO; 
import controladores.MMU;
import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors; 
import javax.swing.Timer;
import modelos.Page;

/**
 * Ventana principal que muestra la simulación en tiempo real.
 * CORREGIDA: Esta clase ahora es la única dueña del Timer y recibe la velocidad inicial.
 *
 * @author wess
 */
public class VentanaSimulacion extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaSimulacion.class.getName());

    private Controller controller;
    private Timer simulationTimer; // El Timer de refresco de la GUI
    private static final int RAM_SIZE_KB = 400; 
    
    private Menu menuPrincipal; // Referencia para poder volver

    /**
     * Creates new form VentanaSimulacion
     * @param menu La ventana de Menú original, para poder volver a ella.
     * @param controller El controlador ya configurado desde el Menú.
     * @param initialDelay La velocidad inicial del timer (en ms) seleccionada en el menú.
     */
    public VentanaSimulacion(Menu menu, Controller controller, int initialDelay) {
        this.controller = controller;
        this.menuPrincipal = menu; // Guardar la referencia al menú
        
        initComponents();
        
        this.setTitle("Simulación de Paginación en Progreso");
        this.setLocationRelativeTo(null); 
        
        // --- IMPORTANTE: Al cerrar esta ventana, NO cerrar toda la app ---
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE); 
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Si el usuario cierra la ventana, también detenemos el timer
                // y mostramos el menú principal
                pauseSimulationVisuals();
                menuPrincipal.setVisible(true);
            }
        });

        String algNombre = controller.getMmuUser().getAlgorithm().getAlgorithmName();
        panelUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder(
                "Algoritmo Seleccionado: " + algNombre));

        // --- FIX DE NULLPOINTEREXCEPTION ---
        // 1. Inicializar el Timer primero.
        simulationTimer = new Timer(initialDelay, e -> runStep());
        
        // 2. Establecer el valor del slider (esto dispara el evento, pero el timer ya existe).
        sliderVelocidad.setValue(initialDelay); // Refleja la velocidad seleccionada en el menú
        // --- FIN DEL FIX ---
        
        // Ocultar el botón de volver
        btnVolverMenu.setEnabled(false);
        
        // Iniciar la simulación (llama a playSimulation)
        playSimulation();
    }

    /**
     * Ejecuta un solo paso de la simulación y actualiza la GUI.
     * Este método es llamado por el Timer.
     */
    private void runStep() {
        // Llama a stepSimulation (que verifica internamente si está en pausa)
        controller.stepSimulation();

        // Actualizar toda la interfaz gráfica
        updateGUI();
        
        // Si la simulación se pausó a sí misma (porque se acabó),
        // actualizar el botón.
        if (controller.isPaused() || controller.isSimulationEnded()) {
            pauseSimulationVisuals();
        }
    }

    /**
     * Extrae todos los datos de las MMUs y actualiza las etiquetas y
     * visualizaciones de RAM.
     */
    private void updateGUI() {
        MMU mmuOpt = controller.getMmuOpt();
        MMU mmuUser = controller.getMmuUser();

        // --- Actualizar Panel ÓPTIMO ---
        if (mmuOpt != null) {
            updateStatsPanel(mmuOpt, lblOptTiempo, lblOptRam, lblOptVRam, 
                             lblOptThrashing, lblOptFrag, lblOptProcesos);
            updateRamVisual(mmuOpt.getComputer().getRam(), txtOptRam);
        }

        // --- Actualizar Panel USUARIO ---
        if (mmuUser != null) {
            updateStatsPanel(mmuUser, lblUserTiempo, lblUserRam, lblUserVRam, 
                             lblUserThrashing, lblUserFrag, lblUserProcesos);
            updateRamVisual(mmuUser.getComputer().getRam(), txtUserRam);
        }
    }
    
    /**
     * Método ayudante para actualizar un bloque de estadísticas.
     */
    private void updateStatsPanel(MMU mmu, javax.swing.JLabel tiempo, 
                                  javax.swing.JLabel ram, javax.swing.JLabel vram,
                                  javax.swing.JLabel thrashing, javax.swing.JLabel frag,
                                  javax.swing.JLabel procesos) {
        
        long tiempoTotal = mmu.getTotalTime();
        long tiempoThrashing = mmu.getThrashingTime();
        int ramUsadaKB = mmu.getComputer().getRealMemoryUsed();
        int vramUsadaKB = mmu.getComputer().getVirtualMemoryUsed();
        int fragmentacion = mmu.getComputer().getTotalRamFragmentation();
        
        double ramPct = (RAM_SIZE_KB == 0) ? 0 : (ramUsadaKB / (double) RAM_SIZE_KB) * 100.0;
        double vramPct = (RAM_SIZE_KB == 0) ? 0 : (vramUsadaKB / (double) RAM_SIZE_KB) * 100.0;
        double thrashingPct = (tiempoTotal == 0) ? 0 : 
                              (tiempoThrashing / (double) tiempoTotal) * 100.0;
        
        int procesosActivos = 0;
        if (mmu.getProcessMap() != null) {
            for (modelos.Process p : mmu.getProcessMap().values()) {
                if (p.isActive()) {
                    procesosActivos++;
                }
            }
        }

        tiempo.setText(String.format("%d s", tiempoTotal));
        ram.setText(String.format("%d KB (%.1f%%)", ramUsadaKB, ramPct));
        vram.setText(String.format("%d KB (%.1f%%)", vramUsadaKB, vramPct));
        thrashing.setText(String.format("%d s (%.1f%%)", tiempoThrashing, thrashingPct));
        frag.setText(String.format("%d B", fragmentacion));
        procesos.setText(String.format("%d", procesosActivos));
        
        if (thrashingPct > 50.0) {
            thrashing.setForeground(Color.RED);
        } else {
            thrashing.setForeground(Color.BLACK); 
        }
    }
    
    /**
     * Método ayudante para renderizar el estado de la RAM.
     */
    private void updateRamVisual(List<Page> ram, javax.swing.JTextArea textArea) {
        StringBuilder sb = new StringBuilder();
        // Asegurarse de que ram no sea nula (importante durante la inicialización)
        if (ram == null) {
            textArea.setText("Inicializando RAM...");
            return;
        }
        
        for (int i = 0; i < ram.size(); i++) {
            Page page = ram.get(i);
            sb.append(String.format("Frame %02d: ", i));
            if (page == null) {
                sb.append("[ VACÍO ]\n");
            } else {
                sb.append(String.format("[ PID: %d ]\n", page.getId()));
            }
        }
        textArea.setText(sb.toString());
        textArea.setCaretPosition(0); 
    }
    
    /** Inicia/Reanuda el Timer y actualiza la lógica del Controller */
    private void playSimulation() {
        controller.resumeSimulation();
        simulationTimer.start();
        btnPlayPause.setText("Pausar");
    }
    
    /** Pausa el Timer y actualiza la lógica del Controller */
    private void pauseSimulationVisuals() {
        controller.pauseSimulation(); // Asegura que isPaused = true
        simulationTimer.stop();
        btnPlayPause.setText("Reanudar");
        
        // Si la simulación ha terminado, cambiar estado de botones
        if (controller.isSimulationEnded()) {
            btnPlayPause.setText("Finalizado");
            btnPlayPause.setEnabled(false);
            sliderVelocidad.setEnabled(false);
            btnVolverMenu.setEnabled(true); // Activar el botón para volver
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        panelControles = new javax.swing.JPanel();
        btnPlayPause = new javax.swing.JButton();
        lblVelocidad = new javax.swing.JLabel();
        sliderVelocidad = new javax.swing.JSlider();
        btnVolverMenu = new javax.swing.JButton();
        splitPaneSimulacion = new javax.swing.JSplitPane();
        panelOpt = new javax.swing.JPanel();
        panelOptStats = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblOptTiempo = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblOptRam = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblOptVRam = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblOptThrashing = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblOptFrag = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblOptProcesos = new javax.swing.JLabel();
        scrollOptRam = new javax.swing.JScrollPane();
        txtOptRam = new javax.swing.JTextArea();
        panelUsuario = new javax.swing.JPanel();
        panelUserStats = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lblUserTiempo = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblUserRam = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblUserVRam = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lblUserThrashing = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lblUserFrag = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        lblUserProcesos = new javax.swing.JLabel();
        scrollUserRam = new javax.swing.JScrollPane();
        txtUserRam = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnPlayPause.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnPlayPause.setText("Pausar");
        btnPlayPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayPauseActionPerformed(evt);
            }
        });

        lblVelocidad.setText("Velocidad:");

        // --- LÍMITES DE VELOCIDAD MODIFICADOS ---
        sliderVelocidad.setMajorTickSpacing(250);
        sliderVelocidad.setMaximum(2000);
        sliderVelocidad.setMinimum(12); // Mínimo de 12ms
        sliderVelocidad.setMinorTickSpacing(13); // Paso mínimo
        sliderVelocidad.setPaintTicks(true);
        sliderVelocidad.setToolTipText("Tiempo en ms por paso (Mín: 12ms)"); // Tooltip actualizado
        sliderVelocidad.setValue(200);
        sliderVelocidad.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderVelocidadStateChanged(evt);
            }
        });

        btnVolverMenu.setText("Volver al Menú");
        btnVolverMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverMenuActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelControlesLayout = new javax.swing.GroupLayout(panelControles);
        panelControles.setLayout(panelControlesLayout);
        panelControlesLayout.setHorizontalGroup(
            panelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelControlesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnPlayPause, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                // --- POSICIÓN DEL BOTÓN REGRESAR ---
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVolverMenu)
                // --- FIN POSICIÓN ---
                .addGap(18, 18, 18)
                .addComponent(lblVelocidad)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sliderVelocidad, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelControlesLayout.setVerticalGroup(
            panelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelControlesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnPlayPause, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    // --- POSICIÓN DEL BOTÓN REGRESAR ---
                    .addComponent(btnVolverMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    // --- FIN POSICIÓN ---
                    .addGroup(panelControlesLayout.createSequentialGroup()
                        .addGroup(panelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblVelocidad)
                            .addComponent(sliderVelocidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        splitPaneSimulacion.setDividerLocation(400);
        splitPaneSimulacion.setResizeWeight(0.5);

        panelOpt.setBorder(javax.swing.BorderFactory.createTitledBorder("Algoritmo Óptimo (OPT)"));
        panelOpt.setLayout(new java.awt.BorderLayout());

        panelOptStats.setLayout(new java.awt.GridLayout(6, 2, 5, 5));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("Tiempo Total:");
        panelOptStats.add(jLabel1);

        lblOptTiempo.setText("0 s");
        panelOptStats.add(lblOptTiempo);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("RAM Usada:");
        panelOptStats.add(jLabel3);

        lblOptRam.setText("0 KB (0.0%)");
        panelOptStats.add(lblOptRam);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("V-RAM Usada:");
        panelOptStats.add(jLabel5);

        lblOptVRam.setText("0 KB (0.0%)");
        panelOptStats.add(lblOptVRam);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Thrashing:");
        panelOptStats.add(jLabel7);

        lblOptThrashing.setText("0 s (0.0%)");
        panelOptStats.add(lblOptThrashing);

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Fragmentación:");
        panelOptStats.add(jLabel9);

        lblOptFrag.setText("0 B");
        panelOptStats.add(lblOptFrag);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("Procesos Activos:");
        panelOptStats.add(jLabel11);

        lblOptProcesos.setText("0");
        panelOptStats.add(lblOptProcesos);

        panelOpt.add(panelOptStats, java.awt.BorderLayout.NORTH);

        txtOptRam.setEditable(false);
        txtOptRam.setColumns(20);
        txtOptRam.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txtOptRam.setRows(5);
        txtOptRam.setText("Iniciando simulación...");
        scrollOptRam.setViewportView(txtOptRam);

        panelOpt.add(scrollOptRam, java.awt.BorderLayout.CENTER);

        splitPaneSimulacion.setLeftComponent(panelOpt);

        panelUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder("Algoritmo Seleccionado"));
        panelUsuario.setLayout(new java.awt.BorderLayout());

        panelUserStats.setLayout(new java.awt.GridLayout(6, 2, 5, 5));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Tiempo Total:");
        panelUserStats.add(jLabel2);

        lblUserTiempo.setText("0 s");
        panelUserStats.add(lblUserTiempo);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("RAM Usada:");
        panelUserStats.add(jLabel4);

        lblUserRam.setText("0 KB (0.0%)");
        panelUserStats.add(lblUserRam);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("V-RAM Usada:");
        panelUserStats.add(jLabel6);

        lblUserVRam.setText("0 KB (0.0%)");
        panelUserStats.add(lblUserVRam);

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("Thrashing:");
        panelUserStats.add(jLabel8);

        lblUserThrashing.setText("0 s (0.0%)");
        panelUserStats.add(lblUserThrashing);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("Fragmentación:");
        panelUserStats.add(jLabel10);

        lblUserFrag.setText("0 B");
        panelUserStats.add(lblUserFrag);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setText("Procesos Activos:");
        panelUserStats.add(jLabel12);

        lblUserProcesos.setText("0");
        panelUserStats.add(lblUserProcesos);

        panelUsuario.add(panelUserStats, java.awt.BorderLayout.NORTH);

        txtUserRam.setEditable(false);
        txtUserRam.setColumns(20);
        txtUserRam.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txtUserRam.setRows(5);
        txtUserRam.setText("Iniciando simulación...");
        scrollUserRam.setViewportView(txtUserRam);

        panelUsuario.add(scrollUserRam, java.awt.BorderLayout.CENTER);

        splitPaneSimulacion.setRightComponent(panelUsuario);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelControles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(splitPaneSimulacion, javax.swing.GroupLayout.DEFAULT_SIZE, 825, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelControles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPaneSimulacion, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>                        

    private void btnPlayPauseActionPerformed(java.awt.event.ActionEvent evt) {                                             
        if (simulationTimer.isRunning()) {
            pauseSimulationVisuals();
        } else {
            playSimulation();
        }
    }                                            

    private void sliderVelocidadStateChanged(javax.swing.event.ChangeEvent evt) {                                             
        int newDelay = sliderVelocidad.getValue();
        // ESTA LÍNEA YA NO DEBE CAUSAR EXCEPCIÓN.
        simulationTimer.setDelay(newDelay);
    }                                            

    private void btnVolverMenuActionPerformed(java.awt.event.ActionEvent evt) {                                              
        // Detener todo
        pauseSimulationVisuals();
        
        // Volver al menú principal
        this.dispose(); // Cierra esta ventana
        menuPrincipal.setVisible(true); // Muestra el menú original
    }                                             

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            // --- CÓDIGO DE PRUEBA ---
            // Necesita un Menú "falso" para volver.
            Menu fakeMenu = new Menu();
            
            Controller testController = new Controller();
            // Prueba en modo "Generar" (filePath = null)
            testController.setupSimulation(new FIFO(), 123L, null, 10, 500);
            
            // Llamada al constructor AHORA requiere la velocidad inicial (200ms por defecto)
            new VentanaSimulacion(fakeMenu, testController, 200).setVisible(true);
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton btnPlayPause;
    private javax.swing.JButton btnVolverMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel lblOptFrag;
    private javax.swing.JLabel lblOptProcesos;
    private javax.swing.JLabel lblOptRam;
    private javax.swing.JLabel lblOptThrashing;
    private javax.swing.JLabel lblOptTiempo;
    private javax.swing.JLabel lblOptVRam;
    private javax.swing.JLabel lblUserFrag;
    private javax.swing.JLabel lblUserProcesos;
    private javax.swing.JLabel lblUserRam;
    private javax.swing.JLabel lblUserThrashing;
    private javax.swing.JLabel lblUserTiempo;
    private javax.swing.JLabel lblUserVRam;
    private javax.swing.JLabel lblVelocidad;
    private javax.swing.JPanel panelControles;
    private javax.swing.JPanel panelOpt;
    private javax.swing.JPanel panelOptStats;
    private javax.swing.JPanel panelUserStats;
    private javax.swing.JPanel panelUsuario;
    private javax.swing.JScrollPane scrollOptRam;
    private javax.swing.JScrollPane scrollUserRam;
    private javax.swing.JSlider sliderVelocidad;
    private javax.swing.JSplitPane splitPaneSimulacion;
    private javax.swing.JTextArea txtOptRam;
    private javax.swing.JTextArea txtUserRam;
    // End of variables declaration                   
}