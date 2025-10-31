# Documentación del Controlador del Simulador

---

## controladores/InstructionFileHandler.java

### Visión General

Esta clase es una utilidad de backend. Su único propósito es manejar toda la lógica de entrada y salida (I/O) de archivos para la simulación. Se encarga de tres tareas requeridas por el PDF:

1. Cargar un archivo de simulación proporcionado por el usuario (como en el Anexo 1).
2. Generar una nueva lista aleatoria de procesos e instrucciones si el usuario no proporciona un archivo.
3. Guardar la lista de instrucciones generadas en un archivo de texto que el usuario pueda descargar.

### El "Parser Inteligente" (Concepto Clave)

El requisito más complejo que esta clase resuelve es la ambigüedad del Anexo 1.

* El Problema: El Anexo 1 usa punteros "ordinales" (ej. **use(2)** significa "usar el segundo puntero que se creó"). Sin embargo, sus clases de modelo (como **Use.java** y **Delete.java**) esperan un **PID** de proceso y el **ptrID** único y real que la **SymbolTable** asigna.
* La Solución: Esta clase actúa como un "parser inteligente". Mientras lee el archivo (**parseLine()**), mantiene sus propios mapas de traducción temporales.
    1. Lee **new(1, 500)**. Mapea Ordinal 1 -> ptrID 1 y ptrID 1 -> PID 1.
    2. Lee **new(2, 1000)**. Mapea Ordinal 2 -> ptrID 2 y ptrID 2 -> PID 2.
    3. Lee **use(1)**. Traduce Ordinal 1 -> ptrID 1 -> PID 1.
    4. Crea el objeto final: **new Use(1, 1)**.

### Métodos Principales

#### **public SimulationData loadInstructionsFromFile(String filePath)**
* Lee un archivo de texto línea por línea.
* Ignora líneas de comentario (como **$use(1)$**) o números de línea (como **1 new...**).
* Llama a **parseLine()** en cada línea válida para realizar la traducción del "parser inteligente".
* Devuelve un objeto **SimulationData** que contiene dos listas: la secuencia completa de instrucciones intercaladas y la lista de todos los objetos **Process** detectados.

#### **public SimulationData generateProcesses(int P, int N, long seed)**
* Cumple el requisito de generar una simulación aleatoria.
* Crea **P** objetos **Process**.
* Genera aleatoriamente **N-P** instrucciones (**new**, **use**, **delete**), distribuyéndolas entre los procesos.
* Sigue las reglas del PDF, como forzar un **new** si **use** o **delete** se eligen cuando el proceso no tiene punteros.
* Añade una instrucción **kill** al final de la lista de cada proceso.
* Intercala aleatoriamente la lista final de **N** instrucciones.
* Usa la **seed** para inicializar **java.util.Random**, asegurando que la generación sea repetible, como lo exige el requisito de "Repetibilidad del escenario".

#### **public static class SimulationData**
* Es una simple clase interna (struct) usada para devolver dos valores (**List<Instruction>** y **List<Process>**) desde los métodos **load** y **generate**.

---

## controladores/Controller.java

### Visión General

Esta es la clase controladora central de su patrón MVC. Es el "director de orquesta" que conecta la **Vista** (GUI) con toda la lógica de simulación. La Vista solo se comunica con esta clase; nunca debe acceder directamente a la **MMU** o al **InstructionFileHandler**.

### Conceptos Clave

#### 1. Simulación Dual (Requisito Principal)
El **Controller** gestiona la simulación dual requerida por el PDF.
* Mantiene dos instancias de la **MMU**:
    * **private MMU mmuOpt;**
    * **private MMU mmuUser;**
* Cuando **stepSimulation()** es llamado, el **Controller** envía la misma instrucción a ambas MMUs.
* Esto permite a la Vista consultar ambas MMUs (**getMmuOpt()** y **getMmuUser()**) después de cada paso y mostrar sus estadísticas lado a lado.

#### 2. Reloj Simulado vs. Temporizador de GUI
Es vital entender los dos "tiempos" diferentes:
* Reloj Simulado (en la **MMU**): Es una variable **long** que acumula el tiempo de ejecución simulado. Suma **+1s** por un hit de página y **+5s** por un fallo de página (thrashing).
* Temporizador de GUI (**uiRefreshTimer**): Es un **javax.swing.Timer** (o similar). Su único propósito es refrescar la pantalla. Llama a **stepSimulation()** repetidamente (ej. cada 200ms) para que el usuario vea la simulación avanzar. No tiene nada que ver con el reloj simulado de **+1s/+5s**.

### Métodos Principales (Llamados por la Vista)

#### **public void setupSimulation(PageReplacementAlgorithm algorithm, long seed, ...)**
* Quién lo llama: La Vista, cuando el usuario hace clic en "Iniciar Simulación" en la pantalla de preparación.
* Qué hace:
    1. Llama a **fileHandler.loadInstructionsFromFile()** (si se proporciona un archivo) o **fileHandler.generateProcesses()** (si se proporcionan P y N) para obtener el **SimulationData**.
    2. Prepara los dos algoritmos: **new OPT(listaDeInstrucciones)** y el **algorithm** seleccionado por el usuario (ej. **new FIFO()**).
    3. Si el algoritmo es **RND**, le pasa la **seed** para asegurar la repetibilidad.
    4. Crea las dos MMUs: **mmuOpt = new MMU(...)** y **mmuUser = new MMU(...)**, pasando a ambas la misma lista de procesos e instrucciones.
    5. Resetea el índice de la simulación a 0.

#### **public void resumeSimulation()**
* Quién lo llama: La Vista, cuando el usuario hace clic en el botón "Play" o "Reanudar".
* Qué hace: Establece **isPaused = false** e inicia el **uiRefreshTimer**. El **Timer** comenzará a llamar a **stepSimulation()** repetidamente.

#### **public void pauseSimulation()**
* Quién lo llama: La Vista, cuando el usuario hace clic en el botón "Pausa".
* Qué hace: Establece **isPaused = true** y detiene el **uiRefreshTimer**, congelando la simulación.

#### **public void saveGeneratedInstructions(String savePath)**
* Quién lo llama: La Vista, cuando el usuario hace clic en "Descargar archivo generado".
* Qué hace: Simplemente le pasa la ruta de guardado al **fileHandler**.

### Lógica Interna (No llamada por la Vista)

#### **public void stepSimulation()**
* Quién lo llama: El **uiRefreshTimer** (el bucle de refresco de la GUI).
* Qué hace:
    1. Comprueba si la simulación está en pausa o ha terminado.
    2. Obtiene la siguiente instrucción de **fullInstructionSequence** usando **currentInstructionIndex**.
    3. Ejecuta la instrucción en ambas MMUs: **mmuOpt.executeInstruction(inst)** y **mmuUser.executeInstruction(inst)**.
    4. Incrementa el **currentInstructionIndex**.
    5. (Implícitamente) Le dice a la Vista que se actualice. La Vista llamará a los getters.

#### **public MMU getMmuOpt()** y **public MMU getMmuUser()**
* Quién lo llama: La Vista, al final de cada **stepSimulation()**.
* Qué hace: Son "getters" simples. La Vista los usa para obtener los objetos **MMU** actualizados y extraer de ellos todas las estadísticas necesarias para dibujar en la pantalla (tiempo total, % thrashing, uso de RAM/V-RAM, etc.).
