package vista;

// --- Imports ---
import controladores.Controller;
import controladores.FIFO;
import controladores.MRU;
import controladores.PageReplacementAlgorithm;
import controladores.RND;
import controladores.SC;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SpinnerNumberModel; 

// --- Imports de Swing para UI ---
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Clase que configura la simulación.
 * Se han aplicado todas las mejoras: límites ampliados, slider de velocidad y botón plano.
 */
public class Menu extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Menu.class.getName());

    // --- Variables de UI ---
    JButton btnBuscarArchivo;
    ButtonGroup btnGroupFuente;
    JButton btnIniciarSimulacion;
    JComboBox<String> comboAlgoritmo;
    JLabel lblAlgoritmo;
    JLabel lblOperaciones;
    JLabel lblProcesos;
    JLabel lblRutaArchivo;
    JLabel lblSemilla;
    JPanel panelFuente;
    JPanel panelParametros;
    JRadioButton radioCargarArchivo;
    JRadioButton radioGenerarNuevo;
    JSpinner spinnerOperaciones;
    JSpinner spinnerProcesos;
    JTextField txtRutaArchivo;
    JTextField txtSemilla;
    
    JLabel lblVelocidad;
    JSlider sliderVelocidad;

    // --- Fuentes y Colores Personalizados ---
    private final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 14);
    private final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 16);
    private final Font FONT_BUTTON_BIG = new Font("SansSerif", Font.BOLD, 16);
    private final Color COLOR_FONDO = new Color(245, 245, 245); 
    private final Color COLOR_PANEL = new Color(255, 255, 255); 
    private final Color COLOR_BOTON_PRIMARIO = new Color(0, 120, 215); 
    private final Color COLOR_TEXTO_BOTON = Color.WHITE;
    private final Insets INSETS_CAMPO = new Insets(5, 5, 5, 5); 

    /**
     * Creates new form Menu
     */
    public Menu() {
        // 1. Configuración básica de la ventana
        super("Simulador de Paginaci\u00f3n - Configuraci\u00f3n");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 2. Inicializar y construir la UI
        initUIComponents();
        
        // 3. Configuración inicial de lógica
        setupInitialState();
    }
    
    /**
     * Inicializa y organiza todos los componentes de Swing manualmente.
     */
    private void initUIComponents() {
        // --- Panel Principal ---
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15)); 
        contentPane.setBackground(COLOR_FONDO);
        setContentPane(contentPane);

        // --- Panel Central (para los dos paneles de configuración) ---
        JPanel panelCentral = new JPanel(new BorderLayout(0, 10)); 
        panelCentral.setOpaque(false); 

        // --- 1. Construcción del Panel de Fuente ---
        panelFuente = new JPanel(new GridBagLayout());
        panelFuente.setBackground(COLOR_PANEL);
        TitledBorder tbFuente = BorderFactory.createTitledBorder("Fuente de Instrucciones");
        tbFuente.setTitleFont(FONT_TITLE);
        panelFuente.setBorder(BorderFactory.createCompoundBorder(tbFuente, new EmptyBorder(10, 10, 10, 10)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = INSETS_CAMPO; 

        // Fila 0: Radio Cargar Archivo
        radioCargarArchivo = new JRadioButton("Cargar desde Archivo");
        radioCargarArchivo.setFont(FONT_LABEL);
        radioCargarArchivo.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3; 
        panelFuente.add(radioCargarArchivo, gbc);

        // Fila 1: Componentes de Archivo
        lblRutaArchivo = new JLabel("Ruta del Archivo:");
        lblRutaArchivo.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; 
        gbc.weightx = 0; 
        panelFuente.add(lblRutaArchivo, gbc);

        txtRutaArchivo = new JTextField();
        txtRutaArchivo.setFont(FONT_LABEL);
        txtRutaArchivo.setEditable(false);
        txtRutaArchivo.setColumns(20); 
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0; 
        panelFuente.add(txtRutaArchivo, gbc);

        btnBuscarArchivo = new JButton("Buscar...");
        btnBuscarArchivo.setFont(FONT_LABEL);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0; 
        panelFuente.add(btnBuscarArchivo, gbc);

        // Fila 2: Radio Generar Nuevo
        radioGenerarNuevo = new JRadioButton("Generar Nueva Simulaci\u00f3n");
        radioGenerarNuevo.setFont(FONT_LABEL);
        radioGenerarNuevo.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 5, 5, 5); 
        panelFuente.add(radioGenerarNuevo, gbc);

        gbc.insets = INSETS_CAMPO; 

        // Fila 3: Generar Procesos
        lblProcesos = new JLabel("Procesos (P):");
        lblProcesos.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panelFuente.add(lblProcesos, gbc);

        spinnerProcesos = new JSpinner();
        spinnerProcesos.setFont(FONT_LABEL);
        spinnerProcesos.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panelFuente.add(spinnerProcesos, gbc);
        
        // Fila 4: Generar Operaciones
        lblOperaciones = new JLabel("Operaciones (N):");
        lblOperaciones.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panelFuente.add(lblOperaciones, gbc);
        
        spinnerOperaciones = new JSpinner();
        spinnerOperaciones.setFont(FONT_LABEL);
        spinnerOperaciones.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panelFuente.add(spinnerOperaciones, gbc);

        // Agrupar los radio buttons
        btnGroupFuente = new ButtonGroup();
        btnGroupFuente.add(radioCargarArchivo);
        btnGroupFuente.add(radioGenerarNuevo);

        // --- 2. Construcción del Panel de Parámetros ---
        panelParametros = new JPanel(new GridBagLayout());
        panelParametros.setBackground(COLOR_PANEL);
        TitledBorder tbParams = BorderFactory.createTitledBorder("Par\u00e1metros de Simulaci\u00f3n");
        tbParams.setTitleFont(FONT_TITLE);
        panelParametros.setBorder(BorderFactory.createCompoundBorder(tbParams, new EmptyBorder(10, 10, 10, 10)));
        
        gbc = new GridBagConstraints(); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = INSETS_CAMPO;

        // Fila 0: Algoritmo
        lblAlgoritmo = new JLabel("Algoritmo a Simular:");
        lblAlgoritmo.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panelParametros.add(lblAlgoritmo, gbc);
        
        comboAlgoritmo = new JComboBox<>(new String[] { "FIFO", "SC", "MRU", "RND" });
        comboAlgoritmo.setFont(FONT_LABEL);
        comboAlgoritmo.setPreferredSize(new Dimension(100, 30)); 
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        panelParametros.add(comboAlgoritmo, gbc);

        // Fila 1: Semilla
        lblSemilla = new JLabel("Semilla (para RND y Generaci\u00f3n):");
        lblSemilla.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panelParametros.add(lblSemilla, gbc);

        txtSemilla = new JTextField("123456");
        txtSemilla.setFont(FONT_LABEL);
        txtSemilla.setPreferredSize(new Dimension(100, 30)); 
        txtSemilla.setColumns(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panelParametros.add(txtSemilla, gbc);
        
        // Fila 2: SLIDER DE VELOCIDAD
        lblVelocidad = new JLabel("Velocidad Inicial (ms):");
        lblVelocidad.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panelParametros.add(lblVelocidad, gbc);

        sliderVelocidad = new JSlider();
        sliderVelocidad.setMajorTickSpacing(250);
        sliderVelocidad.setMaximum(2000);
        sliderVelocidad.setMinimum(6); // Permite 12ms
        sliderVelocidad.setMinorTickSpacing(13); 
        sliderVelocidad.setPaintTicks(true);
        sliderVelocidad.setToolTipText("Tiempo en ms por paso (12ms - 2000ms)");
        sliderVelocidad.setValue(200);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        panelParametros.add(sliderVelocidad, gbc);

        
        // --- 3. Construcción del Panel de Botón Inferior ---
        JPanel panelBotonSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonSur.setOpaque(false); 

        btnIniciarSimulacion = new JButton("Iniciar Simulaci\u00f3n");
        btnIniciarSimulacion.setFont(FONT_BUTTON_BIG);
        btnIniciarSimulacion.setBackground(COLOR_BOTON_PRIMARIO);
        btnIniciarSimulacion.setForeground(COLOR_TEXTO_BOTON);
        btnIniciarSimulacion.setOpaque(true); 
        
        // --- CÓDIGO CLAVE PARA ELIMINAR EFECTOS DE HOVER/FOCUS ---
        btnIniciarSimulacion.setBorderPainted(false); // Elimina el borde de 3D
        btnIniciarSimulacion.setFocusPainted(false); // Elimina el borde de enfoque (focus)
        
        // Esto es esencial en muchos LaFs, previene el efecto visual de "presionado" o "rollover" 
        // cuando se usan colores de fondo personalizados.
        btnIniciarSimulacion.setContentAreaFilled(true); 
        btnIniciarSimulacion.setRolloverEnabled(false); 
        // ---------------------------------------------------------
        
        btnIniciarSimulacion.setMargin(new Insets(10, 20, 10, 20)); 
        
        panelBotonSur.add(btnIniciarSimulacion);
        
        // --- 4. Ensamblar Paneles en la Ventana ---
        panelCentral.add(panelFuente, BorderLayout.NORTH);
        panelCentral.add(panelParametros, BorderLayout.CENTER);
        
        contentPane.add(panelCentral, BorderLayout.CENTER);
        contentPane.add(panelBotonSur, BorderLayout.SOUTH);
        
        // --- 5. Añadir Action Listeners ---
        radioCargarArchivo.addActionListener(e -> toggleInputSource(true));
        radioGenerarNuevo.addActionListener(e -> {
            toggleInputSource(false);
        });
        
        btnBuscarArchivo.addActionListener(this::btnBuscarArchivoActionPerformed);
        btnIniciarSimulacion.addActionListener(this::btnIniciarSimulacionActionPerformed);
    }
    
    /**
     * Configura los valores iniciales de los componentes.
     */
    private void setupInitialState() {
        // Usar SpinnerNumberModel para permitir cualquier valor
        // (Valor Inicial, Mínimo, Máximo, Step)
        spinnerProcesos.setModel(new SpinnerNumberModel(10, 1, 1000, 1));
        spinnerOperaciones.setModel(new SpinnerNumberModel(500, 10, 100000, 10));
        
        // Estado inicial de la UI (Cargar por defecto)
        radioCargarArchivo.setSelected(true);
        toggleInputSource(true);
        
        // Ajustar tamaño de la ventana al contenido y centrar
        pack(); 
        setLocationRelativeTo(null); 
    }

    /**
     * Habilita/Deshabilita los paneles de entrada según la selección del radio button.
     * @param isFileMode true si "Cargar Archivo" está seleccionado, false si "Generar" lo está.
     */
    private void toggleInputSource(boolean isFileMode) {
        // Controles de "Cargar Archivo"
        lblRutaArchivo.setEnabled(isFileMode);
        txtRutaArchivo.setEnabled(isFileMode);
        btnBuscarArchivo.setEnabled(isFileMode);

        // Controles de "Generar Simulación"
        lblProcesos.setEnabled(!isFileMode);
        spinnerProcesos.setEnabled(!isFileMode);
        lblOperaciones.setEnabled(!isFileMode);
        spinnerOperaciones.setEnabled(!isFileMode);
    }
    
    // --- MÉTODOS DE EVENTOS ---
    
    private void btnBuscarArchivoActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Seleccionar Archivo de Instrucciones");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            txtRutaArchivo.setText(selectedFile.getAbsolutePath());
        }
    }                                                

    private void btnIniciarSimulacionActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        // 1. Crear el Controlador
        Controller controller = new Controller();

        // 2. Recolectar Parámetros de Simulación
        String algNombre = (String) comboAlgoritmo.getSelectedItem();
        long semilla;
        try {
            semilla = Long.parseLong(txtSemilla.getText());
        } catch (NumberFormatException e) {
            semilla = System.currentTimeMillis(); 
            txtSemilla.setText(String.valueOf(semilla));
        }
        
        // OBTENER VELOCIDAD
        int velocidadInicialMs = sliderVelocidad.getValue();
        
        // 3. Instanciar el algoritmo seleccionado
        PageReplacementAlgorithm algoritmo;
        switch (algNombre) {
            case "FIFO":
                algoritmo = new FIFO();
                break;
            case "SC":
                algoritmo = new SC();
                break;
            case "MRU":
                algoritmo = new MRU();
                break;
            case "RND":
            default:
                algoritmo = new RND(); 
                break;
        }
        
        // 4. Configurar el controlador según la fuente de datos
        try {
            if (radioCargarArchivo.isSelected()) {
                String rutaArchivo = txtRutaArchivo.getText();
                if (rutaArchivo.isEmpty()) {
                    javax.swing.JOptionPane.showMessageDialog(this, 
                            "Por favor, seleccione un archivo de simulaci\u00f3n.", 
                            "Error", 
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
                controller.setupSimulation(algoritmo, semilla, rutaArchivo, 0, 0);
            } else {
                // Generar nuevo
                int P = (int) spinnerProcesos.getValue();
                int N = (int) spinnerOperaciones.getValue();
                controller.setupSimulation(algoritmo, semilla, null, P, N);
            }
        } catch (Exception e) {
             javax.swing.JOptionPane.showMessageDialog(this, 
                    "Error al configurar la simulaci\u00f3n:\n" + e.getMessage(), 
                    "Error Cr\u00edtico", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
             return;
        }

        // 5. Ocultar este menú y mostrar la ventana de simulación
        this.setVisible(false); 
        
        // LANZAR LA NUEVA VENTANA, pasando la velocidad inicial
        VentanaSimulacion ventanaSim = new VentanaSimulacion(this, controller, velocidadInicialMs);
        ventanaSim.setVisible(true);
    }                                                   

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the System look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo establecer el System Look and Feel.", ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Menu().setVisible(true));
    }
                  
}