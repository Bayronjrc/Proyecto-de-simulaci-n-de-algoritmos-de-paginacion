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
import javax.swing.SpinnerListModel;

// --- Imports de Swing para UI ---
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 *
 * @author wess (Revisado y mejorado por IA)
 */
public class Menu extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Menu.class.getName());

    // --- Variables de UI ---
    // No es necesario que sean 'private', pero las mantenemos como en el original
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

    // --- Fuentes y Colores Personalizados ---
    private final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 14);
    private final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 16);
    private final Font FONT_BUTTON_BIG = new Font("SansSerif", Font.BOLD, 16);
    private final Color COLOR_FONDO = new Color(245, 245, 245); // Un gris muy claro
    private final Color COLOR_PANEL = new Color(255, 255, 255); // Blanco
    private final Color COLOR_BOTON_PRIMARIO = new Color(0, 120, 215); // Azul
    private final Color COLOR_TEXTO_BOTON = Color.WHITE;
    private final Insets INSETS_CAMPO = new Insets(5, 5, 5, 5); // Espaciado para GridBag

    /**
     * Creates new form Menu
     */
    public Menu() {
        // 1. Configuración básica de la ventana
        super("Simulador de Paginación - Configuración");
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
        // Usamos un JPanel como contentPane para poder ponerle un borde (padding)
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding general
        contentPane.setBackground(COLOR_FONDO);
        setContentPane(contentPane);

        // --- Panel Central (para los dos paneles de configuración) ---
        JPanel panelCentral = new JPanel(new BorderLayout(0, 10)); // Espacio vertical de 10px
        panelCentral.setOpaque(false); // Transparente, usa el fondo del contentPane

        // --- 1. Construcción del Panel de Fuente ---
        panelFuente = new JPanel(new GridBagLayout());
        panelFuente.setBackground(COLOR_PANEL);
        // Borde con título y padding interno
        TitledBorder tbFuente = BorderFactory.createTitledBorder("Fuente de Instrucciones");
        tbFuente.setTitleFont(FONT_TITLE);
        panelFuente.setBorder(BorderFactory.createCompoundBorder(tbFuente, new EmptyBorder(10, 10, 10, 10)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = INSETS_CAMPO; // Espaciado

        // Fila 0: Radio Cargar Archivo
        radioCargarArchivo = new JRadioButton("Cargar desde Archivo");
        radioCargarArchivo.setFont(FONT_LABEL);
        radioCargarArchivo.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3; // Ocupa 3 columnas
        panelFuente.add(radioCargarArchivo, gbc);

        // Fila 1: Componentes de Archivo
        lblRutaArchivo = new JLabel("Ruta del Archivo:");
        lblRutaArchivo.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; // Resetea
        gbc.weightx = 0; // Etiqueta no se estira
        panelFuente.add(lblRutaArchivo, gbc);

        txtRutaArchivo = new JTextField();
        txtRutaArchivo.setFont(FONT_LABEL);
        txtRutaArchivo.setEditable(false);
        txtRutaArchivo.setColumns(20); // Tamaño sugerido
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0; // Campo de texto se estira
        panelFuente.add(txtRutaArchivo, gbc);

        btnBuscarArchivo = new JButton("Buscar...");
        btnBuscarArchivo.setFont(FONT_LABEL);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0; // Botón no se estira
        panelFuente.add(btnBuscarArchivo, gbc);

        // Fila 2: Radio Generar Nuevo
        radioGenerarNuevo = new JRadioButton("Generar Nueva Simulación");
        radioGenerarNuevo.setFont(FONT_LABEL);
        radioGenerarNuevo.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 5, 5, 5); // Más espacio arriba
        panelFuente.add(radioGenerarNuevo, gbc);

        gbc.insets = INSETS_CAMPO; // Resetea insets

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
        // Hacemos el spinner más alto
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
        TitledBorder tbParams = BorderFactory.createTitledBorder("Parámetros de Simulación");
        tbParams.setTitleFont(FONT_TITLE);
        panelParametros.setBorder(BorderFactory.createCompoundBorder(tbParams, new EmptyBorder(10, 10, 10, 10)));
        
        gbc = new GridBagConstraints(); // Reiniciamos gbc por si acaso
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
        comboAlgoritmo.setPreferredSize(new Dimension(100, 30)); // Más alto
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        panelParametros.add(comboAlgoritmo, gbc);

        // Fila 1: Semilla
        lblSemilla = new JLabel("Semilla (para RND y Generación):");
        lblSemilla.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panelParametros.add(lblSemilla, gbc);

        txtSemilla = new JTextField("123456");
        txtSemilla.setFont(FONT_LABEL);
        txtSemilla.setPreferredSize(new Dimension(100, 30)); // Más alto
        txtSemilla.setColumns(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panelParametros.add(txtSemilla, gbc);
        
        // --- 3. Construcción del Panel de Botón Inferior ---
        JPanel panelBotonSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonSur.setOpaque(false); // Transparente

        btnIniciarSimulacion = new JButton("Iniciar Simulación");
        btnIniciarSimulacion.setFont(FONT_BUTTON_BIG);
        btnIniciarSimulacion.setBackground(COLOR_BOTON_PRIMARIO);
        btnIniciarSimulacion.setForeground(COLOR_TEXTO_BOTON);
        btnIniciarSimulacion.setOpaque(true); // Necesario en algunos LaF para que se vea el fondo
        btnIniciarSimulacion.setBorderPainted(false); // Aspecto más plano
        btnIniciarSimulacion.setFocusPainted(false);
        // Padding interno del botón
        btnIniciarSimulacion.setMargin(new Insets(10, 20, 10, 20)); 
        
        panelBotonSur.add(btnIniciarSimulacion);
        
        // --- 4. Ensamblar Paneles en la Ventana ---
        panelCentral.add(panelFuente, BorderLayout.NORTH);
        panelCentral.add(panelParametros, BorderLayout.CENTER);
        
        contentPane.add(panelCentral, BorderLayout.CENTER);
        contentPane.add(panelBotonSur, BorderLayout.SOUTH);
        
        // --- 5. Añadir Action Listeners ---
        // Usamos lambdas para acciones simples y referencias a métodos para las complejas
        radioCargarArchivo.addActionListener(e -> toggleInputSource(true));
        radioGenerarNuevo.addActionListener(e -> toggleInputSource(false));
        
        btnBuscarArchivo.addActionListener(this::btnBuscarArchivoActionPerformed);
        btnIniciarSimulacion.addActionListener(this::btnIniciarSimulacionActionPerformed);
    }
    
    /**
     * Configura los valores iniciales de los componentes.
     */
    private void setupInitialState() {
        // Configurar los spinners con los valores del PDF
        spinnerProcesos.setModel(new SpinnerListModel(new Integer[]{10, 50, 100}));
        spinnerOperaciones.setModel(new SpinnerListModel(new Integer[]{500, 1000, 5000}));
        
        // Estado inicial de la UI (Cargar por defecto)
        radioCargarArchivo.setSelected(true);
        toggleInputSource(true);
        
        // Ajustar tamaño de la ventana al contenido y centrar
        // setMinimumSize(new Dimension(550, 480)); // Opcional: poner un mínimo
        pack(); // Ajusta la ventana al tamaño de los componentes
        setLocationRelativeTo(null); // Centrar en pantalla
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

    // --- MÉTODOS DE EVENTOS (Sin cambios en la lógica) ---
    
    private void btnBuscarArchivoActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
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
            semilla = System.currentTimeMillis(); // Usar semilla aleatoria si es inválida
            txtSemilla.setText(String.valueOf(semilla));
        }

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
                algoritmo = new RND(); // RND usa la semilla
                break;
        }

        // 4. Configurar el controlador según la fuente de datos
        if (radioCargarArchivo.isSelected()) {
            String rutaArchivo = txtRutaArchivo.getText();
            if (rutaArchivo.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, 
                        "Por favor, seleccione un archivo de simulación.", 
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
            
            // TODO: Agregar lógica para "Descargar archivo generado"
        }

        // 5. Ocultar este menú y mostrar la ventana de simulación
        this.setVisible(false); // Ocultar el menú
        
        // --- ¡LANZAR LA NUEVA VENTANA! ---
        // Se le pasa 'this' (el menú actual) para que la simulación pueda volver
        VentanaSimulacion ventanaSim = new VentanaSimulacion(this, controller);
        ventanaSim.setVisible(true);
    }                                                   

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the System look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* Intentamos establecer el Look and Feel del sistema operativo
         * para que se vea más nativo y moderno que "Nimbus".
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo establecer el System Look and Feel.", ex);
            // Si falla, Java usará el Look and Feel "Metal" por defecto.
            // También podríamos forzar "Nimbus" aquí si quisiéramos.
            /*
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception nimbusEx) {
                 logger.log(java.util.logging.Level.SEVERE, "Tampoco se pudo cargar Nimbus.", nimbusEx);
            }
            */
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Menu().setVisible(true));
    }

    // --- El bloque de Variables de NetBeans ya no es necesario ---
    // ...
    // End of variables declaration                   
}
